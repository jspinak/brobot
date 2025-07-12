package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.project.RunnerInterface;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Service for managing task buttons and project tasks.
 * Handles task organization, button creation, and category management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskManagementService {
    
    private final AutomationProjectManager projectManager;
    
    // State
    private final Map<String, Button> taskButtons = new ConcurrentHashMap<>();
    private Consumer<TaskButton> taskExecutionHandler;
    
    // Configuration
    private TaskConfiguration configuration = TaskConfiguration.builder().build();
    
    /**
     * Task management configuration.
     */
    public static class TaskConfiguration {
        private String defaultCategory = "General";
        private String emptyCategoryStyle = "task-category";
        private String categoryLabelStyle = "category-label";
        private String taskButtonStyle = "task-button";
        private boolean groupByCategory = true;
        private int categorySpacing = 8;
        private int buttonSpacing = 8;
        
        public static TaskConfigurationBuilder builder() {
            return new TaskConfigurationBuilder();
        }
        
        public static class TaskConfigurationBuilder {
            private TaskConfiguration config = new TaskConfiguration();
            
            public TaskConfigurationBuilder defaultCategory(String category) {
                config.defaultCategory = category;
                return this;
            }
            
            public TaskConfigurationBuilder emptyCategoryStyle(String style) {
                config.emptyCategoryStyle = style;
                return this;
            }
            
            public TaskConfigurationBuilder categoryLabelStyle(String style) {
                config.categoryLabelStyle = style;
                return this;
            }
            
            public TaskConfigurationBuilder taskButtonStyle(String style) {
                config.taskButtonStyle = style;
                return this;
            }
            
            public TaskConfigurationBuilder groupByCategory(boolean group) {
                config.groupByCategory = group;
                return this;
            }
            
            public TaskConfigurationBuilder categorySpacing(int spacing) {
                config.categorySpacing = spacing;
                return this;
            }
            
            public TaskConfigurationBuilder buttonSpacing(int spacing) {
                config.buttonSpacing = spacing;
                return this;
            }
            
            public TaskConfiguration build() {
                return config;
            }
        }
    }
    
    /**
     * Sets the configuration.
     */
    public void setConfiguration(TaskConfiguration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Sets the task execution handler.
     */
    public void setTaskExecutionHandler(Consumer<TaskButton> handler) {
        this.taskExecutionHandler = handler;
    }
    
    /**
     * Gets the current project.
     */
    public AutomationProject getActiveProject() {
        return projectManager.getCurrentProject();
    }
    
    /**
     * Gets the current project name.
     */
    public String getCurrentProjectName() {
        AutomationProject project = getActiveProject();
        return project != null ? project.getName() : "No project loaded";
    }
    
    /**
     * Gets all task buttons.
     */
    public Map<String, Button> getTaskButtons() {
        return new HashMap<>(taskButtons);
    }
    
    /**
     * Gets task count.
     */
    public int getTaskCount() {
        return taskButtons.size();
    }
    
    /**
     * Loads tasks from the current project.
     */
    public ProjectTasksResult loadProjectTasks() {
        taskButtons.clear();
        
        AutomationProject currentProject = getActiveProject();
        if (currentProject == null) {
            return new ProjectTasksResult(null, Collections.emptyList(), "No project loaded");
        }
        
        // Extract tasks from project
        List<TaskButton> tasks = extractTasksFromProject(currentProject);
        
        if (tasks.isEmpty()) {
            return new ProjectTasksResult(currentProject.getName(), tasks, "No tasks defined");
        }
        
        return new ProjectTasksResult(currentProject.getName(), tasks, null);
    }
    
    /**
     * Creates UI for tasks.
     */
    public List<Node> createTaskUI(List<TaskButton> tasks) {
        List<Node> nodes = new ArrayList<>();
        
        if (tasks.isEmpty()) {
            nodes.add(createEmptyState());
            return nodes;
        }
        
        if (configuration.groupByCategory) {
            // Group by category
            Map<String, List<TaskButton>> tasksByCategory = groupTasksByCategory(tasks);
            
            // Create UI for each category
            for (Map.Entry<String, List<TaskButton>> entry : tasksByCategory.entrySet()) {
                nodes.add(createCategoryBox(entry.getKey(), entry.getValue()));
            }
        } else {
            // Create flat layout
            FlowPane allButtons = new FlowPane();
            allButtons.setHgap(configuration.buttonSpacing);
            allButtons.setVgap(configuration.buttonSpacing);
            
            for (TaskButton task : tasks) {
                Button button = createTaskButton(task);
                allButtons.getChildren().add(button);
                registerButton(task, button);
            }
            
            nodes.add(allButtons);
        }
        
        return nodes;
    }
    
    /**
     * Creates an empty state UI.
     */
    public Node createEmptyState() {
        return createEmptyState("No tasks defined", "Add task buttons to your project configuration");
    }
    
    /**
     * Creates an empty state UI with custom messages.
     */
    public Node createEmptyState(String title, String instruction) {
        Label emptyLabel = new Label(title);
        emptyLabel.getStyleClass().add("empty-state-title");
        
        Label instructionLabel = new Label(instruction);
        instructionLabel.getStyleClass().add("empty-state-text");
        
        VBox emptyState = new VBox(8, emptyLabel, instructionLabel);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(40));
        
        return emptyState;
    }
    
    /**
     * Extracts tasks from project.
     */
    private List<TaskButton> extractTasksFromProject(AutomationProject project) {
        List<TaskButton> tasks = new ArrayList<>();
        
        RunnerInterface automation = project.getAutomation();
        if (automation != null && automation.getButtons() != null) {
            tasks.addAll(automation.getButtons());
        }
        
        return tasks;
    }
    
    /**
     * Groups tasks by category.
     */
    private Map<String, List<TaskButton>> groupTasksByCategory(List<TaskButton> tasks) {
        Map<String, List<TaskButton>> tasksByCategory = new LinkedHashMap<>();
        
        for (TaskButton task : tasks) {
            String category = task.getCategory() != null ? task.getCategory() : configuration.defaultCategory;
            tasksByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(task);
        }
        
        return tasksByCategory;
    }
    
    /**
     * Creates a category box.
     */
    private VBox createCategoryBox(String category, List<TaskButton> tasks) {
        VBox categoryBox = new VBox(configuration.categorySpacing);
        categoryBox.getStyleClass().add(configuration.emptyCategoryStyle);
        categoryBox.setPadding(new Insets(8));
        
        Label categoryLabel = new Label(category);
        categoryLabel.getStyleClass().add(configuration.categoryLabelStyle);
        
        FlowPane categoryButtons = new FlowPane();
        categoryButtons.setHgap(configuration.buttonSpacing);
        categoryButtons.setVgap(configuration.buttonSpacing);
        
        for (TaskButton task : tasks) {
            Button button = createTaskButton(task);
            categoryButtons.getChildren().add(button);
            registerButton(task, button);
        }
        
        categoryBox.getChildren().addAll(categoryLabel, categoryButtons);
        return categoryBox;
    }
    
    /**
     * Creates a task button.
     */
    private Button createTaskButton(TaskButton taskButton) {
        String buttonText = taskButton.getLabel() != null ? taskButton.getLabel() : taskButton.getId();
        Button button = new Button(buttonText);
        button.getStyleClass().addAll("button", configuration.taskButtonStyle);
        
        // Apply styling if specified
        TaskButton.ButtonStyling styling = taskButton.getStyling();
        if (styling != null && styling.getCustomClass() != null) {
            button.getStyleClass().add(styling.getCustomClass());
        }
        
        // Set tooltip if available
        if (taskButton.getTooltip() != null && !taskButton.getTooltip().isEmpty()) {
            Tooltip tooltip = new Tooltip(taskButton.getTooltip());
            button.setTooltip(tooltip);
        }
        
        // Set action
        if (taskExecutionHandler != null) {
            button.setOnAction(e -> taskExecutionHandler.accept(taskButton));
        }
        
        return button;
    }
    
    /**
     * Registers a button.
     */
    private void registerButton(TaskButton task, Button button) {
        String key = task.getLabel() != null ? task.getLabel() : task.getId();
        taskButtons.put(key, button);
    }
    
    /**
     * Finds a button by task.
     */
    public Button findButton(TaskButton task) {
        String key = task.getLabel() != null ? task.getLabel() : task.getId();
        return taskButtons.get(key);
    }
    
    /**
     * Enables or disables all task buttons.
     */
    public void setAllButtonsEnabled(boolean enabled) {
        Platform.runLater(() -> {
            taskButtons.values().forEach(button -> button.setDisable(!enabled));
        });
    }
    
    /**
     * Result of loading project tasks.
     */
    public static class ProjectTasksResult {
        private final String projectName;
        private final List<TaskButton> tasks;
        private final String message;
        
        public ProjectTasksResult(String projectName, List<TaskButton> tasks, String message) {
            this.projectName = projectName;
            this.tasks = tasks;
            this.message = message;
        }
        
        public String getProjectName() { return projectName; }
        public List<TaskButton> getTasks() { return tasks; }
        public String getMessage() { return message; }
        public boolean hasProject() { return projectName != null; }
        public boolean hasTasks() { return !tasks.isEmpty(); }
    }
}