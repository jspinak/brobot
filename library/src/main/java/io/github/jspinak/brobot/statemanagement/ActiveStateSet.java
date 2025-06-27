package io.github.jspinak.brobot.statemanagement;

import java.util.HashSet;
import java.util.Set;

import io.github.jspinak.brobot.model.state.StateEnum;

/**
 * Manages a collection of currently active states in the Brobot framework.
 * 
 * <p>ActiveStateSet provides a lightweight container for tracking which states are currently 
 * active in the GUI using StateEnum identifiers. This class is particularly useful during 
 * the development phase when working with enum-based state definitions, before transitioning 
 * to the more robust database-backed state management system.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Set Semantics</b>: Ensures each state is tracked only once, preventing duplicates</li>
 *   <li><b>Bulk Operations</b>: Supports adding individual states or entire collections</li>
 *   <li><b>Enum-based</b>: Works with StateEnum interface for compile-time type safety</li>
 *   <li><b>Merge Support</b>: Can combine multiple ActiveStateSet instances</li>
 * </ul>
 * </p>
 * 
 * <p>Use cases:
 * <ul>
 *   <li>Tracking active states during automation execution</li>
 *   <li>Building state sets for transition operations</li>
 *   <li>Maintaining state context in enum-based automation scripts</li>
 *   <li>Debugging state transitions by examining active state sets</li>
 * </ul>
 * </p>
 * 
 * <p>This class represents a simpler alternative to StateMemory for scenarios where 
 * database integration is not required. It's particularly useful for:
 * <ul>
 *   <li>Unit testing state management logic</li>
 *   <li>Lightweight automation scripts</li>
 *   <li>Prototyping state structures before database implementation</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, ActiveStateSet provides a foundation for tracking the 
 * current GUI configuration without the overhead of full state management infrastructure. 
 * This makes it ideal for simpler automation scenarios or as a stepping stone to more 
 * sophisticated state tracking mechanisms.</p>
 * 
 * @since 1.0
 * @see StateEnum
 * @see StateMemory
 * @see State
 */
public class ActiveStateSet {

    private Set<StateEnum> activeStates = new HashSet<>();

    public void addState(StateEnum stateEnum) {
        activeStates.add(stateEnum);
    }

    public void addStates(Set<StateEnum> states) {
        activeStates.addAll(states);
    }

    public void addStates(ActiveStateSet activeStates) {
        this.activeStates.addAll(activeStates.getActiveStates());
    }

    public Set<StateEnum> getActiveStates() {
        return activeStates;
    }

}
