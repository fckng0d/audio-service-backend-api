package me.fckng0d.audioservicebackend.controller;

import me.fckng0d.audioservicebackend.DTO.AudioFileDTO;
import me.fckng0d.audioservicebackend.model.AudioFile;
import me.fckng0d.audioservicebackend.model.Image;
import me.fckng0d.audioservicebackend.service.AudioFileService;
import me.fckng0d.audioservicebackend.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "${cross-origin}", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class AudioFileController {

    private final AudioFileService audioFileService;
    private final ImageService imageService;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    public AudioFileController(AudioFileService audioFileService, ImageService imageService, PlatformTransactionManager transactionManager) {
        this.audioFileService = audioFileService;
        this.imageService = imageService;
        this.transactionManager = transactionManager;
    }

    @GetMapping("/audio")
    public ResponseEntity<List<AudioFileDTO>> getAllAudioFiles() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            List<AudioFile> audioFiles = audioFileService.getAllAudioFiles();

            transactionManager.commit(status);

            if (!audioFiles.isEmpty()) {

                List<AudioFileDTO> audioFileDTOs = audioFiles.stream()
                        .map(audioFile -> {
                            AudioFileDTO dto = new AudioFileDTO();
                            dto.setId(audioFile.getId());
//                            dto.setFileName(audioFile.getFileName());
                            dto.setTitle(audioFile.getTitle());
                            dto.setAuthor(audioFile.getAuthor());
                            dto.setDuration(audioFile.getDuration());
//                            dto.setGenres(audioFile.getGenres());
                            dto.setImage(audioFile.getImage());
                            return dto;
                        })
                        .collect(Collectors.toList());

                return new ResponseEntity<>(audioFileDTOs, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            transactionManager.rollback(status);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/audio/{id}")
//    @Cacheable("audio_file")
    public ResponseEntity<byte[]> getAudioFile(@PathVariable UUID id) {
        try {
            AudioFile audioFile = audioFileService.getAudioFileById(id)
                    .orElseThrow(() -> new IllegalArgumentException("AudioFile not found"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(audioFile.getData().length);
            headers.set("Accept-Ranges", "bytes");

//                String encodedFileName = Base64.getEncoder()
//                        .encodeToString(audioFile.getFileName().getBytes(StandardCharsets.UTF_8));
//
//                headers.setContentDispositionFormData("attachment", encodedFileName);


            return new ResponseEntity<>(audioFile.getData(), headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //    @Cacheable("audioImages")
    @GetMapping("/audio/{id}/image")
    public ResponseEntity<byte[]> getAudioImage(@PathVariable UUID id) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            AudioFile audioFile = audioFileService.getAudioFileById(id)
                    .orElseThrow(() -> new IllegalArgumentException("AudioFile not found"));

            Optional<Image> optionalImageFile = Optional.ofNullable(audioFile.getImage());

            if (optionalImageFile.isPresent()) {
                Image image = optionalImageFile.get();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentLength(image.getData().length);

                String encodedFileName = Base64.getEncoder()
                        .encodeToString(image.getFileName().getBytes(StandardCharsets.UTF_8));

                headers.setContentDispositionFormData("attachment", encodedFileName);

                transactionManager.commit(status);

                return new ResponseEntity<>(image.getData(), headers, HttpStatus.OK);
            }
        } catch (Exception e) {
            transactionManager.rollback(status);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/audio/{id}/incrementCountOfAuditions")
    public ResponseEntity<String> incrementCountOfAuditions(@PathVariable UUID id) {

        try {
            audioFileService.incrementCountOfAuditions(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}