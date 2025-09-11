package io.github.jspinak.brobot.logging.modular;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Formatter for NORMAL verbosity level.
 *
 * <p>Produces balanced output with timestamps, key info, and success indicators. Format examples:
 * [12:34:56] ✓ Find Working.ClaudeIcon completed in 234ms (1 match) [12:34:56] ✗ Click
 * Button.Submit failed in 156ms (No matches found)
 */
@Component
public class NormalFormatter implements ActionLogFormatter {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String format(ActionResult actionResult) {
        if (!shouldLog(actionResult)) {
            return null;
        }

        ActionResult.ActionExecutionContext context = actionResult.getExecutionContext();
        if (context == null) {
            return null;
        }

        StringBuilder formatted = new StringBuilder();

        // Timestamp
        if (context.getEndTime() != null) {
            formatted
                    .append("[")
                    .append(
                            context.getEndTime()
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .format(TIME_FORMATTER))
                    .append("] ");
        }

        // Success/failure symbol
        formatted.append(context.isSuccess() ? "✓" : "✗");
        formatted.append(" ");

        // Action type
        String actionType = context.getActionType();
        if (actionType != null && !actionType.isEmpty()) {
            actionType = cleanActionType(actionType);
            formatted.append(actionType);
        } else {
            formatted.append("Action");
        }

        // Target information
        String target = buildTargetInfo(context);
        if (target != null && !target.isEmpty()) {
            formatted.append(" ").append(target);
        }

        // Completion status
        formatted.append(context.isSuccess() ? " completed" : " failed");

        // Duration
        if (context.getExecutionDuration() != null && !context.getExecutionDuration().isZero()) {
            formatted.append(" in ").append(context.getExecutionDuration().toMillis()).append("ms");
        }

        // Match information
        if (!context.getResultMatches().isEmpty()) {
            int matchCount = context.getResultMatches().size();
            formatted
                    .append(" (")
                    .append(matchCount)
                    .append(matchCount == 1 ? " match)" : " matches)");
        } else if (!context.isSuccess()) {
            formatted.append(" (No matches found)");
        }

        // Error information if available
        if (context.getExecutionError() != null) {
            formatted.append(" - ").append(context.getExecutionError().getMessage());
        }

        return formatted.toString();
    }

    @Override
    public boolean shouldLog(ActionResult actionResult) {
        if (actionResult == null) {
            return false;
        }

        ActionResult.ActionExecutionContext context = actionResult.getExecutionContext();
        if (context == null) {
            return false;
        }

        // Log completed actions and significant start events
        return context.getEndTime() != null
                || (context.getStartTime() != null && isSignificantAction(context));
    }

    @Override
    public VerbosityLevel getVerbosityLevel() {
        return VerbosityLevel.NORMAL;
    }

    /** Check if this is a significant action worth logging at start */
    private boolean isSignificantAction(ActionResult.ActionExecutionContext context) {
        // Log start events for actions with targets or specific action types
        return !context.getTargetImages().isEmpty()
                || !context.getTargetStrings().isEmpty()
                || !context.getTargetRegions().isEmpty()
                || (context.getActionType() != null
                        && context.getActionType().matches("(FIND|CLICK|TYPE|WAIT).*"));
    }

    /** Clean action type by removing suffixes */
    private String cleanActionType(String actionType) {
        if (actionType == null) {
            return "";
        }

        actionType = actionType.replaceAll("_(COMPLETE|FAILED|START)$", "");

        if (actionType.length() > 0) {
            return actionType.substring(0, 1).toUpperCase() + actionType.substring(1).toLowerCase();
        }

        return actionType;
    }

    /** Build detailed target information */
    private String buildTargetInfo(ActionResult.ActionExecutionContext context) {
        StringBuilder targetInfo = new StringBuilder();

        if (!context.getTargetImages().isEmpty()) {
            if (context.getTargetImages().size() == 1) {
                StateImage stateImage = context.getTargetImages().get(0);
                String imageName = stateImage.getName();
                String ownerState = stateImage.getOwnerStateName();

                if (ownerState != null && !ownerState.isEmpty() && !ownerState.equals("null")) {
                    targetInfo.append(ownerState).append(".");
                }

                if (imageName != null && !imageName.isEmpty()) {
                    targetInfo.append(imageName);
                } else {
                    targetInfo.append("Image");
                }
            } else {
                targetInfo.append("Images[").append(context.getTargetImages().size()).append("]");
            }
        } else if (!context.getTargetStrings().isEmpty()) {
            if (context.getTargetStrings().size() == 1) {
                String firstString = context.getTargetStrings().get(0);
                if (firstString != null && firstString.length() <= 30) {
                    targetInfo.append("\"").append(firstString).append("\"");
                } else if (firstString != null) {
                    targetInfo.append("\"").append(firstString.substring(0, 27)).append("...\"");
                }
            } else {
                targetInfo.append("Strings[").append(context.getTargetStrings().size()).append("]");
            }
        } else if (!context.getTargetRegions().isEmpty()) {
            targetInfo.append("Regions[").append(context.getTargetRegions().size()).append("]");
        } else if (context.getPrimaryTargetName() != null
                && !context.getPrimaryTargetName().isEmpty()) {
            targetInfo.append(context.getPrimaryTargetName());
        }

        return targetInfo.toString();
    }
}
