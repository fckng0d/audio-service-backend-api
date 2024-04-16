package me.fckng0d.audioservicebackend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.fckng0d.audioservicebackend.models.enums.PlayListOwnerEnum;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PlaylistContainer {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private UUID id;

    @Column(name = "name")
    @NotBlank
    private String name;

    @Column(name = "description")
    @NotBlank
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "playlist_owner", nullable = false)
    private PlayListOwnerEnum playlistOwner;

    @Column(name = "count_of_playlists")
    @Max(value = 30)
    private Integer countOfPlaylists = 0;

    @ManyToMany
    @OrderColumn(name = "playlist_order")
    private List<Playlist> playlists = new ArrayList<>();
}


