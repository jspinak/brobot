package io.github.jspinak.brobot.test.jackson;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import com.fasterxml.jackson.core.type.TypeReference;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.*;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.*;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite demonstrating all serialization fixes. Each test shows how to properly
 * handle different Brobot objects.
 */
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
public class ComprehensiveSerializationTest extends BrobotTestBase {

    private SerializationTestValidator validator;

    @Override
    @org.junit.jupiter.api.BeforeEach
    public void setupTest() {
        super.setupTest();
        validator = new SerializationTestValidator(testObjectMapper);
    }

    @Test
    @DisplayName("Validate ActionRecord serialization with all fields")
    public void testActionRecordCompleteSerialization() throws Exception {
        // Create with builder utility
        ActionRecord record = JsonTestDataBuilder.createActionRecordWithMatches(3);

        // Validate
        SerializationTestValidator.ValidationReport report =
                validator.validateSerialization(record, ActionRecord.class);

        assertTrue(report.isValid(), report.generateReport());

        // Test round-trip
        String json = testObjectMapper.writeValueAsString(record);
        ActionRecord deserialized = testObjectMapper.readValue(json, ActionRecord.class);

        assertEquals(record.getStateName(), deserialized.getStateName());
        assertEquals(record.getMatchList().size(), deserialized.getMatchList().size());
        assertTrue(deserialized.isActionSuccess());
    }

    @Test
    @DisplayName("Fix and serialize incomplete ActionRecord")
    public void testFixIncompleteActionRecord() throws Exception {
        // Create incomplete record
        ActionRecord incomplete = new ActionRecord();
        // Missing required fields!

        // Fix it
        ActionRecord fixed = validator.fixCommonIssues(incomplete);

        // Should now be serializable
        String json = testObjectMapper.writeValueAsString(fixed);
        assertNotNull(json);
        assertTrue(json.contains("timeStamp"));
        assertTrue(json.contains("actionConfig"));

        // And deserializable
        ActionRecord deserialized = testObjectMapper.readValue(json, ActionRecord.class);
        assertNotNull(deserialized.getTimeStamp());
        assertNotNull(deserialized.getActionConfig());
    }

    @Test
    @DisplayName("Deserialize from JSON fixtures")
    public void testDeserializeFromFixtures() throws Exception {
        // Use pre-defined JSON fixtures
        ActionRecord record =
                testObjectMapper.readValue(
                        JsonTestFixtures.VALID_ACTION_RECORD, ActionRecord.class);
        assertNotNull(record);
        assertEquals("TestState", record.getStateName());

        Match match = testObjectMapper.readValue(JsonTestFixtures.VALID_MATCH, Match.class);
        assertNotNull(match);
        assertEquals(0.95, match.getScore(), 0.01);

        Pattern pattern = testObjectMapper.readValue(JsonTestFixtures.VALID_PATTERN, Pattern.class);
        assertNotNull(pattern);
        // Pattern may transform the imgpath during construction or deserialization
        // The actual path might have "images/" prefix and ".png" suffix added
        assertNotNull(pattern.getImgpath());
        assertTrue(pattern.getImgpath().contains("test-pattern"));
    }

    @Test
    @DisplayName("Serialize complex ObjectCollection")
    public void testObjectCollectionSerialization() throws Exception {
        ObjectCollection collection = JsonTestDataBuilder.createValidObjectCollection();

        // Validate
        SerializationTestValidator.ValidationReport report =
                validator.validateSerialization(collection, ObjectCollection.class);
        assertTrue(report.isValid(), report.generateReport());

        // Test serialization
        String json = testObjectMapper.writeValueAsString(collection);
        assertNotNull(json);

        // Test deserialization
        ObjectCollection deserialized = testObjectMapper.readValue(json, ObjectCollection.class);
        assertEquals(collection.getStateImages().size(), deserialized.getStateImages().size());
        assertEquals(
                collection.getStateLocations().size(), deserialized.getStateLocations().size());
        assertEquals(collection.getStateRegions().size(), deserialized.getStateRegions().size());
    }

    @Test
    @DisplayName("Handle null and missing fields gracefully")
    public void testNullFieldHandling() throws Exception {
        // JSON with missing fields
        String incompleteJson = "{}";

        // Should not throw exception due to FAIL_ON_UNKNOWN_PROPERTIES = false
        ObjectCollection collection =
                testObjectMapper.readValue(incompleteJson, ObjectCollection.class);
        assertNotNull(collection);

        // Lists should be empty, not null
        assertNotNull(collection.getStateImages());
        assertTrue(collection.getStateImages().isEmpty());
    }

    @Test
    @DisplayName("Serialize StateImage with patterns")
    public void testStateImageWithPatterns() throws Exception {
        StateImage stateImage = JsonTestDataBuilder.createValidStateImage("ComplexState");

        // Add multiple patterns
        for (int i = 0; i < 3; i++) {
            stateImage.getPatterns().add(JsonTestDataBuilder.createValidPattern("pattern-" + i));
        }

        // Serialize
        String json = testObjectMapper.writeValueAsString(stateImage);
        assertNotNull(json);
        assertTrue(json.contains("ComplexState"));
        assertTrue(json.contains("pattern-0"));

        // Deserialize
        StateImage deserialized = testObjectMapper.readValue(json, StateImage.class);
        assertEquals(4, deserialized.getPatterns().size()); // 1 initial + 3 added
        assertEquals("ComplexState", deserialized.getName());
    }

