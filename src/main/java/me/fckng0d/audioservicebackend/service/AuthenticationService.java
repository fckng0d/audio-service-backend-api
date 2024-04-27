package me.fckng0d.audioservicebackend.service;

import lombok.RequiredArgsConstructor;
import me.fckng0d.audioservicebackend.DTO.JwtAuthenticationResponse;
import me.fckng0d.audioservicebackend.DTO.SignInRequest;
import me.fckng0d.audioservicebackend.DTO.SignUpRequest;
import me.fckng0d.audioservicebackend.exception.UserNotFoundException;
import me.fckng0d.audioservicebackend.model.enums.UserRoleEnum;
import me.fckng0d.audioservicebackend.model.user.User;
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
                .userRoleEnum(UserRoleEnum.ROLE_USER)
//                .profileImage(null)
                .build();

        userService.create(user);

        user.setEmail(null);
        user.setPassword(null);
        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt/*, user.getUserRoleEnum().toString()*/);
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signIn(SignInRequest request) {
//        String role = "";
        String identifier = request.getIdentifier();

        if (identifier.matches("[a-zA-Z0-9]+")) {
                boolean isExistsUsername = userService.isExistsUsername(identifier);
                if (!isExistsUsername) {
                    throw new UserNotFoundException("Пользователь не найден по username");
                }
        } else {
            try {
                User existingUser = userService.getByEmail(identifier);
                identifier = existingUser.getUsername();
            } catch (UsernameNotFoundException e) {
                throw new UserNotFoundException("Пользователь не найден по email");
            }
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                identifier,
                request.getPassword()
        ));

        var user = userService
                .userDetailsService()
                .loadUserByUsername(identifier);

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt/*, role*/);
    }
}
