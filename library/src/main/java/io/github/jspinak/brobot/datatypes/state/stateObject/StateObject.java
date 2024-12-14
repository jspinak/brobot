package io.github.jspinak.brobot.datatypes.state.stateObject;

/**
 The MatchHistory keep a record of the StateObject's Snapshots. Snapshots record the results
 of Actions involving this StateObject. These results can be used to run mocks or adjust
 settings.
 */
public interface StateObject {

    public enum Type {
        IMAGE, REGION, LOCATION, STRING, TEXT
    }

    String getIdAsString();
    Type getObjectType();
    String getName();
    String getOwnerStateName();
    Long getOwnerStateId();
    void addTimesActedOn();
    void setTimesActedOn(int times);

}
