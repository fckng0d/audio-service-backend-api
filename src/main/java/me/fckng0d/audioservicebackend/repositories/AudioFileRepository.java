package me.fckng0d.audioservicebackend.repositories;

import me.fckng0d.audioservicebackend.models.AudioFile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AudioFileRepository extends JpaRepository<AudioFile, UUID> {
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"image", "genres"})
    AudioFile getAudioFileById(UUID id);
}
