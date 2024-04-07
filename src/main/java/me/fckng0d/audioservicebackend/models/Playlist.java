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
public class Playlist {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UuidGenerator
    private UUID id;

    @Column(name = "name")
    @NotBlank
    private String name;

    @Column(name = "author")
    @NotBlank
    private String author;

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
}
