package io.github.jspinak.brobot.states;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateImageBuilderTest extends BrobotTestBase {
    
    @Test
    public void testStateImageWithSearchRegionOnObject() {
        SearchRegionOnObject searchRegion = SearchRegionOnObject.builder()
                .setTargetType(StateObject.Type.IMAGE)
                .setTargetStateName("Prompt")
                .setTargetObjectName("ClaudePrompt")
                .setAdjustments(MatchAdjustmentOptions.builder()
                        .setAddX(3)
                        .setAddY(10)
                        .setAddW(30)
                        .setAddH(55)
                        .build())
                .build();
        
        assertNotNull(searchRegion);
        
        // Now test with StateImage.Builder
        // Create a Pattern object directly without loading an image file
        Pattern testPattern = new Pattern();
        
        StateImage stateImage = new StateImage.Builder()
                .addPattern(testPattern)
                .setName("TestImage")
                .setSearchRegionOnObject(searchRegion)
                .build();
        
        assertNotNull(stateImage);
        assertNotNull(stateImage.getSearchRegionOnObject());
        assertEquals("TestImage", stateImage.getName());
        assertEquals(searchRegion, stateImage.getSearchRegionOnObject());
    }
}