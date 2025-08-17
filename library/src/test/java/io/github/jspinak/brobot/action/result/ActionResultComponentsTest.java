package io.github.jspinak.brobot.action.result;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Movement;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the refactored ActionResult component architecture.
 * Verifies that the delegation pattern works correctly and all components
 * integrate properly.
 */
class ActionResultComponentsTest {

    private ActionResult result;
    private Match testMatch1;
    private Match testMatch2;
    
    @BeforeEach
    void setUp() {
        result = new ActionResult();
        
        // Create test matches
        StateObjectMetadata metadata1 = new StateObjectMetadata();
        metadata1.setOwnerStateName("StateA");
        metadata1.setStateObjectName("Button1");
        
        StateObjectMetadata metadata2 = new StateObjectMetadata();
        metadata2.setOwnerStateName("StateB");
        metadata2.setStateObjectName("Button2");
        
        testMatch1 = new Match.Builder()
            .setRegion(10, 10, 50, 30)
            .setSimScore(0.85)
            .setName("match1")
            .setStateObjectData(metadata1)
            .build();
            
        testMatch2 = new Match.Builder()
            .setRegion(100, 100, 60, 40)
            .setSimScore(0.95)
            .setName("match2")
            .setStateObjectData(metadata2)
            .build();
    }
    
    @Test
    void testMatchCollectionDelegation() {
        // Add matches through facade
        result.add(testMatch1, testMatch2);
        
        // Verify through facade methods
        assertEquals(2, result.size());
        assertFalse(result.isEmpty());
        
        // Verify through component
        MatchCollection collection = result.getMatchCollection();
        assertNotNull(collection);
        assertEquals(2, collection.size());
        assertTrue(collection.contains(testMatch1));
        assertTrue(collection.contains(testMatch2));
        
        // Test sorting through facade
        result.sortMatchObjectsDescending();
        Optional<Match> best = result.getBestMatch();
        assertTrue(best.isPresent());
        assertEquals(testMatch2, best.get());
        
        // Test statistics
        MatchStatistics stats = collection.getStatistics();
        assertThat(stats.getAverageScore()).isCloseTo(0.9, within(0.01));
        assertThat(stats.getMaxScore()).isEqualTo(0.95);
        assertThat(stats.getMinScore()).isEqualTo(0.85);
    }
    
    @Test
    void testTimingDataDelegation() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusSeconds(5);
        
        // Set through facade
        result.setStartTime(startTime);
        result.setEndTime(endTime);
        
        // Verify through facade
        assertEquals(startTime, result.getStartTime());
        assertEquals(endTime, result.getEndTime());
        
        Duration duration = result.getDuration();
        assertThat(duration.getSeconds()).isEqualTo(5);
        
