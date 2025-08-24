package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.model.state.special.SpecialStateType;

/**
 * Marker interface for state enumerations in the Brobot framework.
 * 
 * <p>StateEnum serves as a type-safe contract for all enumerations that represent states 
 * in automation scripts. By implementing this interface, enums can be used polymorphically 
 * throughout the framework while maintaining compile-time type safety and enabling 
 * enum-specific features like switch statements and ordinal values.</p>
 * 
 * <p>Design benefits:
 * <ul>
 *   <li><b>Type Safety</b>: Ensures only valid state enums are used in state management APIs</li>
 *   <li><b>Polymorphism</b>: Allows different automation projects to define their own state enums</li>
 *   <li><b>Framework Integration</b>: Enables generic handling of states across the framework</li>
 *   <li><b>Compile-time Validation</b>: Prevents runtime errors from invalid state references</li>
 * </ul>
 * </p>
 * 
 * <p>Implementation pattern:
 * <pre>{@code
 * public enum MyStates implements StateEnum {
 *     LOGIN_SCREEN,
 *     MAIN_MENU,
 *     SETTINGS_PAGE,
 *     LOGOUT_SCREEN
 * }
 * }</pre>
 * </p>
 * 
 * <p>Common usage scenarios:
 * <ul>
 *   <li>Defining application-specific states for automation scripts</li>
 *   <li>Creating reusable state sets for common UI patterns</li>
 *   <li>Building state transition definitions with type safety</li>
 *   <li>Enabling framework features that operate on generic states</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateEnum enables developers to define their application's 
 * state structure using familiar Java enums while integrating seamlessly with Brobot's 
 * state management infrastructure. This approach combines the benefits of enum constants 
 * (readability, type safety, IDE support) with the flexibility needed for diverse 
 * automation scenarios.</p>
 * 
 * <p>Note: This is a marker interface with no methods, following the pattern of interfaces 
 * like Serializable. Its purpose is purely to enable type constraints and polymorphic 
 * handling of state enumerations.</p>
 * 
 * @since 1.0
 * @see SpecialStateType
 * @see ActiveStateSet
 * @see StateMemory
 */
public interface StateEnum {
}
