package io.github.jspinak.brobot.logging.modular;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;

/**
 * Provides standard output for action results. Shows important information without overwhelming
 * detail.
 */
@Component
public class NormalFormatter implements ActionLogFormatter {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String format(ActionResult actionResult) {
        if (!shouldLog(actionResult)) {
            return null;
        }

        StringBuilder formatted = new StringBuilder();

        // Timestamp (if available)
        if (actionResult.getStartTime() != null) {
            formatted
                    .append("[")
                    .append(actionResult.getStartTime().format(TIME_FORMATTER))
                    .append("] ");
        }

        // Success/failure symbol
        formatted.append(actionResult.isSuccess() ? "✓" : "✗");
        formatted.append(" ");

        // Action type
        String actionType = extractActionType(actionResult);
        formatted.append(actionType);

        // Target information
        String target = buildTargetInfo(actionResult);
        if (target != null && !target.isEmpty()) {
            formatted.append(": ").append(target);
        }

        // Match count for find operations
        if (!actionResult.getMatchList().isEmpty()) {
            formatted.append(" (").append(actionResult.size()).append(" matches)");
        }

        // Duration for significant actions
        if (isSignificantAction(actionResult)) {
            if (actionResult.getDuration() != null) {
                formatted.append(" - ").append(formatDuration(actionResult.getDuration()));
            }
        }

        // Add failure reason if available and action failed
        if (!actionResult.isSuccess()
                && actionResult.getOutputText() != null
                && !actionResult.getOutputText().isEmpty()) {
            formatted.append("\n  → ").append(actionResult.getOutputText());
        }

        return formatted.toString();
    }

    @Override
    public boolean shouldLog(ActionResult actionResult) {
        if (actionResult == null || actionResult.getEndTime() == null) {
            return false;
        }

        // Always log failures
        if (!actionResult.isSuccess()) {
            return true;
        }

        // Log significant successful actions
        return isSignificantAction(actionResult);
    }

    @Override
    public ActionLogFormatter.VerbosityLevel getVerbosityLevel() {
        return VerbosityLevel.NORMAL;
    }

    private String extractActionType(ActionResult actionResult) {
        if (actionResult.getActionConfig() != null) {
            String className = actionResult.getActionConfig().getClass().getSimpleName();
            String type = className.replace("Options", "").replace("Config", "");

            // Capitalize appropriately
            if (type.length() > 0) {
                return type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
            }
            return type;
        }
        return "Action";
    }

    private boolean isSignificantAction(ActionResult actionResult) {
        // Check if it's a significant action type based on config
        if (actionResult.getActionConfig() != null) {
            String className =
                    actionResult.getActionConfig().getClass().getSimpleName().toLowerCase();

            // Significant actions include clicks, typing, dragging, etc.
            if (className.contains("click")
                    || className.contains("type")
                    || className.contains("drag")
                    || className.contains("scroll")) {
                return true;
            }

            // Find operations with matches are significant
            if (className.contains("find") && !actionResult.getMatchList().isEmpty()) {
                return true;
            }
        }

        // Actions that take more than 500ms are significant
        if (actionResult.getDuration() != null && actionResult.getDuration().toMillis() > 500) {
            return true;
        }

        // Actions that produce text output are significant
        if (actionResult.getText() != null && !actionResult.getText().isEmpty()) {
            return true;
        }

        return false;
    }

    private String buildTargetInfo(ActionResult actionResult) {
        // First try to get from log target name
        String targetName = actionResult.getLogTargetName();
        if (targetName != null && !targetName.equals("unknown")) {
            return targetName;
        }

        // If we have matches, use the first match name
        if (!actionResult.getMatchList().isEmpty()) {
            Match firstMatch = actionResult.getMatchList().get(0);
            if (firstMatch.getName() != null && !firstMatch.getName().isEmpty()) {
                return firstMatch.getName();
            }
        }

        // If we have extracted text, show a snippet
        if (actionResult.getText() != null && !actionResult.getText().isEmpty()) {
            String text = String.join(", ", actionResult.getText().getAll());
            if (text.length() > 30) {
                text = text.substring(0, 27) + "...";
            }
            return "\"" + text + "\"";
        }

        return "";
    }

    private String formatDuration(Duration duration) {
        long millis = duration.toMillis();
        if (millis < 1000) {
            return millis + "ms";
        } else {
            return String.format("%.1fs", millis / 1000.0);
        }
    }
}
