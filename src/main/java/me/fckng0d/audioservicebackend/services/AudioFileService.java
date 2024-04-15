package me.fckng0d.audioservicebackend.services;

import me.fckng0d.audioservicebackend.models.AudioFile;
import me.fckng0d.audioservicebackend.models.Image;
import me.fckng0d.audioservicebackend.models.Playlist;
import me.fckng0d.audioservicebackend.repositories.AudioFileRepository;
import me.fckng0d.audioservicebackend.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Service
public class AudioFileService {
    private final AudioFileRepository audioFileRepository;
    private final ImageRepository imageRepository;
    private final Map<UUID, Semaphore> audioFileSemaphores = new ConcurrentHashMap<>();

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
//    @Cacheable("audio_file")
    public Optional<AudioFile> getAudioFileById(UUID id) {
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

    @Transactional
    public void incrementCountOfAuditions(UUID id) throws InterruptedException {
        Semaphore audioFileSemaphore = audioFileSemaphores.computeIfAbsent(id, k -> new Semaphore(1));
        audioFileSemaphore.acquire();
        try {

            Optional<AudioFile> audioFileOptional = audioFileRepository.getAudioFileById(id);

            if (audioFileOptional.isPresent()) {
                AudioFile audioFile = audioFileOptional.get();

                audioFile.setCountOfAuditions(audioFile.getCountOfAuditions() + 1);
                audioFileRepository.save(audioFile);
            }
        } finally {
            audioFileSemaphore.release();
            if (audioFileSemaphore.availablePermits() == 1) {
                audioFileSemaphores.remove(id);
            }
        }
    }
}
