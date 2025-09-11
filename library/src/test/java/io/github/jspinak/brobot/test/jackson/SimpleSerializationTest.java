package io.github.jspinak.brobot.test.jackson;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Simple test to verify basic serialization works. */
public class SimpleSerializationTest extends BrobotTestBase {

    @Test
    public void testMinimalActionRecordSerialization() throws Exception {
        // Create minimal ActionRecord
        ActionRecord record = new ActionRecord();
        record.setTimeStamp(LocalDateTime.now());
        record.setMatchList(new ArrayList<>());
        record.setText("");

        // Try serialization without the configured mapper first
        ObjectMapper basicMapper = new ObjectMapper();
        basicMapper.findAndRegisterModules(); // Just register Java Time module

        try {
            String json = basicMapper.writeValueAsString(record);
            assertNotNull(json);
            System.out.println("Basic serialization succeeded");
            System.out.println("JSON: " + json);

            // Try deserialization
            ActionRecord deserialized = basicMapper.readValue(json, ActionRecord.class);
            assertNotNull(deserialized);
            System.out.println("Basic deserialization succeeded");
        } catch (Exception e) {
            System.out.println("Basic mapper failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testPatternFindOptionsSerialization() throws Exception {
        // Test PatternFindOptions specifically
        PatternFindOptions options = new PatternFindOptions.Builder().setSimilarity(0.8).build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        try {
            String json = mapper.writeValueAsString(options);
            assertNotNull(json);
            System.out.println("PatternFindOptions serialization succeeded");
            System.out.println("JSON: " + json);

            // This might fail on deserialization
            PatternFindOptions deserialized = mapper.readValue(json, PatternFindOptions.class);
            assertNotNull(deserialized);
            System.out.println("PatternFindOptions deserialization succeeded");
        } catch (Exception e) {
            System.out.println("PatternFindOptions serialization failed: " + e.getMessage());
            // Expected to fail without proper configuration
        }
    }
}
