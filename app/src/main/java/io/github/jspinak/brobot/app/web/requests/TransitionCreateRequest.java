package io.github.jspinak.brobot.app.web.requests;

import io.github.jspinak.brobot.manageStates.JavaStateTransition;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class TransitionCreateRequest {
    private Long sourceStateId;
    private Long stateImageId;
    private JavaStateTransition.StaysVisible staysVisibleAfterTransition;
    private Set<Long> statesToEnter = new HashSet<>(); // Renamed from 'activate'
    private Set<Long> statesToExit = new HashSet<>(); // Renamed from 'exit'
    private int score;
    private int timesSuccessful;
    private ActionDefinitionRequest actionDefinition;
}