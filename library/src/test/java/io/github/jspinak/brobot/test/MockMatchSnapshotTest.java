package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify MatchSnapshot comparison behavior in mock mode
 */
public class MockMatchSnapshotTest {
    
    @BeforeEach
    void setUp() {
        FrameworkSettings.mock = true;
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
        ActionRecord snapshot1 = new ActionRecord();
        snapshot1.addMatch(match1);
        snapshot1.setText("Test");
        
        ActionRecord snapshot2 = new ActionRecord();
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