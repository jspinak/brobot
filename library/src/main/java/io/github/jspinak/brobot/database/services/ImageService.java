package io.github.jspinak.brobot.database.services;

import io.github.jspinak.brobot.database.data.ImageRepo;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final ImageRepo imageRepo;

    public ImageService(ImageRepo imageRepo) {
        this.imageRepo = imageRepo;
    }

    public List<Image> getImages(String name) {
        return imageRepo.findByName(name).stream()
                .peek(Image::setBufferedImageFromBytes)
                .collect(Collectors.toList());
    }

    public List<Image> getAllImages() {
        return imageRepo.findAll().stream()
                .peek(Image::setBufferedImageFromBytes)
                .collect(Collectors.toList());
    }

    public void saveImages(Image... images) {
        saveImages(List.of(images));
    }

    public void saveImages(List<Image> images) {
        images.forEach(image -> {
            image.setBytesForPersistence();
            imageRepo.save(image);
        });
    }
}