        // Verify through component
        TimingData timing = result.getTimingData();
        assertNotNull(timing);
        assertEquals(startTime, timing.getStartTime());
        assertEquals(endTime, timing.getEndTime());
        assertThat(timing.getExecutionTimeMs()).isCloseTo(5000L, within(100L));
    }
    
    @Test
    void testStateTrackerDelegation() {
        // Add matches with state info
        result.add(testMatch1, testMatch2);
        
        // Verify state tracking through facade
        Set<String> activeStates = result.getActiveStates();
        assertThat(activeStates).containsExactlyInAnyOrder("StateA", "StateB");
        
        // Verify through component
        StateTracker tracker = result.getStateTracker();
        assertNotNull(tracker);
        assertTrue(tracker.isStateActive("StateA"));
        assertTrue(tracker.isStateActive("StateB"));
        
        List<Match> stateAMatches = tracker.getMatchesForState("StateA");
        assertThat(stateAMatches).containsExactly(testMatch1);
    }
    
    @Test
    void testTextExtractionDelegation() {
        // Add text through facade
        result.addString("First text");
        result.addString("Second text");
        result.setSelectedText("Selected");
        
        // Verify through facade
        assertNotNull(result.getText());
        assertEquals("Selected", result.getSelectedText());
        
        // Verify through component
        TextExtractionResult textResult = result.getTextResult();
        assertNotNull(textResult);
        assertTrue(textResult.hasText());
        assertEquals("Selected", textResult.getSelectedText());
        assertThat(textResult.getCombinedText()).contains("First text", "Second text");
    }
    
    @Test
    void testRegionManagerDelegation() {
        Region region1 = new Region(0, 0, 100, 100);
        Region region2 = new Region(50, 50, 100, 100);
        
        // Add through facade
        result.addDefinedRegion(region1);
        result.addDefinedRegion(region2);
        
        // Verify through facade
        List<Region> regions = result.getDefinedRegions();
        assertThat(regions).hasSize(2);
        assertEquals(region1, result.getDefinedRegion());
        
        // Verify through component
        RegionManager manager = result.getRegionManager();
        assertNotNull(manager);
        assertEquals(2, manager.size());
        
        Optional<Region> union = manager.getUnion();
        assertTrue(union.isPresent());
        Region unionRegion = union.get();
        assertEquals(0, unionRegion.getX());
        assertEquals(0, unionRegion.getY());
        assertEquals(150, unionRegion.getW());
        assertEquals(150, unionRegion.getH());
    }
    
    @Test
    void testMovementTrackerDelegation() {
        Location start = new Location(10, 10);
        Location end = new Location(100, 100);
        Movement movement = new Movement(start, end);
        
        // Add through facade
        result.addMovement(movement);
        
        // Verify through facade
        List<Movement> movements = result.getMovements();
        assertThat(movements).hasSize(1);
        
        Optional<Movement> firstMovement = result.getMovement();
        assertTrue(firstMovement.isPresent());
        assertEquals(movement, firstMovement.get());
        
        // Verify through component
        MovementTracker tracker = result.getMovementTracker();
        assertNotNull(tracker);
        assertEquals(1, tracker.size());
        
        double distance = tracker.getTotalDistance();
        assertThat(distance).isGreaterThan(127.0); // sqrt((90)^2 + (90)^2)
    }
    
    @Test
    void testActionResultBuilder() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusSeconds(3);
        
        ActionResult built = new ActionResultBuilder()
            .withSuccess(true)
            .withDescription("Test action")
            .withMatch(testMatch1)
            .withMatch(testMatch2)
            .withTiming(startTime, endTime)
            .withActiveState("TestState")
            .withText("Extracted text")
            .withSelectedText("Selected")
            .withRegion(new Region(0, 0, 50, 50))
            .build();
            
        // Verify all components were properly initialized
        assertTrue(built.isSuccess());
        assertEquals("Test action", built.getActionDescription());
        assertEquals(2, built.size());
        assertTrue(built.getActiveStates().contains("TestState"));
        assertEquals("Selected", built.getSelectedText());
        assertThat(built.getDefinedRegions()).hasSize(1);
        assertThat(built.getDuration().getSeconds()).isEqualTo(3);
    }
    
    @Test
    void testBackwardCompatibility() {
        // Test that all legacy methods still work
        result.add(testMatch1);
        result.sortMatchObjects();
        result.sortBySizeDescending();
        
        List<Region> regions = result.getMatchRegions();
        List<Location> locations = result.getMatchLocations();
        
        assertThat(regions).hasSize(1);
        assertThat(locations).hasSize(1);
        
        // Test deprecated methods still work
        result.sortByMatchScoreDescending();
        assertEquals(1, result.size());
        
        // Test complex operations
        ActionResult other = new ActionResult();
        other.add(testMatch2);
        
        result.addMatchObjects(other);
        assertEquals(2, result.size());
        
        ActionResult difference = result.minus(other);
        assertEquals(1, difference.size());
        assertTrue(difference.containsMatch(testMatch1));
    }
    
    @Test
    void testComponentInteraction() {
        // Test that components interact correctly
        result.add(testMatch1, testMatch2);
        
        // Match should update state tracker
        StateTracker states = result.getStateTracker();
        assertThat(states.getActiveStates()).containsExactlyInAnyOrder("StateA", "StateB");
        
        // Statistics should reflect matches
        MatchStatistics stats = result.getMatchCollection().getStatistics();
        assertEquals(2, result.size());
        assertThat(stats.getConfidence()).isNotNull();
        
        // Timing should be independent but accessible
        result.setStartTime(LocalDateTime.now());
        result.setEndTime(LocalDateTime.now().plusSeconds(1));
        
        TimingData timing = result.getTimingData();
        assertTrue(timing.hasStarted());
        assertTrue(timing.hasCompleted());
    }
    
    private static org.assertj.core.data.Offset<Double> within(double offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
    
    private static org.assertj.core.data.Offset<Long> within(long offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
}