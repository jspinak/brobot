package io.github.jspinak.brobot.model.state.special;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;

/**
 * Defines special state types with reserved IDs in the Brobot framework.
 *
 * <p>SpecialStateType represents meta-states and special conditions that are not part of the
 * application's regular state graph but are essential for state management operations. These
 * special states use negative IDs to distinguish them from regular application states, which use
 * positive IDs, ensuring no conflicts in the state identification system.
 *
 * <p>Special state types:
 *
 * <ul>
 *   <li><b>UNKNOWN (-1)</b>: Represents an unidentified or unrecognized state condition
 *   <li><b>PREVIOUS (-2)</b>: References the state(s) that were active before a transition
 *   <li><b>CURRENT (-3)</b>: Represents the currently active state(s) in the system
 *   <li><b>EXPECTED (-4)</b>: Indicates state(s) expected to become active after a transition
 *   <li><b>NULL (-5)</b>: Represents stateless elements or the absence of state context
 * </ul>
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Reserved IDs</b>: Negative IDs prevent collision with regular state IDs
 *   <li><b>Type Safety</b>: Enum ensures only valid special states are referenced
 *   <li><b>ID Lookup</b>: Supports reverse lookup from ID to enum constant
 *   <li><b>Validation</b>: Methods to check if an ID represents a special state
 * </ul>
 *
 * <p>Common usage patterns:
 *
 * <ul>
 *   <li>State queries: "Find element X in CURRENT state"
 *   <li>Transition logic: "Return to PREVIOUS state"
 *   <li>Expectations: "Verify EXPECTED state is reached"
 *   <li>Stateless operations: "Execute action in NULL state context"
 * </ul>
 *
 * <p>In the model-based approach, SpecialStateType enables sophisticated state management patterns
 * that go beyond simple state identification. These meta-states allow the framework to handle
 * relative state references, track state history, and manage expectations about future states, all
 * of which are crucial for robust automation that can adapt to dynamic GUI behavior.
 *
 * <p>The use of negative IDs is a deliberate design choice that creates a clear separation between
 * the application's state space and the framework's meta-state concepts, preventing any possibility
 * of confusion or collision between the two.
 *
 * @since 1.0
 * @see StateEnum
 * @see State
 * @see StateMemory
 */
public enum SpecialStateType {
    UNKNOWN(-1L),
    PREVIOUS(-2L),
    CURRENT(-3L),
    EXPECTED(-4L),
    NULL(-5L);

    private final Long id;

    SpecialStateType(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public static SpecialStateType fromId(Long id) {
        for (SpecialStateType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No SpecialStateType with id " + id);
    }

    public static boolean isSpecialStateId(Long id) {
        for (SpecialStateType type : values()) {
            if (type.id.equals(id)) {
                return true;
            }
        }
        return false;
    }
}
