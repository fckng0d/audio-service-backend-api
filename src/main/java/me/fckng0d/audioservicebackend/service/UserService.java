package me.fckng0d.audioservicebackend.service;

import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.UserProfileDTO;
import me.fckng0d.audioservicebackend.exception.UserNotFoundException;
import me.fckng0d.audioservicebackend.model.Image;
import me.fckng0d.audioservicebackend.model.PlaylistContainer;
import me.fckng0d.audioservicebackend.model.enums.UserRoleEnum;
import me.fckng0d.audioservicebackend.model.user.User;
import me.fckng0d.audioservicebackend.model.user.UserFavorites;
import me.fckng0d.audioservicebackend.model.user.UserProfileDataRelation;
import me.fckng0d.audioservicebackend.repositoriy.UserFavoritesRepository;
import me.fckng0d.audioservicebackend.repositoriy.UserProfileDataRelationRepository;
import me.fckng0d.audioservicebackend.repositoriy.UserRepository;
import me.fckng0d.audioservicebackend.specification.UserProfileDataRelationSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileDataRelationRepository userProfileDataRelationRepository;
    private final ImageService imageService;
    private final PlaylistContainerService playlistContainerService;
    private final UserFavoritesRepository userFavoritesRepository;

    /**
     * Сохранение пользователя
     *
     * @return сохраненный пользователь
     */
    public User save(User user) {
        userRepository.save(user);

        PlaylistContainer playlistContainer = playlistContainerService.createNewUserPlaylistContainer(user.getUsername());

        UserProfileDataRelation userProfileImageRelation = UserProfileDataRelation.builder()
                .user(user)
                .profileImage(null)
//                .playlistContainer(playlistContainer)
                .build();
        userProfileDataRelationRepository.save(userProfileImageRelation);

        UserFavorites userFavorites = UserFavorites.builder()
                .user(user)
                .playlistContainer(playlistContainer)
                .countOfAudio(0)
                .duration(0f)
                .build();
        userFavoritesRepository.save(userFavorites);

        return user;
    }


    /**
     * Создание пользователя
     *
     * @return созданный пользователь
     */
    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        return save(user);
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

    }

    /**
     * Получение пользователя по email
     *
     * @return пользователь
     */
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

    }

    public UserRoleEnum getRoleByUsername(String username) {
        User user = this.getByUsername(username);
        if (user != null) {
            return user.getUserRoleEnum();
        }

        return null;
    }

    @Transactional
    public String getPasswordByUsername(String username) {
        try {
            User user = this.getByUsername(username);
            System.out.println(username);
            return user.getPassword();
        } catch (UsernameNotFoundException e) {
            throw new UserNotFoundException();
        }
    }

    public boolean isExistsUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isExistsEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    @Transactional
    public UserProfileDataRelation getProfileDataRelationByUsername(String username) {
        Specification<UserProfileDataRelation> spec = UserProfileDataRelationSpecifications.findByUsername(username);
        return userProfileDataRelationRepository.findOne(spec).orElse(null);
    }

    @Transactional
    public UserProfileDTO getProfileDataByUsername(String username) {
        User user = this.getByUsername(username);

        Image profileImage = this.getProfileImageByUser(user);

        UserProfileDTO userProfileDTO = UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImage(profileImage)
                .build();

        return userProfileDTO;

    }

    @Transactional
    public void updateUsername(String oldUsername, String newUsername) {
        try {
            User user = this.getByUsername(oldUsername);

            user.setUsername(newUsername);
            userRepository.save(user);
        } catch (UsernameNotFoundException e) {
            throw new UserNotFoundException();
        }
    }

    @Transactional
    public Image getProfileImageByUser(User user) {
        UserProfileDataRelation userProfileDataRelation = userProfileDataRelationRepository.findByUser(user)
                .orElseGet(() -> userProfileDataRelationRepository.save(
                        UserProfileDataRelation.builder()
                                .user(user)
                                .profileImage(null)
                                .build()
                ));

        return userProfileDataRelation.getProfileImage();
    }

    @Transactional
    public Image getProfileImageByUsername(String username) {
        User user = this.getByUsername(username);

        return this.getProfileImageByUser(user);
    }

    @Transactional
    public void uploadUserProfileImage(String username, MultipartFile imageFile) {
        Image profileImage = this.getProfileImageByUsername(username);

        UserProfileDataRelation userProfileDataRelation = getProfileDataRelationByUsername(username);
//                userProfileDataRelationRepository.findByProfileImage(profileImage)
//                        .orElseThrow(() -> new RuntimeException("UserProfileImageRelation not found"));

        if (profileImage != null) {
            imageService.deleteImage(profileImage);
        }

        profileImage = imageService.saveImage(imageFile);
        userProfileDataRelation.setProfileImage(profileImage);

        userProfileDataRelationRepository.save(userProfileDataRelation).getProfileImage();
    }

    @Transactional
    public void deleteProfileImage(String username) {
        Image profileImage = this.getProfileImageByUsername(username);

        if (profileImage == null) {
            return;
        }

        UserProfileDataRelation userProfileDataRelation =
                userProfileDataRelationRepository.findByProfileImage(profileImage)
                        .orElseThrow(() -> new RuntimeException("UserProfileImageRelation not found"));

        imageService.deleteImage(profileImage);

        userProfileDataRelation.setProfileImage(null);

//        UserProfileImageRelation newUserProfileImageRelation =
//                UserProfileImageRelation.builder()
//                .user(userProfileImageRelation.getUser())
//                .profileImage(null)
//                .build();
//
//        userProfileImageRelationRepository.delete(userProfileImageRelation);
        userProfileDataRelationRepository.save(userProfileDataRelation);
    }

    /**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужен для Spring Security
     *
     * @return пользователь
     */
    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    /**
     * Получение текущего пользователя
     *
     * @return текущий пользователь
     */
    public User getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }


    /**
     * Выдача прав администратора текущему пользователю
     * <p>
     * Нужен для демонстрации
     */
    @Deprecated
    public void getAdmin() {
        var user = getCurrentUser();
        user.setUserRoleEnum(UserRoleEnum.ROLE_ADMIN);
        save(user);
    }
}
