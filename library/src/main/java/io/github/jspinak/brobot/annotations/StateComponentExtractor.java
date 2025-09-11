package io.github.jspinak.brobot.annotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateString;

import lombok.extern.slf4j.Slf4j;

/**
 * Extracts Brobot state components (StateImage, StateString, StateObject) from annotated state
 * classes.
 *
 * <p>This class is responsible solely for extracting state components through reflection. It does
 * not handle state building or registration, following the Single Responsibility Principle.
 *
 * <p>The extractor looks for fields of the following types:
 *
 * <ul>
 *   <li>StateImage - Visual elements used for pattern matching
 *   <li>StateString - Text elements for typing or verification
 *   <li>StateObject - Generic state objects
 * </ul>
 *
 * @since 1.1.0
 */
@Component
@Slf4j
public class StateComponentExtractor {

    /** Container for extracted state components. */
    public static class StateComponents {
        private final List<StateImage> stateImages = new ArrayList<>();
        private final List<StateString> stateStrings = new ArrayList<>();
        private final List<StateObject> stateObjects = new ArrayList<>();

        public List<StateImage> getStateImages() {
            return stateImages;
        }

        public List<StateString> getStateStrings() {
            return stateStrings;
        }

        public List<StateObject> getStateObjects() {
            return stateObjects;
        }

        public boolean isEmpty() {
            return stateImages.isEmpty() && stateStrings.isEmpty() && stateObjects.isEmpty();
        }

        public int getTotalComponents() {
            return stateImages.size() + stateStrings.size() + stateObjects.size();
        }
    }

    /**
     * Extracts state components from a state instance.
     *
     * @param stateInstance The instance of a @State annotated class
     * @return Container with all extracted components
     */
    public StateComponents extractComponents(Object stateInstance) {
        StateComponents components = new StateComponents();
        Class<?> stateClass = stateInstance.getClass();

        log.debug("Extracting components from state class: {}", stateClass.getSimpleName());

        // Process all declared fields including private ones
        for (Field field : stateClass.getDeclaredFields()) {
            extractField(field, stateInstance, components);
        }

        // Also process inherited fields
        Class<?> superClass = stateClass.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            for (Field field : superClass.getDeclaredFields()) {
                extractField(field, stateInstance, components);
            }
            superClass = superClass.getSuperclass();
        }

        log.debug(
                "Extracted {} total components from {}",
                components.getTotalComponents(),
                stateClass.getSimpleName());

        return components;
    }

    /** Extracts a single field if it's a state component type. */
    private void extractField(Field field, Object instance, StateComponents components) {
        try {
            field.setAccessible(true);
            Object value = field.get(instance);

            if (value == null) {
                return;
            }

            // Extract based on field type
            if (value instanceof StateImage) {
                components.getStateImages().add((StateImage) value);
                log.trace("Extracted StateImage: {}", ((StateImage) value).getName());
            } else if (value instanceof StateString) {
                components.getStateStrings().add((StateString) value);
                log.trace("Extracted StateString: {}", ((StateString) value).getName());
            } else if (value instanceof StateObject) {
                components.getStateObjects().add((StateObject) value);
                log.trace("Extracted StateObject: {}", ((StateObject) value).getName());
            }

        } catch (IllegalAccessException e) {
            log.warn(
                    "Failed to access field {} in class {}",
                    field.getName(),
                    instance.getClass().getSimpleName(),
                    e);
        }
    }
}
