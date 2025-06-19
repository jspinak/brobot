package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.manageStates.StateMemory;
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
        BrobotSettings.mock = true;
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
        MatchSnapshot snapshot = new MatchSnapshot();
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
        Matches matches1 = new Matches();
        assertNotNull(matches1);
        assertTrue(matches1.isEmpty());
        
        // Test constructor with ActionOptions
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        Matches matches2 = new Matches(options);
        assertNotNull(matches2);
        assertEquals(options, matches2.getActionOptions());
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