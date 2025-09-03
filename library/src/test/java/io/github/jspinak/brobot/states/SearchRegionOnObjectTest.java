package io.github.jspinak.brobot.states;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SearchRegionOnObjectTest extends BrobotTestBase {
    
    @Test
    public void testSearchRegionOnObjectBuilder() {
        MatchAdjustmentOptions adjustOptions = MatchAdjustmentOptions.builder()
                .setAddX(3)
                .setAddY(10)
                .setAddW(30)
                .setAddH(55)
                .build();
        
        assertNotNull(adjustOptions);
        assertEquals(3, adjustOptions.getAddX());
        assertEquals(10, adjustOptions.getAddY());
        assertEquals(30, adjustOptions.getAddW());
        assertEquals(55, adjustOptions.getAddH());
        
        SearchRegionOnObject searchRegion = SearchRegionOnObject.builder()
                .setTargetType(StateObject.Type.IMAGE)
                .setTargetStateName("Prompt")
                .setTargetObjectName("ClaudePrompt")
                .setAdjustments(adjustOptions)
                .build();
        
        assertNotNull(searchRegion);
        assertEquals(StateObject.Type.IMAGE, searchRegion.getTargetType());
        assertEquals("Prompt", searchRegion.getTargetStateName());
        assertEquals("ClaudePrompt", searchRegion.getTargetObjectName());
        assertNotNull(searchRegion.getAdjustments());
    }
}