package me.fckng0d.audioservicebackend.repositories;

import me.fckng0d.audioservicebackend.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {
    Image getImageByImageId(UUID id);
}
