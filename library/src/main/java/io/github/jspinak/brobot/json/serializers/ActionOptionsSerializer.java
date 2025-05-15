package io.github.jspinak.brobot.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Custom serializer for ActionOptions to handle special cases
 */
@Component
public class ActionOptionsSerializer extends JsonSerializer<ActionOptions> {

    private static final Logger log = LoggerFactory.getLogger(ActionOptionsSerializer.class);

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
