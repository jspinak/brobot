package io.github.jspinak.brobot.runner.ui.automation.services;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.RunnerInterface;
import io.github.jspinak.brobot.runner.project.TaskButton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing task buttons and their organization. Handles loading, categorizing, and task
 * execution confirmation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskButtonService {

    private final AutomationProjectManager projectManager;

    // Task button cache
    private final Map<String, TaskButton> taskButtonCache = new ConcurrentHashMap<>();
    private final Map<String, List<TaskButton>> tasksByCategory = new ConcurrentHashMap<>();

    // Listeners
    private TaskLoadListener taskLoadListener;
    private TaskActionListener taskActionListener;

    /** Sets the task load listener. */
    public void setTaskLoadListener(TaskLoadListener listener) {
        this.taskLoadListener = listener;
    }

    /** Sets the task action listener. */
    public void setTaskActionListener(TaskActionListener listener) {
        this.taskActionListener = listener;
    }

    /** Loads tasks from the current project. */
    public ProjectTasks loadProjectTasks() {
        taskButtonCache.clear();
        tasksByCategory.clear();

        AutomationProject currentProject = projectManager.getCurrentProject();
        if (currentProject == null) {
            log.info("No project loaded");
            if (taskLoadListener != null) {
                taskLoadListener.onTasksLoaded(null, 0);
            }
            return new ProjectTasks(null, Collections.emptyMap(), 0);
        }

        log.info("Loading tasks from project: {}", currentProject.getName());

        // Extract tasks from project
        RunnerInterface automation = currentProject.getAutomation();
        if (automation == null || automation.getButtons() == null) {
            log.warn("No automation or buttons defined in project");
            if (taskLoadListener != null) {
                taskLoadListener.onTasksLoaded(currentProject.getName(), 0);
            }
            return new ProjectTasks(currentProject.getName(), Collections.emptyMap(), 0);
        }

        // Organize tasks by category
        for (TaskButton taskButton : automation.getButtons()) {
            String buttonKey = getTaskKey(taskButton);
            taskButtonCache.put(buttonKey, taskButton);

            String category =
                    taskButton.getCategory() != null ? taskButton.getCategory() : "General";
            tasksByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(taskButton);
        }

        int totalTasks = taskButtonCache.size();
        log.info("Loaded {} tasks in {} categories", totalTasks, tasksByCategory.size());

        if (taskLoadListener != null) {
            taskLoadListener.onTasksLoaded(currentProject.getName(), totalTasks);
        }

        return new ProjectTasks(
                currentProject.getName(), new HashMap<>(tasksByCategory), totalTasks);
    }

    /** Gets a task button by key. */
    public TaskButton getTaskButton(String key) {
        return taskButtonCache.get(key);
    }

    /** Gets all task categories. */
    public Set<String> getCategories() {
        return new HashSet<>(tasksByCategory.keySet());
    }

    /** Gets tasks for a specific category. */
    public List<TaskButton> getTasksForCategory(String category) {
        return tasksByCategory.getOrDefault(category, Collections.emptyList());
    }

    /** Gets all tasks sorted by category. */
    public Map<String, List<TaskButton>> getTasksByCategory() {
        return new HashMap<>(tasksByCategory);
    }

    /** Handles task execution request with confirmation if needed. */
    public void requestTaskExecution(TaskButton taskButton) {
        if (taskButton.isConfirmationRequired()) {
            showConfirmationDialog(taskButton);
        } else {
            executeTask(taskButton);
        }
    }

    /** Shows confirmation dialog for task execution. */
    private void showConfirmationDialog(TaskButton taskButton) {
        String taskName = getTaskName(taskButton);
        String message =
                taskButton.getConfirmationMessage() != null
                        ? taskButton.getConfirmationMessage()
                        : "Are you sure you want to execute " + taskName + "?";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Execution");
        confirm.setHeaderText("Task Confirmation");
        confirm.setContentText(message);

        confirm.showAndWait()
                .ifPresent(
                        response -> {
                            if (response == ButtonType.OK) {
                                executeTask(taskButton);
                            }
                        });
    }

    /** Executes a task. */
    private void executeTask(TaskButton taskButton) {
        if (taskActionListener != null) {
            taskActionListener.onTaskExecutionRequested(taskButton);
        }
    }

    /** Validates a task button. */
    public TaskValidationResult validateTask(TaskButton taskButton) {
        List<String> issues = new ArrayList<>();

        // Check for function name
        if (taskButton.getFunctionName() == null || taskButton.getFunctionName().isEmpty()) {
            issues.add("No function name defined");
        }

        // Check for display name
        if (taskButton.getLabel() == null && taskButton.getId() == null) {
            issues.add("No label or ID defined");
        }

        // Validate parameters if present
        Map<String, Object> parameters = taskButton.getParametersAsMap();
        if (parameters != null && parameters.isEmpty()) {
            // Parameters exist but are empty - this might be ok
            log.trace("Task has empty parameters map");
        }

        return new TaskValidationResult(issues.isEmpty(), issues);
    }

    /** Gets the display name for a task. */
    public String getTaskName(TaskButton taskButton) {
        return taskButton.getLabel() != null ? taskButton.getLabel() : taskButton.getId();
    }

    /** Gets the unique key for a task. */
    private String getTaskKey(TaskButton taskButton) {
        return taskButton.getLabel() != null ? taskButton.getLabel() : taskButton.getId();
    }

    /** Filters tasks by search term. */
    public List<TaskButton> searchTasks(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>(taskButtonCache.values());
        }

        String lowerSearch = searchTerm.toLowerCase();
        return taskButtonCache.values().stream()
                .filter(
                        task -> {
                            String name = getTaskName(task).toLowerCase();
                            String category =
                                    (task.getCategory() != null ? task.getCategory() : "")
                                            .toLowerCase();
                            String tooltip =
                                    (task.getTooltip() != null ? task.getTooltip() : "")
                                            .toLowerCase();

                            return name.contains(lowerSearch)
                                    || category.contains(lowerSearch)
                                    || tooltip.contains(lowerSearch);
                        })
                .collect(Collectors.toList());
    }

    /** Gets task statistics. */
    public TaskStatistics getStatistics() {
        int totalTasks = taskButtonCache.size();
        int totalCategories = tasksByCategory.size();
        int tasksWithConfirmation =
                (int)
                        taskButtonCache.values().stream()
                                .filter(TaskButton::isConfirmationRequired)
                                .count();
        int tasksWithParameters =
                (int)
                        taskButtonCache.values().stream()
                                .filter(
                                        task -> {
                                            Map<String, Object> params = task.getParametersAsMap();
                                            return params != null && !params.isEmpty();
                                        })
                                .count();

        return new TaskStatistics(
                totalTasks, totalCategories, tasksWithConfirmation, tasksWithParameters);
    }

    /** Data class for project tasks. */
    public static class ProjectTasks {
        private final String projectName;
        private final Map<String, List<TaskButton>> tasksByCategory;
        private final int totalTasks;

        public ProjectTasks(
                String projectName, Map<String, List<TaskButton>> tasksByCategory, int totalTasks) {
            this.projectName = projectName;
            this.tasksByCategory = tasksByCategory;
            this.totalTasks = totalTasks;
        }

        public String getProjectName() {
            return projectName;
        }

        public Map<String, List<TaskButton>> getTasksByCategory() {
            return tasksByCategory;
        }

        public int getTotalTasks() {
            return totalTasks;
        }

        public boolean hasProject() {
            return projectName != null;
        }
    }

    /** Task validation result. */
    public static class TaskValidationResult {
        private final boolean valid;
        private final List<String> issues;

        public TaskValidationResult(boolean valid, List<String> issues) {
            this.valid = valid;
            this.issues = issues;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getIssues() {
            return issues;
        }
    }

    /** Task statistics. */
    public static class TaskStatistics {
        private final int totalTasks;
        private final int totalCategories;
        private final int tasksWithConfirmation;
        private final int tasksWithParameters;

        public TaskStatistics(
                int totalTasks,
                int totalCategories,
                int tasksWithConfirmation,
                int tasksWithParameters) {
            this.totalTasks = totalTasks;
            this.totalCategories = totalCategories;
            this.tasksWithConfirmation = tasksWithConfirmation;
            this.tasksWithParameters = tasksWithParameters;
        }

        public int getTotalTasks() {
            return totalTasks;
        }

        public int getTotalCategories() {
            return totalCategories;
        }

        public int getTasksWithConfirmation() {
            return tasksWithConfirmation;
        }

        public int getTasksWithParameters() {
            return tasksWithParameters;
        }
    }

    /** Listener for task loading events. */
    @FunctionalInterface
    public interface TaskLoadListener {
        void onTasksLoaded(String projectName, int taskCount);
    }

    /** Listener for task actions. */
    @FunctionalInterface
    public interface TaskActionListener {
        void onTaskExecutionRequested(TaskButton taskButton);
    }
}
