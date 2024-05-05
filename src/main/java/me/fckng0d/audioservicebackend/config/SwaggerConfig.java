package me.fckng0d.audioservicebackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Value("${app.api.origin}")
    private String[] apiOrigins;

    @Bean
    public OpenAPI api() {
        List<Server> servers = Arrays.stream(apiOrigins)
                .map(origin -> new Server().url(origin))
                .collect(Collectors.toList());

        return new OpenAPI().servers(servers).info(
                new Info()
                        .title("Audio Service API")
                        .description("")
                        .contact(
                                new Contact()
                                        .name("fckng0d")
                                        .email("")
                                        .url("https://github.com/fckng0d")
                        )
        );
    }
}
