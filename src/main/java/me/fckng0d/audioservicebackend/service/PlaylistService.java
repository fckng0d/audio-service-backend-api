package me.fckng0d.audioservicebackend.service;

import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.PlaylistDTO;
import me.fckng0d.audioservicebackend.model.AudioFile;
import me.fckng0d.audioservicebackend.model.Image;
import me.fckng0d.audioservicebackend.model.Playlist;
import me.fckng0d.audioservicebackend.model.enums.PlayListOwnerEnum;
import me.fckng0d.audioservicebackend.repositoriy.AudioFileRepository;
import me.fckng0d.audioservicebackend.repositoriy.ImageRepository;
import me.fckng0d.audioservicebackend.repositoriy.PlaylistRepository;
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
@RequiredArgsConstructor
public class PlaylistService {
    private final AudioFileRepository audioFileRepository;
    private final PlaylistRepository playlistRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;

    private final Map<UUID, Semaphore> audioFilesSemaphores = new ConcurrentHashMap<>();
    //    private final Semaphore audioFilesSemaphore = new Semaphore(1);
    private final Semaphore playlistsSemaphore = new Semaphore(1);
    private final AtomicInteger countOfRequests = new AtomicInteger(0);

    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll(Sort.by("orderIndex"));
    }

    //    @Transactional
    @Transactional(readOnly = true)
//    @Cacheable(cacheNames="playlist")
    public Optional<Playlist> getPlaylistById(UUID id) {
        return playlistRepository.getPlaylistsById(id);
    }


    public PlaylistDTO convertToDTO(Playlist playlist) {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setId(playlist.getId());
        dto.setName(playlist.getName());
        dto.setAuthor(playlist.getAuthor());
        dto.setOwnerUsername(playlist.getOwnerUsername());
        dto.setPlaylistOwnerRole(playlist.getPlaylistOwnerRole());
        dto.setCountOfAudio(playlist.getCountOfAudio());
        dto.setDuration(playlist.getDuration());
        dto.setImage(playlist.getImage());
        dto.setOrderIndex(playlist.getOrderIndex());

        return dto;
    }

    public List<PlaylistDTO> convertListToDTOs(List<Playlist> playlists) {
        List<PlaylistDTO> playlistDTOS = playlists.stream()
                .map(this::convertToDTO)
                .toList();

        return playlistDTOS;
    }


    @Transactional
    public Playlist createNewPlaylist(String name, String author, String ownerUsername,
                                      PlayListOwnerEnum playlistOwnerRole, MultipartFile imageFile) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setAuthor(author);
        playlist.setOwnerUsername(ownerUsername);
        playlist.setPlaylistOwnerRole(playlistOwnerRole);
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
            return playlistRepository.save(playlist);
        }

        return null;
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
    public void updatePlaylistName(UUID playlistId, String newPlaylistName) {
        Optional<Playlist> playlistOptional = playlistRepository.getPlaylistsById(playlistId);

        if (playlistOptional.isPresent()) {
            Playlist playlist = playlistOptional.get();
            playlist.setName(newPlaylistName);
            playlistRepository.save(playlist);
        }
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
    public void updatePlaylistAudioFilesOrder(UUID id, List<AudioFile> updatedAudioFiles) throws InterruptedException {
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
            audioFilesSemaphore.release();
            if (audioFilesSemaphore.availablePermits() == 1) {
                audioFilesSemaphores.remove(id);
            }
        }
    }

    @Transactional
    public void deleteAudioFile(UUID playlistId, UUID audioFileId) throws InterruptedException {
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

            existingPlaylist.getAudioFiles().remove(existingAudioFile);

            existingPlaylist.setCountOfAudio(existingPlaylist.getCountOfAudio() - 1);
            existingPlaylist.setDuration(existingPlaylist.getDuration() - existingAudioFile.getDuration());
            playlistRepository.save(existingPlaylist);
            existingAudioFile.getPlaylists().remove(existingPlaylist);
            audioFileRepository.save(existingAudioFile);
        } finally {
            audioFilesSemaphore.release();
            if (audioFilesSemaphore.availablePermits() == 1) {
                audioFilesSemaphores.remove(playlistId);
            }
        }
    }
}
