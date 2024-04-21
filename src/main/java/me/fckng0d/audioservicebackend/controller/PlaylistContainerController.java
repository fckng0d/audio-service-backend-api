package me.fckng0d.audioservicebackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.PlaylistContainerDTO;
import me.fckng0d.audioservicebackend.DTO.UpdatedPlaylistOrderIndexesDto;
import me.fckng0d.audioservicebackend.model.Playlist;
import me.fckng0d.audioservicebackend.model.PlaylistContainer;
import me.fckng0d.audioservicebackend.model.enums.PlayListOwnerEnum;
import me.fckng0d.audioservicebackend.model.enums.UserRoleEnum;
import me.fckng0d.audioservicebackend.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "${cross-origin}", maxAge = 3600)
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlaylistContainerController {
    private final PlaylistService playlistService;
    private final PlaylistContainerService playlistContainerService;
    private final UserFavoritesService userFavoritesService;
    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/public/playlistContainers")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PlaylistContainerDTO>> getAllPublicPlaylistContainers() {
        try {
            List<PlaylistContainer> playlistContainers = playlistContainerService.getAllPublicPlaylistContainers();
            if (!playlistContainers.isEmpty()) {
                List<PlaylistContainerDTO> playlistContainerDTOs = playlistContainers.stream()
                        .map(playlistContainerService::convertToDTO)
                        .toList();

                return new ResponseEntity<>(playlistContainerDTOs, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/playlistContainers/{playlistContainerId}")
    @Transactional(readOnly = true)
    public ResponseEntity<PlaylistContainerDTO> getPlaylistContainer(HttpServletRequest request,
                                                                         @PathVariable UUID playlistContainerId) {
        try {
            Optional<PlaylistContainer> playlistContainerOptional =
                    playlistContainerService.getPlaylistContainerById(playlistContainerId);
            PlaylistContainer playlistContainer = playlistContainerOptional
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PlaylistContainer not found")
                    );

            String username = jwtService.extractUsernameFromRequest(request);
            UserRoleEnum userRole = userService.getRoleByUsername(username);

            PlaylistContainerDTO playlistContainerDTO = null;
            if (playlistContainer.getPlaylistOwner() == PlayListOwnerEnum.PUBLIC) {
                playlistContainerDTO = playlistContainerService.convertToDTO(playlistContainer);
            } else if (playlistContainer.getPlaylistOwner() == PlayListOwnerEnum.USER) {
                if (playlistContainer.getOwnerUsername().equals(username) || userRole == UserRoleEnum.ROLE_ADMIN) {
                    playlistContainerDTO = playlistContainerService.convertToDTO(playlistContainer);
                }
            }

            if (playlistContainerDTO != null) {
                return new ResponseEntity<>(playlistContainerDTO, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/public/playlistContainers/create")
    @Transactional
    public ResponseEntity<String> createPlaylistContainers(@RequestParam("name") String name
//                                                           ,@RequestParam("description") String description,
    ) {

        try {
            playlistContainerService.createNewPublicPlaylistContainer(name);

            return new ResponseEntity<>("Playlist container created successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to create playlist container", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/playlistContainers/{playlistContainerId}/add")
    @Transactional
    public ResponseEntity<String> addPlaylistToPlaylistContainers(HttpServletRequest request,
                                                                  @PathVariable UUID playlistContainerId,
                                                                  @RequestParam("name") String playlistName,
                                                                  @RequestParam("author") String playlistAuthor,
                                                                  @RequestParam("imageFile") MultipartFile playlistImageFile) {
        try {
            Optional<PlaylistContainer> playlistContainerOptional =
                    playlistContainerService.getPlaylistContainerById(playlistContainerId);
            if (playlistContainerOptional.isPresent()) {
                if (playlistContainerOptional.get().getCountOfPlaylists() >= 30) {
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
                }
            }

            String username = jwtService.extractUsernameFromRequest(request);
            UserRoleEnum playlistOwnerRole = userService.getRoleByUsername(username);

            Playlist playlist = null;
            if (playlistContainerOptional.get().getPlaylistOwner() == PlayListOwnerEnum.PUBLIC) {
                if (playlistOwnerRole == UserRoleEnum.ROLE_ADMIN) {
                    playlist = playlistService.createNewPlaylist(playlistName, playlistAuthor, "admin", PlayListOwnerEnum.PUBLIC, playlistImageFile);
                }
            } else if (playlistContainerOptional.get().getPlaylistOwner() == PlayListOwnerEnum.USER) {
                if (playlistContainerOptional.get().getOwnerUsername().equals(username)) {
                    playlist = playlistService.createNewPlaylist(playlistName, playlistAuthor, username, PlayListOwnerEnum.USER, playlistImageFile);
                }
            }

            if (playlist != null) {
                playlistContainerService.addPlaylist(playlistContainerId, playlist);

                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/playlistContainers/{playlistContainerId}/updateOrder")
    public ResponseEntity<String> updatePlaylistsOrder(@PathVariable UUID playlistContainerId,
                                                       @RequestBody List<UpdatedPlaylistOrderIndexesDto> updatedWithIndexes) {
        try {
            List<Optional<Playlist>> optionalPlaylists  = updatedWithIndexes.stream()
                    .map(updateWithIndex -> playlistService.getPlaylistById(updateWithIndex.getId()))
                    .toList();

            List<Playlist> playlists = optionalPlaylists .stream()
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());

            playlistContainerService.updatePlaylistsOrder(playlistContainerId, playlists);
//            while (!playlistService.hasQueuedThreads()) {
//                Thread.sleep(1);
//            }
            return new ResponseEntity<>("Playlists order updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update playlists order", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
