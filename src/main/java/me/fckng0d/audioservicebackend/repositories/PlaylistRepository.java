package me.fckng0d.audioservicebackend.repositories;

import me.fckng0d.audioservicebackend.models.Playlist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {
    // Жадная загрузка
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"image", "audioFiles", "audioFiles.image"})
    Playlist getPlaylistsById(UUID id);
}