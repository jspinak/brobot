package io.github.jspinak.brobot.runner.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import lombok.Setter;

/**
 * A breadcrumb navigation component showing the path to the current screen. Allows navigation back
 * to previous screens in the path.
 */
public class BreadcrumbBar extends HBox {

    private final ObservableList<BreadcrumbItem> items = FXCollections.observableArrayList();
    private final ObjectProperty<BreadcrumbItem> activeItem = new SimpleObjectProperty<>();

    /** Creates a new BreadcrumbBar. */
    public BreadcrumbBar() {
        initialize();
    }

    /** Initialize the breadcrumb bar. */
    private void initialize() {
        getStyleClass().add("breadcrumb-bar");
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(8, 12, 8, 12));
        setSpacing(5);

        // Listen for changes to the items list
        items.addListener(
                (ListChangeListener<BreadcrumbItem>)
                        change -> {
                            refreshBreadcrumbs();
                        });

        // Listen for changes to the active item
        activeItem.addListener(
                (obs, oldItem, newItem) -> {
                    refreshBreadcrumbs();
                });
    }

    /** Refreshes the breadcrumb display. */
    private void refreshBreadcrumbs() {
        getChildren().clear();

        // Add all items
        for (int i = 0; i < items.size(); i++) {
            BreadcrumbItem item = items.get(i);

            // Create label for the item
            Label itemLabel = new Label(item.getText());
            itemLabel.getStyleClass().add("breadcrumb-item");

            // Check if this is the active item
            boolean isActive = item.equals(activeItem.get());
            if (isActive) {
                itemLabel.getStyleClass().add("active");
            } else {
                // Add click handler for non-active items
                final int index = i;
                itemLabel.setOnMouseClicked(e -> handleItemClick(index));
            }

            // Add the item to the breadcrumb
            getChildren().add(itemLabel);

            // Add separator if not the last item
            if (i < items.size() - 1) {
                Text separator = new Text("›");
                separator.getStyleClass().add("breadcrumb-separator");
                getChildren().add(separator);
            }
        }

        // Add home button if needed (can be customized)
        if (items.isEmpty()) {
            Button homeButton = new Button("Home");
            homeButton.getStyleClass().addAll("breadcrumb-home", "breadcrumb-item", "active");
            getChildren().add(homeButton);
        }
    }

    /**
     * Handles clicking on a breadcrumb item.
     *
     * @param index The index of the clicked item
     */
    private void handleItemClick(int index) {
        if (index >= 0 && index < items.size()) {
            BreadcrumbItem item = items.get(index);

            // Execute the on-click handler if provided
            if (item.getOnClick() != null) {
                item.getOnClick().accept(item);
            }

            // Remove all items after the clicked item
            while (items.size() > index + 1) {
                items.removeLast();
            }

            // Set the clicked item as active
            setActiveItem(item);
        }
    }

    /**
     * Sets the active breadcrumb item.
     *
     * @param item The item to set as active
     */
    public void setActiveItem(BreadcrumbItem item) {
        if (items.contains(item)) {
            activeItem.set(item);
        }
    }

    /**
     * Sets the active breadcrumb item by index.
     *
     * @param index The index of the item to set as active
     */
    public void setActiveItem(int index) {
        if (index >= 0 && index < items.size()) {
            activeItem.set(items.get(index));
        }
    }

    /**
     * Gets the currently active breadcrumb item.
     *
     * @return The active item
     */
    public BreadcrumbItem getActiveItem() {
        return activeItem.get();
    }

    /**
     * Gets the active item property.
     *
     * @return The active item property
     */
    public ObjectProperty<BreadcrumbItem> activeItemProperty() {
        return activeItem;
    }

    /**
     * Adds a breadcrumb item to the end of the path.
     *
     * @param text The text to display for the item
     * @param onClick The action to perform when the item is clicked
     * @return The added item
     */
    public BreadcrumbItem addItem(String text, Consumer<BreadcrumbItem> onClick) {
        BreadcrumbItem item = new BreadcrumbItem(text, onClick);
        items.add(item);
        setActiveItem(item);
        return item;
    }

    /**
     * Adds a breadcrumb item to the end of the path.
     *
     * @param item The item to add
     */
    public void addItem(BreadcrumbItem item) {
        items.add(item);
        setActiveItem(item);
    }

    /**
     * Removes a breadcrumb item.
     *
     * @param item The item to remove
     */
    public void removeItem(BreadcrumbItem item) {
        items.remove(item);

        // Update active item if needed
        if (activeItem.get() == item) {
            if (!items.isEmpty()) {
                activeItem.set(items.getLast());
            } else {
                activeItem.set(null);
            }
        }
    }

    /** Removes all breadcrumb items. */
    public void clearItems() {
        items.clear();
        activeItem.set(null);
    }

    /**
     * Gets all breadcrumb items.
     *
     * @return The list of items
     */
    public List<BreadcrumbItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Gets the observable list of breadcrumb items.
     *
     * @return The observable list of items
     */
    public ObservableList<BreadcrumbItem> itemsProperty() {
        return items;
    }

    /**
     * Sets the path by replacing all existing items.
     *
     * @param items The new items to set
     */
    public void setPath(List<BreadcrumbItem> items) {
        this.items.clear();
        this.items.addAll(items);

        if (!items.isEmpty()) {
            setActiveItem(items.get(items.size() - 1));
        } else {
            activeItem.set(null);
        }
    }

    /** Represents a single item in the breadcrumb path. */
    public static class BreadcrumbItem {
        private final String text;
        private final Consumer<BreadcrumbItem> onClick;

        /** The data associated with the item. */
        @Setter private Object data;

        /**
         * Creates a new BreadcrumbItem.
         *
         * @param text The text to display
         * @param onClick The action to perform when clicked
         */
        public BreadcrumbItem(String text, Consumer<BreadcrumbItem> onClick) {
            this.text = text;
            this.onClick = onClick;
        }

        /**
         * Creates a new BreadcrumbItem.
         *
         * @param text The text to display
         * @param onClick The action to perform when clicked
         * @param data Additional data associated with the item
         */
        public BreadcrumbItem(String text, Consumer<BreadcrumbItem> onClick, Object data) {
            this.text = text;
            this.onClick = onClick;
            this.data = data;
        }

        /**
         * Gets the text for the item.
         *
         * @return The item text
         */
        public String getText() {
            return text;
        }

        /**
         * Gets the click handler for the item.
         *
         * @return The click handler
         */
        public Consumer<BreadcrumbItem> getOnClick() {
            return onClick;
        }

        /**
         * Gets the data associated with the item.
         *
         * @return The item data
         */
        public Object getData() {
            return data;
        }
    }
}
