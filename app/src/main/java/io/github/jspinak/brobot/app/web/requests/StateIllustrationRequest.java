package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateIllustrationRequest {
    private ImageRequest screenshot;
    private ImageRequest illustratedScreenshot;
}
