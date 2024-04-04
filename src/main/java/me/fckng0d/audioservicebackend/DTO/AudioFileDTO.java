package me.fckng0d.audioservicebackend.DTO;

import lombok.Data;
import me.fckng0d.audioservicebackend.models.Image;

import java.util.List;
import java.util.UUID;

@Data
public class AudioFileDTO {
    private UUID id;
    private String fileName;
    private String title;
    private String author;
    private Float duration;
    private List<String> genres;
    private Image image;
}
