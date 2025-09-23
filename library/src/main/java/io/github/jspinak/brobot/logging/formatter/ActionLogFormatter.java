package io.github.jspinak.brobot.logging.formatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.correlation.ActionSessionManager;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;

import lombok.RequiredArgsConstructor;

/**
 * Formats action logs in a concise, consistent manner for all Brobot actions. Provides one-line
 * summaries with essential information only.
 *
 * <p>This formatter is used by the framework to provide consistent logging across all action types.
 * Applications can use this directly or through the ActionLoggingService for session-based logging.
 */
@Component
@RequiredArgsConstructor
public class ActionLogFormatter {

    private final BrobotLogger logger;

    @Autowired(required = false)
    private ActionSessionManager sessionManager;

    /**
     * Log a completed action with its results
     *
     * @param actionType The type of action performed (e.g., "CLICK", "FIND")
     * @param result The action result
     * @param collections The object collections involved in the action
     */
    public void logAction(String actionType, ActionResult result, ObjectCollection... collections) {
        if (result == null) return;

        String target = getTargetDescription(collections);
        String location = getLocationString(result);
        double similarity = getHighestSimilarity(result);
        long duration = result.getDuration() != null ? result.getDuration().toMillis() : 0;

        if (result.isSuccess()) {
            String message =
                    String.format(
                            "✓ %s %s | loc:%s | sim:%.2f | %dms",
                            actionType, target, location, similarity, duration);
            logger.info(LogCategory.ACTIONS, message);
        } else {
            String message =
                    String.format("✗ %s %s | NOT FOUND | %dms", actionType, target, duration);
            logger.warn(LogCategory.ACTIONS, message);
        }
    }

    /**
     * Log an action attempt before execution
     *
     * @param actionType The type of action being attempted
     * @param collections The object collections involved in the action
     */
    public void logAttempt(String actionType, ObjectCollection... collections) {
        // Automatically increment sequence if we're in a session
        if (sessionManager != null && sessionManager.hasActiveSession()) {
            sessionManager.incrementSequence();
        }

        String target = getTargetDescription(collections);
        String message = String.format("→ %s %s", actionType, target);
        logger.debug(LogCategory.ACTIONS, message);
    }

    private String getTargetDescription(ObjectCollection... collections) {
        if (collections == null || collections.length == 0) return "[no target]";

        ObjectCollection first = collections[0];
        if (!first.getStateImages().isEmpty()) {
            return first.getStateImages().get(0).getName();
        } else if (!first.getStateLocations().isEmpty()) {
            StateLocation loc = first.getStateLocations().get(0);
            return loc.getName() != null ? loc.getName() : loc.getLocation().toString();
        } else if (!first.getStateRegions().isEmpty()) {
            StateRegion reg = first.getStateRegions().get(0);
            return "region:"
                    + (reg.getName() != null ? reg.getName() : reg.getSearchRegion().toString());
        }
        return "[unknown]";
    }

    private String getLocationString(ActionResult result) {
        if (result.getMatchList().isEmpty()) return "none";

        Match firstMatch = result.getMatchList().get(0);
        Location target = firstMatch.getTarget();
        return String.format("(%d,%d)", target.getX(), target.getY());
    }

    private double getHighestSimilarity(ActionResult result) {
        return result.getMatchList().stream().mapToDouble(Match::getScore).max().orElse(0.0);
    }
}
