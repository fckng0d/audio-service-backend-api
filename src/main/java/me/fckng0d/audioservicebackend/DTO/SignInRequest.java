package me.fckng0d.audioservicebackend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignInRequest {

    @Size(min = 5, max = 50, message = "Имя пользователя или почта должны содержать от 5 до 50 символов")
    @NotBlank(message = "Имя пользователя или почта не могут быть пустыми")
    private String identifier;

    @Size(min = 8, max = 255, message = "Длина пароля должна быть от 8 до 255 символов")
    @NotBlank(message = "Пароль не может быть пустыми")
    private String password;
}
