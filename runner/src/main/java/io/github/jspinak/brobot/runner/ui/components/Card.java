package io.github.jspinak.brobot.runner.ui.components;

import lombok.Data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A card component for displaying content with a title and optional actions.
 * Useful for dashboard items, list items, etc.
 */
@Data
public class Card extends VBox {

    private final ObjectProperty<Node> header = new SimpleObjectProperty<>();
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>();
    private final ObjectProperty<Node> footer = new SimpleObjectProperty<>();
    private final BooleanProperty elevated = new SimpleBooleanProperty(false);

    private final VBox headerBox;
    private final VBox contentBox;
    private final HBox footerBox;

    /**
     * Creates a new Card.
     */
    public Card() {
        // Set CSS class for styling
        getStyleClass().add("card");

        // Create layout boxes
        headerBox = new VBox();
        headerBox.getStyleClass().add("card-header");

        contentBox = new VBox();
        contentBox.getStyleClass().add("card-content");
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        footerBox = new HBox();
        footerBox.getStyleClass().add("card-footer");
        footerBox.setAlignment(Pos.CENTER_RIGHT);

        // Add to parent
        getChildren().addAll(headerBox, contentBox, footerBox);

        // Set default padding
        setPadding(new Insets(10));
        setSpacing(10);

        // Setup bindings
        setupBindings();
    }

    /**
     * Creates a new Card with the specified title.
     *
     * @param title The card title
     */
    public Card(String title) {
        this();
        setTitle(title);
    }

    /**
     * Creates a new Card with the specified title and content.
     *
     * @param title The card title
     * @param content The card content
     */
    public Card(String title, Node content) {
        this(title);
        setContent(content);
    }

    /**
     * Sets up property bindings.
     */
    private void setupBindings() {
        // Update header when header property changes
        header.addListener((obs, oldVal, newVal) -> {
            headerBox.getChildren().clear();
            if (newVal != null) {
                headerBox.getChildren().add(newVal);
                headerBox.setVisible(true);
                headerBox.setManaged(true);
            } else {
                headerBox.setVisible(false);
                headerBox.setManaged(false);
            }
        });

        // Update content when content property changes
        content.addListener((obs, oldVal, newVal) -> {
            contentBox.getChildren().clear();
            if (newVal != null) {
                contentBox.getChildren().add(newVal);
                contentBox.setVisible(true);
                contentBox.setManaged(true);
            } else {
                contentBox.setVisible(false);
                contentBox.setManaged(false);
            }
        });

        // Update footer when footer property changes
        footer.addListener((obs, oldVal, newVal) -> {
            footerBox.getChildren().clear();
            if (newVal != null) {
                // Don't add the footerBox to itself - this causes the cycle
                if (newVal != footerBox) {
                    footerBox.getChildren().add(newVal);
                    footerBox.setVisible(true);
                    footerBox.setManaged(true);
                } else {
                    // Just make the footerBox visible and managed, no need to add it to itself
                    footerBox.setVisible(true);
                    footerBox.setManaged(true);
                }
            } else {
                footerBox.setVisible(false);
                footerBox.setManaged(false);
            }
        });

        // Update elevation style when elevated property changes
        elevated.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                getStyleClass().add("card-elevated");
            } else {
                getStyleClass().remove("card-elevated");
            }
        });
    }

    /**
     * Sets the card title.
     *
     * @param title The title to set
     */
    public void setTitle(String title) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        setHeader(titleLabel);
    }

    /**
     * Sets the card header.
     *
     * @param header The header to set
     */
    public void setHeader(Node header) {
        this.header.set(header);
    }

    /**
     * Gets the card header.
     *
     * @return The card header
     */
    public Node getHeader() {
        return header.get();
    }

    /**
     * Gets the header property.
     *
     * @return The header property
     */
    public ObjectProperty<Node> headerProperty() {
        return header;
    }

    /**
     * Sets the card content.
     *
     * @param content The content to set
     */
    public void setContent(Node content) {
        this.content.set(content);
    }

    /**
     * Gets the card content.
     *
     * @return The card content
     */
    public Node getContent() {
        return content.get();
    }

    /**
     * Gets the content property.
     *
     * @return The content property
     */
    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    /**
     * Sets the card footer.
     *
     * @param footer The footer to set
     */
    public void setFooter(Node footer) {
        this.footer.set(footer);
    }

    /**
     * Gets the card footer.
     *
     * @return The card footer
     */
    public Node getFooter() {
        return footer.get();
    }

    /**
     * Gets the footer property.
     *
     * @return The footer property
     */
    public ObjectProperty<Node> footerProperty() {
        return footer;
    }

    /**
     * Sets whether the card is elevated.
     *
     * @param elevated true if the card should be elevated, false otherwise
     */
    public void setElevated(boolean elevated) {
        this.elevated.set(elevated);
    }

    /**
     * Gets whether the card is elevated.
     *
     * @return true if the card is elevated, false otherwise
     */
    public boolean isElevated() {
        return elevated.get();
    }

    /**
     * Gets the elevated property.
     *
     * @return The elevated property
     */
    public BooleanProperty elevatedProperty() {
        return elevated;
    }

    /**
     * Adds an action button to the card footer.
     *
     * @param action The action button to add
     */
    public void addAction(Button action) {
        if (action != null) {
            // Always add the action directly to the footerBox
            footerBox.getChildren().add(action);

            // Only set the footer property if it's not already set to footerBox
            if (footer.get() != footerBox) {
                footer.set(footerBox);
            }

            // Make sure the footer is visible
            footerBox.setVisible(true);
            footerBox.setManaged(true);
        }
    }

    /**
     * Removes an action button from the card footer.
     *
     * @param action The action button to remove
     */
    public void removeAction(Button action) {
        if (footer.get() instanceof HBox) {
            ((HBox) footer.get()).getChildren().remove(action);
        }
    }

    /**
     * Clears all action buttons from the card footer.
     */
    public void clearActions() {
        if (footer.get() instanceof HBox) {
            ((HBox) footer.get()).getChildren().clear();
        }
    }
}