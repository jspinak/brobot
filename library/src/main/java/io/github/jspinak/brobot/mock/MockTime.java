package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.ActionDurations;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Keeps track of the mocked value of 'now' and performs mocked pauses.
 */
@Component
public class MockTime {
    private final ActionDurations actionDurations;
    private LocalDateTime now = LocalDateTime.now(); // keeps track of mock time

    public MockTime(ActionDurations actionDurations) {
        this.actionDurations = actionDurations;
    }

    /**
     * LocalDateTime is immutable, so the 'now' variable can be directly referenced for a deep copy.
     * @return the current time, either as the real current time or the mocked current time.
     */
    public LocalDateTime now() {
        return now;
    }

    public void wait(double seconds) {
        if (seconds <= 0) return;
        if (Report.minReportingLevel(Report.OutputLevel.HIGH))
            System.out.format("%s-%.1f ", "wait", seconds);
        long nanoTimeout = (long) (seconds * Math.pow(10, 9));
        now = now.plusNanos(nanoTimeout);
    }

    public void wait(ActionOptions.Action action) {
        wait(actionDurations.getActionDuration(action));
    }

    public void wait(ActionOptions.Find find) {
        wait(actionDurations.getFindDuration(find));
    }

}
