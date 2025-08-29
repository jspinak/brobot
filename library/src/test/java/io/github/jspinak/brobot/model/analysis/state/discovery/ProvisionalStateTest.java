package io.github.jspinak.brobot.model.analysis.state.discovery;

import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ProvisionalState - temporary container for building states.
 * Tests image management, scene tracking, and nesting detection.
 */
@DisplayName("ProvisionalState Tests")
public class ProvisionalStateTest extends BrobotTestBase {
    
    private ProvisionalState provisionalState;
    
    @Mock
    private StateImage mockImage1;
    
    @Mock
    private StateImage mockImage2;
    
    @Mock
    private StateImage mockImage3;
    
    @Mock
    private Region mockRegion1;
    
    @Mock
    private Region mockRegion2;
    
    @Mock
    private Region mockRegion3;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        provisionalState = new ProvisionalState("TestState");
    }
    
    @Nested
    @DisplayName("State Creation")
    class StateCreation {
        
        @Test
        @DisplayName("Should create provisional state with name")
        void shouldCreateProvisionalState() {
            assertNotNull(provisionalState);
            assertEquals("TestState", provisionalState.getName());
        }
        
        @Test
        @DisplayName("Should initialize with empty images")
        void shouldInitializeWithEmptyImages() {
            assertTrue(provisionalState.getImages().isEmpty());
        }
        
        @Test
        @DisplayName("Should initialize with empty scenes")
        void shouldInitializeWithEmptyScenes() {
            assertTrue(provisionalState.getScenes().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Image Management")
    class ImageManagement {
        
        @Test
        @DisplayName("Should add image when not nested")
        void shouldAddNonNestedImage() {
            when(mockImage1.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion1);
            when(mockRegion1.contains(any(Region.class))).thenReturn(false);
            
            provisionalState.addImage(mockImage1);
            
            assertEquals(1, provisionalState.getImages().size());
            assertTrue(provisionalState.getImages().contains(mockImage1));
        }
        
        @Test
        @DisplayName("Should add multiple non-nested images")
        void shouldAddMultipleNonNestedImages() {
            when(mockImage1.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion1);
            when(mockImage2.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion2);
            when(mockRegion1.contains(mockRegion2)).thenReturn(false);
            when(mockRegion2.contains(mockRegion1)).thenReturn(false);
            
            provisionalState.addImage(mockImage1);
            provisionalState.addImage(mockImage2);
            
            assertEquals(2, provisionalState.getImages().size());
            assertTrue(provisionalState.getImages().contains(mockImage1));
            assertTrue(provisionalState.getImages().contains(mockImage2));
        }
        
        @Test
        @DisplayName("Should not add nested image")
        void shouldNotAddNestedImage() {
            // Setup first image
            when(mockImage1.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion1);
            
            // Setup second image that is nested within first
            when(mockImage2.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion2);
            when(mockRegion1.contains(mockRegion2)).thenReturn(true); // Image2 is nested in Image1
            
            provisionalState.addImage(mockImage1);
            provisionalState.addImage(mockImage2); // Should not be added
            
            assertEquals(1, provisionalState.getImages().size());
            assertTrue(provisionalState.getImages().contains(mockImage1));
            assertFalse(provisionalState.getImages().contains(mockImage2));
        }
        
        @Test
        @DisplayName("Should handle complex nesting scenarios")
        void shouldHandleComplexNesting() {
            // Setup three regions with specific nesting relationships
            when(mockImage1.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion1);
            when(mockImage2.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion2);
            when(mockImage3.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion3);
            
            // Image1 doesn't contain Image2
            when(mockRegion1.contains(mockRegion2)).thenReturn(false);
            // Image2 doesn't contain Image1
            when(mockRegion2.contains(mockRegion1)).thenReturn(false);
            // Image1 contains Image3 (nested)
            when(mockRegion1.contains(mockRegion3)).thenReturn(true);
            // Image2 doesn't contain Image3
            when(mockRegion2.contains(mockRegion3)).thenReturn(false);
            
            provisionalState.addImage(mockImage1);
            provisionalState.addImage(mockImage2);
            provisionalState.addImage(mockImage3); // Should not be added (nested in Image1)
            
            assertEquals(2, provisionalState.getImages().size());
            assertTrue(provisionalState.getImages().contains(mockImage1));
            assertTrue(provisionalState.getImages().contains(mockImage2));
            assertFalse(provisionalState.getImages().contains(mockImage3));
        }
    }
    
    @Nested
    @DisplayName("Scene Management")
    class SceneManagement {
        
        @Test
        @DisplayName("Should check if scene is contained")
        void shouldCheckSceneContainment() {
            provisionalState.getScenes().add(1);
            provisionalState.getScenes().add(2);
            
            assertTrue(provisionalState.contains(1));
            assertTrue(provisionalState.contains(2));
            assertFalse(provisionalState.contains(3));
        }
        
        @Test
        @DisplayName("Should check equal scene sets")
        void shouldCheckEqualSceneSets() {
            provisionalState.getScenes().add(1);
            provisionalState.getScenes().add(2);
            provisionalState.getScenes().add(3);
            
            Set<Integer> matchingSet = new HashSet<>();
            matchingSet.add(1);
            matchingSet.add(2);
            matchingSet.add(3);
            
            Set<Integer> differentSet = new HashSet<>();
            differentSet.add(1);
            differentSet.add(2);
            differentSet.add(4);
            
            assertTrue(provisionalState.hasEqualSceneSets(matchingSet));
            assertFalse(provisionalState.hasEqualSceneSets(differentSet));
        }
        
        @Test
        @DisplayName("Should handle empty scene sets")
        void shouldHandleEmptySceneSets() {
            Set<Integer> emptySet = new HashSet<>();
            
            assertTrue(provisionalState.hasEqualSceneSets(emptySet));
            assertFalse(provisionalState.contains(1));
        }
        
        @Test
        @DisplayName("Should add scenes to provisional state")
        void shouldAddScenes() {
            provisionalState.getScenes().add(1);
            provisionalState.getScenes().add(5);
            provisionalState.getScenes().add(10);
            
            assertEquals(3, provisionalState.getScenes().size());
            assertTrue(provisionalState.contains(1));
            assertTrue(provisionalState.contains(5));
            assertTrue(provisionalState.contains(10));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle duplicate scene additions")
        void shouldHandleDuplicateScenes() {
            provisionalState.getScenes().add(1);
            provisionalState.getScenes().add(1); // Duplicate
            provisionalState.getScenes().add(1); // Another duplicate
            
            assertEquals(1, provisionalState.getScenes().size());
            assertTrue(provisionalState.contains(1));
        }
        
        @Test
        @DisplayName("Should maintain image order")
        void shouldMaintainImageOrder() {
            when(mockImage1.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion1);
            when(mockImage2.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion2);
            when(mockImage3.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion3);
            
            when(mockRegion1.contains(any(Region.class))).thenReturn(false);
            when(mockRegion2.contains(any(Region.class))).thenReturn(false);
            when(mockRegion3.contains(any(Region.class))).thenReturn(false);
            
            provisionalState.addImage(mockImage1);
            provisionalState.addImage(mockImage2);
            provisionalState.addImage(mockImage3);
            
            assertEquals(3, provisionalState.getImages().size());
            assertEquals(mockImage1, provisionalState.getImages().get(0));
            assertEquals(mockImage2, provisionalState.getImages().get(1));
            assertEquals(mockImage3, provisionalState.getImages().get(2));
        }
        
        @Test
        @DisplayName("Should handle null name in constructor")
        void shouldHandleNullName() {
            ProvisionalState stateWithNullName = new ProvisionalState(null);
            assertNull(stateWithNullName.getName());
            assertNotNull(stateWithNullName.getImages());
            assertNotNull(stateWithNullName.getScenes());
        }
    }
    
    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {
        
        @Test
        @DisplayName("Should build complete provisional state")
        void shouldBuildCompleteProvisionalState() {
            // Add images
            when(mockImage1.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion1);
            when(mockImage2.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion2);
            when(mockRegion1.contains(mockRegion2)).thenReturn(false);
            when(mockRegion2.contains(mockRegion1)).thenReturn(false);
            
            provisionalState.addImage(mockImage1);
            provisionalState.addImage(mockImage2);
            
            // Add scenes
            provisionalState.getScenes().add(1);
            provisionalState.getScenes().add(2);
            provisionalState.getScenes().add(3);
            
            // Verify complete state
            assertEquals("TestState", provisionalState.getName());
            assertEquals(2, provisionalState.getImages().size());
            assertEquals(3, provisionalState.getScenes().size());
            assertTrue(provisionalState.contains(1));
            assertTrue(provisionalState.contains(2));
            assertTrue(provisionalState.contains(3));
        }
        
        @Test
        @DisplayName("Should handle state with only scenes")
        void shouldHandleStateWithOnlyScenes() {
            provisionalState.getScenes().add(1);
            provisionalState.getScenes().add(2);
            
            assertEquals("TestState", provisionalState.getName());
            assertTrue(provisionalState.getImages().isEmpty());
            assertEquals(2, provisionalState.getScenes().size());
        }
        
        @Test
        @DisplayName("Should handle state with only images")
        void shouldHandleStateWithOnlyImages() {
            when(mockImage1.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion1);
            provisionalState.addImage(mockImage1);
            
            assertEquals("TestState", provisionalState.getName());
            assertEquals(1, provisionalState.getImages().size());
            assertTrue(provisionalState.getScenes().isEmpty());
        }
    }
}