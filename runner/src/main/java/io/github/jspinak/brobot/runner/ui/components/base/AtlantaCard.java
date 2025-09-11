package io.github.jspinak.brobot.runner.ui.components.base;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import lombok.Getter;
import lombok.Setter;

/**
 * A modern card component styled according to AtlantaFX design principles. Features: - Rounded
 * corners with consistent border radius - Subtle shadows that enhance on hover - Proper spacing and
 * padding - Optional header with title - Action buttons support
 */
public class AtlantaCard extends VBox {

    private final StringProperty title = new SimpleStringProperty();

    @Getter private final HBox header;

    @Getter private final Label titleLabel;

    @Getter private final HBox actionBox;

    @Getter @Setter private Node content;

    @Getter private final VBox contentContainer;

    public AtlantaCard() {
        this(null);
    }

    public AtlantaCard(String title) {
        super();
        getStyleClass().addAll("card", "brobot-card");

        // Create header
        header = new HBox();
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        titleLabel = new Label();
        titleLabel.textProperty().bind(this.title);
        titleLabel.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(titleLabel, spacer, actionBox);

        // Create content container
        contentContainer = new VBox();
        contentContainer.getStyleClass().add("card-body");

        // Only add header if title is set
        if (title != null && !title.isEmpty()) {
            setTitle(title);
            getChildren().add(header);
        }

        getChildren().add(contentContainer);

        // Bind visibility of header to title property
        header.managedProperty().bind(header.visibleProperty());
        header.visibleProperty().bind(this.title.isNotEmpty());
    }

    /**
     * Sets the card title.
     *
     * @param title The title text
     */
    public void setTitle(String title) {
        this.title.set(title);
    }

    /**
     * Gets the card title.
     *
     * @return The title text
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
     * Sets the main content of the card.
     *
     * @param content The content node
     */
    public void setContent(Node content) {
        this.content = content;
        contentContainer.getChildren().clear();
        if (content != null) {
            contentContainer.getChildren().add(content);
        }
    }

    /**
     * Adds an action button to the header.
     *
     * @param button The button to add
     */
    public void addAction(Button button) {
        if (button != null) {
            button.getStyleClass().add("card-action");
            actionBox.getChildren().add(button);
        }
    }

    /**
     * Removes an action button from the header.
     *
     * @param button The button to remove
     */
    public void removeAction(Button button) {
        actionBox.getChildren().remove(button);
    }

    /** Clears all action buttons. */
    public void clearActions() {
        actionBox.getChildren().clear();
    }

    /**
     * Creates a styled card with the given title and content.
     *
     * @param title The card title
     * @param content The card content
     * @return The configured card
     */
    public static AtlantaCard create(String title, Node content) {
        AtlantaCard card = new AtlantaCard(title);
        card.setContent(content);
        return card;
    }

    /**
     * Creates a card suitable for forms with proper spacing.
     *
     * @param title The card title
     * @return The configured card
     */
    public static AtlantaCard createFormCard(String title) {
        AtlantaCard card = new AtlantaCard(title);
        card.contentContainer.setSpacing(16);
        return card;
    }

    /** Adds a CSS class to style the card as a primary/highlighted card. */
    public void setPrimary(boolean primary) {
        if (primary) {
            getStyleClass().add("card-primary");
        } else {
            getStyleClass().remove("card-primary");
        }
    }

    /**
     * Sets whether the card should expand to fill available space.
     *
     * @param expand True to expand, false otherwise
     */
    public void setExpand(boolean expand) {
        if (expand) {
            VBox.setVgrow(this, Priority.ALWAYS);
            VBox.setVgrow(contentContainer, Priority.ALWAYS);
        } else {
            VBox.setVgrow(this, Priority.NEVER);
            VBox.setVgrow(contentContainer, Priority.NEVER);
        }
    }
}
