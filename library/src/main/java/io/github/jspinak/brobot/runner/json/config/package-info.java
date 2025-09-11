/**
 * JSON configuration classes for the Brobot runner.
 *
 * <p>This package contains configuration classes that control JSON processing behavior throughout
 * the Brobot framework. It provides centralized settings for serialization, deserialization, and
 * validation operations.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.config.JsonConfiguration} - Global JSON
 *       processing configuration and settings
 * </ul>
 *
 * <h2>Configuration Options</h2>
 *
 * <h3>Serialization Settings</h3>
 *
 * <ul>
 *   <li>Pretty printing for human-readable output
 *   <li>Null value handling (include/exclude)
 *   <li>Date/time formatting options
 *   <li>Property naming strategies
 * </ul>
 *
 * <h3>Deserialization Settings</h3>
 *
 * <ul>
 *   <li>Unknown property handling
 *   <li>Type coercion rules
 *   <li>Default value policies
 *   <li>Validation on read
 * </ul>
 *
 * <h3>Module Registration</h3>
 *
 * <ul>
 *   <li>Custom serializer modules
 *   <li>Mixin configurations
 *   <li>Type mapping overrides
 * </ul>
 *
 * <h2>Usage</h2>
 *
 * <p>Configuration is typically handled through Spring dependency injection:
 *
 * <pre>{@code
 * @Autowired
 * private JsonConfiguration jsonConfig;
 *
 * // Access configured settings
 * ObjectMapper mapper = jsonConfig.getObjectMapper();
 * boolean validateOnParse = jsonConfig.isValidationEnabled();
 * }</pre>
 *
 * <h2>Default Configuration</h2>
 *
 * <p>The default configuration includes:
 *
 * <ul>
 *   <li>Pretty printing enabled
 *   <li>Unknown properties ignored
 *   <li>Java 8 time module registered
 *   <li>Brobot custom module registered
 *   <li>All mixins configured
 * </ul>
 *
 * <h2>Customization</h2>
 *
 * <p>Configuration can be customized through:
 *
 * <ul>
 *   <li>Application properties
 *   <li>Environment variables
 *   <li>Programmatic configuration
 *   <li>Spring profiles
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.json.parsing.BrobotObjectMapper
 * @see io.github.jspinak.brobot.runner.json.module.BrobotJsonModule
 */
package io.github.jspinak.brobot.runner.json.config;
