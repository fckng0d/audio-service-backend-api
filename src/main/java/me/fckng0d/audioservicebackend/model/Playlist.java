package me.fckng0d.audioservicebackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
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
@Entity
public class Playlist {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private UUID id;

    @Column(name = "name")
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @Column(name = "author")
    @NotBlank
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(name = "playlist_owner_role")
    private PlayListOwnerEnum playlistOwnerRole;

    @Column(name = "owner_username")
    private String ownerUsername;

    @Column(name = "count_of_audio")
    private Integer countOfAudio;

    @Column(name = "duration")
    private Float duration;

    @OneToOne
    private Image image = null;

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToMany
    @OrderColumn(name = "audio_order")
    private List<AudioFile> audioFiles = new ArrayList<>();

    @ManyToMany(mappedBy = "playlists", cascade = CascadeType.ALL)
    private List<PlaylistContainer> playlistContainers = new ArrayList<>();

    @PreRemove
    private void preRemove() {
        for (PlaylistContainer container : playlistContainers) {
            container.getPlaylists().remove(this);
        }
        playlistContainers.clear();
    }
}
