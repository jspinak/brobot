package io.github.jspinak.brobot.tools.migration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.model.action.ActionConfigAdapter;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON converter for ActionHistory migration.
 * 
 * <p>This converter handles the serialization and deserialization of ActionHistory
 * objects, automatically migrating from ActionOptions to ActionConfig during
 * deserialization when needed.</p>
 * 
 * <p>Features:
 * <ul>
 *   <li>Automatic migration during deserialization</li>
 *   <li>Backward compatible JSON reading</li>
 *   <li>Modern ActionConfig-based serialization</li>
 *   <li>Batch file processing support</li>
 * </ul>
 * 
 * @since 1.2.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActionHistoryJsonConverter {
    
    private final ActionConfigAdapter actionConfigAdapter;
    private final ActionHistoryMigrationService migrationService;
    private final ObjectMapper objectMapper = createObjectMapper();
    
    /**
     * Creates and configures the ObjectMapper with migration support.
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        // Register custom deserializer for ActionHistory
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ActionHistory.class, new ActionHistoryDeserializer());
        module.addDeserializer(ActionRecord.class, new ActionRecordDeserializer());
        mapper.registerModule(module);
        
        return mapper;
    }
    
    /**
     * Serializes an ActionHistory to JSON string.
     * 
     * @param history the ActionHistory to serialize
     * @return JSON representation
     * @throws JsonProcessingException if serialization fails
     */
    public String serialize(ActionHistory history) throws JsonProcessingException {
        return objectMapper.writeValueAsString(history);
    }
    
    /**
     * Deserializes JSON to ActionHistory, automatically migrating if needed.
     * 
     * @param json the JSON string
     * @return deserialized and possibly migrated ActionHistory
     * @throws JsonProcessingException if deserialization fails
     */
    public ActionHistory deserialize(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, ActionHistory.class);
    }
    
    /**
     * Migrates a JSON file in place.
     * 
     * @param filePath path to the JSON file
     * @return true if migration successful, false otherwise
     */
    public boolean migrateJsonFile(Path filePath) {
        try {
            log.info("Migrating JSON file: {}", filePath);
            
            // Read existing JSON
            String json = Files.readString(filePath);
            
            // Deserialize (will auto-migrate if needed)
            ActionHistory history = deserialize(json);
            
            // Ensure full migration
            ActionHistory migrated = migrationService.migrate(history);
            
            // Serialize back to modern format
            String migratedJson = serialize(migrated);
            
            // Backup original
            Path backupPath = filePath.resolveSibling(filePath.getFileName() + ".backup");
            Files.copy(filePath, backupPath);
            
            // Write migrated JSON
            Files.writeString(filePath, migratedJson);
            
            log.info("Successfully migrated: {}", filePath);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to migrate JSON file {}: {}", filePath, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Custom deserializer for ActionHistory that handles migration.
     */
    private class ActionHistoryDeserializer extends StdDeserializer<ActionHistory> {
        
        public ActionHistoryDeserializer() {
            super(ActionHistory.class);
        }
        
        @Override
        public ActionHistory deserialize(JsonParser parser, DeserializationContext context) 
                throws IOException {
            
            ObjectNode node = parser.getCodec().readTree(parser);
            
            ActionHistory history = new ActionHistory();
            
            // Deserialize basic fields
            if (node.has("timesSearched")) {
                history.setTimesSearched(node.get("timesSearched").asInt());
            }
            if (node.has("timesFound")) {
                history.setTimesFound(node.get("timesFound").asInt());
            }
            
            // Deserialize snapshots with migration
            if (node.has("snapshots")) {
                List<ActionRecord> snapshots = new ArrayList<>();
                for (JsonNode snapshotNode : node.get("snapshots")) {
                    ActionRecord record = parseActionRecord(snapshotNode);
                    snapshots.add(record);
                }
                history.setSnapshots(snapshots);
            }
            
            return history;
        }
        
        private ActionRecord parseActionRecord(JsonNode node) throws IOException {
            // Use the custom ActionRecord deserializer
            return objectMapper.treeToValue(node, ActionRecord.class);
        }
    }
    
    /**
     * Custom deserializer for ActionRecord that handles migration.
     */
    private class ActionRecordDeserializer extends StdDeserializer<ActionRecord> {
        
        public ActionRecordDeserializer() {
            super(ActionRecord.class);
        }
        
        @Override
        public ActionRecord deserialize(JsonParser parser, DeserializationContext context) 
                throws IOException {
            
            ObjectNode node = parser.getCodec().readTree(parser);
            
            ActionRecord.Builder builder = new ActionRecord.Builder();
            
            // Check if this is a legacy record (has actionOptions but no actionConfig)
            boolean hasActionConfig = node.has("actionConfig") && !node.get("actionConfig").isNull();
            boolean hasActionOptions = node.has("actionOptions") && !node.get("actionOptions").isNull();
            
            if (!hasActionConfig && hasActionOptions) {
                // Legacy record - needs migration
                ActionOptions options = objectMapper.treeToValue(
                    node.get("actionOptions"), 
                    ActionOptions.class
                );
                
                // Convert to ActionConfig
                ActionConfig config = convertToActionConfig(options);
                builder.setActionConfig(config);
                
                log.debug("Migrated legacy ActionRecord from ActionOptions to ActionConfig");
            } else if (hasActionConfig) {
                // Modern record - deserialize ActionConfig
                JsonNode configNode = node.get("actionConfig");
                String configType = configNode.get("@type").asText();
                
                ActionConfig config = deserializeActionConfig(configType, configNode);
                builder.setActionConfig(config);
            }
            
            // Deserialize other fields
            if (node.has("actionSuccess")) {
                builder.setActionSuccess(node.get("actionSuccess").asBoolean());
            }
            if (node.has("duration")) {
                builder.setDuration(node.get("duration").asLong());
            }
            if (node.has("text")) {
                builder.setText(node.get("text").asText());
            }
            // Legacy field 'timesActedOn' - no longer used in current version
            // Skip deserialization of this field
            
            // Deserialize matches
            if (node.has("matchList")) {
                for (JsonNode matchNode : node.get("matchList")) {
                    builder.addMatch(objectMapper.treeToValue(matchNode, 
                        io.github.jspinak.brobot.model.match.Match.class));
                }
            }
            
            // Legacy field 'definedRegions' - no longer used in current version
            // Skip deserialization of this field
            
            return builder.build();
        }
        
        private ActionConfig deserializeActionConfig(String type, JsonNode node) 
                throws IOException {
            // Deserialize based on type
            switch (type) {
                case "PatternFindOptions":
                    return objectMapper.treeToValue(node, PatternFindOptions.class);
                case "ClickOptions":
                    return objectMapper.treeToValue(node, ClickOptions.class);
                case "TypeTextOptions":
                    return objectMapper.treeToValue(node, TypeOptions.class);
                case "VanishOptions":
                    return objectMapper.treeToValue(node, VanishOptions.class);
                case "MouseMoveOptions":
                    return objectMapper.treeToValue(node, MouseMoveOptions.class);
                case "DragOptions":
                    return objectMapper.treeToValue(node, DragOptions.class);
                case "DefineRegionOptions":
                    return objectMapper.treeToValue(node, DefineRegionOptions.class);
                case "HighlightOptions":
                    return objectMapper.treeToValue(node, HighlightOptions.class);
                default:
                    log.warn("Unknown ActionConfig type: {}, using default", type);
                    return new PatternFindOptions.Builder().build();
            }
        }
        
        private ActionConfig convertToActionConfig(ActionOptions options) {
            // Delegate to migration service
            return migrationService.convertActionOptionsToConfig(options);
        }
    }
    
    /**
     * Batch migrates multiple JSON files.
     * 
     * @param directory directory containing JSON files
     * @param pattern file pattern (e.g., "*.json")
     * @return migration statistics
     */
    public BatchMigrationResult migrateDirectory(Path directory, String pattern) {
        BatchMigrationResult result = new BatchMigrationResult();
        
        try {
            Files.walk(directory)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().matches(pattern))
                .forEach(path -> {
                    result.totalFiles++;
                    if (migrateJsonFile(path)) {
                        result.successfulMigrations++;
                    } else {
                        result.failedMigrations++;
                        result.failedFiles.add(path.toString());
                    }
                });
        } catch (IOException e) {
            log.error("Failed to walk directory {}: {}", directory, e.getMessage());
            result.error = e.getMessage();
        }
        
        return result;
    }
    
    /**
     * Result of batch migration operation.
     */
    public static class BatchMigrationResult {
        public int totalFiles;
        public int successfulMigrations;
        public int failedMigrations;
        public List<String> failedFiles = new ArrayList<>();
        public String error;
        
        public double getSuccessRate() {
            if (totalFiles == 0) return 0;
            return (double) successfulMigrations / totalFiles * 100;
        }
        
        @Override
        public String toString() {
            return String.format("Batch Migration: %d files, %d successful (%.1f%%), %d failed",
                    totalFiles, successfulMigrations, getSuccessRate(), failedMigrations);
        }
    }
}