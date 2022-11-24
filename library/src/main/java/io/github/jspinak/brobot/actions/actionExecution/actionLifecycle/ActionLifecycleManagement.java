package io.github.jspinak.brobot.actions.actionExecution.actionLifecycle;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.TimeWrapper;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class ActionLifecycleManagement {

    private final ActionLifecylceRepo actionLifecylceRepo;
    private TimeWrapper timeWrapper;

    public ActionLifecycleManagement(ActionLifecylceRepo actionLifecylceRepo, TimeWrapper timeWrapper) {
        this.actionLifecylceRepo = actionLifecylceRepo;
        this.timeWrapper = timeWrapper;
    }

    /**
     * Creates a new ActionLifecycle object and adds it to the repo.
     * @param actionOptions ActionOptions object that contains all the information about the action.
     * @return id of the created ActionLifecycle object
     */
    public int newActionLifecycle(ActionOptions actionOptions) {
        ActionLifecycle actionLifecycle = new ActionLifecycle(actionOptions, timeWrapper.now());
        return actionLifecylceRepo.add(actionLifecycle);
    }

    /**
     * Increments the number of completed repetitions of the action.
     * @param id id of the ActionLifecycle object
     */
    public void incrementCompletedRepetitions(int id) {
        actionLifecylceRepo.getActionLifecycles().get(id).incrementCompletedRepetitions();
    }

    /**
     * Sets the end time of the action.
     * @param id id of the ActionLifecycle object
     */
    public void setEndTime(int id) {
        actionLifecylceRepo.getActionLifecycles().get(id).setEndTime(timeWrapper.now());
    }

    public Duration getAndSetDuration(int id) {
        actionLifecylceRepo.getActionLifecycles().get(id).setEndTime(timeWrapper.now());
        LocalDateTime start = actionLifecylceRepo.getActionLifecycles().get(id).getStartTime();
        LocalDateTime end = actionLifecylceRepo.getActionLifecycles().get(id).getEndTime();
        return Duration.between(start, end);
    }

    public Duration getCurrentDuration(int id) {
        LocalDateTime start = actionLifecylceRepo.getActionLifecycles().get(id).getStartTime();
        LocalDateTime end = timeWrapper.now();
        return Duration.between(start, end);
    }

    public boolean actionCompleted(int id) {
        return actionLifecylceRepo.getActionLifecycles().get(id).getEndTime() != null;
    }

    public int getCompletedRepetitions(int id) {
        return actionLifecylceRepo.getActionLifecycles().get(id).getCompletedRepetitions();
    }

    public ActionOptions getActionOptions(int id) {
        return actionLifecylceRepo.getActionLifecycles().get(id).getActionOptions();
    }

    public boolean continueAction(int id) {
        if (getCompletedRepetitions(id) >= getActionOptions(id).getMaxTimesToRepeatActionSequence()) {
            return false;
        }
        return getAndSetDuration(id).compareTo(Duration.ofSeconds((long) getActionOptions(id).getMaxWait())) <= 0;
    }

    public boolean continueActionIfNotFound(int id, Matches matches) {
        if (!continueAction(id)) return false;
        return matches.isEmpty();
    }
}
