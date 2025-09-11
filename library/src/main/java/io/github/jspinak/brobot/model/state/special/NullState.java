package io.github.jspinak.brobot.model.state.special;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;

import lombok.Getter;

/**
 * Special state for handling stateless objects in the Brobot framework.
 *
 * <p>NullState provides a container for objects that don't belong to any specific state in the
 * application's state graph. It enables the framework to process temporary or standalone objects
 * using the same action infrastructure designed for state-based automation, maintaining consistency
 * in how all objects are handled.
 *
 * <p>Key characteristics:
 *
 * <ul>
 *   <li><b>Stateless Context</b>: Objects belong to no particular application state
 *   <li><b>No State Activation</b>: Finding these objects doesn't trigger state changes
 *   <li><b>Temporary Objects</b>: Typically used for transient elements or utilities
 *   <li><b>Repository Exclusion</b>: Should not be stored in the state repository
 *   <li><b>Action Compatibility</b>: Can be acted upon like any other state object
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Processing temporary dialogs or notifications
 *   <li>Handling utility objects that appear across multiple states
 *   <li>Working with objects during state transitions
 *   <li>Testing individual patterns without state context
 *   <li>Operating on objects before state structure is established
 * </ul>
 *
 * <p>Design pattern benefits:
 *
 * <ul>
 *   <li>Enables uniform object handling regardless of state association
 *   <li>Simplifies action implementation by avoiding null checks
 *   <li>Provides clear semantic meaning for stateless operations
 *   <li>Maintains type safety in the state system
 * </ul>
 *
 * <p>Implementation notes:
 *
 * <ul>
 *   <li>Contains a single State instance named "null"
 *   <li>Implements StateEnum through the Name enum for type compatibility
 *   <li>Should be used sparingly - most objects should belong to states
 *   <li>Not a singleton to allow multiple contexts if needed
 * </ul>
 *
 * <p>In the model-based approach, NullState represents the framework's acknowledgment that not all
 * GUI elements fit neatly into state categories. It provides a pragmatic solution for handling edge
 * cases while maintaining the benefits of the state-based architecture for the majority of
 * automation scenarios.
 *
 * @since 1.0
 * @see State
 * @see StateEnum
 * @see SpecialStateType
 * @see ObjectCollection
 */
@Getter
public class NullState {

    // convert simple objects to state objects

    public enum Name implements StateEnum {
        NULL
    }

    private final State state = new State.Builder("null").build();
}
