package io.github.jspinak.brobot.model.region;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import io.github.jspinak.brobot.BrobotTestApplication;

@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class,
    properties = {
        "brobot.gui-access.continue-on-error=true",
        "brobot.gui-access.check-on-startup=false",
        "java.awt.headless=true",
        "spring.main.allow-bean-definition-overriding=true",
        "brobot.test.type=unit",
        "brobot.capture.physical-resolution=false",
        "brobot.mock.enabled=true"
    })
@Import({MockGuiAccessConfig.class, MockGuiAccessMonitor.class, MockScreenConfig.class})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
public class SearchRegionsJsonParserTest {
    
    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic SearchRegions from JSON
     */
    @Test
    public void testParseBasicSearchRegions() throws ConfigurationException {
        String json = """
                {
                  "regions": [
                    {
                      "x": 100,
                      "y": 200,
                      "w": 300,
                      "h": 400
                    },
                    {
                      "x": 500,
                      "y": 600,
                      "w": 700,
                      "h": 800
                    }
                  ],
                  "fixedRegion": {
                    "x": 10,
                    "y": 20,
                    "w": 30,
                    "h": 40
                  }
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        SearchRegions searchRegions = jsonParser.convertJson(jsonNode, SearchRegions.class);

        assertNotNull(searchRegions);

        // Verify regions list
        assertNotNull(searchRegions.getRegions());
        assertEquals(2, searchRegions.getRegions().size());
        assertEquals(100, searchRegions.getRegions().getFirst().x());
        assertEquals(200, searchRegions.getRegions().getFirst().y());
        assertEquals(300, searchRegions.getRegions().getFirst().w());
        assertEquals(400, searchRegions.getRegions().getFirst().h());
        assertEquals(500, searchRegions.getRegions().get(1).x());
        assertEquals(600, searchRegions.getRegions().get(1).y());
        assertEquals(700, searchRegions.getRegions().get(1).w());
        assertEquals(800, searchRegions.getRegions().get(1).h());

        // Verify fixed region
        assertNotNull(searchRegions.getFixedRegion());
        assertEquals(10, searchRegions.getFixedRegion().x());
        assertEquals(20, searchRegions.getFixedRegion().y());
        assertEquals(30, searchRegions.getFixedRegion().w());
        assertEquals(40, searchRegions.getFixedRegion().h());
    }

    /**
     * Test serializing and deserializing SearchRegions
     */
    @Test
    public void testSerializeDeserializeSearchRegions() throws ConfigurationException {
        // Create search regions
        SearchRegions searchRegions = new SearchRegions();

        // Add regions
        searchRegions.addSearchRegions(new Region(100, 200, 300, 400));
        searchRegions.addSearchRegions(new Region(500, 600, 700, 800));

        // Set fixed region
        searchRegions.setFixedRegion(new Region(10, 20, 30, 40));

        // Serialize
        String json = jsonUtils.toJsonSafe(searchRegions);
        System.out.println("DEBUG: Serialized SearchRegions: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        SearchRegions deserializedRegions = jsonParser.convertJson(jsonNode, SearchRegions.class);

        // Verify
        assertNotNull(deserializedRegions);

        // Verify regions
        assertNotNull(deserializedRegions.getRegions());
        assertEquals(2, deserializedRegions.getRegions().size());

        // Verify fixed region
        assertNotNull(deserializedRegions.getFixedRegion());
        assertEquals(10, deserializedRegions.getFixedRegion().x());
        assertEquals(20, deserializedRegions.getFixedRegion().y());
        assertEquals(30, deserializedRegions.getFixedRegion().w());
        assertEquals(40, deserializedRegions.getFixedRegion().h());
    }

    /**
     * Test isFixedRegionSet method
     */
    @Test
    public void testIsFixedRegionSet() {
        // Create search regions
        SearchRegions searchRegions = new SearchRegions();

        // By default, fixed region should not be set
        assertFalse(searchRegions.isFixedRegionSet());

        // Set fixed region
        searchRegions.setFixedRegion(new Region(10, 20, 30, 40));

        // Now it should be set
        assertTrue(searchRegions.isFixedRegionSet());
    }

    /**
     * Test getOneRegion method
     */
    @Test
    public void testGetOneRegion() {
        // Create search regions
        SearchRegions searchRegions = new SearchRegions();

        // Add regions
        searchRegions.addSearchRegions(new Region(100, 200, 300, 400));

        // Test without fixed region
        Region oneRegion = searchRegions.getOneRegion();
        assertNotNull(oneRegion);
        assertEquals(100, oneRegion.x());

        // Set fixed region
        searchRegions.setFixedRegion(new Region(10, 20, 30, 40));

        // Should now return fixed region
        oneRegion = searchRegions.getOneRegion();
        assertNotNull(oneRegion);
        assertEquals(10, oneRegion.x());
    }

    /**
     * Test getFixedIfDefinedOrRandomRegion method
     */
    @Test
    public void testGetFixedIfDefinedOrRandomRegion() {
        // Create search regions
        SearchRegions searchRegions = new SearchRegions();

        // Add regions
        searchRegions.addSearchRegions(new Region(100, 200, 300, 400));

        // Test with fixed=false
        Region region = searchRegions.getFixedIfDefinedOrRandomRegion(false);
        assertNotNull(region);
        assertEquals(100, region.x());

        // Test with fixed=true but no fixed region defined
        region = searchRegions.getFixedIfDefinedOrRandomRegion(true);
        assertNotNull(region);
        assertEquals(0, searchRegions.getFixedRegion().x()); // Default region

        // Set fixed region and test again
        searchRegions.setFixedRegion(new Region(10, 20, 30, 40));
        region = searchRegions.getFixedIfDefinedOrRandomRegion(true);
        assertNotNull(region);
        assertEquals(10, region.x());
    }

    /**
     * Test getRegions method
     */
    @Test
    public void testGetRegions() {
        // Create search regions
        SearchRegions searchRegions = new SearchRegions();

        // Add regions
        searchRegions.addSearchRegions(new Region(100, 200, 300, 400));

        // Test with fixed=false
        List<Region> regions = searchRegions.getRegions(false);
        assertNotNull(regions);
        assertEquals(1, regions.size());

        // Set fixed region and test again
        searchRegions.setFixedRegion(new Region(10, 20, 30, 40));
        regions = searchRegions.getRegions(true);
        assertNotNull(regions);
        assertEquals(1, regions.size());
        assertEquals(10, regions.getFirst().x());
    }

    /**
     * Test isDefined method
     */
    @Test
    public void testIsDefined() {
        // Create search regions
        SearchRegions searchRegions = new SearchRegions();

        // By default, should not be defined
        assertFalse(searchRegions.isDefined(true));

        // Add a region and test with fixed=false
        searchRegions.addSearchRegions(new Region(100, 200, 300, 400));
        assertTrue(searchRegions.isDefined(false));

        // Should still not be defined for fixed=true
        assertFalse(searchRegions.isDefined(true));

        // Set fixed region and test with fixed=true
        searchRegions.setFixedRegion(new Region(10, 20, 30, 40));
        assertTrue(searchRegions.isDefined(true));
    }
}