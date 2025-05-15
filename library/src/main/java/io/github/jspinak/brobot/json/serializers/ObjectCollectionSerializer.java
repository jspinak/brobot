package io.github.jspinak.brobot.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom serializer for ObjectCollection to handle special cases
 */
@Component
public class ObjectCollectionSerializer extends JsonSerializer<ObjectCollection> {
    @Override
    public void serialize(ObjectCollection collection, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        // Write state locations
        gen.writeObjectField("stateLocations", collection.getStateLocations());

        // Write state images
        gen.writeObjectField("stateImages", collection.getStateImages());

        // Write state regions
        gen.writeObjectField("stateRegions", collection.getStateRegions());

        // Write state strings
        gen.writeObjectField("stateStrings", collection.getStateStrings());

        // Write matches - but with care to avoid circular references
        // We'll just write simplified versions
        gen.writeArrayFieldStart("matches");
        for (int i = 0; i < collection.getMatches().size(); i++) {
            provider.defaultSerializeValue(collection.getMatches().get(i), gen);
        }
        gen.writeEndArray();

        // Write scenes - but skip the bufferedImage and Mat fields
        gen.writeObjectField("scenes", collection.getScenes());

        gen.writeEndObject();
    }
}

