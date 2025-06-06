package io.github.jspinak.brobot.datatypes.primitives.match;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.primatives.enums.SpecialStateType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.sikuli.script.Screen;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Find.UNIVERSAL;

/**
 * MatchSnapshots record a match (or failed match) and the search options at a single point in time.
 * They are used to build a MatchHistory to allow for more accurate mocking. The idea is to have a
 * history of examples from specific situations that give an accurate picture of what may occur
 * in a similar situation in the future. For this reason, only one Snapshot is created in situations
 * where an Image is searched multiple times. Actions that search multiple times until a condition is
 * met are good examples of such situations. Snapshots are kept in the Matches object as DanglingSnapshots
 * until the Action finishes, and only then saved to the Image or StateRegion.
 *
 * When mocking, it is best to use Snapshots of the same Action. Otherwise, a FIND operation
 * may have a higher probability of failure if the same Image was used often for a VANISH operation
 * and frequently not found.
 *
 * A Snapshot with no Match objects records a failed search.
 *
 * How do Snapshots deal with Find.ALL and Find.EACH operations? MatchHistory has a method to return
 *   a list with Snapshots from the Find.ALL and Find.EACH operations.
 *   This is done for the following reasons:
 * 1. Multiple Match Snapshots are used for mocking Find.ALL and Find.EACH Actions. This is why
 *    there is a separate list for these Snapshots.
 * 2. The best Match can be taken from multiple Match objects to simulate a Find.FIRST or Find.BEST
 *    operation. This is why there is a method that returns Snapshots for all Find operations.
 *
 * What happens with a Match that is passed to an ObjectCollection, or a Match that is used multiple
 *   times within an Action (for example, GetText may find multiple Strings with the same Match)?
 *   This Match shouldn't be recorded again in a separate Snapshot because it was only found once.
 *   Found Strings are added to the Text field of the same Match. TimeStamp is for the Match
 *   and not for the Strings in Text.
 *
 * GetText, when used with Find.ALL, returns a series of non-null Strings that are saved in the Text object.
 *
 *      * Action start times are set from the Action class, as the Action is called.
 *      * Many Actions call a Find operation within the Action. These Find operations
 *      * produce MatchObjects and need to set their start times.
 *      * This is done with the findStartTime field.
 *      *
 *      * Snapshots are set at the end of the Action. For a Vanish Action, for example,
 *      * we want to know how long it takes for a Vanish to succeed. This will be useful
 *      * if we perform another Vanish operation and want to know the average wait time,
 *      * or are using the MatchHistory for mocks. On the other hand, every Find operation
 *      * gives us useful information about Images, and we could also save this information.
 *      * Find operation Durations should be measured from the start of the Find operation
 *      * and not from the start of the Action.
 *      * On the other hand, we want MatchSnapshots to be representative of how
 *      * Images will respond in real scenarios, and saving Snapshots in scenarios where
 *      * we are waiting a while for an Image to appear will skew the distribution of
 *      * successful and unsuccessful matches. It is sufficient for us to have Snapshots saved
 *      * for Find Actions and not every individual Find operation, especially if the Duration
 *      * is also saved. One scenario where this may not be optimal is when we always use
 *      * an Image with an Action other than Find, meaning that we won't have a Find Action MatchSnapshot
 *      * for the Image, and we won't have any data with which to mock finding
 *      * this Image. Of course, if we never use a Find Action on this Image, it is unlikely that
 *      * a Find Action will occur in real execution. The Image will be used with other Actions,
 *      * and these Actions will have MatchSnapshots.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchSnapshot {

    /**
     * The ActionOptions can be queried to find which settings lead to success.
     */
    private ActionOptions actionOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setFind(UNIVERSAL)
            .build();
    private List<Match> matchList = new ArrayList<>();
    private String text = "";
    private double duration = 0.0;
    private LocalDateTime timeStamp; // JPA can handle LocalDateTime without @Embedded
    /**
     * The Action was successfully performed.
     */
    private boolean actionSuccess = false;
    /**
     * The Result was successful. Results must be defined by the app.
     * For example, the result of a Click is finding a particular Image.
     * This field can be used to analyze Action options in order to find
     * the best settings for an application.
     */
    private boolean resultSuccess = false;
    /**
     * The state in which the image is found gives important information about where and when the state can be found.
     * Mock operations can query this information when an action is carried out in a particular state.
     */
    private String stateName = SpecialStateType.NULL.toString();
    private Long stateId = SpecialStateType.NULL.getId();

    public <E> MatchSnapshot(List<E> matchInSnapshot) {
    }

    public boolean wasFound() {
        return !matchList.isEmpty() || !text.isEmpty();
    }

    public void setString(String str) {
        text = str;
    }

    public void addMatch(Match match) {
        matchList.add(match);
    }

    public void addMatchList(List<Match> matchList) {
        this.matchList.addAll(matchList);
    }

    public void print() {
        System.out.print(timeStamp.format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")));
        System.out.format(" %s", actionOptions.getAction());
        matchList.forEach(match -> System.out.format(" %d.%d,%d.%d", match.x(), match.y(), match.w(), match.h()));
        System.out.format(" %s", text);
        System.out.println();
    }

    /**
     * Match objects and Text are compared. If they are the same, true is returned.
     * @param snapshot the MatchSnapshot to compare
     * @return true if the Match list and Text are the same.
     */
    public boolean hasSameResultsAs(MatchSnapshot snapshot) {
        boolean matchFound;
        for (Match match : matchList) {
            matchFound = false;
            for (Match match1 : snapshot.matchList) {
                if (match.equals(match1)) {
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) return false;
        }
        if (!text.equals(snapshot.getText())) return false;
        return true;
    }

    /*
    The most common Snapshot added directly to a State is a Match (x,y,w,h) with a Find operation.
     */
    public MatchSnapshot(int x, int y, int w, int h) {
        Match match = new Match.Builder()
                .setRegion(x,y,w,h)
                .build();
        this.matchList.add(match);
    }

    public MatchSnapshot() {}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MatchSnapshot that = (MatchSnapshot) obj;
        return Double.compare(that.duration, duration) == 0 &&
                actionSuccess == that.actionSuccess &&
                resultSuccess == that.resultSuccess &&
                Objects.equals(actionOptions, that.actionOptions) &&
                Objects.equals(matchList, that.matchList) &&
                Objects.equals(text, that.text) &&
                Objects.equals(stateName, that.stateName) &&
                Objects.equals(stateId, that.stateId) &&
                (timeStamp == null && that.timeStamp == null ||
                        timeStamp != null && that.timeStamp != null &&
                                timeStamp.truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
                                        .equals(that.timeStamp.truncatedTo(java.time.temporal.ChronoUnit.SECONDS)));
    }

    @Override
    public int hashCode() {
        // Truncate timestamp to seconds to be consistent with the equals() method.
        java.time.LocalDateTime truncatedTimestamp = (timeStamp != null) ?
                timeStamp.truncatedTo(java.time.temporal.ChronoUnit.SECONDS) : null;

        return java.util.Objects.hash(actionOptions, matchList, text, duration, truncatedTimestamp,
                actionSuccess, resultSuccess, stateName, stateId);
    }

    @Override
    public String toString() {
        return "MatchSnapshot{" +
                "actionOptions=" + actionOptions +
                ", matchList=" + matchList +
                ", text='" + text + '\'' +
                ", duration=" + duration +
                ", timeStamp=" + timeStamp +
                ", actionSuccess=" + actionSuccess +
                ", resultSuccess=" + resultSuccess +
                ", stateName='" + stateName + '\'' +
                ", stateId=" + stateId +
                '}';
    }

    public static class Builder {
        private ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(UNIVERSAL)
                .build();
        private List<Match> matchList = new ArrayList<>();
        private String text = "";
        private double duration = 0.0;
        private LocalDateTime timeStamp;
        private boolean actionSuccess = false; // can be initialized for mocks
        private boolean resultSuccess = false; // can be initialized for mocks
        private String state = SpecialStateType.NULL.toString(); // the state in which it was found

        public Builder setActionOptions(ActionOptions actionOptions) {
            this.actionOptions = actionOptions;
            return this;
        }

        public Builder setActionOptions(ActionOptions.Action action) {
            this.actionOptions = new ActionOptions.Builder()
                    .setAction(action)
                    .build();
            return this;
        }

        public Builder setActionOptions(ActionOptions.Find find) {
            this.actionOptions = new ActionOptions.Builder()
                    .setAction(ActionOptions.Action.FIND)
                    .setFind(find)
                    .build();
            return this;
        }

        public Builder setMatchList(List<Match> matchList) {
            this.matchList = matchList;
            return this;
        }

        public Builder addMatch(Match match) {
            this.matchList.add(match);
            return this;
        }

        public Builder addMatch(int x, int y, int w, int h) {
            Match match = new Match.Builder()
                    .setRegion(x,y,w,h)
                    .build();
            this.matchList.add(match);
            return this;
        }

        public Builder addMatches(int numberOfMatches, int w, int h) {
            for (int i=0; i<numberOfMatches; i++) {
                int x = new Random().nextInt(new Screen().w);
                int y = new Random().nextInt(new Screen().h);
                Match match = new Match.Builder()
                        .setRegion(x,y,w,h)
                        .build();
                this.matchList.add(match);
            }
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setDuration(double duration) {
            this.duration = duration;
            return this;
        }

        public Builder setActionSuccess(boolean success) {
            this.actionSuccess = success;
            return this;
        }

        public Builder setResultSuccess(boolean success) {
            this.resultSuccess = success;
            return this;
        }

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public MatchSnapshot build() {
            MatchSnapshot matchSnapshot = new MatchSnapshot();
            matchSnapshot.actionOptions = actionOptions;
            matchSnapshot.text = text;
            matchSnapshot.matchList = matchList;
            matchSnapshot.duration = duration;
            matchSnapshot.timeStamp = LocalDateTime.now();
            matchSnapshot.actionSuccess = actionSuccess;
            matchSnapshot.resultSuccess = resultSuccess;
            matchSnapshot.stateName = state;
            return matchSnapshot;
        }
    }

}
