package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.json.config.BrobotJsonTestConfig;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(BrobotJsonTestConfig.class)
class MatchesJsonParserTest {

    @Autowired
    @Qualifier("testJsonParser")
    private JsonParser jsonParser;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
        ImagePath.setBundlePath("images");
    }

    @Test
    void testSerializeAndDeserializeMatches() throws ConfigurationException {
        // Create a test Matches object
        Matches matches = new Matches();
        matches.setActionDescription("Test Find Action");
        matches.setDuration(Duration.ofMillis(500));
        matches.setStartTime(LocalDateTime.now());
        matches.setEndTime(LocalDateTime.now().plus(500, ChronoUnit.MILLIS));
        matches.setSuccess(true);

        // Add a defined region
        Region definedRegion = new Region(100, 200, 300, 150);
        matches.addDefinedRegion(definedRegion);

        // Add some text
        matches.addString("Sample text from match");
        matches.setSelectedText("Sample text");

        // Serialize to JSON
        String json = jsonParser.toJson(matches);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Print the JSON for debugging
        System.out.println("Serialized Matches JSON:");
        System.out.println(json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("Test Find Action"));
        assertTrue(json.contains("Sample text"));
        assertTrue(json.contains("true"));  // for success

        // Deserialize back to Matches
        Matches deserializedMatches = jsonParser.convertJson(json, Matches.class);

        // Verify the object was correctly deserialized
        assertNotNull(deserializedMatches);
        assertEquals("Test Find Action", deserializedMatches.getActionDescription());
        assertEquals(500, deserializedMatches.getDuration().toMillis());        assertTrue(deserializedMatches.isSuccess());
        assertEquals("Sample text", deserializedMatches.getSelectedText());

        // Verify defined regions
        assertFalse(deserializedMatches.getDefinedRegions().isEmpty());
        assertEquals(100, deserializedMatches.getDefinedRegions().getFirst().x());
        assertEquals(200, deserializedMatches.getDefinedRegions().getFirst().y());
        assertEquals(300, deserializedMatches.getDefinedRegions().getFirst().w());
        assertEquals(150, deserializedMatches.getDefinedRegions().getFirst().h());
    }

    @Test
    void testSerializeAndDeserializeMatchesWithMatchObjects() throws ConfigurationException {
        // Create a test Matches object with match objects
        Matches matches = new Matches();

        // Create some match objects
        Match match1 = new Match.Builder()
                .setRegion(new Region(10, 20, 50, 30))
                .setSimScore(0.95)
                .setName("Button1")
                .build();
        match1.getStateObjectData().setOwnerStateName("HomeScreen");

        Match match2 = new Match.Builder()
                .setRegion(new Region(100, 150, 40, 40))
                .setSimScore(0.87)
                .setName("Button2")
                .build();
        match2.getStateObjectData().setOwnerStateName("HomeScreen");

        // Add matches to the Matches object
        matches.add(match1, match2);
        matches.setSuccess(true);

        // Set active states
        matches.getActiveStates().add("HomeScreen");

        // Serialize to JSON
        String json = jsonParser.toJson(matches);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Print the JSON for debugging
        System.out.println("Serialized Matches with MatchObjects JSON:");
        System.out.println(json);

        // Verify JSON contains expected values
        assertTrue(json.contains("Button1"));
        assertTrue(json.contains("Button2"));
        assertTrue(json.contains("0.95"));
        assertTrue(json.contains("0.87"));
        assertTrue(json.contains("HomeScreen"));

        // Deserialize back to Matches
        Matches deserializedMatches = jsonParser.convertJson(json, Matches.class);

        // Verify the object was correctly deserialized
        assertNotNull(deserializedMatches);
        assertEquals(2, deserializedMatches.getMatchList().size());
        assertTrue(deserializedMatches.isSuccess());

        // Verify match objects
        boolean foundButton1 = false;
        boolean foundButton2 = false;

        for (Match match : deserializedMatches.getMatchList()) {
            if (match.getName().equals("Button1")) {
                foundButton1 = true;
                assertEquals(0.95, match.getScore(), 0.001);
                assertEquals(10, match.getRegion().x());
                assertEquals(20, match.getRegion().y());
            }

            if (match.getName().equals("Button2")) {
                foundButton2 = true;
                assertEquals(0.87, match.getScore(), 0.001);
                assertEquals(100, match.getRegion().x());
                assertEquals(150, match.getRegion().y());
            }
        }

        assertTrue(foundButton1, "Button1 match wasn't found in deserialized object");
        assertTrue(foundButton2, "Button2 match wasn't found in deserialized object");

        // Verify active states
        assertEquals(1, deserializedMatches.getActiveStates().size());
        assertTrue(deserializedMatches.getActiveStates().contains("HomeScreen"));
    }
}