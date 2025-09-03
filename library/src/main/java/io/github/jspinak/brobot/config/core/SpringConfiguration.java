package io.github.jspinak.brobot.config.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class for the Brobot library's dependency injection setup.
 * <p>
 * This configuration class serves as the central Spring configuration for the
 * Brobot automation library. It enables component scanning to automatically
 * detect and register Spring-managed beans throughout the library.
 * <p>
 * Key responsibilities:
 * <ul>
 * <li>Enables Spring's component scanning for automatic bean discovery</li>
 * <li>Provides a configuration entry point for the Brobot library</li>
 * <li>Allows the library to be integrated into Spring applications</li>
 * <li>Ensures all @Component, @Service, @Repository classes are registered</li>
 * </ul>
 * <p>
 * Component scanning behavior:
 * <ul>
 * <li>Base package: io.github.jspinak.brobot (inferred from class location)</li>
 * <li>Recursively scans all sub-packages</li>
 * <li>Registers all Spring-annotated classes as beans</li>
 * <li>Enables dependency injection throughout the library</li>
 * </ul>
 * <p>
 * Integration usage:
 * <pre>
 * // In a Spring Boot application
 * @SpringBootApplication
 * @Import(SpringConfiguration.class)
 * public class MyApplication {
 *     // Brobot components are now available for injection
 * }
 * 
 * // Or with AnnotationConfigApplicationContext
 * ApplicationContext context = new AnnotationConfigApplicationContext(
 *     SpringConfiguration.class);
 * </pre>
 * <p>
 * Design considerations:
 * <ul>
 * <li>Minimal configuration to reduce complexity</li>
 * <li>No explicit bean definitions - relies on component scanning</li>
 * <li>Can be extended by applications needing custom configurations</li>
 * <li>Compatible with Spring Boot auto-configuration</li>
 * </ul>
 * <p>
 * Note: Ensure this class is on the classpath when using Brobot as a library
 * in Spring applications to enable proper bean registration.
 *
 * @see Configuration
 * @see ComponentScan
 */
@Configuration
@ComponentScan
public class SpringConfiguration {

    /**
     * Default constructor for Spring configuration class.
     * <p>
     * Empty constructor is explicitly defined to ensure Spring can
     * instantiate this configuration class. No initialization logic
     * is needed as all configuration is handled through annotations.
     */
    public SpringConfiguration() {}
}
