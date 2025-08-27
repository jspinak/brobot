package io.github.jspinak.brobot.model.analysis.state.discovery;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ProvisionalState discovery.
 */
@DisplayName("ProvisionalState Tests")
public class ProvisionalStateTest extends BrobotTestBase {
    
    private ProvisionalState provisionalState;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        provisionalState = new ProvisionalState("TestState");
    }
    
    @Nested
    @DisplayName("State Creation and Basic Properties")
    class StateCreation {
        
        @Test
        @DisplayName("Should create provisional state with name")
        void shouldCreateProvisionalStateWithName() {
            assertNotNull(provisionalState);
            assertEquals("TestState", provisionalState.getName());
        }
        
        @Test
        @DisplayName("Should initialize with empty collections")
        void shouldInitializeWithEmptyCollections() {
            assertTrue(provisionalState.getScenes().isEmpty());
            assertTrue(provisionalState.getImages().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle different state names")
        void shouldHandleDifferentStateNames() {
            ProvisionalState loginState = new ProvisionalState("LoginState");
            ProvisionalState dashboardState = new ProvisionalState("DashboardState");
            
            assertEquals("LoginState", loginState.getName());
            assertEquals("DashboardState", dashboardState.getName());
            assertNotEquals(loginState.getName(), dashboardState.getName());
        }
    }
    
    @Nested
    @DisplayName("Scene Management")
    class SceneManagement {
        
        @Test
        @DisplayName("Should add scenes to provisional state")
        void shouldAddScenes() {
            provisionalState.getScenes().add(1);
            provisionalState.getScenes().add(2);
            provisionalState.getScenes().add(3);
            
            assertEquals(3, provisionalState.getScenes().size());
            assertTrue(provisionalState.contains(1));
            assertTrue(provisionalState.contains(2));
            assertTrue(provisionalState.contains(3));
            assertFalse(provisionalState.contains(4));
        }
        
        @Test
        @DisplayName("Should prevent duplicate scenes")
        void shouldPreventDuplicateScenes() {
            provisionalState.getScenes().add(1);
            provisionalState.getScenes().add(1);
            provisionalState.getScenes().add(1);
            
            assertEquals(1, provisionalState.getScenes().size());
        }
        
        @Test
        @DisplayName("Should check for equal scene sets")
        void shouldCheckEqualSceneSets() {
            Set<Integer> scenes = new HashSet<>();
            scenes.add(1);
            scenes.add(2);
            scenes.add(3);
            
            provisionalState.getScenes().addAll(scenes);
            
            assertTrue(provisionalState.hasEqualSceneSets(scenes));
            
            Set<Integer> differentScenes = new HashSet<>();
            differentScenes.add(1);
            differentScenes.add(2);
            assertFalse(provisionalState.hasEqualSceneSets(differentScenes));
        }
        
        @Test
        @DisplayName("Should handle empty scene sets")
        void shouldHandleEmptySceneSets() {
            assertTrue(provisionalState.hasEqualSceneSets(new HashSet<>()));
            assertFalse(provisionalState.contains(0));
        }
    }
    
    @Nested
    @DisplayName("Image Management")
    class ImageManagement {
        
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
        void setupMocks() {
            MockitoAnnotations.openMocks(this);
            
            when(mockImage1.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion1);
            when(mockImage2.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion2);
            when(mockImage3.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(mockRegion3);
        }
        
        @Test
        @DisplayName("Should add non-nested images")
        void shouldAddNonNestedImages() {
            // No regions contain each other
            when(mockRegion1.contains(mockRegion2)).thenReturn(false);
            when(mockRegion2.contains(mockRegion1)).thenReturn(false);
            
            provisionalState.addImage(mockImage1);
            provisionalState.addImage(mockImage2);
            
            assertEquals(2, provisionalState.getImages().size());
            assertTrue(provisionalState.getImages().contains(mockImage1));
            assertTrue(provisionalState.getImages().contains(mockImage2));
        }
        
        @Test
        @DisplayName("Should reject nested images")
        void shouldRejectNestedImages() {
            // Region1 contains Region2
            when(mockRegion1.contains(mockRegion2)).thenReturn(true);
            when(mockRegion2.contains(mockRegion1)).thenReturn(false);
            
            provisionalState.addImage(mockImage1);
            provisionalState.addImage(mockImage2); // This should be rejected
            
            assertEquals(1, provisionalState.getImages().size());
            assertTrue(provisionalState.getImages().contains(mockImage1));
            assertFalse(provisionalState.getImages().contains(mockImage2));
        }
        
        @Test
        @DisplayName("Should handle overlapping but not nested images")
        void shouldHandleOverlappingImages() {
            // Regions overlap but don't contain each other
            when(mockRegion1.contains(mockRegion2)).thenReturn(false);
            when(mockRegion2.contains(mockRegion1)).thenReturn(false);
            when(mockRegion1.contains(mockRegion3)).thenReturn(false);
            when(mockRegion3.contains(mockRegion1)).thenReturn(false);
            when(mockRegion2.contains(mockRegion3)).thenReturn(false);
            when(mockRegion3.contains(mockRegion2)).thenReturn(false);
            
            provisionalState.addImage(mockImage1);
            provisionalState.addImage(mockImage2);
            provisionalState.addImage(mockImage3);
            
            assertEquals(3, provisionalState.getImages().size());
        }
        
        @Test
        @DisplayName("Should maintain image order")
        void shouldMaintainImageOrder() {
            when(mockRegion1.contains(any(Region.class))).thenReturn(false);
            when(mockRegion2.contains(any(Region.class))).thenReturn(false);
            when(mockRegion3.contains(any(Region.class))).thenReturn(false);
            
            provisionalState.addImage(mockImage1);
            provisionalState.addImage(mockImage2);
            provisionalState.addImage(mockImage3);
            
            assertEquals(mockImage1, provisionalState.getImages().get(0));
            assertEquals(mockImage2, provisionalState.getImages().get(1));
            assertEquals(mockImage3, provisionalState.getImages().get(2));
        }
        
        @Test
        @DisplayName("Should handle chain of nested images")
        void shouldHandleChainOfNestedImages() {
            // Region1 contains Region2 contains Region3
            when(mockRegion1.contains(mockRegion2)).thenReturn(true);
            when(mockRegion1.contains(mockRegion3)).thenReturn(true);
            when(mockRegion2.contains(mockRegion3)).thenReturn(true);
            when(mockRegion2.contains(mockRegion1)).thenReturn(false);
            when(mockRegion3.contains(any(Region.class))).thenReturn(false);
            
            provisionalState.addImage(mockImage1);
            provisionalState.addImage(mockImage2); // Rejected - nested in 1
            provisionalState.addImage(mockImage3); // Rejected - nested in 1 (and 2)
            
            assertEquals(1, provisionalState.getImages().size());
            assertTrue(provisionalState.getImages().contains(mockImage1));
        }
        
        @Test
        @DisplayName("Should add image when previous is nested in new one")
        void shouldAddImageWhenPreviousIsNestedInNew() {
            // Region2 contains Region1 (opposite of usual case)
            when(mockRegion1.contains(mockRegion2)).thenReturn(false);
            when(mockRegion2.contains(mockRegion1)).thenReturn(true);
            
            provisionalState.addImage(mockImage1);
            provisionalState.addImage(mockImage2); // Should be added even though it contains image1
            
            assertEquals(2, provisionalState.getImages().size());
        }
    }
    
    @Nested
    @DisplayName("Complex State Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("Should handle state with multiple scenes and images")
        void shouldHandleComplexState() {
            // Add scenes
            provisionalState.getScenes().add(1);
            provisionalState.getScenes().add(2);
            provisionalState.getScenes().add(3);
            
            // Create mock images
            StateImage image1 = mock(StateImage.class);
            StateImage image2 = mock(StateImage.class);
            Region region1 = mock(Region.class);
            Region region2 = mock(Region.class);
            
            when(image1.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(region1);
            when(image2.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(region2);
            when(region1.contains(region2)).thenReturn(false);
            when(region2.contains(region1)).thenReturn(false);
            
            provisionalState.addImage(image1);
            provisionalState.addImage(image2);
            
            assertEquals(3, provisionalState.getScenes().size());
            assertEquals(2, provisionalState.getImages().size());
            assertEquals("TestState", provisionalState.getName());
        }
        
        @Test
        @DisplayName("Should support state merging scenarios")
        void shouldSupportStateMerging() {
            ProvisionalState state1 = new ProvisionalState("State1");
            ProvisionalState state2 = new ProvisionalState("State2");
            
            state1.getScenes().add(1);
            state1.getScenes().add(2);
            
            state2.getScenes().add(3);
            state2.getScenes().add(4);
            
            // Simulate merging by checking scene intersections
            Set<Integer> combinedScenes = new HashSet<>();
            combinedScenes.addAll(state1.getScenes());
            combinedScenes.addAll(state2.getScenes());
            
            assertEquals(4, combinedScenes.size());
            
            // No overlap
            Set<Integer> intersection = new HashSet<>(state1.getScenes());
            intersection.retainAll(state2.getScenes());
            assertTrue(intersection.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle state comparison")
        void shouldHandleStateComparison() {
            ProvisionalState state1 = new ProvisionalState("LoginState");
            ProvisionalState state2 = new ProvisionalState("LoginState");
            
            Set<Integer> scenes = new HashSet<>();
            scenes.add(1);
            scenes.add(2);
            
            state1.getScenes().addAll(scenes);
            state2.getScenes().addAll(scenes);
            
            // Same name and scenes
            assertEquals(state1.getName(), state2.getName());
            assertTrue(state1.hasEqualSceneSets(state2.getScenes()));
            assertTrue(state2.hasEqualSceneSets(state1.getScenes()));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle null image gracefully")
        void shouldHandleNullImage() {
            // The current implementation actually adds null to the list
            // This is because isImageNested returns false for empty images list
            // and then null is added to the list
            provisionalState.addImage(null);
            
            // Null is actually added to the list
            assertEquals(1, provisionalState.getImages().size());
            assertNull(provisionalState.getImages().get(0));
        }
        
        @Test
        @DisplayName("Should handle state with only scenes")
        void shouldHandleStateWithOnlyScenes() {
            provisionalState.getScenes().add(1);
            provisionalState.getScenes().add(2);
            
            assertEquals(2, provisionalState.getScenes().size());
            assertEquals(0, provisionalState.getImages().size());
        }
        
        @Test
        @DisplayName("Should handle state with only images")
        void shouldHandleStateWithOnlyImages() {
            StateImage image = mock(StateImage.class);
            Region region = mock(Region.class);
            when(image.getLargestDefinedFixedRegionOrNewRegion()).thenReturn(region);
            when(region.contains(any(Region.class))).thenReturn(false);
            
            provisionalState.addImage(image);
            
            assertEquals(0, provisionalState.getScenes().size());
            assertEquals(1, provisionalState.getImages().size());
        }
        
        @Test
        @DisplayName("Should handle empty state name")
        void shouldHandleEmptyStateName() {
            ProvisionalState emptyNameState = new ProvisionalState("");
            assertEquals("", emptyNameState.getName());
        }
        
        @Test
        @DisplayName("Should handle special character state names")
        void shouldHandleSpecialCharacterStateNames() {
            ProvisionalState specialState = new ProvisionalState("State-123_Test#Special");
            assertEquals("State-123_Test#Special", specialState.getName());
        }
    }
}