package me.fckng0d.audioservicebackend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
//        @Index(name = "idx_audio_file_image_id", columnList = "image_id")
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
    private String title;

    @Column(name = "author")
    @NotBlank
    private String author;

    @Column(name = "duration")
    private Float duration;

    @ElementCollection
    @NotNull
    private List<String> genres;

    @Lob
    @Column(name = "data")
    @NotNull
    private byte[] data;

    @OneToOne
    private Image image = null;

    @ManyToMany(mappedBy = "audioFiles")
    private List<Playlist> playlists = new ArrayList<>();
}