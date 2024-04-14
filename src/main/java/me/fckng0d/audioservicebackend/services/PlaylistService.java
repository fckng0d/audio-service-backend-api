package me.fckng0d.audioservicebackend.services;

import me.fckng0d.audioservicebackend.DTO.UpdatedPlaylistOrderIndexesDto;
import me.fckng0d.audioservicebackend.models.AudioFile;
import me.fckng0d.audioservicebackend.models.Image;
import me.fckng0d.audioservicebackend.models.Playlist;
import me.fckng0d.audioservicebackend.repositories.AudioFileRepository;
import me.fckng0d.audioservicebackend.repositories.ImageRepository;
import me.fckng0d.audioservicebackend.repositories.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PlaylistService {
    private final AudioFileRepository audioFileRepository;
    private final PlaylistRepository playlistRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;

    private final Map<UUID, Semaphore> audioFilesSemaphores = new ConcurrentHashMap<>();
    //    private final Semaphore audioFilesSemaphore = new Semaphore(1);
    private final Semaphore playlistsSemaphore = new Semaphore(1);
    private final AtomicInteger countOfRequests = new AtomicInteger(0);


    @Autowired
    public PlaylistService(AudioFileRepository audioFileRepository, PlaylistRepository playlistRepository, ImageRepository imageRepository, ImageService imageService) {
        this.audioFileRepository = audioFileRepository;
        this.playlistRepository = playlistRepository;
        this.imageRepository = imageRepository;
        this.imageService = imageService;
    }

    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll(Sort.by("orderIndex"));
    }

    //    @Transactional
    @Transactional(readOnly = true)
