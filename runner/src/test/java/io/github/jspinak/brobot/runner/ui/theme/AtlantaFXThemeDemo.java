package io.github.jspinak.brobot.runner.ui.theme;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Demo application showcasing AtlantaFX themes.
 * Run this to see how different themes look with the Brobot Runner UI components.
 */
public class AtlantaFXThemeDemo extends Application {
    
    private ComboBox<String> themeSelector;
    private Label statusLabel;
    private ProgressBar progressBar;
    
    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        
        // Header
        Label titleLabel = new Label("Brobot Runner - AtlantaFX Theme Demo");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label subtitleLabel = new Label("Modern themes for JavaFX applications");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-muted;");
        
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(titleLabel, subtitleLabel);
        
        // Theme selector
        HBox themeBox = new HBox(10);
        themeBox.setAlignment(Pos.CENTER);
        Label themeLabel = new Label("Select Theme:");
        themeSelector = new ComboBox<>();
        themeSelector.getItems().addAll(
            "Primer Light",
            "Primer Dark",
            "Nord Light",
            "Nord Dark",
            "Cupertino Light",
            "Cupertino Dark",
            "Dracula"
        );
        themeSelector.setValue("Primer Light");
        themeSelector.setOnAction(e -> changeTheme());
        themeBox.getChildren().addAll(themeLabel, themeSelector);
        
        // Automation controls section
        TitledPane automationPane = createAutomationSection();
        
        // Sample components section
        TitledPane componentsPane = createComponentsSection();
        
        // File browser section
        TitledPane filePane = createFileBrowserSection();
        
        // Status section
        VBox statusSection = createStatusSection();
        
        // Add all sections to root
        root.getChildren().addAll(
            header,
            new Separator(),
            themeBox,
            automationPane,
            componentsPane,
            filePane,
            statusSection
        );
        
        // Create scrollable scene
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        
        Scene scene = new Scene(scrollPane, 800, 700);
        
        // Apply initial theme
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        
        primaryStage.setTitle("AtlantaFX Theme Demo - Brobot Runner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private TitledPane createAutomationSection() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        // Control buttons
        HBox controls = new HBox(10);
        Button startBtn = new Button("▶ Start");
        startBtn.getStyleClass().add("accent");
        
        Button pauseBtn = new Button("⏸ Pause");
        Button stopBtn = new Button("⏹ Stop");
        stopBtn.getStyleClass().add("danger");
        
        Button resumeBtn = new Button("↻ Resume");
        
        controls.getChildren().addAll(startBtn, pauseBtn, stopBtn, resumeBtn);
        
        // Hotkey info
        Label hotkeyLabel = new Label("Hotkeys: Ctrl+P (Pause), Ctrl+R (Resume), Ctrl+S (Stop)");
        hotkeyLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        
        content.getChildren().addAll(controls, hotkeyLabel);
        
        TitledPane pane = new TitledPane("Automation Controls", content);
        pane.setExpanded(true);
        return pane;
    }
    
    private TitledPane createComponentsSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));
        
        // Text inputs
        grid.add(new Label("Text Field:"), 0, 0);
        TextField textField = new TextField();
        textField.setPromptText("Enter text...");
        grid.add(textField, 1, 0);
        
        grid.add(new Label("Password:"), 0, 1);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password...");
        grid.add(passwordField, 1, 1);
        
        // Choices
        grid.add(new Label("Choice Box:"), 0, 2);
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("Option 1", "Option 2", "Option 3");
        choiceBox.setValue("Option 1");
        grid.add(choiceBox, 1, 2);
        
        // Toggles
        grid.add(new Label("Options:"), 0, 3);
        VBox toggles = new VBox(5);
        CheckBox checkBox = new CheckBox("Enable feature");
        RadioButton radio1 = new RadioButton("Mode A");
        RadioButton radio2 = new RadioButton("Mode B");
        ToggleGroup group = new ToggleGroup();
        radio1.setToggleGroup(group);
        radio2.setToggleGroup(group);
        radio1.setSelected(true);
        toggles.getChildren().addAll(checkBox, radio1, radio2);
        grid.add(toggles, 1, 3);
        
        // Slider
        grid.add(new Label("Slider:"), 0, 4);
        Slider slider = new Slider(0, 100, 50);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        grid.add(slider, 1, 4);
        
        TitledPane pane = new TitledPane("UI Components", grid);
        pane.setExpanded(false);
        return pane;
    }
    
    private TitledPane createFileBrowserSection() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search files...");
        
        // File list
        ListView<String> fileList = new ListView<>();
        fileList.getItems().addAll(
            "📁 automation-scripts",
            "📁 configurations",
            "📄 brobot-config.json",
            "📄 automation-log.txt",
            "🖼 screenshot-001.png",
            "📊 test-results.csv"
        );
        fileList.setPrefHeight(150);
        
        // Action buttons
        HBox actions = new HBox(10);
        Button uploadBtn = new Button("⬆ Upload");
        uploadBtn.getStyleClass().add("accent");
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("danger");
        actions.getChildren().addAll(uploadBtn, deleteBtn);
        
        content.getChildren().addAll(searchField, fileList, actions);
        
        TitledPane pane = new TitledPane("File Browser", content);
        pane.setExpanded(false);
        return pane;
    }
    
    private VBox createStatusSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10, 0, 0, 0));
        
        // Status indicators
        HBox statusBox = new HBox(20);
        statusBox.setAlignment(Pos.CENTER);
        
        statusLabel = new Label("● Connected");
        statusLabel.setStyle("-fx-text-fill: -color-success-fg;");
        
        Label automationStatus = new Label("● Automation Ready");
        automationStatus.setStyle("-fx-text-fill: -color-warning-fg;");
        
        statusBox.getChildren().addAll(statusLabel, automationStatus);
        
        // Progress
        progressBar = new ProgressBar(0.65);
        progressBar.setPrefWidth(300);
        
        Label progressLabel = new Label("Processing: 65%");
        progressLabel.setStyle("-fx-text-fill: -color-fg-muted;");
        
        VBox progressBox = new VBox(5);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.getChildren().addAll(progressBar, progressLabel);
        
        section.getChildren().addAll(
            new Separator(),
            statusBox,
            progressBox
        );
        
        return section;
    }
    
    private void changeTheme() {
        String selected = themeSelector.getValue();
        Theme theme = switch (selected) {
            case "Primer Light" -> new PrimerLight();
            case "Primer Dark" -> new PrimerDark();
            case "Nord Light" -> new NordLight();
            case "Nord Dark" -> new NordDark();
            case "Cupertino Light" -> new CupertinoLight();
            case "Cupertino Dark" -> new CupertinoDark();
            case "Dracula" -> new Dracula();
            default -> new PrimerLight();
        };
        
        Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
        
        // Update status to show theme change
        statusLabel.setText("● Theme: " + selected);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}