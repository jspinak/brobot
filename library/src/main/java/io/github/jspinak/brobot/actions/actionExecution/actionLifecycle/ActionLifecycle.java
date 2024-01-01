package io.github.jspinak.brobot.actions.actionExecution.actionLifecycle;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ActionLifecycle {

    private ActionOptions actionOptions;
    private LocalDateTime startTime;
    /**
     * This is the time the action was completed, not necessarily the allowed finish time.
     */
    private LocalDateTime endTime;
    /**
     * Allowed end time is the start time plus the max wait in seconds.
     */
    private LocalDateTime allowedEndTime;
    private int completedRepetitions = 0;
    private boolean printed = false;
    private boolean allImagesFound = false;

    public ActionLifecycle(ActionOptions actionOptions, LocalDateTime now) {
        this.actionOptions = actionOptions;
        this.startTime = now;
        long nanos = (long) (actionOptions.getMaxWait() * Math.pow(10, 9));
        this.allowedEndTime = startTime.plusNanos(nanos);
    }

    public void incrementCompletedRepetitions() {
        completedRepetitions++;
    }

}
