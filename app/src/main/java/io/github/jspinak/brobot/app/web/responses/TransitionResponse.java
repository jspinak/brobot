package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.jspinak.brobot.manageStates.JavaStateTransition;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL) // Include non-null properties only
@Getter
@Setter
public class TransitionResponse {

    private Long id;
    private Long sourceStateId;
    private Long targetStateId;
    private Long stateImageId;
    private JavaStateTransition.StaysVisible staysVisibleAfterTransition;
    private Set<Long> activate;
    private Set<Long> exit;
    private int score;
    private int timesSuccessful;
    private ActionDefinitionResponse actionDefinition;

}
