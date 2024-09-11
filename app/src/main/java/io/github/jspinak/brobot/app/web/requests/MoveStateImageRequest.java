package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveStateImageRequest  {
    private Long stateImageId;
    private Long newStateId;

}
