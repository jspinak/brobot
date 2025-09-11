package io.github.jspinak.brobot.runner.ui.components.base;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Base panel class for all Atlanta-styled panels. Provides consistent padding, spacing, and layout
 * structure.
 */
public abstract class AtlantaBasePanel extends VBox {

    // Standard spacing constants
    protected static final double PANEL_PADDING = 24.0;
    protected static final double ACTION_BAR_SPACING = 16.0;
    protected static final double BUTTON_GROUP_SPACING = 24.0;
    protected static final double CONTENT_SPACING = 24.0;
    protected static final double CARD_SPACING = 24.0;

    private Label titleLabel;
    private HBox actionBar;
    private VBox contentArea;

    public AtlantaBasePanel() {
        initialize();
    }

    public AtlantaBasePanel(String title) {
        initialize();
        setTitle(title);
    }

    private void initialize() {
        // Set consistent panel padding
        setPadding(new Insets(PANEL_PADDING));
        setSpacing(CONTENT_SPACING);
        getStyleClass().add("atlanta-panel");

        // Create title area
        titleLabel = new Label();
        titleLabel.getStyleClass().add("panel-title");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);

        // Create action bar
        actionBar = new HBox(ACTION_BAR_SPACING);
        actionBar.getStyleClass().add("action-bar");
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setVisible(false);
        actionBar.setManaged(false);

        // Create content area
        contentArea = new VBox(CONTENT_SPACING);
        contentArea.getStyleClass().add("content-area");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Add components in order
        getChildren().addAll(titleLabel, actionBar, contentArea);
    }

    /** Sets the panel title. If null or empty, the title is hidden. */
    protected void setTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            titleLabel.setText(title);
            titleLabel.setVisible(true);
            titleLabel.setManaged(true);
        } else {
            titleLabel.setVisible(false);
            titleLabel.setManaged(false);
        }
    }

    /**
     * Returns the action bar for adding controls. The action bar is automatically shown when
     * controls are added.
     */
    protected HBox getActionBar() {
        if (!actionBar.isVisible() && !actionBar.getChildren().isEmpty()) {
            actionBar.setVisible(true);
            actionBar.setManaged(true);
        }
        return actionBar;
    }

    /** Adds content to the action bar with proper spacing. */
    protected void addToActionBar(Region... nodes) {
        actionBar.getChildren().addAll(nodes);
        actionBar.setVisible(true);
        actionBar.setManaged(true);
    }

    /** Creates a button group with consistent spacing. */
    protected HBox createButtonGroup(Region... buttons) {
        HBox group = new HBox(8); // Smaller spacing within groups
        group.setAlignment(Pos.CENTER_LEFT);
        group.getStyleClass().add("button-group");
        group.getChildren().addAll(buttons);
        return group;
    }

    /** Creates a separator for the action bar. */
    protected Region createSeparator() {
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);
        return separator;
    }

    /** Creates a flexible spacer. */
    protected Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /** Returns the content area for adding main content. */
    protected VBox getContentArea() {
        return contentArea;
    }

    /** Adds content to the main content area. */
    protected void addContent(Region content) {
        contentArea.getChildren().add(content);
    }

    /** Creates a standard split layout with two cards. */
    protected HBox createSplitLayout(
            AtlantaCard leftCard, AtlantaCard rightCard, double leftWidth) {
        HBox splitLayout = new HBox(CARD_SPACING);
        splitLayout.getStyleClass().add("split-layout");

        if (leftWidth > 0) {
            leftCard.setMinWidth(leftWidth);
            leftCard.setPrefWidth(leftWidth);
        }

        splitLayout.getChildren().addAll(leftCard, rightCard);
        HBox.setHgrow(rightCard, Priority.ALWAYS);

        return splitLayout;
    }

    /** Creates a responsive split layout that stacks vertically on small screens. */
    protected Region createResponsiveSplitLayout(AtlantaCard leftCard, AtlantaCard rightCard) {
        HBox splitLayout = new HBox(CARD_SPACING);
        splitLayout.getStyleClass().addAll("split-layout", "responsive");

        // Set minimum widths for responsive behavior
        leftCard.setMinWidth(300);
        rightCard.setMinWidth(300);

        splitLayout.getChildren().addAll(leftCard, rightCard);
        HBox.setHgrow(leftCard, Priority.ALWAYS);
        HBox.setHgrow(rightCard, Priority.ALWAYS);

        return splitLayout;
    }
}
