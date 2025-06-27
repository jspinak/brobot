package io.github.jspinak.brobot.model.state;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Lightweight reference to StateObject instances in the Brobot framework.
 * 
 * <p>StateObjectMetadata provides a minimal, serializable representation of StateObject identity 
 * without the full object graph. This design pattern solves critical architectural challenges 
 * around circular dependencies and persistence while maintaining the ability to reference 
 * state objects throughout the framework.</p>
 * 
 * <p>Key design benefits:
 * <ul>
 *   <li><b>Prevents Circular Dependencies</b>: Avoids infinite object graphs that would occur 
 *       if Match objects contained full StateObject references</li>
 *   <li><b>Persistence Friendly</b>: Can be embedded in entities without complex mappings, 
 *       as StateObjects are entities while this is embeddable</li>
 *   <li><b>Lightweight References</b>: Minimal memory footprint for object references</li>
 *   <li><b>Repository Pattern Support</b>: Contains sufficient data to retrieve full objects 
 *       from their respective repositories</li>
 * </ul>
 * </p>
 * 
 * <p>Reference data captured:
 * <ul>
 *   <li><b>Object Identity</b>: Unique ID for repository lookup</li>
 *   <li><b>Object Type</b>: Specifies which repository to query (IMAGE, REGION, etc.)</li>
 *   <li><b>Object Name</b>: Human-readable identifier for debugging</li>
 *   <li><b>Owner State</b>: Both name and ID of the containing state</li>
 * </ul>
 * </p>
 * 
 * <p>Common usage patterns:
 * <ul>
 *   <li>Stored in Match objects to track which StateObject was found</li>
 *   <li>Used in action results to reference involved state objects</li>
 *   <li>Enables lazy loading of full StateObjects when needed</li>
 *   <li>Facilitates cross-reference tracking without object coupling</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateObjectMetadata enables the framework to maintain rich 
 * cross-references between matches, actions, and state objects without the complexity and 
 * performance overhead of full object graphs. This is essential for scalable automation 
 * that can handle complex state structures with many interconnected elements.</p>
 * 
 * @since 1.0
 * @see StateObject
 * @see StateImage
 * @see StateRegion
 * @see Match
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateObjectMetadata {

    private String stateObjectId;
    private StateObject.Type objectType;
    private String stateObjectName;
    private String ownerStateName;
    private Long ownerStateId;

    public StateObjectMetadata(StateObject stateObject) {
        this.stateObjectId = stateObject.getIdAsString();
        this.objectType = stateObject.getObjectType();
        this.stateObjectName = stateObject.getName();
        this.ownerStateName = stateObject.getOwnerStateName();
        this.ownerStateId = stateObject.getOwnerStateId();
    }

    public StateObjectMetadata() {
        stateObjectId = "";
        objectType = StateObject.Type.IMAGE;
        stateObjectName = "";
        ownerStateName = "";
        ownerStateId = null;
    }

    @Override
    public String toString() {
        return "StateObject: " + stateObjectName + ", " + objectType +
                ", ownerState=" + ownerStateName + ", id=" + stateObjectId +
                ", owner state id=" + ownerStateId;
    }

}
