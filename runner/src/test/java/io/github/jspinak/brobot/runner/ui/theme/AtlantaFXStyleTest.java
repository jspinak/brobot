package io.github.jspinak.brobot.runner.ui.theme;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Styles;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotPanel;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotCard;

/**
 * Test application to verify AtlantaFX styling
 */
public class AtlantaFXStyleTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Apply AtlantaFX theme
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        
        // Main container with background
        VBox root = new VBox(16);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #f8f9fa;");
        
        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().addAll("toolbar");
        toolbar.setPadding(new Insets(8, 12, 8, 12));
        
        Label titleLabel = new Label("AtlantaFX Style Test");
        titleLabel.getStyleClass().addAll(Styles.TITLE_3);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        ToggleButton themeToggle = new ToggleButton("🌙");
        themeToggle.getStyleClass().addAll(Styles.BUTTON_ICON);
        
        toolbar.getChildren().addAll(titleLabel, spacer, themeToggle);
        
        // Cards container
        FlowPane cardContainer = new FlowPane(16, 16);
        
        // Create sample cards
        BrobotCard card1 = new BrobotCard("Configuration");
        card1.addContent(createFormContent());
        card1.setPrefWidth(300);
        
        BrobotCard card2 = new BrobotCard("Actions");
        card2.addContent(createButtonContent());
        card2.setPrefWidth(300);
        
        BrobotCard card3 = new BrobotCard("Status");
        card3.addContent(createStatusContent());
        card3.setPrefWidth(300);
        
        cardContainer.getChildren().addAll(card1, card2, card3);
        
        // Tab pane
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add(Styles.TABS_FLOATING);
        
        Tab tab1 = new Tab("Automation");
        tab1.setContent(new Label("Automation content"));
        tab1.setClosable(false);
        
        Tab tab2 = new Tab("Configuration");
        tab2.setContent(new Label("Configuration content"));
        tab2.setClosable(false);
        
        Tab tab3 = new Tab("Logs");
        tab3.setContent(new Label("Logs content"));
        tab3.setClosable(false);
        
        tabPane.getTabs().addAll(tab1, tab2, tab3);
        
        root.getChildren().addAll(toolbar, cardContainer, tabPane);
        
        Scene scene = new Scene(new ScrollPane(root), 1000, 700);
        
        primaryStage.setTitle("AtlantaFX Style Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox createFormContent() {
        VBox content = new VBox(12);
        
        TextField textField = new TextField();
        textField.setPromptText("Enter project name");
        
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Option 1", "Option 2", "Option 3");
        comboBox.setPromptText("Select an option");
        
        CheckBox checkBox = new CheckBox("Enable feature");
        
        content.getChildren().addAll(
            new Label("Project Name:"),
            textField,
            new Label("Type:"),
            comboBox,
            checkBox
        );
        
        return content;
    }
    
    private VBox createButtonContent() {
        VBox content = new VBox(12);
        
        Button accentBtn = new Button("Run Automation");
        accentBtn.getStyleClass().addAll(Styles.ACCENT);
        
        Button successBtn = new Button("Save");
        successBtn.getStyleClass().addAll(Styles.SUCCESS);
        
        Button dangerBtn = new Button("Stop");
        dangerBtn.getStyleClass().addAll(Styles.DANGER);
        
        Button outlinedBtn = new Button("Configure");
        outlinedBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        
        HBox buttonGroup = new HBox(8);
        buttonGroup.getChildren().addAll(accentBtn, successBtn, dangerBtn, outlinedBtn);
        
        content.getChildren().add(buttonGroup);
        
        return content;
    }
    
    private GridPane createStatusContent() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        
        grid.add(new Label("Status:"), 0, 0);
        Label statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add(Styles.SUCCESS);
        grid.add(statusLabel, 1, 0);
        
        grid.add(new Label("Progress:"), 0, 1);
        ProgressBar progressBar = new ProgressBar(0.7);
        grid.add(progressBar, 1, 1);
        
        grid.add(new Label("Tasks:"), 0, 2);
        grid.add(new Label("15 / 20"), 1, 2);
        
        return grid;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}