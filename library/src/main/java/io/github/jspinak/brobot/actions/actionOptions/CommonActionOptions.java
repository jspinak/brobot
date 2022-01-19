package io.github.jspinak.brobot.actions.actionOptions;

import org.springframework.stereotype.Component;

@Component
public class CommonActionOptions {

    public ActionOptions standard(ActionOptions.Action action, double maxWait) {
        return new ActionOptions.Builder()
                .setAction(action)
                .setMaxWait(maxWait)
                .build();
    }

    public ActionOptions findAndMultipleClicks(double maxWait, int clicks) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setMaxWait(maxWait)
                .setTimesToRepeatIndividualAction(clicks)
                .build();
    }

    public ActionOptions type(String modifier) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.TYPE)
                .setModifiers(modifier)
                .build();
    }

    public ActionOptions type() {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.TYPE)
                .build();
    }
}
