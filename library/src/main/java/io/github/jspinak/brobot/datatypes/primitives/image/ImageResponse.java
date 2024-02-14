package io.github.jspinak.brobot.datatypes.primitives.image;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL) // Include non-null properties only
@Getter
public class ImageResponse {

    private Long id = 0L;
    private String name = "";
    private String imageBase64 = "";

    public ImageResponse(Image image) {
        if (image == null) return;
        this.id = image.getId();
        this.name = image.getName();
        this.imageBase64 = BufferedImageOps.encodeImage(image.getBufferedImage());
    }
}
