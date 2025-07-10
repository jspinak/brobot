package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.project.TaskButton;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Service for managing task button categories and preventing duplicate rendering.
 * Maintains state of rendered categories and buttons for efficient updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ButtonCategoryService {
    
    // State tracking
    private final Map<String, Node> renderedCategories = new ConcurrentHashMap<>();
    private final Map<String, Button> renderedButtons = new ConcurrentHashMap<>();
    private final Map<String, List<TaskButton>> categoryCache = new ConcurrentHashMap<>();
    
    // Configuration
    private CategoryConfiguration configuration = CategoryConfiguration.builder().build();
    
    // Action handler
    private Consumer<TaskButton> buttonActionHandler;
    
    /**
     * Category rendering configuration.
     */
    public static class CategoryConfiguration {
        private String defaultCategory = "General";
        private boolean sortCategories = true;
        private boolean sortButtonsInCategory = true;
        private String categoryStyleClass = "category-box";
        private String categoryLabelStyleClass = "category-label";
        private String buttonStyleClass = "automation-button";
        
        public static CategoryConfigurationBuilder builder() {
            return new CategoryConfigurationBuilder();
        }
        
        public static class CategoryConfigurationBuilder {
            private CategoryConfiguration config = new CategoryConfiguration();
            
            public CategoryConfigurationBuilder defaultCategory(String category) {
                config.defaultCategory = category;
                return this;
            }
            
            public CategoryConfigurationBuilder sortCategories(boolean sort) {
                config.sortCategories = sort;
                return this;
            }
            
            public CategoryConfigurationBuilder sortButtonsInCategory(boolean sort) {
                config.sortButtonsInCategory = sort;
                return this;
            }
            
            public CategoryConfigurationBuilder categoryStyleClass(String styleClass) {
                config.categoryStyleClass = styleClass;
                return this;
            }
            
            public CategoryConfigurationBuilder categoryLabelStyleClass(String styleClass) {
                config.categoryLabelStyleClass = styleClass;
                return this;
            }
            
            public CategoryConfigurationBuilder buttonStyleClass(String styleClass) {
                config.buttonStyleClass = styleClass;
                return this;
            }
            
            public CategoryConfiguration build() {
                return config;
            }
        }
    }
    
    /**
     * Sets the configuration.
     */
    public void setConfiguration(CategoryConfiguration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Sets the button action handler.
     */
    public void setButtonActionHandler(Consumer<TaskButton> handler) {
        this.buttonActionHandler = handler;
    }
    
    /**
     * Updates categories with new button definitions.
     * Returns a CategoryUpdate object describing what changed.
     */
    public CategoryUpdate updateCategories(List<TaskButton> buttons) {
        if (buttons == null || buttons.isEmpty()) {
            return clearAllCategories();
        }
        
        // Group buttons by category
        Map<String, List<TaskButton>> newCategories = groupByCategory(buttons);
        
        // Determine changes
        Set<String> currentCategories = new HashSet<>(renderedCategories.keySet());
        Set<String> newCategoryNames = new HashSet<>(newCategories.keySet());
        
        Set<String> toRemove = new HashSet<>(currentCategories);
        toRemove.removeAll(newCategoryNames);
        
        Set<String> toAdd = new HashSet<>(newCategoryNames);
        toAdd.removeAll(currentCategories);
        
        Set<String> toUpdate = new HashSet<>(currentCategories);
        toUpdate.retainAll(newCategoryNames);
        
        // Process removals
        List<Node> removedNodes = new ArrayList<>();
        for (String category : toRemove) {
            Node node = renderedCategories.remove(category);
            if (node != null) {
                removedNodes.add(node);
                removeButtonsForCategory(category);
            }
        }
        
        // Process additions
        List<Node> addedNodes = new ArrayList<>();
        for (String category : toAdd) {
            VBox categoryBox = createCategoryBox(category, newCategories.get(category));
            renderedCategories.put(category, categoryBox);
            addedNodes.add(categoryBox);
        }
        
        // Process updates
        List<Node> updatedNodes = new ArrayList<>();
        for (String category : toUpdate) {
            if (hasCategoryChanged(category, newCategories.get(category))) {
                VBox categoryBox = updateCategoryBox(category, newCategories.get(category));
                updatedNodes.add(categoryBox);
            }
        }
        
        // Update cache
        categoryCache.clear();
        categoryCache.putAll(newCategories);
        
        return new CategoryUpdate(removedNodes, addedNodes, updatedNodes, getSortedCategories());
    }
    
    /**
     * Clears all categories.
     */
    public CategoryUpdate clearAllCategories() {
        List<Node> removed = new ArrayList<>(renderedCategories.values());
        renderedCategories.clear();
        renderedButtons.clear();
        categoryCache.clear();
        
        return new CategoryUpdate(removed, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }
    
    /**
     * Groups buttons by category.
     */
    private Map<String, List<TaskButton>> groupByCategory(List<TaskButton> buttons) {
        Map<String, List<TaskButton>> grouped = new HashMap<>();
        
        for (TaskButton button : buttons) {
            String category = button.getCategory() != null ? button.getCategory() : configuration.defaultCategory;
            grouped.computeIfAbsent(category, k -> new ArrayList<>()).add(button);
        }
        
        // Sort buttons within categories if configured
        if (configuration.sortButtonsInCategory) {
            grouped.values().forEach(list -> 
                list.sort(Comparator.comparing(TaskButton::getLabel, String.CASE_INSENSITIVE_ORDER))
            );
        }
        
        return grouped;
    }
    
    /**
     * Creates a category box.
     */
    private VBox createCategoryBox(String category, List<TaskButton> buttons) {
        VBox categoryBox = new VBox(5);
        categoryBox.setId("category_" + category.hashCode());
        categoryBox.setPadding(new Insets(5));
        categoryBox.getStyleClass().add(configuration.categoryStyleClass);
        categoryBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");
        
        // Category label
        Label categoryLabel = new Label(category);
        categoryLabel.setId("label_" + category.hashCode());
        categoryLabel.getStyleClass().add(configuration.categoryLabelStyleClass);
        categoryLabel.setStyle("-fx-font-weight: bold;");
        categoryBox.getChildren().add(categoryLabel);
        
        // Add buttons
        for (TaskButton buttonDef : buttons) {
            Button uiButton = createButton(buttonDef);
            String buttonKey = generateButtonKey(category, buttonDef);
            renderedButtons.put(buttonKey, uiButton);
            categoryBox.getChildren().add(uiButton);
        }
        
        return categoryBox;
    }
    
    /**
     * Updates an existing category box.
     */
    private VBox updateCategoryBox(String category, List<TaskButton> buttons) {
        // For simplicity, recreate the category box
        // In a more sophisticated implementation, we could diff the buttons
        removeButtonsForCategory(category);
        return createCategoryBox(category, buttons);
    }
    
    /**
     * Creates a button from task definition.
     */
    private Button createButton(TaskButton buttonDef) {
        Button uiButton = new Button(buttonDef.getLabel());
        uiButton.setId("button_" + buttonDef.getId());
        uiButton.getStyleClass().add(configuration.buttonStyleClass);
        
        // Apply custom styling
        applyButtonStyling(uiButton, buttonDef.getStyling());
        
        // Set tooltip
        if (buttonDef.getTooltip() != null && !buttonDef.getTooltip().isEmpty()) {
            uiButton.setTooltip(new Tooltip(buttonDef.getTooltip()));
        }
        
        // Set action
        if (buttonActionHandler != null) {
            uiButton.setOnAction(e -> buttonActionHandler.accept(buttonDef));
        }
        
        return uiButton;
    }
    
    /**
     * Applies custom styling to a button.
     */
    private void applyButtonStyling(Button button, TaskButton.ButtonStyling styling) {
        if (styling == null) {
            return;
        }
        
        StringBuilder style = new StringBuilder();
        
        if (styling.getBackgroundColor() != null && !styling.getBackgroundColor().isEmpty()) {
            style.append("-fx-background-color: ").append(styling.getBackgroundColor()).append("; ");
        }
        
        if (styling.getTextColor() != null && !styling.getTextColor().isEmpty()) {
            style.append("-fx-text-fill: ").append(styling.getTextColor()).append("; ");
        }
        
        if (styling.getSize() != null && !styling.getSize().isEmpty()) {
            switch (styling.getSize().toLowerCase()) {
                case "small":
                    style.append("-fx-font-size: 10px; ");
                    break;
                case "large":
                    style.append("-fx-font-size: 14px; ");
                    break;
                default:
                    style.append("-fx-font-size: 12px; ");
            }
        }
        
        if (style.length() > 0) {
            button.setStyle(style.toString());
        }
        
        if (styling.getCustomClass() != null && !styling.getCustomClass().isEmpty()) {
            button.getStyleClass().add(styling.getCustomClass());
        }
    }
    
    /**
     * Checks if a category has changed.
     */
    private boolean hasCategoryChanged(String category, List<TaskButton> newButtons) {
        List<TaskButton> cachedButtons = categoryCache.get(category);
        if (cachedButtons == null || cachedButtons.size() != newButtons.size()) {
            return true;
        }
        
        // Simple comparison - could be more sophisticated
        Set<String> cachedIds = new HashSet<>();
        cachedButtons.forEach(b -> cachedIds.add(b.getId()));
        
        for (TaskButton newButton : newButtons) {
            if (!cachedIds.contains(newButton.getId())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Removes buttons for a category.
     */
    private void removeButtonsForCategory(String category) {
        Iterator<Map.Entry<String, Button>> iter = renderedButtons.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Button> entry = iter.next();
            if (entry.getKey().startsWith(category + "_")) {
                iter.remove();
            }
        }
    }
    
    /**
     * Generates a unique key for a button.
     */
    private String generateButtonKey(String category, TaskButton button) {
        return category + "_" + button.getId();
    }
    
    /**
     * Gets sorted category names.
     */
    private List<String> getSortedCategories() {
        List<String> categories = new ArrayList<>(renderedCategories.keySet());
        if (configuration.sortCategories) {
            categories.sort(String.CASE_INSENSITIVE_ORDER);
        }
        return categories;
    }
    
    /**
     * Gets the rendered node for a category.
     */
    public Node getRenderedCategory(String category) {
        return renderedCategories.get(category);
    }
    
    /**
     * Gets all rendered categories in order.
     */
    public List<Node> getAllRenderedCategories() {
        if (configuration.sortCategories) {
            return getSortedCategories().stream()
                .map(renderedCategories::get)
                .filter(Objects::nonNull)
                .collect(ArrayList::new, (list, node) -> list.add(node), ArrayList::addAll);
        } else {
            return new ArrayList<>(renderedCategories.values());
        }
    }
    
    /**
     * Result of category update operation.
     */
    public static class CategoryUpdate {
        private final List<Node> removedNodes;
        private final List<Node> addedNodes;
        private final List<Node> updatedNodes;
        private final List<String> orderedCategories;
        
        public CategoryUpdate(List<Node> removedNodes, List<Node> addedNodes, 
                            List<Node> updatedNodes, List<String> orderedCategories) {
            this.removedNodes = removedNodes;
            this.addedNodes = addedNodes;
            this.updatedNodes = updatedNodes;
            this.orderedCategories = orderedCategories;
        }
        
        public List<Node> getRemovedNodes() { return removedNodes; }
        public List<Node> getAddedNodes() { return addedNodes; }
        public List<Node> getUpdatedNodes() { return updatedNodes; }
        public List<String> getOrderedCategories() { return orderedCategories; }
        
        public boolean hasChanges() {
            return !removedNodes.isEmpty() || !addedNodes.isEmpty() || !updatedNodes.isEmpty();
        }
    }
}