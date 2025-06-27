package io.github.jspinak.brobot.model.state.special;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateObject;
import lombok.Data;

/**
 * Represents persistent text that uniquely identifies a state in GUI automation.
 * 
 * <p>StateText captures text content that consistently appears within a specific state, 
 * serving as a textual signature for state identification. Unlike visual patterns that 
 * may change with themes or resolutions, text content often remains stable, making it 
 * a reliable indicator of application state. This class provides a foundation for 
 * text-based state recognition, though full implementation is pending.</p>
 * 
 * <p>Key characteristics:
 * <ul>
 *   <li><b>State Signature</b>: Text that uniquely identifies or characterizes a state</li>
 *   <li><b>Persistent Content</b>: Expected to appear consistently when state is active</li>
 *   <li><b>Search Region</b>: Optional area constraint for text location</li>
 *   <li><b>Fallback Identification</b>: Alternative to visual pattern matching</li>
 * </ul>
 * </p>
 * 
 * <p>Intended use cases:
 * <ul>
 *   <li>Identifying states by window titles or headers</li>
 *   <li>Recognizing states through unique labels or messages</li>
 *   <li>Verifying state transitions via text changes</li>
 *   <li>Recovery when visual pattern matching fails</li>
 *   <li>Cross-platform state identification using text</li>
 * </ul>
 * </p>
 * 
 * <p>Examples of state-identifying text:
 * <ul>
 *   <li>Window titles: "Settings - Application Name"</li>
 *   <li>Page headers: "User Account Dashboard"</li>
 *   <li>Status messages: "Ready", "Processing...", "Complete"</li>
 *   <li>Unique labels: "Advanced Options", "System Configuration"</li>
 * </ul>
 * </p>
 * 
 * <p>Advantages over visual patterns:
 * <ul>
 *   <li>Resolution and DPI independent</li>
 *   <li>Theme and color scheme agnostic</li>
 *   <li>Often more stable across application versions</li>
 *   <li>Easier to specify and maintain</li>
 *   <li>Language-aware for internationalized applications</li>
 * </ul>
 * </p>
 * 
 * <p>Implementation note: While the StateText structure is defined, the full integration 
 * with Brobot's state recognition system is pending. Future implementations will include 
 * OCR-based state detection, fuzzy text matching, and integration with the state 
 * management system for robust text-based state identification.</p>
 * 
 * <p>In the model-based approach, StateText will provide an additional dimension for 
 * state recognition, complementing visual patterns with semantic text content. This 
 * multi-modal approach will increase the robustness and adaptability of state 
 * identification across diverse GUI environments.</p>
 * 
 * @since 1.0
 * @see StateObject
 * @see Text
 * @see StateString
 * @see State
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateText {

    private StateObject.Type objectType = StateObject.Type.TEXT;
    private String name;
    private Region searchRegion;
    private String ownerStateName = "null";
    private String text;

    public String getId() {
        String regionId = (searchRegion != null)
            ? searchRegion.getX() + "" + searchRegion.getY() + "" + searchRegion.getW() + "" + searchRegion.getH()
            : "nullRegion";
        return objectType.name() + name + regionId + text;
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