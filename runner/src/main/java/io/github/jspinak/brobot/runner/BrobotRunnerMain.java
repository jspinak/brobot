package io.github.jspinak.brobot.runner;

import javafx.application.Application;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main entry point for the Brobot Runner application. This class serves as a bridge between the
 * Spring Boot application and the JavaFX UI framework.
 */
public class BrobotRunnerMain {

    /**
     * Application entry point.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Launch the JavaFX application with Spring context
        Application.launch(JavaFxApplication.class, args);
    }

    /**
     * Creates and configures the Spring application context. This is used by JavaFxApplication
     * during initialization.
     *
     * @param args Command line arguments
     * @return Configured Spring application context
     */
    public static ConfigurableApplicationContext createSpringApplicationContext(String[] args) {
        try {
            // Configure Spring Boot application
            SpringApplication app = new SpringApplication(BrobotRunnerApplication.class);

            // Disable headless mode for JavaFX compatibility
            app.setHeadless(false);

            // Run the application
            return app.run(args);
        } catch (Exception e) {
            System.err.println("Failed to create Spring application context: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Shutdown hook to ensure clean application exit.
     *
     * @param context The Spring application context to close
     */
    public static void cleanShutdown(ConfigurableApplicationContext context) {
        if (context != null) {
            context.close();
        }
    }
}
