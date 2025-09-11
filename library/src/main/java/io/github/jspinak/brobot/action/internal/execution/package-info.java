/**
 * Core action execution infrastructure and lifecycle management.
 *
 * <p>This package contains the fundamental components responsible for executing actions within the
 * Brobot framework. It manages the complete lifecycle of action execution, from initialization
 * through completion, and maintains registries of available actions.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.execution.ActionExecution}</b> - Central
 *       orchestrator that coordinates the execution of actions, managing the flow from request to
 *       result with proper error handling and resource management
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.execution.ActionLifecycle}</b> - Manages
 *       the distinct phases of action execution (initialization, execution, cleanup), ensuring
 *       proper setup and teardown of resources
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement}</b>
 *       - Coordinates lifecycle operations across multiple actions and manages execution contexts
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.execution.BasicActionRegistry}</b> -
 *       Registry that maps action types to their basic (atomic) implementations, providing O(1)
 *       lookup for action dispatch
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.execution.CompositeActionRegistry}</b> -
 *       Registry for composite actions that combine multiple basic actions into higher-level
 *       operations
 * </ul>
 *
 * <h2>Execution Flow</h2>
 *
 * <ol>
 *   <li><b>Request Reception</b> - Action request received with parameters and options
 *   <li><b>Registry Lookup</b> - Appropriate action implementation located via registries
 *   <li><b>Lifecycle Initialization</b> - Pre-execution setup and validation
 *   <li><b>Action Execution</b> - Core action logic performed with monitoring
 *   <li><b>Result Collection</b> - Outcomes gathered and processed
 *   <li><b>Lifecycle Cleanup</b> - Resources released and state restored
 *   <li><b>Result Return</b> - Formatted results returned to caller
 * </ol>
 *
 * <h2>Registry Pattern</h2>
 *
 * <p>The package implements a registry pattern for action management:
 *
 * <ul>
 *   <li><b>Type Safety</b> - Compile-time verification of action types
 *   <li><b>Extensibility</b> - New actions easily added without modifying core
 *   <li><b>Performance</b> - HashMap-based lookups for efficient dispatch
 *   <li><b>Modularity</b> - Clear separation between registry and implementation
 * </ul>
 *
 * <h2>Lifecycle Management</h2>
 *
 * <p>The lifecycle components ensure:
 *
 * <ul>
 *   <li>Proper resource acquisition and release
 *   <li>Consistent error handling across all actions
 *   <li>Transaction-like behavior with rollback capabilities
 *   <li>Performance monitoring and metrics collection
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>Execution components are designed for concurrent use:
 *
 * <ul>
 *   <li>Stateless execution handlers
 *   <li>Thread-local execution contexts
 *   <li>Immutable configuration objects
 *   <li>Safe registry access patterns
 * </ul>
 *
 * <h2>Error Handling</h2>
 *
 * <p>Comprehensive error management includes:
 *
 * <ul>
 *   <li>Graceful degradation for non-critical failures
 *   <li>Detailed error context in results
 *   <li>Automatic retry mechanisms where appropriate
 *   <li>Cleanup guarantees even on failure
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.ActionInterface
 * @see io.github.jspinak.brobot.action.internal.service.ActionService
 */
package io.github.jspinak.brobot.action.internal.execution;
