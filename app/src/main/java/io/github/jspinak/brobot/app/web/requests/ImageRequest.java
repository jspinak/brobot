package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageRequest {
    private Long id;
    private String name = "";
    private Long imageId;
}
