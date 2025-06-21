package io.github.jspinak.brobot.primatives.enums;

/**
 * Represents directional relationships in state transitions and navigation.
 * 
 * <p>Direction provides a simple but essential enumeration for specifying the 
 * orientation of relationships between states, transitions, and navigation paths. 
 * This binary distinction between TO and FROM enables clear expression of 
 * directional semantics throughout the framework, particularly in state graph 
 * traversal and transition definitions.</p>
 * 
 * <p>Usage contexts:
 * <ul>
 *   <li><b>State Transitions</b>: Specify transition direction in state graphs</li>
 *   <li><b>Path Navigation</b>: Indicate traversal direction along paths</li>
 *   <li><b>Relationship Queries</b>: Find states connected TO or FROM a given state</li>
 *   <li><b>Animation Direction</b>: Control visual feedback direction</li>
 *   <li><b>Data Flow</b>: Express information flow between components</li>
 * </ul>
 * </p>
 * 
 * <p>Common patterns:
 * <ul>
 *   <li>TO: Moving towards a target state or destination</li>
 *   <li>FROM: Coming from a source state or origin</li>
 *   <li>Bidirectional: Using both TO and FROM for complete relationships</li>
 *   <li>Query filters: Finding all transitions TO or FROM specific states</li>
 * </ul>
 * </p>
 * 
 * <p>Examples in state management:
 * <ul>
 *   <li>Find all states reachable FROM current state (TO direction)</li>
 *   <li>Find all states that can reach current state (FROM direction)</li>
 *   <li>Define transition direction: LoginState TO HomeState</li>
 *   <li>Reverse navigation: Going FROM destination back TO origin</li>
 * </ul>
 * </p>
 * 
 * <p>Integration points:
 * <ul>
 *   <li>StateTransitionsJointTable: Indexes transitions by direction</li>
 *   <li>PathManager: Uses direction for path traversal</li>
 *   <li>AdjacentStates: Queries neighboring states by direction</li>
 *   <li>TransitionFunction: Specifies execution direction</li>
 * </ul>
 * </p>
 * 
 * <p>Semantic clarity:
 * <ul>
 *   <li>TO implies forward movement or target-oriented action</li>
 *   <li>FROM implies backward reference or source-oriented query</li>
 *   <li>Direction-neutral operations don't use this enum</li>
 *   <li>Always relative to a reference point (usually current state)</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Direction enables precise specification of 
 * navigational intent within the state graph. This simple abstraction supports 
 * complex graph operations while maintaining code readability and preventing 
 * ambiguity in directional operations.</p>
 * 
 * @since 1.0
 * @see StateTransitionsJointTable
 * @see AdjacentStates
 * @see PathManager
 * @see IStateTransition
 */
public enum Direction {
    TO, FROM
}
