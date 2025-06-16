package io.github.jspinak.brobot.runner;

import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.runner.cache.CacheManager;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.session.SessionManager;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.spring.SpringFxWeaver;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import javafx.application.Application;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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

	@Bean
	public SessionManager sessionManager(EventBus eventBus,
										 BrobotRunnerProperties properties,
										 ResourceManager resourceManager,
										 JsonParser jsonParser,
										 StateTransitionsRepository stateTransitionsRepository) {
		return new SessionManager(eventBus, properties, resourceManager, jsonParser, stateTransitionsRepository);
	}
}