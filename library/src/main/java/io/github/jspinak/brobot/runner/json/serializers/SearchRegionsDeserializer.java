package io.github.jspinak.brobot.runner.json.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom deserializer for {@link SearchRegions} objects that handles flexible JSON formats
 * and filters out invalid or default region data during deserialization.
 * 
 * <p>This deserializer addresses several challenges when deserializing SearchRegions:</p>
 * <ul>
 *   <li><b>Format Flexibility:</b> SearchRegions may be serialized with different field
 *       names depending on the serialization context (regions, regionsMutable, allRegions).
 *       This deserializer checks multiple possible field names for compatibility.</li>
 *   <li><b>Invalid Data Filtering:</b> Automatically filters out "empty" regions that
 *       represent defaults or invalid states (e.g., zero dimensions or full-screen defaults
 *       like 1920x1080).</li>
 *   <li><b>Backward Compatibility:</b> Supports multiple JSON formats to maintain
 *       compatibility with different versions of the serialized data.</li>
 *   <li><b>Data Validation:</b> Ensures only valid regions with meaningful dimensions
 *       are added to the SearchRegions object.</li>
 * </ul>
 * 
 * <p><b>Deserialization Strategy:</b></p>
 * <ol>
 *   <li>Attempts to read regions from multiple possible field names in order of preference:
 *       "regions" → "regionsMutable" → "allRegions"</li>
 *   <li>Filters out invalid regions (zero dimensions or default full-screen values)</li>
 *   <li>Handles the optional fixedRegion field separately</li>
 *   <li>Constructs Region objects from valid JSON data</li>
 * </ol>
 * 
 * <p><b>Expected JSON Formats:</b></p>
 * <pre>{@code
 * // Format 1: Direct regions array
 * {
 *   "regions": [
 *     {"x": 10, "y": 20, "w": 100, "h": 50},
 *     {"x": 200, "y": 300, "w": 150, "h": 75}
 *   ],
 *   "fixedRegion": {"x": 0, "y": 0, "w": 800, "h": 600}
 * }
 * 
 * // Format 2: Mutable regions (legacy)
 * {
 *   "regionsMutable": [...],
 *   "fixedRegion": {...}
 * }
 * 
 * // Format 3: All regions (fallback)
 * {
 *   "allRegions": [...],
 *   "fixedRegion": {...}
 * }
 * }</pre>
 * 
 * <p><b>Empty Region Detection:</b></p>
 * <p>Regions are considered "empty" and filtered out if they have:</p>
 * <ul>
 *   <li>Width or height of 0</li>
 *   <li>Default full-screen dimensions (0,0,1920,1080) which often indicate uninitialized regions</li>
 * </ul>
 * 
 * @see SearchRegions
 * @see Region
 * @see JsonDeserializer
 */
@Component
public class SearchRegionsDeserializer extends JsonDeserializer<SearchRegions> {
    /**
     * Deserializes JSON content into a SearchRegions object, handling multiple
     * possible field names and filtering out invalid regions.
     * 
     * <p><b>Processing Order:</b></p>
     * <ol>
     *   <li>Creates an empty SearchRegions object</li>
     *   <li>Checks for regions in order: "regions", "regionsMutable", "allRegions"</li>
     *   <li>Populates valid regions while filtering out empty ones</li>
     *   <li>Sets the fixed region if present and valid</li>
     * </ol>
     * 
     * @param jp the JsonParser positioned at the SearchRegions JSON object
     * @param ctxt the deserialization context
     * @return a new SearchRegions object populated with valid regions
     * @throws IOException if there's an error reading the JSON content
     */
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

    /**
     * Populates a SearchRegions object with regions from a JSON array, filtering
     * out any empty or invalid regions.
     * 
     * <p>Iterates through each region in the array, validates it using
     * {@link #isEmptyRegion(JsonNode)}, and adds only valid regions to the
     * SearchRegions object.</p>
     * 
     * @param searchRegions the SearchRegions object to populate
     * @param regionsArray the JSON array containing region data
     */
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

    /**
     * Determines whether a region should be considered "empty" and thus filtered out
     * during deserialization.
     * 
     * <p>A region is considered empty if it meets any of these criteria:</p>
     * <ul>
     *   <li>Has a width of 0</li>
     *   <li>Has a height of 0</li>
     *   <li>Matches the default full-screen dimensions (0,0,1920,1080), which often
     *       indicates an uninitialized or default region that shouldn't be included</li>
     * </ul>
     * 
     * <p>This filtering helps ensure that only meaningful regions with actual
     * search areas are included in the deserialized SearchRegions object.</p>
     * 
     * @param regionNode the JSON node representing a region
     * @return true if the region should be filtered out, false if it's valid
     */
    private boolean isEmptyRegion(JsonNode regionNode) {
        return regionNode.get("w").asInt() == 0 ||
                regionNode.get("h").asInt() == 0 ||
                (regionNode.get("x").asInt() == 0 &&
                        regionNode.get("y").asInt() == 0 &&
                        regionNode.get("w").asInt() == 1920 &&
                        regionNode.get("h").asInt() == 1080);
    }
}
