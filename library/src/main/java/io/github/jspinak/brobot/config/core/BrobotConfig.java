package io.github.jspinak.brobot.config.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring configuration class for the Brobot framework.
 * <p>
 * BrobotConfig serves as the root configuration for Spring's component scanning
 * and dependency injection within the Brobot library. It enables automatic
 * discovery and registration of Spring-managed components (@Component, @Service,
 * @Repository) throughout the framework.
 * <p>
 * <strong>Key responsibilities:</strong>
 * <ul>
 * <li>Triggers component scanning from this package and all sub-packages</li>
 * <li>Establishes the Spring application context for dependency injection</li>
 * <li>Serves as a central point for framework-wide Spring configuration</li>
 * </ul>
 * <p>
 * <strong>Usage:</strong>
 * <p>
 * This configuration is typically loaded when integrating Brobot into a Spring
 * application:
 * <pre>{@code
 * // In a Spring Boot application
 * @SpringBootApplication
 * @Import(BrobotConfig.class)
 * public class MyAutomationApp {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyAutomationApp.class, args);
 *     }
 * }
 * 
 * // Or in a standard Spring application
 * ApplicationContext context = new AnnotationConfigApplicationContext(BrobotConfig.class);
 * }</pre>
 * <p>
 * The @ComponentScan annotation without parameters scans from the current package
 * (io.github.jspinak.brobot.config) and all sub-packages, ensuring all Brobot
 * components are properly registered in the Spring container.
 * <p>
 * <strong>Design rationale:</strong>
 * <p>
 * By centralizing Spring configuration, this class ensures that all Brobot
 * components are consistently managed and that dependency injection works
 * seamlessly across the framework. This approach supports the model-based
 * architecture by allowing flexible component composition and easy testing
 * through dependency injection.
 *
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ComponentScan
 * @see ExecutionEnvironment
 * @see FrameworkSettings
 */
@Configuration
@ComponentScan(basePackages = "io.github.jspinak.brobot")
@Import({BrobotDefaultsConfiguration.class, io.github.jspinak.brobot.config.logging.SikuliXLoggingConfig.class})
public class BrobotConfig {

    /**
     * Default constructor for Spring instantiation.
     * <p>
     * Spring requires a no-argument constructor for configuration classes.
     * This constructor performs no initialization as all configuration is
     * handled through annotations and component scanning.
     */
    public BrobotConfig() {}
}
