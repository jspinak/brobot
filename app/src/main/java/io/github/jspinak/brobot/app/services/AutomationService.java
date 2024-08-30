package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.models.BuildModel;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Service;

@Service
public class AutomationService {

    private final BuildModel buildModel;
    private final Action action;

    public AutomationService(BuildModel buildModel, Action action) {
        this.buildModel = buildModel;
        this.action = action;
    }

    public String runAutomation() {
        buildModel.build();
        return "Model built and integrating into the Brobot framework.";
    }

    public String testMouseMove() {
        action.perform(ActionOptions.Action.MOVE, new Location(500, 500).asObjectCollection());
        return "Moved mouse to 500, 500.";
    }

}