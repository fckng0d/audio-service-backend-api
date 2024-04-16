package me.fckng0d.audioservicebackend.controllers;

import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.PlaylistContainerDTO;
import me.fckng0d.audioservicebackend.DTO.UpdatedPlaylistOrderIndexesDto;
import me.fckng0d.audioservicebackend.models.Playlist;
import me.fckng0d.audioservicebackend.models.PlaylistContainer;
import me.fckng0d.audioservicebackend.models.enums.PlayListOwnerEnum;
import me.fckng0d.audioservicebackend.services.PlaylistContainerService;
import me.fckng0d.audioservicebackend.services.PlaylistService;
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

@CrossOrigin(origins = "${cross-origin}", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlaylistContainerController {
    private final PlaylistService playlistService;
    private final PlaylistContainerService playlistContainerService;

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
    public ResponseEntity<PlaylistContainerDTO> getPlaylistContainerById(@PathVariable UUID playlistContainerId) {
        try {
            Optional<PlaylistContainer> playlistContainerOptional =
                    playlistContainerService.getPlaylistContainerById(playlistContainerId);
            PlaylistContainer playlistContainer = playlistContainerOptional
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PlaylistContainer not found")
                    );

            PlaylistContainerDTO playlistContainerDTO = playlistContainerService.convertToDTO(playlistContainer);

                return new ResponseEntity<>(playlistContainerDTO, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/playlistContainers/create")
    public ResponseEntity<String> createPlaylistContainers(@RequestParam("name") String name,
                                                           @RequestParam("description") String description,
                                                           @RequestParam("playlistOwner") PlayListOwnerEnum playlistOwner) {

        try {
            playlistContainerService.createNewPlaylistContainer(name, description, playlistOwner);

            return new ResponseEntity<>("Playlist container created successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to create playlist container", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/playlistContainers/{playlistContainerId}/add")
    @Transactional
    public ResponseEntity<String> addPlaylistToPlaylistContainers(@PathVariable UUID playlistContainerId,
                                                                  @RequestParam("name") String playlistName,
                                                                  @RequestParam("author") String playlistAuthor,
                                                                  @RequestParam("imageFile") MultipartFile playlistImageFile) {
        try {
            Playlist playlist = playlistService.createNewPlaylist(playlistName, playlistAuthor, playlistImageFile);
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
