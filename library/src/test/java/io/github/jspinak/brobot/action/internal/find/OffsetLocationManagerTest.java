package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.sikuli.script.Mouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OffsetLocationManagerTest {

    private OffsetLocationManager offsetLocationManager;
    
    @BeforeEach
    void setUp() {
        offsetLocationManager = new OffsetLocationManager();
    }
    
    @Test
    void testAddOffset_EmptyMatches_AddsOffsetAsOnlyMatch() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Setup
            Location mouseLocation = new Location(100, 100);
            mouseMock.when(Mouse::at).thenReturn(mouseLocation.sikuli());
            
            ActionOptions actionOptions = new ActionOptions();
            actionOptions.setAddX(10);
            actionOptions.setAddY(20);
            
            ActionResult matches = new ActionResult();
            matches.setActionOptions(actionOptions);
            
            ObjectCollection collection = new ObjectCollection.Builder().build();
            List<ObjectCollection> collections = Arrays.asList(collection);
            
            // Execute
            offsetLocationManager.addOffset(collections, matches, actionOptions);
            
            // Verify
            assertEquals(1, matches.getMatchList().size());
            Match offsetMatch = matches.getMatchList().get(0);
            assertNotNull(offsetMatch);
            assertNotNull(offsetMatch.getStateObjectData());
            assertEquals(110, offsetMatch.x()); // 100 + 10
            assertEquals(120, offsetMatch.y()); // 100 + 20
        }
    }
    
    @Test
    void testAddOffset_NonEmptyMatches_AddsOffsetAsLastMatch() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setAddX2(30);
        actionOptions.setAddY2(40);
        
        ActionResult matches = new ActionResult();
        matches.setActionOptions(actionOptions);
        
        // Add existing match
        Match existingMatch = new Match.Builder()
                .setRegion(new Region(100, 100, 50, 50))
                .setSimScore(0.9)
                .setStateObjectData(new StateObjectMetadata())
                .build();
        matches.add(existingMatch);
        
        ObjectCollection collection = new ObjectCollection.Builder().build();
        List<ObjectCollection> collections = Arrays.asList(collection);
        
        // Execute
        offsetLocationManager.addOffset(collections, matches, actionOptions);
        
        // Verify
        assertEquals(2, matches.getMatchList().size());
        Match offsetMatch = matches.getMatchList().get(1);
        assertEquals(130, offsetMatch.x()); // 100 + 30
        assertEquals(140, offsetMatch.y()); // 100 + 40
    }
    
    @Test
    void testAddOffsetAsOnlyMatch_WithOffset() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Setup
            Location mouseLocation = new Location(200, 300);
            mouseMock.when(Mouse::at).thenReturn(mouseLocation.sikuli());
            
            ActionOptions actionOptions = new ActionOptions();
            actionOptions.setAddX(50);
            actionOptions.setAddY(60);
            
            ActionResult matches = new ActionResult();
            matches.setActionOptions(actionOptions);
            
            ObjectCollection collection = new ObjectCollection.Builder().build();
            List<ObjectCollection> collections = Arrays.asList(collection);
            
            // Execute
            offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, false);
            
            // Verify
            assertEquals(1, matches.getMatchList().size());
            Match offsetMatch = matches.getMatchList().get(0);
            assertEquals(250, offsetMatch.x()); // 200 + 50
            assertEquals(360, offsetMatch.y()); // 300 + 60
        }
    }
    
    @Test
    void testAddOffsetAsOnlyMatch_NoOffset() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setAddX(0); // No offset
        actionOptions.setAddY(10);
        
        ActionResult matches = new ActionResult();
        matches.setActionOptions(actionOptions);
        
        List<ObjectCollection> collections = new ArrayList<>();
        
        // Execute
        offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, false);
        
        // Verify
        assertTrue(matches.isEmpty()); // Should not add offset when addX is 0
    }
    
    @Test
    void testAddOffsetAsOnlyMatch_DoOnlyWhenEmpty_CollectionsNotEmpty() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setAddX(10);
        actionOptions.setAddY(20);
        
        ActionResult matches = new ActionResult();
        matches.setActionOptions(actionOptions);
        
        // Create non-empty collection
        Pattern pattern = new Pattern.Builder()
                .setName("pattern1")
                .build();
        ObjectCollection collection = new ObjectCollection.Builder()
                .withPatterns(pattern)
                .build();
        List<ObjectCollection> collections = Arrays.asList(collection);
        
        // Execute
        offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, true);
        
        // Verify
        assertTrue(matches.isEmpty()); // Should not add offset when collections not empty and doOnlyWhenEmpty is true
    }
    
    @Test
    void testAddOffsetAsOnlyMatch_DoOnlyWhenEmpty_CollectionsEmpty() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Setup
            Location mouseLocation = new Location(100, 200);
            mouseMock.when(Mouse::at).thenReturn(mouseLocation.sikuli());
            
            ActionOptions actionOptions = new ActionOptions();
            actionOptions.setAddX(25);
            actionOptions.setAddY(35);
            
            ActionResult matches = new ActionResult();
            matches.setActionOptions(actionOptions);
            
            // Create empty collection
            ObjectCollection collection = new ObjectCollection.Builder().build();
            List<ObjectCollection> collections = Arrays.asList(collection);
            
            // Execute
            offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, true);
            
            // Verify
            assertEquals(1, matches.getMatchList().size());
            Match offsetMatch = matches.getMatchList().get(0);
            assertEquals(125, offsetMatch.x()); // 100 + 25
            assertEquals(235, offsetMatch.y()); // 200 + 35
        }
    }
    
    @Test
    void testAddOffsetAsLastMatch_WithOffset() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setAddX2(15);
        actionOptions.setAddY2(25);
        
        ActionResult matches = new ActionResult();
        
        // Add existing match with state data
        StateObjectMetadata stateData = new StateObjectMetadata();
        Match existingMatch = new Match.Builder()
                .setRegion(new Region(50, 75, 20, 20))
                .setSimScore(0.95)
                .setStateObjectData(stateData)
                .build();
        matches.add(existingMatch);
        
        // Execute
        offsetLocationManager.addOffsetAsLastMatch(matches, actionOptions);
        
        // Verify
        assertEquals(2, matches.getMatchList().size());
        Match offsetMatch = matches.getMatchList().get(1);
        assertEquals(65, offsetMatch.x()); // 50 + 15
        assertEquals(100, offsetMatch.y()); // 75 + 25
        assertSame(stateData, offsetMatch.getStateObjectData()); // Should inherit state data
    }
    
    @Test
    void testAddOffsetAsLastMatch_NoOffset() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setAddX2(0); // No offset
        actionOptions.setAddY2(10);
        
        ActionResult matches = new ActionResult();
        Match existingMatch = new Match.Builder()
                .setRegion(new Region(10, 10, 10, 10))
                .setSimScore(0.9)
                .build();
        matches.add(existingMatch);
        
        // Execute
        offsetLocationManager.addOffsetAsLastMatch(matches, actionOptions);
        
        // Verify
        assertEquals(1, matches.getMatchList().size()); // Should not add offset when addX2 is 0
    }
    
    @Test
    void testAddOffsetAsLastMatch_EmptyMatches() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setAddX2(10);
        actionOptions.setAddY2(20);
        
        ActionResult matches = new ActionResult();
        
        // Execute
        offsetLocationManager.addOffsetAsLastMatch(matches, actionOptions);
        
        // Verify
        assertTrue(matches.isEmpty()); // Should not add offset when no existing matches
    }
    
    @Test
    void testAddOffsetAsLastMatch_MultipleMatches() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        actionOptions.setAddX2(5);
        actionOptions.setAddY2(10);
        
        ActionResult matches = new ActionResult();
        
        // Add multiple matches
        Match match1 = new Match.Builder()
                .setRegion(new Region(10, 10, 10, 10))
                .setSimScore(0.9)
                .setStateObjectData(new StateObjectMetadata())
                .build();
        Match match2 = new Match.Builder()
                .setRegion(new Region(30, 30, 10, 10))
                .setSimScore(0.95)
                .setStateObjectData(new StateObjectMetadata())
                .build();
        Match match3 = new Match.Builder()
                .setRegion(new Region(60, 70, 10, 10))
                .setSimScore(0.85)
                .setStateObjectData(new StateObjectMetadata())
                .build();
        
        matches.add(match1);
        matches.add(match2);
        matches.add(match3);
        
        // Execute
        offsetLocationManager.addOffsetAsLastMatch(matches, actionOptions);
        
        // Verify
        assertEquals(4, matches.getMatchList().size());
        Match offsetMatch = matches.getMatchList().get(3);
        assertEquals(65, offsetMatch.x()); // 60 + 5 (from last match)
        assertEquals(80, offsetMatch.y()); // 70 + 10 (from last match)
    }
}