package me.fckng0d.audioservicebackend.DTO;

import lombok.Builder;
import lombok.Data;
import me.fckng0d.audioservicebackend.models.Image;

import java.util.UUID;

@Data
@Builder
public class UserProfileDTO {
    private UUID id;
    private String username;
    private String email;
    private Image profileImage;
}
