package io.github.jspinak.brobot.runner.json.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.histogram.HistogramFindOptions;
import io.github.jspinak.brobot.action.basic.find.motion.MotionFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions;
import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.composite.PlaybackOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
// ClickUntilOptions removed - use ClickOptions with success criteria

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Custom deserializer for ActionConfig hierarchy that handles polymorphic deserialization.
 * Uses a type discriminator field (@type or type) to determine the concrete class.
 */
@Component
public class ActionConfigDeserializer extends JsonDeserializer<ActionConfig> {
    
    private static final Map<String, Class<? extends ActionConfig>> TYPE_MAPPINGS = new HashMap<>();
    
    static {
        // Core options
        TYPE_MAPPINGS.put("ActionChainOptions", ActionChainOptions.class);
        
        // Basic find options
        TYPE_MAPPINGS.put("BaseFindOptions", BaseFindOptions.class);
        TYPE_MAPPINGS.put("PatternFindOptions", PatternFindOptions.class);
        TYPE_MAPPINGS.put("HistogramFindOptions", HistogramFindOptions.class);
        TYPE_MAPPINGS.put("MotionFindOptions", MotionFindOptions.class);
        TYPE_MAPPINGS.put("VanishOptions", VanishOptions.class);
        
        // Basic action options
        TYPE_MAPPINGS.put("ClickOptions", ClickOptions.class);
        TYPE_MAPPINGS.put("TypeOptions", TypeOptions.class);
        TYPE_MAPPINGS.put("MouseMoveOptions", MouseMoveOptions.class);
        TYPE_MAPPINGS.put("MouseDownOptions", MouseDownOptions.class);
        TYPE_MAPPINGS.put("MouseUpOptions", MouseUpOptions.class);
        TYPE_MAPPINGS.put("ScrollOptions", ScrollOptions.class);
        
        // Visual action options
        TYPE_MAPPINGS.put("DefineRegionOptions", DefineRegionOptions.class);
        TYPE_MAPPINGS.put("HighlightOptions", HighlightOptions.class);
        
        // Composite action options
        TYPE_MAPPINGS.put("DragOptions", DragOptions.class);
        // ClickUntilOptions removed - use ClickOptions with success criteria
        TYPE_MAPPINGS.put("PlaybackOptions", PlaybackOptions.class);
        
        // Add shortened versions for convenience
        TYPE_MAPPINGS.put("chain", ActionChainOptions.class);
        TYPE_MAPPINGS.put("find", BaseFindOptions.class);
        TYPE_MAPPINGS.put("patternFind", PatternFindOptions.class);
        TYPE_MAPPINGS.put("histogramFind", HistogramFindOptions.class);
        TYPE_MAPPINGS.put("motionFind", MotionFindOptions.class);
        TYPE_MAPPINGS.put("vanish", VanishOptions.class);
        TYPE_MAPPINGS.put("click", ClickOptions.class);
        TYPE_MAPPINGS.put("type", TypeOptions.class);
        TYPE_MAPPINGS.put("move", MouseMoveOptions.class);
        TYPE_MAPPINGS.put("mouseDown", MouseDownOptions.class);
        TYPE_MAPPINGS.put("mouseUp", MouseUpOptions.class);
        TYPE_MAPPINGS.put("scroll", ScrollOptions.class);
        TYPE_MAPPINGS.put("define", DefineRegionOptions.class);
        TYPE_MAPPINGS.put("highlight", HighlightOptions.class);
        TYPE_MAPPINGS.put("drag", DragOptions.class);
        // clickUntil removed - use click with success criteria
        TYPE_MAPPINGS.put("playback", PlaybackOptions.class);
    }
    
    @Override
    public ActionConfig deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);
        
        // Check for type discriminator
        String type = null;
        if (node.has("@type")) {
            type = node.get("@type").asText();
        } else if (node.has("type")) {
            type = node.get("type").asText();
        }
        
        if (type == null) {
            throw new JsonProcessingException("Missing type discriminator (@type or type) for ActionConfig") {};
        }
        
        Class<? extends ActionConfig> targetClass = TYPE_MAPPINGS.get(type);
        if (targetClass == null) {
            throw new JsonProcessingException("Unknown ActionConfig type: " + type) {};
        }
        
        // Remove the type field before deserializing to avoid unknown property errors
        if (node.has("@type")) {
            ((com.fasterxml.jackson.databind.node.ObjectNode) node).remove("@type");
        } else if (node.has("type")) {
            ((com.fasterxml.jackson.databind.node.ObjectNode) node).remove("type");
        }
        
        // Use the context's deserializer but bypass this custom deserializer to avoid recursion
        // Convert the node directly to the target class
        try {
            // Use reflection to create the builder or instance
            return ctxt.readTreeAsValue(node, targetClass);
        } catch (Exception e) {
            // Fallback to a simple mapper if context fails
            ObjectMapper plainMapper = new ObjectMapper();
            plainMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            plainMapper.findAndRegisterModules();
            return plainMapper.treeToValue(node, targetClass);
        }
    }
    
    /**
     * Register a custom type mapping.
     * 
     * @param typeName The type discriminator value
     * @param clazz The concrete ActionConfig class
     */
    public static void registerType(String typeName, Class<? extends ActionConfig> clazz) {
        TYPE_MAPPINGS.put(typeName, clazz);
    }
}