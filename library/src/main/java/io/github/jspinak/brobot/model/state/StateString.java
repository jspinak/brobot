package io.github.jspinak.brobot.model.state;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateObject;
import lombok.Data;

/**
 * Represents text input associated with a specific state in the Brobot framework.
 * 
 * <p>StateString encapsulates text data that has contextual meaning within a particular 
 * state. Unlike simple string parameters, StateString maintains state ownership and can 
 * include spatial context, making it suitable for sophisticated text input scenarios where 
 * the location or state context affects how the text should be entered.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>State Association</b>: Text is bound to a specific state for contextual relevance</li>
 *   <li><b>Spatial Context</b>: Optional search region for click/hover before typing</li>
 *   <li><b>Usage Tracking</b>: Counts how many times the string has been acted upon</li>
 *   <li><b>Flexible Construction</b>: Builder pattern and convenience methods for creation</li>
 *   <li><b>Unique Identification</b>: Generated ID based on properties for tracking</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Entering passwords or usernames in login states</li>
 *   <li>Typing search queries in search states</li>
 *   <li>Filling form fields with state-specific data</li>
 *   <li>Entering commands in terminal or command-line states</li>
 *   <li>Providing input that triggers state transitions</li>
 * </ul>
 * </p>
 * 
 * <p>Search region functionality:
 * <ul>
 *   <li>Defines where to click or hover before typing</li>
 *   <li>Ensures proper focus for text input fields</li>
 *   <li>Handles scenarios where text fields must be activated first</li>
 *   <li>Optional - can be null for global keyboard input</li>
 * </ul>
 * </p>
 * 
 * <p>Special patterns:
 * <ul>
 *   <li><b>InNullState</b>: Factory for creating stateless strings for temporary use</li>
 *   <li><b>Builder</b>: Comprehensive construction with all optional properties</li>
 *   <li><b>ID Generation</b>: Combines type, name, region, and content for uniqueness</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateString enables precise control over text input 
 * by maintaining the relationship between text data and application states. This is 
 * crucial for automation scenarios where the same text might have different meanings 
 * or require different handling in different states, such as search terms vs. file names 
 * vs. command inputs.</p>
 * 
 * @since 1.0
 * @see StateObject
 * @see TypeText
 * @see State
 * @see Region
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateString {

    private StateObject.Type objectType = StateObject.Type.STRING;
    private String name;
    private Region searchRegion; // sometimes we need to hover over or click on a region before typing the string
    private String ownerStateName = "null";
    private Long ownerStateId = null;
    private int timesActedOn = 0;
    private String string;

    public StateString() {} // for mapping

public String getId() {
    String regionId = (searchRegion != null)
        ? searchRegion.getX() + "" + searchRegion.getY() + "" + searchRegion.getW() + "" + searchRegion.getH()
        : "nullRegion";
    return objectType.name() + name + regionId + string;
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
        private Long ownerStateId = 0L;
        private int timesActedOn;
        private String string;

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

        public Builder setString(String string) {
            this.string = string;
            return this;
        }

        public StateString build() {
            // Use empty string as default if setString() wasn't called
            String stringValue = (string != null) ? string : "";
            StateString stateString = new StateString(stringValue);
            stateString.name = name;
            stateString.searchRegion = searchRegion;
            stateString.ownerStateName = ownerStateName;
            stateString.ownerStateId = ownerStateId;
            stateString.timesActedOn = timesActedOn;
            return stateString;
        }

    }

}
