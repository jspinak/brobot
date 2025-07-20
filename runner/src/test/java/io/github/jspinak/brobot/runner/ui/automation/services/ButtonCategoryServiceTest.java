package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.project.TaskButton;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ButtonCategoryServiceTest {
    
    private ButtonCategoryService service;
    
    @BeforeEach
    void setUp() {
        service = new ButtonCategoryService();
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testUpdateCategoriesWithEmptyList() {
        // When
        ButtonCategoryService.CategoryUpdate update = service.updateCategories(Collections.emptyList());
        
        // Then
        assertFalse(update.hasChanges());
        assertTrue(update.getAddedNodes().isEmpty());
        assertTrue(update.getRemovedNodes().isEmpty());
        assertTrue(update.getUpdatedNodes().isEmpty());
    }
    
    @Test
    void testUpdateCategoriesWithNewButtons() {
        // Given
        TaskButton button1 = createTaskButton("1", "Button 1", "Category A");
        TaskButton button2 = createTaskButton("2", "Button 2", "Category A");
        TaskButton button3 = createTaskButton("3", "Button 3", "Category B");
        
        List<TaskButton> buttons = Arrays.asList(button1, button2, button3);
        
        // When
        ButtonCategoryService.CategoryUpdate update = service.updateCategories(buttons);
        
        // Then
        assertTrue(update.hasChanges());
        assertEquals(2, update.getAddedNodes().size()); // Two categories
        assertTrue(update.getRemovedNodes().isEmpty());
        assertTrue(update.getUpdatedNodes().isEmpty());
        assertEquals(2, update.getOrderedCategories().size());
        
        // Verify categories were created
        Node categoryA = service.getRenderedCategory("Category A");
        Node categoryB = service.getRenderedCategory("Category B");
        assertNotNull(categoryA);
        assertNotNull(categoryB);
        
        // Verify categories are VBoxes with correct children count
        assertTrue(categoryA instanceof VBox);
        assertTrue(categoryB instanceof VBox);
        VBox vboxA = (VBox) categoryA;
        VBox vboxB = (VBox) categoryB;
        assertEquals(3, vboxA.getChildren().size()); // Label + 2 buttons
        assertEquals(2, vboxB.getChildren().size()); // Label + 1 button
    }
    
    @Test
    void testUpdateCategoriesWithDefaultCategory() {
        // Given
        TaskButton buttonNoCategory = createTaskButton("1", "Button 1", null);
        
        // When
        ButtonCategoryService.CategoryUpdate update = service.updateCategories(Arrays.asList(buttonNoCategory));
        
        // Then
        assertTrue(update.hasChanges());
        assertEquals(1, update.getAddedNodes().size());
        assertEquals("General", update.getOrderedCategories().get(0)); // Default category
    }
    
    @Test
    void testUpdateCategoriesWithChanges() {
        // Given - Initial buttons
        TaskButton button1 = createTaskButton("1", "Button 1", "Category A");
        TaskButton button2 = createTaskButton("2", "Button 2", "Category B");
        service.updateCategories(Arrays.asList(button1, button2));
        
        // When - Update with different buttons
        TaskButton button3 = createTaskButton("3", "Button 3", "Category A"); // Changed button in A
        TaskButton button4 = createTaskButton("4", "Button 4", "Category C"); // New category
        ButtonCategoryService.CategoryUpdate update = service.updateCategories(Arrays.asList(button3, button4));
        
        // Then
        assertTrue(update.hasChanges());
        assertEquals(1, update.getAddedNodes().size()); // Category C added
        assertEquals(1, update.getRemovedNodes().size()); // Category B removed
        assertEquals(1, update.getUpdatedNodes().size()); // Category A updated
    }
    
    @Test
    void testButtonActionHandler() {
        // Given
        AtomicReference<TaskButton> clickedButton = new AtomicReference<>();
        Consumer<TaskButton> handler = clickedButton::set;
        service.setButtonActionHandler(handler);
        
        TaskButton button = createTaskButton("1", "Test Button", "Test");
        
        // When
        service.updateCategories(Arrays.asList(button));
        
        // Then - Find the created button and simulate click
        Node categoryNode = service.getRenderedCategory("Test");
        assertNotNull(categoryNode);
        VBox vbox = (VBox) categoryNode;
        
        // Find the button (skip the label)
        Button uiButton = null;
        for (Node child : vbox.getChildren()) {
            if (child instanceof Button) {
                uiButton = (Button) child;
                break;
            }
        }
        
        assertNotNull(uiButton);
        uiButton.fire(); // Simulate button click
        
        assertEquals(button, clickedButton.get());
    }
    
    @Test
    void testClearAllCategories() {
        // Given
        TaskButton button1 = createTaskButton("1", "Button 1", "Category A");
        TaskButton button2 = createTaskButton("2", "Button 2", "Category B");
        service.updateCategories(Arrays.asList(button1, button2));
        
        // Verify categories exist
        assertNotNull(service.getRenderedCategory("Category A"));
        assertNotNull(service.getRenderedCategory("Category B"));
        
        // When
        ButtonCategoryService.CategoryUpdate update = service.clearAllCategories();
        
        // Then
        assertTrue(update.hasChanges());
        assertEquals(2, update.getRemovedNodes().size());
        assertTrue(update.getAddedNodes().isEmpty());
        assertNull(service.getRenderedCategory("Category A"));
        assertNull(service.getRenderedCategory("Category B"));
    }
    
    @Test
    void testConfigurationSettings() {
        // Given
        ButtonCategoryService.CategoryConfiguration config = 
            ButtonCategoryService.CategoryConfiguration.builder()
                .defaultCategory("Custom Default")
                .sortCategories(false)
                .sortButtonsInCategory(false)
                .categoryStyleClass("custom-category")
                .categoryLabelStyleClass("custom-label")
                .buttonStyleClass("custom-button")
                .build();
        
        service.setConfiguration(config);
        
        // When - Add button with no category
        TaskButton button = createTaskButton("1", "Button", null);
        ButtonCategoryService.CategoryUpdate update = service.updateCategories(Arrays.asList(button));
        
        // Then
        assertEquals("Custom Default", update.getOrderedCategories().get(0));
        Node categoryNode = service.getRenderedCategory("Custom Default");
        assertNotNull(categoryNode);
        
        VBox vbox = (VBox) categoryNode;
        assertTrue(vbox.getStyleClass().contains("custom-category"));
    }
    
    @Test
    void testButtonStyling() {
        // Given
        TaskButton.ButtonStyling styling = new TaskButton.ButtonStyling();
        styling.setBackgroundColor("#FF0000");
        styling.setTextColor("#FFFFFF");
        styling.setSize("large");
        styling.setCustomClass("special-button");
        
        TaskButton button = createTaskButton("1", "Styled Button", "Test");
        button.setStyling(styling);
        
        // When
        service.updateCategories(Arrays.asList(button));
        
        // Then
        Node categoryNode = service.getRenderedCategory("Test");
        VBox vbox = (VBox) categoryNode;
        
        Button uiButton = null;
        for (Node child : vbox.getChildren()) {
            if (child instanceof Button) {
                uiButton = (Button) child;
                break;
            }
        }
        
        assertNotNull(uiButton);
        assertTrue(uiButton.getStyle().contains("-fx-background-color: #FF0000"));
        assertTrue(uiButton.getStyle().contains("-fx-text-fill: #FFFFFF"));
        assertTrue(uiButton.getStyle().contains("-fx-font-size: 14px"));
        assertTrue(uiButton.getStyleClass().contains("special-button"));
    }
    
    @Test
    void testGetAllRenderedCategories() {
        // Given
        TaskButton button1 = createTaskButton("1", "Button 1", "B Category");
        TaskButton button2 = createTaskButton("2", "Button 2", "A Category");
        TaskButton button3 = createTaskButton("3", "Button 3", "C Category");
        
        // When
        service.updateCategories(Arrays.asList(button1, button2, button3));
        List<Node> categories = service.getAllRenderedCategories();
        
        // Then
        assertEquals(3, categories.size());
        
        // Should be sorted alphabetically by default
        VBox firstCategory = (VBox) categories.get(0);
        assertEquals("category_" + "A Category".hashCode(), firstCategory.getId());
    }
    
    @Test
    void testNoChangesWhenButtonsUnchanged() {
        // Given
        TaskButton button1 = createTaskButton("1", "Button 1", "Category A");
        TaskButton button2 = createTaskButton("2", "Button 2", "Category A");
        service.updateCategories(Arrays.asList(button1, button2));
        
        // When - Update with same buttons
        ButtonCategoryService.CategoryUpdate update = service.updateCategories(Arrays.asList(button1, button2));
        
        // Then
        assertFalse(update.hasChanges());
        assertTrue(update.getAddedNodes().isEmpty());
        assertTrue(update.getRemovedNodes().isEmpty());
        assertTrue(update.getUpdatedNodes().isEmpty());
    }
    
    @Test
    void testButtonTooltip() {
        // Given
        TaskButton button = createTaskButton("1", "Button with Tooltip", "Test");
        button.setTooltip("This is a helpful tooltip");
        
        // When
        service.updateCategories(Arrays.asList(button));
        
        // Then
        Node categoryNode = service.getRenderedCategory("Test");
        VBox vbox = (VBox) categoryNode;
        
        Button uiButton = null;
        for (Node child : vbox.getChildren()) {
            if (child instanceof Button) {
                uiButton = (Button) child;
                break;
            }
        }
        
        assertNotNull(uiButton);
        assertNotNull(uiButton.getTooltip());
        assertEquals("This is a helpful tooltip", uiButton.getTooltip().getText());
    }
    
    private TaskButton createTaskButton(String id, String label, String category) {
        TaskButton button = new TaskButton();
        button.setId(id);
        button.setLabel(label);
        button.setCategory(category);
        return button;
    }
}