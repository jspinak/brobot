package io.github.jspinak.brobot.app.web.requests;

import io.github.jspinak.brobot.manageStates.JavaStateTransition;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class TransitionUpdateRequest {
    private Long id;
    private Long sourceStateId;
    private Long targetStateId;
    private Long stateImageId;
    private JavaStateTransition.StaysVisible staysVisibleAfterTransition;
    private Set<Long> activateStateIds = new HashSet<>();
    private Set<Long> exitStateIds = new HashSet<>();
    private Integer score;
    private Integer timesSuccessful;
    private ActionDefinitionRequest actionDefinition;
}