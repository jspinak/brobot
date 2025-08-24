package io.github.jspinak.brobot.runner;

import io.github.jspinak.brobot.runner.cache.CacheManager;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.spring.SpringFxWeaver;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import javafx.application.Application;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application class for the Brobot Runner.
 * 
 * <p>The Brobot Runner is a JavaFX-based GUI application that provides a visual interface
 * for configuring and executing Brobot automation scripts. It manages automation configurations,
 * state transitions, and provides real-time monitoring of automation execution.</p>
 * 
 * <p>This class configures the Spring application context with:
 * <ul>
 *   <li>JPA repositories for persistence</li>
 *   <li>Component scanning for both runner and library packages</li>
 *   <li>Essential beans for resource management, caching, and session handling</li>
 * </ul>
 * </p>
 * 
 * @author jspinak
 * @see JavaFxApplication
 * @see BrobotRunnerProperties
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "io.github.jspinak.brobot.runner.persistence.repo")
@ComponentScan(basePackages = {
		"io.github.jspinak.brobot"
})
public class BrobotRunnerApplication {

	public static void main(String[] args) {
		// Launch the JavaFX application with Spring context
		Application.launch(JavaFxApplication.class, args);
	}

	@Bean
	public FxWeaver fxWeaver(ConfigurableApplicationContext applicationContext) {
		// Create a SpringFxWeaver to manage JavaFX controller instantiation with Spring
		return new SpringFxWeaver(applicationContext);
	}

	@Bean
	public ResourceManager resourceManager(EventBus eventBus) {
		return new ResourceManager(eventBus);
	}

	@Bean
	public ImageResourceManager imageResourceManager(ResourceManager resourceManager,
													 EventBus eventBus,
													 BrobotRunnerProperties properties) {
		return new ImageResourceManager(resourceManager, eventBus, properties);
	}

	@Bean
	public CacheManager cacheManager(EventBus eventBus, ResourceManager resourceManager) {
		return new CacheManager(eventBus, resourceManager);
	}

	// SessionManager is now created automatically via @Component annotation and dependency injection
}