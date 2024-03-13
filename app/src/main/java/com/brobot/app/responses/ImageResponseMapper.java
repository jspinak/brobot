package com.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

@Mapper(componentModel = "spring")
@Component
public interface ImageResponseMapper {
    ImageResponseMapper INSTANCE = Mappers.getMapper(ImageResponseMapper.class);

    @Mapping(source = "bufferedImage", target = "imageBase64", qualifiedByName = "convertBufferedImageToBase64")
    ImageResponse mapToResponse(Image image);

    @Named("convertBufferedImageToBase64")
    default String convertBufferedImageToBase64(BufferedImage bufferedImage) {
        return BufferedImageOps.encodeImage(bufferedImage);
    }
}
