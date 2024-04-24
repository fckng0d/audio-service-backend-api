package me.fckng0d.audioservicebackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.*;
import me.fckng0d.audioservicebackend.model.AudioFile;
import me.fckng0d.audioservicebackend.model.Playlist;
import me.fckng0d.audioservicebackend.model.PlaylistContainer;
import me.fckng0d.audioservicebackend.model.user.UserFavorites;
import me.fckng0d.audioservicebackend.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "${cross-origin}", maxAge = 3600)
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserFavoritesController {
    private final UserService userService;
    private final PlaylistService playlistService;
    private final UserFavoritesService userFavoritesService;
    private final PlaylistContainerService playlistContainerService;
    private final JwtService jwtService;

    @GetMapping("/favorites/playlists")
    @Transactional(readOnly = true)
    public ResponseEntity<PlaylistContainerDTO> getFavoritePlaylistContainer(HttpServletRequest request) {
        try {
            String username = jwtService.extractUsernameFromRequest(request);

            if (username != null) {
                PlaylistContainer playlistContainer =
                        userFavoritesService.getFavoritePlaylistContainerByUsername(username);

                PlaylistContainerDTO playlistContainerDTO = playlistContainerService.convertToDTO(playlistContainer);
                return new ResponseEntity<>(playlistContainerDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/favorites/audioFiles")
    @Transactional(readOnly = true)
    public ResponseEntity<PlaylistDTO> getFavoriteAudioFilesMetaData(HttpServletRequest request) {
        try {
            String username = jwtService.extractUsernameFromRequest(request);

            if (username != null) {
                UserFavorites userFavorites = userFavoritesService.getByUsername(username);

                PlaylistDTO playlistDTO = new PlaylistDTO();
                playlistDTO.setCountOfAudio(userFavorites.getCountOfAudio());
                playlistDTO.setDuration(userFavorites.getDuration());

                return new ResponseEntity<>(playlistDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/favorites/audioFiles/partial")
    @Transactional(readOnly = true)
    public ResponseEntity<PlaylistAudioFilesDTO> getFavoriteAudioFiles(HttpServletRequest request,
                                                                       @RequestParam int startIndex,
                                                                       @RequestParam int count) throws InterruptedException {

        String username = jwtService.extractUsernameFromRequest(request);

        if (username != null) {
            try {
                UserFavorites userFavorites = userFavoritesService.getByUsername(username);

                AtomicInteger index = new AtomicInteger(startIndex);

                List<AudioFile> audioFiles = userFavorites.getFavoriteAudioFiles();

                List<AudioFileDTO> audioFileDTOs =
                        audioFiles
                                .stream()
                                .skip(startIndex)
                                .limit(count)
                                .map(audioFile -> {
                                    AudioFileDTO dto = new AudioFileDTO();
                                    dto.setId(audioFile.getId());
//                            dto.setFileName(audioFile.getFileName());
                                    dto.setTitle(audioFile.getTitle());
                                    dto.setAuthor(audioFile.getAuthor());
                                    dto.setDuration(audioFile.getDuration());
                                    dto.setCountOfAuditions(audioFile.getCountOfAuditions());
//                            dto.setGenres(audioFile.getGenres());
                                    dto.setImage(audioFile.getImage());
                                    int currentIndex = index.getAndIncrement();
//                                    System.out.println(currentIndex);
                                    dto.setIndexInPlaylist(currentIndex);
                                    return dto;
                                })
                                .collect(Collectors.toList());

                PlaylistAudioFilesDTO playlistAudioFilesDTO = PlaylistAudioFilesDTO.builder()
                        .audioFiles(audioFileDTOs)
                        .build();

                return new ResponseEntity<>(playlistAudioFilesDTO, HttpStatus.OK);

            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/favorites/playlists/create")
    @Transactional
    public ResponseEntity<String> createPlaylist(HttpServletRequest request,
                                                 @RequestParam("name") String playlistName,
                                                 @RequestParam("author") String playlistAuthor,
                                                 @RequestParam("imageFile") MultipartFile playlistImageFile) {
        try {
            String username = jwtService.extractUsernameFromRequest(request);

            userFavoritesService.createNewFavoritePlaylist(playlistName,
                    playlistAuthor, playlistImageFile, username);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/favorites/playlists/add/{playlistId}")
    @Transactional
    public ResponseEntity<String> addPlaylist(HttpServletRequest request,
                                                 @PathVariable UUID playlistId) {
        try {
            String username = jwtService.extractUsernameFromRequest(request);

            userFavoritesService.addPlaylistToFavorites(username, playlistId);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/favorites/playlists/delete/{playlistId}")
    @Transactional
    public ResponseEntity<String> deletePlaylist(HttpServletRequest request,
                                                 @PathVariable UUID playlistId) {
        try {
            String username = jwtService.extractUsernameFromRequest(request);

            userFavoritesService.deletePlaylistFromFavorites(username, playlistId);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/favorites/playlists/updateOrder")
    @Transactional
    public ResponseEntity<String> updatePlaylistsOrder(HttpServletRequest request,
                                                       @RequestBody List<UpdatedPlaylistOrderIndexesDto> updatedWithIndexes) {
        try {
            List<Optional<Playlist>> optionalPlaylists = updatedWithIndexes.stream()
                    .map(updateWithIndex -> playlistService.getPlaylistById(updateWithIndex.getId()))
                    .toList();

            List<Playlist> playlists = optionalPlaylists.stream()
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());

            String username = jwtService.extractUsernameFromRequest(request);

            UserFavorites userFavorites = userFavoritesService.getByUsername(username);
            playlistContainerService.updatePlaylistsOrder(userFavorites.getPlaylistContainer().getId(), playlists);

            return new ResponseEntity<>("Playlists order updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update playlists order", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/favorites/audioFiles/add/{audioFileId}")
    @Transactional
    public ResponseEntity<String> addAudioFileToFavorites(HttpServletRequest request,
                                                 @PathVariable UUID audioFileId) {
        try {
            String username = jwtService.extractUsernameFromRequest(request);

//            try {
                AudioFile audioFile = userFavoritesService.addAudioFileToFavorites(username, audioFileId);
                if (audioFile == null) {
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
                }
//            } catch (AudioFileIsAlreadyInPlaylistException e) {
//                return new ResponseEntity<>(HttpStatus.CONFLICT);
//            }

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/favorites/audioFiles/delete/{audioFileId}")
    @Transactional
    public ResponseEntity<String> deleteAudioFileFromPlaylist(HttpServletRequest request, @PathVariable UUID audioFileId) {
        try {
            String username = jwtService.extractUsernameFromRequest(request);

            userFavoritesService.deleteAudioFileFromFavorites(username, audioFileId);

            return new ResponseEntity<>("Audiofile deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to delete audiofile", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/favorites/audioFiles/update")
    @Transactional
    public ResponseEntity<String> updatePlaylist(HttpServletRequest request, @RequestBody List<AudioFile> updatedAudioFiles) {
        try {
            String username = jwtService.extractUsernameFromRequest(request);

            userFavoritesService.updateFavoriteAudioFilesOrder(username, updatedAudioFiles);

            return new ResponseEntity<>("Playlist updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update playlist", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

