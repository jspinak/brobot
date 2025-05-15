package io.github.jspinak.brobot.json.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SearchRegionsDeserializer extends JsonDeserializer<SearchRegions> {
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
