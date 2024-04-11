package me.fckng0d.audioservicebackend.services;

import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.JwtAuthenticationResponse;
import me.fckng0d.audioservicebackend.DTO.SignInRequest;
import me.fckng0d.audioservicebackend.DTO.SignUpRequest;
import me.fckng0d.audioservicebackend.models.Role;
import me.fckng0d.audioservicebackend.models.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signUp(SignUpRequest request) {

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        userService.create(user);

        user.setEmail(null);
        user.setPassword(null);
        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt, user.getRole().toString());
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        String role = "";
        String identifier = request.getIdentifier();

        try {
            User existingUser = userService.getByUsername(identifier);
            role = existingUser.getRole().toString();
        } catch (UsernameNotFoundException ignored) {
        }

        try {
            User existingUser = userService.getByEmail(identifier);
            identifier = existingUser.getUsername();
            role = existingUser.getRole().toString();
        } catch (UsernameNotFoundException ignored) {
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                identifier,
                request.getPassword()
        ));

        var user = userService
                .userDetailsService()
                .loadUserByUsername(identifier);

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt, role);
    }
}
