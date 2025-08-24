package io.github.jspinak.brobot.actions.actionOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ActionOptionsJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
        ImagePath.setBundlePath("images");
    }

    @Test
    void testSerializeAndDeserializeActionOptions() throws ConfigurationException {
        // Create a test PatternFindOptions object
        PatternFindOptions actionOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.8)
                .setMaxWait(10.0)
                .build();

        // Serialize to JSON
        String json = jsonParser.toJson(actionOptions);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Print the JSON for debugging
        System.out.println("Serialized ActionOptions JSON:");
        System.out.println(json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("strategy"));
        assertTrue(json.contains("FIRST"));

        // Deserialize back to PatternFindOptions
        PatternFindOptions deserializedOptions = jsonParser.convertJson(json, PatternFindOptions.class);

        // Verify the object was correctly deserialized
        assertNotNull(deserializedOptions);
        assertEquals(PatternFindOptions.Strategy.FIRST, deserializedOptions.getStrategy());
        assertEquals(0.8, deserializedOptions.getSimilarity());
        assertEquals(10.0, deserializedOptions.getMaxWait());

        // PatternFindOptions has simpler structure than ActionOptions
        // Target offset/position functionality is handled differently in the new API
    }

    @Test
    void testSerializeComplexActionOptions() throws ConfigurationException {
        // Create a ClickOptions object
        ClickOptions actionOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setNumberOfClicks(1)
                .build();

        // Serialize to JSON
        String json = jsonParser.toJson(actionOptions);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Print the JSON for debugging
        System.out.println("Serialized Complex ActionOptions JSON:");
        System.out.println(json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("clickType"));
        assertTrue(json.contains("LEFT"));

        // Deserialize back to ClickOptions
        ClickOptions deserializedOptions = jsonParser.convertJson(json, ClickOptions.class);

        // Verify the object was correctly deserialized
        assertNotNull(deserializedOptions);
        assertEquals(ClickOptions.Type.LEFT, deserializedOptions.getClickType());
        assertEquals(1, deserializedOptions.getNumberOfClicks());

        // ClickOptions has different structure than legacy ActionOptions
        // Search regions and advanced options are handled by ActionConfig patterns
    }
}