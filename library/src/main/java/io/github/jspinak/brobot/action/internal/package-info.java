/**
 * Internal infrastructure and support classes for action execution.
 *
 * <p>This package contains the internal implementation details of the action framework, including
 * action lifecycle management, execution infrastructure, utility classes, and support components.
 * These classes are not intended for direct use by API consumers but provide the foundation upon
 * which the public action API is built.
 *
 * <h2>Package Organization</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.execution execution}</b> - Core action
 *       execution, lifecycle management, and registries
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.factory factory}</b> - Factory classes
 *       for creating action-related objects
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.service service}</b> - Service layer for
 *       action coordination and dispatch
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.utility utility}</b> - Utility classes
 *       and helper components
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.app app}</b> - Application-specific
 *       internal components
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.capture capture}</b> - Internal support
 *       for region definition and capture operations
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find find}</b> - Internal utilities for
 *       pattern matching and element location
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.mouse mouse}</b> - Low-level mouse
 *       operation wrappers and utilities
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.text text}</b> - Keyboard and text
 *       operation support classes
 * </ul>
 *
 * <h2>Core Components by Subsystem</h2>
 *
 * <h3>Execution Subsystem</h3>
 *
 * <ul>
 *   <li><b>ActionExecution</b> - Orchestrates the execution of actions
 *   <li><b>ActionLifecycle</b> - Manages action lifecycle phases
 *   <li><b>ActionLifecycleManagement</b> - Coordinates lifecycle operations
 *   <li><b>BasicAction</b> - Registry of basic action implementations
 *   <li><b>CompositeAction</b> - Registry of composite action implementations
 * </ul>
 *
 * <h3>Factory Subsystem</h3>
 *
 * <ul>
 *   <li><b>ActionResultFactory</b> - Creates properly initialized ActionResult instances
 * </ul>
 *
 * <h3>Service Subsystem</h3>
 *
 * <ul>
 *   <li><b>ActionService</b> - Central service for action coordination
 * </ul>
 *
 * <h3>Utility Subsystem</h3>
 *
 * <ul>
 *   <li><b>Success</b> - Evaluates action success criteria
 *   <li><b>CopyActionOptions</b> - Clones and modifies action configurations
 *   <li><b>DragLocation</b> - Calculates drag operation coordinates
 *   <li><b>ActionText</b> - Text-related utility operations
 * </ul>
 *
 * <h2>Internal Architecture</h2>
 *
 * <h3>Action Execution Flow</h3>
 *
 * <ol>
 *   <li>Action request received by ActionService
 *   <li>ActionLifecycle initializes execution context
 *   <li>ActionExecution coordinates the operation
 *   <li>Specific action implementation performs work
 *   <li>Results collected and processed
 *   <li>Lifecycle cleanup and result return
 * </ol>
 *
 * <h3>Registry Pattern</h3>
 *
 * <p>Actions are registered in two main registries:
 *
 * <ul>
 *   <li><b>BasicAction</b> - Maps action types to basic implementations
 *   <li><b>CompositeAction</b> - Maps action types to composite implementations
 * </ul>
 *
 * <h3>Wrapper Pattern</h3>
 *
 * <p>Low-level operations are wrapped for consistent interfaces:
 *
 * <ul>
 *   <li>Mouse operations (click, move, drag)
 *   <li>Keyboard operations (type, key press)
 *   <li>Screen operations (capture, find)
 * </ul>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><b>Separation of Concerns</b> - Clear boundaries between layers
 *   <li><b>Single Responsibility</b> - Each class has one clear purpose
 *   <li><b>Dependency Injection</b> - Spring-managed dependencies
 *   <li><b>Extensibility</b> - Easy to add new action types
 *   <li><b>Testability</b> - Mockable interfaces and clear contracts
 * </ul>
 *
 * <h2>Extension Points</h2>
 *
 * <p>To add new actions to the framework:
 *
 * <ol>
 *   <li>Implement {@link io.github.jspinak.brobot.action.ActionInterface}
 *   <li>Register in appropriate registry (BasicAction or CompositeAction)
 *   <li>Add any required internal support classes
 *   <li>Update ActionOptions with new action type if needed
 * </ol>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>Internal classes handle thread safety through:
 *
 * <ul>
 *   <li>Immutable configuration objects
 *   <li>Thread-local execution contexts
 *   <li>Synchronized access to shared resources
 *   <li>Atomic operations where appropriate
 * </ul>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 *   <li>Action lookup is O(1) through HashMap registries
 *   <li>Screen captures are cached when possible
 *   <li>Pattern matching results are reused within execution
 *   <li>Resource cleanup is automatic through lifecycle management
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.Action
 * @see io.github.jspinak.brobot.action.ActionInterface
 * @see io.github.jspinak.brobot.action.ActionOptions
 */
package io.github.jspinak.brobot.action.internal;
