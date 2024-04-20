package me.fckng0d.audioservicebackend.repositoriy;

import me.fckng0d.audioservicebackend.model.user.User;
import me.fckng0d.audioservicebackend.model.user.UserFavorites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFavoritesRepository extends JpaRepository<UserFavorites, UUID> {
    Optional<UserFavorites> findByUser(User user);
}
