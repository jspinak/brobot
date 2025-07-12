package io.github.jspinak.brobot.annotations;

import io.github.jspinak.brobot.model.state.State;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Builds Brobot State objects from @State annotated classes.
 * 
 * <p>This class is responsible solely for constructing State objects from annotated classes
 * and their extracted components. It does not handle component extraction or state registration,
 * following the Single Responsibility Principle.</p>
 * 
 * <p>The builder creates a proper State object with:
 * <ul>
 *   <li>A generated StateEnum based on the class name</li>
 *   <li>All extracted StateImage, StateString, and StateObject components</li>
 *   <li>Proper state configuration from the @State annotation</li>
 * </ul>
 * </p>
 * 
 * @since 1.1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AnnotatedStateBuilder {
    
    private final StateComponentExtractor componentExtractor;
    
    /**
     * Builds a State object from an annotated state instance.
     * 
     * @param stateInstance The instance of a @State annotated class
     * @param stateAnnotation The @State annotation from the class
     * @return A fully constructed State object ready for registration
     */
    public State buildState(Object stateInstance, io.github.jspinak.brobot.annotations.State stateAnnotation) {
        Class<?> stateClass = stateInstance.getClass();
        String stateName = deriveStateName(stateClass, stateAnnotation);
        
        log.debug("Building state '{}' from class {}", stateName, stateClass.getSimpleName());
        
        // Extract components from the state instance
        StateComponentExtractor.StateComponents components = componentExtractor.extractComponents(stateInstance);
        
        // Build the State object using the string constructor
        State.Builder stateBuilder = new State.Builder(stateName);
        
        // Add all extracted components
        if (!components.getStateImages().isEmpty()) {
            stateBuilder.withImages(components.getStateImages().toArray(new io.github.jspinak.brobot.model.state.StateImage[0]));
            log.trace("Added {} StateImages to state '{}'", components.getStateImages().size(), stateName);
        }
        
        if (!components.getStateStrings().isEmpty()) {
            stateBuilder.withStrings(components.getStateStrings().toArray(new io.github.jspinak.brobot.model.state.StateString[0]));
            log.trace("Added {} StateStrings to state '{}'", components.getStateStrings().size(), stateName);
        }
        
        // Note: State.Builder doesn't support StateObjects directly
        // They would need to be converted to StateImages or StateStrings
        if (!components.getStateObjects().isEmpty()) {
            log.warn("Found {} StateObjects in state '{}' but State.Builder doesn't support them directly", 
                    components.getStateObjects().size(), stateName);
        }
        
        State state = stateBuilder.build();
        log.info("Built state '{}' with {} total components", stateName, components.getTotalComponents());
        
        return state;
    }
    
    /**
     * Derives the state name from the class and annotation.
     */
    private String deriveStateName(Class<?> stateClass, io.github.jspinak.brobot.annotations.State annotation) {
        // Use annotation name if provided
        if (annotation != null && !annotation.name().isEmpty()) {
            return annotation.name();
        }
        
        // Otherwise derive from class name
        String className = stateClass.getSimpleName();
        
        // Remove "State" suffix if present
        if (className.endsWith("State")) {
            return className.substring(0, className.length() - 5);
        }
        
        return className;
    }
    
}