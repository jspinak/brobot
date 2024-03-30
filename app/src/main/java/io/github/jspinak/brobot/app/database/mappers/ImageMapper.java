package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
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
public interface ImageMapper {
    ImageMapper INSTANCE = Mappers.getMapper(ImageMapper.class);

    @Mapping(source = "bufferedImage", target = "bytes", qualifiedByName = "convertBufferedImageToBytes")
    ImageEntity mapToEntity(Image image);

    @Mapping(source = "bytes", target = "bufferedImage", qualifiedByName = "convertBytesToBufferedImage")
    Image mapFromEntity(ImageEntity imageEntity);

    @Named("convertBufferedImageToBytes")
    default byte[] convertBufferedImageToBytes(BufferedImage bufferedImage) {
        return BufferedImageOps.toByteArray(bufferedImage);
    }

    @Named("convertBytesToBufferedImage")
    default BufferedImage convertBytesToBufferedImage(byte[] bytes) {
        return BufferedImageOps.fromByteArray(bytes);
    }
}
