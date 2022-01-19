package io.github.jspinak.brobot.actions.methods.time;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Wraps the Time functions to allow for mocking actions.
 * Keeps track of the mocked value of 'now' and performs mocked pauses.
 */
@Component
public class TimeWrapper {

    private ActionDurations actionDurations;

    private LocalDateTime now = LocalDateTime.now(); // keeps track of mock time

    public TimeWrapper(ActionDurations actionDurations) {
        this.actionDurations = actionDurations;
    }

    /**
     * LocalDateTime is immutable, so the 'now' variable can be directly referenced for a deep copy.
     */
    public LocalDateTime now() {
        if (BrobotSettings.mock) return now;
        return LocalDateTime.now();
    }

    public void wait(double seconds) {
        long nanoTimeout = (long) (seconds * Math.pow(10, 9));
        now = now.plusNanos(nanoTimeout);
    }

    public void wait(ActionOptions.Action action) {
        wait(actionDurations.getActionDuration(action));
    }

    public void wait(ActionOptions.Find find) {
        wait(actionDurations.getFindDuration(find));
    }

    public void printNow() {
        System.out.print(now().format(DateTimeFormatter.ofPattern("mm:ss"))+" ");
    }

    public void goBackInTime(double years, Object thingsYouWishYouCouldChange) {
        now = now.minusYears((long)years);
        //change(thingsYouWishYouCouldChange); // seems difficult, maybe replace with an 'accept' method
    }
}
