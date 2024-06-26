package me.fckng0d.audioservicebackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.fckng0d.audioservicebackend.model.user.UserFavorites;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(indexes = {
        @Index(name = "idx_audio_file_id", columnList = "id"),
        @Index(name = "idx_audio_file_title", columnList = "title"),
        @Index(name = "idx_audio_file_author", columnList = "author"),
})
public class AudioFile {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private UUID id;

    @Column(name = "file_name")
    @NotNull
    private String fileName;

    @Column(name = "title")
    @NotBlank
    @Size(max = 50)
    private String title;

    @Column(name = "author")
    @NotBlank
    private String author;

    @Column(name = "duration")
    private Float duration;

    @Column(name = "count_of_auditions")
    private Long countOfAuditions = 0L;

    @ElementCollection
//    @NotNull
    private List<String> genres;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_data_id")
    private AudioData audioData;

    @Column(name = "url_path")
    private String urlPath;

    @OneToOne
    private Image image = null;

    @ManyToMany(mappedBy = "audioFiles", cascade = CascadeType.ALL)
    private List<Playlist> playlists = new ArrayList<>();

    @ManyToMany(mappedBy = "favoriteAudioFiles", cascade = CascadeType.ALL)
    private List<UserFavorites> userFavorites = new ArrayList<>();

    @PreRemove
    private void preRemove() {
        for (Playlist playlist : playlists) {
            playlist.getAudioFiles().remove(this);
        }
        playlists.clear();

        for (UserFavorites userFavorite : userFavorites) {
            userFavorite.getFavoriteAudioFiles().remove(this);
        }
        userFavorites.clear();
    }
}