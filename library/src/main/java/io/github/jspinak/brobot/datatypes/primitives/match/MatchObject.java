package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.text.Text;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import lombok.Data;
import org.sikuli.script.Match;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MatchObject is used to store information about new Image matches. It includes:
 *   The Sikuli Match
 *   A Text object holding Strings found during text detection
 *   The StateObject used in the Find operation
 *   The time the MatchObject was created
 *   The duration of the Find operation in seconds
 *
 *   This object is similar to MatchSnapshot but has the following differences:
 *     MatchObjects always have Match objects, whereas Snapshots can have failed matches.
 *     Snapshots are contained within Image objects and StateRegions. MatchObjects include
 *       a StateObject as a variable.
 *     MatchObjects have only one Match, as opposed to Snapshots, which can have
 *       multiple Match objects.
 */
@Data
public class MatchObject {

    private Match match;
    private Text text = new Text();
    private String selectedText = "";
    private StateObject stateObject;
    private LocalDateTime timeStamp;
    private double duration;

    /**
     * It shouldn't be possible to create a MatchObject with Match == null.
     *
     * @param match the match found
     * @param stateObject the StateObject resulting in the Match
     * @param duration the time it took to find the Match
     * @throws Exception if the Match or the StateObject is null
     */
    public MatchObject(Match match, StateObject stateObject, double duration) throws Exception {
        if (match == null || stateObject == null)
            throw new Exception("null parameters in MatchObject are not allowed");
        this.match = match;
        this.stateObject = stateObject;
        this.timeStamp = LocalDateTime.now();
        this.duration = duration;
    }

    public StateEnum getState() {
        return stateObject.getOwnerStateName();
    }

    public Location getLocation() {
        return new Location(match, stateObject.getPosition());
    }

    public List<Anchor> getAnchorList() {
        return stateObject.getAnchors().getAnchors();
    }

    public void addString(String str) {
        text.add(str);
    }

    public void print() {
        System.out.format("%s matchXYWH:%d.%d,%d.%d selectedText:%s\n",
                timeStamp.format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")),
                match.x, match.y, match.w, match.h, selectedText);
    }

    public void printXY() {
        System.out.print(match.x+"."+match.y);
    }

    public boolean contains(Matches matches) {
        for (Match match : matches.getMatches()) {
            if (this.match.contains(match)) return true;
        }
        return false;
    }
}
