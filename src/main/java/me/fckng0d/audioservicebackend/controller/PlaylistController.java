package me.fckng0d.audioservicebackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.AudioFileDTO;
import me.fckng0d.audioservicebackend.DTO.PlaylistDTO;
import me.fckng0d.audioservicebackend.DTO.PlaylistImageDTO;
import me.fckng0d.audioservicebackend.model.AudioFile;
import me.fckng0d.audioservicebackend.model.Image;
import me.fckng0d.audioservicebackend.model.Playlist;
import me.fckng0d.audioservicebackend.model.enums.PlayListOwnerEnum;
import me.fckng0d.audioservicebackend.model.enums.UserRoleEnum;
import me.fckng0d.audioservicebackend.repositoriy.PlaylistRepository;
import me.fckng0d.audioservicebackend.service.JwtService;
import me.fckng0d.audioservicebackend.service.PlaylistService;
import me.fckng0d.audioservicebackend.service.UserService;
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

@RestController
@CrossOrigin(origins = "${cross-origin}", maxAge = 3600)
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlaylistController {
    public static final String BEARER_PREFIX = "Bearer ";
    private final PlaylistService playlistService;
    private final PlaylistRepository playlistRepository;
    private final PlatformTransactionManager transactionManager;
    private final JwtService jwtService;
    private final UserService userService;

    @GetMapping("/playlists/{id}")
    @Transactional(readOnly = true)
//    @Cacheable(cacheNames = {"playlist", "audio_file", "image"})
    public ResponseEntity<PlaylistDTO> getPlaylistById(HttpServletRequest request, @PathVariable UUID id) {
        long startTime = System.currentTimeMillis();

        try {
            Optional<Playlist> optionalPlaylist = playlistService.getPlaylistById(id);

            if (optionalPlaylist.isPresent()) {
                Playlist playlist = optionalPlaylist.get();

                String token = request.getHeader("Authorization").substring(BEARER_PREFIX.length());
                String username = jwtService.extractUserName(token);

                UserRoleEnum userRole = userService.getRoleByUsername(username);

                if (playlist.getPlaylistOwnerRole() == PlayListOwnerEnum.USER) {
                    if (!playlist.getOwnerUsername().equals(username) && userRole != UserRoleEnum.ROLE_ADMIN) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                }

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

    @GetMapping("/playlists/{id}/isAccess")
    @Transactional(readOnly = true)
    public ResponseEntity<String> isThereAccessToPlaylist(HttpServletRequest request, @PathVariable UUID id) {
        try {
            Optional<Playlist> optionalPlaylist = playlistService.getPlaylistById(id);

            if (optionalPlaylist.isPresent()) {
                Playlist playlist = optionalPlaylist.get();

                String token = request.getHeader("Authorization").substring(BEARER_PREFIX.length());
                String username = jwtService.extractUserName(token);

                UserRoleEnum userRole = userService.getRoleByUsername(username);

                if (playlist.getPlaylistOwnerRole() == PlayListOwnerEnum.USER) {
                    if (!playlist.getOwnerUsername().equals(username) && userRole != UserRoleEnum.ROLE_ADMIN) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                }

                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/playlists/{playlistId}/upload")
    @Transactional
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

    @PutMapping("/playlists/{playlistId}/image/update")
    @Transactional
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

    @PutMapping("/playlists/{playlistId}/edit/name")
    public ResponseEntity<String> updatePlaylistName(@PathVariable UUID playlistId,
                                                       @RequestParam("newPlaylistName") String newPlaylistName) {
        try {
            playlistService.updatePlaylistName(playlistId, newPlaylistName);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update playlists order", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/playlists/{id}/update")
    @Transactional
    public ResponseEntity<String> updatePlaylist(@PathVariable UUID id, @RequestBody List<AudioFile> updatedAudioFiles) {
        try {
            playlistService.updatePlaylistAudioFilesOrder(id, updatedAudioFiles);
            return new ResponseEntity<>("Playlist updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update playlist", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/playlists/{playlistId}/delete/{audioFileId}")
    @Transactional
    public ResponseEntity<String> deleteAudioFileFromPlaylist(@PathVariable UUID playlistId, @PathVariable UUID audioFileId) {
        try {
            playlistService.deleteAudioFile(playlistId, audioFileId);
            return new ResponseEntity<>("Audiofile deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to delete audiofile", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
