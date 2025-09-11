/**
 * Fluent API for building Brobot DSL automation sequences programmatically.
 *
 * <p>This package provides a fluent, builder-style API for creating automation sequences that are
 * fully compatible with Brobot's JSON-based Domain Specific Language (DSL). The fluent API serves
 * as a programmatic alternative to defining automation workflows in JSON files, while producing the
 * exact same underlying data structures.
 *
 * <h2>Key Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.fluent.Brobot} - Entry point for the fluent API
 *   <li>{@link io.github.jspinak.brobot.fluent.ActionSequenceBuilder} - Main builder for creating
 *       sequences
 *   <li>{@link io.github.jspinak.brobot.fluent.FluentApiExample} - Examples of common patterns
 * </ul>
 *
 * <h2>Design Philosophy</h2>
 *
 * <p>The fluent API follows these principles:
 *
 * <ul>
 *   <li><b>DSL Compatibility</b>: Every fluent API call creates the same data structures as the
 *       JSON DSL, ensuring full interoperability
 *   <li><b>Type Safety</b>: Unlike JSON, the fluent API provides compile-time type checking and IDE
 *       autocompletion
 *   <li><b>Consistency</b>: State objects (StateImage, StateString, etc.) are treated uniformly,
 *       just as in the rest of Brobot
 *   <li><b>Chainability</b>: Methods return the builder instance for easy chaining
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Create state objects (these might come from a State definition)
 * StateImage loginButton = new StateImage();
 * loginButton.setName("loginButton");
 *
 * StateString username = new StateString.InNullState().withString("user123");
 *
 * // Build the automation sequence
 * InstructionSet loginSequence = Brobot.buildSequence()
 *     .withName("login")
 *     .find(loginButton)
 *     .thenClick()
 *     .thenType(username)
 *     .build();
 *
 * // The resulting InstructionSet can be:
 * // 1. Executed directly by the DSL runner
 * // 2. Serialized to JSON for storage/transmission
 * // 3. Combined with other InstructionSets
 * }</pre>
 *
 * <h2>Integration with DSL</h2>
 *
 * <p>The fluent API creates the following DSL structures:
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.InstructionSet} - Top-level container
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.BusinessTask} - Automation functions
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.model.ActionStep} - Individual actions
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.model.TaskSequence} - Ordered action lists
 * </ul>
 *
 * <h2>String Conversion</h2>
 *
 * <p>Note that raw strings must be converted to StateString objects before use. This conversion is
 * typically handled by the framework, not by the fluent API, maintaining consistency with how other
 * conversions (e.g., Pattern to StateImage) are handled throughout Brobot.
 *
 * @since 1.0
 */
package io.github.jspinak.brobot.fluent;
