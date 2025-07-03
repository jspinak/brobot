package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Anchor;
import io.github.jspinak.brobot.model.element.Anchors;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NonImageObjectConverterTest {

    private NonImageObjectConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new NonImageObjectConverter();
    }
    
    @Test
    void testGetOtherObjectsDirectlyAsMatchObjects_Empty() {
        // Setup
        ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
        
        // Execute
        ActionResult result = converter.getOtherObjectsDirectlyAsMatchObjects(emptyCollection);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetOtherObjectsDirectlyAsMatchObjects_AllTypes() {
        // Setup
        StateRegion stateRegion = new StateRegion.Builder()
                .setSearchRegion(new Region(10, 10, 100, 100))
                .setOwnerStateName("TestState")
                .build();
                
        StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(new Location(50, 50))
                .setOwnerStateName("TestState")
                .build();
                
        Match existingMatch = new Match.Builder()
                .setRegion(new Region(200, 200, 50, 50))
                .build();
        ActionResult existingMatches = new ActionResult();
        existingMatches.add(existingMatch);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(stateRegion)
                .withLocations(stateLocation)
                .withMatches(existingMatches)
                .build();
        
        // Execute
        ActionResult result = converter.getOtherObjectsDirectlyAsMatchObjects(collection);
        
        // Verify
        assertNotNull(result);
        assertEquals(3, result.size());
    }
    
    @Test
    void testAddRegions_Single() {
        // Setup
        ActionResult matches = new ActionResult();
        StateRegion stateRegion = new StateRegion.Builder()
                .setSearchRegion(new Region(10, 20, 30, 40))
                .setOwnerStateName("TestState")
                .build();
                
        ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(stateRegion)
                .build();
        
        // Execute
        converter.addRegions(matches, collection);
        
        // Verify
        assertEquals(1, matches.size());
        Match match = matches.getMatchList().get(0);
        assertEquals(10, match.x());
        assertEquals(20, match.y());
        assertEquals(30, match.w());
        assertEquals(40, match.h());
        assertNotNull(match.getStateObjectData());
    }
    
    @Test
    void testAddRegions_Multiple() {
        // Setup
        ActionResult matches = new ActionResult();
        StateRegion region1 = new StateRegion.Builder()
                .setSearchRegion(new Region(0, 0, 50, 50))
                .setOwnerStateName("State1")
                .build();
        StateRegion region2 = new StateRegion.Builder()
                .setSearchRegion(new Region(100, 100, 75, 75))
                .setOwnerStateName("State2")
                .build();
                
        ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(region1, region2)
                .build();
        
        // Execute
        converter.addRegions(matches, collection);
        
        // Verify
        assertEquals(2, matches.size());
    }
    
    @Test
    void testAddRegions_WithAnchors() {
        // Setup
        ActionResult matches = new ActionResult();
        Anchors anchors = new Anchors();
        anchors.add(new Anchor(Positions.Name.MIDDLEMIDDLE, new Position(0.5, 0.5)));
        
        StateRegion stateRegion = new StateRegion.Builder()
                .setSearchRegion(new Region(10, 10, 100, 100))
                .setAnchors(anchors)
                .setOwnerStateName("TestState")
                .build();
                
        ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(stateRegion)
                .build();
        
        // Execute
        converter.addRegions(matches, collection);
        
        // Verify
        assertEquals(1, matches.size());
        Match match = matches.getMatchList().get(0);
        assertNotNull(match.getAnchors());
        assertEquals(anchors, match.getAnchors());
    }
    
    @Test
    void testAddLocations_Single() {
        // Setup
        ActionResult matches = new ActionResult();
        StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(new Location(100, 200))
                .setOwnerStateName("TestState")
                .build();
                
        ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
        
        // Execute
        converter.addLocations(matches, collection);
        
        // Verify
        assertEquals(1, matches.size());
        Match match = matches.getMatchList().get(0);
        assertNotNull(match.getRegion());
        assertNotNull(match.getStateObjectData());
    }
    
    @Test
    void testAddLocations_Multiple() {
        // Setup
        ActionResult matches = new ActionResult();
        StateLocation loc1 = new StateLocation.Builder()
                .setLocation(new Location(10, 20))
                .setOwnerStateName("State1")
                .build();
        StateLocation loc2 = new StateLocation.Builder()
                .setLocation(new Location(30, 40))
                .setOwnerStateName("State2")
                .build();
        StateLocation loc3 = new StateLocation.Builder()
                .setLocation(new Location(50, 60))
                .setOwnerStateName("State3")
                .build();
                
        ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(loc1, loc2, loc3)
                .build();
        
        // Execute
        converter.addLocations(matches, collection);
        
        // Verify
        assertEquals(3, matches.size());
    }
    
    @Test
    void testAddMatches_Single() {
        // Setup
        ActionResult matches = new ActionResult();
        
        Match existingMatch = new Match.Builder()
                .setRegion(new Region(10, 10, 20, 20))
                .setSimScore(0.95)
                .build();
        ActionResult existingResults = new ActionResult();
        existingResults.add(existingMatch);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withMatches(existingResults)
                .build();
        
        // Execute
        converter.addMatches(matches, collection);
        
        // Verify
        assertEquals(1, matches.size());
        assertEquals(existingMatch, matches.getMatchList().get(0));
    }
    
    @Test
    void testAddMatches_Multiple() {
        // Setup
        ActionResult matches = new ActionResult();
        
        Match match1 = new Match.Builder()
                .setRegion(new Region(0, 0, 10, 10))
                .build();
        Match match2 = new Match.Builder()
                .setRegion(new Region(20, 20, 10, 10))
                .build();
                
        ActionResult results1 = new ActionResult();
        results1.add(match1);
        
        ActionResult results2 = new ActionResult();
        results2.add(match2);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withMatches(results1, results2)
                .build();
        
        // Execute
        converter.addMatches(matches, collection);
        
        // Verify
        assertEquals(2, matches.size());
        List<Match> matchList = matches.getMatchList();
        assertTrue(matchList.contains(match1));
        assertTrue(matchList.contains(match2));
    }
    
    @Test
    void testAddMatches_WithMultipleMatchesInSingleResult() {
        // Setup
        ActionResult matches = new ActionResult();
        
        Match match1 = new Match.Builder()
                .setRegion(new Region(0, 0, 10, 10))
                .build();
        Match match2 = new Match.Builder()
                .setRegion(new Region(20, 20, 10, 10))
                .build();
        Match match3 = new Match.Builder()
                .setRegion(new Region(40, 40, 10, 10))
                .build();
                
        ActionResult existingResults = new ActionResult();
        existingResults.add(match1);
        existingResults.add(match2);
        existingResults.add(match3);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withMatches(existingResults)
                .build();
        
        // Execute
        converter.addMatches(matches, collection);
        
        // Verify
        assertEquals(3, matches.size());
        List<Match> matchList = matches.getMatchList();
        assertTrue(matchList.contains(match1));
        assertTrue(matchList.contains(match2));
        assertTrue(matchList.contains(match3));
    }
    
    @Test
    void testComplexScenario_MixedObjects() {
        // Setup - Create various objects with different properties
        Anchors anchors = new Anchors();
        anchors.add(new Anchor(Positions.Name.BOTTOMRIGHT, new Position(1.0, 1.0)));
        
        StateRegion region1 = new StateRegion.Builder()
                .setSearchRegion(new Region(0, 0, 100, 100))
                .setOwnerStateName("RegionState")
                .setAnchors(anchors)
                .build();
                
        StateLocation location1 = new StateLocation.Builder()
                .setLocation(new Location(150, 150))
                .setOwnerStateName("LocationState")
                .build();
                
        Match preMatch1 = new Match.Builder()
                .setRegion(new Region(200, 200, 50, 50))
                .setSimScore(0.85)
                .build();
        Match preMatch2 = new Match.Builder()
                .setRegion(new Region(300, 300, 60, 60))
                .setSimScore(0.90)
                .build();
                
        ActionResult preResults = new ActionResult();
        preResults.add(preMatch1);
        preResults.add(preMatch2);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(region1)
                .withLocations(location1)
                .withMatches(preResults)
                .build();
        
        // Execute
        ActionResult result = converter.getOtherObjectsDirectlyAsMatchObjects(collection);
        
        // Verify
        assertEquals(4, result.size()); // 1 region + 1 location + 2 pre-matches
        
        // Verify the region was converted correctly
        Match regionMatch = result.getMatchList().stream()
                .filter(m -> m.x() == 0 && m.y() == 0)
                .findFirst()
                .orElse(null);
        assertNotNull(regionMatch);
        assertEquals(100, regionMatch.w());
        assertEquals(100, regionMatch.h());
        assertEquals(anchors, regionMatch.getAnchors());
        
        // Verify pre-existing matches are preserved
        assertTrue(result.getMatchList().contains(preMatch1));
        assertTrue(result.getMatchList().contains(preMatch2));
    }
}