package io.github.jspinak.brobot.actions.actionExecution.actionLifecycle;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ActionLifecycleManagement {
    private final Time time;

    public ActionLifecycleManagement(Time time) {
        this.time = time;
    }

    /**
     * Increments the number of completed repetitions of the action.
     * @param matches holds the ActionLifecycle object
     */
    public void incrementCompletedRepetitions(Matches matches) {
        matches.getActionLifecycle().incrementCompletedRepetitions();
    }

    public Duration getCurrentDuration(Matches matches) {
        LocalDateTime start = matches.getActionLifecycle().getStartTime();
        LocalDateTime end = time.now();
        return Duration.between(start, end);
    }

    public int getCompletedRepetitions(Matches matches) {
        return matches.getActionLifecycle().getCompletedRepetitions();
    }

    /**
     * Continue the action if
     * - the max repetitions has not been reached
     * - the time has not expired
     * @param matches holds the ActionLifecycle
     * @return true if the action should be continued, false otherwise
     */
    private boolean isTimeLeftAndMoreRepsAllowed(Matches matches) {
        ActionOptions actionOptions = matches.getActionOptions();
        int completedReps = getCompletedRepetitions(matches);
        if (completedReps == 0) return true;
        int maxReps = actionOptions.getMaxTimesToRepeatActionSequence();
        ActionOptions.Action action = actionOptions.getAction();
        if (action != ActionOptions.Action.FIND && completedReps >= maxReps) {
            return false;
        }
        double maxWait = actionOptions.getMaxWait();
        Duration duration = getCurrentDuration(matches);
        return duration.getSeconds() <= maxWait;
    }

    public boolean isOkToContinueAction(Matches matches, int numberOfImages) {
        boolean timeLeftAndMoreRepsAllowed = isTimeLeftAndMoreRepsAllowed(matches);
        boolean findFirstAndAtLeastOneMatchFound = isFindFirstAndAtLeastOneMatchFound(matches);
        boolean findEachFirstAndEachPatternFound = isFindEachFirstAndEachPatternFound(matches, numberOfImages);
        if (!timeLeftAndMoreRepsAllowed) return false;
        if (findFirstAndAtLeastOneMatchFound) return false;
        if (findEachFirstAndEachPatternFound) return false;
        return true;
    }

    public boolean isFindFirstAndAtLeastOneMatchFound(Matches matches) {
        return !matches.isEmpty() && matches.getActionOptions().getFind() == ActionOptions.Find.FIRST;
    }

    public boolean isFindEachFirstAndEachPatternFound(Matches matches, int numberOfPatterns) {
        if (matches.getActionOptions().getFind() != ActionOptions.Find.EACH) return false;
        if (matches.getActionOptions().getDoOnEach() != ActionOptions.DoOnEach.FIRST) return false;
        return areAllImagesFound(matches, numberOfPatterns);
    }

    private boolean areAllImagesFound(Matches matches, int numberOfPatterns) {
        int patternsFound = 0;
        List<Pattern> foundPatterns = new ArrayList<>();
        for (Match m : matches.getMatchList()) {
            if (!foundPatterns.contains(m.getPattern())) {
                foundPatterns.add(m.getPattern());
                patternsFound++;
            }
        }
        return patternsFound == numberOfPatterns;
    }

    public boolean printActionOnce(Matches matches) {
        ActionLifecycle actionLifecycle = matches.getActionLifecycle();
        if (actionLifecycle.isPrinted()) {
            return false;
        }
        actionLifecycle.setPrinted(true);
        ActionOptions actionOptions = matches.getActionOptions();
        if (Report.minReportingLevel(Report.OutputLevel.LOW)) {
            if (actionOptions.getAction() == ActionOptions.Action.FIND)
                System.out.format("Find.%s ", actionOptions.getFind());
            else
                System.out.format("%s ", actionOptions.getAction());
        }
        return true;
    }
}
