package com.brobot.app.services;

import com.brobot.app.database.entities.ImageEntity;
import com.brobot.app.database.mappers.ImageMapper;
import com.brobot.app.database.repositories.ImageRepo;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final ImageRepo imageRepo;
    private final ImageMapper imageMapper;

    public ImageService(ImageRepo imageRepo,
                        ImageMapper imageMapper) {
        this.imageRepo = imageRepo;
        this.imageMapper = imageMapper;
    }

    public List<Image> getImages(String name) {
        List<ImageEntity> imageEntities = imageRepo.findByName(name);
        return imageEntities.stream()
                .map(imageMapper.INSTANCE::mapFromEntity)
                .collect(Collectors.toList());
    }

    public List<Image> getAllImages() {
        List<ImageEntity> imageEntities = imageRepo.findAll();
        return imageEntities.stream()
                .map(imageMapper.INSTANCE::mapFromEntity)
                .collect(Collectors.toList());
    }

    public void saveImages(Image... images) {
        saveImages(List.of(images));
    }

    public void saveImages(List<Image> images) {
        images.forEach(image -> imageRepo.save(imageMapper.INSTANCE.mapToEntity(image)));
    }
}
