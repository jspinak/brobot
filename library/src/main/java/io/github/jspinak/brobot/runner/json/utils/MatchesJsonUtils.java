package io.github.jspinak.brobot.runner.json.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Specialized JSON serialization utility for ActionResult (Matches) in the Brobot framework.
 * 
 * <p>ActionResult is the universal return type for all Brobot actions, containing match 
 * results, timing data, extracted text, and various metadata. This utility addresses the 
 * unique serialization challenges posed by ActionResult's complex structure, particularly 
 * the presence of OpenCV Mat objects and circular references that standard JSON 
 * serialization cannot handle.</p>
 * 
 * <p>Key serialization challenges addressed:
 * <ul>
 *   <li><b>OpenCV Mat objects</b>: These native image buffers cannot be serialized 
 *       directly and are excluded from the JSON representation</li>
 *   <li><b>Circular references</b>: Match objects may reference their parent 
 *       ActionResult, requiring careful handling to avoid infinite recursion</li>
 *   <li><b>Complex nested structures</b>: Match objects contain StateObjectData 
 *       with multiple levels of nesting that need proper flattening</li>
 *   <li><b>Performance data</b>: Preserves timing information and success metrics 
 *       essential for action analysis and debugging</li>
 * </ul>
 * </p>
 * 
 * <p>This utility is critical for:
 * <ul>
 *   <li>Persisting action results for offline analysis and replay</li>
 *   <li>Transmitting results between distributed automation nodes</li>
 *   <li>Creating reproducible test scenarios with known match data</li>
 *   <li>Building illustrated action histories for debugging</li>
 *   <li>Analyzing automation performance through saved results</li>
 * </ul>
 * </p>
 * 
 * <p>The Map-based serialization approach allows fine control over which fields 
 * are included, ensuring that all essential match data is preserved while 
 * excluding problematic references and native objects.</p>
 * 
 * @see ActionResult
 * @see Match
 * @see io.github.jspinak.brobot.action.Action
 * @since 1.0
 */
@Component
public class MatchesJsonUtils {

    private static final Logger log = LoggerFactory.getLogger(MatchesJsonUtils.class);
    private final JsonUtils jsonUtils;
    private final ConfigurationParser jsonParser;

    public MatchesJsonUtils(JsonUtils jsonUtils, ConfigurationParser jsonParser) {
        this.jsonUtils = jsonUtils;
        this.jsonParser = jsonParser;
    }

    /**
     * Converts ActionResult to a Map representation for safe JSON serialization.
     * 
     * <p>This method carefully extracts all serializable data from an ActionResult while 
     * avoiding problematic fields that could cause serialization failures. The resulting 
     * Map preserves the complete action execution context including matches, timing, 
     * text results, and state information.</p>
     * 
     * <p>Serialization strategy:
     * <ul>
     *   <li><b>Basic fields</b>: Direct inclusion of primitives and strings 
     *       (success, duration, timestamps, text)</li>
     *   <li><b>Match list</b>: Each Match is converted to a simplified Map containing 
     *       only essential fields (region, score, name, stateObjectData)</li>
     *   <li><b>State data</b>: Active states and defined regions are preserved 
     *       for state transition analysis</li>
     *   <li><b>Excluded fields</b>: Mat objects, scene analysis data, and other 
     *       non-serializable components are omitted</li>
     * </ul>
     * </p>
     * 
     * <p>The flattened structure prevents circular references while maintaining 
     * all information needed to understand what happened during action execution, 
     * making it ideal for logging, debugging, and performance analysis.</p>
     * 
     * @param matches The ActionResult to convert
     * @return Map containing all serializable action result data
     */
    public Map<String, Object> matchesToMap(ActionResult matches) {
        Map<String, Object> map = new HashMap<>();

        // Add basic fields
        map.put("actionDescription", matches.getActionDescription());
        map.put("success", matches.isSuccess());
        map.put("duration", matches.getDuration());
        map.put("startTime", matches.getStartTime());
        map.put("endTime", matches.getEndTime());
        map.put("selectedText", matches.getSelectedText());
        map.put("activeStates", matches.getActiveStates());
        map.put("definedRegions", matches.getDefinedRegions());
        map.put("text", matches.getText());

        // Handle match list - copy to avoid circular references
        List<Map<String, Object>> matchesList = new ArrayList<>();
        for (Match match : matches.getMatchList()) {
            Map<String, Object> matchMap = new HashMap<>();
            matchMap.put("region", match.getRegion());
            matchMap.put("score", match.getScore());
            matchMap.put("name", match.getName());

            if (match.getStateObjectData() != null) {
                Map<String, String> stateObjectData = new HashMap<>();
                stateObjectData.put("ownerStateName", match.getOwnerStateName());
                stateObjectData.put("stateObjectName", match.getStateObjectData().getStateObjectName());
                matchMap.put("stateObjectData", stateObjectData);
            }

            matchesList.add(matchMap);
        }
        map.put("matchList", matchesList);

        return map;
    }

