package me.fckng0d.audioservicebackend.controller;

import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.AudioDataDTO;
import me.fckng0d.audioservicebackend.model.AudioFile;
import me.fckng0d.audioservicebackend.model.Image;
import me.fckng0d.audioservicebackend.service.AudioFileService;
import me.fckng0d.audioservicebackend.service.ImageService;
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
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "${cross-origin}", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AudioFileController {

    private final AudioFileService audioFileService;
    private final ImageService imageService;
    private final PlatformTransactionManager transactionManager;

    @GetMapping("/audio/{id}")
//    @Cacheable("audio_file")
    public ResponseEntity<?> getAudioFile(@PathVariable UUID id) {
        try {
            AudioFile audioFile = audioFileService.getAudioFileById(id)
                    .orElseThrow(() -> new IllegalArgumentException("AudioFile not found"));

            if (audioFile.getUrlPath() == null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentLength(audioFile.getData().length);
                headers.set("Accept-Ranges", "bytes");

//                String encodedFileName = Base64.getEncoder()
//                        .encodeToString(audioFile.getFileName().getBytes(StandardCharsets.UTF_8));
//
//                headers.setContentDispositionFormData("attachment", encodedFileName);

                return new ResponseEntity<>(audioFile.getData(), headers, HttpStatus.OK);

            } else if (audioFile.getData() == null) {

                AudioDataDTO audioDataDTO = AudioDataDTO.builder()
                        .data(null)
                        .urlPath(audioFile.getUrlPath())
                        .build();

                return new ResponseEntity<>(audioDataDTO, HttpStatus.OK);
            }

            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

//    @GetMapping("/audio-url/{id}")
////    @Cacheable("audio_file")
//    public ResponseEntity<String> getAudioFileUrlPath(@PathVariable UUID id) {
//        try {
//            AudioFile audioFile = audioFileService.getAudioFileById(id)
//                    .orElseThrow(() -> new IllegalArgumentException("AudioFile not found"));
//
//            String urlPath = audioFile.getUrlPath();
//
//            return new ResponseEntity<>(urlPath, HttpStatus.OK);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//    }



//    @GetMapping("/audio-stream/{id}")
////    @Cacheable("audio_file")
//    public ResponseEntity<InputStreamResource> getAudioFileStream(@PathVariable UUID id) {
//        try {
//            AudioFile audioFile = audioFileService.getAudioFileById(id)
//                    .orElseThrow(() -> new IllegalArgumentException("AudioFile not found"));
//
//            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(audioFile.getData()));
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//            headers.setContentLength(audioFile.getData().length);
//            headers.set("Accept-Ranges", "bytes");
//
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                    .body(resource);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//    }

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