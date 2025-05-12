package io.github.jspinak.brobot.json.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import io.github.jspinak.brobot.dsl.ActionStep;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that provides additional JSON functionality focused on
 * handling problematic Java classes (circular references, module system issues).
 */
@Component
public class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    private final JsonParser jsonParser;
    private final io.github.jspinak.brobot.json.parsing.ObjectMapper objectMapper;
    private final ObjectMapper fallbackMapper;
    private final ObjectMapper circularReferenceMapper;

    public JsonUtils(JsonParser jsonParser, io.github.jspinak.brobot.json.parsing.ObjectMapper objectMapper) {
        this.jsonParser = jsonParser;
        this.objectMapper = objectMapper;
        this.fallbackMapper = createFallbackMapper();
        this.circularReferenceMapper = createCircularReferenceMapper();
    }

    /**
     * Creates a specialized mapper to handle problematic serialization scenarios
     */
    private ObjectMapper createFallbackMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Add a custom serializer for ActionDefinition
        SimpleModule module = new SimpleModule();
        module.addSerializer(ActionDefinition.class, new ActionDefinitionSerializer());
        mapper.registerModule(module);

        // Exclude problematic fields that often cause serialization issues
        mapper.setFilterProvider(new SimpleFilterProvider()
                .setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept(
                        // Image-related fields
                        "bufferedImage", "image", "matBGR", "matHSV", "mat",
                        // Java AWT fields
                        "raster", "colorModel", "data", "graphics",
                        // Sikuli-related fields
                        "sikuli", "screen", "robot", "mouseRobot",
                        // Location-related fields
                        "opposite", "oppositeToLocation",
                        // Potentially circular references
                        "reference"))
                .setFailOnUnknownId(false));

        return mapper;
    }

    /**
     * Creates a mapper specifically designed to handle circular references
     */
    private ObjectMapper createCircularReferenceMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Add a custom serializer for ActionDefinition
        SimpleModule module = new SimpleModule();
        module.addSerializer(ActionDefinition.class, new ActionDefinitionSerializer());
        mapper.registerModule(module);

        // Enable detection of circular references
        mapper.enable(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL);

        // Configure to use fields directly rather than getters
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);

        // Exclude problematic fields
        mapper.setFilterProvider(new SimpleFilterProvider()
                .setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept(
                        "bufferedImage", "image", "matBGR", "matHSV", "mat",
                        "raster", "colorModel", "data", "graphics",
                        "sikuli", "screen", "robot", "mouseRobot", "reference",
                        "opposite", "oppositeToLocation"))
                .setFailOnUnknownId(false));

        return mapper;
    }

    /**
     * Custom serializer for ActionDefinition to handle all the problematic references
     */
    public static class ActionDefinitionSerializer extends JsonSerializer<ActionDefinition> {
        @Override
        public void serialize(ActionDefinition definition, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();

            // Write the type field
            gen.writeStringField("type", "ActionDefinition");

            // Start steps array
            gen.writeArrayFieldStart("steps");

            List<ActionStep> steps = getStepsFromActionDefinition(definition);
            if (steps != null) {
                for (ActionStep step : steps) {
                    gen.writeStartObject();

                    // Write action options
                    gen.writeObjectFieldStart("actionOptions");
                    Object action = getActionFromStep(step);
                    if (action != null) {
                        gen.writeStringField("action", action.toString());
                    } else {
                        gen.writeStringField("action", "UNKNOWN");
                    }
                    gen.writeEndObject(); // End actionOptions

                    // Write object collection
                    gen.writeObjectFieldStart("objectCollection");
                    gen.writeArrayFieldStart("stateImages");
                    gen.writeEndArray();
                    gen.writeArrayFieldStart("stateRegions");
                    gen.writeEndArray();
                    gen.writeEndObject(); // End objectCollection

                    gen.writeEndObject(); // End step
                }
            }

            gen.writeEndArray(); // End steps
            gen.writeEndObject(); // End definition
        }
    }

    /**
     * Safely extract steps from ActionDefinition using reflection
     */
    private static List<ActionStep> getStepsFromActionDefinition(ActionDefinition definition) {
        try {
            Method getStepsMethod = ActionDefinition.class.getMethod("getSteps");
            @SuppressWarnings("unchecked")
            List<ActionStep> steps = (List<ActionStep>) getStepsMethod.invoke(definition);
            return steps;
        } catch (Exception e) {
            log.debug("Could not get steps using getSteps method: {}", e.getMessage());

            // Try with field access
            try {
                Field stepsField = ActionDefinition.class.getDeclaredField("steps");
                stepsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<ActionStep> steps = (List<ActionStep>) stepsField.get(definition);
                return steps;
            } catch (Exception ex) {
                log.warn("Could not get steps using field access: {}", ex.getMessage());
            }
        }

        return new ArrayList<>();
    }

    /**
     * Safely extract action from ActionStep using reflection
     */
    private static Object getActionFromStep(ActionStep step) {
        try {
            Method getActionOptionsMethod = ActionStep.class.getMethod("getActionOptions");
            Object actionOptions = getActionOptionsMethod.invoke(step);

            if (actionOptions != null) {
                Method getActionMethod = actionOptions.getClass().getMethod("getAction");
                return getActionMethod.invoke(actionOptions);
            }
        } catch (Exception e) {
            log.debug("Could not get action from step: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Safely serializes an object to JSON, handling circular references and module system issues
     */
    public String toJsonSafe(Object object) throws ConfigurationException {
        if (object instanceof ActionDefinition) {
            log.info("Serializing ActionDefinition using custom serializer");
        }

        try {
            // First try with the circular reference mapper
            return circularReferenceMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Circular reference serialization failed: {}", e.getMessage());
            try {
                // Then try with the fallback mapper
                return fallbackMapper.writeValueAsString(object);
            } catch (JsonProcessingException ex) {
                log.warn("Fallback serialization failed: {}", ex.getMessage());
                try {
                    // Last resort - try with a minimal approach
                    return createMinimalStructure(object);
                } catch (Exception exc) {
                    log.error("Failed to serialize object using all methods: {}", exc.getMessage());
                    throw new ConfigurationException("Failed to serialize object safely: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Safely serializes an object to pretty-printed JSON, handling circular references and module system issues
     */
    public String toPrettyJsonSafe(Object object) throws ConfigurationException {
        if (object instanceof ActionDefinition) {
            log.info("Pretty printing ActionDefinition using custom serializer");
        }

        try {
            // First try with the circular reference mapper
            return circularReferenceMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Circular reference pretty serialization failed: {}", e.getMessage());
            try {
                // Then try with the fallback mapper
                return fallbackMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } catch (JsonProcessingException ex) {
                log.warn("Fallback pretty serialization failed: {}", ex.getMessage());
                try {
                    // Last resort - try with a minimal approach
                    return createMinimalStructurePretty(object);
                } catch (Exception exc) {
                    log.error("Failed to pretty serialize object using all methods: {}", exc.getMessage());
                    throw new ConfigurationException("Failed to serialize object safely: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Creates a minimal JSON structure for any object when all other serialization methods fail
     */
    private String createMinimalStructure(Object object) throws JsonProcessingException {
        if (object instanceof ActionDefinition) {
            // Direct handling for ActionDefinition
            ActionDefinition definition = (ActionDefinition) object;
            List<ActionStep> steps = getStepsFromActionDefinition(definition);

            StringBuilder json = new StringBuilder();
            json.append("{\"type\":\"ActionDefinition\",\"steps\":[");

            for (int i = 0; i < steps.size(); i++) {
                ActionStep step = steps.get(i);
                Object action = getActionFromStep(step);
                String actionStr = (action != null) ? action.toString() : "UNKNOWN";

                json.append("{\"actionOptions\":{\"action\":\"").append(actionStr).append("\"},")
                        .append("\"objectCollection\":{\"stateImages\":[],\"stateRegions\":[]}}");

                if (i < steps.size() - 1) {
                    json.append(",");
                }
            }

            json.append("]}");
            return json.toString();
        } else {
            // Generic minimal structure for other objects
            return "{\"type\":\"" + object.getClass().getSimpleName() + "\",\"name\":\"" +
                    getObjectName(object) + "\"}";
        }
    }

    /**
     * Creates a minimal pretty-printed JSON structure
     */
    private String createMinimalStructurePretty(Object object) throws JsonProcessingException {
        String json = createMinimalStructure(object);

        // Use a temporary ObjectMapper just for pretty printing
        ObjectMapper prettyMapper = new ObjectMapper();
        prettyMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            // Parse and re-format
            Object jsonObj = prettyMapper.readValue(json, Object.class);
            return prettyMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
        } catch (Exception e) {
            // If that doesn't work, use simple indentation
            return json.replace("{", "{\n  ")
                    .replace("}", "\n}")
                    .replace(",", ",\n  ")
                    .replace(":[", ": [\n    ")
                    .replace("],", "\n  ],")
                    .replace("]}", "\n]}");
        }
    }

    /**
     * Try to get the name property of an object, or return a default
     */
    private String getObjectName(Object object) {
        try {
            // Try to find a name property using reflection
            Method getNameMethod = object.getClass().getMethod("getName");
            Object nameObj = getNameMethod.invoke(object);
            if (nameObj != null) {
                return nameObj.toString();
            }
        } catch (Exception e) {
            // Ignore - no getName method or other reflection error
        }

        // Fallback to class name
        return object.getClass().getSimpleName() + "@" + Integer.toHexString(object.hashCode());
    }

    /**
     * Writes an object to a file using safe serialization
     */
    public void writeToFileSafe(Object object, Path filePath) throws ConfigurationException, IOException {
        String json = toPrettyJsonSafe(object);
        Files.writeString(filePath, json);
    }

    /**
     * Converts an object to JSON and back to ensure it can be safely serialized
     * This is useful for validating that an object can be properly serialized and deserialized
     */
    public <T> T validateSerializationCycle(Object object, Class<T> clazz) throws ConfigurationException {
        String json = toJsonSafe(object);
        return jsonParser.convertJson(json, clazz);
    }
}