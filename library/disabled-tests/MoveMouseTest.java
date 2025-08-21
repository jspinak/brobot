package io.github.jspinak.brobot.action.basic.mouse;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.internal.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for MoveMouse action - cursor positioning operation.
 * Tests mouse movement to locations, regions, and pattern-based targets.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MoveMouseTest extends BrobotTestBase {
    
    @Mock private Find find;
    @Mock private MoveMouseWrapper moveMouseWrapper;
    @Mock private TimeProvider timeProvider;
    
    @Mock private ActionConfig actionConfig;
    @Mock private ActionResult actionResult;
    @Mock private ObjectCollection objectCollection;
    @Mock private Location location;
    @Mock private StateLocation stateLocation;
    @Mock private StateRegion stateRegion;
    @Mock private StateImage stateImage;
    @Mock private Region region;
    @Mock private Match match;
    
    private MoveMouse moveMouse;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        moveMouse = new MoveMouse(find, moveMouseWrapper, timeProvider);
        
        // Setup default mock behaviors
        when(actionResult.getActionConfig()).thenReturn(actionConfig);
        when(actionResult.getMatchLocations()).thenReturn(new ArrayList<>());
        when(actionConfig.getPauseAfterEnd()).thenReturn(0.5);
        when(objectCollection.getStateLocations()).thenReturn(new ArrayList<>());
        when(objectCollection.getStateRegions()).thenReturn(new ArrayList<>());
        when(objectCollection.getStateImages()).thenReturn(new ArrayList<>());
    }
    
    @Nested
    @DisplayName("Constructor and Type Tests")
    class ConstructorAndTypeTests {
        
        @Test
        @DisplayName("Should create MoveMouse with dependencies")
        void shouldCreateMoveMouseWithDependencies() {
            assertNotNull(moveMouse);
            assertEquals(ActionInterface.Type.MOVE, moveMouse.getActionType());
        }
        
        @Test
        @DisplayName("Should return correct action type")
        void shouldReturnCorrectActionType() {
            assertEquals(ActionInterface.Type.MOVE, moveMouse.getActionType());
        }
    }
    
    @Nested
    @DisplayName("Direct Location Movement Tests")
    class DirectLocationMovementTests {
        
        @Test
        @DisplayName("Should move to single location directly")
        void shouldMoveToSingleLocationDirectly() {
            // Given
            when(stateLocation.getLocation()).thenReturn(location);
            when(objectCollection.getStateLocations()).thenReturn(List.of(stateLocation));
            when(location.toMatch()).thenReturn(match);
            List<Location> matchLocations = new ArrayList<>();
            when(actionResult.getMatchLocations()).thenReturn(matchLocations);
            
            // When
            moveMouse.perform(actionResult, objectCollection);
            
            // Then
            verify(moveMouseWrapper).move(location);
            verify(actionResult).add(match);
            assertEquals(1, matchLocations.size());
            assertTrue(matchLocations.contains(location));
            verify(find, never()).perform(any(), any());
        }
        
        @Test
        @DisplayName("Should move to multiple locations in sequence")
        void shouldMoveToMultipleLocationsInSequence() {
            // Given
            StateLocation stateLocation1 = mock(StateLocation.class);
            StateLocation stateLocation2 = mock(StateLocation.class);
            StateLocation stateLocation3 = mock(StateLocation.class);
            Location location1 = mock(Location.class);
            Location location2 = mock(Location.class);
            Location location3 = mock(Location.class);
            Match match1 = mock(Match.class);
            Match match2 = mock(Match.class);
            Match match3 = mock(Match.class);
            
            when(stateLocation1.getLocation()).thenReturn(location1);
            when(stateLocation2.getLocation()).thenReturn(location2);
            when(stateLocation3.getLocation()).thenReturn(location3);
            when(location1.toMatch()).thenReturn(match1);
            when(location2.toMatch()).thenReturn(match2);
            when(location3.toMatch()).thenReturn(match3);
            
            when(objectCollection.getStateLocations())
                .thenReturn(List.of(stateLocation1, stateLocation2, stateLocation3));
            when(actionResult.getMatchLocations()).thenReturn(new ArrayList<>());
            
            // When
            moveMouse.perform(actionResult, objectCollection);
            
            // Then
            verify(moveMouseWrapper).move(location1);
            verify(moveMouseWrapper).move(location2);
            verify(moveMouseWrapper).move(location3);
            verify(actionResult).add(match1);
            verify(actionResult).add(match2);
            verify(actionResult).add(match3);
        }
    }
    
    @Nested
    @DisplayName("Region Movement Tests")
    class RegionMovementTests {
        
        @Test
        @DisplayName("Should move to center of region")
        void shouldMoveToCenterOfRegion() {
            // Given
            when(stateRegion.getSearchRegion()).thenReturn(region);
            when(region.getLocation()).thenReturn(location);
            when(location.toMatch()).thenReturn(match);
            when(objectCollection.getStateRegions()).thenReturn(List.of(stateRegion));
            when(objectCollection.getStateLocations()).thenReturn(new ArrayList<>());
            List<Location> matchLocations = new ArrayList<>();
            when(actionResult.getMatchLocations()).thenReturn(matchLocations);
            
            // When
            moveMouse.perform(actionResult, objectCollection);
            
            // Then
            verify(moveMouseWrapper).move(location);
            verify(actionResult).add(match);
            assertEquals(1, matchLocations.size());
            verify(find, never()).perform(any(), any());
        }
        
        @Test
        @DisplayName("Should move to multiple regions in sequence")
        void shouldMoveToMultipleRegionsInSequence() {
            // Given
            StateRegion stateRegion1 = mock(StateRegion.class);
            StateRegion stateRegion2 = mock(StateRegion.class);
            Region region1 = mock(Region.class);
            Region region2 = mock(Region.class);
            Location location1 = mock(Location.class);
            Location location2 = mock(Location.class);
            Match match1 = mock(Match.class);
            Match match2 = mock(Match.class);
            
            when(stateRegion1.getSearchRegion()).thenReturn(region1);
            when(stateRegion2.getSearchRegion()).thenReturn(region2);
            when(region1.getLocation()).thenReturn(location1);
            when(region2.getLocation()).thenReturn(location2);
            when(location1.toMatch()).thenReturn(match1);
            when(location2.toMatch()).thenReturn(match2);
            
            when(objectCollection.getStateRegions())
                .thenReturn(List.of(stateRegion1, stateRegion2));
            when(objectCollection.getStateLocations()).thenReturn(new ArrayList<>());
            when(actionResult.getMatchLocations()).thenReturn(new ArrayList<>());
            
            // When
            moveMouse.perform(actionResult, objectCollection);
            
            // Then
            verify(moveMouseWrapper).move(location1);
            verify(moveMouseWrapper).move(location2);
            verify(actionResult).add(match1);
            verify(actionResult).add(match2);
        }
    }
    
    @Nested
    @DisplayName("Find-based Movement Tests")
    class FindBasedMovementTests {
        
        @Test
        @DisplayName("Should use Find when no direct locations or regions")
        void shouldUseFindWhenNoDirectLocationsOrRegions() {
            // Given
            when(objectCollection.getStateLocations()).thenReturn(new ArrayList<>());
            when(objectCollection.getStateRegions()).thenReturn(new ArrayList<>());
            when(objectCollection.getStateImages()).thenReturn(List.of(stateImage));
            
            Location foundLocation = mock(Location.class);
            List<Location> matchLocations = List.of(foundLocation);
            when(actionResult.getMatchLocations()).thenReturn(matchLocations);
            
            // When
            moveMouse.perform(actionResult, objectCollection);
            
            // Then
            verify(find).perform(actionResult, objectCollection);
            verify(moveMouseWrapper).move(foundLocation);
        }
        
        @Test
        @DisplayName("Should move to all locations found by Find")
        void shouldMoveToAllLocationsFoundByFind() {
            // Given
            when(objectCollection.getStateLocations()).thenReturn(new ArrayList<>());
            when(objectCollection.getStateRegions()).thenReturn(new ArrayList<>());
            
            Location location1 = mock(Location.class);
            Location location2 = mock(Location.class);
            Location location3 = mock(Location.class);
            List<Location> matchLocations = List.of(location1, location2, location3);
            when(actionResult.getMatchLocations()).thenReturn(matchLocations);
            
            // When
            moveMouse.perform(actionResult, objectCollection);
            
            // Then
            verify(find).perform(actionResult, objectCollection);
            verify(moveMouseWrapper).move(location1);
            verify(moveMouseWrapper).move(location2);
            verify(moveMouseWrapper).move(location3);
        }
    }
    
    @Nested
    @DisplayName("Multiple ObjectCollection Tests")
    class MultipleObjectCollectionTests {
        
        @Test
        @DisplayName("Should process multiple collections in sequence")
        void shouldProcessMultipleCollectionsInSequence() {
            // Given
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            
            StateLocation stateLocation1 = mock(StateLocation.class);
            StateLocation stateLocation2 = mock(StateLocation.class);
            Location location1 = mock(Location.class);
            Location location2 = mock(Location.class);
            Match match1 = mock(Match.class);
            Match match2 = mock(Match.class);
            
            when(stateLocation1.getLocation()).thenReturn(location1);
            when(stateLocation2.getLocation()).thenReturn(location2);
            when(location1.toMatch()).thenReturn(match1);
            when(location2.toMatch()).thenReturn(match2);
            
            when(collection1.getStateLocations()).thenReturn(List.of(stateLocation1));
            when(collection1.getStateRegions()).thenReturn(new ArrayList<>());
            when(collection2.getStateLocations()).thenReturn(List.of(stateLocation2));
            when(collection2.getStateRegions()).thenReturn(new ArrayList<>());
            
            when(actionResult.getMatchLocations()).thenReturn(new ArrayList<>());
            when(actionConfig.getPauseAfterEnd()).thenReturn(1.0);
            
            // When
            moveMouse.perform(actionResult, collection1, collection2);
            
            // Then
            verify(moveMouseWrapper).move(location1);
            verify(moveMouseWrapper).move(location2);
            verify(timeProvider).wait(1.0); // Pause between collections
        }
        
        @Test
        @DisplayName("Should not pause after last collection")
        void shouldNotPauseAfterLastCollection() {
            // Given
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            
            when(collection1.getStateLocations()).thenReturn(new ArrayList<>());
            when(collection1.getStateRegions()).thenReturn(new ArrayList<>());
            when(collection2.getStateLocations()).thenReturn(new ArrayList<>());
            when(collection2.getStateRegions()).thenReturn(new ArrayList<>());
            
            when(actionConfig.getPauseAfterEnd()).thenReturn(2.0);
            
            // When
            moveMouse.perform(actionResult, collection1, collection2);
            
            // Then
            verify(timeProvider, times(1)).wait(2.0); // Only one pause between two collections
        }
    }
    
    @Nested
    @DisplayName("Priority Order Tests")
    class PriorityOrderTests {
        
        @Test
        @DisplayName("Should prioritize locations over regions")
        void shouldPrioritizeLocationsOverRegions() {
            // Given
            when(stateLocation.getLocation()).thenReturn(location);
            when(location.toMatch()).thenReturn(match);
            when(objectCollection.getStateLocations()).thenReturn(List.of(stateLocation));
            when(objectCollection.getStateRegions()).thenReturn(List.of(stateRegion));
            when(actionResult.getMatchLocations()).thenReturn(new ArrayList<>());
            
            // When
            moveMouse.perform(actionResult, objectCollection);
            
            // Then
            verify(moveMouseWrapper).move(location);
            verify(stateRegion, never()).getSearchRegion();
            verify(find, never()).perform(any(), any());
        }
        
        @Test
        @DisplayName("Should prioritize regions over Find")
        void shouldPrioritizeRegionsOverFind() {
            // Given
            when(objectCollection.getStateLocations()).thenReturn(new ArrayList<>());
            when(stateRegion.getSearchRegion()).thenReturn(region);
            when(region.getLocation()).thenReturn(location);
            when(location.toMatch()).thenReturn(match);
            when(objectCollection.getStateRegions()).thenReturn(List.of(stateRegion));
            when(objectCollection.getStateImages()).thenReturn(List.of(stateImage));
            when(actionResult.getMatchLocations()).thenReturn(new ArrayList<>());
            
            // When
            moveMouse.perform(actionResult, objectCollection);
            
            // Then
            verify(moveMouseWrapper).move(location);
            verify(find, never()).perform(any(), any());
        }
    }
    
    @Nested
    @DisplayName("Pause Timing Tests")
    class PauseTimingTests {
        
        @Test
        @DisplayName("Should apply pause duration from config")
        void shouldApplyPauseDurationFromConfig() {
            // Given
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            
            when(collection1.getStateLocations()).thenReturn(new ArrayList<>());
            when(collection1.getStateRegions()).thenReturn(new ArrayList<>());
            when(collection2.getStateLocations()).thenReturn(new ArrayList<>());
            when(collection2.getStateRegions()).thenReturn(new ArrayList<>());
            
            when(actionConfig.getPauseAfterEnd()).thenReturn(0.75);
            
            // When
            moveMouse.perform(actionResult, collection1, collection2);
            
            // Then
            verify(timeProvider).wait(0.75);
        }
        
        @Test
        @DisplayName("Should not pause when duration is zero")
        void shouldNotPauseWhenDurationIsZero() {
            // Given
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            
            when(collection1.getStateLocations()).thenReturn(new ArrayList<>());
            when(collection1.getStateRegions()).thenReturn(new ArrayList<>());
            when(collection2.getStateLocations()).thenReturn(new ArrayList<>());
            when(collection2.getStateRegions()).thenReturn(new ArrayList<>());
            
            when(actionConfig.getPauseAfterEnd()).thenReturn(0.0);
            
            // When
            moveMouse.perform(actionResult, collection1, collection2);
            
            // Then
            verify(timeProvider, never()).wait(anyDouble());
        }
        
        @Test
        @DisplayName("Should not pause when duration is negative")
        void shouldNotPauseWhenDurationIsNegative() {
            // Given
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            
            when(collection1.getStateLocations()).thenReturn(new ArrayList<>());
            when(collection1.getStateRegions()).thenReturn(new ArrayList<>());
            when(collection2.getStateLocations()).thenReturn(new ArrayList<>());
            when(collection2.getStateRegions()).thenReturn(new ArrayList<>());
            
            when(actionConfig.getPauseAfterEnd()).thenReturn(-1.0);
            
            // When
            moveMouse.perform(actionResult, collection1, collection2);
            
            // Then
            verify(timeProvider, never()).wait(anyDouble());
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle null state locations list")
        void shouldHandleNullStateLocationsList() {
            // Given
            when(objectCollection.getStateLocations()).thenReturn(null);
            when(objectCollection.getStateRegions()).thenReturn(new ArrayList<>());
            
            // When
            moveMouse.perform(actionResult, objectCollection);
            
            // Then
            verify(find).perform(actionResult, objectCollection);
            verify(moveMouseWrapper, never()).move(any(Location.class));
        }
        
        @Test
        @DisplayName("Should handle null state regions list")
        void shouldHandleNullStateRegionsList() {
            // Given
            when(objectCollection.getStateLocations()).thenReturn(new ArrayList<>());
            when(objectCollection.getStateRegions()).thenReturn(null);
            
            // When
            moveMouse.perform(actionResult, objectCollection);
            
            // Then
            verify(find).perform(actionResult, objectCollection);
        }
        
        @Test
        @DisplayName("Should propagate exception from move wrapper")
        void shouldPropagateExceptionFromMoveWrapper() {
            // Given
            when(stateLocation.getLocation()).thenReturn(location);
            when(objectCollection.getStateLocations()).thenReturn(List.of(stateLocation));
            doThrow(new RuntimeException("Move failed"))
                .when(moveMouseWrapper).move(any());
            
            // When/Then
            assertThrows(RuntimeException.class,
                () -> moveMouse.perform(actionResult, objectCollection));
        }
    }
    
    @Nested
    @DisplayName("Empty Collection Tests")
    class EmptyCollectionTests {
        
        @Test
        @DisplayName("Should handle all empty collections")
        void shouldHandleAllEmptyCollections() {
            // Given
            when(objectCollection.getStateLocations()).thenReturn(new ArrayList<>());
            when(objectCollection.getStateRegions()).thenReturn(new ArrayList<>());
            when(actionResult.getMatchLocations()).thenReturn(new ArrayList<>());
            
            // When
            moveMouse.perform(actionResult, objectCollection);
            
            // Then
            verify(find).perform(actionResult, objectCollection);
        }
        
        @Test
        @DisplayName("Should handle empty object collections array")
        void shouldHandleEmptyObjectCollectionsArray() {
            // When
            moveMouse.perform(actionResult);
            
            // Then
            verify(moveMouseWrapper, never()).move(any());
            verify(find, never()).perform(any(), any());
        }
    }
}