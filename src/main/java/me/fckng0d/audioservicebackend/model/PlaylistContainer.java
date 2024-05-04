package me.fckng0d.audioservicebackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.fckng0d.audioservicebackend.model.enums.PlayListOwnerEnum;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class PlaylistContainer {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private UUID id;

    @Column(name = "name")
    @NotBlank
    @Size(max = 50)
    private String name;

    // не нужно
    @Column(name = "description")
//    @NotBlank
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "playlist_owner")
    private PlayListOwnerEnum playlistOwner;

    @Column(name = "owner_username")
    private String ownerUsername;

    @Column(name = "count_of_playlists")
    @Max(value = 100)
    private Integer countOfPlaylists = 0;

    @ManyToMany
    @OrderColumn(name = "playlist_order")
    private List<Playlist> playlists = new ArrayList<>();
}