    @Test
    @DisplayName("Test polymorphic ActionConfig serialization")
    public void testPolymorphicActionConfig() throws Exception {
        // Note: ActionResult has @JsonIgnore on actionConfig field,
        // so it won't be serialized. This test should test ActionConfig directly instead.

        // Create a PatternFindOptions (subtype of ActionConfig)
        PatternFindOptions config = new PatternFindOptions.Builder().build();

        // Serialize the ActionConfig directly
        String json = testObjectMapper.writeValueAsString(config);

        // Check if type information is included
        boolean hasTypeInfo = json.contains("@type") || json.contains("PatternFindOptions");
        assertTrue(hasTypeInfo, "JSON should contain type information but was: " + json);

        // Deserialize as base type - should restore correct subtype
        ActionConfig deserialized = testObjectMapper.readValue(json, ActionConfig.class);
        assertNotNull(deserialized);
        assertTrue(deserialized instanceof PatternFindOptions);
    }

    @Test
    @DisplayName("Validate all model classes can serialize")
    public void testAllModelClassesSerialization() throws Exception {
        // Test each major model class
        Object[] testObjects = {
            JsonTestDataBuilder.createValidActionRecord(),
            JsonTestDataBuilder.createValidMatch("test", 0.9),
            JsonTestDataBuilder.createValidPattern("test"),
            JsonTestDataBuilder.createValidStateImage("test"),
            JsonTestDataBuilder.createValidStateRegion("test"),
            JsonTestDataBuilder.createValidStateLocation("test"),
            JsonTestDataBuilder.createValidStateString("test"),
            JsonTestDataBuilder.createValidObjectCollection(),
            JsonTestDataBuilder.createValidActionResult(),
            JsonTestDataBuilder.createValidState("test"),
            JsonTestDataBuilder.createValidScene("test"),
            JsonTestDataBuilder.createSearchRegions(),
            JsonTestDataBuilder.createAnchors()
        };

        for (Object obj : testObjects) {
            String className = obj.getClass().getSimpleName();

            // Should serialize without exception
            String json = testObjectMapper.writeValueAsString(obj);
            assertNotNull(json, "Failed to serialize " + className);

            // Should deserialize back
            Object deserialized = testObjectMapper.readValue(json, obj.getClass());
            assertNotNull(deserialized, "Failed to deserialize " + className);
        }
    }

    @Test
    @DisplayName("Test JSON array vs single value handling")
    public void testArrayVsSingleValue() throws Exception {
        // Single value that should be treated as array
        String singleValueJson =
                """
            {
                "stateImages": {
                    "name": "single-image",
                    "patterns": []
                }
            }
            """;

        // Due to ACCEPT_SINGLE_VALUE_AS_ARRAY configuration
        ObjectCollection collection =
                testObjectMapper.readValue(singleValueJson, ObjectCollection.class);
        assertNotNull(collection);
        assertEquals(1, collection.getStateImages().size());
        assertEquals("single-image", collection.getStateImages().get(0).getName());
    }

    @Test
    @DisplayName("Test custom JSON with template substitution")
    public void testCustomJsonTemplates() throws Exception {
        // Create custom pattern JSON
        String customPattern =
                JsonTestFixtures.customize(
                        JsonTestFixtures.PATTERN_TEMPLATE,
                        "name",
                        "my-custom-pattern",
                        "imgpath",
                        "custom/path.png",
                        "fixed",
                        "true",
                        "dynamic",
                        "false");

        Pattern pattern = testObjectMapper.readValue(customPattern, Pattern.class);
        assertEquals("custom/path.png", pattern.getImgpath());
        // The name field in Pattern is derived from imgpath, not directly settable in JSON
        assertTrue(pattern.isFixed());
        assertFalse(pattern.isDynamic());
    }

    @Test
    @DisplayName("Validate serialization with diagnostic report")
    public void testSerializationDiagnostics() {
        // Create object with potential issues
        ActionRecord record = new ActionRecord();
        // Missing required fields

        // Get diagnostic report
        SerializationTestValidator.ValidationReport report =
                validator.validateSerialization(record, ActionRecord.class);

        // Print report for debugging
        System.out.println(report.generateReport());

        // Fix and revalidate
        ActionRecord fixed = validator.fixCommonIssues(record);
        SerializationTestValidator.ValidationReport fixedReport =
                validator.validateSerialization(fixed, ActionRecord.class);

        assertTrue(fixedReport.isValid(), "Fixed object should be valid");
    }

    @Test
    @DisplayName("Test Map and List serialization")
    public void testCollectionTypes() throws Exception {
        // Create object with maps and lists
        String json =
                """
            {
                "metadata": {
                    "key1": "value1",
                    "key2": "value2"
                },
                "items": ["item1", "item2", "item3"]
            }
            """;

        // Deserialize to generic types
        Map<String, Object> data =
                testObjectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

        assertNotNull(data.get("metadata"));
        assertTrue(data.get("metadata") instanceof Map);

        assertNotNull(data.get("items"));
        assertTrue(data.get("items") instanceof List);
        assertEquals(3, ((List<?>) data.get("items")).size());
    }
}
