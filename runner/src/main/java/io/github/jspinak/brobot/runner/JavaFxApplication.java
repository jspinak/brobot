package io.github.jspinak.brobot.runner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.ui.BrobotRunnerView;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.runner.ui.services.ScreenshotService;
import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import io.github.jspinak.brobot.runner.ui.window.WindowManager;

import atlantafx.base.theme.PrimerLight;
import lombok.Getter;

/**
 * Main JavaFX application class that integrates with Spring Boot.
 *
 * <p>This class serves as the entry point for the JavaFX UI and handles the lifecycle of the JavaFX
 * application. It creates and manages the Spring application context, initializes the primary
 * stage, and sets up the main application view.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Bootstrapping the Spring context before JavaFX starts
 *   <li>Creating and configuring the primary stage with the main view
 *   <li>Setting up application icons and theme
 *   <li>Handling application lifecycle events (init, start, stop)
 *   <li>Managing thread safety between JavaFX and Spring
 * </ul>
 *
 * @see BrobotRunnerApplication
 * @see BrobotRunnerView
 */
public class JavaFxApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(JavaFxApplication.class);

    private ConfigurableApplicationContext applicationContext;

    /** The primary stage of the application. */
    @Getter private Stage primaryStage;

    @Override
    public void init() {
        try {
            // Log environment information
            logger.info(
                    "JavaFX init() - Display: {}, Headless: {}, AWT Headless: {}",
                    System.getenv("DISPLAY"),
                    System.getProperty("javafx.headless"),
                    System.getProperty("java.awt.headless"));
            logger.info(
                    "JavaFX platform: {}, Monocle: {}",
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
            e.printStackTrace(); // Also print to console for debugging

            // Show error dialog and exit if Spring context initialization fails
            Platform.runLater(
                    () -> {
                        showInitializationErrorDialog(e);
                        Platform.exit();
                    });
        }
    }

    @Override
    public void start(Stage stage) {
        logger.info("JavaFX start() method called");
        try {
            // Check if Spring context was initialized successfully
            if (applicationContext == null) {
                logger.error("Cannot start application: Spring context is null");
                Platform.exit();
                return;
            }
            logger.info("Spring context is valid");

            // Store the primary stage
            this.primaryStage = stage;
            logger.info("Primary stage stored");

            // Get dependencies directly from the Spring context
            logger.info("Getting beans from Spring context...");
            ThemeManager themeManager = applicationContext.getBean(ThemeManager.class);
            logger.info("Got ThemeManager");
            IconRegistry iconRegistry = applicationContext.getBean(IconRegistry.class);
            logger.info("Got IconRegistry");
            EventBus eventBus = applicationContext.getBean(EventBus.class);
            logger.info("Got EventBus");
            WindowManager windowManager = applicationContext.getBean(WindowManager.class);
            logger.info("Got WindowManager");

            // Initialize ScreenshotService with primary stage if available
            try {
                ScreenshotService screenshotService =
                        applicationContext.getBean(ScreenshotService.class);
                screenshotService.setPrimaryStage(stage);
                logger.info("ScreenshotService initialized with primary stage");
            } catch (Exception e) {
                logger.warn("ScreenshotService not available: {}", e.getMessage());
            }

            // Register primary stage with WindowManager
            windowManager.registerStage("main", stage);

            // Get the BrobotRunnerView directly from the context
            logger.info("About to get BrobotRunnerView from context...");
            BrobotRunnerView view = null;
            try {
                view = applicationContext.getBean(BrobotRunnerView.class);
                logger.info("Successfully got BrobotRunnerView: " + view);
            } catch (Exception e) {
                logger.error("Failed to get BrobotRunnerView", e);
                throw e;
            }

            // Apply AtlantFX theme globally before creating scene
            logger.info("Applying AtlantaFX theme...");
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            logger.info("Theme applied");

            // Create the scene with the view - larger default size
            Scene scene = new Scene(view, 1400, 900);

            // Set light theme in ThemeManager
            themeManager.setTheme(ThemeManager.Theme.LIGHT);

            // Apply theme (will override with dark if needed)
            themeManager.registerScene(scene);

            // Add Modena override CSS (only for dark mode)
            // String modenaOverride =
            // getClass().getResource("/css/modena-dark-override.css").toExternalForm();
            // scene.getStylesheets().add(modenaOverride);

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

            // Display on monitor 1 (the left monitor at x=0, y=0, width=1080, height=1920)
            // Get all screens and find the one that starts at x=0
            javafx.stage.Screen targetScreen = null;
            for (javafx.stage.Screen screen : javafx.stage.Screen.getScreens()) {
                javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
                logger.info(
                        "Screen found - X: {}, Y: {}, Width: {}, Height: {}",
                        bounds.getMinX(),
                        bounds.getMinY(),
                        bounds.getWidth(),
                        bounds.getHeight());
                if (bounds.getMinX() == 0) {
                    targetScreen = screen;
                    break;
                }
            }

            if (targetScreen == null) {
                logger.warn("Could not find monitor 1, using primary screen");
                targetScreen = javafx.stage.Screen.getPrimary();
            }

            javafx.geometry.Rectangle2D targetBounds = targetScreen.getVisualBounds();
            logger.info(
                    "Target screen bounds - X: {}, Y: {}, Width: {}, Height: {}",
                    targetBounds.getMinX(),
                    targetBounds.getMinY(),
                    targetBounds.getWidth(),
                    targetBounds.getHeight());

            // Adjust window size to fit monitor 1 (1080x1920)
            // Make window smaller to fit comfortably
            double windowWidth = Math.min(1000, targetBounds.getWidth() - 80);
            double windowHeight = Math.min(800, targetBounds.getHeight() - 100);

            // Center the window on monitor 1
            double centerX = targetBounds.getMinX() + (targetBounds.getWidth() - windowWidth) / 2;
            double centerY = targetBounds.getMinY() + (targetBounds.getHeight() - windowHeight) / 2;

            stage.setWidth(windowWidth);
            stage.setHeight(windowHeight);
            stage.setX(centerX);
            stage.setY(centerY);

            logger.info(
                    "Setting window size to {}x{} at position X: {}, Y: {}",
                    windowWidth,
                    windowHeight,
                    centerX,
                    centerY);

            // Add screenshot capability with F12 key
            scene.setOnKeyPressed(
                    event -> {
                        if (event.getCode() == javafx.scene.input.KeyCode.F12) {
                            io.github.jspinak.brobot.runner.ui.utils.ScreenshotUtil
                                    .captureAndAnalyze(stage, "Manual Screenshot");
                        }
                    });

            // Log window properties before showing
            logger.info(
                    "About to show stage - Width: {}, Height: {}, Title: {}",
                    stage.getWidth(),
                    stage.getHeight(),
                    stage.getTitle());
            logger.info("Stage showing: {}, iconified: {}", stage.isShowing(), stage.isIconified());

            logger.info("Calling stage.show()...");
            stage.show();
            logger.info("stage.show() completed");

            // Log window properties after showing
            logger.info(
                    "Stage shown - Showing: {}, X: {}, Y: {}, Width: {}, Height: {}",
                    stage.isShowing(),
                    stage.getX(),
                    stage.getY(),
                    stage.getWidth(),
                    stage.getHeight());
            logger.info("Stage focused: {}, iconified: {}", stage.isFocused(), stage.isIconified());

            // Force stage to front
            stage.toFront();
            stage.requestFocus();

            logger.info("Stage brought to front and focus requested");

            // Add a delay and check again
            Platform.runLater(
                    () -> {
                        try {
                            Thread.sleep(1000);
                            logger.info(
                                    "After 1 second - Stage showing: {}, X: {}, Y: {}, Width: {},"
                                            + " Height: {}",
                                    stage.isShowing(),
                                    stage.getX(),
                                    stage.getY(),
                                    stage.getWidth(),
                                    stage.getHeight());
                            logger.info(
                                    "After 1 second - Stage focused: {}, iconified: {},"
                                            + " alwaysOnTop: {}",
                                    stage.isFocused(),
                                    stage.isIconified(),
                                    stage.isAlwaysOnTop());

                            // Try to force window to be visible
                            stage.setAlwaysOnTop(true);
                            stage.setAlwaysOnTop(false);
                            stage.toFront();
                            stage.requestFocus();

                            logger.info("Forced window visibility attempt completed");
                        } catch (Exception e) {
                            logger.error("Error in delayed window check", e);
                        }
                    });

            // Publish application started event
            eventBus.publish(LogEvent.info(this, "JavaFX application started", "System"));

            logger.info("JavaFX application started");

            // Apply style fixes after scene is shown (if needed)
            // javafx.application.Platform.runLater(() -> {
            //     logger.info("Applying dark mode style fixes");
            //
            // io.github.jspinak.brobot.runner.ui.utils.StyleDiagnostic.removeWhiteBorders(scene);
            // });

            // Take automatic screenshot after 3 seconds to verify styling
            // DISABLED - causing performance issues with tab switching
            // javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            //     new javafx.animation.KeyFrame(
            //         javafx.util.Duration.seconds(3),
            //         e -> {
            //             logger.info("Taking automatic screenshot for style verification");
            //
            // io.github.jspinak.brobot.runner.ui.utils.ScreenshotUtil.captureAndAnalyze(stage,
            // "Auto-Screenshot-Light-Mode");
            //         }
            //     )
            // );
            // timeline.play();

            // Test theme switching after 5 seconds
            // DISABLED - causing performance issues
            // javafx.animation.Timeline themeTestTimeline = new javafx.animation.Timeline(
            //     new javafx.animation.KeyFrame(
            //         javafx.util.Duration.seconds(5),
            //         e -> {
            //             logger.info("Testing theme switching");
            //
            // io.github.jspinak.brobot.runner.ui.utils.ThemeTestUtil.testThemeSwitching(stage,
            // themeManager);
            //         }
            //     )
            // );
            // themeTestTimeline.play();
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
     * Shows an error dialog for initialization failures. This uses a plain JavaFX dialog since the
     * application context might not be available.
     *
     * @param exception The exception that caused the initialization failure
     */
    private void showInitializationErrorDialog(Exception exception) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Initialization Error");
        alert.setHeaderText("Failed to initialize the application");
        alert.setContentText(
                "An error occurred during application initialization. Please check the logs for"
                        + " details.");

        // Add exception details
        if (exception != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);

            javafx.scene.control.TextArea textArea =
                    new javafx.scene.control.TextArea(sw.toString());
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
