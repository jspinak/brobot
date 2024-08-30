package io.github.jspinak.brobot.app.web.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StateTransitionsResponse {
    private Long id;
    private Long stateId;
    private List<TransitionResponse> transitions = new ArrayList<>();
    private TransitionResponse finishTransition;
}