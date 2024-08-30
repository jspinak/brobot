package io.github.jspinak.brobot.app.web.requests;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionOptionsRequest {
    private Long id;
    private ActionOptions.Action action;
    private ActionOptions.Find find;
    private boolean keepLargerMatches;
    private double similarity;
}

