package io.github.jspinak.brobot.logging.modular;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;

/**
 * Provides comprehensive, detailed output for action results. Includes all available information
 * about the action execution.
 */
@Component
public class VerboseFormatter implements ActionLogFormatter {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    @Override
    public String format(ActionResult actionResult) {
        if (!shouldLog(actionResult)) {
            return null;
        }

        StringBuilder formatted = new StringBuilder();

        // Build header line with timestamp, status, and action type
        formatted.append(buildHeaderLine(actionResult));

        // Add detailed information sections
        formatted.append(buildTargetSection(actionResult));
        formatted.append(buildResultSection(actionResult));
        formatted.append(buildTimingSection(actionResult));
        formatted.append(buildMatchSection(actionResult));

        return formatted.toString();
    }

    private String buildHeaderLine(ActionResult actionResult) {
        StringBuilder header = new StringBuilder();

        // Timestamp
        if (actionResult.getStartTime() != null) {
            header.append("[")
                    .append(actionResult.getStartTime().format(TIME_FORMATTER))
                    .append("] ");
        }

        // Status symbol and text
        if (actionResult.isSuccess()) {
            header.append("✓ SUCCESS: ");
        } else {
            header.append("✗ FAILED: ");
        }

        // Action type
        String actionType = extractActionType(actionResult);
        header.append(actionType);

        header.append("\n");
        return header.toString();
    }

    private String buildTargetSection(ActionResult actionResult) {
        StringBuilder section = new StringBuilder();

        // Get target info from matches
        String targetName = actionResult.getLogTargetName();
        if (targetName != null && !targetName.equals("unknown")) {
            section.append("  Target: ").append(targetName).append("\n");
        }

        // Add match count if available
        if (!actionResult.getMatchList().isEmpty()) {
            section.append("  Matches Found: ").append(actionResult.size()).append("\n");
        }

        return section.toString();
    }

    private String buildResultSection(ActionResult actionResult) {
        StringBuilder section = new StringBuilder();

        // Output text if available
        if (actionResult.getOutputText() != null && !actionResult.getOutputText().isEmpty()) {
            section.append("  Output: ").append(actionResult.getOutputText()).append("\n");
        }

        // Text result if available
        if (actionResult.getText() != null && !actionResult.getText().isEmpty()) {
            String text = String.join(", ", actionResult.getText().getAll());
            if (text.length() > 100) {
                text = text.substring(0, 97) + "...";
            }
            section.append("  Extracted Text: \"").append(text).append("\"\n");
        }

        return section.toString();
    }

    private String buildTimingSection(ActionResult actionResult) {
        StringBuilder section = new StringBuilder();

        if (actionResult.getDuration() != null) {
            Duration duration = actionResult.getDuration();
            section.append("  Duration: ").append(formatDuration(duration)).append("\n");

            // Add performance indicator for slow actions
            if (duration.toMillis() > 1000) {
                section.append("  ⚠ Slow Action (>1s)\n");
            }
        }

        // Start and end times
        if (actionResult.getStartTime() != null) {
            section.append("  Started: ")
                    .append(actionResult.getStartTime().format(TIME_FORMATTER))
                    .append("\n");
        }
        if (actionResult.getEndTime() != null) {
            section.append("  Ended: ")
                    .append(actionResult.getEndTime().format(TIME_FORMATTER))
                    .append("\n");
        }

        return section.toString();
    }

    private String buildMatchSection(ActionResult actionResult) {
        if (actionResult.getMatchList().isEmpty()) {
            return "";
        }

        StringBuilder section = new StringBuilder();
        section.append("  Match Details:\n");

        int count = 0;
        for (Match match : actionResult.getMatchList()) {
            count++;
            if (count > 5) {
                section.append("    ... and ").append(actionResult.size() - 5).append(" more\n");
                break;
            }

            section.append("    [").append(count).append("] ");
            section.append("Score: ").append(String.format("%.2f", match.getScore()));
            section.append(" @ (").append(match.x()).append(",").append(match.y()).append(")");
            section.append(" Size: ").append(match.w()).append("x").append(match.h());

            if (match.getName() != null && !match.getName().isEmpty()) {
                section.append(" - ").append(match.getName());
            }
            section.append("\n");
        }

        return section.toString();
    }

    @Override
    public boolean shouldLog(ActionResult actionResult) {
        // Log all actions in verbose mode
        return actionResult != null;
    }

    @Override
    public ActionLogFormatter.VerbosityLevel getVerbosityLevel() {
        return VerbosityLevel.VERBOSE;
    }

    private String extractActionType(ActionResult actionResult) {
        if (actionResult.getActionConfig() != null) {
            String className = actionResult.getActionConfig().getClass().getSimpleName();
            return className.replace("Options", "").replace("Config", "").toUpperCase();
        }
        return "ACTION";
    }

    private String formatDuration(Duration duration) {
        long millis = duration.toMillis();
        if (millis < 1000) {
            return millis + "ms";
        } else if (millis < 60000) {
            return String.format("%.2fs", millis / 1000.0);
        } else {
            long minutes = millis / 60000;
            long seconds = (millis % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
}
