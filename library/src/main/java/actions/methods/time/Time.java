package actions.methods.time;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Deals with Action durations, both real and mock.
 */
@Component
public class Time {

    private TimeWrapper timeWrapper;

    /**
     * Action start times are set from the Action class, as the Action is called.
     * Many Actions call a Find operation within the Action. These Find operations
     * produce MatchObjects and need to set their start times.
     * This is done with the findStartTime field.
     *
     * Snapshots are set at the end of the Action. For a Vanish Action, for example,
     * we want to know how long it takes for a Vanish to succeed. This will be useful
     * if we perform another Vanish operation and want to know the average wait time,
     * or are using the MatchHistory for mocks. On the other hand, ever Find operation
     * gives us useful information about Images, and we could also save this information.
     * Find operation Durations should be measured from the start of the Find operation
     * and not from the start of the Action.
     * On the other hand, we want MatchSnapshots to be representative of how
     * Images will respond in real scenarios, and saving Snapshots in scenarios where
     * we are waiting a while for an Image to appear will skew the distribution of
     * successful and unsuccessful matches. It is sufficient for us to have Snapshots saved
     * for Find Actions and not every individual Find operation, especially if the Duration
     * is also saved. One scenario where this may not be optimal is when we always use
     * an Image with an Action other than Find, meaning that we won't have a Find Action MatchSnapshot
     * for the Image, and we won't have any data with which to mock finding
     * this Image. Of course, if we never use a Find Action on this Image, it is unlikely that
     * a Find Action will occur in real execution. The Image will be used with other Actions,
     * and these Actions will have MatchSnapshots.
     * The startTime field is used for miscellaneous operations and is set by the user.
     */
    private Map<ActionOptions.Action, LocalDateTime> startTimes = new HashMap<>();
    private LocalDateTime findStartTime = LocalDateTime.now();
    private LocalDateTime startTime = LocalDateTime.now(); // when not using Actions

    public Time(TimeWrapper timeWrapper) {
        this.timeWrapper = timeWrapper;
    }

    public void setStartTime(ActionOptions.Action action) {
        startTimes.put(action, timeWrapper.now());
    }

    public void setFindStartTime() {
        findStartTime = timeWrapper.now();
    }

    public void setStartTime() {
        startTime = timeWrapper.now();
    }

    private long getNano(double seconds) {
        return (long) (seconds * Math.pow(10, 9));
    }

    public boolean expired(ActionOptions.Action action, double maxWait) {
        return timeWrapper.now().isAfter(startTimes.get(action).plusNanos(getNano(maxWait)));
    }

    public boolean findExpired(double maxWait) {
        return timeWrapper.now().isAfter(findStartTime.plusNanos(getNano(maxWait)));
    }

    public boolean expired(double maxWait) {
        return timeWrapper.now().isAfter(startTime.plusNanos(getNano(maxWait)));
    }

    public Duration getDuration(ActionOptions.Action action) {
        return Duration.between(startTimes.get(action), timeWrapper.now());
    }

    public Duration getDuration() {
        return Duration.between(startTime, timeWrapper.now());
    }

    public void printNow() {
        timeWrapper.printNow();
    }

}
