package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionRequest {
    private double percentW;
    private double percentH;
}
