package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify correct API usage
 */
public class APIVerificationTest {

    @Test
    void testPatternCreation() {
        // Creating a Pattern with a name
        Pattern pattern = new Pattern.Builder()
                .setName("TestPattern")
                .setBufferedImage(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
                .build();
        
        assertEquals("TestPattern", pattern.getName());
    }
    
    @Test
    void testStateImageCreation() {
        Pattern pattern = new Pattern.Builder()
                .setName("TestPattern")
                .build();
                
        StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .setName("TestStateImage")
                .build();
                
        assertEquals("TestStateImage", stateImage.getName());
    }
    
    @Test
    void testMatchSnapshotCreation() {
        ActionRecord snapshot = new ActionRecord();
        snapshot.setActionSuccess(true);
        snapshot.setDuration(0.5);
        
        Match match = new Match.Builder()
                .setRegion(new Region(10, 10, 50, 50))
                .setSimScore(0.95)
                .build();
                
        snapshot.addMatch(match);
        
        assertTrue(snapshot.isActionSuccess());
        assertEquals(1, snapshot.getMatchList().size());
    }
    
    @Test
    void testMatchesCreation() {
        ActionResult matches = new ActionResult();
        
        Match match = new Match.Builder()
                .setRegion(new Region(10, 10, 50, 50))
                .setSimScore(0.95)
                .build();
                
        matches.add(match);
        
        assertEquals(1, matches.size());
    }
    
    @Test
    void testPatternMatchHistory() {
        Pattern pattern = new Pattern.Builder()
                .setName("TestPattern")
                .build();
                
        ActionRecord snapshot = new ActionRecord();
        snapshot.setActionSuccess(true);
        
        // Add snapshot to pattern's match history
        pattern.getMatchHistory().addSnapshot(snapshot);
        
        assertEquals(1, pattern.getMatchHistory().getSnapshots().size());
    }
}