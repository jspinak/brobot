package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.datatypes.project.Button;
import io.github.jspinak.brobot.datatypes.project.Project;
import io.github.jspinak.brobot.runner.automation.AutomationExecutor;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.services.ProjectManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AutomationPanel extends VBox {
    private static final Logger logger = LoggerFactory.getLogger(AutomationPanel.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ApplicationContext context;
    private final ProjectManager projectManager;
    private final BrobotRunnerProperties properties;
    private final AutomationExecutor automationExecutor;

    private final TextArea logArea;
    private final FlowPane buttonPane;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean isRunning = false;

    /**
     * No-arg constructor for JavaFX initialization
     */
    public AutomationPanel() {
        this.context = null;
        this.projectManager = null;
        this.properties = null;
        this.automationExecutor = null;
        this.logArea = new TextArea();
        this.buttonPane = new FlowPane();
        setupBasicUI();

        // Show message when not properly initialized with Spring
        log("AutomationPanel initialized without Spring context. Limited functionality available.");
    }

    /**
     * Constructor with Spring dependencies
     */
    @Autowired
    public AutomationPanel(ApplicationContext context, ProjectManager projectManager,
                           BrobotRunnerProperties properties, AutomationExecutor automationExecutor) {
        this.context = context;
        this.projectManager = projectManager;
        this.properties = properties;
        this.automationExecutor = automationExecutor;
        this.logArea = new TextArea();
        this.buttonPane = new FlowPane();

        setupBasicUI();
        setupLogArea();

        // Register for automation events
        if (automationExecutor != null) {
            automationExecutor.setLogCallback(this::log);
        }
    }

    private void setupBasicUI() {
        setPadding(new Insets(20));
        setSpacing(10);

        Label titleLabel = new Label("Automation Control");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        javafx.scene.control.Button refreshButton = new javafx.scene.control.Button("Refresh Automation Buttons");
        refreshButton.setId("refreshAutomationButtons");
        refreshButton.setOnAction(e -> refreshAutomationButtons());

        javafx.scene.control.Button stopAllButton = new javafx.scene.control.Button("Stop All Automation");
        stopAllButton.setId("stopAllAutomation");
        stopAllButton.setOnAction(e -> stopAllAutomation());

        HBox controlBar = new HBox(10, refreshButton, stopAllButton);

        // Setup button pane
        buttonPane.setPadding(new Insets(10));
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);
        buttonPane.setBorder(new Border(new BorderStroke(
                Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT
        )));

        ScrollPane buttonScrollPane = new ScrollPane(buttonPane);
        buttonScrollPane.setFitToWidth(true);
        buttonScrollPane.setPrefHeight(200);

        getChildren().addAll(
                titleLabel,
                new Separator(),
                controlBar,
                new Label("Available Automation Functions:"),
                buttonScrollPane,
                new Label("Automation Log:")
        );
    }

    private void setupLogArea() {
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(300);

        getChildren().add(logArea);

        log("Automation panel initialized. Load a configuration to see available automation functions.");
    }

    /**
     * Refreshes the automation buttons based on the currently loaded project
     */
    public void refreshAutomationButtons() {
        buttonPane.getChildren().clear();

        if (projectManager == null || projectManager.getActiveProject() == null) {
            log("No project loaded. Please load a configuration first.");
            return;
        }

        Project project = projectManager.getActiveProject();
        if (project.getAutomation() == null || project.getAutomation().getButtons() == null) {
            log("No automation buttons defined in the current project.");
            return;
        }

        List<Button> buttons = project.getAutomation().getButtons();
        if (buttons.isEmpty()) {
            log("No automation buttons defined in the current project.");
            return;
        }

        log("Found " + buttons.size() + " automation functions.");

        // Group buttons by category if available
        Map<String, List<Button>> buttonsByCategory = new HashMap<>();

        for (Button buttonDef : buttons) {
            String category = buttonDef.getCategory() != null ? buttonDef.getCategory() : "General";
            buttonsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(buttonDef);
        }

        // Create button sections by category
        for (Map.Entry<String, List<Button>> entry : buttonsByCategory.entrySet()) {
            VBox categoryBox = new VBox(5);
            categoryBox.setPadding(new Insets(5));
            categoryBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");

            Label categoryLabel = new Label(entry.getKey());
            categoryLabel.setStyle("-fx-font-weight: bold;");
            categoryBox.getChildren().add(categoryLabel);

            for (Button buttonDef : entry.getValue()) {
                javafx.scene.control.Button button = createAutomationButton(buttonDef);
                categoryBox.getChildren().add(button);
            }

            buttonPane.getChildren().add(categoryBox);
        }
    }

    /**
     * Creates a JavaFX button from a button definition
     */
    private javafx.scene.control.Button createAutomationButton(Button buttonDef) {
        javafx.scene.control.Button button = new javafx.scene.control.Button(buttonDef.getLabel());

        // Apply styling if defined
        if (buttonDef.getStyling() != null) {
            Button.ButtonStyling styling = buttonDef.getStyling();
            StringBuilder styleString = new StringBuilder();

            if (styling.getBackgroundColor() != null) {
                styleString.append("-fx-background-color: ").append(styling.getBackgroundColor()).append("; ");
            }

            if (styling.getTextColor() != null) {
                styleString.append("-fx-text-fill: ").append(styling.getTextColor()).append("; ");
            }

            if (styling.getSize() != null) {
                switch (styling.getSize().toLowerCase()) {
                    case "small":
                        styleString.append("-fx-font-size: 10px; ");
                        break;
                    case "large":
                        styleString.append("-fx-font-size: 14px; ");
                        break;
                    default:
                        styleString.append("-fx-font-size: 12px; ");
                }
            }

            if (styling.getCustomClass() != null) {
                button.getStyleClass().add(styling.getCustomClass());
            }

            button.setStyle(styleString.toString());
        }

        // Set tooltip if defined
        if (buttonDef.getTooltip() != null) {
            button.setTooltip(new Tooltip(buttonDef.getTooltip()));
        }

        // Set action
        button.setOnAction(e -> runAutomation(buttonDef));

        return button;
    }

    /**
     * Runs the automation function associated with the button
     */
    private void runAutomation(Button buttonDef) {
        if (isRunning) {
            log("Another automation task is already running. Please wait or stop it first.");
            return;
        }

        // Add confirmation dialog if required
        if (buttonDef.isConfirmationRequired()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Automation");
            alert.setHeaderText("Run " + buttonDef.getLabel() + "?");
            alert.setContentText(buttonDef.getConfirmationMessage() != null ?
                    buttonDef.getConfirmationMessage() :
                    "Are you sure you want to run this automation?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        log("Starting automation: " + buttonDef.getLabel());
        isRunning = true;

        executorService.submit(() -> {
            try {
                automationExecutor.executeAutomation(buttonDef);
            } catch (Exception ex) {
                logger.error("Error executing automation", ex);
                log("ERROR: " + ex.getMessage());
            } finally {
                isRunning = false;
                log("Automation complete: " + buttonDef.getLabel());
            }
        });
    }

    /**
     * Stops all running automation
     */
    private void stopAllAutomation() {
        if (!isRunning) {
            log("No automation is currently running.");
            return;
        }

        log("Stopping all automation...");
        automationExecutor.stopAllAutomation();
        isRunning = false;
    }

    /**
     * Adds a log entry to the log area
     */
    public void log(String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String logEntry = "[" + timestamp + "] " + message;

        Platform.runLater(() -> {
            logArea.appendText(logEntry + "\n");
            // Auto-scroll to bottom
            logArea.positionCaret(logArea.getText().length());
        });
    }
}