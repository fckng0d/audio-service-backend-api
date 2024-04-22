package me.fckng0d.audioservicebackend.service;

import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.model.AudioFile;
import me.fckng0d.audioservicebackend.model.Playlist;
import me.fckng0d.audioservicebackend.model.PlaylistContainer;
import me.fckng0d.audioservicebackend.model.enums.PlayListOwnerEnum;
import me.fckng0d.audioservicebackend.model.user.User;
import me.fckng0d.audioservicebackend.model.user.UserFavorites;
import me.fckng0d.audioservicebackend.repositoriy.UserFavoritesRepository;
import me.fckng0d.audioservicebackend.repositoriy.UserProfileDataRelationRepository;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
public class UserFavoritesService {
    private final UserService userService;
    private final UserProfileDataRelationRepository userProfileDataRelationRepository;
    private final ImageService imageService;
    private final PlaylistContainerService playlistContainerService;
    private final UserFavoritesRepository userFavoritesRepository;
    private final PlaylistService playlistService;
    private final AudioFileService audioFileService;
    private final Map<String, Semaphore> audioFilesSemaphores = new ConcurrentHashMap<>();


    public UserFavorites getByUsername(String username) {
        User user = userService.getByUsername(username);

        if (user != null) {
            Optional<UserFavorites> userFavoritesOptional = userFavoritesRepository.findByUser(user);

            if (userFavoritesOptional.isPresent()) {
                return userFavoritesOptional.get();
            }
        }
        return null;
    }

    public UserFavorites getById(UUID userFavoriteId) {
        Optional<UserFavorites> userFavoritesOptional = userFavoritesRepository.findById(userFavoriteId);

        return userFavoritesOptional.orElse(null);
    }

    public List<AudioFile> getFavoriteAudioFilesByUsername(String username) {
        UserFavorites userFavorites = this.getByUsername(username);

        if (userFavorites != null) {
            return userFavorites.getFavoriteAudioFiles();
        }
        return null;
    }

    public PlaylistContainer getFavoritePlaylistContainerByUsername(String username) {
        UserFavorites userFavorites = this.getByUsername(username);

        if (userFavorites != null) {
            return userFavorites.getPlaylistContainer();
        }
        return null;
    }

    @Transactional
    public void createNewFavoritePlaylist(String playlistName, String playlistAuthor, MultipartFile playlistImageFile, String username) {
        try {
            UserFavorites userFavorites = this.getByUsername(username);

            Playlist playlist = playlistService.
                    createNewPlaylist(playlistName, playlistAuthor, username,
                            PlayListOwnerEnum.USER, playlistImageFile);

            if (playlist != null) {
                playlistContainerService.addPlaylist(userFavorites.getPlaylistContainer().getId(), playlist);
            } else {
                throw new RuntimeException();
            }

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Transactional
    public AudioFile addAudioFileToFavorites(String username, UUID audioFileId) throws InterruptedException {
        Semaphore audioFilesSemaphore = audioFilesSemaphores.computeIfAbsent(username, k -> new Semaphore(1));
        audioFilesSemaphore.acquire();
        try {
            UserFavorites userFavorites = this.getByUsername(username);

            if (userFavorites != null) {
                AudioFile audioFile = audioFileService.getAudioFileById(audioFileId)
                        .orElseThrow(() -> new ResourceNotFoundException("AudioFile not found "));

                if (userFavorites.getFavoriteAudioFiles().contains(audioFile)) {
                    return null;
//                    throw new AudioFileIsAlreadyInPlaylistException("AudioFile is already in favorites");
                }

                userFavorites.getFavoriteAudioFiles().add(0, audioFile);
                userFavorites.setCountOfAudio(userFavorites.getFavoriteAudioFiles().size());
                userFavorites.setDuration(userFavorites.getDuration() + audioFile.getDuration());
                userFavoritesRepository.save(userFavorites);

                audioFile.getUserFavorites().add(userFavorites);
                audioFileService.save(audioFile);


                return audioFile;
            }
        } finally {
            audioFilesSemaphore.release();
            if (audioFilesSemaphore.availablePermits() == 1) {
                audioFilesSemaphores.remove(username);
            }
        }
        return null;
    }

    @Transactional
    public void deleteAudioFileFromFavorites(String username, UUID audioFileId) throws InterruptedException {
        Semaphore audioFilesSemaphore = audioFilesSemaphores.computeIfAbsent(username, k -> new Semaphore(1));
        audioFilesSemaphore.acquire();
        try {
            UserFavorites userFavorites = this.getByUsername(username);

            if (userFavorites != null) {
                AudioFile audioFile = audioFileService.getAudioFileById(audioFileId)
                        .orElseThrow(() -> new IllegalArgumentException("AudioFile not found"));

                if (!userFavorites.getFavoriteAudioFiles().contains(audioFile)) {
                    throw new IllegalArgumentException("AudioFile is not in the playlist");
                }

                userFavorites.getFavoriteAudioFiles().remove(audioFile);
                userFavorites.setCountOfAudio(userFavorites.getFavoriteAudioFiles().size());
                userFavorites.setDuration(userFavorites.getDuration() - audioFile.getDuration());
                userFavoritesRepository.save(userFavorites);

                audioFile.getUserFavorites().remove(userFavorites);
                audioFileService.save(audioFile);
            }
        } finally {
            audioFilesSemaphore.release();
            if (audioFilesSemaphore.availablePermits() == 1) {
                audioFilesSemaphores.remove(username);
            }
        }
    }

    @Transactional
    public void updateFavoriteAudioFilesOrder(String username, List<AudioFile> updatedAudioFiles) throws InterruptedException {
        Semaphore audioFilesSemaphore = audioFilesSemaphores.computeIfAbsent(username, k -> new Semaphore(1));
        audioFilesSemaphore.acquire();
        try {
            UserFavorites userFavorites = this.getByUsername(username);

            List<AudioFile> newAudioFiles = new ArrayList<>();
            for (AudioFile updatedAudioFile : updatedAudioFiles) {
                AudioFile existingAudioFile = userFavorites.getFavoriteAudioFiles().stream()
                        .filter(audioFile -> audioFile.getId().equals(updatedAudioFile.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("AudioFile not found"));

                newAudioFiles.add(existingAudioFile);

            }

            if (updatedAudioFiles.size() < userFavorites.getFavoriteAudioFiles().size()) {
                userFavorites.getFavoriteAudioFiles().stream()
                        .filter(audioFile -> !updatedAudioFiles.contains(audioFile))
                        .forEach(newAudioFiles::add);
            }

            userFavorites.setFavoriteAudioFiles(newAudioFiles);

            userFavoritesRepository.save(userFavorites);
        } finally {
            audioFilesSemaphore.release();
            if (audioFilesSemaphore.availablePermits() == 1) {
                audioFilesSemaphores.remove(username);
            }
        }
    }
}
