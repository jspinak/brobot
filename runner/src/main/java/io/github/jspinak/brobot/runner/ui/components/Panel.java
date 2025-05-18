package io.github.jspinak.brobot.runner.ui.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;

/**
 * A panel with a title, content, and optional actions.
 * Can be collapsible to save space.
 */
public class Panel extends VBox {

    private final StringProperty title = new SimpleStringProperty();
    private final BooleanProperty collapsible = new SimpleBooleanProperty(false);
    private final BooleanProperty collapsed = new SimpleBooleanProperty(false);

    private final Label titleLabel;
    private final HBox headerBox;
    private final Button collapseButton;
    /**
     *  The content of the panel.
     */
    @Getter
    private final VBox contentBox;
    private final HBox actionsBox;

    /**
     * Creates a new Panel with the specified title.
     *
     * @param title The panel title
     */
    public Panel(String title) {
        this.title.set(title);

        // Set CSS class for styling
        getStyleClass().add("panel");

        // Create header with title
        titleLabel = new Label();
        titleLabel.textProperty().bind(this.title);
        titleLabel.getStyleClass().add("panel-title");

        // Create collapse button
        collapseButton = new Button("▼");
        collapseButton.getStyleClass().add("panel-collapse-button");
        collapseButton.setVisible(false);
        collapseButton.setManaged(false);
        collapseButton.setOnAction(e -> setCollapsed(!isCollapsed()));

        // Create header box
        headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("panel-header");
        headerBox.getChildren().addAll(titleLabel, collapseButton);

        // Create content box
        contentBox = new VBox();
        contentBox.getStyleClass().add("panel-content");
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        // Create actions box
        actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.getStyleClass().add("panel-actions");

        // Add to parent
        getChildren().addAll(headerBox, contentBox, actionsBox);

        // Setup bindings
        setupBindings();
    }

    /**
     * Sets up property bindings.
     */
    private void setupBindings() {
        // Bind collapse button visibility to collapsible property
        collapseButton.visibleProperty().bind(collapsible);
        collapseButton.managedProperty().bind(collapsible);

        // Update collapse button text based on collapsed state
        collapsed.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                collapseButton.setText("▶");
                contentBox.setVisible(false);
                contentBox.setManaged(false);
            } else {
                collapseButton.setText("▼");
                contentBox.setVisible(true);
                contentBox.setManaged(true);
            }
        });
    }

    /**
     * Sets the panel title.
     *
     * @param title The title to set
     */
    public void setTitle(String title) {
        this.title.set(title);
    }

    /**
     * Gets the panel title.
     *
     * @return The panel title
     */
    public String getTitle() {
        return title.get();
    }

    /**
     * Gets the title property.
     *
     * @return The title property
     */
    public StringProperty titleProperty() {
        return title;
    }

    /**
     * Sets whether the panel is collapsible.
     *
     * @param collapsible true if the panel should be collapsible, false otherwise
     */
    public void setCollapsible(boolean collapsible) {
        this.collapsible.set(collapsible);
    }

    /**
     * Gets whether the panel is collapsible.
     *
     * @return true if the panel is collapsible, false otherwise
     */
    public boolean isCollapsible() {
        return collapsible.get();
    }

    /**
     * Gets the collapsible property.
     *
     * @return The collapsible property
     */
    public BooleanProperty collapsibleProperty() {
        return collapsible;
    }

    /**
     * Sets whether the panel is collapsed.
     *
     * @param collapsed true if the panel should be collapsed, false otherwise
     */
    public void setCollapsed(boolean collapsed) {
        this.collapsed.set(collapsed);
    }

    /**
     * Gets whether the panel is collapsed.
     *
     * @return true if the panel is collapsed, false otherwise
     */
    public boolean isCollapsed() {
        return collapsed.get();
    }

    /**
     * Gets the collapsed property.
     *
     * @return The collapsed property
     */
    public BooleanProperty collapsedProperty() {
        return collapsed;
    }

    /**
     * Sets the content of the panel.
     *
     * @param content The content to set
     */
    public void setContent(Node content) {
        contentBox.getChildren().clear();
        if (content != null) {
            contentBox.getChildren().add(content);
        }
    }

    /**
     * Adds a node to the panel content.
     *
     * @param content The content to add
     */
    public void addContent(Node content) {
        if (content != null) {
            contentBox.getChildren().add(content);
        }
    }

    /**
     * Adds an action button to the panel.
     *
     * @param action The action button to add
     */
    public void addAction(Button action) {
        if (action != null) {
            actionsBox.getChildren().add(action);
        }
    }

    /**
     * Removes an action button from the panel.
     *
     * @param action The action button to remove
     */
    public void removeAction(Button action) {
        actionsBox.getChildren().remove(action);
    }

    /**
     * Clears all action buttons from the panel.
     */
    public void clearActions() {
        actionsBox.getChildren().clear();
    }

    /**
     * Gets the actions box.
     *
     * @return The actions box
     */
    public HBox getActionsBox() {
        return actionsBox;
    }
}