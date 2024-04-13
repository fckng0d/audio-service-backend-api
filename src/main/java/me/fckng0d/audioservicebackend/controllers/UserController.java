package me.fckng0d.audioservicebackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.UserProfileDTO;
import me.fckng0d.audioservicebackend.models.Image;
import me.fckng0d.audioservicebackend.services.JwtService;
import me.fckng0d.audioservicebackend.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    public static final String BEARER_PREFIX = "Bearer ";
    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/profile/image")
    @Transactional(readOnly = true)
    public ResponseEntity<Image> getProfileImage(HttpServletRequest request) {
        try {
//            System.out.println(request);
            String token = request.getHeader("Authorization").substring(BEARER_PREFIX.length());;
            String username = jwtService.extractUserName(token);

            if (username != null) {
                Image profileImage = userService.getProfileImageByUsername(username);
                return new ResponseEntity<>(profileImage, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileDTO> getProfileData(HttpServletRequest request) {
        try {
//            System.out.println(request);
            String token = request.getHeader("Authorization").substring(BEARER_PREFIX.length());;
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
}
