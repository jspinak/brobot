package io.github.jspinak.brobot.runner.ui.utils;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import lombok.extern.slf4j.Slf4j;

/**
 * Special fix for making tabs clickable in TestFX tests. This works around JavaFX's complex tab
 * header structure.
 */
@Slf4j
public class TestFXTabClickFix {

    /** Make tabs clickable by TestFX by adding clickable labels. */
    public static void makeTabsClickable(TabPane tabPane) {
        if (tabPane == null) return;

        // For each tab, ensure it has a graphic that TestFX can click on
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getGraphic() == null && tab.getText() != null) {
                // Create a clickable label
                Label label = new Label(tab.getText());
                label.setOnMouseClicked(
                        event -> {
                            log.debug("Label clicked for tab: {}", tab.getText());
                            Platform.runLater(
                                    () -> {
                                        tabPane.getSelectionModel().select(tab);
                                    });
                        });

                // Set the label as the tab's graphic
                tab.setGraphic(label);
                // Clear the text to avoid duplication
                tab.setText("");
            }
        }

        // Also add a more direct click handler using lookup
        Platform.runLater(
                () -> {
                    Node tabHeaderArea = tabPane.lookup(".tab-header-area");
                    if (tabHeaderArea != null) {
                        tabHeaderArea.setOnMouseClicked(
                                event -> {
                                    handleTabHeaderClick(event, tabPane);
                                });
                    }
                });

        log.info("Tabs made clickable for TestFX");
    }

    /** Handle clicks on the tab header area. */
    private static void handleTabHeaderClick(MouseEvent event, TabPane tabPane) {
        double x = event.getX();
        Node tabHeaderArea = (Node) event.getSource();
        double width = tabHeaderArea.getBoundsInLocal().getWidth();
        int tabCount = tabPane.getTabs().size();

        if (tabCount > 0) {
            double tabWidth = width / tabCount;
            int clickedIndex = (int) (x / tabWidth);

            if (clickedIndex >= 0 && clickedIndex < tabCount) {
                Tab clickedTab = tabPane.getTabs().get(clickedIndex);
                log.debug(
                        "Tab header clicked at index: {} ({})", clickedIndex, clickedTab.getText());
                tabPane.getSelectionModel().select(clickedIndex);
            }
        }
    }

    /** Create a simple clickable tab setup for testing. */
    public static TabPane createTestableTabPane(String... tabNames) {
        TabPane tabPane = new TabPane();

        for (String name : tabNames) {
            Tab tab = new Tab();
            tab.setClosable(false);

            // Create a clickable label as the graphic
            Label label = new Label(name);
            label.setStyle("-fx-padding: 5 10 5 10;"); // Add padding for easier clicking
            tab.setGraphic(label);

            // Add click handler to the label
            label.setOnMouseClicked(
                    e -> {
                        tabPane.getSelectionModel().select(tab);
                    });

            // Add some content
            tab.setContent(new StackPane(new Label("Content for " + name)));

            tabPane.getTabs().add(tab);
        }

        // Apply additional fixes
        makeTabsClickable(tabPane);

        return tabPane;
    }

    /** Simulate a click on a tab by name. */
    public static void clickTab(TabPane tabPane, String tabName) {
        Platform.runLater(
                () -> {
                    for (Tab tab : tabPane.getTabs()) {
                        // Check the graphic (label) text
                        if (tab.getGraphic() instanceof Label) {
                            Label label = (Label) tab.getGraphic();
                            if (tabName.equals(label.getText())) {
                                log.debug("Selecting tab by name: {}", tabName);
                                tabPane.getSelectionModel().select(tab);
                                return;
                            }
                        }
                        // Also check the tab text
                        if (tabName.equals(tab.getText())) {
                            log.debug("Selecting tab by text: {}", tabName);
                            tabPane.getSelectionModel().select(tab);
                            return;
                        }
                    }
                    log.warn("Tab not found: {}", tabName);
                });
    }
}
