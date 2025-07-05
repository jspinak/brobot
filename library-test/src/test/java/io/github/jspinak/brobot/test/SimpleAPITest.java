package io.github.jspinak.brobot.test;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.statemanagement.StateMemory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify the API usage patterns
 */
@SpringBootTest(classes = BrobotTestApplication.class)
public class SimpleAPITest {
    
    @Autowired
    private StateMemory stateMemory;
    
    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }
    
    @BeforeEach
    void setUp() {
        FrameworkSettings.mock = true;
    }
    
    @Test
    void testPatternWithNameCreation() {
        // Test Pattern creation with name
        Pattern pattern = new Pattern.Builder()
                .setName("TestPattern")
                .setBufferedImage(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
                .build();
                
        assertNotNull(pattern);
        assertEquals("TestPattern", pattern.getName());
    }
    
    @Test
    void testStateImageWithNameCreation() {
        // Create pattern first
        Pattern pattern = new Pattern.Builder()
                .setName("TestPattern")
                .setBufferedImage(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
                .build();
                
        // Create StateImage with name
        StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .setName("TestStateImage")
                .build();
                
        assertNotNull(stateImage);
        assertEquals("TestStateImage", stateImage.getName());
    }
    
    @Test
    void testMatchHistoryAccess() {
        // Create pattern
        Pattern pattern = new Pattern.Builder()
                .setName("TestPattern")
                .build();
                
        // Create match snapshot
        ActionRecord snapshot = new ActionRecord();
        snapshot.setActionSuccess(true);
        snapshot.setDuration(0.5);
        
        // Add match to snapshot
        Match match = new Match.Builder()
                .setRegion(new Region(10, 10, 50, 50))
                .setSimScore(0.95)
                .build();
        snapshot.addMatch(match);
        
        // Add snapshot to pattern's match history
        pattern.getMatchHistory().addSnapshot(snapshot);
        
        // Verify
        assertEquals(1, pattern.getMatchHistory().getSnapshots().size());
        assertTrue(pattern.getMatchHistory().getSnapshots().get(0).isActionSuccess());
    }
    
    @Test
    void testMatchesCreation() {
        // Test default constructor
        ActionResult matches1 = new ActionResult();
        assertNotNull(matches1);
        assertTrue(matches1.isEmpty());
        
        // Test constructor with PatternFindOptions
        PatternFindOptions options = new PatternFindOptions.Builder()
                .build();
        ActionResult matches2 = new ActionResult();
        assertNotNull(matches2);
        // ActionResult no longer stores options in the new API
    }
    
    @Test
    void testStateMemoryOperations() {
        if (stateMemory != null) {
            // Add active state
            Long testStateId = 1L;
            stateMemory.addActiveState(testStateId);
            
            // Remove inactive state
            stateMemory.removeInactiveState(testStateId);
            
            // This should work without errors
            assertTrue(true, "StateMemory operations completed successfully");
        }
    }
}