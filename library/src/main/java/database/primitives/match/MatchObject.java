package database.primitives.match;

import com.brobot.multimodule.database.primitives.location.Anchor;
import com.brobot.multimodule.database.primitives.location.Location;
import com.brobot.multimodule.database.primitives.text.Text;
import com.brobot.multimodule.database.state.stateObject.StateObject;
import com.brobot.multimodule.primatives.enums.StateEnum;
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
}
