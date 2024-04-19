package me.fckng0d.audioservicebackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.JwtAuthenticationResponse;
import me.fckng0d.audioservicebackend.DTO.UserProfileDTO;
import me.fckng0d.audioservicebackend.services.AuthenticationService;
import me.fckng0d.audioservicebackend.services.ImageService;
import me.fckng0d.audioservicebackend.services.JwtService;
import me.fckng0d.audioservicebackend.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    public static final String BEARER_PREFIX = "Bearer ";
    private final UserService userService;
    private final ImageService imageService;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileDTO> getProfileData(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(BEARER_PREFIX.length());
            String username = jwtService.extractUserName(token);

            if (username != null) {
                UserProfileDTO userProfileDTO = userService.getProfileDataByUsername(username);
                return new ResponseEntity<>(userProfileDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/profile/edit/username")
    @Transactional
    public ResponseEntity<JwtAuthenticationResponse> updateUsername(HttpServletRequest request,
                                                                    @RequestParam("newUsername") String newUsername) {
        try {
            if (userService.isExistsUsername(newUsername)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }

            String token = request.getHeader("Authorization").substring(BEARER_PREFIX.length());
            String username = jwtService.extractUserName(token);

            if (username != null) {
                userService.updateUsername(username, newUsername);

                var updatedUser = userService.getByUsername(newUsername);
                var updatedUserDetails = userService.userDetailsService().loadUserByUsername(updatedUser.getUsername());
                var newToken = jwtService.generateToken(updatedUserDetails);

                JwtAuthenticationResponse jwtAuthenticationResponse =
                        JwtAuthenticationResponse.builder()
                                .token(newToken)
                                .role(updatedUser.getUserRoleEnum().toString())
                                .build();
                return new ResponseEntity<>(jwtAuthenticationResponse, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @GetMapping("/profile/image")
//    @Transactional(readOnly = true)
//    public ResponseEntity<Image> getProfileImage(HttpServletRequest request) {
//        try {
//            String token = request.getHeader("Authorization").substring(BEARER_PREFIX.length());;
//            String username = jwtService.extractUserName(token);
//
//            if (username != null) {
//                Image profileImage = userService.getProfileImageByUsername(username);
//                return new ResponseEntity<>(profileImage, HttpStatus.OK);
//            } else {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @Transactional
    @PostMapping("/profile/image/upload")
    public ResponseEntity<String> uploadProfileImage(HttpServletRequest request,
                                                     @RequestParam("profileImage") MultipartFile profileImage) {
        try {
            String token = request.getHeader("Authorization").substring(BEARER_PREFIX.length());
            ;
            String username = jwtService.extractUserName(token);

            if (username != null) {
                userService.uploadUserProfileImage(username, profileImage);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @DeleteMapping("/profile/image/delete")
    public ResponseEntity<String> deleteProfileImage(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(BEARER_PREFIX.length());
            ;
            String username = jwtService.extractUserName(token);

            if (username != null) {
                userService.deleteProfileImage(username);
                return new ResponseEntity<>("Фотография пользователя удалена", HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
