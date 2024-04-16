package me.fckng0d.audioservicebackend.specifications;

import me.fckng0d.audioservicebackend.models.PlaylistContainer;
import org.springframework.data.jpa.domain.Specification;

public class PlaylistContainerSpecifications {
    public static Specification<PlaylistContainer> isPublicPlaylistContainer() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("playlistOwner"), "PUBLIC");
    }
}
