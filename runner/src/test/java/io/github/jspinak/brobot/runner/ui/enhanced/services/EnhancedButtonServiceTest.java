package io.github.jspinak.brobot.runner.ui.enhanced.services;

import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.ProjectDefinition;
import io.github.jspinak.brobot.runner.project.RunnerInterface;
import io.github.jspinak.brobot.runner.project.TaskButton;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnhancedButtonServiceTest {
    
    @Mock
    private AutomationProjectManager projectManager;
    
    @Mock
    private ProjectDefinition projectDefinition;
    
    @Mock
    private AutomationProject project;
    
    @Mock
    private RunnerInterface automation;
    
    private EnhancedButtonService service;
    
    @BeforeEach
    void setUp() {
        service = new EnhancedButtonService(projectManager);
        
        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testLoadProjectButtonsWithNoProject() {
        // Given
        when(projectManager.getActiveProject()).thenReturn(null);
        
        // When
        EnhancedButtonService.ButtonLoadResult result = service.loadProjectButtons();
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals(0, result.getButtonCount());
        assertEquals("No project loaded. Please load a configuration first.", result.getMessage());
        assertTrue(result.getButtons().isEmpty());
    }
    
    @Test
    void testLoadProjectButtonsWithNoAutomation() {
        // Given
        when(projectManager.getActiveProject()).thenReturn(projectDefinition);
        when(projectManager.getActiveAutomationProject()).thenReturn(project);
        when(project.getAutomation()).thenReturn(null);
        
        // When
        EnhancedButtonService.ButtonLoadResult result = service.loadProjectButtons();
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("No automation buttons defined in the current project.", result.getMessage());
    }
    
    @Test
    void testLoadProjectButtonsWithEmptyButtons() {
        // Given
        when(projectManager.getActiveProject()).thenReturn(projectDefinition);
        when(projectManager.getActiveAutomationProject()).thenReturn(project);
        when(project.getAutomation()).thenReturn(automation);
        when(automation.getButtons()).thenReturn(Collections.emptyList());
        
        // When
        EnhancedButtonService.ButtonLoadResult result = service.loadProjectButtons();
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals("No automation buttons defined in the current project.", result.getMessage());
    }
    
    @Test
    void testLoadProjectButtonsSuccess() {
        // Given
        TaskButton button1 = createTaskButton("1", "Button 1", "Category A");
        TaskButton button2 = createTaskButton("2", "Button 2", "Category B");
        List<TaskButton> buttons = Arrays.asList(button1, button2);
        
        when(projectManager.getActiveProject()).thenReturn(projectDefinition);
        when(projectManager.getActiveAutomationProject()).thenReturn(project);
        when(project.getAutomation()).thenReturn(automation);
        when(automation.getButtons()).thenReturn(buttons);
        
        AtomicReference<String> logMessage = new AtomicReference<>();
        service.setLogHandler(logMessage::set);
        
        // When
        EnhancedButtonService.ButtonLoadResult result = service.loadProjectButtons();
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals(2, result.getButtonCount());
        assertEquals("Found 2 automation functions.", result.getMessage());
        assertEquals(2, result.getButtons().size());
        assertTrue(logMessage.get().contains("Loaded 2 buttons"));
    }
    
    @Test
    void testPopulateButtonPaneWithCategories() {
        // Given
        FlowPane buttonPane = new FlowPane();
        TaskButton button1 = createTaskButton("1", "Button 1", "Category A");
        TaskButton button2 = createTaskButton("2", "Button 2", "Category A");
        TaskButton button3 = createTaskButton("3", "Button 3", "Category B");
        List<TaskButton> buttons = Arrays.asList(button1, button2, button3);
        
        // When
        service.populateButtonPane(buttonPane, buttons);
        
        // Then
        assertEquals(2, buttonPane.getChildren().size()); // Two categories
        
        // Verify first category
        Node node1 = buttonPane.getChildren().get(0);
        assertTrue(node1 instanceof VBox);
        VBox category1 = (VBox) node1;
        assertEquals(3, category1.getChildren().size()); // Label + 2 buttons
        
        // Verify second category
        Node node2 = buttonPane.getChildren().get(1);
        assertTrue(node2 instanceof VBox);
        VBox category2 = (VBox) node2;
        assertEquals(2, category2.getChildren().size()); // Label + 1 button
    }
    
    @Test
    void testPopulateButtonPaneWithoutCategories() {
        // Given
        service.setConfiguration(
            EnhancedButtonService.ButtonConfiguration.builder()
                .groupByCategory(false)
                .build()
        );
        
        FlowPane buttonPane = new FlowPane();
        TaskButton button1 = createTaskButton("1", "Button 1", "Category A");
        TaskButton button2 = createTaskButton("2", "Button 2", "Category B");
        List<TaskButton> buttons = Arrays.asList(button1, button2);
        
        // When
        service.populateButtonPane(buttonPane, buttons);
        
        // Then
        assertEquals(2, buttonPane.getChildren().size());
        assertTrue(buttonPane.getChildren().get(0) instanceof Button);
        assertTrue(buttonPane.getChildren().get(1) instanceof Button);
    }
    
    @Test
    void testDefaultCategoryAssignment() {
        // Given
        FlowPane buttonPane = new FlowPane();
        TaskButton button1 = createTaskButton("1", "Button 1", null); // No category
        TaskButton button2 = createTaskButton("2", "Button 2", "Custom");
        List<TaskButton> buttons = Arrays.asList(button1, button2);
        
        // When
        service.populateButtonPane(buttonPane, buttons);
        
        // Then
        assertEquals(2, buttonPane.getChildren().size());
        
        // First category should be "General" (default)
        VBox generalCategory = (VBox) buttonPane.getChildren().get(0);
        Label generalLabel = (Label) generalCategory.getChildren().get(0);
        assertEquals("General", generalLabel.getText());
        
        // Second category should be "Custom"
        VBox customCategory = (VBox) buttonPane.getChildren().get(1);
        Label customLabel = (Label) customCategory.getChildren().get(0);
        assertEquals("Custom", customLabel.getText());
    }
    
    @Test
    void testUpdateButtonStates() {
        // Given
        FlowPane buttonPane = new FlowPane();
        TaskButton button1 = createTaskButton("1", "Button 1", "Category A");
        TaskButton button2 = createTaskButton("2", "Button 2", null);
        service.populateButtonPane(buttonPane, Arrays.asList(button1, button2));
        
        // When - Disable all buttons
        service.updateButtonStates(buttonPane, true);
        
        // Then - All buttons should be disabled
        for (Node categoryNode : buttonPane.getChildren()) {
            VBox categoryBox = (VBox) categoryNode;
            for (Node child : categoryBox.getChildren()) {
                if (child instanceof Button) {
                    assertTrue(((Button) child).isDisable());
                }
            }
        }
        
        // When - Enable all buttons
        service.updateButtonStates(buttonPane, false);
        
        // Then - All buttons should be enabled
        for (Node categoryNode : buttonPane.getChildren()) {
            VBox categoryBox = (VBox) categoryNode;
            for (Node child : categoryBox.getChildren()) {
                if (child instanceof Button) {
                    assertFalse(((Button) child).isDisable());
                }
            }
        }
    }
    
    @Test
    void testButtonActionHandler() {
        // Given
        AtomicReference<TaskButton> clickedButton = new AtomicReference<>();
        service.setButtonActionHandler(clickedButton::set);
        
        FlowPane buttonPane = new FlowPane();
        TaskButton taskButton = createTaskButton("1", "Test Button", "Test");
        
        // When
        service.populateButtonPane(buttonPane, Arrays.asList(taskButton));
        
        // Then - Find and click the button
        VBox categoryBox = (VBox) buttonPane.getChildren().get(0);
        Button uiButton = (Button) categoryBox.getChildren().get(1); // After label
        
        uiButton.fire();
        
        assertEquals(taskButton, clickedButton.get());
    }
    
    @Test
    void testButtonStyling() {
        // Given
        FlowPane buttonPane = new FlowPane();
        TaskButton taskButton = createTaskButton("1", "Styled Button", "Test");
        TaskButton.ButtonStyling styling = new TaskButton.ButtonStyling();
        styling.setBackgroundColor("#FF0000");
        styling.setTextColor("#FFFFFF");
        styling.setSize("large");
        styling.setCustomClass("special-button");
        taskButton.setStyling(styling);
        
        // When
        service.populateButtonPane(buttonPane, Arrays.asList(taskButton));
        
        // Then
        VBox categoryBox = (VBox) buttonPane.getChildren().get(0);
        Button uiButton = (Button) categoryBox.getChildren().get(1);
        
        String style = uiButton.getStyle();
        assertTrue(style.contains("-fx-background-color: #FF0000"));
        assertTrue(style.contains("-fx-text-fill: #FFFFFF"));
        assertTrue(style.contains("-fx-font-size: 14px")); // large = 14px
        assertTrue(uiButton.getStyleClass().contains("special-button"));
    }
    
    @Test
    void testButtonTooltip() {
        // Given
        FlowPane buttonPane = new FlowPane();
        TaskButton taskButton = createTaskButton("1", "Button with Tooltip", "Test");
        taskButton.setTooltip("This is a helpful tooltip");
        
        // When
        service.populateButtonPane(buttonPane, Arrays.asList(taskButton));
        
        // Then
        VBox categoryBox = (VBox) buttonPane.getChildren().get(0);
        Button uiButton = (Button) categoryBox.getChildren().get(1);
        
        assertNotNull(uiButton.getTooltip());
        assertEquals("This is a helpful tooltip", uiButton.getTooltip().getText());
    }
    
    @Test
    void testConfiguration() {
        // Given
        EnhancedButtonService.ButtonConfiguration config = 
            EnhancedButtonService.ButtonConfiguration.builder()
                .groupByCategory(false)
                .defaultCategory("Custom Default")
                .categorySpacing(10)
                .categoryPadding(new Insets(10))
                .categoryStyle("-fx-border-color: blue;")
                .categoryLabelStyle("-fx-font-weight: bold;")
                .defaultButtonFontSize("16px")
                .build();
        
        service.setConfiguration(config);
        
        // Then
        assertFalse(config.isGroupByCategory());
        assertEquals("Custom Default", config.getDefaultCategory());
        assertEquals(10, config.getCategorySpacing());
        assertEquals(new Insets(10), config.getCategoryPadding());
        assertEquals("-fx-border-color: blue;", config.getCategoryStyle());
        assertEquals("-fx-font-weight: bold;", config.getCategoryLabelStyle());
        assertEquals("16px", config.getDefaultButtonFontSize());
    }
    
    private TaskButton createTaskButton(String id, String label, String category) {
        TaskButton button = new TaskButton();
        button.setId(id);
        button.setLabel(label);
        button.setCategory(category);
        return button;
    }
}