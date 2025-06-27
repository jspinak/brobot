/**
 * State structure construction utilities for rapid development and testing.
 * 
 * <p>This package provides builder classes that simplify the creation of
 * State Structures (Ω) in Brobot. These utilities offer fluent APIs for
 * constructing states, defining transitions, and assembling complete state
 * graphs, making it easier to build and test automation scenarios without
 * extensive boilerplate code.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.builder.StateStructureBuilder} - 
 *       Low-level builder for detailed state and transition construction</li>
 *   <li>{@link io.github.jspinak.brobot.tools.builder.FluentStateBuilder} - 
 *       High-level fluent API wrapping StateStructureBuilder for simplified usage</li>
 * </ul>
 * 
 * <h2>Building State Structures</h2>
 * 
 * <p>The builders support the theoretical model where State Structure Ω = (E, S, T):</p>
 * <ul>
 *   <li><b>E</b> - Elements (images) added to states</li>
 *   <li><b>S</b> - States created and configured</li>
 *   <li><b>T</b> - Transitions defined between states</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Using FluentStateBuilder</h3>
 * <pre>{@code
 * FluentStateBuilder builder = new FluentStateBuilder();
 * 
 * // Create states with transitions
 * builder.newState("loginPage", "login-button.png", "homePage")
 *        .newState("homePage", "menu-icon.png", "menuPage")
 *        .newState("menuPage", "logout.png");
 * 
 * // Set initial states
 * builder.setStartStates("loginPage");
 * 
 * // Build and initialize
 * builder.build();
 * }</pre>
 * 
 * <h3>Using StateStructureBuilder</h3>
 * <pre>{@code
 * StateStructureBuilder builder = new StateStructureBuilder();
 * 
 * // Create a state with multiple elements
 * builder.init("dashboardState")
 *        .addImage("header.png")
 *        .addTransitionImage("profile-button.png", "profileState")
 *        .addTransitionImage("settings-button.png", "settingsState")
 *        .build();
 * }</pre>
 * 
 * <h2>Builder Features</h2>
 * 
 * <h3>State Construction</h3>
 * <ul>
 *   <li>Create states with unique names</li>
 *   <li>Add multiple images to states</li>
 *   <li>Define ownership percentages for images</li>
 *   <li>Configure state properties</li>
 * </ul>
 * 
 * <h3>Transition Definition</h3>
 * <ul>
 *   <li>Simple click transitions</li>
 *   <li>Image-based transition triggers</li>
 *   <li>Automatic transition registration</li>
 * </ul>
 * 
 * <h3>Framework Integration</h3>
 * <ul>
 *   <li>Automatic repository registration</li>
 *   <li>Initial state configuration</li>
 *   <li>Framework initialization support</li>
 * </ul>
 * 
 * <h2>Use Cases</h2>
 * 
 * <h3>Testing</h3>
 * <pre>{@code
 * // Quick test setup
 * FluentStateBuilder testBuilder = new FluentStateBuilder();
 * testBuilder.newState("testState", "test-element.png")
 *            .setStartStates("testState")
 *            .build();
 * }</pre>
 * 
 * <h3>Prototyping</h3>
 * <pre>{@code
 * // Rapid prototype development
 * StateStructureBuilder prototype = new StateStructureBuilder();
 * prototype.init("mainScreen")
 *          .addTransitionImage("button1.png", "screen1")
 *          .addTransitionImage("button2.png", "screen2")
 *          .build();
 * }</pre>
 * 
 * <h3>Simple Projects</h3>
 * <pre>{@code
 * // Bootstrap small automation projects
 * FluentStateBuilder project = new FluentStateBuilder();
 * project.newState("start", "start-button.png", "process")
 *        .newState("process", "continue.png", "end")
 *        .newState("end", "finish.png")
 *        .setStartStates("start")
 *        .build();
 * }</pre>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Use FluentStateBuilder for simple, linear workflows</li>
 *   <li>Use StateStructureBuilder for complex state configurations</li>
 *   <li>Always set initial states before building</li>
 *   <li>Ensure image files exist in the configured path</li>
 *   <li>Use descriptive state names following naming conventions</li>
 * </ul>
 * 
 * <h2>Limitations</h2>
 * 
 * <p>These builders are designed for:</p>
 * <ul>
 *   <li>Simple click-based transitions only</li>
 *   <li>Basic state structures without complex actions</li>
 *   <li>Testing and prototyping scenarios</li>
 * </ul>
 * 
 * <p>For production use with complex transitions and actions, use the
 * full state and transition definition APIs.</p>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state.State
 * @see io.github.jspinak.brobot.navigation.transition.StateTransitions
 * @see io.github.jspinak.brobot.statemanagement
 */
package io.github.jspinak.brobot.tools.builder;