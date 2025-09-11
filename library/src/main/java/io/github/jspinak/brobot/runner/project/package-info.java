/**
 * Project management and execution components.
 *
 * <p>This package contains classes that manage automation projects, providing the top-level
 * organizational structure for Brobot automation. It handles project lifecycle, configuration
 * management, and runner interface definitions, implementing the complete Applied Model (Ω, F, ι)
 * for GUI automation.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.project.AutomationProject} - Complete automation
 *       project container
 *   <li>{@link io.github.jspinak.brobot.runner.project.AutomationProjectManager} - Manages active
 *       project and configuration access
 *   <li>{@link io.github.jspinak.brobot.runner.project.AutomationConfiguration} - Project-level
 *       settings and references
 *   <li>{@link io.github.jspinak.brobot.runner.project.RunnerInterface} - UI configuration for
 *       desktop runners
 *   <li>{@link io.github.jspinak.brobot.runner.project.TaskButton} - UI button definitions for task
 *       execution
 * </ul>
 *
 * <h2>Project Structure</h2>
 *
 * <p>An AutomationProject contains:
 *
 * <ul>
 *   <li><b>Metadata</b> - Name, version, description, author
 *   <li><b>States</b> - All GUI states (State Structure Ω)
 *   <li><b>Transitions</b> - Navigation paths between states
 *   <li><b>Configuration</b> - Settings and function references
 *   <li><b>Runner Interface</b> - UI button definitions
 * </ul>
 *
 * <h2>Project Lifecycle</h2>
 *
 * <ol>
 *   <li><b>Loading</b> - Parse project from JSON
 *   <li><b>Validation</b> - Verify structure and references
 *   <li><b>Initialization</b> - Set up runtime environment
 *   <li><b>Execution</b> - Run automation tasks
 *   <li><b>Cleanup</b> - Release resources
 * </ol>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Loading a Project</h3>
 *
 * <pre>{@code
 * // Load project from file
 * AutomationProjectManager manager = new AutomationProjectManager();
 * AutomationProject project = manager.loadProject(
 *     "projects/myapp/project.json"
 * );
 *
 * // Access project components
 * List<State> states = project.getStates();
 * List<StateTransitions> transitions = project.getTransitions();
 * AutomationConfiguration config = project.getConfiguration();
 * }</pre>
 *
 * <h3>Project Configuration</h3>
 *
 * <pre>{@code
 * // Access configuration settings
 * AutomationConfiguration config = project.getConfiguration();
 *
 * // Timing settings
 * int pauseAfterAction = config.getPauseAfterAction();
 * int maxWaitTime = config.getMaxWaitTime();
 *
 * // Similarity thresholds
 * double defaultSimilarity = config.getDefaultSimilarity();
 *
 * // Function references
 * String loginFunction = config.getFunctions().get("login");
 * }</pre>
 *
 * <h3>Runner Interface</h3>
 *
 * <pre>{@code
 * // Configure UI buttons
 * RunnerInterface ui = project.getRunnerInterface();
 *
 * for (TaskButton button : ui.getButtons()) {
 *     System.out.println(
 *         "Button: " + button.getLabel() +
 *         " -> " + button.getFunction()
 *     );
 * }
 * }</pre>
 *
 * <h2>Project Manager</h2>
 *
 * <p>The AutomationProjectManager provides centralized access:
 *
 * <pre>{@code
 * @Autowired
 * private AutomationProjectManager projectManager;
 *
 * // Get active project
 * AutomationProject current = projectManager.getActiveProject();
 *
 * // Switch projects
 * projectManager.loadProject("another-project.json");
 *
 * // Access configuration anywhere
 * double similarity = projectManager.getConfiguration()
 *     .getDefaultSimilarity();
 * }</pre>
 *
 * <h2>Task Buttons</h2>
 *
 * <p>TaskButton configuration for UI:
 *
 * <pre>{@code
 * {
 *   "id": "login-button",
 *   "label": "Login",
 *   "tooltip": "Login to the application",
 *   "function": "performLogin",
 *   "parameters": {
 *     "username": "testuser",
 *     "password": "testpass"
 *   },
 *   "enabled": true,
 *   "style": "primary",
 *   "icon": "login-icon.png"
 * }
 * }</pre>
 *
 * <h2>Integration Points</h2>
 *
 * <p>The project package integrates with:
 *
 * <ul>
 *   <li><b>DSL</b> - Executes automation instructions
 *   <li><b>State Management</b> - Manages active states
 *   <li><b>Navigation</b> - Handles state transitions
 *   <li><b>Action Framework</b> - Performs GUI operations
 * </ul>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Validate projects before execution
 *   <li>Use project manager for configuration access
 *   <li>Keep project files versioned
 *   <li>Organize resources relative to project root
 *   <li>Document custom functions clearly
 *   <li>Test runner interface configurations
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.model
 * @see io.github.jspinak.brobot.runner.dsl
 * @see io.github.jspinak.brobot.runner.json
 */
package io.github.jspinak.brobot.runner.project;
