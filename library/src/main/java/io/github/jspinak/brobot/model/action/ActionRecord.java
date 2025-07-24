package io.github.jspinak.brobot.model.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import lombok.Data;
import org.sikuli.script.Screen;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static io.github.jspinak.brobot.action.internal.options.ActionOptions.Find.UNIVERSAL;

/**
 * Records match results and context at a specific point in time for the Brobot framework.
 * 
 * <p>ActionRecord captures comprehensive information about a match operation, including the 
 * matches found (or not found), the action options used, timing data, and contextual state 
 * information. These records form the foundation of Brobot's learning and mocking capabilities, 
 * enabling the framework to simulate realistic GUI behavior based on historical data.</p>
 * 
 * <p>Key components captured:
 * <ul>
 *   <li><b>Match Results</b>: List of matches found (empty for failed searches)</li>
 *   <li><b>Action Context</b>: The ActionOptions that produced this result</li>
 *   <li><b>Timing Data</b>: Duration of the operation for performance analysis</li>
 *   <li><b>State Context</b>: Which state the match occurred in</li>
 *   <li><b>Success Indicators</b>: Both action success and result success flags</li>
 *   <li><b>Text Data</b>: Extracted text for text-based operations</li>
 * </ul>
 * </p>
 * 
 * <p>Snapshot creation strategy:
 * <ul>
 *   <li>One record per action completion, not per find operation</li>
 *   <li>Avoids skewing data with repeated searches in wait loops</li>
 *   <li>Captures representative examples of real-world behavior</li>
 * </ul>
 * </p>
 * 
 * <p>Mock operation support:
 * <ul>
 *   <li>Provides realistic match/failure distributions based on history</li>
 *   <li>Action-specific records prevent cross-action interference</li>
 *   <li>Enables accurate simulation of Find.ALL and Find.EACH operations</li>
 *   <li>Supports extraction of best matches for Find.FIRST/BEST simulation</li>
 * </ul>
 * </p>
 * 
 * <p>Special handling:
 * <ul>
 *   <li><b>Failed Searches</b>: Empty match list indicates search failure</li>
 *   <li><b>Multiple Results</b>: Text operations may produce multiple strings in one record</li>
 *   <li><b>Reused Matches</b>: Matches used multiple times within an action are recorded once</li>
 *   <li><b>State Association</b>: Links matches to their containing state for context</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, ActionRecord provides the empirical foundation for 
 * understanding how GUI elements behave over time. By capturing not just what was found 
 * but also when, where, and under what conditions, these records enable sophisticated 
 * analysis and realistic simulation of GUI interactions.</p>
 * 
 * <p>The extensive documentation in this class reflects its central role in Brobot's 
 * learning and simulation capabilities. The detailed timing and context capture enables 
 * the framework to adapt to varying application behaviors and provide accurate mock 
 * responses during development and testing.</p>
 * 
 * @since 1.0
 * @see ActionHistory
 * @see Match
 * @see ActionOptions
 * @see State
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionRecord {

    /**
     * The ActionOptions can be queried to find which settings lead to success.
     * @deprecated Use actionConfig instead
     */
    @Deprecated
    private ActionOptions actionOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setFind(UNIVERSAL)
            .build();
    
    /**
     * The ActionConfig can be queried to find which settings lead to success.
     * This is the new API replacement for actionOptions.
     */
    private ActionConfig actionConfig;
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

    public <E> ActionRecord(List<E> matchInSnapshot) {
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
     * @param actionRecord the ActionRecord to compare
     * @return true if the Match list and Text are the same.
     */
    public boolean hasSameResultsAs(ActionRecord actionRecord) {
        boolean matchFound;
        for (Match match : matchList) {
            matchFound = false;
            for (Match match1 : actionRecord.matchList) {
                if (match.equals(match1)) {
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) return false;
        }
        if (!text.equals(actionRecord.getText())) return false;
        return true;
    }

    /*
    The most common Snapshot added directly to a State is a Match (x,y,w,h) with a Find operation.
     */
    public ActionRecord(int x, int y, int w, int h) {
        Match match = new Match.Builder()
                .setRegion(x,y,w,h)
                .build();
        this.matchList.add(match);
    }

    public ActionRecord() {}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ActionRecord that = (ActionRecord) obj;
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
        return "ActionRecord{" +
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

        public ActionRecord build() {
            ActionRecord actionRecord = new ActionRecord();
            actionRecord.actionOptions = actionOptions;
            actionRecord.text = text;
            actionRecord.matchList = matchList;
            actionRecord.duration = duration;
            actionRecord.timeStamp = LocalDateTime.now();
            actionRecord.actionSuccess = actionSuccess;
            actionRecord.resultSuccess = resultSuccess;
            actionRecord.stateName = state;
            return actionRecord;
        }
    }

}