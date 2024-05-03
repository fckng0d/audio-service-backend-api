package me.fckng0d.audioservicebackend.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AudioDataDTO {
    private byte[] data;
    private String urlPath;
}
