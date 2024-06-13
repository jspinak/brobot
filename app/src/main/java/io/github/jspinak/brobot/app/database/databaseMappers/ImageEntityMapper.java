package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;

import java.util.ArrayList;
import java.util.List;

public class ImageEntityMapper {

    public static ImageEntity map(Image image) {
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setName(image.getName());
        imageEntity.setBytes(BufferedImageOps.toByteArray(image.getBufferedImage()));
        return imageEntity;
    }

    public static Image map(ImageEntity imageEntity) {
        Image image = new Image(BufferedImageOps.fromByteArray(imageEntity.getBytes()));
        image.setName(imageEntity.getName());
        return image;
    }

    public static List<ImageEntity> mapToImageEntityList(List<Image> scenes) {
        List<ImageEntity> imageEntityList = new ArrayList<>();
        scenes.forEach(scene -> imageEntityList.add(map(scene)));
        return imageEntityList;
    }

    public static List<Image> mapToImageList(List<ImageEntity> imageEntities) {
        List<Image> images = new ArrayList<>();
        imageEntities.forEach(imageEntity -> images.add(map(imageEntity)));
        return images;
    }
}
