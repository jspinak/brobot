package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.jspinak.brobot.manageStates.JavaStateTransition;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class TransitionResponse {
    private Long id;
    private Long sourceStateId;
    private Long stateImageId;
    private JavaStateTransition.StaysVisible staysVisibleAfterTransition;
    private Set<Long> statesToEnter; // Renamed from 'activate'
    private Set<Long> statesToExit; // Renamed from 'exit'
    private int score;
    private int timesSuccessful;
    private ActionDefinitionResponse actionDefinition;
}
