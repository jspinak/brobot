package io.github.jspinak.brobot.runner.ui.utils;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Fixes tab clicking issues in JavaFX TabPane. This addresses the problem where tab clicks don't
 * register or are delayed.
 */
@Slf4j
public class TabClickFix {

    /** Apply comprehensive tab clicking fixes to a TabPane. */
    public static void fixTabClicking(TabPane tabPane) {
        if (tabPane == null) return;

        // Disable animations that might interfere
        tabPane.setStyle("-fx-open-tab-animation: NONE; -fx-close-tab-animation: NONE;");

        // Force tab pane to be focusable
        tabPane.setFocusTraversable(true);

        // Add multiple event filters to catch clicks at different stages
        tabPane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> handleTabClick(event, tabPane));
        tabPane.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> handleTabClick(event, tabPane));
        tabPane.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> handleTabClick(event, tabPane));

        // Add additional handlers for each tab
        for (Tab tab : tabPane.getTabs()) {
            enhanceTab(tab, tabPane);
        }

        // Listen for new tabs being added
        tabPane.getTabs()
                .addListener(
                        (javafx.collections.ListChangeListener<Tab>)
                                c -> {
                                    while (c.next()) {
                                        if (c.wasAdded()) {
                                            for (Tab tab : c.getAddedSubList()) {
                                                enhanceTab(tab, tabPane);
                                            }
                                        }
                                    }
                                });

        log.info("Tab clicking fixes applied to TabPane");
    }

    /** Handle tab click events. */
    private static void handleTabClick(MouseEvent event, TabPane tabPane) {
        // Find which tab was clicked
        Node target = (Node) event.getTarget();
        Tab clickedTab = findTabFromNode(target, tabPane);

        if (clickedTab != null && clickedTab != tabPane.getSelectionModel().getSelectedItem()) {
            log.debug(
                    "Tab {} event detected on: {}",
                    event.getEventType().getName(),
                    clickedTab.getText());

            // Select the tab immediately
            tabPane.getSelectionModel().select(clickedTab);
            tabPane.requestFocus();

            // Consume the event to prevent double processing
            event.consume();
        }
    }

    /** Enhance individual tab with better click handling. */
    private static void enhanceTab(Tab tab, TabPane tabPane) {
        // Make tab content focusable
        if (tab.getContent() != null) {
            tab.getContent().setFocusTraversable(true);
        }

        // Add direct click handler to tab graphic if present
        if (tab.getGraphic() != null) {
            tab.getGraphic()
                    .setOnMouseClicked(
                            event -> {
                                Platform.runLater(() -> tabPane.getSelectionModel().select(tab));
                                event.consume();
                            });
        }
    }

    /** Find which tab a node belongs to by traversing up the scene graph. */
    private static Tab findTabFromNode(Node node, TabPane tabPane) {
        // First check if we clicked on text that matches a tab
        if (node instanceof javafx.scene.text.Text) {
            String text = ((javafx.scene.text.Text) node).getText();
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getText() != null && tab.getText().equals(text)) {
                    log.debug("Found tab by text match: {}", text);
                    return tab;
                }
            }
        }

        // Check if the node is a Label
        if (node instanceof javafx.scene.control.Label) {
            String text = ((javafx.scene.control.Label) node).getText();
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getText() != null && tab.getText().equals(text)) {
                    log.debug("Found tab by label match: {}", text);
                    return tab;
                }
            }
        }

        // Check if the node or its parents contain tab header information
        Node current = node;
        while (current != null) {
            // Check style classes for tab identification
            var styleClasses = current.getStyleClass();

            // Look for tab header indicators
            if (styleClasses.contains("tab")
                    || styleClasses.contains("tab-label")
                    || styleClasses.contains("tab-close-button")) {

                // Try to find the tab by position
                Bounds nodeBounds = current.localToScene(current.getBoundsInLocal());
                return findTabByPosition(tabPane, nodeBounds);
            }

            current = current.getParent();
        }

        // Fallback: find by mouse position
        return null;
    }

    /** Find tab by position in the tab header area. */
    private static Tab findTabByPosition(TabPane tabPane, Bounds bounds) {
        if (bounds == null) return null;

        // Get tab header area
        Node tabHeaderArea = tabPane.lookup(".tab-header-area");
        if (tabHeaderArea == null) return null;

        // Simple approach: divide header width by number of tabs
        double headerWidth = tabHeaderArea.getBoundsInLocal().getWidth();
        double tabWidth = headerWidth / tabPane.getTabs().size();
        double clickX =
                bounds.getMinX()
                        - tabHeaderArea.localToScene(tabHeaderArea.getBoundsInLocal()).getMinX();

        int tabIndex = (int) (clickX / tabWidth);

        if (tabIndex >= 0 && tabIndex < tabPane.getTabs().size()) {
            return tabPane.getTabs().get(tabIndex);
        }

        return null;
    }

    /** Force immediate tab selection without animation. */
    public static void selectTabImmediately(TabPane tabPane, Tab tab) {
        if (tabPane == null || tab == null) return;

        Platform.runLater(
                () -> {
                    // Disable any ongoing animations
                    tabPane.setDisable(true);

                    // Select the tab
                    tabPane.getSelectionModel().select(tab);

                    // Re-enable after selection
                    Platform.runLater(
                            () -> {
                                tabPane.setDisable(false);
                                tabPane.requestFocus();
                            });
                });
    }

    /** Test method to simulate a tab click programmatically. */
    public static void simulateTabClick(TabPane tabPane, int tabIndex) {
        if (tabPane == null || tabIndex < 0 || tabIndex >= tabPane.getTabs().size()) {
            return;
        }

        Tab targetTab = tabPane.getTabs().get(tabIndex);

        Platform.runLater(
                () -> {
                    // Create and fire a mouse click event
                    Node tabHeaderArea = tabPane.lookup(".tab-header-area");
                    if (tabHeaderArea != null) {
                        double tabWidth =
                                tabHeaderArea.getBoundsInLocal().getWidth()
                                        / tabPane.getTabs().size();
                        double clickX = (tabIndex * tabWidth) + (tabWidth / 2);
                        double clickY = tabHeaderArea.getBoundsInLocal().getHeight() / 2;

                        MouseEvent clickEvent =
                                new MouseEvent(
                                        MouseEvent.MOUSE_CLICKED,
                                        clickX,
                                        clickY,
                                        clickX,
                                        clickY,
                                        MouseButton.PRIMARY,
                                        1,
                                        false,
                                        false,
                                        false,
                                        false,
                                        true,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        null);

                        tabHeaderArea.fireEvent(clickEvent);
                    }

                    // Fallback: direct selection
                    tabPane.getSelectionModel().select(targetTab);
                });
    }
}
