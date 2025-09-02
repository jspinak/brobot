package io.github.jspinak.brobot.runner.json.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.serializers.ActionConfigDeserializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Specialized JSON serialization utility for ActionConfig objects in the Brobot framework.
 * 
 * <p>ActionConfig is the base class for all action configurations in the modern Brobot API,
 * replacing the monolithic ActionOptions. This utility provides safe serialization and
 * deserialization for the ActionConfig hierarchy, handling polymorphic types through
 * type discriminators.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Polymorphic handling</b>: Supports all ActionConfig subclasses (ClickOptions, 
 *       FindOptions, TypeOptions, etc.) through type discrimination</li>
 *   <li><b>Type safety</b>: Ensures correct concrete type is restored during deserialization</li>
 *   <li><b>Deep copying</b>: Enables true deep copies through serialization/deserialization</li>
 *   <li><b>Configuration persistence</b>: Allows configurations to be saved and restored 
 *       across sessions</li>
 * </ul>
 * </p>
 * 
 * <p>This utility is essential for:
 * <ul>
 *   <li>Saving action configurations to JSON files for reuse</li>
 *   <li>Creating independent copies of configurations for parallel execution</li>
 *   <li>Transmitting action configurations over network protocols</li>
 *   <li>Converting between different ActionConfig types</li>
 * </ul>
 * </p>
 * 
 * @see ActionConfig
 * @see ActionConfigDeserializer
 * @see JsonUtils
 * @since 2.0
 */
@Component
public class ActionConfigJsonUtils {

    private static final Logger log = LoggerFactory.getLogger(ActionConfigJsonUtils.class);
    private final ObjectMapper objectMapper;
    private final JsonUtils jsonUtils;
    private final ConfigurationParser jsonParser;

    /**
     * Custom serializer for OpenCV Mat that serializes it as null.
     * This prevents issues with Mat's conflicting setter methods.
     */
    static class MatSerializer extends JsonSerializer<org.bytedeco.opencv.opencv_core.Mat> {
        @Override
        public void serialize(org.bytedeco.opencv.opencv_core.Mat value, JsonGenerator gen, 
                              SerializerProvider serializers) throws IOException {
            gen.writeNull();
        }
    }
    
    /**
     * Custom deserializer for OpenCV Mat that always returns null.
     * This prevents issues with Mat's conflicting setter methods.
     */
    static class MatDeserializer extends JsonDeserializer<org.bytedeco.opencv.opencv_core.Mat> {
        @Override
        public org.bytedeco.opencv.opencv_core.Mat deserialize(JsonParser p, DeserializationContext ctxt) 
                throws IOException {
            p.skipChildren();
            return null;
        }
    }

    public ActionConfigJsonUtils(JsonUtils jsonUtils, ConfigurationParser jsonParser) {
        this.jsonUtils = jsonUtils;
        this.jsonParser = jsonParser;
        
        // Create a custom ObjectMapper with ActionConfig support
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Configure to ignore Mat serialization issues
        this.objectMapper.configure(com.fasterxml.jackson.databind.MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, true);
        this.objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
        
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ActionConfig.class, new ActionConfigDeserializer());
        
        // Add custom serializer/deserializer for OpenCV Mat to prevent conflicts
        try {
            module.addSerializer(org.bytedeco.opencv.opencv_core.Mat.class, new MatSerializer());
            module.addDeserializer(org.bytedeco.opencv.opencv_core.Mat.class, new MatDeserializer());
        } catch (Exception e) {
            log.debug("Could not add Mat serializers: " + e.getMessage());
        }
        
