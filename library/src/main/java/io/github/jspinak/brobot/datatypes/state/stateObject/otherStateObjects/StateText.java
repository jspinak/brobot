package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Data;

/**
 * StateText is text that always appears in its owner State.
 * Since it is representative of the State it can be used to narrow
 * the set of States to search for in case Brobot is lost.
 * StateText is not yet implemented by Brobot.
 */
@Data
public class StateText {

    private StateObject.Type objectType = StateObject.Type.TEXT;
    private String name;
    private Region searchRegion;
    private String ownerStateName = "null";
    private String text;

    public String getId() {
        return objectType.name() + name + searchRegion.getX() + searchRegion.getY() + searchRegion.getW() + searchRegion.getY() + text;
    }

    public boolean defined() { return text != null && !text.isEmpty(); }

    public static class Builder {
        private String name;
        private Region searchRegion;
        private String ownerStateName;
        private String text = "";

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

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public StateText build() {
            StateText stateText = new StateText();
            stateText.name = name;
            stateText.searchRegion = searchRegion;
            stateText.ownerStateName = ownerStateName;
            stateText.text = text;
            return stateText;
        }

    }

}
