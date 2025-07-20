package io.github.jspinak.brobot.runner.ui;

import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Styles;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotPanel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Simple test to verify AtlantaFX styling is applied correctly
 */
public class StyleTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Apply AtlantaFX theme
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        
        // Create a test panel
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");
        
        // Create a BrobotPanel to test card styling
        BrobotPanel panel = new BrobotPanel() {
            @Override
            protected void initialize() {
                VBox content = new VBox(12);
                content.setPadding(new Insets(16));
                
                Label title = new Label("AtlantaFX Style Test");
                title.getStyleClass().addAll(Styles.TITLE_3);
                
                Label subtitle = new Label("Testing button styles:");
                subtitle.getStyleClass().addAll(Styles.TEXT_MUTED);
                
                Button accentBtn = new Button("Accent Button");
                accentBtn.getStyleClass().addAll(Styles.ACCENT);
                
                Button successBtn = new Button("Success Button");
                successBtn.getStyleClass().addAll(Styles.SUCCESS);
                
                Button dangerBtn = new Button("Danger Button");
                dangerBtn.getStyleClass().addAll(Styles.DANGER);
                
                Button outlinedBtn = new Button("Outlined Button");
                outlinedBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
                
                content.getChildren().addAll(title, subtitle, accentBtn, successBtn, dangerBtn, outlinedBtn);
                getChildren().add(content);
            }
        };
        
        root.getChildren().add(panel);
        
        Scene scene = new Scene(root, 400, 350);
        primaryStage.setTitle("Brobot Runner - Style Test");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Print confirmation
        System.out.println("Style test window opened successfully!");
        System.out.println("Card background: white with shadow");
        System.out.println("Content background: #f8f9fa");
        System.out.println("Buttons styled with AtlantaFX classes");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}