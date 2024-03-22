//package me.fckng0d.audioservicebackend.config;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.access.AccessDeniedHandler;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import javax.sql.DataSource;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Collections;
//
//
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class SecurityConfig extends WebSecurityConfiguration {
//    @SuppressWarnings("removal")
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.cors().and()
//                .authorizeHttpRequests( authorize -> authorize
////                        .requestMatchers("/audio/**").denyAll()
//                        .requestMatchers("/clients/{id:\\d+}").hasAnyRole("ADMIN", "USER")
//                        .requestMatchers("/clients/new").hasAnyRole("ADMIN")
//                        .requestMatchers("/clients/filter**").hasAnyRole("ADMIN")
//                        .requestMatchers("/clients/{id:\\d+}/loans/filter**").hasAnyRole("ADMIN", "USER")
//                        .requestMatchers("/clients/{id:\\d+}/loans").hasAnyRole("ADMIN", "USER")
//                        .requestMatchers("/clients/{id}/loans/{loanId}").hasAnyRole("ADMIN", "USER")
//                        .requestMatchers("/clients/{id}/loans/{loanId}/new").hasAnyRole("ADMIN", "USER")
//                        .requestMatchers("/clients/{clientId}/loans/{loanId}/delete").hasAnyRole("ADMIN")
//                        .requestMatchers("/clients/{clientId}/loans/{loanId}/edit").hasAnyRole("ADMIN")
//                        .anyRequest().permitAll())
//                .formLogin((form) -> form
//                        .loginPage("/login")
////                        .loginProcessingUrl("/")
//                        .permitAll())
//                .logout((logout) -> logout
//                        .invalidateHttpSession(true)
//                        .logoutSuccessUrl("/logout")
//                        .deleteCookies("JSESSIONID")
//                        .clearAuthentication(true)
//                        .permitAll())
//                .exceptionHandling()
//                .accessDeniedHandler(accessDeniedHandler());
//        return http.build();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000/"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
//        configuration.setAllowedHeaders(Collections.singletonList("*"));
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/audio/**", configuration);
//        return source;
//    }
//
//    public AccessDeniedHandler accessDeniedHandler() {
//        return new CustomAccessDeniedHandler();
//    }
//
//    public static class CustomAccessDeniedHandler implements AccessDeniedHandler {
//        @Override
//        public void handle(HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException, ServletException {
//            response.sendRedirect(request.getContextPath() + "/access-denied");
//        }
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//}
//
