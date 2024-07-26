package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ImageEntityMapper {

    public ImageEntity map(Image image) {
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setName(image.getName());
        imageEntity.setBytes(BufferedImageOps.toByteArray(image.getBufferedImage()));
        return imageEntity;
    }

    public Image map(ImageEntity imageEntity) {
        Image image = new Image(BufferedImageOps.fromByteArray(imageEntity.getBytes()));
        image.setName(imageEntity.getName());
        return image;
    }

    public List<ImageEntity> mapToImageEntityList(List<Image> scenes) {
        List<ImageEntity> imageEntityList = new ArrayList<>();
        scenes.forEach(scene -> imageEntityList.add(map(scene)));
        return imageEntityList;
    }

    public List<Image> mapToImageList(List<ImageEntity> imageEntities) {
        List<Image> images = new ArrayList<>();
        imageEntities.forEach(imageEntity -> images.add(map(imageEntity)));
        return images;
    }
}
