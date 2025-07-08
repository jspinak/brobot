package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service for controlling automation execution.
 * Provides methods to run, pause, resume, and stop automations.
 */
@Slf4j
@Service
public class AutomationControlService {
    
    private final AutomationOrchestrator orchestrator;
    private final AutomationProjectManager projectManager;
    private final EventBus eventBus;
    
    // Execution callbacks
    private final List<Consumer<ExecutionEvent>> executionListeners = new ArrayList<>();
    
    @Autowired
    public AutomationControlService(
            AutomationOrchestrator orchestrator,
            AutomationProjectManager projectManager,
            EventBus eventBus) {
        
        this.orchestrator = orchestrator;
        this.projectManager = projectManager;
        this.eventBus = eventBus;
    }
    
    /**
     * Executes an automation by name.
     * Returns a CompletableFuture that completes when the automation finishes.
     */
    public CompletableFuture<ExecutionResult> executeAutomation(String automationName) {
        log.info("Executing automation: {}", automationName);
        
        // Check if already running
        io.github.jspinak.brobot.runner.execution.ExecutionStatus status = orchestrator.getExecutionStatus();
        if (status != null && status.getState() == io.github.jspinak.brobot.runner.execution.ExecutionState.RUNNING) {
            log.warn("Cannot execute automation - another automation is already running");
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Another automation is already running")
            );
        }
        
        // Get current project
        AutomationProject project = projectManager.getCurrentProject();
        if (project == null) {
            log.error("No project loaded");
            return CompletableFuture.completedFuture(
                ExecutionResult.error("No project loaded")
            );
        }
        
        // Find the task button for this automation
        io.github.jspinak.brobot.runner.project.TaskButton taskButton = project.getAutomations().stream()
            .filter(button -> button.getLabel().equals(automationName))
            .findFirst()
            .orElse(null);
            
