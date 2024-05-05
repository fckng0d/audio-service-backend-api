package me.fckng0d.audioservicebackend.service;

import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.AudioFileDTO;
import me.fckng0d.audioservicebackend.model.AudioData;
import me.fckng0d.audioservicebackend.model.AudioFile;
import me.fckng0d.audioservicebackend.model.Image;
import me.fckng0d.audioservicebackend.repositoriy.AudioDataRepository;
import me.fckng0d.audioservicebackend.repositoriy.AudioFileRepository;
import me.fckng0d.audioservicebackend.repositoriy.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
public class AudioFileService {
    private final AudioFileRepository audioFileRepository;
    private final AudioDataRepository audioDataRepository;
    private final ImageRepository imageRepository;
    private final Map<UUID, Semaphore> audioFileSemaphores = new ConcurrentHashMap<>();


    public AudioFileDTO convertToDTO(AudioFile audioFile) {
        AudioFileDTO dto = new AudioFileDTO();
        dto.setId(audioFile.getId());
            // dto.setFileName(audioFile.getFileName());
        dto.setTitle(audioFile.getTitle());
        dto.setAuthor(audioFile.getAuthor());
        dto.setDuration(audioFile.getDuration());
        dto.setCountOfAuditions(audioFile.getCountOfAuditions());
            // dto.setGenres(audioFile.getGenres());
        dto.setImage(audioFile.getImage());
        return dto;
    }

    public List<AudioFileDTO> convertListToDTOs(List<AudioFile> audioFiles) {
        List<AudioFileDTO> audioFileDTOS = audioFiles.stream()
                .map(this::convertToDTO)
                .toList();

        return audioFileDTOS;
    }

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
            AudioData audioData = new AudioData();
            audioData.setData(audioFile.getBytes());
            audioDataRepository.save(audioData);

            audio.setAudioData(audioData);
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

    @Transactional
    public void save(AudioFile audioFile) {
        audioFileRepository.save(audioFile);
    }

//    public List<AudioFile> getAllAudioFilesByPlaylistId(Long playlistId) {
//        List<AudioFile> audioFiles = new ArrayList<>();
//        for (AudioFile audioFile : getAllAudioFiles()) {
//            for (Playlist playlist : audioFile.getPlaylists()) {
//                if (Objects.equals(playlist.getId(), playlistId)) {
//                    audioFiles.add(audioFile);
//                }
//            }
//        }
//        return audioFiles;
//    }

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
