package io.github.jspinak.brobot.runner.ui.components.base;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;

/**
 * Extended Card component that provides consistent styling and structure for all card-based UI
 * elements in Brobot.
 */
public class BrobotCard extends Card {

    private final VBox contentContainer;
    private HBox headerBox;
    private Label titleLabel;
    private Node headerRightContent;

    public BrobotCard() {
        this(null);
    }

    public BrobotCard(String title) {
        super();
        getStyleClass().addAll("brobot-card", Styles.ELEVATED_1);

        contentContainer = new VBox(16);
        contentContainer.setPadding(new Insets(16));

        if (title != null) {
            createHeader(title);
        }

        setBody(contentContainer);
    }

    /** Create a header with title and optional right-side content. */
    private void createHeader(String title) {
        headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(8, 12, 8, 12));
        headerBox.getStyleClass().add("card-header");

        titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll(Styles.TITLE_4, "card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(titleLabel, spacer);
        setHeader(headerBox);
    }

    /** Set the card title. */
    public void setTitle(String title) {
        if (titleLabel == null && title != null) {
            createHeader(title);
        } else if (titleLabel != null) {
            titleLabel.setText(title);
        }
    }

    /** Add content to the right side of the header. */
    public void setHeaderRightContent(Node content) {
        if (headerBox == null) {
            createHeader("");
        }

        if (headerRightContent != null) {
            headerBox.getChildren().remove(headerRightContent);
        }

        headerRightContent = content;
        if (content != null) {
            headerBox.getChildren().add(content);
        }
    }

    /** Add content to the card body. */
    public void addContent(Node... nodes) {
        contentContainer.getChildren().addAll(nodes);
    }

    /** Clear all content from the card body. */
    public void clearContent() {
        contentContainer.getChildren().clear();
    }

    /** Apply additional style classes to the card. */
    public void applyStyle(String... styleClasses) {
        getStyleClass().addAll(styleClasses);
    }

    /** Create a card with consistent spacing and padding. */
    public static BrobotCard create(String title, Node... content) {
        BrobotCard card = new BrobotCard(title);
        card.addContent(content);
        return card;
    }
}
