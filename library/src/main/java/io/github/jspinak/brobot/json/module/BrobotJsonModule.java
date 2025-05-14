package io.github.jspinak.brobot.json.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.json.utils.ActionOptionsJsonUtils;
import io.github.jspinak.brobot.json.utils.MatchesJsonUtils;
import io.github.jspinak.brobot.json.utils.ObjectCollectionJsonUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Jackson module for custom serialization/deserialization of Brobot classes.
 * Registers custom serializers for complex classes that require special handling.
 */
@Component
public class BrobotJsonModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public BrobotJsonModule() {
        super("BrobotJsonModule");
        configure();
    }

    /**
     * Configures the module with all necessary serializers and deserializers
     */
    public void configure() {
        // Register serializers for problematic classes
        addSerializer(ActionOptions.class, new ActionOptionsJsonUtils.ActionOptionsSerializer());
        addSerializer(Matches.class, new MatchesJsonUtils.MatchesSerializer());
        addSerializer(ObjectCollection.class, new ObjectCollectionJsonUtils.ObjectCollectionSerializer());
        addSerializer(Mat.class, new MatchesJsonUtils.MatSerializer());

        // Add serializers for Scene to properly handle image data
        addSerializer(Scene.class, new SceneSerializer());
        addDeserializer(Scene.class, new SceneDeserializer());

        addDeserializer(SearchRegions.class, new SearchRegionsDeserializer());
    }

    /**
     * Custom serializer for Scene objects that skips image data
     */
    public static class SceneSerializer extends JsonSerializer<Scene> {
        @Override
        public void serialize(Scene scene, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            if (scene.getId() != null) {
                gen.writeNumberField("id", scene.getId());
            }
            if (scene.getPattern() != null) {
                // This will use the refined serialization rules for Pattern
                gen.writeObjectField("pattern", scene.getPattern());
            }
            gen.writeEndObject();
        }
    }

    public static class SceneDeserializer extends JsonDeserializer<Scene> {
        @Override
        public Scene deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            ObjectCodec oc = jp.getCodec();
            JsonNode node = oc.readTree(jp);

            if (node.has("filename")) {
                return new Scene(node.get("filename").asText());
            }

            // Handle other cases if needed
            return new Scene("");
        }
    }

    public static class SearchRegionsDeserializer extends JsonDeserializer<SearchRegions> {
        @Override
        public SearchRegions deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            ObjectCodec oc = jp.getCodec();
            JsonNode node = oc.readTree(jp);

            SearchRegions searchRegions = new SearchRegions();

            // First try to get regions directly
            if (node.has("regions") && node.get("regions").isArray()) {
                populateRegionsFromArray(searchRegions, node.get("regions"));
            }
            // If no direct regions field, fall back to regionsMutable or allRegions
            else if (node.has("regionsMutable") && node.get("regionsMutable").isArray()) {
                populateRegionsFromArray(searchRegions, node.get("regionsMutable"));
            }
            else if (node.has("allRegions") && node.get("allRegions").isArray()) {
                JsonNode regionsArray = node.get("allRegions");
                // Only use allRegions if it contains actual regions (not just an empty region)
                if (!regionsArray.isEmpty() && !isEmptyRegion(regionsArray.get(0))) {
                    populateRegionsFromArray(searchRegions, regionsArray);
                }
            }

            // Set fixed region if present
            if (node.has("fixedRegion")) {
                JsonNode fixedRegionNode = node.get("fixedRegion");
                if (!isEmptyRegion(fixedRegionNode)) {
                    int x = fixedRegionNode.get("x").asInt();
                    int y = fixedRegionNode.get("y").asInt();
                    int w = fixedRegionNode.get("w").asInt();
                    int h = fixedRegionNode.get("h").asInt();
                    searchRegions.setFixedRegion(new Region(x, y, w, h));
                }
            }

            return searchRegions;
        }

        private void populateRegionsFromArray(SearchRegions searchRegions, JsonNode regionsArray) {
            for (JsonNode regionNode : regionsArray) {
                if (isEmptyRegion(regionNode)) continue;

                int x = regionNode.get("x").asInt();
                int y = regionNode.get("y").asInt();
                int w = regionNode.get("w").asInt();
                int h = regionNode.get("h").asInt();
                searchRegions.addSearchRegions(new Region(x, y, w, h));
            }
        }

        private boolean isEmptyRegion(JsonNode regionNode) {
            // Consider a region "empty" if it's the default (0,0,1920,1080) or
            // has width/height of 0
            return regionNode.get("w").asInt() == 0 ||
                    regionNode.get("h").asInt() == 0 ||
                    (regionNode.get("x").asInt() == 0 &&
                            regionNode.get("y").asInt() == 0 &&
                            regionNode.get("w").asInt() == 1920 &&
                            regionNode.get("h").asInt() == 1080);
        }
    }
}