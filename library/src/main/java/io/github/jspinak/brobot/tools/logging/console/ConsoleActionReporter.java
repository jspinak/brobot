package io.github.jspinak.brobot.tools.logging.console;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Console reporter for action logs that provides real-time feedback about
 * automation actions being performed. Converts structured LogData into
 * human-readable console output with configurable verbosity levels.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Visual indicators for success/failure (✓/✗)</li>
 *   <li>Timing information for performance analysis</li>
 *   <li>Match details including location and score</li>
 *   <li>Configurable verbosity levels</li>
 *   <li>Colored output support (when enabled)</li>
 * </ul>
 * 
 * @see LogData for the log entry structure
 * @see ConsoleActionConfig for configuration options
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsoleActionReporter {
    
    private final BrobotLogger brobotLogger;
    private final ConsoleActionConfig config;
    
    // Pattern to extract action type from description
    private static final Pattern ACTION_PATTERN = Pattern.compile("^(\\w+)\\s+(.*)");
    
    // Icons for different states
    private static final String SUCCESS_ICON = "✓";
    private static final String FAILURE_ICON = "✗";
    private static final String WARNING_ICON = "⚠️";
    private static final String ERROR_ICON = "❌";
    private static final String INFO_ICON = "ℹ️";
    private static final String SEARCH_ICON = "🔍";
    private static final String CLICK_ICON = "👆";
    private static final String TYPE_ICON = "⌨️";
    private static final String DRAG_ICON = "↔️";
    private static final String STATE_ICON = "🔄";
    
    /**
     * Reports an action log entry to the console based on configuration settings.
     * 
     * @param logData The log data to report
     */
    public void reportLogEntry(LogData logData) {
        if (!config.isEnabled()) {
            return;
        }
        
        if (logData.getType() == LogEventType.ACTION) {
            reportAction(logData);
        } else if (logData.getType() == LogEventType.TRANSITION) {
            reportTransition(logData);
        } else if (logData.getType() == LogEventType.ERROR) {
            reportError(logData);
        }
    }
    
    /**
     * Reports an action to the console with appropriate formatting.
     */
    private void reportAction(LogData logData) {
        String actionType = extractActionType(logData);
        
        if (!shouldReportAction(actionType)) {
            return;
        }
        
        switch (actionType.toUpperCase()) {
            case "FIND":
                reportFind(logData);
                break;
            case "CLICK":
                reportClick(logData);
                break;
            case "TYPE":
                reportType(logData);
                break;
            case "DRAG":
                reportDrag(logData);
                break;
            default:
                reportGenericAction(logData);
        }
        
        // Check for performance warnings
        if (config.isShowTiming() && logData.getDuration() > config.getPerformanceWarnThreshold()) {
            reportPerformanceWarning(logData);
        }
    }
    
    /**
     * Reports a FIND action with match details.
     */
    private void reportFind(LogData logData) {
        String target = extractTargetName(logData);
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();
        
        String icon = config.isUseIcons() ? SEARCH_ICON + " " : "";
        String status = success ? SUCCESS_ICON + " FOUND" : FAILURE_ICON + " NOT FOUND";
        
        if (config.getLevel() == ConsoleActionConfig.Level.VERBOSE) {
            String message = String.format("%sFIND: %s → %s (%dms)", 
                icon, target, status, duration);
            
            brobotLogger.log()
                .console(message)
                .metadata("action", "FIND")
                .metadata("target", target)
                .metadata("success", success)
                .metadata("duration", duration)
                .log();
                
            if (success && config.isShowMatchDetails()) {
                reportMatchDetails(logData);
            } else if (!success) {
                reportFindFailureDetails(logData);
            }
        } else if (config.getLevel() == ConsoleActionConfig.Level.NORMAL) {
            String message = String.format("%s %s %s", 
                success ? SUCCESS_ICON : FAILURE_ICON, 
                "FIND", 
                target);
            
            brobotLogger.log()
                .console(message)
                .log();
        }
    }
    
    /**
     * Reports a CLICK action.
     */
    private void reportClick(LogData logData) {
        String target = extractTargetName(logData);
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();
        
        String icon = config.isUseIcons() ? CLICK_ICON + " " : "";
        
        if (config.getLevel() == ConsoleActionConfig.Level.VERBOSE) {
            String message = String.format("%sCLICK: %s (%dms) %s", 
                icon, target, duration, success ? SUCCESS_ICON : FAILURE_ICON);
            
            brobotLogger.log()
                .console(message)
                .metadata("action", "CLICK")
                .metadata("target", target)
                .metadata("success", success)
                .log();
        } else if (config.getLevel() == ConsoleActionConfig.Level.NORMAL) {
            String message = String.format("%s CLICK %s", 
                success ? SUCCESS_ICON : FAILURE_ICON, 
                target);
            
            brobotLogger.log()
                .console(message)
                .log();
        }
    }
    
    /**
     * Reports a TYPE action.
     */
    private void reportType(LogData logData) {
        String text = extractTypeText(logData);
        boolean success = logData.isSuccess();
        
        String icon = config.isUseIcons() ? TYPE_ICON + " " : "";
        String displayText = text.length() > 30 ? text.substring(0, 27) + "..." : text;
        
        if (config.getLevel() == ConsoleActionConfig.Level.VERBOSE) {
            String message = String.format("%sTYPE: \"%s\" %s", 
                icon, displayText, success ? SUCCESS_ICON : FAILURE_ICON);
            
            brobotLogger.log()
                .console(message)
                .metadata("action", "TYPE")
                .metadata("textLength", text.length())
                .metadata("success", success)
                .log();
        } else if (config.getLevel() == ConsoleActionConfig.Level.NORMAL) {
            String message = String.format("%s TYPE", 
                success ? SUCCESS_ICON : FAILURE_ICON);
            
            brobotLogger.log()
                .console(message)
                .log();
        }
    }
    
    /**
     * Reports a DRAG action.
     */
    private void reportDrag(LogData logData) {
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();
        
        String icon = config.isUseIcons() ? DRAG_ICON + " " : "";
        
        if (config.getLevel() == ConsoleActionConfig.Level.VERBOSE) {
            String message = String.format("%sDRAG (%dms) %s", 
                icon, duration, success ? SUCCESS_ICON : FAILURE_ICON);
            
            brobotLogger.log()
                .console(message)
                .metadata("action", "DRAG")
                .metadata("success", success)
                .log();
        } else if (config.getLevel() == ConsoleActionConfig.Level.NORMAL) {
            String message = String.format("%s DRAG", 
                success ? SUCCESS_ICON : FAILURE_ICON);
            
            brobotLogger.log()
                .console(message)
                .log();
        }
    }
    
    /**
     * Reports a generic action.
     */
    private void reportGenericAction(LogData logData) {
        String actionType = extractActionType(logData);
        boolean success = logData.isSuccess();
        
        if (config.getLevel() != ConsoleActionConfig.Level.QUIET) {
            String message = String.format("%s %s", 
                success ? SUCCESS_ICON : FAILURE_ICON, 
                actionType.toUpperCase());
            
            brobotLogger.log()
                .console(message)
                .log();
        }
    }
    
    /**
     * Reports match details for successful finds.
     */
    private void reportMatchDetails(LogData logData) {
        // Parse match details from description or other fields
        // This is a simplified version - in reality, we'd need to extract
        // match data from the ActionResult
        
        String details = extractMatchDetails(logData);
        if (details != null && !details.isEmpty()) {
            brobotLogger.log()
                .console("   └─ " + details)
                .log();
        }
    }
    
    /**
     * Reports details about why a find failed.
     */
    private void reportFindFailureDetails(LogData logData) {
        if (config.getLevel() == ConsoleActionConfig.Level.VERBOSE) {
            String errorMessage = logData.getErrorMessage();
            if (errorMessage != null && !errorMessage.isEmpty()) {
                brobotLogger.log()
                    .console("   └─ " + errorMessage)
                    .log();
            }
        }
    }
    
    /**
     * Reports state transitions.
     */
    private void reportTransition(LogData logData) {
        if (!config.isReportTransitions()) {
            return;
        }
        
        String from = logData.getFromStates() != null ? logData.getFromStates() : "Unknown";
        List<String> to = logData.getToStateNames();
        String toStates = to != null && !to.isEmpty() ? String.join(", ", to) : "Unknown";
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();
        
        String icon = config.isUseIcons() ? STATE_ICON + " " : "";
        String message = String.format("%sSTATE: %s → %s [%dms] [%s]", 
            icon, from, toStates, duration, success ? "SUCCESS" : "FAILED");
        
        brobotLogger.log()
            .console(message)
            .metadata("type", "TRANSITION")
            .metadata("from", from)
            .metadata("to", toStates)
            .metadata("success", success)
            .log();
    }
    
    /**
     * Reports errors with appropriate formatting.
     */
    private void reportError(LogData logData) {
        String icon = config.isUseIcons() ? ERROR_ICON + " " : "";
        String message = String.format("%sERROR: %s", 
            icon, logData.getErrorMessage());
        
        brobotLogger.log()
            .console(message)
            .metadata("type", "ERROR")
            .log();
    }
    
    /**
     * Reports performance warnings for slow operations.
     */
    private void reportPerformanceWarning(LogData logData) {
        String icon = config.isUseIcons() ? WARNING_ICON + " " : "";
        String actionType = extractActionType(logData);
        
        String message = String.format("%sPerformance Warning: %s took %dms (threshold: %dms)", 
            icon, actionType, logData.getDuration(), config.getPerformanceWarnThreshold());
        
        brobotLogger.log()
            .console(message)
            .metadata("type", "PERFORMANCE_WARNING")
            .metadata("action", actionType)
            .metadata("duration", logData.getDuration())
            .log();
    }
    
    /**
     * Extracts the action type from the log data.
     */
    private String extractActionType(LogData logData) {
        if (logData.getActionType() != null) {
            return logData.getActionType();
        }
        
        // Try to extract from description
        String description = logData.getDescription();
        if (description != null) {
            Matcher matcher = ACTION_PATTERN.matcher(description);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Extracts the target name from the log data.
     */
    private String extractTargetName(LogData logData) {
        String description = logData.getDescription();
        if (description != null) {
            Matcher matcher = ACTION_PATTERN.matcher(description);
            if (matcher.find()) {
                return matcher.group(2);
            }
        }
        
        return "unknown target";
    }
    
    /**
     * Extracts typed text from the log data.
     */
    private String extractTypeText(LogData logData) {
        // In a real implementation, this would extract from the ObjectCollection
        // or ActionResult stored in the log data
        String description = logData.getDescription();
        if (description != null && description.contains("\"")) {
            int start = description.indexOf("\"") + 1;
            int end = description.lastIndexOf("\"");
            if (end > start) {
                return description.substring(start, end);
            }
        }
        
        return "";
    }
    
    /**
     * Extracts match details from the log data.
     */
    private String extractMatchDetails(LogData logData) {
        // In a real implementation, this would extract from the ActionResult
        // For now, return a placeholder
        return null;
    }
    
    /**
     * Checks if an action type should be reported based on configuration.
     */
    private boolean shouldReportAction(String actionType) {
        switch (actionType.toUpperCase()) {
            case "FIND":
                return config.isReportFind();
            case "CLICK":
                return config.isReportClick();
            case "TYPE":
                return config.isReportType();
            case "DRAG":
                return config.isReportDrag();
            case "HIGHLIGHT":
                return config.isReportHighlight();
            default:
                return true; // Report unknown actions by default
        }
    }
}