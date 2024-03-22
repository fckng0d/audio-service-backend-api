package me.fckng0d.audioservicebackend.services;

import me.fckng0d.audioservicebackend.models.AudioFile;
import me.fckng0d.audioservicebackend.models.Image;
import me.fckng0d.audioservicebackend.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    @Autowired
    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public void saveImage(MultipartFile imageFile) {
        Image image = new Image();
        image.setFileName(imageFile.getOriginalFilename());
        try {
            image.setData(imageFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        imageRepository.save(image);
    }

    public Image getImageById(UUID id) {
        return imageRepository.getImageByImageId(id);
    }
}
