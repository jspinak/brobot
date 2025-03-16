package io.github.jspinak.brobot.testingAUTs;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.state.State;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class VisitAllStateImages {

    private final Action action;

    public VisitAllStateImages(Action action) {
        this.action = action;
    }

    public void visitAllStateImages(State state) {
        state.getStateImages().forEach(stateImage -> {
            action.perform(ActionOptions.Action.FIND, stateImage.asObjectCollection());
        });
    }
}
