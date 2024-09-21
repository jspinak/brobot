package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.web.requests.ImageRequest;
import io.github.jspinak.brobot.app.web.responses.ImageResponse;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class ImageResponseMapper {

    public ImageResponse map(ImageEntity imageEntity) {
        if (imageEntity == null) return null;
        ImageResponse imageResponse = new ImageResponse();
        imageResponse.setId(imageEntity.getId());
        imageResponse.setName(imageEntity.getName());
        if (imageEntity.getImageData() != null)
            imageResponse.setImageBase64(Base64.getEncoder().encodeToString(imageEntity.getImageData()));
        return imageResponse;
    }

    public ImageEntity map(ImageResponse imageResponse) {
        if (imageResponse == null) {
            return null;
        }
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setId(imageResponse.getId());
        imageEntity.setName(imageResponse.getName());
        if (imageResponse.getImageBase64() != null && !imageResponse.getImageBase64().isEmpty()) {
            byte[] bytes = BufferedImageOps.base64StringToByteArray(imageResponse.getImageBase64());
            imageEntity.setImageData(bytes);
        }
        return imageEntity;
    }

    public ImageEntity fromRequest(ImageRequest request) {
        if (request == null) return null;
        ImageEntity entity = new ImageEntity();
        entity.setId(request.getId());
        entity.setName(request.getName());
        // only an id is provided
        return entity;
    }

}
