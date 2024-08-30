package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegionRequest {
    private Long id;
    private int x;
    private int y;
    private int w;
    private int h;
}
