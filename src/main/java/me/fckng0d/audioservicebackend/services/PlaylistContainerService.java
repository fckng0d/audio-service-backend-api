package me.fckng0d.audioservicebackend.services;

import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.PlaylistContainerDTO;
import me.fckng0d.audioservicebackend.models.Playlist;
import me.fckng0d.audioservicebackend.models.PlaylistContainer;
import me.fckng0d.audioservicebackend.models.enums.PlayListOwnerEnum;
import me.fckng0d.audioservicebackend.repositories.PlaylistContainerRepository;
import me.fckng0d.audioservicebackend.repositories.PlaylistRepository;
import me.fckng0d.audioservicebackend.specifications.PlaylistContainerSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PlaylistContainerService {
    private final PlaylistContainerRepository playlistContainerRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistService playlistService;
    private final ImageService imageService;

    private final Map<UUID, Semaphore> audioFilesSemaphores = new ConcurrentHashMap<>();
    //    private final Semaphore audioFilesSemaphore = new Semaphore(1);
    private final Semaphore playlistsSemaphore = new Semaphore(1);
    private final AtomicInteger countOfRequests = new AtomicInteger(0);

    public List<PlaylistContainer> getAllPublicPlaylistContainers() {
        Specification<PlaylistContainer> spec = PlaylistContainerSpecifications.isPublicPlaylistContainer();
        return playlistContainerRepository.findAll(spec);
    }

    public PlaylistContainerDTO convertToDTO(PlaylistContainer playlistContainer) {
        PlaylistContainerDTO dto = PlaylistContainerDTO.builder()
                .id(playlistContainer.getId())
                .name(playlistContainer.getName())
                .playlistOwner(playlistContainer.getPlaylistOwner())
                .description(playlistContainer.getDescription())
                .playlists(playlistService.convertListToDTOs(playlistContainer.getPlaylists()))
                .build();
        return dto;
    }

    @Transactional(readOnly = true)
    public Optional<PlaylistContainer> getPlaylistContainerById(UUID id) {
        return playlistContainerRepository.getPlaylistContainerById(id);
    }

    @Transactional
    public void createNewPublicPlaylistContainer(String name) {
        PlaylistContainer playlistContainer = new PlaylistContainer();
        playlistContainer.setName(name);
        playlistContainer.setDescription("");
        playlistContainer.setPlaylistOwner(PlayListOwnerEnum.PUBLIC);

        playlistContainerRepository.save(playlistContainer);
    }

    @Transactional
    public PlaylistContainer createNewUserPlaylistContainer() {
        PlaylistContainer playlistContainer = new PlaylistContainer();
        playlistContainer.setName("Избранные плейлисты");
        playlistContainer.setDescription("");
        playlistContainer.setPlaylistOwner(PlayListOwnerEnum.USER);

        return playlistContainerRepository.save(playlistContainer);
    }

    @Transactional
    public void addPlaylist(UUID playlistContainerId, Playlist playlist) {

        Optional<PlaylistContainer> playlistContainerOptional =
                playlistContainerRepository.getPlaylistContainerById(playlistContainerId);
        PlaylistContainer playlistContainer = playlistContainerOptional
                .orElseThrow(() -> new RuntimeException("PlaylistContainer not found"));

        if (playlistContainer.getCountOfPlaylists() >= 30) {
            return;
        }


        playlist.getPlaylistContainers().add(playlistContainer);
        playlistRepository.save(playlist);
        playlistContainer.getPlaylists().add(playlist);
        playlistContainer.setCountOfPlaylists(playlistContainer.getCountOfPlaylists());
        playlistContainerRepository.save(playlistContainer);
    }

    @Transactional
    public void updatePlaylistsOrder(UUID playlistContainerId, List<Playlist> updatedWithIndexes) throws InterruptedException {
        playlistsSemaphore.acquire();
        try {
            Optional<PlaylistContainer> playlistContainerOptional =
                    playlistContainerRepository.getPlaylistContainerById(playlistContainerId);
            PlaylistContainer playlistContainer = playlistContainerOptional
                    .orElseThrow(() -> new RuntimeException("PlaylistContainer not found"));

            List<Playlist> oldPlaylists = playlistContainer.getPlaylists();

            IntStream.range(0, playlistContainer.getPlaylists().size())
                    .forEachOrdered(i -> {
                        Playlist currentPlaylist = playlistContainer.getPlaylists().get(i);
                        Playlist newPlaylist = updatedWithIndexes.get(i);

                        if (!currentPlaylist.equals(newPlaylist)) {
                            int newIndex = updatedWithIndexes.indexOf(currentPlaylist);
                            Collections.swap(playlistContainer.getPlaylists(), i, newIndex);
                        }
                    });

            playlistContainerRepository.save(playlistContainer);
        } finally {
            playlistsSemaphore.release();
        }
    }
}
