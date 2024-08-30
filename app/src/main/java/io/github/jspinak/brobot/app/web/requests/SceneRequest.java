package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SceneRequest {
    private Long id;
    private PatternRequest pattern;
}
