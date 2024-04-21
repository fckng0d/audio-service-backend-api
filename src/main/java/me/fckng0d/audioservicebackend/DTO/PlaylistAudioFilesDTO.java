package me.fckng0d.audioservicebackend.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlaylistAudioFilesDTO {
    private List<AudioFileDTO> audioFiles;
}
