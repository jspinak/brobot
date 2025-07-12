import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Standalone demo showing the AtlantaFX-styled UI
 */
public class AtlantaFXDemo extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Main container with light background
        VBox root = new VBox(16);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #f8f9fa;");
        
        // Toolbar with AtlantaFX styling
        HBox toolbar = createToolbar();
        
        // Content area with cards
        FlowPane cardContainer = new FlowPane(16, 16);
        
        // Create styled cards
        VBox card1 = createCard("Configuration", createFormContent());
        VBox card2 = createCard("Actions", createButtonContent());
        VBox card3 = createCard("Status", createStatusContent());
        
        cardContainer.getChildren().addAll(card1, card2, card3);
        
        // Tab pane with floating style
        TabPane tabPane = createTabPane();
        
        root.getChildren().addAll(toolbar, cardContainer, tabPane);
        
        Scene scene = new Scene(new ScrollPane(root), 1000, 700);
        
        // Apply basic AtlantaFX-like styling
        scene.getStylesheets().add("data:text/css," + getAtlantaFXStyles());
        
        primaryStage.setTitle("Brobot Runner - AtlantaFX Style Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(8, 12, 8, 12));
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        
        Label titleLabel = new Label("Brobot Runner");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button themeButton = new Button("🌙");
        themeButton.setStyle("-fx-background-color: transparent; -fx-padding: 8px; -fx-font-size: 16px;");
        
        toolbar.getChildren().addAll(titleLabel, spacer, themeButton);
        return toolbar;
    }
    
    private VBox createCard(String title, Region content) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        card.setPrefWidth(300);
        
        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(8);
        shadow.setOffsetY(2);
        card.setEffect(shadow);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600;");
        
        card.getChildren().addAll(titleLabel, content);
        return card;
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
    
    private HBox createButtonContent() {
        HBox content = new HBox(8);
        
        Button accentBtn = new Button("Run Automation");
        accentBtn.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; -fx-padding: 8px 16px; -fx-background-radius: 6px;");
        
        Button successBtn = new Button("Save");
        successBtn.setStyle("-fx-background-color: #4ADE80; -fx-text-fill: white; -fx-padding: 8px 16px; -fx-background-radius: 6px;");
        
        Button dangerBtn = new Button("Stop");
        dangerBtn.setStyle("-fx-background-color: #F87171; -fx-text-fill: white; -fx-padding: 8px 16px; -fx-background-radius: 6px;");
        
        content.getChildren().addAll(accentBtn, successBtn, dangerBtn);
        
        return content;
    }
    
    private GridPane createStatusContent() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        
        grid.add(new Label("Status:"), 0, 0);
        Label statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #4ADE80; -fx-font-weight: 600;");
        grid.add(statusLabel, 1, 0);
        
        grid.add(new Label("Progress:"), 0, 1);
        ProgressBar progressBar = new ProgressBar(0.7);
        grid.add(progressBar, 1, 1);
        
        grid.add(new Label("Tasks:"), 0, 2);
        grid.add(new Label("15 / 20"), 1, 2);
        
        return grid;
    }
    
    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: white;");
        
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
        return tabPane;
    }
    
    private String getAtlantaFXStyles() {
        return """
            .root {
                -fx-font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                -fx-font-size: 14px;
            }
            .label {
                -fx-text-fill: #1e293b;
            }
            .text-field, .combo-box {
                -fx-background-color: #f1f5f9;
                -fx-background-radius: 6px;
                -fx-padding: 8px 12px;
                -fx-border-color: transparent;
            }
            .text-field:focused, .combo-box:focused {
                -fx-background-color: white;
                -fx-border-color: #5865F2;
                -fx-border-width: 2px;
            }
            .button {
                -fx-cursor: hand;
                -fx-font-weight: 600;
            }
            .button:hover {
                -fx-opacity: 0.9;
            }
            .tab-pane {
                -fx-background-color: transparent;
            }
            .tab-pane .tab-header-area {
                -fx-background-color: transparent;
            }
            .tab-pane .tab {
                -fx-background-color: transparent;
                -fx-padding: 8px 16px;
            }
            .tab-pane .tab:selected {
                -fx-background-color: white;
                -fx-background-radius: 8px 8px 0 0;
            }
            """;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}