/**
 * Service layer for action coordination and high-level operations.
 *
 * <p>This package contains service classes that provide high-level coordination of action
 * operations. These services act as the primary interface between the public API and the internal
 * execution infrastructure.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.service.ActionService}</b> - Central
 *       service that coordinates action dispatch, manages execution flow, and provides the main
 *       entry point for action processing
 * </ul>
 *
 * <h2>Service Responsibilities</h2>
 *
 * <h3>Action Coordination</h3>
 *
 * <ul>
 *   <li><b>Request Validation</b> - Ensures action requests are properly formed
 *   <li><b>Action Dispatch</b> - Routes requests to appropriate implementations
 *   <li><b>Flow Control</b> - Manages complex action sequences and dependencies
 *   <li><b>Result Aggregation</b> - Combines results from multiple operations
 * </ul>
 *
 * <h3>Cross-Cutting Concerns</h3>
 *
 * <ul>
 *   <li><b>Transaction Management</b> - Coordinates multi-step operations
 *   <li><b>Error Recovery</b> - Implements retry and fallback strategies
 *   <li><b>Performance Monitoring</b> - Tracks execution metrics
 *   <li><b>Audit Logging</b> - Records action execution for debugging
 * </ul>
 *
 * <h2>Service Architecture</h2>
 *
 * <p>Services in this package follow these principles:
 *
 * <ul>
 *   <li><b>Stateless Design</b> - Services maintain no request-specific state
 *   <li><b>Dependency Injection</b> - All dependencies injected via Spring
 *   <li><b>Interface Segregation</b> - Focused interfaces for specific operations
 *   <li><b>High Cohesion</b> - Related operations grouped logically
 * </ul>
 *
 * <h2>Integration Points</h2>
 *
 * <p>Services integrate with:
 *
 * <ul>
 *   <li><b>Execution Layer</b> - Delegates to execution components
 *   <li><b>Registry Layer</b> - Looks up action implementations
 *   <li><b>Factory Layer</b> - Uses factories for object creation
 *   <li><b>Public API</b> - Provides implementation for public interfaces
 * </ul>
 *
 * <h2>Error Handling Strategy</h2>
 *
 * <p>Services implement comprehensive error handling:
 *
 * <ul>
 *   <li>Graceful degradation for recoverable errors
 *   <li>Clear error messages with actionable information
 *   <li>Proper cleanup on failure paths
 *   <li>Detailed logging for troubleshooting
 * </ul>
 *
 * <h2>Performance Optimization</h2>
 *
 * <ul>
 *   <li>Request batching for improved throughput
 *   <li>Caching of frequently used resources
 *   <li>Asynchronous processing where beneficial
 *   <li>Connection pooling for external resources
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>All services are designed for concurrent access:
 *
 * <ul>
 *   <li>Thread-safe singleton instances
 *   <li>No mutable shared state
 *   <li>Proper synchronization where needed
 *   <li>Concurrent data structures for performance
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.Action
 * @see io.github.jspinak.brobot.action.internal.execution.ActionExecution
 */
package io.github.jspinak.brobot.action.internal.service;
