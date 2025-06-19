package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
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
        MatchSnapshot snapshot = new MatchSnapshot();
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
        Matches matches = new Matches();
        
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
                
        MatchSnapshot snapshot = new MatchSnapshot();
        snapshot.setActionSuccess(true);
        
        // Add snapshot to pattern's match history
        pattern.getMatchHistory().addSnapshot(snapshot);
        
        assertEquals(1, pattern.getMatchHistory().getSnapshots().size());
    }
}