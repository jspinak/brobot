package io.github.jspinak.brobot.runner.dsl;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import io.github.jspinak.brobot.runner.dsl.model.ActionStep;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ActionStep JSON parsing with the new ActionConfig API.
 * Uses Spring Boot's configured ObjectMapper with all necessary Jackson modules.
 * 
 * Key points:
 * - ActionStep uses 'actionConfig' field instead of 'actionOptions'
 * - ActionConfig uses @type for polymorphic deserialization
 * - Specific config classes like ClickOptions are used
 */
@DisplayName("ActionStep JSON Parser Tests")
@SpringBootTest(classes = BrobotTestApplication.class,
    properties = {
        "spring.main.lazy-initialization=true",
        "brobot.mock.enabled=true",
        "brobot.illustration.disabled=true",
        "brobot.scene.analysis.disabled=true",
        "brobot.gui-access.continue-on-error=true",
        "brobot.gui-access.check-on-startup=false",
        "java.awt.headless=true",
        "spring.main.allow-bean-definition-overriding=true",
        "brobot.test.type=unit",
        "brobot.capture.physical-resolution=false"
    })
@Import({MockGuiAccessConfig.class, MockGuiAccessMonitor.class, MockScreenConfig.class,
         io.github.jspinak.brobot.test.config.TestApplicationConfiguration.class})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
class ActionStepJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Test
    @DisplayName("Should parse ActionStep with ClickOptions from JSON")
    void testActionStepWithClickOptionsFromJson() throws ConfigurationException {
        String json = """
        {
            "actionConfig": {
                "@type": "ClickOptions",
                "numberOfClicks": 2
            },
            "objectCollection": {
                "stateImages": [
                    {
                        "name": "TestImage"
                    }
                ]
            }
        }
        """;

        ActionStep actionStep = jsonParser.convertJson(json, ActionStep.class);

        assertNotNull(actionStep);
        assertNotNull(actionStep.getActionConfig());
        assertTrue(actionStep.getActionConfig() instanceof ClickOptions);
        
        ClickOptions clickOptions = (ClickOptions) actionStep.getActionConfig();
        assertEquals(2, clickOptions.getNumberOfClicks());
        
        assertNotNull(actionStep.getObjectCollection());
        assertEquals(1, actionStep.getObjectCollection().getStateImages().size());
        assertEquals("TestImage", actionStep.getObjectCollection().getStateImages().get(0).getName());
    }

    @Test
    @DisplayName("Should serialize ActionStep to JSON")
    void testActionStepToJson() throws ConfigurationException {
        // Create ActionStep programmatically
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .build();
            
        ActionStep actionStep = new ActionStep();
        actionStep.setActionConfig(clickOptions);
        
        String json = jsonParser.toJson(actionStep);
        
        assertNotNull(json);
        assertTrue(json.contains("\"@type\" : \"ClickOptions\""));
        assertTrue(json.contains("\"numberOfClicks\" : 2"));
    }

    @Test
    @DisplayName("Should parse ActionStep with PatternFindOptions from JSON")
    void testActionStepWithFindOptionsFromJson() throws ConfigurationException {
        String json = """
        {
            "actionConfig": {
                "@type": "PatternFindOptions",
                "similarity": 0.95,
                "searchDuration": 2.0,
                "strategy": "FIRST"
            },
            "objectCollection": {
                "stateRegions": [
                    {
                        "name": "SearchArea",
                        "searchRegion": {
                            "x": 0,
                            "y": 0,
                            "w": 100,
                            "h": 100
                        }
                    }
                ]
            }
        }
        """;

        ActionStep actionStep = jsonParser.convertJson(json, ActionStep.class);

        assertNotNull(actionStep);
        assertNotNull(actionStep.getActionConfig());
        assertTrue(actionStep.getActionConfig() instanceof PatternFindOptions);
        
        PatternFindOptions findOptions = (PatternFindOptions) actionStep.getActionConfig();
        assertEquals(PatternFindOptions.Strategy.FIRST, findOptions.getStrategy());
        assertEquals(0.95, findOptions.getSimilarity(), 0.01);
        assertEquals(2.0, findOptions.getSearchDuration(), 0.01);
        
        assertNotNull(actionStep.getObjectCollection());
        assertEquals(1, actionStep.getObjectCollection().getStateRegions().size());
    }

    @Test
    @DisplayName("Should handle empty ObjectCollection")
    void testActionStepWithEmptyObjectCollection() throws ConfigurationException {
        String json = """
        {
            "actionConfig": {
                "@type": "ClickOptions"
            },
            "objectCollection": {}
        }
        """;

        ActionStep actionStep = jsonParser.convertJson(json, ActionStep.class);

        assertNotNull(actionStep);
        assertNotNull(actionStep.getObjectCollection());
        assertTrue(actionStep.getObjectCollection().getStateImages().isEmpty());
        assertTrue(actionStep.getObjectCollection().getStateRegions().isEmpty());
        assertTrue(actionStep.getObjectCollection().getStateLocations().isEmpty());
    }

    @Test
    @DisplayName("Should handle null ActionConfig")
    void testActionStepWithNullActionConfig() throws ConfigurationException {
        String json = """
        {
            "objectCollection": {
                "stateImages": []
            }
        }
        """;

        ActionStep actionStep = jsonParser.convertJson(json, ActionStep.class);

        assertNotNull(actionStep);
        assertNull(actionStep.getActionConfig());
        assertNotNull(actionStep.getObjectCollection());
    }
}