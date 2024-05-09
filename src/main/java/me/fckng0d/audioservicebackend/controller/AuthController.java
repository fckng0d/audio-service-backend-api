package me.fckng0d.audioservicebackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.JwtAuthenticationResponse;
import me.fckng0d.audioservicebackend.DTO.SignInRequest;
import me.fckng0d.audioservicebackend.DTO.SignUpRequest;
import me.fckng0d.audioservicebackend.DTO.TokenValidationRequest;
import me.fckng0d.audioservicebackend.exception.UserNotFoundException;
import me.fckng0d.audioservicebackend.model.enums.UserRoleEnum;
import me.fckng0d.audioservicebackend.service.UserService;
import me.fckng0d.audioservicebackend.service.jwt.AuthenticationService;
import me.fckng0d.audioservicebackend.service.jwt.JwtService;
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
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/check-admin-role")
    public ResponseEntity<Boolean> checkAdminRole(HttpServletRequest request) {
        try {
            String username = jwtService.extractUsernameFromRequest(request);
            UserRoleEnum userRole = userService.getRoleByUsername(username);

            if (username != null) {
                if (userRole.equals(UserRoleEnum.ROLE_ADMIN)) {
                    return new ResponseEntity<>(true, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(false, HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
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
