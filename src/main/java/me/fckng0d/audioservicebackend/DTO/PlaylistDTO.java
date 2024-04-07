package me.fckng0d.audioservicebackend.DTO;

import lombok.Data;
import me.fckng0d.audioservicebackend.models.AudioFile;
import me.fckng0d.audioservicebackend.models.Image;

import java.util.List;
import java.util.UUID;

@Data
public class PlaylistDTO {
    private UUID id;
    private String name;
    private String author;
    private Integer countOfAudio;
    private Float duration;
    private Image image;
    private Integer orderIndex;
    private List<AudioFileDTO> audioFiles;
}
