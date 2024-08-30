package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class StateTransitionsUpdateRequest {

    private List<TransitionUpdateRequest> transitions;
    private TransitionUpdateRequest finishTransition;

}
