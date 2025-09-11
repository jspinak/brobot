package io.github.jspinak.brobot.runner.ui.config;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.ui.components.BrobotButton;

import lombok.extern.slf4j.Slf4j;

/**
 * Action bar component for configuration panel. Single responsibility: Manages
 * configuration-related actions.
 */
@Slf4j
public class ConfigActionBar extends HBox {

    private final EventBus eventBus;

    // Action callbacks
    private Runnable onNewConfiguration;
    private Runnable onImport;
    private Runnable onRefresh;

    // UI components
    private final Label configPathLabel;
    private final BrobotButton changePathBtn;
    private final BrobotButton openFolderBtn;

    public ConfigActionBar(EventBus eventBus) {
        this.eventBus = eventBus;

        // Setup styling
        getStyleClass().add("config-action-bar");
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);

        // Create primary action buttons
        BrobotButton newConfigBtn = BrobotButton.primary("New Configuration");
        newConfigBtn.setOnAction(
                e -> {
                    if (onNewConfiguration != null) onNewConfiguration.run();
                });

        BrobotButton importBtn = BrobotButton.secondary("Import");
        importBtn.setOnAction(
                e -> {
                    if (onImport != null) onImport.run();
                });

        BrobotButton refreshBtn = BrobotButton.secondary("Refresh");
        refreshBtn.setOnAction(
                e -> {
                    if (onRefresh != null) onRefresh.run();
                });

        // Config path section
        configPathLabel = new Label("Config Path: config");
        configPathLabel.getStyleClass().add("config-path-label");

        changePathBtn = BrobotButton.secondary("Change...");
        changePathBtn.setOnAction(e -> changeConfigPath());

        openFolderBtn = BrobotButton.secondary("Open Folder");
        openFolderBtn.setOnAction(e -> openConfigFolder());

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Add all components
        getChildren()
                .addAll(
                        newConfigBtn,
                        importBtn,
                        refreshBtn,
                        spacer,
                        configPathLabel,
                        changePathBtn,
                        openFolderBtn);
    }

    private void changeConfigPath() {
        log.info("Changing configuration path");
        // Implementation for changing config path
    }

    private void openConfigFolder() {
        log.info("Opening configuration folder");
        // Implementation for opening config folder
    }

    // Setters for action callbacks
    public void setOnNewConfiguration(Runnable action) {
        this.onNewConfiguration = action;
    }

    public void setOnImport(Runnable action) {
        this.onImport = action;
    }

    public void setOnRefresh(Runnable action) {
        this.onRefresh = action;
    }

    public void updateConfigPath(String path) {
        configPathLabel.setText("Config Path: " + path);
    }
}
