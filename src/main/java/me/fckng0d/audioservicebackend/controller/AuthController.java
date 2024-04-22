package me.fckng0d.audioservicebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.JwtAuthenticationResponse;
import me.fckng0d.audioservicebackend.DTO.SignInRequest;
import me.fckng0d.audioservicebackend.DTO.SignUpRequest;
import me.fckng0d.audioservicebackend.DTO.TokenValidationRequest;
import me.fckng0d.audioservicebackend.exception.AudioFileIsAlreadyInPlaylistException;
import me.fckng0d.audioservicebackend.service.AuthenticationService;
import me.fckng0d.audioservicebackend.service.JwtService;
import me.fckng0d.audioservicebackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/sign-up")
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @PostMapping("/sign-up/is-exists/username")
    public ResponseEntity<String> isExistsUsername(@RequestParam("username") String username) {
        if (userService.isExistsUsername(username)) {
            return new ResponseEntity<>("Пользователь с таким username уже существует",
                    HttpStatus.CONFLICT);
        }
        return ResponseEntity.ok("Такого username нет");
    }

    @PostMapping("/sign-up/is-exists/email")
    public ResponseEntity<String> isExistsEmail(@RequestParam("email") String email) {
        if (userService.isExistsEmail(email)) {
            return new ResponseEntity<>("Пользователь с таким email уже существует",
                    HttpStatus.CONFLICT);
        }
        return ResponseEntity.ok("Такого email нет");
    }

    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthenticationResponse> signIn(@RequestBody SignInRequest request) {
        try {
            JwtAuthenticationResponse jwtAuthenticationResponse = authenticationService.signIn(request);
            return new ResponseEntity<>(jwtAuthenticationResponse, HttpStatus.OK);
        } catch (AudioFileIsAlreadyInPlaylistException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody TokenValidationRequest request) {
        try {
            String token = request.getToken();
            if (token == null) {
                return ResponseEntity.badRequest().body("Token is invalid");
            }

            var username = jwtService.extractUserName(token);
            UserDetails userDetails = userService
                    .userDetailsService()
                    .loadUserByUsername(username);

            boolean isValid = jwtService.isTokenValid(token, userDetails);

            if (isValid) {
                return ResponseEntity.ok("Token is valid");
            } else {
                return ResponseEntity.badRequest().body("Token is invalid");
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return ResponseEntity.badRequest().body("Token is expired");
        }
    }
}
