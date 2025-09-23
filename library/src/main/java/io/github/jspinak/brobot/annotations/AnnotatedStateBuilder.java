package io.github.jspinak.brobot.annotations;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Builds Brobot State objects from @State annotated classes.
 *
 * <p>This class is responsible solely for constructing State objects from annotated classes and
 * their extracted components. It does not handle component extraction or state registration,
 * following the Single Responsibility Principle.
 *
 * <p>The builder creates a proper State object with:
 *
 * <ul>
 *   <li>A generated StateEnum based on the class name
 *   <li>All extracted StateImage, StateString, and StateObject components
 *   <li>Proper state configuration from the @State annotation
 * </ul>
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
    public State buildState(
            Object stateInstance, io.github.jspinak.brobot.annotations.State stateAnnotation) {
        Class<?> stateClass = stateInstance.getClass();
        String stateName = deriveStateName(stateClass, stateAnnotation);

        log.debug("Building state '{}' from class {}", stateName, stateClass.getSimpleName());

        // Extract components from the state instance
        StateComponentExtractor.StateComponents components =
                componentExtractor.extractComponents(stateInstance);

        // Set the owner state name for all extracted components
        setOwnerStateNameForComponents(components, stateName);

        // Build the State object using the string constructor
        State.Builder stateBuilder = new State.Builder(stateName);

        // Add all extracted components
        if (!components.getStateImages().isEmpty()) {
            stateBuilder.withImages(
                    components
                            .getStateImages()
                            .toArray(new io.github.jspinak.brobot.model.state.StateImage[0]));
            log.trace(
                    "Added {} StateImages to state '{}'",
                    components.getStateImages().size(),
                    stateName);
        }

        if (!components.getStateStrings().isEmpty()) {
            stateBuilder.withStrings(
                    components
                            .getStateStrings()
                            .toArray(new io.github.jspinak.brobot.model.state.StateString[0]));
            log.trace(
                    "Added {} StateStrings to state '{}'",
                    components.getStateStrings().size(),
                    stateName);
        }

        // Note: State.Builder doesn't support StateObjects directly
        // They would need to be converted to StateImages or StateStrings
        if (!components.getStateObjects().isEmpty()) {
            log.warn(
                    "Found {} StateObjects in state '{}' but State.Builder doesn't support them"
                            + " directly",
                    components.getStateObjects().size(),
                    stateName);
        }

        // Set pathCost from annotation (default is 1)
        stateBuilder.setPathCost(stateAnnotation.pathCost());
        log.trace("Set pathCost {} for state '{}'", stateAnnotation.pathCost(), stateName);

        State state = stateBuilder.build();
        log.debug(
                "Built state '{}' with {} total components",
                stateName,
                components.getTotalComponents());

        return state;
    }

    /**
     * Sets the owner state name for all components in the collection. This ensures that all
     * StateObjects know which state they belong to, which is essential for features like
     * cross-state search region resolution.
     */
    private void setOwnerStateNameForComponents(
            StateComponentExtractor.StateComponents components, String stateName) {
        // Set owner state name for all StateImages
        for (io.github.jspinak.brobot.model.state.StateImage stateImage :
                components.getStateImages()) {
            String previousOwner = stateImage.getOwnerStateName();
            stateImage.setOwnerStateName(stateName);
            log.debug(
                    "Set owner state '{}' for StateImage '{}' (was: '{}')",
                    stateName,
                    stateImage.getName(),
                    previousOwner);
        }

        // Set owner state name for all StateStrings
        for (io.github.jspinak.brobot.model.state.StateString stateString :
                components.getStateStrings()) {
            stateString.setOwnerStateName(stateName);
            log.trace(
                    "Set owner state '{}' for StateString '{}'", stateName, stateString.getName());
        }

        // Set owner state name for all StateObjects (handle specific types)
        for (io.github.jspinak.brobot.model.state.StateObject stateObject :
                components.getStateObjects()) {
            // StateObject is a base class - we need to check for specific implementations
            if (stateObject instanceof io.github.jspinak.brobot.model.state.StateLocation) {
                ((io.github.jspinak.brobot.model.state.StateLocation) stateObject)
                        .setOwnerStateName(stateName);
                log.trace(
                        "Set owner state '{}' for StateLocation '{}'",
                        stateName,
                        stateObject.getName());
            } else if (stateObject instanceof io.github.jspinak.brobot.model.state.StateRegion) {
                ((io.github.jspinak.brobot.model.state.StateRegion) stateObject)
                        .setOwnerStateName(stateName);
                log.trace(
                        "Set owner state '{}' for StateRegion '{}'",
                        stateName,
                        stateObject.getName());
            } else {
                log.trace(
                        "StateObject '{}' type {} does not support owner state name",
                        stateObject.getName(),
                        stateObject.getClass().getSimpleName());
            }
        }

        log.debug(
                "Set owner state name '{}' for {} components",
                stateName,
                components.getTotalComponents());
    }

    /** Derives the state name from the class and annotation. */
    private String deriveStateName(
            Class<?> stateClass, io.github.jspinak.brobot.annotations.State annotation) {
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
