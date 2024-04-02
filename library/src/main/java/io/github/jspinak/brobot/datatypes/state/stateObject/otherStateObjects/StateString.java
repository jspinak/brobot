package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Data;

/**
 * A State String belongs to a State and usually has a String that
 * has a special meaning for the owner State. For example, typing this
 * String may be part of a Transition for this State but not for other States.
 */
@Data
public class StateString {

    private StateObject.Type objectType = StateObject.Type.STRING;
    private String name;
    private Region searchRegion; // sometimes we need to hover over or click on a region before typing the string
    private String ownerStateName = "null";
    private int timesActedOn = 0;
    private String string;

    public StateString() {} // for mapping

    public String getId() {
        return objectType.name() + name + searchRegion.getX() + searchRegion.getY() + searchRegion.getW() + searchRegion.getY() + string;
    }

    private StateString(String string) {
        this.string = string;
    }

    public boolean defined() { return string != null && string.length() > 0; }

    public static class InNullState {
        public StateString withString(String string) {
            StateString stateString = new StateString(string);
            stateString.ownerStateName = "null";
            return stateString;
        }
    }

    public void addTimesActedOn() {
        timesActedOn++;
    }

    public static class Builder {
        private String name;
        private Region searchRegion;
        private String ownerStateName;
        private int timesActedOn;
        private String string; // defined when build is called

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSearchRegion(Region searchRegion) {
            this.searchRegion = searchRegion;
            return this;
        }

        public Builder setOwnerStateName(String stateName) {
            this.ownerStateName = stateName;
            return this;
        }

        public Builder setTimesActedOn(int timesActedOn) {
            this.timesActedOn = timesActedOn;
            return this;
        }

        // String as param because it shouldn't be created without a string
        public StateString build(String string) {
            StateString stateString = new StateString(string);
            stateString.name = name;
            stateString.searchRegion = searchRegion;
            stateString.ownerStateName = ownerStateName;
            stateString.timesActedOn = timesActedOn;
            return stateString;
        }

    }

}
