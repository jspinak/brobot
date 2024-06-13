package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.ImageEntityMapper;
import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.database.repositories.ImageRepo;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final ImageRepo imageRepo;
    //private ImageMapper imageMapper = ImageMapper.INSTANCE;

    public ImageService(ImageRepo imageRepo) {
        this.imageRepo = imageRepo;
    }

    public List<Image> getImages(String name) {
        List<ImageEntity> imageEntities = imageRepo.findByName(name);
        return imageEntities.stream()
                //.map(imageMapper::map)
                .map(ImageEntityMapper::map)
                .collect(Collectors.toList());
    }

    public List<Image> getAllImages() {
        List<ImageEntity> imageEntities = imageRepo.findAll();
        return imageEntities.stream()
                //.map(imageMapper::map)
                .map(ImageEntityMapper::map)
                .collect(Collectors.toList());
    }

    public void saveImages(Image... images) {
        saveImages(List.of(images));
    }

    public void saveImages(List<Image> images) {
        //images.forEach(image -> imageRepo.save(imageMapper.map(image)));
        images.forEach(image -> imageRepo.save(ImageEntityMapper.map(image)));
    }
}
