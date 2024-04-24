package me.fckng0d.audioservicebackend.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PlaylistAudioFilesDTO {
    private UUID playlistId;
    private List<AudioFileDTO> audioFiles;
}
