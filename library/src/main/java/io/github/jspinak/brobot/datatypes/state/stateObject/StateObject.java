package io.github.jspinak.brobot.datatypes.state.stateObject;

import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.primatives.enums.StateEnum;

/**
 The MatchHistory keep a record of the StateObject's Snapshots. Snapshots record the results
 of Actions involving this StateObject. These results can be used to run mocks or adjust
 settings.
 */
public interface StateObject {

    public enum Type {
        IMAGE, REGION, LOCATION, STRING, TEXT
    }

    Long getId();
    Type getObjectType();
    String getName();
    String getOwnerStateName();
    void addTimesActedOn();
    void setTimesActedOn(int times);

}
