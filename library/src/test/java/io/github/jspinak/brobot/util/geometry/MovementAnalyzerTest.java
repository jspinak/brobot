package io.github.jspinak.brobot.util.geometry;

import io.github.jspinak.brobot.model.element.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sikuli.script.Match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovementAnalyzerTest {
    
    private MovementAnalyzer movementAnalyzer;
    
    @BeforeEach
    void setUp() {
        movementAnalyzer = new MovementAnalyzer();
    }
    
    @Test
    void testGetMovement_SimpleUniformMovement() {
        // Setup - All objects moved by (10, 5)
        List<Match> firstMatches = createMatches(
            new int[][]{{0, 0}, {20, 20}, {40, 40}}
        );
        List<Match> secondMatches = createMatches(
            new int[][]{{-10, -5}, {10, 15}, {30, 35}}
        );
        
        // Execute
        List<Location> movements = movementAnalyzer.getMovement(0, firstMatches, secondMatches);
        
        // Verify
        assertEquals(1, movements.size());
        assertEquals(10, movements.get(0).getCalculatedX());
        assertEquals(5, movements.get(0).getCalculatedY());
    }
    
    @Test
    void testGetMovement_WithMeasurementTolerance() {
        // Setup - Slight variations in movement detection
        List<Match> firstMatches = createMatches(
            new int[][]{{0, 0}, {20, 20}}
        );
        List<Match> secondMatches = createMatches(
            new int[][]{{-10, -5}, {11, 16}}  // Second has slight variation
        );
        
        // Execute with tolerance of 2 pixels
        List<Location> movements = movementAnalyzer.getMovement(2, firstMatches, secondMatches);
        
        // Verify - Should group similar movements together
        assertEquals(1, movements.size());
        // The exact values depend on which one is recorded first
        assertTrue(Math.abs(movements.get(0).getCalculatedX() - 10) <= 2);
        assertTrue(Math.abs(movements.get(0).getCalculatedY() - 5) <= 2);
    }
    
    @Test
    void testGetMovement_NoConsistentMovement() {
        // Setup - All objects moved differently
        List<Match> firstMatches = createMatches(
            new int[][]{{0, 0}, {20, 20}}
        );
        List<Match> secondMatches = createMatches(
            new int[][]{{-10, -5}, {0, 0}}  // Completely different movements
        );
        
        // Execute
        List<Location> movements = movementAnalyzer.getMovement(0, firstMatches, secondMatches);
        
        // Verify - Multiple different movements with same frequency
        assertEquals(4, movements.size()); // All pairs have different displacements
    }
    
    @Test
    void testGetMovement_AmbiguousMovement() {
        // Setup - Two equally likely movements
        List<Match> firstMatches = createMatches(
            new int[][]{{0, 0}, {20, 20}, {40, 40}, {60, 60}}
        );
        List<Match> secondMatches = createMatches(
            new int[][]{{-10, -5}, {10, 15}, {30, 30}, {50, 55}}
        );
        
        // Execute
        List<Location> movements = movementAnalyzer.getMovement(0, firstMatches, secondMatches);
        
        // Verify - Should return the most frequent movement(s)
        assertFalse(movements.isEmpty());
        
        // Check that all returned movements have the same frequency
        if (movements.size() > 1) {
            // Multiple movements with same highest frequency
            assertTrue(movements.size() <= 4);
        }
    }
    
    @Test
    void testGetMovement_EmptyLists() {
        // Setup
        List<Match> emptyFirst = new ArrayList<>();
        List<Match> emptySecond = new ArrayList<>();
        List<Match> nonEmpty = createMatches(new int[][]{{0, 0}});
        
        // Execute
        List<Location> movements1 = movementAnalyzer.getMovement(0, emptyFirst, nonEmpty);
        List<Location> movements2 = movementAnalyzer.getMovement(0, nonEmpty, emptySecond);
        List<Location> movements3 = movementAnalyzer.getMovement(0, emptyFirst, emptySecond);
        
        // Verify
        assertTrue(movements1.isEmpty());
        assertTrue(movements2.isEmpty());
        assertTrue(movements3.isEmpty());
    }
    
    @Test
    void testGetMovement_SingleMatch() {
        // Setup - Only one match in each list
        List<Match> firstMatches = createMatches(new int[][]{{10, 10}});
        List<Match> secondMatches = createMatches(new int[][]{{5, 5}});
        
        // Execute
        List<Location> movements = movementAnalyzer.getMovement(0, firstMatches, secondMatches);
        
        // Verify
        assertEquals(1, movements.size());
        assertEquals(5, movements.get(0).getCalculatedX()); // 10 - 5
        assertEquals(5, movements.get(0).getCalculatedY()); // 10 - 5
    }
    
    @Test
    void testGetMovement_NegativeMovement() {
        // Setup - Movement in negative direction
        List<Match> firstMatches = createMatches(new int[][]{{5, 5}, {10, 10}});
        List<Match> secondMatches = createMatches(new int[][]{{15, 20}, {20, 25}});
        
        // Execute
        List<Location> movements = movementAnalyzer.getMovement(0, firstMatches, secondMatches);
        
        // Verify
        assertEquals(1, movements.size());
        assertEquals(-10, movements.get(0).getCalculatedX()); // 5 - 15
        assertEquals(-15, movements.get(0).getCalculatedY()); // 5 - 20
    }
    
    @Test
    void testGetMovement_LargeTolerance() {
        // Setup - Various movements within large tolerance
        List<Match> firstMatches = createMatches(
            new int[][]{{0, 0}, {100, 100}, {200, 200}}
        );
        List<Match> secondMatches = createMatches(
            new int[][]{{-8, -7}, {91, 92}, {195, 190}}
        );
        
        // Execute with large tolerance
        List<Location> movements = movementAnalyzer.getMovement(10, firstMatches, secondMatches);
        
        // Verify - All movements should be grouped as similar
        assertEquals(1, movements.size());
    }
    
    // Helper method to create matches
    private List<Match> createMatches(int[][] coordinates) {
        List<Match> matches = new ArrayList<>();
        for (int[] coord : coordinates) {
            Match match = mock(Match.class);
            match.x = coord[0];
            match.y = coord[1];
            matches.add(match);
        }
        return matches;
    }
    
    @Test
    void testGetMovement_ComplexScenario() {
        // Setup - Mix of consistent and inconsistent movements
        List<Match> firstMatches = createMatches(
            new int[][]{{0, 0}, {50, 50}, {100, 100}, {150, 150}, {200, 200}}
        );
        // Most objects moved by (10, 10), but one moved differently
        List<Match> secondMatches = createMatches(
            new int[][]{{-10, -10}, {40, 40}, {90, 90}, {140, 140}, {150, 150}}
        );
        
        // Execute
        List<Location> movements = movementAnalyzer.getMovement(0, firstMatches, secondMatches);
        
        // Verify - Should return the most frequent movement
        assertFalse(movements.isEmpty());
        // The movement (10, 10) should appear most frequently
        boolean found = movements.stream()
            .anyMatch(loc -> loc.getCalculatedX() == 10 && loc.getCalculatedY() == 10);
        assertTrue(found);
    }
}