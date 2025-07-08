package io.github.jspinak.brobot.runner;

import lombok.Data;

import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.services.DialogService;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.runner.ui.theme.OptimizedThemeManager;
import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import io.github.jspinak.brobot.runner.util.ErrorUtils;
import io.github.jspinak.brobot.runner.util.FxThreadUtils;
import jakarta.annotation.PreDestroy;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Application lifecycle manager that handles startup and shutdown operations.
 * This component is responsible for initializing the application and cleaning up resources.
 */
@Component
@Data
public class ApplicationLifecycle {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationLifecycle.class);

    private final ApplicationContext context;
    private final EventBus eventBus;
    private final ApplicationConfig appConfig;
    private final DialogService dialogService;
    private final OptimizedThemeManager themeManager;
    private final IconRegistry iconRegistry;

    // Command line arguments
    private final ApplicationArguments appArgs;

    // Primary stage reference
    private Stage primaryStage;

    // Application state
    private boolean shuttingDown = false;

    /**
     * Creates a new ApplicationLifecycle.
     */
    public ApplicationLifecycle(
            ApplicationContext context,
            EventBus eventBus,
            ApplicationConfig appConfig,
            DialogService dialogService,
            OptimizedThemeManager themeManager,
            IconRegistry iconRegistry,
            ApplicationArguments appArgs) {
        this.context = context;
        this.eventBus = eventBus;
        this.appConfig = appConfig;
        this.dialogService = dialogService;
        this.themeManager = themeManager;
        this.iconRegistry = iconRegistry;
        this.appArgs = appArgs;
    }

    /**
     * Called when the application is fully initialized and ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application ready, initializing additional resources");

        // Load theme preference
        String savedTheme = appConfig.getString("ui.theme", "light");
        if ("dark".equalsIgnoreCase(savedTheme)) {
            themeManager.setTheme(ThemeManager.Theme.DARK);
        }

        // Set up shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::performShutdown));

        // Log application parameters
        logApplicationParameters();

        // Publish startup event
        eventBus.publish(LogEvent.info(this, "Application fully initialized and ready", "System"));
    }

    /**
     * Sets the primary stage reference.
     *
     * @param stage The primary stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;

        // Set stage in dialog service
        dialogService.setPrimaryStage(stage);

        // Set application icons
        List<Image> appIcons = iconRegistry.getAppIcons();
        if (!appIcons.isEmpty()) {
            stage.getIcons().addAll(appIcons);
        }

        // Set up stage close request handler
        stage.setOnCloseRequest(event -> {
            logger.info("Application close requested via stage close request");
            shutdown();
            event.consume(); // Handle closing ourselves
        });
    }

    /**
     * Logs the application startup parameters.
     */
    private void logApplicationParameters() {
        logger.info("Application command line arguments:");
        for (String optionName : appArgs.getOptionNames()) {
            List<String> values = appArgs.getOptionValues(optionName);
            logger.info("  {} = {}", optionName, values);
        }

        String[] sourceArgs = appArgs.getSourceArgs();
        if (sourceArgs.length > 0) {
            logger.info("Source arguments:");
            for (String arg : sourceArgs) {
                logger.info("  {}", arg);
            }
        }
    }

    /**
     * Initiates application shutdown.
     */
    public void shutdown() {
        if (shuttingDown) {
            logger.info("Shutdown already in progress, ignoring additional request");
            return;
        }

        shuttingDown = true;
        logger.info("Application shutdown initiated");

        // Ask for confirmation if needed
        boolean confirmShutdown = appConfig.getBoolean("app.confirmOnExit", true);
        if (confirmShutdown && primaryStage != null && primaryStage.isShowing()) {
            // Perform on JavaFX thread
            if (Platform.isFxApplicationThread()) {
                if (!confirmAndShutdown()) {
                    // User cancelled shutdown
                    shuttingDown = false;
                    return;
                }
            } else {
                // Need to wait for the result from dialog
                try {
                    Boolean result = FxThreadUtils.runAndWait(() -> confirmAndShutdown());
                    if (!result) {
                        // User cancelled shutdown
                        shuttingDown = false;
                        return;
                    }
                } catch (Exception e) {
                    logger.error("Error displaying shutdown confirmation dialog", e);
                    // Continue with shutdown anyway
                }
            }
        }

        // Start actual shutdown
        performShutdown();
    }

    /**
     * Shows a confirmation dialog for shutdown and proceeds if confirmed.
     *
     * @return true if shutdown should proceed, false to cancel
     */
    private boolean confirmAndShutdown() {
        boolean confirmed = dialogService.showConfirmation(
                "Confirm Exit",
                "Are you sure you want to exit the application?");

        if (confirmed) {
            logger.info("User confirmed application shutdown");

            // Hide the primary stage immediately to give feedback
            if (primaryStage != null) {
                primaryStage.hide();
            }

            // Continue with shutdown
            return true;
        } else {
            logger.info("User cancelled application shutdown");
            return false;
        }
    }

    /**
     * Performs the actual shutdown operations.
     */
    private void performShutdown() {
        if (context instanceof ConfigurableApplicationContext) {
            try {
                logger.info("Performing application shutdown");

                // Save theme preference
                appConfig.setString("ui.theme",
                        themeManager.getCurrentTheme() == ThemeManager.Theme.DARK ? "dark" : "light");

                // Publish shutdown event
                eventBus.publish(LogEvent.info(this, "Application shutting down", "System"));

                // Close the primary stage if it's still showing
                if (primaryStage != null && primaryStage.isShowing()) {
                    Platform.runLater(() -> primaryStage.hide());
                }

                // Clean up threads
                FxThreadUtils.shutdown();

                // Close Spring context
                ((ConfigurableApplicationContext) context).close();

                // Exit the application
                Platform.exit();
                System.exit(0);
            } catch (Exception e) {
                logger.error("Error during application shutdown", e);
                // Force exit in case of error
                System.exit(1);
            }
        }
    }

    /**
     * Clean up method called by Spring before bean destruction.
     */
    @PreDestroy
    public void cleanup() {
        logger.info("ApplicationLifecycle PreDestroy called");

        // Save application configuration
        try {
            appConfig.saveConfiguration();
        } catch (Exception e) {
            logger.error("Error saving application configuration during shutdown", e);
        }
    }

    /**
     * Launches the JavaFX application with Spring integration.
     *
     * @param args Command line arguments
     */
    public static void launchApp(String[] args) {
        try {
            // Set up Spring Boot application
            ConfigurableApplicationContext context = SpringApplication.run(BrobotRunnerApplication.class, args);

            // Store context for JavaFX application
            BrobotRunnerApp.setApplicationContext(context);

            // Launch JavaFX application
            Application.launch(BrobotRunnerApp.class, args);
        } catch (Exception e) {
            logger.error("Error launching application", e);
            ErrorUtils.handleException(e, "Failed to launch application", true);
        }
    }
}