package io.github.jspinak.brobot.model.transition;

import io.github.jspinak.brobot.action.Action;

@FunctionalInterface
public interface TransitionFunction {
    boolean execute(Action action, Object... context);
}
