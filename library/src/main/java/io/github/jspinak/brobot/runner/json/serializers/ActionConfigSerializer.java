package io.github.jspinak.brobot.runner.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.jspinak.brobot.action.ActionConfig;

import java.io.IOException;

/**
 * Custom serializer for ActionConfig hierarchy that adds a type discriminator field.
 * This works in conjunction with ActionConfigDeserializer for polymorphic serialization.
 */
public class ActionConfigSerializer extends JsonSerializer<ActionConfig> {
    
    @Override
    public void serialize(ActionConfig value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        
        gen.writeStartObject();
        
        // Add type discriminator based on class name
        String typeName = value.getClass().getSimpleName();
        gen.writeStringField("@type", typeName);
        
        // Serialize all fields
        serializers.defaultSerializeValue(value, gen);
        
        gen.writeEndObject();
    }
    
    @Override
    public Class<ActionConfig> handledType() {
        return ActionConfig.class;
    }
}