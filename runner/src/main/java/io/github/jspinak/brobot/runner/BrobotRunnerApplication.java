package io.github.jspinak.brobot.runner;

import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.spring.SpringFxWeaver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import javafx.application.Application;

@SpringBootApplication
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
}