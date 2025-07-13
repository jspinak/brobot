package io.github.jspinak.brobot.tools.logging.enhanced;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.model.element.basic.match.Match;
import io.github.jspinak.brobot.model.element.basic.region.Region;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.console.ConsoleActionConfig;
import io.github.jspinak.brobot.tools.logging.console.ConsoleActionReporter;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessMonitor;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of EnhancedActionLogger that combines standard action logging
 * with visual feedback, console reporting, and GUI access monitoring.
 * 
 * <p>This implementation decorates the base ActionLogger with additional
 * capabilities while maintaining backward compatibility.</p>
 * 
 * @see EnhancedActionLogger for the interface
 * @see ActionLogger for the base logging functionality
 */
@Component
@Slf4j
public class EnhancedActionLoggerImpl implements EnhancedActionLogger {
    
    private final ActionLogger baseLogger;
    private final BrobotLogger brobotLogger;
    private final ConsoleActionReporter consoleReporter;
    private final HighlightManager highlightManager;
    private final GuiAccessMonitor guiAccessMonitor;
    private final VisualFeedbackConfig visualConfig;
    
    private boolean visualFeedbackEnabled = true;
    private String consoleVerbosity = "NORMAL";
    
    @Autowired
    public EnhancedActionLoggerImpl(
        ActionLogger baseLogger,
        BrobotLogger brobotLogger,
        ConsoleActionReporter consoleReporter,
        HighlightManager highlightManager,
        GuiAccessMonitor guiAccessMonitor,
        VisualFeedbackConfig visualConfig
    ) {
        this.baseLogger = baseLogger;
        this.brobotLogger = brobotLogger;
        this.consoleReporter = consoleReporter;
        this.highlightManager = highlightManager;
        this.guiAccessMonitor = guiAccessMonitor;
        this.visualConfig = visualConfig;
    }
    
    @Override
    public void logActionWithVisuals(
        String action, 
        ObjectCollection target, 
        ActionResult result,
        VisualFeedbackOptions visualOptions
    ) {
        // Create unified log event
        try (var operation = brobotLogger.operation("Action-" + action)) {
            long startTime = System.currentTimeMillis();
            
            // Log through unified system
            brobotLogger.log()
                .action(action)
                .target(extractPrimaryTarget(target))
                .result(result)
                .performance("duration", result.getDuration())
                .log();
            
            // Console reporting
            LogData logData = createLogData(action, target, result);
            consoleReporter.reportLogEntry(logData);
            
            // Visual feedback if enabled
            if (visualFeedbackEnabled && visualOptions.isHighlightEnabled()) {
                VisualFeedbackOptions mergedOptions = visualOptions.mergeWithGlobal(visualConfig);
                performVisualFeedback(action, target, result, mergedOptions);
            }
            
            // Delegate to base logger for compatibility
            baseLogger.logAction(action, target, result);
            
            long totalDuration = System.currentTimeMillis() - startTime;
            if (totalDuration > 50) { // Log if visual feedback takes significant time
                brobotLogger.log()
                    .observation("Action logging with visuals completed")
                    .metadata("totalDuration", totalDuration)
                    .metadata("actionDuration", result.getDuration())
                    .metadata("overheadDuration", totalDuration - result.getDuration())
                    .log();
            }
        }
    }
    
    @Override
    public void logGuiAccessProblem(String problem, Exception error) {
        brobotLogger.log()
            .error(error)
            .message("GUI Access Problem: " + problem)
            .metadata("problemType", "GUI_ACCESS")
            .console("❌ GUI Problem: " + problem)
            .log();
            
        // Trigger GUI access check for detailed diagnostics
        guiAccessMonitor.checkGuiAccess();
    }
    
