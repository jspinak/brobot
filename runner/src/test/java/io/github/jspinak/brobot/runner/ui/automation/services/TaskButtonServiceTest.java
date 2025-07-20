package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.RunnerInterface;
import io.github.jspinak.brobot.runner.project.TaskButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskButtonService.
 */
class TaskButtonServiceTest {
    
    @Mock
    private AutomationProjectManager projectManager;
    
    @Mock
    private AutomationProject mockProject;
    
    @Mock
    private RunnerInterface mockRunner;
    
    private TaskButtonService taskButtonService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taskButtonService = new TaskButtonService(projectManager);
    }
    
    @Test
    @DisplayName("Should load tasks from project")
    void testLoadProjectTasks() {
        // Given
        List<TaskButton> taskButtons = Arrays.asList(
            createTaskButton("task1", "Task 1", "Category A"),
            createTaskButton("task2", "Task 2", "Category A"),
            createTaskButton("task3", "Task 3", "Category B"),
            createTaskButton("task4", "Task 4", null) // Should go to "General"
        );
        
        when(projectManager.getCurrentProject()).thenReturn(mockProject);
        when(mockProject.getName()).thenReturn("Test Project");
        when(mockProject.getAutomation()).thenReturn(mockRunner);
        when(mockRunner.getButtons()).thenReturn(taskButtons);
        
        AtomicReference<String> loadedProjectName = new AtomicReference<>();
        AtomicReference<Integer> loadedTaskCount = new AtomicReference<>();
        
        taskButtonService.setTaskLoadListener((projectName, taskCount) -> {
            loadedProjectName.set(projectName);
            loadedTaskCount.set(taskCount);
        });
        
        // When
        TaskButtonService.ProjectTasks result = taskButtonService.loadProjectTasks();
        
        // Then
        assertTrue(result.hasProject());
        assertEquals("Test Project", result.getProjectName());
        assertEquals(4, result.getTotalTasks());
        
        Map<String, List<TaskButton>> tasksByCategory = result.getTasksByCategory();
        assertEquals(3, tasksByCategory.size());
        assertTrue(tasksByCategory.containsKey("Category A"));
        assertTrue(tasksByCategory.containsKey("Category B"));
        assertTrue(tasksByCategory.containsKey("General"));
        
        assertEquals(2, tasksByCategory.get("Category A").size());
        assertEquals(1, tasksByCategory.get("Category B").size());
        assertEquals(1, tasksByCategory.get("General").size());
        
        // Verify listener was called
        assertEquals("Test Project", loadedProjectName.get());
        assertEquals(4, loadedTaskCount.get());
    }
    
    @Test
    @DisplayName("Should handle no project loaded")
    void testLoadProjectTasksNoProject() {
        // Given
        when(projectManager.getCurrentProject()).thenReturn(null);
        
        // When
        TaskButtonService.ProjectTasks result = taskButtonService.loadProjectTasks();
        
        // Then
        assertFalse(result.hasProject());
        assertNull(result.getProjectName());
        assertEquals(0, result.getTotalTasks());
        assertTrue(result.getTasksByCategory().isEmpty());
    }
    
    @Test
    @DisplayName("Should validate task button")
    void testValidateTaskButton() {
        // Test valid task
        TaskButton validTask = createTaskButton("valid", "Valid Task", "Test");
        validTask.setFunctionName("testFunction");
        
        TaskButtonService.TaskValidationResult validResult = taskButtonService.validateTask(validTask);
        assertTrue(validResult.isValid());
        assertTrue(validResult.getIssues().isEmpty());
        
        // Test task without function
        TaskButton noFunctionTask = createTaskButton("no-func", "No Function", "Test");
        
        TaskButtonService.TaskValidationResult noFunctionResult = taskButtonService.validateTask(noFunctionTask);
        assertFalse(noFunctionResult.isValid());
        assertEquals(1, noFunctionResult.getIssues().size());
        assertTrue(noFunctionResult.getIssues().get(0).contains("No function name"));
        
        // Test task without label or ID
        TaskButton noIdentifierTask = new TaskButton();
        noIdentifierTask.setFunctionName("test");
        
        TaskButtonService.TaskValidationResult noIdResult = taskButtonService.validateTask(noIdentifierTask);
        assertFalse(noIdResult.isValid());
        assertTrue(noIdResult.getIssues().stream().anyMatch(issue -> issue.contains("No label or ID")));
    }
    
    @Test
    @DisplayName("Should handle task execution with confirmation")
    void testRequestTaskExecutionWithConfirmation() {
        // Given
        TaskButton taskButton = createTaskButton("confirm-task", "Confirm Task", "Test");
        taskButton.setConfirmationRequired(true);
        taskButton.setConfirmationMessage("Are you sure?");
        
        // Note: In a real test, we'd need to mock the Alert dialog
        // For now, we just verify the method doesn't throw
        assertDoesNotThrow(() -> taskButtonService.requestTaskExecution(taskButton));
    }
    
    @Test
    @DisplayName("Should search tasks")
    void testSearchTasks() {
        // Given - Load some tasks first
        List<TaskButton> taskButtons = Arrays.asList(
            createTaskButton("start", "Start Process", "Automation"),
            createTaskButton("stop", "Stop Process", "Automation"),
            createTaskButton("config", "Configure Settings", "Settings"),
            createTaskButton("export", "Export Data", "Data")
        );
        
        when(projectManager.getCurrentProject()).thenReturn(mockProject);
        when(mockProject.getName()).thenReturn("Test");
        when(mockProject.getAutomation()).thenReturn(mockRunner);
        when(mockRunner.getButtons()).thenReturn(taskButtons);
        
        taskButtonService.loadProjectTasks();
        
        // When - Search for "process"
        List<TaskButton> processResults = taskButtonService.searchTasks("process");
        
        // Then
        assertEquals(2, processResults.size());
        assertTrue(processResults.stream().anyMatch(t -> t.getLabel().equals("Start Process")));
        assertTrue(processResults.stream().anyMatch(t -> t.getLabel().equals("Stop Process")));
        
        // When - Search for "data"
        List<TaskButton> dataResults = taskButtonService.searchTasks("data");
        
        // Then
        assertEquals(1, dataResults.size());
        assertEquals("Export Data", dataResults.get(0).getLabel());
        
        // When - Empty search
        List<TaskButton> allResults = taskButtonService.searchTasks("");
        assertEquals(4, allResults.size());
    }
    
    @Test
    @DisplayName("Should get task statistics")
    void testGetStatistics() {
        // Given
        TaskButton confirmTask = createTaskButton("task1", "Task 1", "Cat1");
        confirmTask.setConfirmationRequired(true);
        
        TaskButton paramTask = createTaskButton("task2", "Task 2", "Cat1");
        // Simulate parameters
        
        List<TaskButton> taskButtons = Arrays.asList(
            confirmTask,
            paramTask,
            createTaskButton("task3", "Task 3", "Cat2"),
            createTaskButton("task4", "Task 4", "Cat2")
        );
        
        when(projectManager.getCurrentProject()).thenReturn(mockProject);
        when(mockProject.getName()).thenReturn("Test");
        when(mockProject.getAutomation()).thenReturn(mockRunner);
        when(mockRunner.getButtons()).thenReturn(taskButtons);
        
        taskButtonService.loadProjectTasks();
        
        // When
        TaskButtonService.TaskStatistics stats = taskButtonService.getStatistics();
        
        // Then
        assertEquals(4, stats.getTotalTasks());
        assertEquals(2, stats.getTotalCategories());
        assertEquals(1, stats.getTasksWithConfirmation());
    }
    
    @Test
    @DisplayName("Should get task by key")
    void testGetTaskButton() {
        // Given
        TaskButton task = createTaskButton("test-id", "Test Task", "Test");
        List<TaskButton> taskButtons = Arrays.asList(task);
        
        when(projectManager.getCurrentProject()).thenReturn(mockProject);
        when(mockProject.getName()).thenReturn("Test");
        when(mockProject.getAutomation()).thenReturn(mockRunner);
        when(mockRunner.getButtons()).thenReturn(taskButtons);
        
        taskButtonService.loadProjectTasks();
        
        // When
        TaskButton retrieved = taskButtonService.getTaskButton("Test Task");
        
        // Then
        assertNotNull(retrieved);
        assertEquals("test-id", retrieved.getId());
        assertEquals("Test Task", retrieved.getLabel());
    }
    
    @Test
    @DisplayName("Should get categories")
    void testGetCategories() {
        // Given
        List<TaskButton> taskButtons = Arrays.asList(
            createTaskButton("1", "Task 1", "UI"),
            createTaskButton("2", "Task 2", "Backend"),
            createTaskButton("3", "Task 3", "UI"),
            createTaskButton("4", "Task 4", "Database")
        );
        
        when(projectManager.getCurrentProject()).thenReturn(mockProject);
        when(mockProject.getName()).thenReturn("Test");
        when(mockProject.getAutomation()).thenReturn(mockRunner);
        when(mockRunner.getButtons()).thenReturn(taskButtons);
        
        taskButtonService.loadProjectTasks();
        
        // When
        var categories = taskButtonService.getCategories();
        
        // Then
        assertEquals(3, categories.size());
        assertTrue(categories.contains("UI"));
        assertTrue(categories.contains("Backend"));
        assertTrue(categories.contains("Database"));
    }
    
    /**
     * Helper method to create a task button.
     */
    private TaskButton createTaskButton(String id, String label, String category) {
        TaskButton button = new TaskButton();
        button.setId(id);
        button.setLabel(label);
        button.setCategory(category);
        button.setTooltip("Tooltip for " + label);
        return button;
    }
}