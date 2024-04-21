package me.fckng0d.audioservicebackend.repositoriy;

import me.fckng0d.audioservicebackend.model.AudioFile;
import me.fckng0d.audioservicebackend.model.Playlist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {
    // Жадная загрузка
    // Мешает загрузке аудио по частям ("audioFiles", "audioFiles.image")
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"image"/*, "audioFiles", "audioFiles.image"*/})
    Optional<Playlist> getPlaylistsById(UUID id);

    @Query("SELECT p.audioFiles FROM Playlist p WHERE p.id = :playlistId")
    List<AudioFile> findAudioFilesByPlaylistId(@Param("playlistId") UUID playlistId, Pageable pageable);
}
