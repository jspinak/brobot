package io.github.jspinak.brobot.runner.json.utils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

/**
 * Specialized JSON serialization utility for ObjectCollection in the Brobot framework.
 *
 * <p>ObjectCollection is a heterogeneous container holding various GUI element types (images,
 * regions, locations, strings, matches, scenes) that serve as action targets. This utility handles
 * the complex serialization challenges arising from the diverse object types and their nested
 * structures, particularly focusing on non-serializable components like OpenCV Mat objects.
 *
 * <p>Key serialization challenges addressed:
 *
 * <ul>
 *   <li><b>Heterogeneous content</b>: Safely serializes different object types (StateImage,
 *       StateLocation, StateRegion, StateString) in a single collection
 *   <li><b>Match objects</b>: Delegates to MatchesJsonUtils for proper handling of ActionResult
 *       objects that may contain Mat references
 *   <li><b>Scene objects</b>: Extracts only essential metadata (filenames) from Scene objects to
 *       avoid serializing large image data
 *   <li><b>Circular references</b>: Prevents infinite recursion when objects reference their parent
 *       collections or states
 * </ul>
 *
 * <p>This utility enables critical functionality:
 *
 * <ul>
 *   <li>Saving collections of GUI elements for offline automation
 *   <li>Transmitting element collections between distributed systems
 *   <li>Creating test fixtures with predefined element collections
 *   <li>Debugging by inspecting serialized element hierarchies
 * </ul>
 *
 * <p>The Map-based approach provides flexibility to customize serialization for each element type
 * while maintaining a consistent structure that can be reliably deserialized back to functional
 * ObjectCollection instances.
 *
 * @see ObjectCollection
 * @see MatchesJsonUtils
 * @see io.github.jspinak.brobot.model.state.StateObject
 * @since 1.0
 */
@Component
public class ObjectCollectionJsonUtils {

    private static final Logger log = LoggerFactory.getLogger(ObjectCollectionJsonUtils.class);
    private final JsonUtils jsonUtils;
    private final MatchesJsonUtils matchesJsonUtils;
    private final ConfigurationParser jsonParser;

    public ObjectCollectionJsonUtils(
            JsonUtils jsonUtils,
            MatchesJsonUtils matchesJsonUtils,
            ConfigurationParser jsonParser) {
        this.jsonUtils = jsonUtils;
        this.matchesJsonUtils = matchesJsonUtils;
        this.jsonParser = jsonParser;
    }

    /**
     * Converts ObjectCollection to a Map representation for controlled serialization.
     *
     * <p>This method transforms the complex ObjectCollection structure into a simplified Map that
     * can be safely serialized to JSON. Each element type is handled according to its specific
     * serialization requirements:
     *
     * <p>Serialization strategy by type:
     *
     * <ul>
     *   <li><b>State elements</b>: StateLocations, StateImages, StateRegions, and StateStrings are
     *       included directly as they are inherently serializable
     *   <li><b>Matches</b>: Delegated to MatchesJsonUtils.matchesToMap() to handle complex
     *       ActionResult objects with potential Mat references
     *   <li><b>Scenes</b>: Reduced to filename metadata only, avoiding serialization of large image
     *       data stored in Pattern objects
     * </ul>
     *
     * <p>This selective approach ensures that:
     *
     * <ul>
     *   <li>All essential configuration data is preserved
     *   <li>Non-serializable components are safely excluded
     *   <li>The resulting JSON remains compact and readable
     *   <li>Deserialization can reconstruct functional collections
     * </ul>
     *
     * @param collection The ObjectCollection to convert
     * @return Map containing serializable representations of all collection elements
     */
    public Map<String, Object> objectCollectionToMap(ObjectCollection collection) {
        Map<String, Object> map = new HashMap<>();

        // Add collection fields
        map.put("stateLocations", collection.getStateLocations());
        map.put("stateImages", collection.getStateImages());
        map.put("stateRegions", collection.getStateRegions());
        map.put("stateStrings", collection.getStateStrings());

        // For matches, create simplified versions
        map.put(
                "matches",
                collection.getMatches().stream().map(matchesJsonUtils::matchesToMap).toList());

        // For scenes, just include filenames
        map.put(
                "scenes",
                collection.getScenes().stream()
                        .map(scene -> Map.of("filename", scene.getPattern().getName()))
                        .toList());

        return map;
    }

    /**
     * Serializes ObjectCollection to a JSON string representation.
     *
     * <p>This method provides safe JSON serialization for ObjectCollection instances, handling all
     * the complexity of heterogeneous element types and nested structures. The resulting JSON
     * preserves all necessary data for reconstructing the collection while avoiding serialization
     * pitfalls.
     *
     * <p>Usage example:
     *
     * <pre>{@code
     * ObjectCollection collection = new ObjectCollection.Builder()
     *     .withImages(stateImage1, stateImage2)
     *     .withRegions(region1, region2)
     *     .withMatches(previousResults)
     *     .build();
     *
     * String json = objectCollectionJsonUtils.objectCollectionToJson(collection);
     * // Use for persistence, transmission, or debugging
     * }</pre>
     *
     * <p>The serialization process automatically handles:
     *
     * <ul>
     *   <li>Complex object graphs with proper reference handling
     *   <li>Exclusion of non-serializable fields
     *   <li>Optimization of large data structures like Scenes
     * </ul>
     *
     * @param collection The ObjectCollection to serialize
     * @return JSON string representation of the collection
     * @throws ConfigurationException if serialization fails
     */
    public String objectCollectionToJson(ObjectCollection collection)
            throws ConfigurationException {
        return jsonUtils.toJsonSafe(collection);
    }

    /**
     * Creates a deep copy of ObjectCollection through serialization.
     *
     * <p>This method implements a robust deep copy mechanism that ensures complete independence
     * between the original and copied ObjectCollection. This is essential for scenarios where
     * collections need to be modified without affecting the original, such as in parallel
     * processing or state management.
     *
     * <p>Deep copy characteristics:
     *
     * <ul>
     *   <li>All element lists are independently copied
     *   <li>Nested objects like Matches are fully cloned
     *   <li>No references are shared between original and copy
     *   <li>Scene references are preserved through filename metadata
     * </ul>
     *
     * <p>Common use cases:
     *
     * <ul>
     *   <li>Creating variants of element collections for testing
     *   <li>Preserving original collections before modifications
     *   <li>Distributing collections across parallel execution threads
     *   <li>Implementing undo/redo functionality
     * </ul>
     *
     * <p>Note: While the copy is independent, Scene objects will need to reload their image data
     * from disk as Mat objects are not serialized.
     *
     * @param collection The ObjectCollection to copy
     * @return A new ObjectCollection instance with no shared references
     * @throws ConfigurationException if the copy operation fails
     */
    public ObjectCollection deepCopy(ObjectCollection collection) throws ConfigurationException {
        String json = objectCollectionToJson(collection);
        try {
            return jsonParser.convertJson(json, ObjectCollection.class);
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Failed to create deep copy of ObjectCollection", e);
        }
    }
}
