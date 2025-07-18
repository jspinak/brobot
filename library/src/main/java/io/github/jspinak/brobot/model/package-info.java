/**
 * Core data structures and models for the Brobot automation framework.
 * 
 * <p>This package implements the formal model defined in the theoretical framework
 * where GUI automation is represented as a state transition system. The model provides
 * concrete implementations of the abstract concepts, enabling practical automation
 * while maintaining theoretical rigor.</p>
 * 
 * <h2>Formal Model Implementation</h2>
 * 
 * <p>The model implements the theoretical tuple <b>Ω = (E, S, T)</b> where:</p>
 * <ul>
 *   <li><b>E</b> = Elements (StateObject, StateImage, StateRegion, etc.)</li>
 *   <li><b>S</b> = States (State class representing application states)</li>
 *   <li><b>T</b> = Transitions (StateTransition connecting states)</li>
 * </ul>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>State Model</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.state.State} - Application states as graph nodes</li>
 *   <li>{@link io.github.jspinak.brobot.model.state.StateObject} - Visual elements within states</li>
 *   <li>{@link io.github.jspinak.brobot.model.state.StateImage} - Image-based state objects</li>
 *   <li>{@link io.github.jspinak.brobot.model.state.StateRegion} - Region-based state objects</li>
 * </ul>
 * 
 * <h3>Action Results</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.match.Match} - Pattern matching results</li>
 *   <li>{@link io.github.jspinak.brobot.model.action.ActionRecord} - Historical action data</li>
 *   <li>{@link io.github.jspinak.brobot.model.action.ActionHistory} - Action tracking over time</li>
 * </ul>
 * 
 * <h3>Basic Elements</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.element.Region} - Screen areas and boundaries</li>
 *   <li>{@link io.github.jspinak.brobot.model.element.Location} - Precise screen coordinates</li>
 *   <li>{@link io.github.jspinak.brobot.model.element.Image} - Image data and metadata</li>
 *   <li>{@link io.github.jspinak.brobot.model.element.Pattern} - Search patterns</li>
 * </ul>
 * 
 * <h3>Transitions</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.transition.StateTransition} - State connections</li>
 *   <li>{@link io.github.jspinak.brobot.model.transition.TransitionFunction} - Transition logic</li>
 * </ul>
 * 
 * <h3>Analysis</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis} - Scene classification</li>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.color.ColorCluster} - Color profiles</li>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.histogram.Histogram} - Image statistics</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * 
 * <ol>
 *   <li><b>Immutability</b> - Core data structures are designed to be immutable where practical</li>
 *   <li><b>Builder Pattern</b> - Complex objects use builders for flexible construction</li>
 *   <li><b>Type Safety</b> - Strong typing ensures compile-time correctness</li>
 *   <li><b>Serialization</b> - Models support JSON serialization for persistence</li>
 * </ol>
 * 
 * <h2>Model Relationships</h2>
 * 
 * <pre>
 * State (1) ---- (*) StateObject
 *   |                    |
 *   |                    +-- StateImage
 *   |                    +-- StateRegion
 *   |
 *   +-------------- (*) StateTransition
 *                         |
 *                         +-- TransitionFunction
 * 
 * Pattern (1) ---- (1) Image
 *   |
 *   +-- used by --> Find Action
 *                     |
 *                     v
 *                   Match
 * </pre>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>State Definition</h3>
 * <pre>{@code
 * State loginScreen = new State.Builder()
 *     .withName("LoginScreen")
 *     .addStateImage(new StateImage.Builder()
 *         .withName("username_field")
 *         .withImage("username.png")
 *         .build())
 *     .addStateImage(new StateImage.Builder()
 *         .withName("login_button")
 *         .withImage("login_btn.png")
 *         .build())
 *     .build();
 * }</pre>
 * 
 * <h3>Pattern Matching</h3>
 * <pre>{@code
 * Pattern searchPattern = new Pattern.Builder()
 *     .withImage("button.png")
 *     .withSimilarity(0.95)
 *     .build();
 * 
 * // Action execution returns Match objects
 * Match match = findAction.execute(searchPattern);
 * if (match.isFound()) {
 *     Location clickPoint = match.getTarget();
 *     // ... perform click
 * }
 * }</pre>
 * 
 * <h3>State Transitions</h3>
 * <pre>{@code
 * StateTransition loginTransition = new StateTransition.Builder()
 *     .setFromState(loginScreen)
 *     .setToState(homeScreen)
 *     .setTransitionImage(loginButton)
 *     .build();
 * }</pre>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>Model objects are designed to be thread-safe through immutability.
 * Mutable components like State use defensive copying and synchronized
 * access where necessary. Builders are not thread-safe and should be
 * confined to single threads.</p>
 * 
 * <h2>Persistence</h2>
 * 
 * <p>All model objects support JSON serialization via Jackson annotations.
 * This enables:</p>
 * <ul>
 *   <li>State model persistence and versioning</li>
 *   <li>Test data recording and playback</li>
 *   <li>Remote model sharing and distribution</li>
 *   <li>Configuration management</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.action
 * @see io.github.jspinak.brobot.state
 */
package io.github.jspinak.brobot.model;