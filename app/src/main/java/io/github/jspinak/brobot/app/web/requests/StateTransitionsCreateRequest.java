package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class StateTransitionsCreateRequest {

    private Long stateId;
    private List<TransitionCreateRequest> transitions;
    private TransitionCreateRequest finishTransition;

}
