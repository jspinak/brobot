package io.github.jspinak.brobot.app.web.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionStepResponse {
    private Long id;
    private ActionOptionsResponse actionOptions;
    private ObjectCollectionResponse objectCollection;
}
