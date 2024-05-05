package me.fckng0d.audioservicebackend.repositoriy;

import me.fckng0d.audioservicebackend.model.AudioData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AudioDataRepository extends JpaRepository<AudioData, UUID> {

}

