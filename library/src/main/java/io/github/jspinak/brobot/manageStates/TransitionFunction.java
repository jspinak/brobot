package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.actionExecution.Action;

@FunctionalInterface
public interface TransitionFunction {
    boolean execute(Action action, Object... context);
}
