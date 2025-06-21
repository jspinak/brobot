package io.github.jspinak.brobot.manageStates;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.jspinak.brobot.dsl.ActionDefinition;

import java.util.Optional;
import java.util.Set;

/**
 * Core interface for state transitions in the Brobot model-based GUI automation framework.
 * 
 * <p>IStateTransition defines the contract for all transition types in the state graph, 
 * enabling polymorphic handling of different transition implementations. It represents 
 * the edges in the state structure (Ω) that enable navigation between GUI configurations, 
 * supporting both programmatic (Java code) and declarative (ActionDefinition) transitions.</p>
 * 
 * <p>Transition implementations:
 * <ul>
 *   <li><b>ActionDefinitionStateTransition</b>: Data-driven transitions using action sequences</li>
 *   <li><b>JavaStateTransition</b>: Code-based transitions using BooleanSupplier functions</li>
 * </ul>
 * </p>
 * 
 * <p>Key properties:
 * <ul>
 *   <li><b>Activation States</b>: Set of states that this transition activates (navigates to)</li>
 *   <li><b>Exit States</b>: Set of states that are exited when this transition executes</li>
 *   <li><b>Visibility Control</b>: Determines if the source state remains visible after transition</li>
 *   <li><b>Score</b>: Path-finding weight (higher scores discourage using this transition)</li>
 *   <li><b>Success Tracking</b>: Count of successful executions for reliability metrics</li>
 * </ul>
 * </p>
 * 
 * <p>Visibility behavior (StaysVisible):
 * <ul>
 *   <li><b>NONE</b>: Inherit visibility behavior from StateTransitions container</li>
 *   <li><b>TRUE</b>: Source state remains visible after transition</li>
 *   <li><b>FALSE</b>: Source state becomes hidden after transition</li>
 * </ul>
 * </p>
 * 
 * <p>Path-finding integration:
 * <ul>
 *   <li>Score affects path selection - lower total path scores are preferred</li>
 *   <li>Success count can inform reliability-based path selection</li>
 *   <li>Multiple activation targets enable branching transitions</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, IStateTransition abstracts the mechanism of state 
 * navigation while preserving essential metadata for intelligent path selection. The 
 * polymorphic design allows mixing declarative and programmatic transitions within 
 * the same state graph, providing maximum flexibility for different automation scenarios.</p>
 * 
 * <p>The Jackson annotations enable proper serialization/deserialization of transition 
 * types, supporting configuration-based state structure definitions.</p>
 * 
 * @since 1.0
 * @see StateTransitions
 * @see ActionDefinitionStateTransition
 * @see JavaStateTransition
 * @see PathFinder
 */
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

    Optional<ActionDefinition> getActionDefinitionOptional();

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
