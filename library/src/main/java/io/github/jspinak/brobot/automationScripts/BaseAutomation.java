package io.github.jspinak.brobot.automationScripts;

/**
 * Abstract base class for building GUI automation applications in the Brobot framework.
 * 
 * <p>BaseAutomation provides the foundational structure for creating automation scripts 
 * that leverage Brobot's model-based approach. It implements the AutomationScript interface 
 * and provides common functionality needed by all automation applications, including 
 * lifecycle management and state handling integration.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Lifecycle Management</b>: Built-in running state tracking and stop mechanism</li>
 *   <li><b>State Handler Integration</b>: Direct access to state-based automation patterns</li>
 *   <li><b>Thread Safety</b>: Volatile running flag for safe multi-threaded operation</li>
 *   <li><b>Extensibility</b>: Abstract design allows custom automation implementations</li>
 * </ul>
 * </p>
 * 
 * <p>Subclasses should implement:
 * <ul>
 *   <li>The {@code runScript()} method to define automation logic</li>
 *   <li>Any initialization logic needed for their specific use case</li>
 *   <li>Custom error handling and recovery strategies</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, BaseAutomation serves as the bridge between the 
 * framework's state management capabilities and user-defined automation logic. It 
 * enables developers to focus on business logic while the framework handles the 
 * complexities of GUI interaction and state navigation.</p>
 * 
 * @since 1.0
 * @see AutomationScript
 * @see StateHandler
 * @see ContinuousAutomation
 */
public abstract class BaseAutomation implements AutomationScript {

    protected volatile boolean running = false;
    protected final StateHandler stateHandler;

    protected BaseAutomation(StateHandler stateHandler) {
        this.stateHandler = stateHandler;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void stop() {
        running = false;
    }
}
