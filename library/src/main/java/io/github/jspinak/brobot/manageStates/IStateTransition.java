package io.github.jspinak.brobot.manageStates;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.jspinak.brobot.dsl.ActionDefinition;

import java.util.Optional;
import java.util.Set;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActionDefinitionStateTransition.class, name = "actionDefinition"),
        @JsonSubTypes.Type(value = JavaStateTransition.class, name = "java")
})
public interface IStateTransition {

    // when set to NONE, the StaysVisible variable in the corresponding StateTransitions object will be used.
    enum StaysVisible {
        NONE, TRUE, FALSE
    }

    Optional<ActionDefinition> getActionDefinition();

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

    String toString(); // for debugging purposes

}
