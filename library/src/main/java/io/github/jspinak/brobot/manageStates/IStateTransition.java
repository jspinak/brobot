package io.github.jspinak.brobot.manageStates;

import java.util.Set;
import java.util.function.Function;

public interface IStateTransition {

    // when set to NONE, the StaysVisible variable in the corresponding StateTransitions object will be used.
    enum StaysVisible {
        NONE, TRUE, FALSE
    }

    /**
     * When set, takes precedence over the same variable in StateTransitions.
     * Only applies to FromTransitions.
     */
    StaysVisible getStaysVisibleAfterTransition();
    void setStaysVisibleAfterTransition(StaysVisible staysVisible);

    Set<Long> getActivate();
    void setActivate(Set<Long> activate);

    Set<Long> getExit();
    void setExit(Set<Long> exit);

    int getScore(); // larger path scores discourage taking a path with this transition
    void setScore(int score);

    int getTimesSuccessful();
    void setTimesSuccessful(int timesSuccessful);

    void convertNamesToIds(Function<String, Long> nameToIdConverter);

}
