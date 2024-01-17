package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.NullState;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;

/**
 * StateText is text that always appears in its owner State.
 * Since it is representative of the State it can be used to narrow
 * the set of States to search for in case Brobot is lost.
 * StateText is not yet implemented by Brobot.
 */
@Entity
@Data
public class StateText {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private StateObject.Type objectType = StateObject.Type.TEXT;
    private String name;
    @Embedded
    private Region searchRegion;
    private String ownerStateName = NullState.Name.NULL.toString();

    private String text;

    private StateText() {}

    public boolean defined() { return text != null && !text.isEmpty(); }

    public static class Builder {
        private String name;
        private Region searchRegion;
        private String ownerStateName;

        public Builder called(String name) {
            this.name = name;
            return this;
        }

        public Builder withSearchRegion(Region searchRegion) {
            this.searchRegion = searchRegion;
            return this;
        }

        public Builder inState(String stateName) {
            this.ownerStateName = stateName;
            return this;
        }

        public StateText build() {
            StateText stateText = new StateText();
            stateText.name = name;
            stateText.searchRegion = searchRegion;
            stateText.ownerStateName = ownerStateName;
            return stateText;
        }

    }

}
