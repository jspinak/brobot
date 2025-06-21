package io.github.jspinak.brobot.datatypes.state.stateObject;

/**
 * Core interface for all objects that belong to states in the Brobot framework.
 * 
 * <p>StateObject defines the contract for all elements that can be contained within a State, 
 * establishing a unified interface for diverse object types while maintaining their association 
 * with parent states. This abstraction is fundamental to the framework's ability to treat 
 * different GUI elements polymorphically while preserving their state context.</p>
 * 
 * <p>Object type hierarchy:
 * <ul>
 *   <li><b>IMAGE</b>: Visual patterns for recognition (StateImage)</li>
 *   <li><b>REGION</b>: Defined screen areas (StateRegion)</li>
 *   <li><b>LOCATION</b>: Specific screen coordinates (StateLocation)</li>
 *   <li><b>STRING</b>: Text strings for typing or validation (StateString)</li>
 *   <li><b>TEXT</b>: Expected text patterns (StateText)</li>
 * </ul>
 * </p>
 * 
 * <p>Core responsibilities:
 * <ul>
 *   <li><b>Identity</b>: Provides unique identification as string for persistence</li>
 *   <li><b>Type Declaration</b>: Declares the specific object type for proper handling</li>
 *   <li><b>Naming</b>: Human-readable name for debugging and logging</li>
 *   <li><b>State Association</b>: Maintains reference to owning state by name and ID</li>
 *   <li><b>Usage Tracking</b>: Records how many times the object has been acted upon</li>
 * </ul>
 * </p>
 * 
 * <p>MatchHistory integration:
 * <ul>
 *   <li>StateObjects maintain MatchHistory to record action results</li>
 *   <li>Snapshots capture success/failure patterns for mock execution</li>
 *   <li>Historical data enables learning and optimization</li>
 *   <li>Usage statistics inform automation strategy adjustments</li>
 * </ul>
 * </p>
 * 
 * <p>Common implementations:
 * <ul>
 *   <li>{@code StateImage}: Images with patterns for visual matching</li>
 *   <li>{@code StateRegion}: Regions with defined areas and search patterns</li>
 *   <li>{@code StateLocation}: Fixed or relative positions</li>
 *   <li>{@code StateString}: Text for keyboard input</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateObject enables the framework to handle diverse GUI 
 * elements uniformly while maintaining their state context. This polymorphic design allows 
 * actions to operate on different object types without knowing their specific implementation, 
 * while the state association ensures proper scoping and context awareness.</p>
 * 
 * @since 1.0
 * @see State
 * @see StateImage
 * @see StateRegion
 * @see StateString
 * @see StateObjectData
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