    @Override
    public void logImageSearch(String imageName, Region region, boolean found) {
        String regionDesc = region != null ? 
            String.format("(%d,%d %dx%d)", region.x(), region.y(), region.w(), region.h()) : 
            "full screen";
            
        brobotLogger.log()
            .observation("Image search")
            .metadata("image", imageName)
            .metadata("region", regionDesc)
            .metadata("found", found)
            .log();
            
        if (visualConfig.isAutoHighlightSearchRegions() && region != null) {
            highlightManager.highlightSearchRegions(List.of(region));
        }
    }
    
    @Override
    public void logSearchStart(StateObject target, Region... searchRegions) {
        List<Region> regions = Arrays.asList(searchRegions);
        
        brobotLogger.log()
            .observation("Starting image search")
            .metadata("target", target.getName())
            .metadata("regionCount", regions.size())
            .metadata("regions", regions.stream()
                .map(r -> String.format("(%d,%d %dx%d)", r.x(), r.y(), r.w(), r.h()))
                .collect(Collectors.joining(", ")))
            .log();
            
        if (visualConfig.isAutoHighlightSearchRegions() && !regions.isEmpty()) {
            highlightManager.highlightSearchRegions(regions);
        }
    }
    
    @Override
    public void logSearchComplete(StateObject target, ActionResult result, long duration) {
        brobotLogger.log()
            .observation("Image search completed")
            .metadata("target", target.getName())
            .metadata("success", result.isSuccess())
            .metadata("matchCount", result.getMatchList().size())
            .metadata("duration", duration)
            .log();
            
        if (result.isSuccess() && visualConfig.isAutoHighlightFinds()) {
            highlightManager.highlightMatches(result.getMatchList());
        } else if (!result.isSuccess() && visualConfig.getError().isEnabled()) {
            // Highlight the search area to show where we looked
            if (!target.getSearchRegions().isEmpty()) {
                highlightManager.highlightError(target.getSearchRegions().get(0));
            }
        }
    }
    
    @Override
    public void logClickWithVisual(int x, int y, boolean success) {
        brobotLogger.log()
            .action("CLICK")
            .metadata("x", x)
            .metadata("y", y)
            .metadata("success", success)
            .log();
            
        if (visualConfig.getClick().isEnabled()) {
            highlightManager.highlightClick(x, y);
        }
    }
    
    @Override
    public void logDragWithVisual(int fromX, int fromY, int toX, int toY, boolean success) {
        brobotLogger.log()
            .action("DRAG")
            .metadata("from", String.format("(%d,%d)", fromX, fromY))
            .metadata("to", String.format("(%d,%d)", toX, toY))
            .metadata("success", success)
            .log();
            
        // Visual feedback for drag could show the path
        // For now, highlight start and end points
        if (visualConfig.isEnabled()) {
            highlightManager.highlightClick(fromX, fromY);
            // Small delay to show sequence
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            highlightManager.highlightClick(toX, toY);
        }
    }
    
    @Override
    public void logHighlight(Region region, String color, double duration) {
        brobotLogger.log()
            .action("HIGHLIGHT")
            .metadata("region", String.format("(%d,%d %dx%d)", 
                region.x(), region.y(), region.w(), region.h()))
            .metadata("color", color)
            .metadata("duration", duration)
            .log();
    }
    
    @Override
    public void setVisualFeedbackEnabled(boolean enabled) {
        this.visualFeedbackEnabled = enabled;
        brobotLogger.log()
            .observation("Visual feedback " + (enabled ? "enabled" : "disabled"))
            .log();
    }
    
    @Override
    public boolean isVisualFeedbackEnabled() {
        return visualFeedbackEnabled && visualConfig.isEnabled();
    }
    
    @Override
    public void setConsoleVerbosity(String level) {
        this.consoleVerbosity = level;
        // This would need to be propagated to ConsoleActionConfig
        // For now, just log the change
        brobotLogger.log()
            .observation("Console verbosity changed")
            .metadata("level", level)
            .log();
    }
    
    @Override
    public boolean checkAndLogGuiAccess() {
        return guiAccessMonitor.checkGuiAccess();
    }
    
