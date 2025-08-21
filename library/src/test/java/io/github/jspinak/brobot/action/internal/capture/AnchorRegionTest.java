package io.github.jspinak.brobot.action.internal.capture;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test suite for AnchorRegion class.
 * Tests region boundary adjustments based on anchor points from matched elements.
 */
@DisplayName("AnchorRegion Tests")
public class AnchorRegionTest extends BrobotTestBase {

    @InjectMocks
    private AnchorRegion anchorRegion;
    
    @Mock
    private DefinedBorders definedBorders;
    
    @Mock
    private Match match1;
    
    @Mock
    private Match match2;
    
    private AutoCloseable mockCloseable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        anchorRegion = new AnchorRegion();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }
    
    @Nested
    @DisplayName("Basic Anchor Processing")
    class BasicAnchorProcessing {
        
        @Test
        @DisplayName("Should process single anchor")
        void shouldProcessSingleAnchor() {
            // Arrange
            Region region = new Region(0, 0, 100, 100);
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.singletonList(match1));
            
            StateLocation anchor = mock(StateLocation.class);
            when(anchor.getPosition()).thenReturn(Positions.Name.TOPLEFT);
            when(match1.getAnchors()).thenReturn(Collections.singletonList(anchor));
            
            // Act
            anchorRegion.fitRegionToAnchors(definedBorders, region, actionResult);
            
            // Assert
            verify(definedBorders, atLeastOnce()).setBorders(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should process multiple anchors")
        void shouldProcessMultipleAnchors() {
            // Arrange
            Region region = new Region(0, 0, 100, 100);
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Arrays.asList(match1, match2));
            
            StateLocation anchor1 = mock(StateLocation.class);
            StateLocation anchor2 = mock(StateLocation.class);
            when(anchor1.getPosition()).thenReturn(Positions.Name.TOPLEFT);
            when(anchor2.getPosition()).thenReturn(Positions.Name.BOTTOMRIGHT);
            
            when(match1.getAnchors()).thenReturn(Collections.singletonList(anchor1));
            when(match2.getAnchors()).thenReturn(Collections.singletonList(anchor2));
            
            // Act
            anchorRegion.fitRegionToAnchors(definedBorders, region, actionResult);
            
            // Assert
            verify(definedBorders, atLeast(2)).setBorders(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should handle empty match list")
        void shouldHandleEmptyMatchList() {
            // Arrange
            Region region = new Region(0, 0, 100, 100);
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.emptyList());
            
            // Act
            anchorRegion.fitRegionToAnchors(definedBorders, region, actionResult);
            
            // Assert
            verify(definedBorders, never()).setBorders(any(), any(), any());
        }
    }
    
    @Nested
    @DisplayName("Anchor Positions")
    class AnchorPositions {
        
        @ParameterizedTest
        @EnumSource(Positions.Name.class)
        @DisplayName("Should handle all position types")
        void shouldHandleAllPositionTypes(Positions.Name position) {
            // Arrange
            Region region = new Region(0, 0, 100, 100);
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.singletonList(match1));
            
            StateLocation anchor = mock(StateLocation.class);
            when(anchor.getPosition()).thenReturn(position);
            when(match1.getAnchors()).thenReturn(Collections.singletonList(anchor));
            
            // Act
            anchorRegion.fitRegionToAnchors(definedBorders, region, actionResult);
            
            // Assert
            verify(definedBorders, atLeastOnce()).setBorders(eq(region), eq(anchor), any());
        }
        
        @Test
        @DisplayName("Should process corner anchors with two boundaries")
        void shouldProcessCornerAnchorsWithTwoBoundaries() {
            // Arrange
            Region region = new Region(0, 0, 100, 100);
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.singletonList(match1));
            
            StateLocation anchor = mock(StateLocation.class);
            when(anchor.getPosition()).thenReturn(Positions.Name.TOPLEFT);
            when(match1.getAnchors()).thenReturn(Collections.singletonList(anchor));
            
            Location anchorLocation = new Location(10, 10);
            when(anchor.getLocationInMatch(match1)).thenReturn(anchorLocation);
            
            // Act
            anchorRegion.fitRegionToAnchors(definedBorders, region, actionResult);
            
            // Assert
            verify(definedBorders).setBorders(region, anchor, anchorLocation);
        }
        
        @Test
        @DisplayName("Should process middle anchors with one boundary")
        void shouldProcessMiddleAnchorsWithOneBoundary() {
            // Arrange
            Region region = new Region(0, 0, 100, 100);
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.singletonList(match1));
            
            StateLocation anchor = mock(StateLocation.class);
            when(anchor.getPosition()).thenReturn(Positions.Name.TOPMIDDLE);
            when(match1.getAnchors()).thenReturn(Collections.singletonList(anchor));
            
            Location anchorLocation = new Location(50, 10);
            when(anchor.getLocationInMatch(match1)).thenReturn(anchorLocation);
            
            // Act
            anchorRegion.fitRegionToAnchors(definedBorders, region, actionResult);
            
            // Assert
            verify(definedBorders).setBorders(region, anchor, anchorLocation);
        }
    }
    
    @Nested
    @DisplayName("Region Adjustment")
    class RegionAdjustment {
        
        @Test
        @DisplayName("Should adjust region based on anchor locations")
        void shouldAdjustRegionBasedOnAnchorLocations() {
            // Arrange
            Region region = new Region(0, 0, 100, 100);
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.singletonList(match1));
            
            StateLocation anchor = mock(StateLocation.class);
            when(anchor.getPosition()).thenReturn(Positions.Name.TOPLEFT);
            when(match1.getAnchors()).thenReturn(Collections.singletonList(anchor));
            
            Location anchorLocation = new Location(20, 30);
            when(anchor.getLocationInMatch(match1)).thenReturn(anchorLocation);
            
            // Act
            anchorRegion.fitRegionToAnchors(definedBorders, region, actionResult);
            
            // Assert
            verify(definedBorders).setBorders(region, anchor, anchorLocation);
        }
        
        @Test
        @DisplayName("Should handle multiple anchors per match")
        void shouldHandleMultipleAnchorsPerMatch() {
            // Arrange
            Region region = new Region(0, 0, 100, 100);
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.singletonList(match1));
            
            StateLocation anchor1 = mock(StateLocation.class);
            StateLocation anchor2 = mock(StateLocation.class);
            when(anchor1.getPosition()).thenReturn(Positions.Name.TOPLEFT);
            when(anchor2.getPosition()).thenReturn(Positions.Name.BOTTOMRIGHT);
            
            when(match1.getAnchors()).thenReturn(Arrays.asList(anchor1, anchor2));
            
            Location anchorLocation1 = new Location(10, 10);
            Location anchorLocation2 = new Location(90, 90);
            when(anchor1.getLocationInMatch(match1)).thenReturn(anchorLocation1);
            when(anchor2.getLocationInMatch(match1)).thenReturn(anchorLocation2);
            
            // Act
            anchorRegion.fitRegionToAnchors(definedBorders, region, actionResult);
            
            // Assert
            verify(definedBorders).setBorders(region, anchor1, anchorLocation1);
            verify(definedBorders).setBorders(region, anchor2, anchorLocation2);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle null anchor list")
        void shouldHandleNullAnchorList() {
            // Arrange
            Region region = new Region(0, 0, 100, 100);
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.singletonList(match1));
            
            when(match1.getAnchors()).thenReturn(null);
            
            // Act
            anchorRegion.fitRegionToAnchors(definedBorders, region, actionResult);
            
            // Assert
            verify(definedBorders, never()).setBorders(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should handle null region")
        void shouldHandleNullRegion() {
            // Arrange
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.singletonList(match1));
            
            // Act & Assert
            assertDoesNotThrow(() -> 
                anchorRegion.fitRegionToAnchors(definedBorders, null, actionResult)
            );
        }
        
        @Test
        @DisplayName("Should handle null action result")
        void shouldHandleNullActionResult() {
            // Arrange
            Region region = new Region(0, 0, 100, 100);
            
            // Act & Assert
            assertDoesNotThrow(() -> 
                anchorRegion.fitRegionToAnchors(definedBorders, region, null)
            );
        }
        
        @Test
        @DisplayName("Should handle anchor with null location")
        void shouldHandleAnchorWithNullLocation() {
            // Arrange
            Region region = new Region(0, 0, 100, 100);
            ActionResult actionResult = new ActionResult();
            actionResult.setMatchList(Collections.singletonList(match1));
            
            StateLocation anchor = mock(StateLocation.class);
            when(anchor.getPosition()).thenReturn(Positions.Name.TOPLEFT);
            when(anchor.getLocationInMatch(match1)).thenReturn(null);
            when(match1.getAnchors()).thenReturn(Collections.singletonList(anchor));
            
            // Act
            anchorRegion.fitRegionToAnchors(definedBorders, region, actionResult);
            
            // Assert
            verify(definedBorders).setBorders(region, anchor, null);
        }
    }
}