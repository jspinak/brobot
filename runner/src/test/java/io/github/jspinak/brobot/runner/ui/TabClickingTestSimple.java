package io.github.jspinak.brobot.runner.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

/** Simplified test to diagnose tab clicking issues. */
@ExtendWith(ApplicationExtension.class)
public class TabClickingTestSimple {

    private TabPane tabPane;
    private Tab tab1;
    private Tab tab2;
    private AtomicInteger selectionCount = new AtomicInteger(0);

    @Start
    public void start(Stage stage) {
        // Create simple tabs with content
        tab1 = new Tab("Tab1");
        tab1.setContent(new Label("Content 1"));
        tab1.setClosable(false);

        tab2 = new Tab("Tab2");
        tab2.setContent(new Label("Content 2"));
        tab2.setClosable(false);

        // Create TabPane
        tabPane = new TabPane(tab1, tab2);

        // Track selections
        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldTab, newTab) -> {
                            if (newTab != null) {
                                selectionCount.incrementAndGet();
                                System.out.println(
                                        "Tab selected: "
                                                + newTab.getText()
                                                + " (selection #"
                                                + selectionCount.get()
                                                + ")");
                            }
                        });

        // Create scene
        StackPane root = new StackPane(tabPane);
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Simple Tab Test");
        stage.show();

        // Force layout
        stage.sizeToScene();

        System.out.println("Stage shown, tabs created");
    }

    @Test
    public void testBasicTabSelection(FxRobot robot) {
        // Wait for UI to be ready
        WaitForAsyncUtils.waitForFxEvents();

        System.out.println("Starting test...");
        System.out.println(
                "Initial selection: " + tabPane.getSelectionModel().getSelectedItem().getText());

        // Try programmatic selection first
        Platform.runLater(
                () -> {
                    System.out.println("Programmatically selecting tab2");
                    tabPane.getSelectionModel().select(tab2);
                });

        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(
                tab2,
                tabPane.getSelectionModel().getSelectedItem(),
                "Programmatic selection should work");

        // Now try clicking
        System.out.println("Clicking on Tab1...");
        robot.clickOn("Tab1");
        WaitForAsyncUtils.waitForFxEvents();

        System.out.println(
                "After click, selected: "
                        + tabPane.getSelectionModel().getSelectedItem().getText());

        assertEquals(
                tab1,
                tabPane.getSelectionModel().getSelectedItem(),
                "Click on Tab1 should select it");
    }

    @Test
    public void testDirectTabSelection() {
        // Test direct selection without clicking
        Platform.runLater(
                () -> {
                    tabPane.getSelectionModel().select(0);
                });
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(tab1, tabPane.getSelectionModel().getSelectedItem());

        Platform.runLater(
                () -> {
                    tabPane.getSelectionModel().select(1);
                });
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(tab2, tabPane.getSelectionModel().getSelectedItem());
    }
}
