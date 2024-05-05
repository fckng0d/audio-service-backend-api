package me.fckng0d.audioservicebackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.AudioFileDTO;
import me.fckng0d.audioservicebackend.DTO.PlaylistAudioFilesDTO;
import me.fckng0d.audioservicebackend.DTO.PlaylistDTO;
import me.fckng0d.audioservicebackend.DTO.PlaylistImageDTO;
import me.fckng0d.audioservicebackend.model.AudioFile;
import me.fckng0d.audioservicebackend.model.Image;
import me.fckng0d.audioservicebackend.model.Playlist;
import me.fckng0d.audioservicebackend.model.enums.PlayListOwnerEnum;
import me.fckng0d.audioservicebackend.model.enums.UserRoleEnum;
import me.fckng0d.audioservicebackend.repositoriy.PlaylistRepository;
import me.fckng0d.audioservicebackend.service.AudioFileService;
import me.fckng0d.audioservicebackend.service.PlaylistService;
import me.fckng0d.audioservicebackend.service.UserService;
import me.fckng0d.audioservicebackend.service.jwt.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "${cross-origin}", maxAge = 3600)
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlaylistController {
    private final PlaylistService playlistService;
    private final PlaylistRepository playlistRepository;
    private final AudioFileService audioFileService;
    private final JwtService jwtService;
    private final UserService userService;
    private final Semaphore audioFilesSemaphore = new Semaphore(1);


    @GetMapping("/playlists/{id}")
    @Transactional(readOnly = true)
//    @Cacheable(cacheNames = {"playlist", "audio_file", "image"})
    public ResponseEntity<PlaylistDTO> getPlaylistDataById(HttpServletRequest request,
                                                           @PathVariable UUID id) {

        try {
            Optional<Playlist> optionalPlaylist = playlistService.getPlaylistById(id);

            if (optionalPlaylist.isPresent()) {
                Playlist playlist = optionalPlaylist.get();

                String username = jwtService.extractUsernameFromRequest(request);
                UserRoleEnum userRole = userService.getRoleByUsername(username);

                if (playlist.getPlaylistOwnerRole() == PlayListOwnerEnum.USER) {
                    if (!playlist.getOwnerUsername().equals(username) && userRole != UserRoleEnum.ROLE_ADMIN) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                }

                PlaylistDTO playlistDTO = playlistService.convertToDTO(playlist);

                return new ResponseEntity<>(playlistDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            audioFilesSemaphore.release();
        }
    }

    @GetMapping("/playlists/{id}/audioFiles/partial")
    @Transactional(readOnly = true)
    public ResponseEntity<PlaylistAudioFilesDTO> getAudioFilesPartial(HttpServletRequest request,
            @PathVariable UUID id,
            @RequestParam int startIndex,
            @RequestParam int count) {

        try {
//            long start = System.currentTimeMillis();

            Optional<Playlist> optionalPlaylist = playlistService.getPlaylistById(id);

            if (optionalPlaylist.isPresent()) {
                Playlist playlist = optionalPlaylist.get();

//                int totalCount = playlist.getAudioFiles().size();
//                int availableCount = Math.min(count, totalCount - startIndex);
//                Pageable pageable = PageRequest.of(startIndex, availableCount);
//                List<AudioFile> audioFilesPageable = playlistRepository.findAudioFilesByPlaylistId(playlist.getId(), pageable);

                AtomicInteger index = new AtomicInteger(startIndex);

//                System.out.println("startIndex = " + startIndex + "\ncount = " + count + "\n\n");

                List<AudioFile> audioFiles = playlist.getAudioFiles();

                List<AudioFileDTO> audioFileDTOs =
                        audioFiles
//                        audioFilesPageable
                                .stream()
                                .skip(startIndex)
                                .limit(count)
                                .map(audioFile -> {
                                    AudioFileDTO dto = audioFileService.convertToDTO(audioFile);
                                    int currentIndex = index.getAndIncrement();
                                    // System.out.println(index.getAndIncrement());
                                    dto.setIndexInPlaylist(currentIndex);
                                    return dto;
                                })
                                .collect(Collectors.toList());

//                System.out.println(audioFileDTOs.size());

                PlaylistAudioFilesDTO playlistAudioFilesDTO = PlaylistAudioFilesDTO.builder()
                        .playlistId(playlist.getId())
                        .audioFiles(audioFileDTOs)
                        .build();

//                long end = System.currentTimeMillis();
//                System.out.println(end - start + " мс");

                return new ResponseEntity<>(playlistAudioFilesDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/playlists/delete/{playlistId}")
    @Transactional
    public ResponseEntity<String> deletePlaylist(@PathVariable UUID playlistId) {
        try {

            Optional<Playlist> playlistOptional = playlistService.getPlaylistById(playlistId);
            if (playlistOptional.isPresent()) {
                Playlist playlist = playlistOptional.get();
                playlistService.deletePlaylist(playlist);

                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
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

                String username = jwtService.extractUsernameFromRequest(request);
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
    public ResponseEntity<AudioFileDTO> uploadAudioFile(@PathVariable UUID playlistId,
                                                        @RequestParam("title") String title,
                                                        @RequestParam("author") String author,
                                                        @RequestParam("audioFile") MultipartFile audioFile,
                                                        @RequestParam("imageFile") MultipartFile imageFile,
//                                                  @RequestParam("genres") List<String> genres,
                                                        @RequestParam("duration") Float duration) {

        try {
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new RuntimeException("Playlist not found"));

            if (Float.isNaN(duration)) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            AudioFile audioFileResponse = playlistService.addAudioFile(playlist, audioFile, imageFile, title, author, null, duration);

            AudioFileDTO dto = audioFileService.convertToDTO(audioFileResponse);

            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
