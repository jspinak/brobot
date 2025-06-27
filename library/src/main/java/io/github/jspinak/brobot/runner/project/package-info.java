/**
 * Project management and execution components.
 * 
 * <p>This package contains classes that manage automation projects, providing
 * the top-level organizational structure for Brobot automation. It handles
 * project lifecycle, configuration management, and runner interface definitions,
 * implementing the complete Applied Model (Ω, F, ι) for GUI automation.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.project.AutomationProject} - 
 *       Complete automation project container</li>
 *   <li>{@link io.github.jspinak.brobot.runner.project.AutomationProjectManager} - 
 *       Manages active project and configuration access</li>
 *   <li>{@link io.github.jspinak.brobot.runner.project.AutomationConfiguration} - 
 *       Project-level settings and references</li>
 *   <li>{@link io.github.jspinak.brobot.runner.project.RunnerInterface} - 
 *       UI configuration for desktop runners</li>
 *   <li>{@link io.github.jspinak.brobot.runner.project.TaskButton} - 
 *       UI button definitions for task execution</li>
 * </ul>
 * 
 * <h2>Project Structure</h2>
 * 
 * <p>An AutomationProject contains:</p>
 * <ul>
 *   <li><b>Metadata</b> - Name, version, description, author</li>
 *   <li><b>States</b> - All GUI states (State Structure Ω)</li>
 *   <li><b>Transitions</b> - Navigation paths between states</li>
 *   <li><b>Configuration</b> - Settings and function references</li>
 *   <li><b>Runner Interface</b> - UI button definitions</li>
 * </ul>
 * 
 * <h2>Project Lifecycle</h2>
 * 
 * <ol>
 *   <li><b>Loading</b> - Parse project from JSON</li>
 *   <li><b>Validation</b> - Verify structure and references</li>
 *   <li><b>Initialization</b> - Set up runtime environment</li>
 *   <li><b>Execution</b> - Run automation tasks</li>
 *   <li><b>Cleanup</b> - Release resources</li>
 * </ol>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Loading a Project</h3>
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
 * <p>The AutomationProjectManager provides centralized access:</p>
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
 * <p>TaskButton configuration for UI:</p>
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
 * <p>The project package integrates with:</p>
 * <ul>
 *   <li><b>DSL</b> - Executes automation instructions</li>
 *   <li><b>State Management</b> - Manages active states</li>
 *   <li><b>Navigation</b> - Handles state transitions</li>
 *   <li><b>Action Framework</b> - Performs GUI operations</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Validate projects before execution</li>
 *   <li>Use project manager for configuration access</li>
 *   <li>Keep project files versioned</li>
 *   <li>Organize resources relative to project root</li>
 *   <li>Document custom functions clearly</li>
 *   <li>Test runner interface configurations</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model
 * @see io.github.jspinak.brobot.runner.dsl
 * @see io.github.jspinak.brobot.runner.json
 */
package io.github.jspinak.brobot.runner.project;