        this.objectMapper.registerModule(module);
        this.objectMapper.findAndRegisterModules();
    }

    /**
     * Serializes an ActionConfig to a JSON string representation.
     * 
     * <p>This method handles polymorphic serialization by including type information
     * in the JSON output. The resulting JSON can be deserialized back to the correct
     * concrete ActionConfig subclass.</p>
     * 
     * <p>Usage example:
     * <pre>{@code
     * ClickOptions clickOptions = new ClickOptions.Builder()
     *     .setNumberOfClicks(2)
     *     .setPauseBeforeBegin(0.5)
     *     .build();
     * 
     * String json = actionConfigJsonUtils.toJson(clickOptions);
     * // JSON will include @type field for polymorphic deserialization
     * }</pre>
     * </p>
     * 
     * @param actionConfig The ActionConfig to serialize
     * @return JSON string representation of the ActionConfig
     * @throws ConfigurationException if serialization fails
     */
    public String toJson(ActionConfig actionConfig) throws ConfigurationException {
        try {
            // Add type information for polymorphic deserialization
            String json = objectMapper.writeValueAsString(actionConfig);
            
            // Insert @type field if not present
            if (!json.contains("\"@type\"") && !json.contains("\"type\"")) {
                String typeName = actionConfig.getClass().getSimpleName();
                json = json.substring(0, 1) + "\"@type\":\"" + typeName + "\"," + json.substring(1);
            }
            
            return json;
        } catch (JsonProcessingException e) {
            throw new ConfigurationException("Failed to serialize ActionConfig to JSON", e);
        }
    }

    /**
     * Deserializes a JSON string to an ActionConfig instance.
     * 
     * <p>This method uses the ActionConfigDeserializer to determine the correct
     * concrete type based on the @type field or other type discriminators in the JSON.</p>
     * 
     * <p>Supported types include:
     * <ul>
     *   <li>ClickOptions</li>
     *   <li>BaseFindOptions and its subclasses</li>
     *   <li>TypeOptions</li>
     *   <li>DragOptions</li>
     *   <li>MouseMoveOptions, MouseDownOptions, MouseUpOptions</li>
     *   <li>And all other ActionConfig subclasses</li>
     * </ul>
     * </p>
     * 
     * @param json The JSON string to deserialize
     * @return The deserialized ActionConfig instance of the correct concrete type
     * @throws ConfigurationException if deserialization fails or type is unknown
     */
    public ActionConfig fromJson(String json) throws ConfigurationException {
        try {
            return objectMapper.readValue(json, ActionConfig.class);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to deserialize ActionConfig from JSON", e);
        }
    }

    /**
     * Creates a deep copy of an ActionConfig through serialization.
     * 
     * <p>This method implements a serialization-based deep copy strategy that ensures 
     * complete independence between the original and copied ActionConfig. The copy
     * will be of the same concrete type as the original.</p>
     * 
     * <p>Benefits:
     * <ul>
     *   <li>Guarantees true deep copy with no shared references</li>
     *   <li>Preserves the concrete type (e.g., ClickOptions remains ClickOptions)</li>
     *   <li>Handles complex nested objects automatically</li>
     *   <li>Thread-safe - can be used for parallel execution scenarios</li>
     * </ul>
     * </p>
     * 
     * <p>Common use cases:
     * <ul>
     *   <li>Creating action variations for testing</li>
     *   <li>Preserving original configuration while experimenting</li>
     *   <li>Parallel execution with different parameters</li>
     * </ul>
     * </p>
     * 
     * @param actionConfig The ActionConfig to copy
     * @param <T> The concrete type of ActionConfig
     * @return A new instance with no shared references
     * @throws ConfigurationException if serialization or deserialization fails
     */
    @SuppressWarnings("unchecked")
    public <T extends ActionConfig> T deepCopy(T actionConfig) throws ConfigurationException {
        String json = toJson(actionConfig);
        ActionConfig copy = fromJson(json);
        
        // Verify the copy is of the same type
        if (!copy.getClass().equals(actionConfig.getClass())) {
            throw new ConfigurationException(
                String.format("Type mismatch during deep copy: expected %s, got %s",
                    actionConfig.getClass().getSimpleName(),
                    copy.getClass().getSimpleName())
            );
        }
        
        return (T) copy;
    }

    /**
     * Converts one type of ActionConfig to another.
     * 
     * <p>This method can be useful when migrating from one action type to another
     * or when creating a new action based on an existing one's configuration.</p>
     * 
     * <p>Note: Only compatible fields will be copied. Fields specific to the source
     * type that don't exist in the target type will be ignored.</p>
     * 
     * @param source The source ActionConfig
     * @param targetClass The target ActionConfig class
     * @param <S> The source type
     * @param <T> The target type
     * @return A new instance of the target type with compatible fields copied
     * @throws ConfigurationException if conversion fails
     */
    public <S extends ActionConfig, T extends ActionConfig> T convert(
            S source, Class<T> targetClass) throws ConfigurationException {
        try {
            // Serialize source without type information
            String json = objectMapper.writeValueAsString(source);
            
            // Add target type information
            String targetTypeName = targetClass.getSimpleName();
            json = json.substring(0, 1) + "\"@type\":\"" + targetTypeName + "\"," + json.substring(1);
            
            // Deserialize as target type
            return objectMapper.readValue(json, targetClass);
        } catch (IOException e) {
            throw new ConfigurationException(
                String.format("Failed to convert %s to %s",
                    source.getClass().getSimpleName(),
                    targetClass.getSimpleName()),
                e
            );
        }
    }
}