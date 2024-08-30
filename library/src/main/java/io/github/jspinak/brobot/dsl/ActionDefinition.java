package io.github.jspinak.brobot.dsl;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ActionDefinition {
    private List<ActionStep> steps;

    public ActionDefinition() {
        this.steps = new ArrayList<>();
    }

    public void addStep(ActionOptions options, ObjectCollection objects) {
        steps.add(new ActionStep(options, objects));
    }

    public static class ActionStep {
        private ActionOptions options;
        private ObjectCollection objects;

        public ActionStep(ActionOptions options, ObjectCollection objects) {
            this.options = options;
            this.objects = objects;
        }

        public ActionOptions getOptions() {
            return options;
        }

        public ObjectCollection getObjects() {
            return objects;
        }
    }
}
