package me.fckng0d.audioservicebackend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
//@Schema(description = "Запрос на аутентификацию")
public class SignInRequest {

//    @Schema(description = "Имя пользователя", example = "Jon")
    @Size(min = 5, max = 50, message = "Имя пользователя или почта должны содержать от 5 до 50 символов")
    @NotBlank(message = "Имя пользователя или почта не могут быть пустыми")
    private String identifier;

//    @Schema(description = "Пароль", example = "my_1secret1_password")
    @Size(min = 8, max = 255, message = "Длина пароля должна быть от 8 до 255 символов")
    @NotBlank(message = "Пароль не может быть пустыми")
    private String password;
}
