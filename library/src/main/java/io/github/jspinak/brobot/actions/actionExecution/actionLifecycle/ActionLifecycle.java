package io.github.jspinak.brobot.actions.actionExecution.actionLifecycle;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ActionLifecycle {

    private ActionOptions actionOptions;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int completedRepetitions = 0;

    public ActionLifecycle(ActionOptions actionOptions, LocalDateTime now) {
        this.actionOptions = actionOptions;
        this.startTime = now;
    }

    public void incrementCompletedRepetitions() {
        completedRepetitions++;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

}
