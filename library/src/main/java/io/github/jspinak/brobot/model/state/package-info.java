/**
 * State-based model for GUI application representation.
 * 
 * <p>This package implements the state component of Brobot's formal model
 * <b>Ω = (E, S, T)</b>, where S represents the set of application states.
 * States serve as nodes in the automation graph, containing visual elements
 * that define each unique application screen or condition.</p>
 * 
 * <h2>State Model Architecture</h2>
 * 
 * <h3>Core Components</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.state.State} - Application state nodes</li>
 *   <li>{@link io.github.jspinak.brobot.model.state.StateObject} - Abstract base for state elements</li>
 *   <li>{@link io.github.jspinak.brobot.model.state.StateImage} - Image-based state objects</li>
 *   <li>{@link io.github.jspinak.brobot.model.state.StateRegion} - Region-based state objects</li>
 *   <li>{@link io.github.jspinak.brobot.model.state.StateLocation} - Fixed location objects</li>
 *   <li>{@link io.github.jspinak.brobot.model.state.StateString} - Text-based state objects</li>
 * </ul>
 * 
 * <h3>Metadata and Management</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.state.StateObjectMetadata} - Object properties and relationships</li>
 *   <li>{@link io.github.jspinak.brobot.model.state.StateEnum} - State identifiers and references</li>
 *   <li>{@link io.github.jspinak.brobot.model.state.StateStore} - Global state repository</li>
 * </ul>
 * 
 * <h2>State Concept</h2>
 * 
 * <p>A State represents a unique configuration of the GUI where specific visual
 * elements are present and can be interacted with. States are identified by:</p>
 * <ul>
 *   <li>Unique visual elements (images, regions, text)</li>
 *   <li>Absence of elements from other states</li>
 *   <li>Specific color patterns or layouts</li>
 * </ul>
 * 
 * <h2>State Objects</h2>
 * 
 * <p>StateObjects are the visual elements that belong to a state:</p>
 * 
 * <h3>StateImage</h3>
 * <pre>{@code
 * StateImage loginButton = new StateImage.Builder()
 *     .withName("login_button")
 *     .withImage("login_btn.png")
 *     .withOwnerStateName("LoginScreen")
 *     .withProbabilityExists(100)  // Always present in this state
 *     .build();
 * }</pre>
 * 
 * <h3>StateRegion</h3>
 * <pre>{@code
 * StateRegion inputArea = new StateRegion.Builder()
 *     .withName("username_field")
 *     .withSearchRegion(new Region(100, 200, 300, 50))
 *     .withOwnerStateName("LoginScreen")
 *     .build();
 * }</pre>
 * 
 * <h3>StateLocation</h3>
 * <pre>{@code
 * StateLocation submitPos = new StateLocation.Builder()
 *     .withName("submit_position")
 *     .withLocation(new Location(400, 350))
 *     .withOwnerStateName("FormScreen")
 *     .build();
 * }</pre>
 * 
 * <h2>State Definition</h2>
 * 
 * <pre>{@code
 * State loginScreen = new State.Builder()
 *     .withName("LoginScreen")
 *     .addStateImage(usernameField)
 *     .addStateImage(passwordField)
 *     .addStateImage(loginButton)
 *     .addStateRegion(formArea)
 *     .setBlocking(false)  // Can transition to other states
 *     .build();
 * 
 * // Register with StateStore
 * StateStore.add(loginScreen);
 * }</pre>
 * 
 * <h2>State Metadata</h2>
 * 
 * <p>StateObjectMetadata provides runtime information:</p>
 * <ul>
 *   <li>Ownership relationships</li>
 *   <li>Visibility probabilities</li>
 *   <li>Timing constraints</li>
 *   <li>Structural properties</li>
 * </ul>
 * 
 * <pre>{@code
 * StateObjectMetadata metadata = stateImage.getStateObjectMetadata();
 * String ownerState = metadata.getOwnerStateName();
 * int probability = metadata.getProbabilityExists();
 * boolean isFixed = metadata.isFixed();
 * }</pre>
 * 
 * <h2>State Store</h2>
 * 
 * <p>The StateStore provides centralized state management:</p>
 * 
 * <pre>{@code
 * // Add states
 * StateStore.add(loginScreen);
 * StateStore.add(homeScreen);
 * 
 * // Retrieve states
 * State state = StateStore.get("LoginScreen");
 * Set<State> allStates = StateStore.getAllStates();
 * 
 * // Find states containing an object
 * Set<State> statesWithButton = StateStore.findStatesWithObject("submit_button");
 * }</pre>
 * 
 * <h2>Special States</h2>
 * 
 * <p>The special subpackage provides predefined states:</p>
 * <ul>
 *   <li><b>NullState</b> - Represents absence of state</li>
 *   <li><b>UnknownState</b> - Unidentified application state</li>
 *   <li><b>StateText</b> - Text-focused state objects</li>
 * </ul>
 * 
 * <h2>State Properties</h2>
 * 
 * <h3>Blocking States</h3>
 * <p>States can be marked as blocking, indicating they prevent
 * transitions until specific conditions are met (e.g., modal dialogs).</p>
 * 
 * <h3>Hidden States</h3>
 * <p>States can be hidden, meaning they exist but are not directly
 * visible or accessible through normal navigation.</p>
 * 
 * <h3>Probability Model</h3>
 * <p>State objects have existence probabilities (0-100) indicating
 * how likely they are to be present when the state is active.</p>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li>Name states after their functional purpose, not visual appearance</li>
 *   <li>Include enough unique elements to distinguish states reliably</li>
 *   <li>Use probability values to handle dynamic UI elements</li>
 *   <li>Organize related states into logical groups via StateEnum</li>
 *   <li>Document state transitions and dependencies</li>
 * </ol>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>State objects are designed to be immutable after construction.
 * The StateStore uses concurrent collections for thread-safe access.
 * Builders are not thread-safe and should be used by single threads.</p>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model.transition
 * @see io.github.jspinak.brobot.state.stateStructure
 * @see io.github.jspinak.brobot.action
 */
package io.github.jspinak.brobot.model.state;