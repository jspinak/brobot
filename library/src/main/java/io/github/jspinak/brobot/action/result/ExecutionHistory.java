package io.github.jspinak.brobot.action.result;

import io.github.jspinak.brobot.action.internal.execution.ActionLifecycle;
import io.github.jspinak.brobot.model.action.ActionRecord;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Tracks the execution history of action chains and composite actions.
 * Maintains a record of all steps performed during action execution.
 * 
 * This class encapsulates execution history functionality that was
 * previously embedded in ActionResult.
 * 
 * @since 2.0
 */
@Data
public class ExecutionHistory {
    private List<ActionRecord> records = new ArrayList<>();
    private ActionLifecycle lifecycle;
    private Instant historyStart;
    private Instant historyEnd;
    
    /**
     * Creates an empty ExecutionHistory.
     */
    public ExecutionHistory() {
        this.historyStart = Instant.now();
    }
    
    /**
     * Creates ExecutionHistory with an ActionLifecycle.
     * 
     * @param lifecycle The action lifecycle to track
     */
    public ExecutionHistory(ActionLifecycle lifecycle) {
        this();
        this.lifecycle = lifecycle;
    }
    
    /**
     * Records a step in the execution history.
     * 
     * @param record The action record to add
     */
    public void recordStep(ActionRecord record) {
        if (record != null) {
            records.add(record);
        }
    }
    
    /**
     * Creates and records a new step.
     * 
     * @param actionType The type of action performed
     * @param success Whether the action succeeded
     * @param description Description of the action
     * @return The created ActionRecord
     */
    public ActionRecord recordStep(String actionType, boolean success, String description) {
        ActionRecord record = new ActionRecord();
        record.setActionSuccess(success);
        record.setResultSuccess(success);
        // Store description in text field
        record.setText(description != null ? description : "");
        records.add(record);
        return record;
    }
    
    /**
     * Gets the complete execution history.
     * 
     * @return List of all action records
     */
    public List<ActionRecord> getHistory() {
        return new ArrayList<>(records);
    }
    
    /**
     * Gets successful action records.
     * 
     * @return List of successful actions
     */
    public List<ActionRecord> getSuccessfulSteps() {
        return records.stream()
                .filter(ActionRecord::isActionSuccess)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets failed action records.
     * 
     * @return List of failed actions
     */
    public List<ActionRecord> getFailedSteps() {
        return records.stream()
                .filter(r -> !r.isActionSuccess())
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the first action record.
     * 
     * @return Optional containing the first record
     */
    public Optional<ActionRecord> getFirstStep() {
        if (records.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(records.get(0));
    }
    
    /**
     * Gets the last action record.
     * 
     * @return Optional containing the last record
     */
    public Optional<ActionRecord> getLastStep() {
        if (records.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(records.get(records.size() - 1));
    }
    
    /**
     * Gets the number of steps in the history.
     * 
     * @return Count of action records
     */
    public int getStepCount() {
        return records.size();
    }
    
    /**
     * Gets the number of successful steps.
     * 
     * @return Count of successful actions
     */
    public int getSuccessCount() {
        return (int) records.stream()
                .filter(ActionRecord::isActionSuccess)
                .count();
    }
    
    /**
     * Gets the number of failed steps.
     * 
     * @return Count of failed actions
     */
    public int getFailureCount() {
        return getStepCount() - getSuccessCount();
    }
    
    /**
     * Gets the success rate.
     * 
     * @return Success rate as a percentage (0-100)
     */
    public double getSuccessRate() {
        if (records.isEmpty()) {
            return 0.0;
        }
        return (getSuccessCount() * 100.0) / getStepCount();
    }
    
    /**
     * Checks if all steps were successful.
     * 
     * @return true if all steps succeeded
     */
    public boolean isCompleteSuccess() {
        return !records.isEmpty() && getFailureCount() == 0;
    }
    
    /**
     * Checks if any step failed.
     * 
     * @return true if at least one step failed
     */
    public boolean hasFailures() {
        return getFailureCount() > 0;
    }
    
    /**
     * Marks the history as complete.
     */
    public void complete() {
        this.historyEnd = Instant.now();
    }
    
    /**
     * Gets the total duration of the history.
     * 
     * @return Duration from start to end (or now if not complete)
     */
    public Duration getTotalDuration() {
        Instant end = historyEnd != null ? historyEnd : Instant.now();
        return Duration.between(historyStart, end);
    }
    
    /**
     * Checks if the history is empty.
     * 
     * @return true if no records exist
     */
    public boolean isEmpty() {
        return records.isEmpty();
    }
    
    /**
     * Merges history from another instance.
     * 
     * @param other The ExecutionHistory to merge
     */
    public void merge(ExecutionHistory other) {
        if (other != null) {
            records.addAll(other.records);
            
            // Update history end time if other is more recent
            if (other.historyEnd != null) {
                if (historyEnd == null || other.historyEnd.isAfter(historyEnd)) {
                    historyEnd = other.historyEnd;
                }
            }
        }
    }
    
    /**
     * Clears all history data.
     */
    public void clear() {
        records.clear();
        lifecycle = null;
        historyStart = Instant.now();
        historyEnd = null;
    }
    
    /**
     * Formats the history as a timeline.
     * 
     * @return Formatted timeline string
     */
    public String formatTimeline() {
        if (records.isEmpty()) {
            return "No execution history";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Execution Timeline:\n");
        
        for (int i = 0; i < records.size(); i++) {
            ActionRecord record = records.get(i);
            sb.append(String.format("  %d. [%s] %s\n",
                i + 1,
                record.isActionSuccess() ? "✓" : "✗",
                record.getText()));
        }
        
        return sb.toString();
    }
    
    /**
     * Formats the history as a summary.
     * 
     * @return Formatted summary string
     */
    public String formatSummary() {
        if (records.isEmpty()) {
            return "No execution history";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Steps: %d (Success: %d, Failed: %d)",
            getStepCount(), getSuccessCount(), getFailureCount()));
        sb.append(String.format(", Success rate: %.1f%%", getSuccessRate()));
        
        Duration duration = getTotalDuration();
        if (duration.toMillis() > 0) {
            sb.append(String.format(", Duration: %dms", duration.toMillis()));
        }
        
        if (lifecycle != null) {
            sb.append(", Lifecycle: present");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return formatSummary();
    }
}