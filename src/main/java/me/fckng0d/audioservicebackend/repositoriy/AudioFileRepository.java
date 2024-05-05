package me.fckng0d.audioservicebackend.repositoriy;

import me.fckng0d.audioservicebackend.model.AudioFile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AudioFileRepository extends JpaRepository<AudioFile, UUID> {
    @EntityGraph(attributePaths = {"image", "genres"}, type = EntityGraph.EntityGraphType.LOAD, value = "audioFile.withoutData")
    Optional<AudioFile> getAudioFileById(UUID id);
}
