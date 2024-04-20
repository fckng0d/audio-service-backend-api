package me.fckng0d.audioservicebackend.service;

import me.fckng0d.audioservicebackend.model.Image;
import me.fckng0d.audioservicebackend.repositoriy.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    @Autowired
    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Transactional
    public Image saveImage(MultipartFile imageFile) {
        Image image = new Image();
        image.setFileName(imageFile.getOriginalFilename());
        try {
            image.setData(imageFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return imageRepository.save(image);
    }

    @Transactional
    public void deleteImage(Image image) {
        if (image != null) {
            imageRepository.delete(image);
        }
    }

    public Image getImageById(UUID id) {
        return imageRepository.getImageByImageId(id);
    }
}
