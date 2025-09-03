/**
 * Framework configuration and initialization components.
 * 
 * <p>
 * This package provides the foundational configuration system for Brobot,
 * managing framework initialization, runtime environment detection, and
 * execution
 * mode control. It implements the separation of concerns between
 * domain-specific
 * knowledge (State Structure Ω) and strategic framework capabilities (Framework
 * F)
 * as defined in the formal model.
 * </p>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>Framework Configuration</h3>
 * <ul>
 * <li>{@link io.github.jspinak.brobot.config.BrobotConfig} -
 * Root Spring configuration enabling component scanning</li>
 * <li>{@link io.github.jspinak.brobot.config.core.FrameworkSettings} -
 * Global settings controlling framework behavior</li>
 * <li>{@link io.github.jspinak.brobot.config.environment.ExecutionEnvironment}
 * -
 * Runtime environment detection and configuration</li>
 * <li>{@link io.github.jspinak.brobot.config.environment.ExecutionMode} -
 * Controls execution mode (mock vs real)</li>
 * </ul>
 * 
 * <h3>Initialization</h3>
 * <ul>
 * <li>{@link io.github.jspinak.brobot.config.core.FrameworkInitializer} -
 * Core initialization service for image preprocessing and state setup</li>
 * <li>{@link io.github.jspinak.brobot.startup.orchestration.FrameworkLifecycleManager}
 * -
 * Spring lifecycle management for ordered initialization</li>
 * </ul>
 * 
 * <h3>Support Configuration</h3>
 * <ul>
 * <li>{@link io.github.jspinak.brobot.config.SpringConfiguration} -
 * Additional Spring bean configuration</li>
 * <li>{@link io.github.jspinak.brobot.config.logging.LoggingConfiguration} -
 * Logging subsystem configuration</li>
 * </ul>
 * 
 * <h2>Initialization Process</h2>
 * 
 * <p>
 * The framework follows a carefully orchestrated initialization sequence:
 * </p>
 * <ol>
 * <li><b>Spring Context Creation</b> - Component scanning and dependency
 * injection</li>
 * <li><b>Environment Detection</b> - Determine execution capabilities</li>
 * <li><b>Framework Initialization</b> - Load and preprocess images</li>
 * <li><b>State Structure Setup</b> - Convert names to IDs, build indices</li>
 * <li><b>Profile Generation</b> - Create color and k-means profiles</li>
 * <li><b>Transition Configuration</b> - Initialize navigation paths</li>
 * </ol>
 * 
 * <h2>Configuration Hierarchy</h2>
 * 
 * <pre>
 * Visual API (Ω, F)
 *     ├── State Structure (Ω) - Domain Knowledge
 *     │   ├── States (S)
 *     │   ├── Elements (E)  
 *     │   └── Transitions (T)
 *     │
 *     └── Framework (F) - Strategic Knowledge
 *         ├── Action Model (a)
 *         ├── State Management (M)
 *         ├── Transition Model (τ)
 *         └── Path Traversal (§)
 * </pre>
 * 
 * <h2>Environment Modes</h2>
 * 
 * <h3>Production Mode</h3>
 * <ul>
 * <li>Full GUI interaction capabilities</li>
 * <li>Real screen capture and control</li>
 * <li>SikuliX integration enabled</li>
 * </ul>
 * 
 * <h3>Mock Mode</h3>
 * <ul>
 * <li>Simulated GUI interactions</li>
 * <li>Predefined action results</li>
 * <li>No actual screen interaction</li>
 * </ul>
 * 
 * <h3>Headless Mode</h3>
 * <ul>
 * <li>No display available</li>
 * <li>Limited to non-visual operations</li>
 * <li>Automatically detected in CI/CD</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Basic Framework Setup</h3>
 * 
 * <pre>{@code
 * // Spring automatically initializes the framework
 * ApplicationContext context = new AnnotationConfigApplicationContext(
 *         BrobotConfig.class);
 * 
 * // Framework is now initialized with:
 * // - All states loaded
 * // - Images preprocessed
 * // - Profiles generated
 * // - Transitions configured
 * }</pre>
 * 
 * <h3>Custom Configuration</h3>
 * 
 * <pre>{@code
 * // Configure framework settings before Spring initialization
 * FrameworkSettings.setTimeToWaitAfterMove(500);
 * FrameworkSettings.setInitProfilesForDynamicImages(true);
 * 
 * // Set execution environment
 * ExecutionEnvironment env = ExecutionEnvironment.builder()
 *         .runMode(RunMode.MOCK)
 *         .displayAvailable(false)
 *         .build();
 * }</pre>
 * 
 * <h3>Manual Image Path Configuration</h3>
 * 
 * <pre>{@code
 * @Autowired
 * private FrameworkInitializer initializer;
 * 
 * // Add additional image paths
 * initializer.add("/path/to/more/images");
 * 
 * // Or reinitialize with new path
 * initializer.setBundlePathAndPreProcessImages("/new/image/path");
 * }</pre>
 * 
 * <h2>Key Settings</h2>
 * 
 * <h3>Performance Settings</h3>
 * <ul>
 * <li>{@code timeToWaitAfterMove} - Delay after mouse movements</li>
 * <li>{@code timeToWaitAfterAction} - Delay between actions</li>
 * <li>{@code maxWaitForVisibleObjects} - Timeout for finding elements</li>
 * </ul>
 * 
 * <h3>Profile Generation</h3>
 * <ul>
 * <li>{@code initProfilesForStaticImages} - Generate profiles for static
 * images</li>
 * <li>{@code initProfilesForDynamicImages} - Generate profiles for dynamic
 * content</li>
 * <li>{@code kmeansGranularity} - Cluster count for k-means</li>
 * </ul>
 * 
 * <h3>Data Collection</h3>
 * <ul>
 * <li>{@code saveHistory} - Enable action history tracking</li>
 * <li>{@code screenshot} - Control screenshot capture</li>
 * <li>{@code dataCollection} - Enable ML dataset generation</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * 
 * <p>
 * The configuration package embodies key architectural principles:
 * </p>
 * <ul>
 * <li><b>Separation of Concerns</b> - Clear distinction between framework and
 * domain</li>
 * <li><b>Dependency Injection</b> - Spring-managed component lifecycle</li>
 * <li><b>Environment Adaptation</b> - Automatic adjustment to runtime
 * capabilities</li>
 * <li><b>Performance Optimization</b> - Preprocessing during
 * initialization</li>
 * <li><b>Testability</b> - Mock mode for unit testing</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 * <li>Configure settings before Spring context initialization</li>
 * <li>Use mock mode for unit tests to avoid GUI dependencies</li>
 * <li>Let environment auto-detection handle CI/CD scenarios</li>
 * <li>Preprocess all images during initialization for performance</li>
 * <li>Use Spring profiles for environment-specific configuration</li>
 * </ol>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model
 * @see io.github.jspinak.brobot.statemanagement
 */
package io.github.jspinak.brobot.config;