package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.project.RunnerInterface;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskManagementServiceTest {
    
    @Mock
    private AutomationProjectManager projectManager;
    
    @Mock
    private AutomationProject project;
    
    @Mock
    private RunnerInterface automation;
    
    private TaskManagementService service;
    
    @BeforeEach
    void setUp() {
        service = new TaskManagementService(projectManager);
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testGetCurrentProjectWhenNull() {
        // Given
        when(projectManager.getCurrentProject()).thenReturn(null);
        
        // When
        AutomationProject result = service.getCurrentProject();
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetCurrentProjectName() {
        // Given
        when(projectManager.getCurrentProject()).thenReturn(project);
        when(project.getName()).thenReturn("Test Project");
        
        // When
        String name = service.getCurrentProjectName();
        
        // Then
        assertEquals("Test Project", name);
    }
    
    @Test
    void testGetCurrentProjectNameWhenNoProject() {
        // Given
        when(projectManager.getCurrentProject()).thenReturn(null);
        
        // When
        String name = service.getCurrentProjectName();
        
        // Then
        assertEquals("No project loaded", name);
    }
    
    @Test
    void testLoadProjectTasksWithNoProject() {
        // Given
        when(projectManager.getCurrentProject()).thenReturn(null);
        
        // When
        TaskManagementService.ProjectTasksResult result = service.loadProjectTasks();
        
        // Then
        assertNull(result.getProjectName());
        assertTrue(result.getTasks().isEmpty());
        assertEquals("No project loaded", result.getMessage());
        assertFalse(result.hasProject());
        assertFalse(result.hasTasks());
    }
    
    @Test
    void testLoadProjectTasksWithNoTasks() {
        // Given
        when(projectManager.getCurrentProject()).thenReturn(project);
        when(project.getName()).thenReturn("Empty Project");
        when(project.getAutomation()).thenReturn(null);
        
        // When
        TaskManagementService.ProjectTasksResult result = service.loadProjectTasks();
        
        // Then
        assertEquals("Empty Project", result.getProjectName());
        assertTrue(result.getTasks().isEmpty());
        assertEquals("No tasks defined", result.getMessage());
        assertTrue(result.hasProject());
        assertFalse(result.hasTasks());
    }
    
    @Test
    void testLoadProjectTasksWithTasks() {
        // Given
        TaskButton task1 = createTaskButton("1", "Task 1", "Category A");
        TaskButton task2 = createTaskButton("2", "Task 2", "Category B");
        List<TaskButton> tasks = Arrays.asList(task1, task2);
        
        when(projectManager.getCurrentProject()).thenReturn(project);
        when(project.getName()).thenReturn("Test Project");
        when(project.getAutomation()).thenReturn(automation);
        when(automation.getButtons()).thenReturn(tasks);
        
        // When
        TaskManagementService.ProjectTasksResult result = service.loadProjectTasks();
        
        // Then
        assertEquals("Test Project", result.getProjectName());
        assertEquals(2, result.getTasks().size());
        assertNull(result.getMessage());
        assertTrue(result.hasProject());
        assertTrue(result.hasTasks());
        assertEquals(2, service.getTaskCount());
    }
    
    @Test
    void testCreateTaskUIWithEmptyList() {
        // When
        List<Node> nodes = service.createTaskUI(Arrays.asList());
        
        // Then
        assertEquals(1, nodes.size());
        Node node = nodes.get(0);
        assertTrue(node instanceof VBox);
        VBox emptyState = (VBox) node;
        assertEquals(2, emptyState.getChildren().size()); // Title and instruction labels
    }
    
    @Test
    void testCreateTaskUIWithCategories() {
        // Given
        TaskButton task1 = createTaskButton("1", "Task 1", "Category A");
        TaskButton task2 = createTaskButton("2", "Task 2", "Category A");
        TaskButton task3 = createTaskButton("3", "Task 3", "Category B");
        
        // When
        List<Node> nodes = service.createTaskUI(Arrays.asList(task1, task2, task3));
        
        // Then
        assertEquals(2, nodes.size()); // Two categories
        
        // Verify first category
        assertTrue(nodes.get(0) instanceof VBox);
        VBox categoryA = (VBox) nodes.get(0);
        assertTrue(categoryA.getChildren().get(0) instanceof Label);
        Label labelA = (Label) categoryA.getChildren().get(0);
        assertEquals("Category A", labelA.getText());
        
        // Verify buttons in category
        assertTrue(categoryA.getChildren().get(1) instanceof FlowPane);
        FlowPane buttonsA = (FlowPane) categoryA.getChildren().get(1);
        assertEquals(2, buttonsA.getChildren().size());
    }
    
    @Test
    void testCreateTaskUIWithoutCategories() {
        // Given
        service.setConfiguration(
            TaskManagementService.TaskConfiguration.builder()
                .groupByCategory(false)
                .build()
        );
        
        TaskButton task1 = createTaskButton("1", "Task 1", "Category A");
        TaskButton task2 = createTaskButton("2", "Task 2", "Category B");
        
        // When
        List<Node> nodes = service.createTaskUI(Arrays.asList(task1, task2));
        
        // Then
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof FlowPane);
        FlowPane allButtons = (FlowPane) nodes.get(0);
        assertEquals(2, allButtons.getChildren().size());
    }
    
    @Test
    void testCreateTaskUIWithDefaultCategory() {
        // Given
        TaskButton task1 = createTaskButton("1", "Task 1", null); // No category
        TaskButton task2 = createTaskButton("2", "Task 2", "Custom");
        
        // When
        List<Node> nodes = service.createTaskUI(Arrays.asList(task1, task2));
        
        // Then
        assertEquals(2, nodes.size());
        
        // First should be "General" (default)
        VBox defaultCategory = (VBox) nodes.get(0);
        Label defaultLabel = (Label) defaultCategory.getChildren().get(0);
        assertEquals("General", defaultLabel.getText());
        
        // Second should be "Custom"
        VBox customCategory = (VBox) nodes.get(1);
        Label customLabel = (Label) customCategory.getChildren().get(0);
        assertEquals("Custom", customLabel.getText());
    }
    
    @Test
    void testTaskExecutionHandler() {
        // Given
        AtomicReference<TaskButton> executedTask = new AtomicReference<>();
        Consumer<TaskButton> handler = executedTask::set;
        service.setTaskExecutionHandler(handler);
        
        TaskButton task = createTaskButton("1", "Test Task", "Test");
        
        // When
        List<Node> nodes = service.createTaskUI(Arrays.asList(task));
        
        // Then - Find and click the button
        VBox categoryBox = (VBox) nodes.get(0);
        FlowPane buttonPane = (FlowPane) categoryBox.getChildren().get(1);
        Button button = (Button) buttonPane.getChildren().get(0);
        
        button.fire();
        
        assertEquals(task, executedTask.get());
    }
    
    @Test
    void testFindButton() {
        // Given
        TaskButton task1 = createTaskButton("1", "Task One", "Test");
        TaskButton task2 = createTaskButton("2", null, "Test"); // No label
        
        service.createTaskUI(Arrays.asList(task1, task2));
        
        // When
        Button button1 = service.findButton(task1);
        Button button2 = service.findButton(task2);
        
        // Then
        assertNotNull(button1);
        assertEquals("Task One", button1.getText());
        
        assertNotNull(button2);
        assertEquals("2", button2.getText()); // Uses ID when no label
    }
    
    @Test
    void testSetAllButtonsEnabled() {
        // Given
        TaskButton task1 = createTaskButton("1", "Task 1", "Test");
        TaskButton task2 = createTaskButton("2", "Task 2", "Test");
        
        service.createTaskUI(Arrays.asList(task1, task2));
        Map<String, Button> buttons = service.getTaskButtons();
        
        // When - Disable all
        service.setAllButtonsEnabled(false);
        
        // Then
        try {
            Thread.sleep(100); // Allow Platform.runLater to execute
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        buttons.values().forEach(button -> assertTrue(button.isDisable()));
        
        // When - Enable all
        service.setAllButtonsEnabled(true);
        
        try {
            Thread.sleep(100); // Allow Platform.runLater to execute
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        buttons.values().forEach(button -> assertFalse(button.isDisable()));
    }
    
    @Test
    void testCreateEmptyState() {
        // When - Default empty state
        Node defaultEmpty = service.createEmptyState();
        
        // Then
        assertTrue(defaultEmpty instanceof VBox);
        VBox vbox = (VBox) defaultEmpty;
        assertEquals(2, vbox.getChildren().size());
        
        Label title = (Label) vbox.getChildren().get(0);
        assertTrue(title.getText().contains("No tasks defined"));
        
        // When - Custom empty state
        Node customEmpty = service.createEmptyState("Custom Title", "Custom Message");
        
        // Then
        VBox customVbox = (VBox) customEmpty;
        Label customTitle = (Label) customVbox.getChildren().get(0);
        Label customMessage = (Label) customVbox.getChildren().get(1);
        
        assertEquals("Custom Title", customTitle.getText());
        assertEquals("Custom Message", customMessage.getText());
    }
    
    @Test
    void testButtonStyling() {
        // Given
        TaskButton task = createTaskButton("1", "Styled Task", "Test");
        TaskButton.ButtonStyling styling = new TaskButton.ButtonStyling();
        styling.setCustomClass("special-button");
        task.setStyling(styling);
        task.setTooltip("This is a tooltip");
        
        // When
        List<Node> nodes = service.createTaskUI(Arrays.asList(task));
        
        // Then
        VBox categoryBox = (VBox) nodes.get(0);
        FlowPane buttonPane = (FlowPane) categoryBox.getChildren().get(1);
        Button button = (Button) buttonPane.getChildren().get(0);
        
        assertTrue(button.getStyleClass().contains("special-button"));
        assertNotNull(button.getTooltip());
        assertEquals("This is a tooltip", button.getTooltip().getText());
    }
    
    @Test
    void testConfiguration() {
        // Given
        TaskManagementService.TaskConfiguration config = 
            TaskManagementService.TaskConfiguration.builder()
                .defaultCategory("Custom Default")
                .emptyCategoryStyle("custom-category")
                .categoryLabelStyle("custom-label")
                .taskButtonStyle("custom-button")
                .groupByCategory(true)
                .categorySpacing(12)
                .buttonSpacing(16)
                .build();
        
        service.setConfiguration(config);
        
        TaskButton task = createTaskButton("1", "Task", null); // No category
        
        // When
        List<Node> nodes = service.createTaskUI(Arrays.asList(task));
        
        // Then
        VBox categoryBox = (VBox) nodes.get(0);
        Label categoryLabel = (Label) categoryBox.getChildren().get(0);
        
        assertEquals("Custom Default", categoryLabel.getText());
        assertTrue(categoryBox.getStyleClass().contains("custom-category"));
        assertTrue(categoryLabel.getStyleClass().contains("custom-label"));
        assertEquals(12.0, categoryBox.getSpacing());
        
        FlowPane buttonPane = (FlowPane) categoryBox.getChildren().get(1);
        assertEquals(16.0, buttonPane.getHgap());
        assertEquals(16.0, buttonPane.getVgap());
    }
    
    private TaskButton createTaskButton(String id, String label, String category) {
        TaskButton button = new TaskButton();
        button.setId(id);
        button.setLabel(label);
        button.setCategory(category);
        return button;
    }
}