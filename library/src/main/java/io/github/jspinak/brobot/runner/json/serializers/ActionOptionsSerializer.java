package io.github.jspinak.brobot.runner.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Custom serializer for {@link ActionOptions} that provides controlled serialization
 * to avoid circular reference issues and handle complex object graphs.
 * 
 * <p>This serializer addresses several challenges when serializing ActionOptions:</p>
 * <ul>
 *   <li><b>Circular References:</b> ActionOptions contains fields like {@code tempFind},
 *       {@code successCriteria}, and {@code actionLifecycle} that can create circular
 *       dependencies when serialized, leading to infinite recursion.</li>
 *   <li><b>Complex Object Graphs:</b> Some fields reference other complex objects that
 *       may not have proper serialization support or contain native resources.</li>
 *   <li><b>Selective Field Serialization:</b> Only serializes primitive fields and
 *       simple objects that can be safely converted to JSON.</li>
 * </ul>
 * 
 * <p><b>Serialization Strategy:</b></p>
 * <p>Uses reflection to iterate through all fields of ActionOptions, serializing only
 * those that are safe and avoiding problematic fields that could cause serialization
 * failures. Fields are serialized as key-value pairs where the key is the field name
 * and the value is the field's content.</p>
 * 
 * <p><b>Excluded Fields:</b></p>
 * <ul>
 *   <li>{@code tempFind} - Can contain circular references to other action objects</li>
 *   <li>{@code successCriteria} - Complex object that may reference parent objects</li>
 *   <li>{@code actionLifecycle} - Contains state management that can create cycles</li>
 * </ul>
 * 
 * @see ActionOptions
 * @see JsonSerializer
 * @see com.fasterxml.jackson.databind.ObjectMapper
 */
@Component
public class ActionOptionsSerializer extends JsonSerializer<ActionOptions> {

    private static final Logger log = LoggerFactory.getLogger(ActionOptionsSerializer.class);

    /**
     * Serializes an ActionOptions instance to JSON format, selectively including only
     * safe fields and excluding those that could cause circular references.
     * 
     * <p>The serialization process:</p>
     * <ol>
     *   <li>Opens a JSON object</li>
     *   <li>Uses reflection to access all declared fields in ActionOptions</li>
     *   <li>Skips problematic fields (tempFind, successCriteria, actionLifecycle)</li>
     *   <li>Writes non-null field values as JSON properties</li>
     *   <li>Closes the JSON object</li>
     * </ol>
     * 
     * @param actionOptions the ActionOptions instance to serialize
     * @param gen the JsonGenerator used to write JSON content
     * @param provider the SerializerProvider that can be used to get serializers for other objects
     * @throws IOException if there's an error writing to the JsonGenerator
     */
    @Override
    public void serialize(ActionOptions actionOptions, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        // Handle all primitive fields using reflection
        Field[] fields = ActionOptions.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();

            // Skip problematic fields
            if (fieldName.equals("tempFind") ||
                    fieldName.equals("successCriteria") ||
                    fieldName.equals("actionLifecycle")) {
                continue;
            }

            try {
                Object value = field.get(actionOptions);
                if (value != null) {
                    gen.writeObjectField(fieldName, value);
                }
            } catch (IllegalAccessException e) {
                log.warn("Cannot access field: {}", fieldName);
            }
        }

        gen.writeEndObject();
    }
}
