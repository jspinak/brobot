/**
 * Special-purpose states for edge cases and system states.
 *
 * <p>This package provides predefined state implementations for special scenarios that occur in GUI
 * automation. These states handle edge cases, error conditions, and system-level states that don't
 * correspond to specific application screens.
 *
 * <h2>Special State Types</h2>
 *
 * <h3>System States</h3>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.state.special.NullState} - Represents the absence of
 *       any state, used as a safe default
 *   <li>{@link io.github.jspinak.brobot.model.state.special.UnknownState} - Represents an
 *       unidentified state when pattern matching fails
 * </ul>
 *
 * <h3>Specialized Objects</h3>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.state.special.StateText} - State object focused on
 *       text content rather than visual patterns
 *   <li>{@link io.github.jspinak.brobot.model.state.special.SpecialStateType} - Enumeration of
 *       special state categories
 * </ul>
 *
 * <h2>Use Cases</h2>
 *
 * <h3>NullState</h3>
 *
 * <p>Used when no state can be determined or as a placeholder:
 *
 * <pre>{@code
 * State currentState = StateService.getCurrentState();
 * if (currentState == NullState.getNullState()) {
 *     // Handle initialization or error condition
 *     initializeApplication();
 * }
 * }</pre>
 *
 * <h3>UnknownState</h3>
 *
 * <p>Represents unrecognized application states:
 *
 * <pre>{@code
 * State detected = stateDetector.detectState();
 * if (detected instanceof UnknownState) {
 *     // Application is in unexpected state
 *     // Attempt recovery or capture diagnostics
 *     captureScreenshot();
 *     navigateToKnownState();
 * }
 * }</pre>
 *
 * <h3>StateText</h3>
 *
 * <p>For states identified primarily by text content:
 *
 * <pre>{@code
 * StateText errorMessage = new StateText.Builder()
 *     .withName("error_dialog")
 *     .withSearchText("An error has occurred")
 *     .withOwnerStateName("ErrorState")
 *     .build();
 * }</pre>
 *
 * <h2>Design Patterns</h2>
 *
 * <h3>Singleton Pattern</h3>
 *
 * <p>NullState and UnknownState use singleton pattern as they represent unique system conditions:
 *
 * <pre>{@code
 * State nullState1 = NullState.getNullState();
 * State nullState2 = NullState.getNullState();
 * assert nullState1 == nullState2; // Same instance
 * }</pre>
 *
 * <h3>Type Checking</h3>
 *
 * <p>Special states can be identified through type checking:
 *
 * <pre>{@code
 * if (state.getSpecialStateType() == SpecialStateType.NULL) {
 *     // Handle null state
 * } else if (state.getSpecialStateType() == SpecialStateType.UNKNOWN) {
 *     // Handle unknown state
 * }
 * }</pre>
 *
 * <h2>Error Handling</h2>
 *
 * <p>Special states facilitate robust error handling:
 *
 * <pre>{@code
 * public State transitionToState(String targetState) {
 *     try {
 *         State newState = performTransition(targetState);
 *         return newState != null ? newState : NullState.getNullState();
 *     } catch (TransitionException e) {
 *         logger.error("Transition failed", e);
 *         return UnknownState.getInstance();
 *     }
 * }
 * }</pre>
 *
 * <h2>State Detection</h2>
 *
 * <p>Special states in state detection flow:
 *
 * <ol>
 *   <li>Attempt to match known state patterns
 *   <li>If no matches found, return UnknownState
 *   <li>If detection fails entirely, return NullState
 *   <li>Log special state occurrences for debugging
 * </ol>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Always check for special states in state-dependent logic
 *   <li>Use NullState instead of null references for safety
 *   <li>Log UnknownState occurrences to identify missing states
 *   <li>Implement recovery strategies for special states
 *   <li>Don't add state objects to special states (they're empty)
 * </ul>
 *
 * <h2>Integration</h2>
 *
 * <p>Special states integrate with the broader state system:
 *
 * <ul>
 *   <li>Can be used in transitions (typically as error paths)
 *   <li>Appear in state detection results
 *   <li>Tracked in state history for debugging
 *   <li>Support standard State interface operations
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state.State
 * @see io.github.jspinak.brobot.state.stateStructure.StateStructureManager
 */
package io.github.jspinak.brobot.model.state.special;
