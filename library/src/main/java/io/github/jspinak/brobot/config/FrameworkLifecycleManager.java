package io.github.jspinak.brobot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Manages the Brobot framework initialization lifecycle within Spring.
 * <p>
 * FrameworkLifecycleManager implements Spring's {@link SmartLifecycle} interface to ensure
 * proper framework initialization after the Spring context is fully loaded. It
 * orchestrates the critical startup sequence: loading images, preprocessing them
 * for pattern matching, and initializing the state structure that forms the
 * foundation of model-based GUI automation.
 * <p>
 * <strong>Initialization sequence:</strong>
 * <ol>
 * <li>Spring context loads all beans and dependencies</li>
 * <li>SmartLifecycle triggers this component (phase = MAX_VALUE)</li>
 * <li>Image resources are loaded from the configured path</li>
 * <li>Images are preprocessed for efficient pattern matching</li>
 * <li>State structure is initialized with loaded resources</li>
 * </ol>
 * <p>
 * <strong>Key features:</strong>
 * <ul>
 * <li>Automatic startup via {@code isAutoStartup() = true}</li>
 * <li>Runs last in the startup sequence (phase = Integer.MAX_VALUE)</li>
 * <li>Graceful shutdown support through stop() methods</li>
 * <li>Status tracking via isRunning() for lifecycle management</li>
 * </ul>
 * <p>
 * <strong>Design rationale:</strong>
 * <p>
 * Using SmartLifecycle ensures that all Spring beans are fully initialized before
 * the framework begins loading resources. This prevents dependency issues and
 * ensures services like logging and configuration are available during startup.
 * The late phase execution guarantees that custom beans can be registered before
 * framework initialization begins.
 * <p>
 * In the model-based approach, proper initialization is crucial as it establishes
 * the state model and preloads all visual assets needed for automation. This
 * upfront loading improves runtime performance by avoiding dynamic resource loading
 * during action execution.
 *
 * @see SmartLifecycle
 * @see FrameworkInitializer
 * @see BrobotConfig
 */
@Slf4j
@Component
public class FrameworkLifecycleManager implements SmartLifecycle {

    private final FrameworkInitializer initService;
    private final BrobotProperties properties;
    private boolean running = false;

    /**
     * Constructs the FrameworkLifecycleManager with required initialization service.
     *
     * @param initService Service responsible for loading images and initializing states
     * @param properties Brobot configuration properties
     */
    public FrameworkLifecycleManager(FrameworkInitializer initService, BrobotProperties properties) {
        this.initService = initService;
        this.properties = properties;
    }

    /**
     * Executes the framework initialization sequence after Spring context is ready.
     * <p>
     * This method is called automatically by Spring when all beans are initialized.
     * It performs the following critical startup tasks:
     * <ol>
     * <li>Sets the image bundle path (default: "images")</li>
     * <li>Preprocesses all images for pattern matching optimization</li>
     * <li>Initializes the state structure with loaded resources</li>
     * <li>Marks the component as running</li>
     * </ol>
     * <p>
     * <strong>Side effects:</strong>
     * <ul>
     * <li>Loads all images from the specified path into memory</li>
     * <li>Creates image patterns and color profiles</li>
     * <li>Initializes the global state structure</li>
     * <li>Sets {@code running} flag to true</li>
     * </ul>
     * <p>
     * The image path can be customized by modifying the imagePath variable
     * or through external configuration in future versions.
     */
    @Override
    public void start() {
        // Properties should already be applied by BrobotPropertiesInitializer
        // Use configured image path
        String imagePath = properties.getCore().getImagePath();
        initService.setBundlePathAndPreProcessImages(imagePath);
        initService.initializeStateStructure();
        
        log.info("Brobot library: All beans initialized with image path: {}", imagePath);
        running = true;
    }

    /**
     * Stops the framework lifecycle component.
     * <p>
     * Called during Spring context shutdown. Currently performs minimal cleanup
     * by setting the running flag to false. Future versions may add resource
     * cleanup or state persistence logic here.
     * <p>
     * <strong>Side effects:</strong> Sets {@code running} flag to false
     */
    @Override
    public void stop() {
        running = false;
    }

    /**
     * Checks if the framework startup component is currently running.
     * <p>
     * Used by Spring to determine the component's lifecycle state. Returns true
     * after successful initialization via {@link #start()}, false after {@link #stop()}
     * or before initialization.
     *
     * @return true if the component has been started and not yet stopped
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the startup phase for this lifecycle component.
     * <p>
     * Returns {@link Integer#MAX_VALUE} to ensure this component starts last
     * in the Spring lifecycle. This guarantees all other beans (services,
     * repositories, configurations) are fully initialized before framework
     * initialization begins.
     * <p>
     * Lower phase values start first, higher values start last. Using MAX_VALUE
     * ensures maximum compatibility with custom beans that may need to initialize
     * before the framework.
     *
     * @return Integer.MAX_VALUE to run after all other lifecycle components
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE; // Run after most other beans
    }

    /**
     * Indicates whether this component should start automatically.
     * <p>
     * Returns true to enable automatic startup when the Spring context is
     * initialized. This ensures the framework is ready for use without requiring
     * manual intervention or explicit startup calls.
     * <p>
     * Automatic startup is essential for the framework to function properly in
     * Spring Boot applications where manual lifecycle management is not desired.
     *
     * @return true to enable automatic startup with Spring context
     */
    @Override
    public boolean isAutoStartup() {
        return true; // Automatically start when context is initialized
    }

    /**
     * Stops the component and executes the provided callback.
     * <p>
     * This method supports asynchronous shutdown scenarios where Spring needs
     * to be notified when shutdown is complete. Currently implements synchronous
     * shutdown by calling {@link #stop()} followed immediately by the callback.
     * <p>
     * Future versions may implement asynchronous resource cleanup with proper
     * callback notification after cleanup completion.
     * <p>
     * <strong>Side effects:</strong>
     * <ul>
     * <li>Sets {@code running} flag to false via {@link #stop()}</li>
     * <li>Executes the provided callback to signal completion</li>
     * </ul>
     *
     * @param callback Runnable to execute after stop completes, must not be null
     */
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
}
