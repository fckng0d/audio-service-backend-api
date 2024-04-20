package me.fckng0d.audioservicebackend.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserFavoritePlaylistContainerDTO {
    private UUID userFavoritesId;
    private PlaylistContainerDTO playlistContainerDTO;
}
