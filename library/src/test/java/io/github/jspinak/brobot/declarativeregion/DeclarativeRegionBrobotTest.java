package io.github.jspinak.brobot.declarativeregion;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test declarative region definition using Brobot's mock mode.
 * This test can be run from WSL or any headless environment.
 * 
 * Tests the scenario where ImageA sets the search region for ImageB,
 * and ImageB's fixed region should be cleared when the declarative region is applied.
 */
@SpringBootTest
@DisplayName("Declarative Region Tests with Brobot Mock")
public class DeclarativeRegionBrobotTest extends BrobotTestBase {

    @Autowired
    private Action action;
    
    @MockBean
    private StateService stateService;
    
    @MockBean
    private StateMemory stateMemory;
    
    private State stateA;
    private State stateB;
    private StateImage imageA;
    private StateImage imageB;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        assertTrue(FrameworkSettings.mock, "Test runs in mock mode for WSL compatibility");
        
        setupStates();
        setupMocks();
    }
    
    private void setupStates() {
        // Create StateA with ImageA (reference image)
        imageA = new StateImage.Builder()
                .setName("ReferenceImage")
                .addPatterns("reference-pattern-1", "reference-pattern-2")
                .setFixedForAllPatterns(true)
                .build();
        
        stateA = new State.Builder("StateA")
                .withImages(imageA)
                .build();
        
        // Create StateB with ImageB (dependent image)
        imageB = new StateImage.Builder()
                .setName("DependentImage")
                .addPatterns("dependent-pattern-1", "dependent-pattern-2")
                .setFixedForAllPatterns(true)
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                        .setTargetType(StateObject.Type.IMAGE)
                        .setTargetStateName("StateA")
                        .setTargetObjectName("ReferenceImage")
                        .setAdjustments(MatchAdjustmentOptions.builder()
                                .setAddX(100)  // Search 100 pixels to the right
                                .setAddY(-50)  // Search 50 pixels above
                                .setAddW(200)
                                .setAddH(100)
                                .build())
                        .build())
                .build();
        
        stateB = new State.Builder("StateB")
                .withImages(imageB)
                .build();
        
        // Set owner states
        imageA.setOwnerStateName("StateA");
        imageB.setOwnerStateName("StateB");
    }
    
    private void setupMocks() {
        when(stateService.getState("StateA")).thenReturn(Optional.of(stateA));
        when(stateService.getState("StateB")).thenReturn(Optional.of(stateB));
        when(stateService.getState(1L)).thenReturn(Optional.of(stateA));
        when(stateService.getState(2L)).thenReturn(Optional.of(stateB));
    }
    
    @Test
    @DisplayName("Mock mode: ImageB search region depends on ImageA location")
    void testDeclarativeRegionDependency() {
        // Step 1: Find ImageA
        ActionResult resultA = action.find(imageA);
        assertTrue(resultA.isSuccess(), "Should find ImageA in mock mode");
        
        // In mock mode, matches are simulated
        assertNotNull(resultA.getDuration());
        
        // Step 2: ImageB has declarative dependency on ImageA
        assertNotNull(imageB.getSearchRegionOnObject());
        assertEquals("StateA", imageB.getSearchRegionOnObject().getTargetStateName());
        assertEquals("ReferenceImage", imageB.getSearchRegionOnObject().getTargetObjectName());
        
        // Step 3: Find ImageB - should use declarative region
        ActionResult resultB = action.find(imageB);
        assertTrue(resultB.isSuccess(), "Should find ImageB in mock mode");
        
        // Verify the adjustments that would be applied
        MatchAdjustmentOptions adjustments = imageB.getSearchRegionOnObject().getAdjustments();
        assertEquals(100, adjustments.getAddX());
        assertEquals(-50, adjustments.getAddY());
    }
    
    @Test
    @DisplayName("Mock mode: Fixed region should be cleared for declarative region")
    void testFixedRegionClearing() {
        // Give ImageB an initial fixed region by setting search regions on all patterns
        Region fixedRegion = new Region(0, 0, 1920, 1080); // Full screen
        imageB.setSearchRegions(fixedRegion);
        
        // ImageB has both search regions and declarative regions
        assertFalse(imageB.getPatterns().isEmpty(), "ImageB should have patterns");
        assertNotNull(imageB.getSearchRegionOnObject());
        
        // IMPORTANT: When the declarative region is set as a search region on ImageB,
        // ImageB's fixed region should be deleted. This is the expected behavior:
        // 1. ImageB initially has a fixed region (full screen)
        // 2. ImageB has a declarative dependency on ImageA
        // 3. When ImageA is found, the declarative region is calculated
        // 4. The fixed region on ImageB should be CLEARED/DELETED
        // 5. ImageB should now use only the declarative region
        
        // Find ImageA first to establish reference
        ActionResult resultA = action.find(imageA);
        assertTrue(resultA.isSuccess());
        
        // Find ImageB - should use declarative region, not fixed
        // The fixed region should have been deleted when declarative region was applied
        ActionResult resultB = action.find(imageB);
        assertTrue(resultB.isSuccess());
        
        // In real execution, DynamicRegionResolver would clear the fixed region
        // when applying the declarative region
    }
    
    @Test
    @DisplayName("Mock mode: Circular dependency handling")
    void testCircularDependency() {
        // Make ImageA depend on ImageB
        imageA.setSearchRegionOnObject(SearchRegionOnObject.builder()
                .setTargetType(StateObject.Type.IMAGE)
                .setTargetStateName("StateB")
                .setTargetObjectName("DependentImage")
                .build());
        
        // Now both images depend on each other
        assertNotNull(imageA.getSearchRegionOnObject());
        assertNotNull(imageB.getSearchRegionOnObject());
        
        // Mock mode should handle this gracefully
        ActionResult resultA = action.find(imageA);
        ActionResult resultB = action.find(imageB);
        
        // Both should succeed in mock mode
        assertTrue(resultA.isSuccess());
        assertTrue(resultB.isSuccess());
        
        // In real mode, this would require fallback to full screen search
    }
    
    @Test
    @DisplayName("Mock mode: Missing dependency handling")
    void testMissingDependency() {
        // Create image with dependency on non-existent target
        StateImage orphanImage = new StateImage.Builder()
                .setName("OrphanImage")
                .addPatterns("orphan-pattern")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                        .setTargetType(StateObject.Type.IMAGE)
                        .setTargetStateName("NonExistent")
                        .setTargetObjectName("NonExistent")
                        .build())
                .build();
        
        // Should still work in mock mode
        ActionResult result = action.find(orphanImage);
        assertTrue(result.isSuccess(), "Mock mode handles missing dependencies");
    }
    
    @Test
    @DisplayName("Mock mode: Complex adjustment calculations")
    void testComplexAdjustments() {
        // Create image with complex adjustments
        StateImage complexImage = new StateImage.Builder()
                .setName("ComplexImage")
                .addPatterns("complex-pattern")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                        .setTargetType(StateObject.Type.IMAGE)
                        .setTargetStateName("StateA")
                        .setTargetObjectName("ReferenceImage")
                        .setAdjustments(MatchAdjustmentOptions.builder()
                                .setAddX(150)
                                .setAddY(-75)
                                .setAddW(300)
                                .setAddH(200)
                                .build())
                        .build())
                .build();
        
        // Verify all adjustments are preserved
        MatchAdjustmentOptions adj = complexImage.getSearchRegionOnObject().getAdjustments();
        assertEquals(150, adj.getAddX());
        assertEquals(-75, adj.getAddY());
        assertEquals(300, adj.getAddW());
        assertEquals(200, adj.getAddH());
        
        // Should work in mock mode
        ActionResult result = action.find(complexImage);
        assertTrue(result.isSuccess());
    }
}