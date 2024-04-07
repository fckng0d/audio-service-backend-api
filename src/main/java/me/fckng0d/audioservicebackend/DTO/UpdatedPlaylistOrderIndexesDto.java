package me.fckng0d.audioservicebackend.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdatedPlaylistOrderIndexesDto {
    private UUID id;
    private Integer orderIndex;
}
