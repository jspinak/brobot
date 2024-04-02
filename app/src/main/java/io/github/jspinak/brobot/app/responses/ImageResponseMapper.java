package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.awt.image.BufferedImage;

@Mapper(componentModel = "spring")
public interface ImageResponseMapper {
    ImageResponseMapper INSTANCE = Mappers.getMapper(ImageResponseMapper.class);

    @Mapping(source = "bufferedImage", target = "imageBase64", qualifiedByName = "convertBufferedImageToBase64")
    ImageResponse map(Image image);

    @Named("convertBufferedImageToBase64")
    default String convertBufferedImageToBase64(BufferedImage bufferedImage) {
        return BufferedImageOps.encodeImage(bufferedImage);
    }

    @Mapping(source = "imageBase64", target = "bufferedImage", qualifiedByName = "convertBase64ToBufferedImage")
    Image map(ImageResponse imageResponse);

    @Named("convertBase64ToBufferedImage")
    default BufferedImage convertBase64ToBufferedImage(String base64) {
        return BufferedImageOps.base64StringToImage(base64);
    }
}
