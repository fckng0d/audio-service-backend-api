package me.fckng0d.audioservicebackend.repositoriy;

import me.fckng0d.audioservicebackend.model.Image;
import me.fckng0d.audioservicebackend.model.user.User;
import me.fckng0d.audioservicebackend.model.user.UserProfileDataRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileDataRelationRepository extends JpaRepository<UserProfileDataRelation, UUID>,
        JpaSpecificationExecutor<UserProfileDataRelation> {
    Optional<UserProfileDataRelation> findByUser(User user);
    Optional<UserProfileDataRelation> findByProfileImage(Image profileImage);
}
