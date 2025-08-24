package io.github.jspinak.brobot.runner.ui.config.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Base class for TestFX UI tests that initializes a JavaFX environment.
 */
@ExtendWith(ApplicationExtension.class)
public abstract class TestFxBase extends ApplicationTest {

    protected Stage stage;
    protected StackPane root;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.root = new StackPane();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Adds a component to the test scene.
     */
    protected void addToRoot(javafx.scene.Node node) {
        root.getChildren().clear();
        root.getChildren().add(node);
    }
}