package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.model.element.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.sikuli.script.Mouse;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ConfigurableOffsetManager.
 * This class tests offset functionality in the modern ActionConfig API.
 */
class OffsetLocationManagerTest {

    private OffsetMatchCreator offsetLocationManager;
    
    @BeforeEach
    void setUp() {
        offsetLocationManager = new OffsetMatchCreator();
    }
    
    @Test
    void testAddOffsetAsOnlyMatch_WithEmptyCollections() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Setup mouse at a known position
            mouseMock.when(Mouse::at).thenReturn(new Location(50, 50).sikuli());
            
            MatchAdjustmentOptions adjustmentOptions = MatchAdjustmentOptions.builder()
                .addX(10)
                .addY(20)
                .build();
            
            ActionResult matches = new ActionResult();
            ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
            List<ObjectCollection> collections = Arrays.asList(emptyCollection);
            
            // Execute
            offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, adjustmentOptions, true);
            
            // Verify - should add an offset match since collections are empty
            assertEquals(1, matches.getMatchList().size());
            Match offsetMatch = matches.getMatchList().get(0);
            assertNotNull(offsetMatch.getRegion());
            assertEquals(60, offsetMatch.getRegion().getX()); // 50 + 10
            assertEquals(70, offsetMatch.getRegion().getY()); // 50 + 20
        }
    }
    
    @Test
    void testAddOffsetAsOnlyMatch_WithMousePosition() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Setup mouse at specific position
            Location mouseLocation = new Location(100, 100);
            mouseMock.when(Mouse::at).thenReturn(mouseLocation.sikuli());
            
            MatchAdjustmentOptions adjustmentOptions = MatchAdjustmentOptions.builder()
                .addX(50)
                .addY(30)
                .build();
            
            ActionResult matches = new ActionResult();
            ObjectCollection collection = new ObjectCollection.Builder().build();
            List<ObjectCollection> collections = Arrays.asList(collection);
            
            // Execute - should add offset from mouse position
            offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, adjustmentOptions, false);
            
            // Verify
            assertEquals(1, matches.getMatchList().size());
            Match offsetMatch = matches.getMatchList().get(0);
            assertEquals(150, offsetMatch.getRegion().getX()); // 100 + 50
            assertEquals(130, offsetMatch.getRegion().getY()); // 100 + 30
        }
    }
    
    @Test
    void testAddOffsetAsOnlyMatch_WithNonEmptyCollections() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Setup mouse at a known position
            mouseMock.when(Mouse::at).thenReturn(new Location(0, 0).sikuli());
            
            MatchAdjustmentOptions adjustmentOptions = MatchAdjustmentOptions.builder()
                .addX(10)
                .addY(20)
                .build();
            
            ActionResult matches = new ActionResult();
            
            // Create non-empty collection
            ObjectCollection nonEmptyCollection = new ObjectCollection.Builder()
                .withStrings("test")
                .build();
            List<ObjectCollection> collections = Arrays.asList(nonEmptyCollection);
            
            // Execute with doOnlyWhenCollectionsAreEmpty = true
            offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, adjustmentOptions, true);
            
            // Verify - should NOT add offset match since collections are not empty
            assertEquals(0, matches.getMatchList().size());
            
            // Execute with doOnlyWhenCollectionsAreEmpty = false
            offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, adjustmentOptions, false);
            
            // Verify - should add offset match regardless of collection content
            assertEquals(1, matches.getMatchList().size());
            assertEquals(10, matches.getMatchList().get(0).getRegion().getX());
            assertEquals(20, matches.getMatchList().get(0).getRegion().getY());
        }
    }
    
    @Test
    void testAddOffsetAsOnlyMatch_WithNullAdjustmentOptions() {
        // Setup
        ActionResult matches = new ActionResult();
        ObjectCollection collection = new ObjectCollection.Builder().build();
        List<ObjectCollection> collections = Arrays.asList(collection);
        
        // Execute with null adjustment options
        offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, null, false);
        
        // Verify - should not add any matches when adjustment options are null
        assertEquals(0, matches.getMatchList().size());
    }
    
    @Test
    void testAddOffsetAsOnlyMatch_WithZeroOffset() {
        // Setup
        MatchAdjustmentOptions adjustmentOptions = MatchAdjustmentOptions.builder()
            .addX(0)  // Zero offset should not create a match
            .addY(0)
            .build();
        
        ActionResult matches = new ActionResult();
        ObjectCollection collection = new ObjectCollection.Builder().build();
        List<ObjectCollection> collections = Arrays.asList(collection);
        
        // Execute
        offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, adjustmentOptions, false);
        
        // Verify - should not add match when offset is zero
        assertEquals(0, matches.getMatchList().size());
    }
    
    @Test
    void testAddOffsetAsOnlyMatch_CreatesCorrectRegion() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Setup mouse at origin
            mouseMock.when(Mouse::at).thenReturn(new Location(0, 0).sikuli());
            
            MatchAdjustmentOptions adjustmentOptions = MatchAdjustmentOptions.builder()
                .addX(100)
                .addY(200)
                .build();
            
            ActionResult matches = new ActionResult();
            List<ObjectCollection> collections = Arrays.asList(new ObjectCollection.Builder().build());
            
            // Execute
            offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, adjustmentOptions, false);
            
            // Verify the created region
            assertEquals(1, matches.getMatchList().size());
            Match match = matches.getMatchList().get(0);
            Region region = match.getRegion();
            assertEquals(100, region.getX());
            assertEquals(200, region.getY());
            assertEquals(1, region.getW());  // Offset matches have 1x1 size
            assertEquals(1, region.getH());
        }
    }
}