package me.fckng0d.audioservicebackend.repositories;

import me.fckng0d.audioservicebackend.models.User;
import me.fckng0d.audioservicebackend.models.UserProfileImageRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileImageRelationRepository extends JpaRepository<UserProfileImageRelation, UUID> {
    Optional<UserProfileImageRelation> findByUser(User user);
}
