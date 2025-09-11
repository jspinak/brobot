package io.github.jspinak.brobot.runner.ui.theme;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Simple test application to verify the modern theme is working */
public class ThemeTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        ThemeManager themeManager = new ThemeManager();
        themeManager.initialize();

        VBox root = new VBox(10);
        root.getStyleClass().add("app-container");
        root.setStyle("-fx-padding: 20px;");

        Label titleLabel = new Label("Brobot Runner - Theme Test");
        titleLabel.getStyleClass().add("title-label");

        Label subtitleLabel = new Label("Testing the modern light theme");
        subtitleLabel.getStyleClass().add("subtitle");

        Label categoryLabel = new Label("Category Label");
        categoryLabel.getStyleClass().add("category-label");

        Button primaryButton = new Button("Primary Button");

        Button secondaryButton = new Button("Secondary Button");
        secondaryButton.getStyleClass().add("button-secondary");

        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.getChildren()
                .addAll(
                        new Label("This is a card"),
                        new Label("With multiple labels"),
                        new Label("To test overlap issues"));

        root.getChildren()
                .addAll(
                        titleLabel,
                        subtitleLabel,
                        categoryLabel,
                        primaryButton,
                        secondaryButton,
                        card);

        Scene scene = new Scene(root, 600, 400);
        themeManager.registerScene(scene);

        primaryStage.setTitle("Theme Test");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Test theme switching
        primaryButton.setOnAction(
                e -> {
                    themeManager.toggleTheme();
                    System.out.println("Switched to theme: " + themeManager.getCurrentTheme());
                });

        // Close after 10 seconds for automated testing
        new Thread(
                        () -> {
                            try {
                                Thread.sleep(10000);
                                Platform.runLater(primaryStage::close);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        })
                .start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
