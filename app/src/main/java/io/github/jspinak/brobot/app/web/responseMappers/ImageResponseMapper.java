package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.web.responses.ImageResponse;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;

public class ImageResponseMapper {

    public static ImageResponse map(Image image) {
        ImageResponse imageResponse = new ImageResponse();
        imageResponse.setName(image.getName());
        imageResponse.setImageBase64(BufferedImageOps.encodeImage(image.getBufferedImage()));
        return imageResponse;
    }

    public static Image map(ImageResponse imageResponse) {
        Image image = new Image(BufferedImageOps.base64StringToImage(imageResponse.getImageBase64()));
        image.setName(imageResponse.getName());
        return image;
    }
}
