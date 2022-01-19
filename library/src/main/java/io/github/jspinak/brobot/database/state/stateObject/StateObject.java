package io.github.jspinak.brobot.database.state.stateObject;

import io.github.jspinak.brobot.database.primitives.location.Anchors;
import io.github.jspinak.brobot.database.primitives.location.Position;
import io.github.jspinak.brobot.database.primitives.match.MatchHistory;
import io.github.jspinak.brobot.database.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.primatives.enums.StateEnum;

/**
 The MatchHistory keep a record of the StateObject's Snapshots. Snapshots record the results
 of Actions involving this StateObject. These results can be used to run mocks or adjust
 settings.
 */
public interface StateObject {

    String getName();
    StateEnum getOwnerStateName();
    Position getPosition();
    Anchors getAnchors();
    int getTimesActedOn(); // times per action. resets to 0 when action is complete.
    void addTimesActedOn();
    void setTimesActedOn(int times);
    void setProbabilityExists(int probabilityExists);
    MatchHistory getMatchHistory();
    void addSnapshot(MatchSnapshot matchSnapshot);

}
