package me.fckng0d.audioservicebackend.repositoriy;

import me.fckng0d.audioservicebackend.model.PlaylistContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaylistContainerRepository extends JpaRepository<PlaylistContainer, UUID>,
        JpaSpecificationExecutor<PlaylistContainer> {
    Optional<PlaylistContainer> getPlaylistContainerById(UUID id);
//    List<PlaylistContainer> findAll(Specification<PlaylistContainer> spec);
}
