package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify MatchSnapshot comparison behavior in mock mode
 */
public class MockMatchSnapshotTest {
    
    @BeforeEach
    void setUp() {
        BrobotSettings.mock = true;
    }
    
    @Test
    void testMatchSnapshotComparison() {
        // Create a match
        Match match1 = new Match.Builder()
                .setName("TestMatch")
                .setRegion(10, 20, 30, 40)
                .build();
                
        // Create another match with same values
        Match match2 = new Match.Builder()
                .setName("TestMatch")
                .setRegion(10, 20, 30, 40)
                .build();
                
        // Test if matches are equal
        System.out.println("Match1 equals Match2: " + match1.equals(match2));
        System.out.println("Match1 region: " + match1.getRegion());
        System.out.println("Match2 region: " + match2.getRegion());
        
        // Create snapshots
        MatchSnapshot snapshot1 = new MatchSnapshot();
        snapshot1.addMatch(match1);
        snapshot1.setText("Test");
        
        MatchSnapshot snapshot2 = new MatchSnapshot();
        snapshot2.addMatch(match2);
        snapshot2.setText("Test");
        
        // Test comparison
        boolean hasSameResults = snapshot1.hasSameResultsAs(snapshot2);
        System.out.println("HasSameResults: " + hasSameResults);
        
        // For debugging
        if (!hasSameResults) {
            System.out.println("Match comparison failed");
            System.out.println("Match1: " + match1);
            System.out.println("Match2: " + match2);
        }
    }
}