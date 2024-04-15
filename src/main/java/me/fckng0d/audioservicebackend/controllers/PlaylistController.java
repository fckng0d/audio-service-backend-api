package me.fckng0d.audioservicebackend.controllers;

import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.AudioFileDTO;
import me.fckng0d.audioservicebackend.DTO.PlaylistDTO;
import me.fckng0d.audioservicebackend.DTO.PlaylistImageDTO;
import me.fckng0d.audioservicebackend.DTO.UpdatedPlaylistOrderIndexesDto;
import me.fckng0d.audioservicebackend.models.AudioFile;
import me.fckng0d.audioservicebackend.models.Image;
import me.fckng0d.audioservicebackend.models.Playlist;
import me.fckng0d.audioservicebackend.repositories.PlaylistRepository;
import me.fckng0d.audioservicebackend.services.PlaylistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "${cross-origin}", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlaylistController {
    private final PlaylistService playlistService;
    private final PlaylistRepository playlistRepository;
    private final PlatformTransactionManager transactionManager;

    @GetMapping("/playlists")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PlaylistDTO>> getAllPlaylists() {
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
//        TransactionStatus status = transactionManager.getTransaction(def);

        try {

//            Playlist playlist2 = new Playlist();
//            playlist2.setName("Плейлист 1");
//            playlist2.setAuthor("Модератор");
//            List<AudioFile> audioFiles = audioFileService.getAllAudioFiles();
//
//            for (AudioFile audioFile : audioFiles) {
//                playlistService.addAudioFile(playlist2, audioFile);
//            }
//            playlistRepository.save(playlist2);

            List<Playlist> playlists = playlistService.getAllPlaylists();
//            System.out.println(playlists.size());

//            transactionManager.commit(status);

            if (!playlists.isEmpty()) {

                List<PlaylistDTO> playlistDTOS = playlists.stream()
                        .map(playlist -> {
                            PlaylistDTO dto = new PlaylistDTO();
                            dto.setId(playlist.getId());
                            dto.setName(playlist.getName());
                            dto.setAuthor(playlist.getAuthor());
                            dto.setCountOfAudio(playlist.getCountOfAudio());
                            dto.setDuration(playlist.getDuration());
                            dto.setImage(playlist.getImage());
                            dto.setOrderIndex(playlist.getOrderIndex());
                            return dto;
                        })
                        .collect(Collectors.toList());

                return new ResponseEntity<>(playlistDTOS, HttpStatus.OK);

//                return new ResponseEntity<>(audioFiles, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
//            transactionManager.rollback(status);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/playlists/{id}")
    @Transactional(readOnly = true)
//    @Cacheable(cacheNames = {"playlist", "audio_file", "image"})
    public ResponseEntity<PlaylistDTO> getPlaylistById(@PathVariable UUID id) {
        long startTime = System.currentTimeMillis();

        try {
            Optional<Playlist> optionalPlaylist = playlistService.getPlaylistById(id);

            if (optionalPlaylist.isPresent()) {
                Playlist playlist = optionalPlaylist.get();

                PlaylistDTO playlistDTO = new PlaylistDTO();
                playlistDTO.setId(playlist.getId());
                playlistDTO.setName(playlist.getName());
                playlistDTO.setAuthor(playlist.getAuthor());
                playlistDTO.setDuration(playlist.getDuration());
                playlistDTO.setCountOfAudio(playlist.getCountOfAudio());
                playlistDTO.setImage(playlist.getImage());


                List<AudioFileDTO> audioFileDTOs = playlist.getAudioFiles().stream()
                        .map(audioFile -> {
                            AudioFileDTO dto = new AudioFileDTO();
                            dto.setId(audioFile.getId());
                            dto.setFileName(audioFile.getFileName());
                            dto.setTitle(audioFile.getTitle());
                            dto.setAuthor(audioFile.getAuthor());
                            dto.setDuration(audioFile.getDuration());
                            dto.setCountOfAuditions(audioFile.getCountOfAuditions());
                            dto.setGenres(audioFile.getGenres());
                            dto.setImage(audioFile.getImage());
                            return dto;
                        })
                        .collect(Collectors.toList());

                playlistDTO.setAudioFiles(audioFileDTOs);

                long endTime = System.currentTimeMillis();
//                System.out.println("Время загрузки плейлиста: " + (endTime - startTime) + " мс");

                return new ResponseEntity<>(playlistDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/playlists/create")
    public ResponseEntity<String> createNewPlaylist(@RequestParam("name") String name,
                                                    @RequestParam("author") String author,
                                                    @RequestParam("imageFile") MultipartFile imageFile) {

        try {
            playlistService.createNewPlaylist(name, author, imageFile);

            return new ResponseEntity<>("Playlist added successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to add playlist", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/playlists/{playlistId}/upload")
//    @CacheEvict(cacheNames="playlist", key="#id")
    public ResponseEntity<String> uploadAudioFile(@PathVariable UUID playlistId,
                                                  @RequestParam("title") String title,
                                                  @RequestParam("author") String author,
                                                  @RequestParam("audioFile") MultipartFile audioFile,
                                                  @RequestParam("imageFile") MultipartFile imageFile,
//                                                  @RequestParam("genres") List<String> genres,
                                                  @RequestParam("duration") Float duration) {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new RuntimeException("Playlist not found"));

            playlistService.addAudioFile(playlist, audioFile, imageFile, title, author, null, duration);

            transactionManager.commit(status);

            return new ResponseEntity<>("Audio file uploaded successfully", HttpStatus.OK);
        } catch (Exception e) {
            transactionManager.rollback(status);
            e.printStackTrace();
            return new ResponseEntity<>("Failed to upload audio file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PutMapping("/playlists/{playlistId}/image/update")
    public ResponseEntity<PlaylistImageDTO> uploadProfileImage(@PathVariable UUID playlistId,
                                                    @RequestParam("playlistImage") MultipartFile playlistImage) {
        try {
            playlistService.updatePlaylisImage(playlistId, playlistImage);
            Image newPlaylistImage = playlistService.getPlaylistImage(playlistId);
            PlaylistImageDTO playlistImageDTO = PlaylistImageDTO.builder()
                    .playlistImage(newPlaylistImage)
                    .build();
            if (newPlaylistImage != null) {
                return new ResponseEntity<>(playlistImageDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/playlists/updateOrder")
    public ResponseEntity<String> updatePlaylistsOrder(@RequestBody List<UpdatedPlaylistOrderIndexesDto> updatedWithIndexes) {
        try {
            playlistService.updatePlaylistsOrder(updatedWithIndexes);
//            while (!playlistService.hasQueuedThreads()) {
//                Thread.sleep(1);
//            }
            return new ResponseEntity<>("Playlists order updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update playlists order", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/playlists/{id}/update")
    public ResponseEntity<String> updatePlaylist(@PathVariable UUID id, @RequestBody List<AudioFile> updatedAudioFiles) {
        try {
            playlistService.updatePlaylist(id, updatedAudioFiles);
//            while (!playlistService.hasQueuedThreads()) {
//                Thread.sleep(1);
//            }
            return new ResponseEntity<>("Playlist updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update playlist", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/playlists/{playlistId}/delete/{audioFileId}")
    public ResponseEntity<String> deleteAudioFileFromPlaylist(@PathVariable UUID playlistId, @PathVariable UUID audioFileId) {
        try {
            playlistService.deleteAudioFile(playlistId, audioFileId);
//            while (!playlistService.hasQueuedThreads()) {
//                Thread.sleep(1);
//            }
//            System.out.println    ("Удалено.\n###########################\n\n");
            return new ResponseEntity<>("Audiofile deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to delete audiofile", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
