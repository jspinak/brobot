package io.github.jspinak.brobot.tools.logging.console;

import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Console reporter for action logs that provides real-time feedback about automation actions being
 * performed. Converts structured LogData into human-readable console output with configurable
 * verbosity levels.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Visual indicators for success/failure (✓/✗)
 *   <li>Timing information for performance analysis
 *   <li>Match details including location and score
 *   <li>Configurable verbosity levels
 *   <li>Colored output support (when enabled)
 * </ul>
 *
 * @see LogData for the log entry structure
 * @see ConsoleActionConfig for configuration options
 */
@Component
@ConditionalOnProperty(
        prefix = "brobot.console.actions",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
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
    private static final String WARNING_ICON = "⚠";
    private static final String ERROR_ICON = "✗";
    private static final String INFO_ICON = "ℹ";
    private static final String ACTION_ICON = "▶";
    private static final String TRANSITION_ICON = "→";
    private static final String DEBUG_ICON = "•";

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

    /** Reports an action to the console with appropriate formatting. */
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

    /** Reports a FIND action with match details. */
    private void reportFind(LogData logData) {
        String target = extractTargetName(logData);
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();

        if (config.getLevel() == ConsoleActionConfig.Level.QUIET) {
            // Minimal format: ✓/✗ Find State.Object • 234ms
            String location = "";
            // Location info would come from ActionResult if available
            // For now, we'll skip location in minimal mode

            String message =
                    String.format(
                            "%s Find %s%s • %dms",
                            success ? SUCCESS_ICON : FAILURE_ICON, target, location, duration);

            brobotLogger.log().observation(message).log();

        } else if (config.getLevel() == ConsoleActionConfig.Level.NORMAL) {
            // Current two-line format for NORMAL mode
            String startMessage = String.format("%s Find_START: ", ACTION_ICON);
            brobotLogger.log().observation(startMessage).log();

            String completeMessage =
                    String.format(
                            "%s Find_COMPLETE:  %s",
                            ACTION_ICON, success ? SUCCESS_ICON : FAILURE_ICON);

            brobotLogger.log().observation(completeMessage).log();

        } else { // VERBOSE
            // For now, use existing verbose format
            reportFindVerboseFormat(logData);
        }
    }

    /** Reports a CLICK action. */
    private void reportClick(LogData logData) {
        String target = extractTargetName(logData);
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();

        if (config.getLevel() == ConsoleActionConfig.Level.QUIET) {
            // Minimal format: ✓/✗ Click State.Object @ (x,y) • 201ms
            String location = "";
            String failureReason = "";

            // Location info would come from ActionResult if available
            if (!success) {
                failureReason = " • Target not found";
            }

            String message =
                    String.format(
                            "%s Click %s%s%s • %dms",
                            success ? SUCCESS_ICON : FAILURE_ICON,
                            target,
                            location,
                            failureReason,
                            duration);

            brobotLogger.log().observation(message).log();

        } else if (config.getLevel() == ConsoleActionConfig.Level.NORMAL) {
            String startMessage = String.format("%s Click_START: ", ACTION_ICON);
            brobotLogger.log().observation(startMessage).log();

            String completeMessage =
                    String.format(
                            "%s Click_COMPLETE:  %s",
                            ACTION_ICON, success ? SUCCESS_ICON : FAILURE_ICON);

            brobotLogger.log().observation(completeMessage).log();

        } else { // VERBOSE
            reportClickVerboseFormat(logData);
        }
    }

    /** Reports a TYPE action. */
    private void reportType(LogData logData) {
        String text = extractTypeText(logData);
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();

        if (config.getLevel() == ConsoleActionConfig.Level.QUIET) {
            // Minimal format: ✓/✗ Type "text" • 14 chars • 702ms
            String displayText = text;
            if (text.length() > 30) {
                displayText = text.substring(0, 27) + "...";
            }

            // Handle password fields - would need to check action options
            // For now, we'll show truncated text

            String message =
                    String.format(
                            "%s Type \"%s\" • %d chars • %dms",
                            success ? SUCCESS_ICON : FAILURE_ICON,
                            displayText,
                            text.length(),
                            duration);

            brobotLogger.log().observation(message).log();

        } else if (config.getLevel() == ConsoleActionConfig.Level.NORMAL) {
            String startMessage = String.format("%s Type_START: ", ACTION_ICON);
            brobotLogger.log().observation(startMessage).log();

            String completeMessage =
                    String.format(
                            "%s Type_COMPLETE:  %s",
                            ACTION_ICON, success ? SUCCESS_ICON : FAILURE_ICON);

            brobotLogger.log().observation(completeMessage).log();

        } else { // VERBOSE
            reportTypeVerboseFormat(logData);
        }
    }

    /** Reports a DRAG action. */
    private void reportDrag(LogData logData) {
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();
        String target = extractTargetName(logData);

        if (config.getLevel() == ConsoleActionConfig.Level.QUIET) {
            // Minimal format: ✓/✗ Drag State.Object • 234ms
            String message =
                    String.format(
                            "%s Drag %s • %dms",
                            success ? SUCCESS_ICON : FAILURE_ICON, target, duration);

            brobotLogger.log().observation(message).log();

        } else if (config.getLevel() == ConsoleActionConfig.Level.NORMAL) {
            String startMessage = String.format("%s Drag_START: ", ACTION_ICON);
            brobotLogger.log().observation(startMessage).log();

            String completeMessage =
                    String.format(
                            "%s Drag_COMPLETE:  %s",
                            ACTION_ICON, success ? SUCCESS_ICON : FAILURE_ICON);

            brobotLogger.log().observation(completeMessage).log();

        } else { // VERBOSE
            reportDragVerboseFormat(logData);
        }
    }

    /** Reports a generic action. */
    private void reportGenericAction(LogData logData) {
        String actionType = extractActionType(logData);
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();
        String target = extractTargetName(logData);

        if (config.getLevel() == ConsoleActionConfig.Level.QUIET) {
            // Minimal format: ✓/✗ Action State.Object • 234ms
            String message =
                    String.format(
                            "%s %s %s • %dms",
                            success ? SUCCESS_ICON : FAILURE_ICON, actionType, target, duration);

            brobotLogger.log().observation(message).log();

        } else if (config.getLevel() == ConsoleActionConfig.Level.NORMAL) {
            String startMessage = String.format("%s %s_START: ", ACTION_ICON, actionType);
            brobotLogger.log().observation(startMessage).log();

            String completeMessage =
                    String.format(
                            "%s %s_COMPLETE:  %s",
                            ACTION_ICON, actionType, success ? SUCCESS_ICON : FAILURE_ICON);

            brobotLogger.log().observation(completeMessage).log();

        } else { // VERBOSE
            reportGenericActionVerboseFormat(logData);
        }
    }

    /** Reports match details for successful finds. */
    private void reportMatchDetails(LogData logData) {
        // Parse match details from description or other fields
        // This is a simplified version - in reality, we'd need to extract
        // match data from the ActionResult

        String details = extractMatchDetails(logData);
        if (details != null && !details.isEmpty()) {
            brobotLogger.log().observation("   └─ " + details).log();
        }
    }

    /** Reports details about why a find failed. */
    private void reportFindFailureDetails(LogData logData) {
        if (config.getLevel() == ConsoleActionConfig.Level.VERBOSE) {
            String errorMessage = logData.getErrorMessage();
            if (errorMessage != null && !errorMessage.isEmpty()) {
                brobotLogger.log().observation("   └─ " + errorMessage).log();
            }
        }
    }

    /** Reports state transitions. */
    private void reportTransition(LogData logData) {
        if (!config.isReportTransitions()) {
            return;
        }

        String from = logData.getFromStates() != null ? logData.getFromStates() : "Unknown";
        List<String> to = logData.getToStateNames();
        String toStates = to != null && !to.isEmpty() ? String.join(", ", to) : "Unknown";
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();

        if (config.getLevel() == ConsoleActionConfig.Level.QUIET) {
            // Minimal format: ✓/✗ State transition: Working → Prompt • 1.2s
            String message =
                    String.format(
                            "%s State transition: %s %s %s • %.1fs",
                            success ? SUCCESS_ICON : FAILURE_ICON,
                            from,
                            TRANSITION_ICON,
                            toStates,
                            duration / 1000.0);

            brobotLogger.log().observation(message).log();

        } else {
            String icon = config.isUseIcons() ? TRANSITION_ICON + " " : "";
            String message =
                    String.format(
                            "%sSTATE: %s -> %s [%dms] [%s]",
                            icon, from, toStates, duration, success ? "SUCCESS" : "FAILED");

            brobotLogger
                    .log()
                    .observation(message)
                    .metadata("type", "TRANSITION")
                    .metadata("from", from)
                    .metadata("to", toStates)
                    .metadata("success", success)
                    .log();
        }
    }

    /** Reports errors with appropriate formatting. */
    private void reportError(LogData logData) {
        String icon = FAILURE_ICON + " ";
        String message = String.format("%sERROR: %s", icon, logData.getErrorMessage());

        brobotLogger.log().observation(message).metadata("type", "ERROR").log();
    }

    /** Reports performance warnings for slow operations. */
    private void reportPerformanceWarning(LogData logData) {
        String icon = WARNING_ICON + " ";
        String actionType = extractActionType(logData);

        String message =
                String.format(
                        "%sPerformance Warning: %s took %dms (threshold: %dms)",
                        icon,
                        actionType,
                        logData.getDuration(),
                        config.getPerformanceWarnThreshold());

        brobotLogger
                .log()
                .observation(message)
                .metadata("type", "PERFORMANCE_WARNING")
                .metadata("action", actionType)
                .metadata("duration", logData.getDuration())
                .log();
    }

    /** Extracts the action type from the log data. */
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

    /** Extracts the target name from the log data. */
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

    /** Extracts typed text from the log data. */
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

    /** Extracts match details from the log data. */
    private String extractMatchDetails(LogData logData) {
        // In a real implementation, this would extract from the ActionResult
        // For now, return a placeholder
        return null;
    }

    /** Checks if an action type should be reported based on configuration. */
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

    // Verbose format methods - these will eventually be moved to VerboseConsoleReporter

    private void reportFindVerboseFormat(LogData logData) {
        String target = extractTargetName(logData);
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();

        brobotLogger.log().observation(String.format("▶ Find [%s]", target)).log();

        brobotLogger
                .log()
                .observation(String.format("├─ ⏱ Started: %s", formatTime(logData.getTimestamp())))
                .log();

        if (success) {
            brobotLogger.log().observation("├─ ✓ Match Found").log();
        } else {
            brobotLogger.log().observation("├─ ✗ No Match Found").log();
        }

        brobotLogger
                .log()
                .observation(
                        String.format("└─ %s Find Complete [%dms]", success ? "✓" : "✗", duration))
                .log();
    }

    private void reportClickVerboseFormat(LogData logData) {
        String target = extractTargetName(logData);
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();

        brobotLogger.log().observation(String.format("▶ Click [%s]", target)).log();

        if (success) {
            brobotLogger.log().observation("├─ 🖱 Button: LEFT").log();
        }

        brobotLogger
                .log()
                .observation(
                        String.format("└─ %s Click Complete [%dms]", success ? "✓" : "✗", duration))
                .log();
    }

    private void reportTypeVerboseFormat(LogData logData) {
        String text = extractTypeText(logData);
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();

        brobotLogger
                .log()
                .observation(
                        String.format(
                                "▶ Type [\"%s\"]",
                                text.length() > 30 ? text.substring(0, 27) + "..." : text))
                .log();

        brobotLogger
                .log()
                .observation(String.format("├─ 📝 Text Length: %d characters", text.length()))
                .log();

        brobotLogger
                .log()
                .observation(
                        String.format("└─ %s Type Complete [%dms]", success ? "✓" : "✗", duration))
                .log();
    }

    private void reportDragVerboseFormat(LogData logData) {
        String target = extractTargetName(logData);
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();

        brobotLogger.log().observation(String.format("▶ Drag [%s]", target)).log();

        brobotLogger
                .log()
                .observation(
                        String.format("└─ %s Drag Complete [%dms]", success ? "✓" : "✗", duration))
                .log();
    }

    private void reportGenericActionVerboseFormat(LogData logData) {
        String actionType = extractActionType(logData);
        String target = extractTargetName(logData);
        boolean success = logData.isSuccess();
        long duration = logData.getDuration();

        brobotLogger.log().observation(String.format("▶ %s [%s]", actionType, target)).log();

        brobotLogger
                .log()
                .observation(
                        String.format(
                                "└─ %s %s Complete [%dms]",
                                success ? "✓" : "✗", actionType, duration))
                .log();
    }

    private String formatTime(Instant timestamp) {
        if (timestamp == null) return "N/A";
        // Simple time format - can be enhanced later
        return timestamp.toString();
    }
}
