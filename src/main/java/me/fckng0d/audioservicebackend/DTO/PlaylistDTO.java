package me.fckng0d.audioservicebackend.DTO;

import lombok.Data;
import me.fckng0d.audioservicebackend.model.Image;
import me.fckng0d.audioservicebackend.model.enums.PlayListOwnerEnum;

import java.util.List;
import java.util.UUID;

@Data
public class PlaylistDTO {
    private UUID id;
    private String name;
    private String author;
    private String ownerUsername;
    private PlayListOwnerEnum playlistOwnerRole;
    private Integer countOfAudio;
    private Float duration;
    private Image image;
    private Integer orderIndex;
    private List<AudioFileDTO> audioFiles;
}
