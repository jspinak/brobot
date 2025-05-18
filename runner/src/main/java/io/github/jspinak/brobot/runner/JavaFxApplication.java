package io.github.jspinak.brobot.runner;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxWeaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Main JavaFX application class that integrates with Spring Boot.
 * This class serves as the entry point for the JavaFX UI.
 */
public class JavaFxApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(JavaFxApplication.class);

    private ConfigurableApplicationContext applicationContext;
    /**
     *  The primary stage of the application.
     */
    @Getter
    private Stage primaryStage;

    @Override
    public void init() {
        try {
            // Get command line arguments
            String[] args = getParameters().getRaw().toArray(new String[0]);

            // Create the Spring application context
            this.applicationContext = BrobotRunnerMain.createSpringApplicationContext(args);

            logger.info("Spring context initialized");
        } catch (Exception e) {
            logger.error("Failed to initialize Spring context", e);

            // Show error dialog and exit if Spring context initialization fails
            Platform.runLater(() -> {
                showInitializationErrorDialog(e);
                Platform.exit();
            });
        }
    }

    @Override
    public void start(Stage stage) {
        try {
            // Store the primary stage
            this.primaryStage = stage;

            // Get dependencies from Spring context
            FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
            EventBus eventBus = applicationContext.getBean(EventBus.class);
            ThemeManager themeManager = applicationContext.getBean(ThemeManager.class);
            IconRegistry iconRegistry = applicationContext.getBean(IconRegistry.class);

            // Use FxWeaver to load the main view
            Scene scene = new Scene(fxWeaver.loadView(io.github.jspinak.brobot.runner.ui.BrobotRunnerView.class), 800, 600);

            // Apply theme to the scene
            themeManager.registerScene(scene);

            // Set application icons
            List<Image> appIcons = iconRegistry.getAppIcons();
            if (!appIcons.isEmpty()) {
                stage.getIcons().addAll(appIcons);
            }

            // Configure the primary stage
            stage.setScene(scene);
            stage.setTitle("Brobot Runner");
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            // Show the stage
            stage.show();

            // Publish application started event
            eventBus.publish(LogEvent.info(this, "JavaFX application started", "System"));

            logger.info("JavaFX application started");
        } catch (Exception e) {
            logger.error("Failed to start JavaFX application", e);
            showInitializationErrorDialog(e);
        }
    }

    @Override
    public void stop() {
        logger.info("JavaFX application stopping");

        // Clean up Spring context
        if (applicationContext != null) {
            BrobotRunnerMain.cleanShutdown(applicationContext);
        }

        Platform.exit();
    }

    /**
     * Shows an error dialog for initialization failures.
     * This uses a plain JavaFX dialog since the application context might not be available.
     *
     * @param exception The exception that caused the initialization failure
     */
    private void showInitializationErrorDialog(Exception exception) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Initialization Error");
        alert.setHeaderText("Failed to initialize the application");
        alert.setContentText("An error occurred during application initialization. Please check the logs for details.");

        // Add exception details
        if (exception != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);

            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            javafx.scene.layout.GridPane.setVgrow(textArea, javafx.scene.layout.Priority.ALWAYS);
            javafx.scene.layout.GridPane.setHgrow(textArea, javafx.scene.layout.Priority.ALWAYS);

            javafx.scene.layout.GridPane expContent = new javafx.scene.layout.GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(new javafx.scene.control.Label("Exception stacktrace:"), 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);
        }

        alert.showAndWait();
    }

}