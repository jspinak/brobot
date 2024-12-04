package io.github.jspinak.brobot.log.entities;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class StateImageLog {

    private Long stateImageId;
    private boolean found;
}
