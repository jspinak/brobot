package io.github.jspinak.brobot.action.internal.region;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateObject;

import lombok.extern.slf4j.Slf4j;

/**
 * Registry that tracks dependencies between StateObjects for search region resolution. When an
 * object A is found, this registry helps identify all objects that depend on A for their search
 * region definition.
 */
@Component
@Slf4j
public class SearchRegionDependencyRegistry {

    /** Maps from source object (stateName.objectName) to list of dependent objects */
    private final Map<String, Set<DependentObject>> dependencies = new ConcurrentHashMap<>();

    /** Represents an object that depends on another for its search region */
    public static class DependentObject {
        private final StateObject stateObject;
        private final SearchRegionOnObject searchRegionConfig;

        public DependentObject(StateObject stateObject, SearchRegionOnObject searchRegionConfig) {
            this.stateObject = stateObject;
            this.searchRegionConfig = searchRegionConfig;
        }

        public StateObject getStateObject() {
            return stateObject;
        }

        public SearchRegionOnObject getSearchRegionConfig() {
            return searchRegionConfig;
        }
    }

    /**
     * Registers a dependency where the dependent object's search region depends on the source
     * object's location.
     *
     * @param dependent The object whose search region depends on another
     * @param searchRegionConfig The configuration defining the dependency
     */
    public void registerDependency(StateObject dependent, SearchRegionOnObject searchRegionConfig) {
        if (searchRegionConfig == null || dependent == null) {
            return;
        }

        String sourceKey =
                buildKey(
                        searchRegionConfig.getTargetStateName(),
                        searchRegionConfig.getTargetObjectName());

        dependencies
                .computeIfAbsent(sourceKey, k -> ConcurrentHashMap.newKeySet())
                .add(new DependentObject(dependent, searchRegionConfig));

        log.info(
                "Registered search region dependency: {} depends on {}",
                buildKey(dependent.getOwnerStateName(), dependent.getName()),
                sourceKey);
    }

    /**
     * Gets all objects that depend on the specified source object for their search regions.
     *
     * @param sourceStateName The state name of the source object
     * @param sourceObjectName The object name of the source object
     * @return Set of dependent objects, or empty set if none
     */
    public Set<DependentObject> getDependents(String sourceStateName, String sourceObjectName) {
        String sourceKey = buildKey(sourceStateName, sourceObjectName);
        Set<DependentObject> dependents =
                dependencies.getOrDefault(sourceKey, Collections.emptySet());
        log.debug("Getting dependents for {}: found {} dependents", sourceKey, dependents.size());
        return dependents;
    }

    /** Clears all registered dependencies. */
    public void clear() {
        dependencies.clear();
    }

    /** Builds a unique key for state object identification. */
    private String buildKey(String stateName, String objectName) {
        return stateName + "." + objectName;
    }

    /** Gets the total number of registered dependencies. */
    public int size() {
        return dependencies.values().stream().mapToInt(Set::size).sum();
    }
}
