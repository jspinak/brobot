/**
 * Factory classes for creating action-related objects and results.
 *
 * <p>This package contains factory implementations that handle the creation of various action
 * framework objects. These factories ensure consistent initialization, proper default values, and
 * encapsulate complex creation logic.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.factory.ActionResultFactory}</b> -
 *       Primary factory for creating ActionResult instances with proper initialization, default
 *       values, and consistent structure across all action types
 * </ul>
 *
 * <h2>Factory Responsibilities</h2>
 *
 * <h3>ActionResult Creation</h3>
 *
 * <ul>
 *   <li><b>Initialization</b> - Sets up empty collections and default values
 *   <li><b>Consistency</b> - Ensures all results have required fields populated
 *   <li><b>Validation</b> - Verifies result integrity before return
 *   <li><b>Defaults</b> - Applies framework-wide default configurations
 * </ul>
 *
 * <h2>Design Patterns</h2>
 *
 * <h3>Factory Method Pattern</h3>
 *
 * <p>Provides flexible object creation with:
 *
 * <ul>
 *   <li>Multiple creation methods for different scenarios
 *   <li>Encapsulation of complex initialization logic
 *   <li>Consistent object state guarantees
 *   <li>Easy extension for new object types
 * </ul>
 *
 * <h3>Builder Integration</h3>
 *
 * <p>Factories may use internal builders for:
 *
 * <ul>
 *   <li>Complex object construction
 *   <li>Optional parameter handling
 *   <li>Fluent configuration interfaces
 *   <li>Validation during construction
 * </ul>
 *
 * <h2>Usage Guidelines</h2>
 *
 * <ul>
 *   <li>Always use factories instead of direct constructors for framework objects
 *   <li>Factories handle cross-cutting concerns like logging and metrics
 *   <li>Creation methods are thread-safe and can be used concurrently
 *   <li>Factories may cache or pool objects for performance when appropriate
 * </ul>
 *
 * <h2>Extension Points</h2>
 *
 * <p>To add new factory capabilities:
 *
 * <ol>
 *   <li>Create new factory class following naming conventions
 *   <li>Implement consistent creation methods
 *   <li>Include proper validation and error handling
 *   <li>Document creation parameters and behaviors
 * </ol>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 *   <li>Factories may implement object pooling for frequently created objects
 *   <li>Lazy initialization used where appropriate
 *   <li>Minimal overhead for simple object creation
 *   <li>Batch creation methods available for bulk operations
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.ActionResult
 * @see io.github.jspinak.brobot.action.internal.execution.ActionExecution
 */
package io.github.jspinak.brobot.action.internal.factory;
