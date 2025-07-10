package io.github.jspinak.brobot.runner.ui.log.services;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;
import javafx.beans.property.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing the view state of the log panel.
 * Tracks selection, auto-scroll, and status information.
 */
@Slf4j
@Service
public class LogViewStateManager {
    
    // View state properties
    private final BooleanProperty autoScroll = new SimpleBooleanProperty(true);
    private final ObjectProperty<LogEntry> selectedEntry = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty("0 logs");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final IntegerProperty totalCount = new SimpleIntegerProperty(0);
    private final IntegerProperty filteredCount = new SimpleIntegerProperty(0);
    
    // UI preferences
    private final BooleanProperty showDetails = new SimpleBooleanProperty(true);
    private final BooleanProperty wrapText = new SimpleBooleanProperty(true);
    private final ObjectProperty<DetailLevel> detailLevel = new SimpleObjectProperty<>(DetailLevel.NORMAL);
    
    /**
     * Detail level for log display.
     */
    public enum DetailLevel {
        MINIMAL("Minimal", "Show only essential information"),
        NORMAL("Normal", "Show standard log information"),
        DETAILED("Detailed", "Show all available information"),
        DEBUG("Debug", "Show debug-level details");
        
        private final String displayName;
        private final String description;
        
        DetailLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * View state snapshot for persistence.
     */
    public static class ViewState {
        private final boolean autoScroll;
        private final boolean showDetails;
        private final boolean wrapText;
        private final DetailLevel detailLevel;
        private final String selectedEntryId;
        
        public ViewState(boolean autoScroll, boolean showDetails, boolean wrapText, 
                        DetailLevel detailLevel, String selectedEntryId) {
            this.autoScroll = autoScroll;
            this.showDetails = showDetails;
            this.wrapText = wrapText;
            this.detailLevel = detailLevel;
            this.selectedEntryId = selectedEntryId;
        }
        
        // Getters
        public boolean isAutoScroll() { return autoScroll; }
        public boolean isShowDetails() { return showDetails; }
        public boolean isWrapText() { return wrapText; }
        public DetailLevel getDetailLevel() { return detailLevel; }
        public String getSelectedEntryId() { return selectedEntryId; }
    }
    
