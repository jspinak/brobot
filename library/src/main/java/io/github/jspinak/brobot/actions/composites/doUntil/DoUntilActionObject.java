package io.github.jspinak.brobot.actions.composites.doUntil;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.Getter;

/**
 * The DoUntilActionObject contains both ActionOptions and ObjectCollections for both
 * 'do' and 'until' Actions. Additionally, it holds any options specific to the DoUntil operation.
 * Instances should be created with the builder.
 */
@Getter
public class DoUntilActionObject {

    private ObjectCollection actionObjectCollection;
    private ObjectCollection conditionObjectCollection;
    private ActionOptions doAction;
    private ActionOptions untilAction;
    private int maxActions = 3;

    //results
    private int totalSuccessfulActions = 0;
    private Matches actionMatches = new Matches();
    private Matches conditionMatches = new Matches();
    private boolean success = false;

    public void resetTimesActedOn() {
        actionObjectCollection.resetTimesActedOn();
    }

    public static class Builder {
        private ObjectCollection actionObjectCollection;
        private ObjectCollection conditionObjectCollection;
        private ActionOptions doAction;
        private ActionOptions untilAction;
        private int maxActions = 3;

        public Builder() {
        }

        public Builder setActionObjectCollection(ObjectCollection objectCollection) {
            this.actionObjectCollection = objectCollection;
            return this;
        }

        public Builder setConditionObjectCollection(ObjectCollection objectCollection) {
            this.conditionObjectCollection = objectCollection;
            return this;
        }

        public Builder setDoAction(ActionOptions actionOptions) {
            this.doAction = actionOptions;
            return this;
        }

        public Builder setUntilAction(ActionOptions actionOptions) {
            this.untilAction = actionOptions;
            return this;
        }

        public Builder setMaxActions(int maxActions) {
            this.maxActions = maxActions;
            return this;
        }

        public DoUntilActionObject build() {
            DoUntilActionObject doUntilActionObject = new DoUntilActionObject();
            doUntilActionObject.actionObjectCollection = actionObjectCollection;
            doUntilActionObject.conditionObjectCollection = conditionObjectCollection;
            doUntilActionObject.doAction = doAction;
            doUntilActionObject.untilAction = untilAction;
            doUntilActionObject.maxActions = maxActions;
            return doUntilActionObject;
        }
    }
}
