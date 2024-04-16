package me.fckng0d.audioservicebackend.repositories;

import me.fckng0d.audioservicebackend.models.Image;
import me.fckng0d.audioservicebackend.models.User;
import me.fckng0d.audioservicebackend.models.UserProfileDataRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileDataRelationRepository extends JpaRepository<UserProfileDataRelation, UUID> {
    Optional<UserProfileDataRelation> findByUser(User user);
    Optional<UserProfileDataRelation> findByProfileImage(Image profileImage);
}
