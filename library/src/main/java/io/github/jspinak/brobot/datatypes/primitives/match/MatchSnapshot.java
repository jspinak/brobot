package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.text.Text;
import lombok.Getter;
import lombok.Setter;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Find.UNIVERSAL;

/**
 * MatchSnapshots record a match (or failed match) and the search options at a single point in time.
 * They are used to build a MatchHistory to allow for more accurate mocking. The idea is to have a
 * history of examples from specific situations that give an accurate picture of what may occur
 * in a similar situation in the future. For this reason, only one Snapshot is created in situations
 * where an Image is searched multiple times. Actions that search multiple times until a condition is
 * met are good examples of such situations. Snapshots are kept in the Matches object as DanglingSnapshots
 * until the Action is finished, and only then saved to the Image or StateRegion.
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
 */
@Getter
@Setter
public class MatchSnapshot {

    /**
     * The ActionOptions can be queried to find which settings lead to success.
     */
    private ActionOptions actionOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setFind(UNIVERSAL)
            .build();
    private List<Match> matchList = new ArrayList<>();
    private Text text = new Text();
    private double duration = 0.0;
    private LocalDateTime timeStamp;
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
    private String state = "null";

    public boolean wasFound() {
        return !matchList.isEmpty() || text.size() > 0;
    }

    public void addString(String str) {
        text.add(str);
    }

    public void addMatch(Match match) {
        matchList.add(match);
    }

    public void addMatchList(List<Match> matchList) {
        this.matchList.addAll(matchList);
    }

    public void addText(Text text) {
        this.text.addAll(text);
    }

    public void print() {
        System.out.print(timeStamp.format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")));
        System.out.format(" %s", actionOptions.getAction());
        matchList.forEach(match -> System.out.format(" %d.%d,%d.%d", match.x, match.y, match.w, match.h));
        text.getAll().forEach(str -> System.out.format(" %s", str));
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
        for (String str : text.getAll()) {
            matchFound = false;
            for (String str1 : snapshot.text.getAll()) {
                if (str.equals(str1)) {
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) return false;
        }
        return true;
    }

    /*
    The most common Snapshot added directly to a State is a Match (x,y,w,h) with a Find operation.
     */
    public MatchSnapshot(int x, int y, int w, int h) {
        Match match = new Match();
        match.x = x;
        match.y = y;
        match.w = w;
        match.h = h;
        this.matchList.add(match);
    }

    public MatchSnapshot() {}

    public static class Builder {
        private ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(UNIVERSAL)
                .build();
        private List<Match> matchList = new ArrayList<>();
        private Text text = new Text();
        private double duration = 0.0;
        private LocalDateTime timeStamp;
        private boolean actionSuccess = false; // can be initialized for mocks
        private boolean resultSuccess = false; // can be initialized for mocks
        private String state = "null"; // the state in which it was found

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
            Match match = new Match();
            match.x = x;
            match.y = y;
            match.w = w;
            match.h = h;
            this.matchList.add(match);
            return this;
        }

        public Builder addMatches(int numberOfMatches, int w, int h) {
            for (int i=0; i<numberOfMatches; i++) {
                Match match = new Match();
                match.x = new Random().nextInt(new Screen().w);
                match.y = new Random().nextInt(new Screen().h);
                match.w = w;
                match.h = h;
                this.matchList.add(match);
            }
            return this;
        }

        public Builder setText(Text text) {
            this.text = text;
            if (actionOptions.getAction() == ActionOptions.Action.FIND)
                actionOptions.setAction(ActionOptions.Action.GET_TEXT);
            return this;
        }

        public Builder addString(String str) {
            this.text.add(str);
            if (actionOptions.getAction() == ActionOptions.Action.FIND)
                actionOptions.setAction(ActionOptions.Action.GET_TEXT);
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
            matchSnapshot.state = state;
            return matchSnapshot;
        }
    }

}
