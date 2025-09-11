package io.github.jspinak.brobot.runner.json.serializers;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import io.github.jspinak.brobot.action.ActionConfig;

/**
 * Custom serializer for ActionConfig hierarchy that adds a type discriminator field. This works in
 * conjunction with ActionConfigDeserializer for polymorphic serialization.
 */
@Component
public class ActionConfigSerializer extends StdSerializer<ActionConfig> {

    public ActionConfigSerializer() {
        super(ActionConfig.class);
    }

    @Override
    public void serialize(ActionConfig value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {

        gen.writeStartObject();

        // Add type discriminator based on class name
        String typeName = value.getClass().getSimpleName();
        gen.writeStringField("@type", typeName);

        // Get the bean serializer for the actual type
        JavaType javaType = provider.constructType(value.getClass());
        BeanDescription beanDesc = provider.getConfig().introspect(javaType);
        JsonSerializer<Object> beanSerializer =
                BeanSerializerFactory.instance.findBeanSerializer(provider, javaType, beanDesc);

        // Serialize all the bean properties
        if (beanSerializer != null) {
            beanSerializer.unwrappingSerializer(null).serialize(value, gen, provider);
        }

        gen.writeEndObject();
    }

    @Override
    public void serializeWithType(
            ActionConfig value,
            JsonGenerator gen,
            SerializerProvider provider,
            TypeSerializer typeSer)
            throws IOException {
        // For polymorphic serialization, we handle type information ourselves
        // by including the @type field in the serialized output
        serialize(value, gen, provider);
    }
}
