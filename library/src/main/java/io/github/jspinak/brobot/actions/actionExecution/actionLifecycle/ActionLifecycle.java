package io.github.jspinak.brobot.actions.actionExecution.actionLifecycle;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ActionLifecycle {

    private LocalDateTime startTime;
    /*
     * This is the time the action was completed, not necessarily the allowed finish time.
     */
    private LocalDateTime endTime;
    /*
     * Allowed end time is the start time plus the max wait in seconds.
     */
    private LocalDateTime allowedEndTime;
    private int completedRepetitions = 0;
    private int completedSequences = 0;
    private boolean printed = false;

    public ActionLifecycle(LocalDateTime now, double maxWait) {
        this.startTime = now;
        long nanos = (long) (maxWait * Math.pow(10, 9));
        this.allowedEndTime = startTime.plusNanos(nanos);
    }

    public void incrementCompletedRepetitions() {
        completedRepetitions++;
    }

    public void incrementCompletedSequences() { completedSequences++; }

}
