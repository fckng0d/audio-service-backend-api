package me.fckng0d.audioservicebackend.DTO;

import lombok.Builder;
import lombok.Data;
import me.fckng0d.audioservicebackend.models.enums.PlayListOwnerEnum;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PlaylistContainerDTO {
    private UUID id;
    private String name;
    private String description;
    private PlayListOwnerEnum playlistOwner;
    private Integer countOfPlaylists;
    private List<PlaylistDTO> playlists;
}
