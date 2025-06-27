/**
 * Runtime execution and configuration management for Brobot automation projects.
 * 
 * <p>This package implements the Automation Instructions (ι) component of Brobot's
 * Applied Model (Ω, F, ι), providing infrastructure for defining, parsing, validating,
 * and executing business logic. It enables users to create automation workflows using
 * a declarative Domain-Specific Language (DSL) without direct Java programming.</p>
 * 
 * <h2>Architectural Overview</h2>
 * 
 * <p>The runner package manages the execution of business logic by:</p>
 * <ul>
 *   <li>Parsing JSON-based automation instructions</li>
 *   <li>Validating configurations against schemas</li>
 *   <li>Managing project lifecycles</li>
 *   <li>Orchestrating task execution</li>
 *   <li>Providing UI configuration for desktop runners</li>
 * </ul>
 * 
 * <h2>Subpackages</h2>
 * 
 * <h3>dsl</h3>
 * <p>Domain-Specific Language for automation instructions:</p>
 * <ul>
 *   <li>Business task definitions</li>
 *   <li>Expression and statement evaluation</li>
 *   <li>Task sequence orchestration</li>
 * </ul>
 * 
 * <h3>json</h3>
 * <p>JSON processing and serialization infrastructure:</p>
 * <ul>
 *   <li>Configuration parsing and validation</li>
 *   <li>Custom serializers for Brobot objects</li>
 *   <li>Schema-based validation</li>
 *   <li>Mixin classes for third-party types</li>
 * </ul>
 * 
 * <h3>project</h3>
 * <p>Project management and execution:</p>
 * <ul>
 *   <li>Automation project lifecycle</li>
 *   <li>Configuration management</li>
 *   <li>Runner interface definitions</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * 
 * <p>The runner package embodies key architectural principles:</p>
 * <ul>
 *   <li><b>Declarative Configuration</b> - Business logic defined in JSON</li>
 *   <li><b>Schema Validation</b> - Ensures configuration correctness</li>
 *   <li><b>Separation of Concerns</b> - Clear boundary between instructions and framework</li>
 *   <li><b>Extensibility</b> - Easy to add new task types and validators</li>
 *   <li><b>Type Safety</b> - Strong typing despite JSON configuration</li>
 * </ul>
 * 
 * <h2>Workflow</h2>
 * 
 * <ol>
 *   <li><b>Project Loading</b> - Parse JSON project definition</li>
 *   <li><b>Schema Validation</b> - Verify against project schema</li>
 *   <li><b>Reference Resolution</b> - Resolve state and function references</li>
 *   <li><b>Business Rule Validation</b> - Check semantic correctness</li>
 *   <li><b>Task Execution</b> - Run automation instructions</li>
 * </ol>
 * 
 * <h2>Integration with Framework</h2>
 * 
 * <p>The runner integrates with Brobot's core components:</p>
 * <ul>
 *   <li>Uses the Action Model for executing steps</li>
 *   <li>Leverages State Management for navigation</li>
 *   <li>Employs Path Traversal for complex workflows</li>
 *   <li>Integrates with Visual API for GUI interaction</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.action
 * @see io.github.jspinak.brobot.model
 * @see io.github.jspinak.brobot.navigation
 */
package io.github.jspinak.brobot.runner;