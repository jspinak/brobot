package io.github.jspinak.brobot.runner;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.ui.BrobotRunnerView;
import io.github.jspinak.brobot.runner.ui.WindowManager;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Main JavaFX application class that integrates with Spring Boot.
 * 
 * <p>This class serves as the entry point for the JavaFX UI and handles the lifecycle
 * of the JavaFX application. It creates and manages the Spring application context,
 * initializes the primary stage, and sets up the main application view.</p>
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Bootstrapping the Spring context before JavaFX starts</li>
 *   <li>Creating and configuring the primary stage with the main view</li>
 *   <li>Setting up application icons and theme</li>
 *   <li>Handling application lifecycle events (init, start, stop)</li>
 *   <li>Managing thread safety between JavaFX and Spring</li>
 * </ul>
 * </p>
 * 
 * @see BrobotRunnerApplication
 * @see BrobotRunnerView
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
            // Log environment information
            logger.info("JavaFX init() - Display: {}, Headless: {}, AWT Headless: {}", 
                System.getenv("DISPLAY"), 
                System.getProperty("javafx.headless"),
                System.getProperty("java.awt.headless"));
            logger.info("JavaFX platform: {}, Monocle: {}", 
                System.getProperty("javafx.platform"),
                System.getProperty("glass.platform"));
            
            // Get command line arguments
            String[] args = getParameters().getRaw().toArray(new String[0]);

            // Create the Spring application context - make sure this works
            this.applicationContext = BrobotRunnerMain.createSpringApplicationContext(args);

            if (this.applicationContext == null) {
                throw new IllegalStateException("Failed to create Spring application context");
            }

            logger.info("Spring context initialized successfully");
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

            // Get dependencies directly from the Spring context
            ThemeManager themeManager = applicationContext.getBean(ThemeManager.class);
            IconRegistry iconRegistry = applicationContext.getBean(IconRegistry.class);
            EventBus eventBus = applicationContext.getBean(EventBus.class);
            WindowManager windowManager = applicationContext.getBean(WindowManager.class);

            // Register primary stage with WindowManager
            windowManager.setPrimaryStage(stage);

            // Get the BrobotRunnerView directly from the context
            BrobotRunnerView view = applicationContext.getBean(BrobotRunnerView.class);

            // Create the scene with the view - larger default size
            Scene scene = new Scene(view, 1400, 900);

            // Apply theme
            themeManager.registerScene(scene);

            // Set application icons
            List<Image> appIcons = iconRegistry.getAppIcons();
            if (!appIcons.isEmpty()) {
                stage.getIcons().addAll(appIcons);
            }

            // Configure and show the stage
            stage.setScene(scene);
            stage.setTitle("Brobot Runner");
            stage.setMinWidth(1200);
            stage.setMinHeight(700);
            
            // Enable maximization and set window to be resizable
            stage.setMaximized(false);
            stage.setResizable(true);
            
            // Ensure window is visible on screen
            stage.setX(100);
            stage.setY(100);
            
            // Log window properties before showing
            logger.info("About to show stage - Width: {}, Height: {}, Title: {}", 
                stage.getWidth(), stage.getHeight(), stage.getTitle());
            logger.info("Stage showing: {}, iconified: {}", stage.isShowing(), stage.isIconified());
            
            stage.show();
            
            // Log window properties after showing
            logger.info("Stage shown - Showing: {}, X: {}, Y: {}, Width: {}, Height: {}", 
                stage.isShowing(), stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            logger.info("Stage focused: {}, iconified: {}", stage.isFocused(), stage.isIconified());

            // Force stage to front
            stage.toFront();
            stage.requestFocus();
            
            logger.info("Stage brought to front and focus requested");

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