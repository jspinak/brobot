package io.github.jspinak.brobot.runner.json.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Specialized JSON serialization utility for ActionOptions objects in the Brobot framework.
 * 
 * <p>ActionOptions contains the full configuration for automation actions, including complex 
 * non-serializable fields like predicates, consumers, and temporary find operations. This 
 * utility provides safe serialization by intelligently handling these problematic fields 
 * while preserving all essential configuration data.</p>
 * 
 * <p>Key challenges addressed:
 * <ul>
 *   <li><b>Non-serializable fields</b>: Excludes fields like tempFind (ObjectCollection), 
 *       successCriteria (Predicate), and actionLifecycle (BiConsumer) that cannot be 
 *       directly serialized to JSON</li>
 *   <li><b>Deep copying</b>: Enables true deep copies of ActionOptions through 
 *       serialization/deserialization, avoiding reference sharing issues</li>
 *   <li><b>Selective serialization</b>: Converts ActionOptions to Map representation 
 *       for custom handling of specific fields</li>
 *   <li><b>Configuration persistence</b>: Allows ActionOptions to be saved and restored 
 *       across sessions while maintaining type safety</li>
 * </ul>
 * </p>
 * 
 * <p>This utility is essential for:
 * <ul>
 *   <li>Saving action configurations to files for reuse</li>
 *   <li>Creating independent copies of ActionOptions for parallel execution</li>
 *   <li>Transmitting action configurations over network protocols</li>
 *   <li>Debugging by inspecting serialized action configurations</li>
 * </ul>
 * </p>
 * 
 * <p>The selective serialization approach ensures that while functional fields are 
 * excluded, all configuration data needed to recreate equivalent ActionOptions 
 * instances is preserved.</p>
 * 
 * @see ActionOptions
 * @see JsonUtils
 * @see io.github.jspinak.brobot.action.Action
 * @since 1.0
 */
@Component
public class ActionOptionsJsonUtils {

    private static final Logger log = LoggerFactory.getLogger(ActionOptionsJsonUtils.class);
    private final JsonUtils jsonUtils;
    private final ConfigurationParser jsonParser;

    public ActionOptionsJsonUtils(JsonUtils jsonUtils, ConfigurationParser jsonParser) {
        this.jsonUtils = jsonUtils;
        this.jsonParser = jsonParser;
    }

    /**
     * Converts ActionOptions to a Map representation for selective serialization.
     * 
     * <p>This method uses reflection to iterate through all fields of ActionOptions and 
     * creates a Map containing only the serializable fields. This approach allows fine-grained 
     * control over which fields are included in the JSON representation.</p>
     * 
     * <p>Excluded fields:
     * <ul>
     *   <li><b>tempFind</b>: ObjectCollection that may contain non-serializable Mat objects</li>
     *   <li><b>successCriteria</b>: Predicate functional interface that cannot be serialized</li>
     *   <li><b>actionLifecycle</b>: BiConsumer for custom action behavior</li>
     * </ul>
     * </p>
     * 
     * <p>The resulting Map preserves all configuration data needed to reconstruct 
     * a functionally equivalent ActionOptions instance, making it suitable for 
     * persistence, transmission, or deep copying.</p>
     * 
     * @param actionOptions The ActionOptions instance to convert
     * @return Map containing all serializable fields and their values
     */
    public Map<String, Object> actionOptionsToMap(ActionOptions actionOptions) {
        Map<String, Object> map = new HashMap<>();

        Field[] fields = ActionOptions.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();

            // Skip non-serializable fields
            if (fieldName.equals("tempFind") ||
                    fieldName.equals("successCriteria") ||
                    fieldName.equals("actionLifecycle")) {
                continue;
            }

            try {
                Object value = field.get(actionOptions);
                if (value != null) {
                    map.put(fieldName, value);
                }
            } catch (IllegalAccessException e) {
                log.warn("Cannot access field: {}", fieldName);
            }
        }

        return map;
    }

    /**
     * Serializes ActionOptions to a JSON string representation.
     * 
     * <p>This method provides a safe way to convert ActionOptions to JSON by delegating 
     * to the framework's JsonUtils, which handles complex object graphs and circular 
     * references. The resulting JSON can be used for configuration persistence, 
     * debugging, or network transmission.</p>
     * 
     * <p>Usage example:
     * <pre>{@code
     * ActionOptions options = new ActionOptions.Builder()
     *     .setSimilarity(0.95)
     *     .setPauseAfterAction(500)
     *     .build();
     * 
     * String json = actionOptionsJsonUtils.actionOptionsToJson(options);
     * // Save to file or transmit over network
     * }</pre>
     * </p>
     * 
     * @param actionOptions The ActionOptions to serialize
     * @return JSON string representation of the ActionOptions
     * @throws ConfigurationException if serialization fails due to invalid data
     */
    public String actionOptionsToJson(ActionOptions actionOptions) throws ConfigurationException {
        return jsonUtils.toJsonSafe(actionOptions);
    }

    /**
     * Creates a deep copy of ActionOptions through serialization.
     * 
     * <p>This method implements a serialization-based deep copy strategy that ensures 
     * complete independence between the original and copied ActionOptions. This is 
     * crucial for scenarios where multiple actions need to run with slightly different 
     * configurations without interfering with each other.</p>
     * 
     * <p>Benefits of serialization-based copying:
     * <ul>
     *   <li>Guarantees true deep copy - no shared references</li>
     *   <li>Handles complex nested objects automatically</li>
     *   <li>Filters out non-serializable fields cleanly</li>
     *   <li>Validates data integrity through the serialization process</li>
     * </ul>
     * </p>
     * 
     * <p>Common use cases:
     * <ul>
     *   <li>Creating action variations for A/B testing</li>
     *   <li>Preserving original configuration while experimenting</li>
     *   <li>Parallel execution with different parameters</li>
     * </ul>
     * </p>
     * 
     * <p>Note: Non-serializable fields (tempFind, successCriteria, actionLifecycle) 
     * will be null in the copied instance and must be set separately if needed.</p>
     * 
     * @param actionOptions The ActionOptions to copy
     * @return A new ActionOptions instance with no shared references
     * @throws ConfigurationException if serialization or deserialization fails
     */
    public ActionOptions deepCopy(ActionOptions actionOptions) throws ConfigurationException {
        String json = actionOptionsToJson(actionOptions);
        try {
            return jsonParser.convertJson(json, ActionOptions.class);
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Failed to create deep copy of ActionOptions", e);
        }
    }
}