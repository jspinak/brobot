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
        int id = actionLifecylceRepo.add(actionLifecycle);
        actionOptions.setActionId(id);
        return id;
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
    public void setEndTime(int id, double maxWait) {
        actionLifecylceRepo.getActionLifecycles().get(id).setEndTime(timeWrapper.now().plusSeconds((long) maxWait));
    }

    public Duration getAndSetDuration(int id, double maxWait) {
        //setEndTime(id, maxWait);
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

    /**
     * Continue the action if
     * - the max repetitions has not been reached
     * - the time has not expired
     * @param id id of the ActionLifecycle object
     * @return true if the action should be continued, false otherwise
     */
    public boolean continueAction(int id) {
        int completedReps = getCompletedRepetitions(id);
        int maxReps = getActionOptions(id).getMaxTimesToRepeatActionSequence();
        ActionOptions.Action action = getActionOptions(id).getAction();
        if (action != ActionOptions.Action.FIND && completedReps >= maxReps) {
            return false;
        }
        double maxWait = getActionOptions(id).getMaxWait();
        Duration duration = getCurrentDuration(id);
        return duration.getSeconds() <= maxWait;
    }

    public boolean continueActionIfNotFound(int id, Matches matches) {
        if (!continueAction(id)) {
            //Report.println("continue action id is false");
            return false;
        }
        if (!matches.isEmpty()) {
            //Report.println("Found the element, stopping find");
            return false;
        }
        return true;
    }

    public boolean printActionOnce(int id) {
        if (actionLifecylceRepo.getActionLifecycles().get(id).isPrinted()) {
            return false;
        }
        actionLifecylceRepo.getActionLifecycles().get(id).setPrinted(true);
        if (Report.minReportingLevel(Report.OutputLevel.LOW)) {
            if (getActionOptions(id).getAction() == ActionOptions.Action.FIND)
                System.out.format("Find.%s ", getActionOptions(id).getFind());
            else
                System.out.format("%s ", getActionOptions(id).getAction());
        }
        return true;
    }
}