    /**
     * Serializes ActionResult to a JSON string representation.
     * 
     * <p>This method provides reliable JSON serialization for ActionResult objects, 
     * which are returned by every action in the Brobot framework. The serialization 
     * process handles the complex structure of match results while avoiding common 
     * pitfalls like Mat object references and circular dependencies.</p>
     * 
     * <p>Usage example:
     * <pre>{@code
     * ActionResult result = action.perform();
     * 
     * // Serialize for persistence or transmission
     * String json = matchesJsonUtils.matchesToJson(result);
     * 
     * // Later, analyze the results
     * ActionResult restored = jsonParser.convertJson(json, ActionResult.class);
     * System.out.println("Found " + restored.getMatchList().size() + " matches");
     * }</pre>
     * </p>
     * 
     * <p>The serialized JSON includes:
     * <ul>
     *   <li>Complete match list with locations and scores</li>
     *   <li>Success/failure status and timing metrics</li>
     *   <li>Any text extracted during the action</li>
     *   <li>Active states discovered during execution</li>
     * </ul>
     * </p>
     * 
     * @param matches The ActionResult to serialize
     * @return JSON string representation of the action results
     * @throws ConfigurationException if serialization fails
     */
    public String matchesToJson(ActionResult matches) throws ConfigurationException {
        return jsonUtils.toJsonSafe(matches);
    }

    /**
     * Creates a deep copy of ActionResult through serialization.
     * 
     * <p>This method provides a reliable way to duplicate ActionResult objects without 
     * any shared references. This is particularly important when results need to be 
     * preserved, modified, or used as templates for new operations without affecting 
     * the original data.</p>
     * 
     * <p>Deep copy benefits:
     * <ul>
     *   <li>Complete independence - modifications to the copy don't affect the original</li>
     *   <li>Safe for concurrent use - no thread safety issues from shared state</li>
     *   <li>Automatic handling of complex nested structures</li>
     *   <li>Consistent copying behavior regardless of result complexity</li>
     * </ul>
     * </p>
     * 
     * <p>Common applications:
     * <ul>
     *   <li>Preserving intermediate results in multi-step automations</li>
     *   <li>Creating result templates for comparison operations</li>
     *   <li>Building result histories for undo/redo functionality</li>
     *   <li>Distributing results across parallel processing pipelines</li>
     * </ul>
     * </p>
     * 
     * <p>Note: The deep copy will not include any Mat objects or other non-serializable 
     * data. These fields will be null in the copied instance and must be reconstructed 
     * if needed for further image processing.</p>
     * 
     * @param matches The ActionResult to copy
     * @return A new ActionResult instance with no shared references
     * @throws ConfigurationException if the copy operation fails
     */
    public ActionResult deepCopy(ActionResult matches) throws ConfigurationException {
        String json = matchesToJson(matches);
        try {
            return jsonParser.convertJson(json, ActionResult.class);
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Failed to create deep copy of Matches", e);
        }
    }

}