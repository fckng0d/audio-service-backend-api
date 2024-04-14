package me.fckng0d.audioservicebackend.services;

import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.UserProfileDTO;
import me.fckng0d.audioservicebackend.models.Image;
import me.fckng0d.audioservicebackend.models.Role;
import me.fckng0d.audioservicebackend.models.User;
import me.fckng0d.audioservicebackend.models.UserProfileImageRelation;
import me.fckng0d.audioservicebackend.repositories.UserProfileImageRelationRepository;
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
    private final UserRepository repository;
    private final UserProfileImageRelationRepository userProfileImageRelationRepository;
    private final ImageService imageService;

    /**
     * Сохранение пользователя
     *
     * @return сохраненный пользователь
     */
    public User save(User user) {
        return repository.save(user);
    }


    /**
     * Создание пользователя
     *
     * @return созданный пользователь
     */
    public User create(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            // Заменить на свои исключения
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        if (repository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        var userProfileImageRelation = UserProfileImageRelation.builder()
                .user(user)
                .profileImage(null)
                .build();
        userProfileImageRelationRepository.save(userProfileImageRelation);

        return save(user);
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

    }

    /**
     * Получение пользователя по email
     *
     * @return пользователь
     */
    public User getByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

    }

    @Transactional
    public UserProfileDTO getProfileDataByUsername(String username) {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        UserProfileDTO userProfileDTO = UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return userProfileDTO;

    }

    @Transactional
    public Image getProfileImageByUsername(String username) {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        UserProfileImageRelation userProfileImageRelation = userProfileImageRelationRepository.findByUser(user)
                .orElseGet(() -> userProfileImageRelationRepository.save(
                        UserProfileImageRelation.builder()
                                .user(user)
                                .profileImage(null)
                                .build()
                ));

        return userProfileImageRelation.getProfileImage();
    }

    @Transactional
    public Image uploadUserProfileImage(String username, MultipartFile imageFile) {
        Image profileImage = this.getProfileImageByUsername(username);

        UserProfileImageRelation userProfileImageRelation =
                userProfileImageRelationRepository.findByProfileImage(profileImage)
                        .orElseThrow(() -> new RuntimeException("UserProfileImageRelation not found"));

        if (profileImage != null) {
            imageService.deleteImage(profileImage);
        }

        profileImage = imageService.saveImage(imageFile);
        userProfileImageRelation.setProfileImage(profileImage);

        return userProfileImageRelationRepository.save(userProfileImageRelation).getProfileImage();
    }

    @Transactional
    public void deleteProfileImage(String username) {
        Image profileImage = this.getProfileImageByUsername(username);

        if (profileImage == null) {
            return;
        }

        UserProfileImageRelation userProfileImageRelation =
                userProfileImageRelationRepository.findByProfileImage(profileImage)
                        .orElseThrow(() -> new RuntimeException("UserProfileImageRelation not found"));

        imageService.deleteImage(profileImage);

        userProfileImageRelation.setProfileImage(null);

//        UserProfileImageRelation newUserProfileImageRelation =
//                UserProfileImageRelation.builder()
//                .user(userProfileImageRelation.getUser())
//                .profileImage(null)
//                .build();
//
//        userProfileImageRelationRepository.delete(userProfileImageRelation);
        userProfileImageRelationRepository.save(userProfileImageRelation);
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
        user.setRole(Role.ROLE_ADMIN);
        save(user);
    }
}
