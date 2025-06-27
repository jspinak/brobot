/**
 * Service layer for state and transition management.
 * 
 * <p>This package provides service interfaces for accessing and managing states and
 * transitions within the Brobot framework. These services act as the primary API
 * for other components to interact with the state model, ensuring consistent
 * access patterns and centralized management of the automation structure.</p>
 * 
 * <h2>Core Services</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.navigation.service.StateService} - 
 *       Comprehensive state management including CRUD operations and queries</li>
 *   <li>{@link io.github.jspinak.brobot.navigation.service.StateTransitionService} - 
 *       Manages transition definitions and relationships between states</li>
 * </ul>
 * 
 * <h2>Service Responsibilities</h2>
 * 
 * <h3>StateService</h3>
 * <p>Primary interface for state operations:</p>
 * <ul>
 *   <li>State retrieval by ID, name, or enum</li>
 *   <li>Bulk state operations</li>
 *   <li>Name-to-ID resolution</li>
 *   <li>State persistence and lifecycle</li>
 *   <li>Visit counter management</li>
 * </ul>
 * 
 * <h3>StateTransitionService</h3>
 * <p>Manages transition relationships:</p>
 * <ul>
 *   <li>Transition registration and storage</li>
 *   <li>Transition query by state pairs</li>
 *   <li>Relationship mapping maintenance</li>
 *   <li>Transition availability checking</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>State Operations</h3>
 * <pre>{@code
 * StateService stateService = context.getBean(StateService.class);
 * 
 * // Retrieve state by name
 * Optional<State> loginState = stateService.findByName("LoginScreen");
 * 
 * // Get state ID from name
 * Long stateId = stateService.getStateId("Dashboard");
 * 
 * // Retrieve multiple states
 * Set<State> states = stateService.getStates("Login", "Dashboard", "Settings");
 * 
 * // Save new state
 * State newState = new State.Builder()
 *     .withName("NewFeature")
 *     .build();
 * stateService.save(newState);
 * 
 * // Reset visit counters for fresh run
 * stateService.resetTimesVisited();
 * }</pre>
 * 
 * <h3>Transition Management</h3>
 * <pre>{@code
 * StateTransitionService transitionService = 
 *     context.getBean(StateTransitionService.class);
 * 
 * // Register new transition
 * StateTransition loginTransition = new StateTransition.Builder()
 *     .setFromState(loginScreen)
 *     .setToState(dashboard)
 *     .build();
 * transitionService.add(loginTransition);
 * 
 * // Query transitions
 * Set<StateTransition> fromLogin = 
 *     transitionService.getTransitionsFrom(loginId);
 * 
 * // Check if direct transition exists
 * boolean canTransition = 
 *     transitionService.hasTransition(currentId, targetId);
 * }</pre>
 * 
 * <h3>State Discovery</h3>
 * <pre>{@code
 * // Find all states that can reach target
 * Set<Long> predecessors = transitionService
 *     .getStatesWithTransitionsTo(targetId);
 * 
 * // Find all states reachable from current
 * Set<Long> successors = transitionService
 *     .getStatesWithTransitionsFrom(currentId);
 * 
 * // Get complete state graph information
 * Map<Long, Set<Long>> stateGraph = transitionService
 *     .getCompleteTransitionMap();
 * }</pre>
 * 
 * <h2>Service Design Patterns</h2>
 * 
 * <h3>Repository Pattern</h3>
 * <p>Services delegate to underlying repositories:</p>
 * <ul>
 *   <li>StateService uses StateStore</li>
 *   <li>StateTransitionService uses transition storage</li>
 *   <li>Provides abstraction over storage details</li>
 * </ul>
 * 
 * <h3>Optional Returns</h3>
 * <p>Services use Optional for potentially missing data:</p>
 * <pre>{@code
 * Optional<State> state = stateService.findByName("MayNotExist");
 * state.ifPresent(s -> {
 *     // Process state
 * });
 * }</pre>
 * 
 * <h3>Bulk Operations</h3>
 * <p>Services support efficient bulk operations:</p>
 * <pre>{@code
 * // Get multiple states in one call
 * Set<State> states = stateService.getStates(stateIds);
 * 
 * // Add multiple transitions
 * transitionService.addAll(transitionList);
 * }</pre>
 * 
 * <h2>State Identification</h2>
 * 
 * <p>Services support multiple identification methods:</p>
 * <ul>
 *   <li><b>State ID (Long)</b> - Internal numeric identifier</li>
 *   <li><b>State Name (String)</b> - Human-readable name</li>
 *   <li><b>State Enum</b> - Type-safe enum reference</li>
 * </ul>
 * 
 * <pre>{@code
 * // By ID
 * State byId = stateService.get(123L);
 * 
 * // By name
 * State byName = stateService.findByName("LoginScreen").orElseThrow();
 * 
 * // By enum
 * State byEnum = stateService.get(States.LOGIN_SCREEN);
 * }</pre>
 * 
 * <h2>Performance Considerations</h2>
 * 
 * <ul>
 *   <li>Services may cache frequently accessed data</li>
 *   <li>Bulk operations reduce round trips</li>
 *   <li>Lazy loading for complex state objects</li>
 *   <li>Efficient indexing for name lookups</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>Services are designed for concurrent access:</p>
 * <ul>
 *   <li>Thread-safe read operations</li>
 *   <li>Synchronized write operations where needed</li>
 *   <li>Immutable return values where possible</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li>Always check Optional returns before using values</li>
 *   <li>Use bulk operations when dealing with multiple items</li>
 *   <li>Cache service references rather than repeated lookups</li>
 *   <li>Handle missing states gracefully</li>
 *   <li>Use appropriate identification method for context</li>
 * </ol>
 * 
 * <h2>Integration</h2>
 * 
 * <p>Services are used throughout the framework:</p>
 * <ul>
 *   <li>PathFinder uses services to resolve state names</li>
 *   <li>TransitionExecutor queries transition availability</li>
 *   <li>StateNavigator relies on both services</li>
 *   <li>Monitoring components query active states</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state.State
 * @see io.github.jspinak.brobot.model.transition.StateTransition
 * @see io.github.jspinak.brobot.model.state.StateStore
 */
package io.github.jspinak.brobot.navigation.service;