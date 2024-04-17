package me.fckng0d.audioservicebackend.services;

import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.UserProfileDTO;
import me.fckng0d.audioservicebackend.models.Image;
import me.fckng0d.audioservicebackend.models.enums.UserRoleEnum;
import me.fckng0d.audioservicebackend.models.User;
import me.fckng0d.audioservicebackend.models.UserProfileDataRelation;
import me.fckng0d.audioservicebackend.repositories.UserProfileDataRelationRepository;
import me.fckng0d.audioservicebackend.repositories.UserRepository;
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

    /**
     * Сохранение пользователя
     *
     * @return сохраненный пользователь
     */
    public User save(User user) {
        return userRepository.save(user);
    }


    /**
     * Создание пользователя
     *
     * @return созданный пользователь
     */
    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            // Заменить на свои исключения
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        var userProfileImageRelation = UserProfileDataRelation.builder()
                .user(user)
                .profileImage(null)
                .build();
        userProfileDataRelationRepository.save(userProfileImageRelation);

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
    public Image uploadUserProfileImage(String username, MultipartFile imageFile) {
        Image profileImage = this.getProfileImageByUsername(username);

        UserProfileDataRelation userProfileDataRelation =
                userProfileDataRelationRepository.findByProfileImage(profileImage)
                        .orElseThrow(() -> new RuntimeException("UserProfileImageRelation not found"));

        if (profileImage != null) {
            imageService.deleteImage(profileImage);
        }

        profileImage = imageService.saveImage(imageFile);
        userProfileDataRelation.setProfileImage(profileImage);

        return userProfileDataRelationRepository.save(userProfileDataRelation).getProfileImage();
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
