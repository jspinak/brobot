/**
 * JSON configuration classes for the Brobot runner.
 * 
 * <p>This package contains configuration classes that control JSON processing
 * behavior throughout the Brobot framework. It provides centralized settings
 * for serialization, deserialization, and validation operations.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.config.JsonConfiguration} - 
 *       Global JSON processing configuration and settings</li>
 * </ul>
 * 
 * <h2>Configuration Options</h2>
 * 
 * <h3>Serialization Settings</h3>
 * <ul>
 *   <li>Pretty printing for human-readable output</li>
 *   <li>Null value handling (include/exclude)</li>
 *   <li>Date/time formatting options</li>
 *   <li>Property naming strategies</li>
 * </ul>
 * 
 * <h3>Deserialization Settings</h3>
 * <ul>
 *   <li>Unknown property handling</li>
 *   <li>Type coercion rules</li>
 *   <li>Default value policies</li>
 *   <li>Validation on read</li>
 * </ul>
 * 
 * <h3>Module Registration</h3>
 * <ul>
 *   <li>Custom serializer modules</li>
 *   <li>Mixin configurations</li>
 *   <li>Type mapping overrides</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * 
 * <p>Configuration is typically handled through Spring dependency injection:</p>
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
 * <p>The default configuration includes:</p>
 * <ul>
 *   <li>Pretty printing enabled</li>
 *   <li>Unknown properties ignored</li>
 *   <li>Java 8 time module registered</li>
 *   <li>Brobot custom module registered</li>
 *   <li>All mixins configured</li>
 * </ul>
 * 
 * <h2>Customization</h2>
 * 
 * <p>Configuration can be customized through:</p>
 * <ul>
 *   <li>Application properties</li>
 *   <li>Environment variables</li>
 *   <li>Programmatic configuration</li>
 *   <li>Spring profiles</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.json.parsing.BrobotObjectMapper
 * @see io.github.jspinak.brobot.runner.json.module.BrobotJsonModule
 */
package io.github.jspinak.brobot.runner.json.config;