    // Delegate methods from ActionLogger interface
    
    @Override
    public void logAction(String action, ObjectCollection objectCollection, ActionResult result) {
        // Use enhanced logging with default visual options
        logActionWithVisuals(action, objectCollection, result, VisualFeedbackOptions.defaults());
    }
    
    @Override
    public void logTransition(String fromState, String toState, boolean success, long duration) {
        baseLogger.logTransition(fromState, toState, success, duration);
        
        // Also create console output for transitions
        LogData logData = new LogData();
        logData.setType(LogEventType.TRANSITION);
        logData.setFromStates(fromState);
        logData.setToStateNames(List.of(toState));
        logData.setSuccess(success);
        logData.setDuration(duration);
        
        consoleReporter.reportLogEntry(logData);
    }
    
    @Override
    public void logObservation(String observation) {
        baseLogger.logObservation(observation);
        brobotLogger.observation(observation);
    }
    
    @Override
    public void logMetrics(String metricName, double value) {
        baseLogger.logMetrics(metricName, value);
        brobotLogger.log()
            .metric(metricName, value)
            .log();
    }
    
    @Override
    public void logError(String error, Exception exception) {
        baseLogger.logError(error, exception);
        
        LogData logData = new LogData();
        logData.setType(LogEventType.ERROR);
        logData.setErrorMessage(error);
        
        consoleReporter.reportLogEntry(logData);
    }
    
    @Override
    public void startVideoRecording(String filename) {
        baseLogger.startVideoRecording(filename);
        brobotLogger.log()
            .observation("Video recording started")
            .metadata("filename", filename)
            .log();
    }
    
    @Override
    public void stopVideoRecording() {
        baseLogger.stopVideoRecording();
        brobotLogger.log()
            .observation("Video recording stopped")
            .log();
    }
    
    // Helper methods
    
    private void performVisualFeedback(String action, ObjectCollection target, 
                                     ActionResult result, VisualFeedbackOptions options) {
        switch (action.toUpperCase()) {
            case "FIND":
                if (result.isSuccess() && options.isHighlightFinds()) {
                    highlightManager.highlightMatches(result.getMatchList());
                } else if (!result.isSuccess() && options.isHighlightErrors()) {
                    highlightSearchAreas(target);
                }
                break;
                
            case "CLICK":
                if (options.isHighlightFinds() && !result.getMatchList().isEmpty()) {
                    Match match = result.getMatchList().get(0);
                    highlightManager.highlightClick(
                        match.getRegion().x() + match.getRegion().w() / 2,
                        match.getRegion().y() + match.getRegion().h() / 2
                    );
                }
                break;
                
            // Add other action types as needed
        }
    }
    
    private void highlightSearchAreas(ObjectCollection target) {
        List<Region> searchRegions = new ArrayList<>();
        
        // Collect search regions from all state objects
        target.getStateObjects().forEach(stateObject -> 
            searchRegions.addAll(stateObject.getSearchRegions())
        );
        
        if (!searchRegions.isEmpty()) {
            highlightManager.highlightSearchRegions(searchRegions);
        }
    }
    
    private LogData createLogData(String action, ObjectCollection target, ActionResult result) {
        LogData logData = new LogData();
        logData.setType(LogEventType.ACTION);
        logData.setActionType(action);
        logData.setSuccess(result.isSuccess());
        logData.setDuration(result.getDuration());
        logData.setDescription(result.getActionDescription());
        
        // Extract target name from first state object
        if (!target.getStateObjects().isEmpty()) {
            logData.setDescription(action + " " + target.getStateObjects().get(0).getName());
        }
        
        return logData;
    }
    
    private StateObject extractPrimaryTarget(ObjectCollection target) {
        if (!target.getStateObjects().isEmpty()) {
            return target.getStateObjects().get(0);
        }
        return null;
    }
}