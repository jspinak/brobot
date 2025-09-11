package io.github.jspinak.brobot.navigation.transition;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;

import lombok.Data;

/**
 * Data-driven state transition implementation for the Brobot framework.
 *
 * <p>TaskSequenceStateTransition represents transitions defined declaratively through TaskSequence
 * objects. This implementation enables configuration-based state navigation where transition logic
 * is expressed as a sequence of actions rather than code, supporting both programmatic creation and
 * visual design through web UIs.
 *
 * <p>Key components:
 *
 * <ul>
 *   <li><b>Action Definition</b>: Declarative sequence of actions to execute the transition
 *   <li><b>Activation Set</b>: State IDs to activate after successful transition
 *   <li><b>Exit Set</b>: State IDs to deactivate after successful transition
 *   <li><b>Visibility Control</b>: Whether source state remains visible post-transition
 *   <li><b>Path Score</b>: Weight for path-finding algorithms (higher = less preferred)
 * </ul>
 *
 * <p>Design patterns and integration:
 *
 * <ul>
 *   <li>Equivalent to TransitionEntity in web UI contexts
 *   <li>Supports both code-based and UI-based transition creation
 *   <li>Uses state IDs directly (unlike JavaStateTransition which uses names)
 *   <li>Fully serializable for persistence and configuration
 * </ul>
 *
 * <p>Transition execution flow:
 *
 * <ol>
 *   <li>TaskSequence steps are executed sequentially
 *   <li>If all steps succeed, transition is considered successful
 *   <li>States in 'activate' set become active
 *   <li>States in 'exit' set become inactive
 *   <li>Success counter is incremented for reliability metrics
 * </ol>
 *
 * <p>Common use patterns:
 *
 * <ul>
 *   <li>Visual workflow design through web interfaces
 *   <li>Configuration-file based automation definitions
 *   <li>Reusable transition templates
 *   <li>Dynamic workflow modification without recompilation
 * </ul>
 *
 * <p>Benefits over code-based transitions:
 *
 * <ul>
 *   <li>No compilation required for changes
 *   <li>Accessible to non-programmers through visual tools
 *   <li>Easily serializable and versionable
 *   <li>Supports runtime modification of behavior
 * </ul>
 *
 * <p>In the model-based approach, TaskSequenceStateTransition enables a declarative style of
 * automation definition that separates the "what" from the "how". This makes automation scripts
 * more maintainable, testable, and accessible to domain experts who may not be programmers.
 *
 * @since 1.0
 * @see StateTransition
 * @see JavaStateTransition
 * @see TaskSequence
 * @see StateTransitions
 */
@Data
public class TaskSequenceStateTransition implements StateTransition {
    // private String type = "actionDefinition";
    private TaskSequence actionDefinition;

    private StaysVisible staysVisibleAfterTransition = StaysVisible.NONE;
    private Set<Long> activate = new HashSet<>();
    private Set<Long> exit = new HashSet<>();
    private int score = 0;
    private int timesSuccessful = 0;

    @JsonIgnore
    @Override
    public Optional<TaskSequence> getTaskSequenceOptional() {
        return Optional.ofNullable(actionDefinition);
    }

    @Override
    public String toString() {
        return "TaskSequenceStateTransition{" + ", activate=" + activate + ", exit=" + exit + '}';
    }
}