        if (taskButton == null) {
            log.error("Automation not found: {}", automationName);
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Automation not found: " + automationName)
            );
        }
        
        // Notify listeners of start
        notifyExecutionListeners(ExecutionEvent.started(automationName));
        
        // Create execution task
        CompletableFuture<ExecutionResult> future = CompletableFuture.supplyAsync(() -> {
            try {
                // Execute the automation
                orchestrator.executeAutomation(taskButton);
                
                // Wait for completion
                io.github.jspinak.brobot.runner.execution.ExecutionStatus execStatus;
                do {
                    Thread.sleep(100);
                    execStatus = orchestrator.getExecutionStatus();
                } while (execStatus != null && 
                         execStatus.getState() == io.github.jspinak.brobot.runner.execution.ExecutionState.RUNNING ||
                         execStatus.getState() == io.github.jspinak.brobot.runner.execution.ExecutionState.PAUSED);
                
                // Check for errors
                if (execStatus != null && execStatus.getState() == io.github.jspinak.brobot.runner.execution.ExecutionState.FAILED) {
                    String error = execStatus.getErrorMessage();
                    log.error("Automation failed: {}", error);
                    return ExecutionResult.error(error);
                }
                
                log.info("Automation completed successfully: {}", automationName);
                return ExecutionResult.success();
                
            } catch (Exception e) {
                log.error("Error executing automation", e);
                return ExecutionResult.error(e.getMessage());
            }
        });
        
        // Add completion handler
        future.thenAccept(result -> {
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    notifyExecutionListeners(ExecutionEvent.completed(automationName));
                } else {
                    notifyExecutionListeners(ExecutionEvent.failed(automationName, result.getError()));
                }
            });
        });
        
        return future;
    }
    
    /**
     * Pauses the currently running automation.
     */
    public boolean pauseAutomation() {
        io.github.jspinak.brobot.runner.execution.ExecutionStatus status = orchestrator.getExecutionStatus();
        if (status == null || status.getState() != io.github.jspinak.brobot.runner.execution.ExecutionState.RUNNING) {
            log.warn("No automation is running to pause");
            return false;
        }
        
        if (orchestrator.isPaused()) {
            log.warn("Automation is already paused");
            return false;
        }
        
        log.info("Pausing automation");
        orchestrator.pause();
        
        String automationName = status.getCurrentTaskName() != null ? status.getCurrentTaskName() : "Unknown";
        notifyExecutionListeners(ExecutionEvent.paused(automationName));
        return true;
    }
    
    /**
     * Resumes a paused automation.
     */
    public boolean resumeAutomation() {
        io.github.jspinak.brobot.runner.execution.ExecutionStatus status = orchestrator.getExecutionStatus();
        if (status == null || (status.getState() != io.github.jspinak.brobot.runner.execution.ExecutionState.RUNNING &&
                               status.getState() != io.github.jspinak.brobot.runner.execution.ExecutionState.PAUSED)) {
            log.warn("No automation is running to resume");
            return false;
        }
        
        if (!orchestrator.isPaused()) {
            log.warn("Automation is not paused");
            return false;
        }
        
        log.info("Resuming automation");
        orchestrator.resume();
        
        String automationName = status.getCurrentTaskName() != null ? status.getCurrentTaskName() : "Unknown";
        notifyExecutionListeners(ExecutionEvent.resumed(automationName));
        return true;
    }
    
    /**
     * Stops all running automations.
     */
    public void stopAllAutomations() {
        io.github.jspinak.brobot.runner.execution.ExecutionStatus status = orchestrator.getExecutionStatus();
        if (status == null || (status.getState() != io.github.jspinak.brobot.runner.execution.ExecutionState.RUNNING &&
                               status.getState() != io.github.jspinak.brobot.runner.execution.ExecutionState.PAUSED)) {
            log.debug("No automation is running to stop");
            return;
        }
        
        String automationName = status.getCurrentTaskName() != null ? status.getCurrentTaskName() : "Unknown";
        log.info("Stopping automation: {}", automationName);
        
        orchestrator.stop();
        
        notifyExecutionListeners(ExecutionEvent.stopped(automationName));
    }
    
    /**
     * Toggles between pause and resume states.
     */
    public boolean togglePauseResume() {
        io.github.jspinak.brobot.runner.execution.ExecutionStatus status = orchestrator.getExecutionStatus();
        if (status == null || (status.getState() != io.github.jspinak.brobot.runner.execution.ExecutionState.RUNNING &&
                               status.getState() != io.github.jspinak.brobot.runner.execution.ExecutionState.PAUSED)) {
            return false;
        }
        
        if (orchestrator.isPaused()) {
            return resumeAutomation();
        } else {
            return pauseAutomation();
        }
    }
    
    /**
     * Checks if any automation is currently running.
     */
    public boolean isRunning() {
        io.github.jspinak.brobot.runner.execution.ExecutionStatus status = orchestrator.getExecutionStatus();
        return status != null && (status.getState() == io.github.jspinak.brobot.runner.execution.ExecutionState.RUNNING ||
                                 status.getState() == io.github.jspinak.brobot.runner.execution.ExecutionState.PAUSED);
    }
    
    /**
     * Checks if the current automation is paused.
     */
    public boolean isPaused() {
        return orchestrator.isPaused();
    }
    
    /**
     * Gets the name of the currently running automation.
     */
    public String getCurrentAutomationName() {
        io.github.jspinak.brobot.runner.execution.ExecutionStatus status = orchestrator.getExecutionStatus();
        return status != null ? status.getCurrentTaskName() : null;
    }
    
    /**
     * Runs all automations in the current project sequentially.
     */
    public CompletableFuture<ExecutionResult> runAllAutomations() {
        AutomationProject project = projectManager.getCurrentProject();
        if (project == null) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("No project loaded")
            );
        }
        
        List<String> automations = project.getAutomations().stream()
            .map(button -> button.getLabel())
            .collect(java.util.stream.Collectors.toList());
            
        if (automations.isEmpty()) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("No automations found in project")
            );
        }
        
        return runAutomationsSequentially(automations);
    }
    
    /**
     * Runs a list of automations sequentially.
     */
    private CompletableFuture<ExecutionResult> runAutomationsSequentially(List<String> automations) {
        if (automations.isEmpty()) {
            return CompletableFuture.completedFuture(ExecutionResult.success());
        }
        
        String first = automations.get(0);
        List<String> rest = automations.subList(1, automations.size());
        
        return executeAutomation(first)
            .thenCompose(result -> {
                if (!result.isSuccess()) {
                    return CompletableFuture.completedFuture(result);
                }
                return runAutomationsSequentially(rest);
            });
    }
    
    /**
     * Adds an execution listener.
     */
    public void addExecutionListener(Consumer<ExecutionEvent> listener) {
        executionListeners.add(listener);
    }
    
    /**
     * Removes an execution listener.
     */
    public void removeExecutionListener(Consumer<ExecutionEvent> listener) {
        executionListeners.remove(listener);
    }
    
    /**
     * Notifies all execution listeners.
     */
    private void notifyExecutionListeners(ExecutionEvent event) {
        for (Consumer<ExecutionEvent> listener : executionListeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                log.error("Error notifying execution listener", e);
            }
        }
    }
    
    /**
     * Result of an automation execution.
     */
    public static class ExecutionResult {
        private final boolean success;
        private final String error;
        
        private ExecutionResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }
        
        public static ExecutionResult success() {
            return new ExecutionResult(true, null);
        }
        
        public static ExecutionResult error(String error) {
            return new ExecutionResult(false, error);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getError() {
            return error;
        }
    }
    
    /**
     * Event representing an execution state change.
     */
    public static class ExecutionEvent {
        public enum Type {
            STARTED, COMPLETED, FAILED, PAUSED, RESUMED, STOPPED
        }
        
        private final Type type;
        private final String automationName;
        private final String error;
        
        private ExecutionEvent(Type type, String automationName, String error) {
            this.type = type;
            this.automationName = automationName;
            this.error = error;
        }
        
        public static ExecutionEvent started(String automationName) {
            return new ExecutionEvent(Type.STARTED, automationName, null);
        }
        
        public static ExecutionEvent completed(String automationName) {
            return new ExecutionEvent(Type.COMPLETED, automationName, null);
        }
        
        public static ExecutionEvent failed(String automationName, String error) {
            return new ExecutionEvent(Type.FAILED, automationName, error);
        }
        
        public static ExecutionEvent paused(String automationName) {
            return new ExecutionEvent(Type.PAUSED, automationName, null);
        }
        
        public static ExecutionEvent resumed(String automationName) {
            return new ExecutionEvent(Type.RESUMED, automationName, null);
        }
        
        public static ExecutionEvent stopped(String automationName) {
            return new ExecutionEvent(Type.STOPPED, automationName, null);
        }
        
        public Type getType() {
            return type;
        }
        
        public String getAutomationName() {
            return automationName;
        }
        
        public String getError() {
            return error;
        }
    }
}