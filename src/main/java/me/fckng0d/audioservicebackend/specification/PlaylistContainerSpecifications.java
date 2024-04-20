package me.fckng0d.audioservicebackend.specification;

import me.fckng0d.audioservicebackend.model.PlaylistContainer;
import org.springframework.data.jpa.domain.Specification;

public class PlaylistContainerSpecifications {
    public static Specification<PlaylistContainer> isPublicPlaylistContainer() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("playlistOwner"), "PUBLIC");
    }
}
