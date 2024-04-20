package me.fckng0d.audioservicebackend.DTO;

import lombok.Builder;
import lombok.Data;
import me.fckng0d.audioservicebackend.model.Image;

@Data
@Builder
public class PlaylistImageDTO {
    private Image playlistImage;
}
