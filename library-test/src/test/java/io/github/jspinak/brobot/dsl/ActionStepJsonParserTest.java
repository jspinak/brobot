package io.github.jspinak.brobot.dsl;

import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ActionStepJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Test
    void testActionStepFromJson() throws ConfigurationException {
        String json = """
        {
            "actionOptions": {
                "action": "CLICK",
                "find": "ALL"
            },
            "objectCollection": {
                "stateImages": [
                    {
                        "id": "123",
                        "name": "TestImage",
                        "patterns": []
                    }
                ]
            }
        }
        """;

        ActionStep actionStep = jsonParser.convertJson(json, ActionStep.class);

        assertNotNull(actionStep);
        assertEquals("CLICK", actionStep.getActionOptions().getAction().name());
        assertEquals("ALL", actionStep.getActionOptions().getFind().name());
        assertEquals(1, actionStep.getObjectCollection().getStateImages().size());
        assertEquals("TestImage", actionStep.getObjectCollection().getStateImages().get(0).getName());
    }
}