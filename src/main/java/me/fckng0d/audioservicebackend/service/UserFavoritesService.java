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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserFavoritesService {
    private final UserService userService;
    private final UserProfileDataRelationRepository userProfileDataRelationRepository;
    private final ImageService imageService;
    private final PlaylistContainerService playlistContainerService;
    private final UserFavoritesRepository userFavoritesRepository;
    private final PlaylistService playlistService;


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
}
