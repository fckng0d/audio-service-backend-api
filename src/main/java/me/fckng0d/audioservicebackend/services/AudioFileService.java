package me.fckng0d.audioservicebackend.services;

import me.fckng0d.audioservicebackend.models.AudioFile;
import me.fckng0d.audioservicebackend.models.Image;
import me.fckng0d.audioservicebackend.models.Playlist;
import me.fckng0d.audioservicebackend.repositories.AudioFileRepository;
import me.fckng0d.audioservicebackend.repositories.ImageRepository;
import me.fckng0d.audioservicebackend.repositories.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AudioFileService {
    private final AudioFileRepository audioFileRepository;
    private final ImageRepository imageRepository;

    @Autowired
    public AudioFileService(AudioFileRepository audioFileRepository, ImageRepository imageRepository) {
        this.audioFileRepository = audioFileRepository;
        this.imageRepository = imageRepository;
    }

    //    @Transactional(propagation = Propagation.REQUIRED)
    @Transactional
    public void saveAudioFile(MultipartFile audioFile, MultipartFile imageFile,
                              String title, String author, List<String> genres, Float duration) {
        AudioFile audio = new AudioFile();
        audio.setFileName(audioFile.getOriginalFilename());
        audio.setTitle(title);
        audio.setAuthor(author);
        audio.setGenres(genres);
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
            System.out.println(image.getImageId());
            audio.setImage(image);
        }

//        Playlist playlist = playlistService.getPlaylistById(2L);
//        audio.getPlaylists().add(playlist); // Добавляем плейлист к аудиофайлу
        audioFileRepository.save(audio); // Сначала сохраняем аудиофайл
//        playlist.getAudioFiles().add(audio);
//        playlistRepository.save(playlist); // Затем сохраняем плейлист
    }


    public List<AudioFile> getAllAudioFiles() {
        return audioFileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AudioFile getAudioFileById(UUID id) {
        return audioFileRepository.getAudioFileById(id);
    }

    public List<AudioFile> getAllAudioFilesByPlaylistId(Long playlistId) {
        List<AudioFile> audioFiles = new ArrayList<>();
        for (AudioFile audioFile : getAllAudioFiles()) {
            for (Playlist playlist : audioFile.getPlaylists()) {
                if (Objects.equals(playlist.getId(), playlistId)) {
                    audioFiles.add(audioFile);
                }
            }
        }
        return audioFiles;
    }
}