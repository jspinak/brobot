package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.web.requests.ImageRequest;
import io.github.jspinak.brobot.app.web.responses.ImageResponse;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.stringUtils.Base64Converter;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class ImageResponseMapper {

    public ImageResponse map(ImageEntity image) {
        ImageResponse imageResponse = new ImageResponse();
        imageResponse.setId(image.getId());
        imageResponse.setName(image.getName());
        imageResponse.setImageBase64(Base64Converter.convert(image.getBytes()));
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
            imageEntity.setBytes(bytes);
        }
        return imageEntity;
    }

    public ImageEntity fromRequest(ImageRequest request) {
        if (request == null) return null;
        ImageEntity entity = new ImageEntity();
        entity.setId(request.getId());
        entity.setName(request.getName());
        entity.setBytes(BufferedImageOps.base64StringToByteArray(request.getImageBase64()));
        return entity;
    }

    public ImageRequest toRequest(ImageEntity entity) {
        if (entity == null) {
            return null;
        }
        ImageRequest request = new ImageRequest();
        request.setId(entity.getId());
        request.setName(entity.getName());
        request.setImageBase64(Base64.getEncoder().encodeToString(entity.getBytes()));
        return request;
    }

}
