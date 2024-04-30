package io.github.jspinak.brobot.app.database.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class PositionEmbeddable {
    private double percentW;
    private double percentH;
}
