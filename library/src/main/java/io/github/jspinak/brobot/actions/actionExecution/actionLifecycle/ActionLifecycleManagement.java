package io.github.jspinak.brobot.actions.actionExecution.actionLifecycle;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.GetTextUntil.*;

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

    public void incrementCompletedSequences(Matches matches) {
        matches.getActionLifecycle().incrementCompletedSequences();
    }

    public Duration getCurrentDuration(Matches matches) {
        LocalDateTime start = matches.getActionLifecycle().getStartTime();
        LocalDateTime end = time.now();
        return Duration.between(start, end);
    }

    public int getCompletedRepetitions(Matches matches) {
        return matches.getActionLifecycle().getCompletedRepetitions();
    }

    public boolean isMoreSequencesAllowed(Matches matches) {
        return matches.getActionOptions().getMaxTimesToRepeatActionSequence() >
                matches.getActionLifecycle().getCompletedSequences();
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
        if (isTextConditionAchieved(matches)) return false;
        return true;
    }

    public boolean isFindFirstAndAtLeastOneMatchFound(Matches matches) {
        return !matches.isEmpty() && matches.getActionOptions().getFind() == ActionOptions.Find.FIRST;
    }

    private boolean matchContainsText(Match match, String text) {
        if (text.isEmpty()) return !match.getText().isEmpty();
        return match.getText().contains(text);
    }

    private boolean isTextConditionAchieved(Matches matches) {
        ActionOptions.GetTextUntil condition = matches.getActionOptions().getGetTextUntil();
        if (condition == NONE) return false; // text is not used as an exit condition
        String textToFind = matches.getActionOptions().getTextToAppearOrVanish();
        boolean containsText;
        for (Match match : matches.getMatchList()) {
            containsText = matchContainsText(match, textToFind);
            if (condition == TEXT_APPEARS && containsText) return true;
            if (condition == TEXT_VANISHES && containsText) return false;
        }
        if (condition == TEXT_APPEARS) return false;
        return true;
    }

    public boolean isFindEachFirstAndEachPatternFound(Matches matches, int numberOfPatterns) {
        if (matches.getActionOptions().getFind() != ActionOptions.Find.EACH) return false;
        if (matches.getActionOptions().getDoOnEach() != ActionOptions.DoOnEach.FIRST) return false;
        return areAllImagesFound(matches, numberOfPatterns);
    }

    private boolean areAllImagesFound(Matches matches, int numberOfPatterns) {
        int patternsFound = 0;
        List<Image> foundImages = new ArrayList<>();
        for (Match m : matches.getMatchList()) {
            if (!foundImages.contains(m.getSearchImage())) {
                foundImages.add(m.getSearchImage());
                patternsFound++;
            }
        }
        return patternsFound == numberOfPatterns;
    }

    public void printActionOnce(Matches matches) {
        ActionLifecycle actionLifecycle = matches.getActionLifecycle();
        if (actionLifecycle.isPrinted()) {
            return;
        }
        actionLifecycle.setPrinted(true);
        ActionOptions actionOptions = matches.getActionOptions();
        if (Report.minReportingLevel(Report.OutputLevel.LOW)) {
            if (actionOptions.getAction() == ActionOptions.Action.FIND)
                System.out.format("Find.%s ", actionOptions.getFind());
            else
                System.out.format("%s ", actionOptions.getAction());
        }
    }
}
