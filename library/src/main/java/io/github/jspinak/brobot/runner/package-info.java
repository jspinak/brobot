/**
 * Runtime execution and configuration management for Brobot automation projects.
 *
 * <p>This package implements the Automation Instructions (ι) component of Brobot's Applied Model
 * (Ω, F, ι), providing infrastructure for defining, parsing, validating, and executing business
 * logic. It enables users to create automation workflows using a declarative Domain-Specific
 * Language (DSL) without direct Java programming.
 *
 * <h2>Architectural Overview</h2>
 *
 * <p>The runner package manages the execution of business logic by:
 *
 * <ul>
 *   <li>Parsing JSON-based automation instructions
 *   <li>Validating configurations against schemas
 *   <li>Managing project lifecycles
 *   <li>Orchestrating task execution
 *   <li>Providing UI configuration for desktop runners
 * </ul>
 *
 * <h2>Subpackages</h2>
 *
 * <h3>dsl</h3>
 *
 * <p>Domain-Specific Language for automation instructions:
 *
 * <ul>
 *   <li>Business task definitions
 *   <li>Expression and statement evaluation
 *   <li>Task sequence orchestration
 * </ul>
 *
 * <h3>json</h3>
 *
 * <p>JSON processing and serialization infrastructure:
 *
 * <ul>
 *   <li>Configuration parsing and validation
 *   <li>Custom serializers for Brobot objects
 *   <li>Schema-based validation
 *   <li>Mixin classes for third-party types
 * </ul>
 *
 * <h3>project</h3>
 *
 * <p>Project management and execution:
 *
 * <ul>
 *   <li>Automation project lifecycle
 *   <li>Configuration management
 *   <li>Runner interface definitions
 * </ul>
 *
 * <h2>Design Principles</h2>
 *
 * <p>The runner package embodies key architectural principles:
 *
 * <ul>
 *   <li><b>Declarative Configuration</b> - Business logic defined in JSON
 *   <li><b>Schema Validation</b> - Ensures configuration correctness
 *   <li><b>Separation of Concerns</b> - Clear boundary between instructions and framework
 *   <li><b>Extensibility</b> - Easy to add new task types and validators
 *   <li><b>Type Safety</b> - Strong typing despite JSON configuration
 * </ul>
 *
 * <h2>Workflow</h2>
 *
 * <ol>
 *   <li><b>Project Loading</b> - Parse JSON project definition
 *   <li><b>Schema Validation</b> - Verify against project schema
 *   <li><b>Reference Resolution</b> - Resolve state and function references
 *   <li><b>Business Rule Validation</b> - Check semantic correctness
 *   <li><b>Task Execution</b> - Run automation instructions
 * </ol>
 *
 * <h2>Integration with Framework</h2>
 *
 * <p>The runner integrates with Brobot's core components:
 *
 * <ul>
 *   <li>Uses the Action Model for executing steps
 *   <li>Leverages State Management for navigation
 *   <li>Employs Path Traversal for complex workflows
 *   <li>Integrates with Visual API for GUI interaction
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.action
 * @see io.github.jspinak.brobot.model
 * @see io.github.jspinak.brobot.navigation
 */
package io.github.jspinak.brobot.runner;
