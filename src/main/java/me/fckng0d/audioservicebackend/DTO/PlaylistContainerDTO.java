package me.fckng0d.audioservicebackend.DTO;

import lombok.Builder;
import lombok.Data;
import me.fckng0d.audioservicebackend.model.enums.PlayListOwnerEnum;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PlaylistContainerDTO {
    private UUID id;
    private String name;
    private String description;
    private PlayListOwnerEnum playlistOwner;
    private String ownerUsername;
    private Integer countOfPlaylists;
    private List<PlaylistDTO> playlists;
}
