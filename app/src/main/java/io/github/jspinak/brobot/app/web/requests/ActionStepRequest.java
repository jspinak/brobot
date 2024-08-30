package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionStepRequest {
    private ActionOptionsRequest actionOptions;
    private ObjectCollectionRequest objectCollection;
}