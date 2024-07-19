package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.web.responses.ImageResponse;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.stringUtils.Base64Converter;
import org.springframework.stereotype.Component;

@Component
public class ImageResponseMapper {

    public ImageResponse map(ImageEntity image) {
        ImageResponse imageResponse = new ImageResponse();
        imageResponse.setId(image.getId());
        imageResponse.setName(image.getName());
        imageResponse.setImageBase64(Base64Converter.convert(image.getBytes()));
        return imageResponse;
    }

    public Image map(ImageResponse imageResponse) {
        Image image = new Image(BufferedImageOps.base64StringToImage(imageResponse.getImageBase64()));
        image.setName(imageResponse.getName());
        return image;
    }
}
