package io.github.jspinak.brobot.action.internal.region;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.state.*;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test suite for DynamicRegionResolver.
 * Tests dynamic search region resolution based on cross-state references.
 */
@DisplayName("DynamicRegionResolver Tests")
public class DynamicRegionResolverTest extends BrobotTestBase {
    
    @Mock
    private StateStore mockStateStore;
    
    @Mock
    private SearchRegionDependencyRegistry mockDependencyRegistry;
    
    @Mock
    private ActionResult mockActionResult;
    
    private DynamicRegionResolver dynamicRegionResolver;
    private AutoCloseable mockCloseable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        dynamicRegionResolver = new DynamicRegionResolver(mockStateStore, mockDependencyRegistry);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }
    
    @Nested
    @DisplayName("StateImage Region Updates")
    class StateImageRegionUpdates {
        
        @Test
        @DisplayName("Should update search region for StateImage with cross-state reference")
        public void testUpdateStateImageWithCrossStateReference() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("target-image")
                .build();
            
            SearchRegionOnObject searchConfig = new SearchRegionOnObject.Builder()
                .setAnchorStateName("other-state")
                .setAnchorStateObjectName("anchor-object")
                .setOffsetX(10)
                .setOffsetY(20)
                .setWidth(100)
                .setHeight(50)
                .build();
            stateImage.setSearchRegionOnObject(searchConfig);
            
            // Mock match history
            ActionHistory history = mock(ActionHistory.class);
            when(history.getTimesFound()).thenReturn(1);
            stateImage.setMatchHistory(history);
            
            // Mock anchor match
            Match anchorMatch = mock(Match.class);
            when(anchorMatch.getRegion()).thenReturn(new Region(100, 100, 200, 150));
            
            List<Match> matches = Collections.singletonList(anchorMatch);
            when(mockActionResult.getMatchList()).thenReturn(matches);
            
            // Act
            dynamicRegionResolver.updateSearchRegions(stateImage, mockActionResult);
            
            // Assert
            assertNotNull(stateImage.getFixedSearchRegion());
            Region updatedRegion = stateImage.getFixedSearchRegion();
            assertEquals(110, updatedRegion.getX()); // 100 + 10 offset
            assertEquals(120, updatedRegion.getY()); // 100 + 20 offset
            assertEquals(100, updatedRegion.getW());
            assertEquals(50, updatedRegion.getH());
        }
        
        @Test
        @DisplayName("Should not update if StateImage already has defined region")
        public void testNoUpdateIfAlreadyHasRegion() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("image-with-region")
                .setSearchRegion(new Region(0, 0, 100, 100))
                .build();
            
            SearchRegionOnObject searchConfig = new SearchRegionOnObject.Builder()
                .setAnchorStateName("other-state")
                .build();
            stateImage.setSearchRegionOnObject(searchConfig);
            
            Region originalRegion = stateImage.getSearchRegion();
            
            // Act
            dynamicRegionResolver.updateSearchRegions(stateImage, mockActionResult);
            
            // Assert - Region should not be changed
            assertEquals(originalRegion, stateImage.getSearchRegion());
        }
        
        @Test
        @DisplayName("Should not update if no match history")
        public void testNoUpdateIfNoActionHistory() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("unfound-image")
                .build();
            
            SearchRegionOnObject searchConfig = new SearchRegionOnObject.Builder()
                .setAnchorStateName("other-state")
                .build();
            stateImage.setSearchRegionOnObject(searchConfig);
            
            ActionHistory history = mock(ActionHistory.class);
            when(history.getTimesFound()).thenReturn(0);
            stateImage.setMatchHistory(history);
            
            // Act
            dynamicRegionResolver.updateSearchRegions(stateImage, mockActionResult);
            
            // Assert
            assertNull(stateImage.getFixedSearchRegion());
        }
    }
    
    @Nested
    @DisplayName("StateLocation Region Updates")
    class StateLocationRegionUpdates {
        
        @Test
        @DisplayName("Should update location based on anchor match")
        public void testUpdateStateLocationFromAnchor() {
            // Arrange
            StateLocation stateLocation = new StateLocation.Builder()
                .setName("target-location")
                .setLocation(new Location(0, 0))
                .build();
            
            SearchRegionOnObject searchConfig = new SearchRegionOnObject.Builder()
                .setAnchorStateName("anchor-state")
                .setAnchorStateObjectName("anchor-object")
                .setOffsetX(50)
                .setOffsetY(50)
                .build();
            stateLocation.setSearchRegionOnObject(searchConfig);
            
            // Mock anchor match
            Match anchorMatch = mock(Match.class);
            when(anchorMatch.getRegion()).thenReturn(new Region(100, 100, 200, 150));
            when(anchorMatch.getStateObjectData())
                .thenReturn(new StateObjectMetadata("anchor-state", "anchor-object"));
            
            List<Match> matches = Collections.singletonList(anchorMatch);
            when(mockActionResult.getMatchList()).thenReturn(matches);
            
            // Act
            dynamicRegionResolver.updateSearchRegions(stateLocation, mockActionResult);
            
            // Assert - Location should be center of resolved region
            Location updatedLocation = stateLocation.getLocation();
            assertNotNull(updatedLocation);
            assertEquals(250, updatedLocation.getX()); // Center of region + offset
            assertEquals(225, updatedLocation.getY()); // Center of region + offset
        }
        
        @Test
        @DisplayName("Should not update location without search config")
        public void testNoUpdateWithoutSearchConfig() {
            // Arrange
            StateLocation stateLocation = new StateLocation.Builder()
                .setName("simple-location")
                .setLocation(new Location(50, 50))
                .build();
            
            Location originalLocation = stateLocation.getLocation();
            
            // Act
            dynamicRegionResolver.updateSearchRegions(stateLocation, mockActionResult);
            
            // Assert
            assertEquals(originalLocation, stateLocation.getLocation());
        }
    }
    
    @Nested
    @DisplayName("StateRegion Updates")
    class StateRegionUpdates {
        
        @Test
        @DisplayName("Should handle StateRegion objects")
        public void testHandleStateRegion() {
            // Arrange
            StateRegion stateRegion = new StateRegion.Builder()
                .setName("target-region")
                .setRegion(new Region(0, 0, 100, 100))
                .build();
            
            // Act & Assert - Should not throw
            assertDoesNotThrow(() -> 
                dynamicRegionResolver.updateSearchRegions(stateRegion, mockActionResult));
        }
    }
    
    @Nested
    @DisplayName("Cross-State Resolution")
    class CrossStateResolution {
        
        @Test
        @DisplayName("Should resolve region from different state")
        public void testResolveFromDifferentState() {
            // Arrange
            State sourceState = new State.Builder("source-state").build();
            StateImage anchorImage = new StateImage.Builder()
                .setName("anchor-image")
                .build();
            sourceState.getStateImages().add(anchorImage);
            
            State targetState = new State.Builder("target-state").build();
            StateImage targetImage = new StateImage.Builder()
                .setName("target-image")
                .build();
            
            SearchRegionOnObject searchConfig = new SearchRegionOnObject.Builder()
                .setAnchorStateName("source-state")
                .setAnchorStateObjectName("anchor-image")
                .setWidth(200)
                .setHeight(100)
                .build();
            targetImage.setSearchRegionOnObject(searchConfig);
            targetState.getStateImages().add(targetImage);
            
            when(mockStateStore.get("source-state")).thenReturn(Optional.of(sourceState));
            
            Match anchorMatch = mock(Match.class);
            when(anchorMatch.getRegion()).thenReturn(new Region(150, 150, 100, 100));
            when(anchorMatch.getStateObjectData())
                .thenReturn(new StateObjectMetadata("source-state", "anchor-image"));
            
            when(mockActionResult.getMatchList()).thenReturn(Collections.singletonList(anchorMatch));
            
            ActionHistory history = mock(ActionHistory.class);
            when(history.getTimesFound()).thenReturn(1);
            targetImage.setMatchHistory(history);
            
            // Act
            dynamicRegionResolver.updateSearchRegions(targetImage, mockActionResult);
            
            // Assert
            assertNotNull(targetImage.getFixedSearchRegion());
            Region region = targetImage.getFixedSearchRegion();
            assertEquals(150, region.getX());
            assertEquals(150, region.getY());
            assertEquals(200, region.getW());
            assertEquals(100, region.getH());
        }
        
        @Test
        @DisplayName("Should register dependencies")
        public void testRegisterDependencies() {
            // Arrange
            StateImage dependent = new StateImage.Builder()
                .setName("dependent")
                .build();
            
            SearchRegionOnObject config = new SearchRegionOnObject.Builder()
                .setAnchorStateName("anchor-state")
                .setAnchorStateObjectName("anchor-object")
                .build();
            dependent.setSearchRegionOnObject(config);
            
            // Act
            dynamicRegionResolver.registerDependency(dependent);
            
            // Assert
            verify(mockDependencyRegistry).registerDependency(
                eq("anchor-state"), 
                eq("anchor-object"),
                any()
            );
        }
    }
    
    @Nested
    @DisplayName("Region Calculation")
    class RegionCalculation {
        
        @Test
        @DisplayName("Should apply offsets correctly")
        public void testApplyOffsets() {
            // Arrange
            StateImage stateImage = new StateImage.Builder().build();
            
            SearchRegionOnObject config = new SearchRegionOnObject.Builder()
                .setOffsetX(25)
                .setOffsetY(-10)
                .setWidth(150)
                .setHeight(75)
                .build();
            stateImage.setSearchRegionOnObject(config);
            
            Match anchorMatch = mock(Match.class);
            when(anchorMatch.getRegion()).thenReturn(new Region(100, 200, 50, 50));
            when(mockActionResult.getMatchList()).thenReturn(Collections.singletonList(anchorMatch));
            
            ActionHistory history = mock(ActionHistory.class);
            when(history.getTimesFound()).thenReturn(1);
            stateImage.setMatchHistory(history);
            
            // Act
            dynamicRegionResolver.updateSearchRegions(stateImage, mockActionResult);
            
            // Assert
            Region region = stateImage.getFixedSearchRegion();
            assertNotNull(region);
            assertEquals(125, region.getX()); // 100 + 25
            assertEquals(190, region.getY()); // 200 - 10
            assertEquals(150, region.getW());
            assertEquals(75, region.getH());
        }
        
        @Test
        @DisplayName("Should use anchor dimensions when no size specified")
        public void testUseAnchorDimensionsWhenNoSize() {
            // Arrange
            StateImage stateImage = new StateImage.Builder().build();
            
            SearchRegionOnObject config = new SearchRegionOnObject.Builder()
                .setAnchorStateName("state")
                .setAnchorStateObjectName("anchor")
                // No width/height specified
                .build();
            stateImage.setSearchRegionOnObject(config);
            
            Match anchorMatch = mock(Match.class);
            when(anchorMatch.getRegion()).thenReturn(new Region(50, 50, 300, 200));
            when(mockActionResult.getMatchList()).thenReturn(Collections.singletonList(anchorMatch));
            
            ActionHistory history = mock(ActionHistory.class);
            when(history.getTimesFound()).thenReturn(1);
            stateImage.setMatchHistory(history);
            
            // Act
            dynamicRegionResolver.updateSearchRegions(stateImage, mockActionResult);
            
            // Assert
            Region region = stateImage.getFixedSearchRegion();
            assertNotNull(region);
            assertEquals(300, region.getW()); // Uses anchor width
            assertEquals(200, region.getH()); // Uses anchor height
        }
    }
    
    @Nested
    @DisplayName("Multiple Matches")
    class MultipleMatches {
        
        @Test
        @DisplayName("Should select best match when multiple available")
        public void testSelectBestMatch() {
            // Arrange
            StateImage stateImage = new StateImage.Builder().build();
            
            SearchRegionOnObject config = new SearchRegionOnObject.Builder()
                .setAnchorStateName("state")
                .setAnchorStateObjectName("anchor")
                .build();
            stateImage.setSearchRegionOnObject(config);
            
            // Create multiple matches for the anchor
            Match match1 = mock(Match.class);
            when(match1.getScore()).thenReturn(0.7);
            when(match1.getRegion()).thenReturn(new Region(100, 100, 50, 50));
            when(match1.getStateObjectData())
                .thenReturn(new StateObjectMetadata("state", "anchor"));
            
            Match match2 = mock(Match.class);
            when(match2.getScore()).thenReturn(0.9); // Higher score
            when(match2.getRegion()).thenReturn(new Region(200, 200, 60, 60));
            when(match2.getStateObjectData())
                .thenReturn(new StateObjectMetadata("state", "anchor"));
            
            Match match3 = mock(Match.class);
            when(match3.getScore()).thenReturn(0.8);
            when(match3.getRegion()).thenReturn(new Region(300, 300, 70, 70));
            when(match3.getStateObjectData())
                .thenReturn(new StateObjectMetadata("state", "anchor"));
            
            when(mockActionResult.getMatchList()).thenReturn(Arrays.asList(match1, match2, match3));
            
            ActionHistory history = mock(ActionHistory.class);
            when(history.getTimesFound()).thenReturn(1);
            stateImage.setMatchHistory(history);
            
            // Act
            dynamicRegionResolver.updateSearchRegions(stateImage, mockActionResult);
            
            // Assert - Should use match2 with highest score
            Region region = stateImage.getFixedSearchRegion();
            assertNotNull(region);
            assertEquals(200, region.getX());
            assertEquals(200, region.getY());
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle null action result")
        public void testHandleNullActionResult() {
            // Arrange
            StateImage stateImage = new StateImage.Builder().build();
            
            // Act & Assert
            assertDoesNotThrow(() -> 
                dynamicRegionResolver.updateSearchRegions(stateImage, null));
        }
        
        @Test
        @DisplayName("Should handle null state object")
        public void testHandleNullStateObject() {
            // Act & Assert
            assertDoesNotThrow(() -> 
                dynamicRegionResolver.updateSearchRegions(null, mockActionResult));
        }
        
        @Test
        @DisplayName("Should handle missing anchor state")
        public void testHandleMissingAnchorState() {
            // Arrange
            StateImage stateImage = new StateImage.Builder().build();
            
            SearchRegionOnObject config = new SearchRegionOnObject.Builder()
                .setAnchorStateName("non-existent-state")
                .setAnchorStateObjectName("anchor")
                .build();
            stateImage.setSearchRegionOnObject(config);
            
            when(mockStateStore.get("non-existent-state")).thenReturn(Optional.empty());
            
            ActionHistory history = mock(ActionHistory.class);
            when(history.getTimesFound()).thenReturn(1);
            stateImage.setMatchHistory(history);
            
            // Act
            dynamicRegionResolver.updateSearchRegions(stateImage, mockActionResult);
            
            // Assert - Should not update region
            assertNull(stateImage.getFixedSearchRegion());
        }
    }
}