package io.github.jspinak.brobot.manageStates;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Data-driven state transition implementation for the Brobot framework.
 * 
 * <p>ActionDefinitionStateTransition represents transitions defined declaratively through 
 * ActionDefinition objects. This implementation enables configuration-based state navigation 
 * where transition logic is expressed as a sequence of actions rather than code, supporting 
 * both programmatic creation and visual design through web UIs.</p>
 * 
 * <p>Key components:
 * <ul>
 *   <li><b>Action Definition</b>: Declarative sequence of actions to execute the transition</li>
 *   <li><b>Activation Set</b>: State IDs to activate after successful transition</li>
 *   <li><b>Exit Set</b>: State IDs to deactivate after successful transition</li>
 *   <li><b>Visibility Control</b>: Whether source state remains visible post-transition</li>
 *   <li><b>Path Score</b>: Weight for path-finding algorithms (higher = less preferred)</li>
 * </ul>
 * </p>
 * 
 * <p>Design patterns and integration:
 * <ul>
 *   <li>Equivalent to TransitionEntity in web UI contexts</li>
 *   <li>Supports both code-based and UI-based transition creation</li>
 *   <li>Uses state IDs directly (unlike JavaStateTransition which uses names)</li>
 *   <li>Fully serializable for persistence and configuration</li>
 * </ul>
 * </p>
 * 
 * <p>Transition execution flow:
 * <ol>
 *   <li>ActionDefinition steps are executed sequentially</li>
 *   <li>If all steps succeed, transition is considered successful</li>
 *   <li>States in 'activate' set become active</li>
 *   <li>States in 'exit' set become inactive</li>
 *   <li>Success counter is incremented for reliability metrics</li>
 * </ol>
 * </p>
 * 
 * <p>Common use patterns:
 * <ul>
 *   <li>Visual workflow design through web interfaces</li>
 *   <li>Configuration-file based automation definitions</li>
 *   <li>Reusable transition templates</li>
 *   <li>Dynamic workflow modification without recompilation</li>
 * </ul>
 * </p>
 * 
 * <p>Benefits over code-based transitions:
 * <ul>
 *   <li>No compilation required for changes</li>
 *   <li>Accessible to non-programmers through visual tools</li>
 *   <li>Easily serializable and versionable</li>
 *   <li>Supports runtime modification of behavior</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, ActionDefinitionStateTransition enables a declarative 
 * style of automation definition that separates the "what" from the "how". This makes 
 * automation scripts more maintainable, testable, and accessible to domain experts who 
 * may not be programmers.</p>
 * 
 * @since 1.0
 * @see IStateTransition
 * @see JavaStateTransition
 * @see ActionDefinition
 * @see StateTransitions
 */
@Data
public class ActionDefinitionStateTransition implements IStateTransition {
    //private String type = "actionDefinition";
    private ActionDefinition actionDefinition;

    private StaysVisible staysVisibleAfterTransition = StaysVisible.NONE;
    private Set<Long> activate = new HashSet<>();
    private Set<Long> exit = new HashSet<>();
    private int score = 0;
    private int timesSuccessful = 0;

    @JsonIgnore
    @Override
    public Optional<ActionDefinition> getActionDefinitionOptional() {
        return Optional.ofNullable(actionDefinition);
    }

    @Override
    public String toString() {
        return "ActionDefinitionStateTransition{" +
                ", activate=" + activate +
                ", exit=" + exit +
                '}';
    }
}
