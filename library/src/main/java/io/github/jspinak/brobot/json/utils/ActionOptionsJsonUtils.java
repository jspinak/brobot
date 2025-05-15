package io.github.jspinak.brobot.json.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for serializing ActionOptions to and from JSON.
 * Handles special cases like tempFind which can't be directly serialized.
 */
@Component
public class ActionOptionsJsonUtils {

    private static final Logger log = LoggerFactory.getLogger(ActionOptionsJsonUtils.class);
    private final JsonUtils jsonUtils;
    private final JsonParser jsonParser;

    public ActionOptionsJsonUtils(JsonUtils jsonUtils, JsonParser jsonParser) {
        this.jsonUtils = jsonUtils;
        this.jsonParser = jsonParser;
    }

    /**
     * Converts ActionOptions to a Map representation that's easier to work with
     * for custom serialization, excluding problematic fields
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
     * Serializes ActionOptions to JSON, handling special cases
     */
    public String actionOptionsToJson(ActionOptions actionOptions) throws ConfigurationException {
        return jsonUtils.toJsonSafe(actionOptions);
    }

    /**
     * Creates a deep copy of ActionOptions by serializing and deserializing
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