//    @Cacheable(cacheNames="playlist")
    public Optional<Playlist> getPlaylistById(UUID id) {
        return playlistRepository.getPlaylistsById(id);
    }

    public void createNewPlaylist(String name, String author, MultipartFile imageFile) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setAuthor(author);
        playlist.setDuration(0f);
        playlist.setCountOfAudio(0);

        playlist.setOrderIndex((int) playlistRepository.count());

        if (imageFile != null) {
            Image image = new Image();
            image.setFileName(imageFile.getOriginalFilename());
            try {
                image.setData(imageFile.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            imageRepository.save(image);
            playlist.setImage(image);
            playlistRepository.save(playlist);
        }
    }


    //    @CacheEvict(cacheNames="playlist")
    public void addAudioFile(Playlist playlist, MultipartFile audioFile, MultipartFile imageFile,
                             String title, String author, List<String> genres, Float duration) {
        AudioFile audio = new AudioFile();
        audio.setFileName(audioFile.getOriginalFilename());
        audio.setTitle(title);
        audio.setAuthor(author);
//        audio.setGenres(genres);
        audio.setDuration(duration);

        try {
            audio.setData(audioFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (imageFile != null) {
            Image image = new Image();
            image.setFileName(imageFile.getOriginalFilename());
            try {
                image.setData(imageFile.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            imageRepository.save(image);
            audio.setImage(image);
        }

        playlist.setCountOfAudio(playlist.getCountOfAudio() + 1);
        playlist.setDuration(playlist.getDuration() + duration);

        audio.getPlaylists().add(playlist);

        audioFileRepository.save(audio);
        playlist.getAudioFiles().add(audio);
        playlistRepository.save(playlist);
    }

    @Transactional
    public Image updatePlaylisImage(UUID playlistId, MultipartFile playlistImage) {
        Optional<Playlist> playlistOptional = playlistRepository.getPlaylistsById(playlistId);

        if (playlistOptional.isPresent()) {
            Playlist playlist = playlistOptional.get();

            imageService.deleteImage(playlist.getImage());
            Image newPlaylistImage = imageService.saveImage(playlistImage);
            playlist.setImage(newPlaylistImage);
            playlistRepository.save(playlist);
            return newPlaylistImage;
        }

        return null;
    }

    @Transactional
    public Image getPlaylistImage(UUID playlistId) {
        Optional<Playlist> playlistOptional = playlistRepository.getPlaylistsById(playlistId);

        if (playlistOptional.isPresent()) {
            Playlist playlist = playlistOptional.get();
            return playlist.getImage();
        }
        return null;
    }

    @Transactional
    public void updatePlaylistsOrder(List<UpdatedPlaylistOrderIndexesDto> updatedWithIndexes) throws InterruptedException {
//        countOfRequests.incrementAndGet();
        playlistsSemaphore.acquire();
        try {
            List<Playlist> oldPlaylists = playlistRepository.findAll();

            oldPlaylists.forEach(oldPlaylist -> {
                UpdatedPlaylistOrderIndexesDto updatedPlaylist = updatedWithIndexes.stream()
                        .filter(playlist -> playlist.getId().equals(oldPlaylist.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));

                oldPlaylist.setOrderIndex(updatedPlaylist.getOrderIndex());
                playlistRepository.save(oldPlaylist);
            });
        } finally {
//            countOfRequests.decrementAndGet();
            playlistsSemaphore.release();
        }
    }

    @Transactional
    public void updatePlaylist(UUID id, List<AudioFile> updatedAudioFiles) throws InterruptedException {
        countOfRequests.incrementAndGet();
        Semaphore audioFilesSemaphore = audioFilesSemaphores.computeIfAbsent(id, k -> new Semaphore(1));
        audioFilesSemaphore.acquire();
        try {
            Playlist existingPlaylist = playlistRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException());

            List<AudioFile> newAudioFiles = new ArrayList<>();
            for (AudioFile updatedAudioFile : updatedAudioFiles) {
                AudioFile existingAudioFile = existingPlaylist.getAudioFiles().stream()
                        .filter(audioFile -> audioFile.getId().equals(updatedAudioFile.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("AudioFile not found"));

                newAudioFiles.add(existingAudioFile);

            }

            existingPlaylist.setAudioFiles(newAudioFiles);

            playlistRepository.save(existingPlaylist);
        } finally {
            countOfRequests.decrementAndGet();
            audioFilesSemaphore.release();
            if (audioFilesSemaphore.availablePermits() == 1) {
                audioFilesSemaphores.remove(id);
            }
        }
    }

    public boolean hasQueuedThreads() {
        return countOfRequests.get() == 0;
    }

    @Transactional
    public void deleteAudioFile(UUID playlistId, UUID audioFileId) throws InterruptedException {
        countOfRequests.incrementAndGet();
        Semaphore audioFilesSemaphore = audioFilesSemaphores.computeIfAbsent(playlistId, k -> new Semaphore(1));
        audioFilesSemaphore.acquire();
        try {
            Playlist existingPlaylist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new RuntimeException("Playlist not found"));

            AudioFile existingAudioFile = audioFileRepository.getAudioFileById(audioFileId)
                    .orElseThrow(() -> new IllegalArgumentException("AudioFile not found"));

            if (!existingPlaylist.getAudioFiles().contains(existingAudioFile)) {
                throw new IllegalArgumentException("AudioFile is not in the playlist");
            }

//            System.out.println(existingAudioFile.getId());

            existingPlaylist.getAudioFiles().remove(existingAudioFile);

//            System.out.println("количество в списке: " + existingPlaylist.getAudioFiles().size());

            existingPlaylist.setCountOfAudio(existingPlaylist.getCountOfAudio() - 1);
            existingPlaylist.setDuration(existingPlaylist.getDuration() - existingAudioFile.getDuration());
            playlistRepository.save(existingPlaylist);
            existingAudioFile.getPlaylists().remove(existingPlaylist);
            audioFileRepository.save(existingAudioFile);
        } finally {
            countOfRequests.decrementAndGet();
            audioFilesSemaphore.release();
            if (audioFilesSemaphore.availablePermits() == 1) {
                audioFilesSemaphores.remove(playlistId);
            }
        }
    }
}
