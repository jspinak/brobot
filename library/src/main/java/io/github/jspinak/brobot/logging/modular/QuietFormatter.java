package io.github.jspinak.brobot.logging.modular;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;

/**
 * Provides minimal, clean output for action results.
 * Only logs completed actions with basic information.
 */
@Component
public class QuietFormatter implements ActionLogFormatter {

    // Removed the first format method that has no @Override annotation

    @Override
    public String format(ActionResult actionResult) {
        if (!shouldLog(actionResult)) {
            return null;
        }

        StringBuilder formatted = new StringBuilder();

        // Success/failure symbol
        formatted.append(actionResult.isSuccess() ? "✓" : "✗");
        formatted.append(" ");

        // Action type from config if available
        String actionType = "Action";
        if (actionResult.getActionConfig() != null) {
            actionType = actionResult.getActionConfig().getClass().getSimpleName()
                    .replace("Options", "")
                    .replace("Config", "");
            actionType = cleanActionType(actionType);
        }
        formatted.append(actionType);

        // Target information
        String target = buildTargetInfo(actionResult);
        if (target != null && !target.isEmpty()) {
            formatted.append(" ").append(target);
        }

        // Duration - always show it
        if (actionResult.getDuration() != null) {
            formatted.append(" • ").append(actionResult.getDuration().toMillis()).append("ms");
        } else {
            // If duration is missing, show 0ms
            formatted.append(" • 0ms");
        }

        return formatted.toString();
    }

    @Override
    public boolean shouldLog(ActionResult actionResult) {
        if (actionResult == null) {
            return false;
        }

        // Only log completed actions (success or failure)
        // Skip start events and incomplete actions
        return actionResult.getEndTime() != null;
    }

    @Override
    public VerbosityLevel getVerbosityLevel() {
        return VerbosityLevel.QUIET;
    }

    /** Clean action type by removing suffixes like _COMPLETE, _FAILED, _START */
    private String cleanActionType(String actionType) {
        if (actionType == null) {
            return "";
        }

        // Remove common suffixes
        actionType = actionType.replaceAll("_(COMPLETE|FAILED|START)$", "");

        // Capitalize first letter, lowercase rest
        if (actionType.length() > 0) {
            return actionType.substring(0, 1).toUpperCase() + actionType.substring(1).toLowerCase();
        }

        return actionType;
    }

    /** Build target information string from ActionResult */
    private String buildTargetInfo(ActionResult actionResult) {
        // Use the log target name method which gets info from matches
        String targetName = actionResult.getLogTargetName();
        if (targetName != null && !targetName.equals("unknown")) {
            return targetName;
        }

        // If no matches, return empty
        return "";
    }

    /** Format duration for display */
    private String formatDuration(Duration duration) {
        long millis = duration.toMillis();
        if (millis < 1000) {
            return millis + "ms";
        } else {
            return String.format("%.2fs", millis / 1000.0);
        }
    }
}