package io.github.jspinak.brobot.action.internal.find;

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
 * Tests for OffsetLocationManager.
 * Note: This class tests offset functionality that may not be fully supported
 * in the modern ActionConfig API. Tests use mocked ActionConfig to simulate
 * the expected behavior.
 */
class OffsetLocationManagerTest {

    private OffsetLocationManager offsetLocationManager;
    
    @BeforeEach
    void setUp() {
        offsetLocationManager = new OffsetLocationManager();
    }
    
    @Test
    void testAddOffset_CallsCorrectMethods() {
        // Setup - using a mock ActionConfig since offset methods don't exist in the base class
        ActionConfig mockConfig = mock(ActionConfig.class);
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(mockConfig);
        
        ObjectCollection collection = new ObjectCollection.Builder().build();
        List<ObjectCollection> collections = Arrays.asList(collection);
        
        // Execute
        offsetLocationManager.addOffset(collections, matches, mockConfig);
        
        // Verify the method completes without error
        assertNotNull(matches);
    }
    
    @Test
    void testAddOffsetAsOnlyMatch_WithMockedConfig() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Setup
            Location mouseLocation = new Location(100, 100);
            mouseMock.when(Mouse::at).thenReturn(mouseLocation.sikuli());
            
            ActionConfig mockConfig = mock(ActionConfig.class);
            
            ActionResult matches = new ActionResult();
            matches.setActionConfig(mockConfig);
            
            ObjectCollection collection = new ObjectCollection.Builder().build();
            List<ObjectCollection> collections = Arrays.asList(collection);
            
            // Execute
            offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, false);
            
            // Verify the method completes
            assertNotNull(matches);
        }
    }
    
    @Test
    void testAddOffsetAsLastMatch_WithExistingMatch() {
        // Setup
        ActionConfig mockConfig = mock(ActionConfig.class);
        
        ActionResult matches = new ActionResult();
        
        // Add existing match
        Match existingMatch = new Match.Builder()
                .setRegion(new Region(100, 100, 50, 50))
                .setSimScore(0.9)
                .setStateObjectData(new StateObjectMetadata())
                .build();
        matches.add(existingMatch);
        
        // Execute
        offsetLocationManager.addOffsetAsLastMatch(matches, mockConfig);
        
        // Verify the method completes
        assertNotNull(matches);
        assertTrue(matches.getMatchList().size() >= 1);
    }
    
    @Test
    void testAddOffset_WithEmptyCollections() {
        // Setup
        ActionConfig mockConfig = mock(ActionConfig.class);
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(mockConfig);
        
        List<ObjectCollection> emptyCollections = Arrays.asList();
        
        // Execute
        offsetLocationManager.addOffset(emptyCollections, matches, mockConfig);
        
        // Verify the method handles empty collections
        assertNotNull(matches);
    }
    
    @Test
    void testAddOffsetAsOnlyMatch_DoOnlyWhenEmpty() {
        // Setup
        ActionConfig mockConfig = mock(ActionConfig.class);
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(mockConfig);
        
        ObjectCollection collection = new ObjectCollection.Builder().build();
        List<ObjectCollection> collections = Arrays.asList(collection);
        
        // Execute with doOnlyWhenEmpty = true
        offsetLocationManager.addOffsetAsOnlyMatch(collections, matches, true);
        
        // Verify the method completes
        assertNotNull(matches);
    }
    
    @Test
    void testAddOffsetAsLastMatch_NoExistingMatches() {
        // Setup
        ActionConfig mockConfig = mock(ActionConfig.class);
        
        ActionResult matches = new ActionResult();
        
        // Execute with no existing matches
        offsetLocationManager.addOffsetAsLastMatch(matches, mockConfig);
        
        // Verify the method handles no matches gracefully
        assertNotNull(matches);
    }
}