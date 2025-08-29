package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.ProjectDefinition;
import io.github.jspinak.brobot.runner.project.RunnerInterface;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.testutils.ImprovedJavaFXTestBase;
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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutomationButtonServiceTest extends ImprovedJavaFXTestBase {
    
    @Mock
    private AutomationProjectManager projectManager;
    
    @Mock
    private ProjectDefinition projectDefinition;
    
    @Mock
    private AutomationProject project;
    
    @Mock
    private RunnerInterface automation;
    
    private AutomationButtonService service;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AutomationButtonService(projectManager);
        // JavaFX initialization is handled by ImprovedJavaFXTestBase
    }
    
    @Test
    void testLoadProjectButtonsWithNoProject() {
        // Given
        when(projectManager.getActiveProject()).thenReturn(null);
        
        // When
        AutomationButtonService.ButtonLoadResult result = service.loadProjectButtons();
        
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
        AutomationButtonService.ButtonLoadResult result = service.loadProjectButtons();
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals(0, result.getButtonCount());
        assertEquals("No automation buttons defined in the current project.", result.getMessage());
        assertTrue(result.getButtons().isEmpty());
    }
    
    @Test
    void testLoadProjectButtonsWithEmptyButtons() {
        // Given
        when(projectManager.getActiveProject()).thenReturn(projectDefinition);
        when(projectManager.getActiveAutomationProject()).thenReturn(project);
        when(project.getAutomation()).thenReturn(automation);
        when(automation.getButtons()).thenReturn(Collections.emptyList());
        
        // When
        AutomationButtonService.ButtonLoadResult result = service.loadProjectButtons();
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals(0, result.getButtonCount());
        assertEquals("No automation buttons defined in the current project.", result.getMessage());
        assertTrue(result.getButtons().isEmpty());
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
        
        // When
        AutomationButtonService.ButtonLoadResult result = service.loadProjectButtons();
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals(2, result.getButtonCount());
        assertEquals("Found 2 automation functions.", result.getMessage());
        assertEquals(buttons, result.getButtons());
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
        assertTrue(category1.getChildren().get(0) instanceof Label);
        Label label1 = (Label) category1.getChildren().get(0);
        assertEquals("Category A", label1.getText());
        assertEquals(3, category1.getChildren().size()); // Label + 2 buttons
        
        // Verify second category
        Node node2 = buttonPane.getChildren().get(1);
        assertTrue(node2 instanceof VBox);
        VBox category2 = (VBox) node2;
        assertTrue(category2.getChildren().get(0) instanceof Label);
        Label label2 = (Label) category2.getChildren().get(0);
        assertEquals("Category B", label2.getText());
        assertEquals(2, category2.getChildren().size()); // Label + 1 button
    }
    
    @Test
    void testPopulateButtonPaneWithoutCategories() {
        // Given
        service.setConfiguration(
            AutomationButtonService.ButtonConfiguration.builder()
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
    void testButtonActionHandler() {
        // Given
        AtomicReference<TaskButton> clickedButton = new AtomicReference<>();
        Consumer<TaskButton> handler = clickedButton::set;
        service.setButtonActionHandler(handler);
        
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
    void testUpdateButtonStates() {
        // Given
        FlowPane buttonPane = new FlowPane();
        TaskButton button1 = createTaskButton("1", "Button 1", "Category A");
        TaskButton button2 = createTaskButton("2", "Button 2", null); // Will use default category
        service.populateButtonPane(buttonPane, Arrays.asList(button1, button2));
        
        // When - Disable all buttons (running = true)
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
        
        // When - Enable all buttons (running = false)
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
    void testConfiguration() {
        // Given
        AutomationButtonService.ButtonConfiguration config = 
            AutomationButtonService.ButtonConfiguration.builder()
                .defaultCategory("Custom Default")
                .groupByCategory(true)
                .categorySpacing(10)
                .categoryPadding(new Insets(10))
                .categoryStyle("-fx-border-color: blue;")
                .categoryLabelStyle("-fx-font-weight: normal;")
                .defaultButtonFontSize("16px")
                .build();
        
        service.setConfiguration(config);
        
        FlowPane buttonPane = new FlowPane();
        TaskButton button = createTaskButton("1", "Test", null); // No category
        
        // When
        service.populateButtonPane(buttonPane, Arrays.asList(button));
        
        // Then
        VBox categoryBox = (VBox) buttonPane.getChildren().get(0);
        assertEquals(10.0, categoryBox.getSpacing());
        assertEquals(new Insets(10), categoryBox.getPadding());
        assertEquals("-fx-border-color: blue;", categoryBox.getStyle());
        
        Label categoryLabel = (Label) categoryBox.getChildren().get(0);
        assertEquals("Custom Default", categoryLabel.getText());
        assertEquals("-fx-font-weight: normal;", categoryLabel.getStyle());
    }
    
    @Test
    void testButtonSizeStyling() {
        // Given
        FlowPane buttonPane = new FlowPane();
        
        TaskButton smallButton = createTaskButton("1", "Small", "Test");
        TaskButton.ButtonStyling smallStyle = new TaskButton.ButtonStyling();
        smallStyle.setSize("small");
        smallButton.setStyling(smallStyle);
        
        TaskButton largeButton = createTaskButton("2", "Large", "Test");
        TaskButton.ButtonStyling largeStyle = new TaskButton.ButtonStyling();
        largeStyle.setSize("large");
        largeButton.setStyling(largeStyle);
        
        TaskButton defaultButton = createTaskButton("3", "Default", "Test");
        
        // When
        service.populateButtonPane(buttonPane, Arrays.asList(smallButton, largeButton, defaultButton));
        
        // Then
        VBox categoryBox = (VBox) buttonPane.getChildren().get(0);
        Button small = (Button) categoryBox.getChildren().get(1);
        Button large = (Button) categoryBox.getChildren().get(2);
        Button def = (Button) categoryBox.getChildren().get(3);
        
        assertTrue(small.getStyle().contains("-fx-font-size: 10px"));
        assertTrue(large.getStyle().contains("-fx-font-size: 14px"));
        assertTrue(def.getStyle().contains("-fx-font-size: 12px"));
    }
    
    private TaskButton createTaskButton(String id, String label, String category) {
        TaskButton button = new TaskButton();
        button.setId(id);
        button.setLabel(label);
        button.setCategory(category);
        return button;
    }
}