    public LogViewStateManager() {
        // Set up property listeners for logging
        autoScroll.addListener((obs, oldVal, newVal) -> 
            log.debug("Auto-scroll changed to: {}", newVal));
        
        selectedEntry.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                log.trace("Selected log entry: {}", newVal.getId());
            }
        });
        
        detailLevel.addListener((obs, oldVal, newVal) -> 
            log.debug("Detail level changed to: {}", newVal));
    }
    
    /**
     * Updates the status based on counts.
     */
    public void updateStatus(int total, int filtered) {
        totalCount.set(total);
        filteredCount.set(filtered);
        
        if (total == filtered) {
            status.set(total + " logs");
        } else {
            status.set(filtered + " of " + total + " logs");
        }
    }
    
    /**
     * Updates the status with custom message.
     */
    public void updateStatus(String message) {
        status.set(message);
    }
    
    /**
     * Sets loading state.
     */
    public void setLoading(boolean loading) {
        this.loading.set(loading);
        if (loading) {
            updateStatus("Loading...");
        }
    }
    
    /**
     * Clears the selection.
     */
    public void clearSelection() {
        selectedEntry.set(null);
    }
    
    /**
     * Saves the current view state.
     */
    public ViewState saveState() {
        String selectedId = selectedEntry.get() != null ? 
            selectedEntry.get().getId() : null;
            
        return new ViewState(
            autoScroll.get(),
            showDetails.get(),
            wrapText.get(),
            detailLevel.get(),
            selectedId
        );
    }
    
    /**
     * Restores view state.
     */
    public void restoreState(ViewState state) {
        if (state == null) {
            return;
        }
        
        autoScroll.set(state.isAutoScroll());
        showDetails.set(state.isShowDetails());
        wrapText.set(state.isWrapText());
        detailLevel.set(state.getDetailLevel());
        
        // Note: selectedEntry would need to be restored by the caller
        // using the selectedEntryId and looking up the actual entry
        
        log.debug("Restored view state");
    }
    
    /**
     * Resets to default state.
     */
    public void resetToDefaults() {
        autoScroll.set(true);
        showDetails.set(true);
        wrapText.set(true);
        detailLevel.set(DetailLevel.NORMAL);
        selectedEntry.set(null);
        status.set("0 logs");
        loading.set(false);
        totalCount.set(0);
        filteredCount.set(0);
        
        log.debug("Reset view state to defaults");
    }
    
    /**
     * Formats details for the selected entry based on detail level.
     */
    public String formatDetails(LogEntry entry) {
        if (entry == null) {
            return "";
        }
        
        StringBuilder details = new StringBuilder();
        DetailLevel level = detailLevel.get();
        
        // Always show basic info
        details.append("Time: ").append(entry.getFormattedTimestamp()).append("\n");
        details.append("Level: ").append(entry.getLevel()).append("\n");
        details.append("Type: ").append(entry.getType()).append("\n");
        
        if (level != DetailLevel.MINIMAL) {
            details.append("Source: ").append(entry.getSource()).append("\n");
            details.append("\nMessage:\n").append(entry.getMessage()).append("\n");
        }
        
        if (level == DetailLevel.DETAILED || level == DetailLevel.DEBUG) {
            if (entry.getDetails() != null) {
                details.append("\nDetails:\n").append(entry.getDetails()).append("\n");
            }
            
            if (entry.hasException()) {
                details.append("\nException:\n").append(entry.getExceptionStackTrace()).append("\n");
            }
            
            if (entry.getStateName() != null) {
                details.append("\nState: ").append(entry.getStateName()).append("\n");
            }
            
            if (entry.getActionName() != null) {
                details.append("Action: ").append(entry.getActionName()).append("\n");
            }
        }
        
        if (level == DetailLevel.DEBUG) {
            details.append("\nDebug Info:\n");
            details.append("ID: ").append(entry.getId()).append("\n");
            
            if (entry.hasMetadata()) {
                details.append("Metadata:\n");
                entry.getMetadata().forEach((key, value) -> 
                    details.append("  ").append(key).append(": ").append(value).append("\n")
                );
            }
        }
        
        return details.toString();
    }
    
    /**
     * Gets display options for the current state.
     */
    public DisplayOptions getDisplayOptions() {
        return new DisplayOptions(
            showDetails.get(),
            wrapText.get(),
            detailLevel.get(),
            autoScroll.get()
        );
    }
    
    /**
     * Display options container.
     */
    public static class DisplayOptions {
        private final boolean showDetails;
        private final boolean wrapText;
        private final DetailLevel detailLevel;
        private final boolean autoScroll;
        
        public DisplayOptions(boolean showDetails, boolean wrapText, 
                            DetailLevel detailLevel, boolean autoScroll) {
            this.showDetails = showDetails;
            this.wrapText = wrapText;
            this.detailLevel = detailLevel;
            this.autoScroll = autoScroll;
        }
        
        public boolean isShowDetails() { return showDetails; }
        public boolean isWrapText() { return wrapText; }
        public DetailLevel getDetailLevel() { return detailLevel; }
        public boolean isAutoScroll() { return autoScroll; }
    }
    
    // Property accessors
    public BooleanProperty autoScrollProperty() { return autoScroll; }
    public ObjectProperty<LogEntry> selectedEntryProperty() { return selectedEntry; }
    public StringProperty statusProperty() { return status; }
    public BooleanProperty loadingProperty() { return loading; }
    public IntegerProperty totalCountProperty() { return totalCount; }
    public IntegerProperty filteredCountProperty() { return filteredCount; }
    public BooleanProperty showDetailsProperty() { return showDetails; }
    public BooleanProperty wrapTextProperty() { return wrapText; }
    public ObjectProperty<DetailLevel> detailLevelProperty() { return detailLevel; }
}