package io.github.jspinak.brobot.app.database.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class RegionEmbeddable {

    private int x;
    private int y;
    private int w;
    private int h;

}
