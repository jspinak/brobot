package io.github.jspinak.brobot.actions.actionOptions;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.action.ActionOptions;

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
        // Create a test ActionOptions object
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.FIRST)
                .setMinSimilarity(0.8)
                .setMoveMouseDelay(0.5f)
                .setPauseBeforeBegin(0.3)
                .setPauseAfterEnd(0.7)
                .setMaxWait(10.0)
                .setTargetOffset(15, 20)
                .setTargetPosition(new Position(50, 50))
                .build();

        // Serialize to JSON
        String json = jsonParser.toJson(actionOptions);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Print the JSON for debugging
        System.out.println("Serialized ActionOptions JSON:");
        System.out.println(json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("FIND"));
        assertTrue(json.contains("FIRST"));

        // Deserialize back to ActionOptions
        ActionOptions deserializedOptions = jsonParser.convertJson(json, ActionOptions.class);

        // Verify the object was correctly deserialized
        assertNotNull(deserializedOptions);
        assertEquals(ActionOptions.Action.FIND, deserializedOptions.getAction());
        assertEquals(ActionOptions.Find.FIRST, deserializedOptions.getFind());
        assertEquals(0.8, deserializedOptions.getSimilarity());
        assertEquals(0.5f, deserializedOptions.getMoveMouseDelay());
        assertEquals(0.3, deserializedOptions.getPauseBeforeBegin());
        assertEquals(0.7, deserializedOptions.getPauseAfterEnd());
        assertEquals(10.0, deserializedOptions.getMaxWait());

        // Compare target offset/position
        assertNotNull(deserializedOptions.getTargetOffset());
        assertEquals(15, deserializedOptions.getTargetOffset().getX());
        assertEquals(20, deserializedOptions.getTargetOffset().getY());

        assertNotNull(deserializedOptions.getTargetPosition());
        assertEquals(.50, deserializedOptions.getTargetPosition().getPercentW());
        assertEquals(.50, deserializedOptions.getTargetPosition().getPercentW());
    }

    @Test
    void testSerializeComplexActionOptions() throws ConfigurationException {
        // Create a more complex ActionOptions with search regions
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setFind(ActionOptions.Find.ALL)
                .addSearchRegion(new Region(10, 20, 100, 100))
                .setHighlightAllAtOnce(true)
                .setHighlightSeconds(2.5)
                .setHighlightColor("blue")
                .setMoveMouseAfterAction(true)
                .setMoveMouseAfterActionTo(new Location(500, 300))
                .setMaxMatchesToActOn(5)
                .build();

        // Serialize to JSON
        String json = jsonParser.toJson(actionOptions);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Print the JSON for debugging
        System.out.println("Serialized Complex ActionOptions JSON:");
        System.out.println(json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("CLICK"));
        assertTrue(json.contains("ALL"));
        assertTrue(json.contains("blue"));

        // Deserialize back to ActionOptions
        ActionOptions deserializedOptions = jsonParser.convertJson(json, ActionOptions.class);

        // Verify the object was correctly deserialized
        assertNotNull(deserializedOptions);
        assertEquals(ActionOptions.Action.CLICK, deserializedOptions.getAction());
        assertEquals(ActionOptions.Find.ALL, deserializedOptions.getFind());
        assertTrue(deserializedOptions.isHighlightAllAtOnce());
        assertEquals(2.5, deserializedOptions.getHighlightSeconds());
        assertEquals("blue", deserializedOptions.getHighlightColor());
        assertTrue(deserializedOptions.isMoveMouseAfterAction());
        assertNotNull(deserializedOptions.getMoveMouseAfterActionTo());
        assertEquals(500, deserializedOptions.getMoveMouseAfterActionTo().getCalculatedX());
        assertEquals(300, deserializedOptions.getMoveMouseAfterActionTo().getCalculatedY());
        assertEquals(5, deserializedOptions.getMaxMatchesToActOn());

        // Verify search regions
        assertFalse(deserializedOptions.getSearchRegions().getRegions().isEmpty());
        assertEquals(10, deserializedOptions.getSearchRegions().getRegions().getFirst().x());
        assertEquals(20, deserializedOptions.getSearchRegions().getRegions().getFirst().y());
        assertEquals(100, deserializedOptions.getSearchRegions().getRegions().getFirst().w());
        assertEquals(100, deserializedOptions.getSearchRegions().getRegions().getFirst().h());
    }
}