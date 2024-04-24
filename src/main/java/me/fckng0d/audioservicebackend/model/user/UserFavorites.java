package me.fckng0d.audioservicebackend.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.fckng0d.audioservicebackend.model.AudioFile;
import me.fckng0d.audioservicebackend.model.PlaylistContainer;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_favorites")
public class UserFavorites {
    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToOne()
    private PlaylistContainer playlistContainer;

    @ManyToMany
    @OrderColumn(name = "audio_order")
    private List<AudioFile> favoriteAudioFiles = new ArrayList<>();

    @Column(name = "count_of_audio")
    private Integer countOfAudio;

    @Column(name = "duration")
    private Float duration;
}
