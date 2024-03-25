package me.fckng0d.audioservicebackend.services;

import me.fckng0d.audioservicebackend.models.AudioFile;
import me.fckng0d.audioservicebackend.models.Image;
import me.fckng0d.audioservicebackend.models.Playlist;
import me.fckng0d.audioservicebackend.repositories.AudioFileRepository;
import me.fckng0d.audioservicebackend.repositories.ImageRepository;
import me.fckng0d.audioservicebackend.repositories.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@Service
public class PlaylistService {
    private final AudioFileRepository audioFileRepository;
    private final PlaylistRepository playlistRepository;
    private final ImageRepository imageRepository;

    private final Semaphore semaphore = new Semaphore(1);


    @Autowired
    public PlaylistService(AudioFileRepository audioFileRepository, PlaylistRepository playlistRepository, ImageRepository imageRepository) {
        this.audioFileRepository = audioFileRepository;
        this.playlistRepository = playlistRepository;
        this.imageRepository = imageRepository;
    }

    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }

    //    @Transactional
    @Transactional(readOnly = true)
//    @Cacheable(cacheNames="playlist")
    public Playlist getPlaylistById(UUID id) {
        return playlistRepository.getPlaylistsById(id);
    }

    public void createNewPlaylist(String name, String author, MultipartFile imageFile) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setAuthor(author);
        playlist.setDuration(0f);
        playlist.setCountOfAudio(0);

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
    public void updatePlaylist(UUID id, List<AudioFile> updatedAudioFiles) {
        try {
            semaphore.acquire();

            Playlist existingPlaylist = playlistRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException());

            System.out.println("\n### обновлено ###");
            List<AudioFile> newAudioFiles = new ArrayList<>();
            for (AudioFile updatedAudioFile : updatedAudioFiles) {
                AudioFile existingAudioFile = existingPlaylist.getAudioFiles().stream()
                        .filter(audioFile -> audioFile.getId().equals(updatedAudioFile.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("AudioFile not found"));

                newAudioFiles.add(existingAudioFile);
                System.out.println(updatedAudioFile.getTitle());

            }

            existingPlaylist.setAudioFiles(newAudioFiles);

            playlistRepository.save(existingPlaylist);
            System.out.println();

            playlistRepository.save(existingPlaylist);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }
}
