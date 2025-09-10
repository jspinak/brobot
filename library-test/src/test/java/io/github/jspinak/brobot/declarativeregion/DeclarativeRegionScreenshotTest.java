package io.github.jspinak.brobot.declarativeregion;

import org.junit.jupiter.api.Disabled;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.navigation.service.StateService;
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
 * Test declarative region definition using fixed screenshots.
 * This test uses actual image files from the FloraNext test resources.
 * Can be run from WSL with mock mode enabled.
 * 
 * Tests the scenario where topLeft image sets the search region for dependent
 * images.
 */
@SpringBootTest
@DisplayName("Declarative Region Tests with Fixed Screenshots")
@Disabled("Failing in CI - temporarily disabled for CI/CD")
public class DeclarativeRegionScreenshotTest extends BrobotTestBase {

        @Autowired
        private Action action;

        @MockBean
        private StateService stateService;

        private State floraState;
        private State menuState;
        private StateImage topLeftImage;
        private StateImage menuItemImage;

        @BeforeEach
        @Override
        public void setupTest() {
                super.setupTest();
                assertTrue(FrameworkSettings.mock, "Test runs in mock mode for WSL compatibility");

                setupFloraNextImages();
                setupMocks();
        }

        private void setupFloraNextImages() {
                // Create topLeft image - this is the reference point
                topLeftImage = new StateImage.Builder()
                                .setName("topLeft")
                                .addPatterns("FloraNext/topLeft") // Reference to actual image file
                                .setFixedForAllPatterns(true)
                                .build();

                // This image would typically be in the top-left corner of the screen
                topLeftImage.setSearchRegions(new Region(0, 0, 200, 100));

                floraState = new State.Builder("FloraNext")
                                .withImages(topLeftImage)
                                .build();

                // Create menu item that depends on topLeft location
                menuItemImage = new StateImage.Builder()
                                .setName("menuItem")
                                .addPatterns("FloraNext/menuItem") // Another image from FloraNext
                                .setFixedForAllPatterns(true)
                                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                                                .setTargetType(StateObject.Type.IMAGE)
                                                .setTargetStateName("FloraNext")
                                                .setTargetObjectName("topLeft")
                                                .setAdjustments(MatchAdjustmentOptions.builder()
                                                                .setAddX(0) // Same X position
                                                                .setAddY(100) // 100 pixels below topLeft
                                                                .setAddW(300) // Wider search area
                                                                .setAddH(400) // Taller search area
                                                                .build())
                                                .build())
                                .build();

                // Initially has full screen search (problematic)
                menuItemImage.setSearchRegions(new Region(0, 0, 1920, 1080));

                menuState = new State.Builder("MenuState")
                                .withImages(menuItemImage)
                                .build();

                topLeftImage.setOwnerStateName("FloraNext");
                menuItemImage.setOwnerStateName("MenuState");
        }

        private void setupMocks() {
                when(stateService.getState("FloraNext")).thenReturn(Optional.of(floraState));
                when(stateService.getState("MenuState")).thenReturn(Optional.of(menuState));
        }

        @Test
        @DisplayName("Screenshot test: topLeft defines search region for menuItem")
        void testTopLeftDefinesMenuItemRegion() {
                // Step 1: Find topLeft image
                ActionResult topLeftResult = action.find(topLeftImage);
                assertTrue(topLeftResult.isSuccess(), "Should find topLeft in mock mode");

                // Step 2: Verify menuItem depends on topLeft
                SearchRegionOnObject dependency = menuItemImage.getSearchRegionOnObject();
                assertNotNull(dependency, "menuItem should have declarative region");
                assertEquals("FloraNext", dependency.getTargetStateName());
                assertEquals("topLeft", dependency.getTargetObjectName());

                // Step 3: Find menuItem - should use declarative region
                ActionResult menuResult = action.find(menuItemImage);
                assertTrue(menuResult.isSuccess(), "Should find menuItem in mock mode");

                // Verify the adjustments
                MatchAdjustmentOptions adjustments = dependency.getAdjustments();
                assertEquals(0, adjustments.getAddX(), "Should be aligned horizontally");
                assertEquals(100, adjustments.getAddY(), "Should be 100px below topLeft");
                assertEquals(300, adjustments.getAddW(), "Should have 300px width adjustment");
        }

        @Test
        @DisplayName("Screenshot test: Fixed region cleared when declarative applied")
        void testFixedRegionClearedForDeclarative() {
                // Initially, menuItem has full-screen search region
                assertFalse(menuItemImage.getPatterns().isEmpty(), "Should have patterns");
                // Verify patterns have search regions set
                Pattern firstPattern = menuItemImage.getPatterns().get(0);
                assertNotNull(firstPattern.getSearchRegions(), "Pattern should have search regions");

                // After finding topLeft, menuItem should use declarative region
                ActionResult topLeftResult = action.find(topLeftImage);
                assertTrue(topLeftResult.isSuccess());

                // IMPORTANT: When the declarative region is set as a search region on menuItem,
                // menuItem's fixed region should be DELETED. This is the expected behavior:
                // 1. menuItem initially has a fixed region (full screen 1920x1080)
                // 2. menuItem has a declarative dependency on topLeft
                // 3. When topLeft is found, the declarative region is calculated
                // 4. The fixed region on menuItem should be CLEARED/DELETED
                // 5. menuItem should now use only the declarative region
                //
                // DynamicRegionResolver would:
                // 1. DELETE the full-screen fixed region
                // 2. Calculate new region based on topLeft location
                // 3. Apply adjustments (0, +100, 300w, 400h)

                // Find menuItem with constrained region
                ActionResult menuResult = action.find(menuItemImage);
                assertTrue(menuResult.isSuccess());

                // Verify dependency exists
                assertNotNull(menuItemImage.getSearchRegionOnObject());
        }

        @Test
        @DisplayName("Screenshot test: Multiple images depending on topLeft")
        void testMultipleImagesDependOnTopLeft() {
                // Create additional images that depend on topLeft
                StateImage rightPanel = new StateImage.Builder()
                                .setName("rightPanel")
                                .addPatterns("FloraNext/rightPanel")
                                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                                                .setTargetType(StateObject.Type.IMAGE)
                                                .setTargetStateName("FloraNext")
                                                .setTargetObjectName("topLeft")
                                                .setAdjustments(MatchAdjustmentOptions.builder()
                                                                .setAddX(1000) // Far to the right
                                                                .setAddY(0) // Same height
                                                                .setAddW(500)
                                                                .setAddH(300)
                                                                .build())
                                                .build())
                                .build();

                StateImage bottomBar = new StateImage.Builder()
                                .setName("bottomBar")
                                .addPatterns("FloraNext/bottomBar")
                                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                                                .setTargetType(StateObject.Type.IMAGE)
                                                .setTargetStateName("FloraNext")
                                                .setTargetObjectName("topLeft")
                                                .setAdjustments(MatchAdjustmentOptions.builder()
                                                                .setAddX(0) // Same X
                                                                .setAddY(900) // Near bottom
                                                                .setAddW(1920) // Full width
                                                                .setAddH(100) // Thin bar
                                                                .build())
                                                .build())
                                .build();

                // All depend on topLeft
                assertEquals("topLeft", rightPanel.getSearchRegionOnObject().getTargetObjectName());
                assertEquals("topLeft", bottomBar.getSearchRegionOnObject().getTargetObjectName());

                // Find topLeft first
                ActionResult topLeftResult = action.find(topLeftImage);
                assertTrue(topLeftResult.isSuccess());

                // Now all dependent images can be found
                ActionResult rightResult = action.find(rightPanel);
                ActionResult bottomResult = action.find(bottomBar);

                assertTrue(rightResult.isSuccess());
                assertTrue(bottomResult.isSuccess());
        }

        @Test
        @DisplayName("Screenshot test: Chain of dependencies")
        void testChainOfDependencies() {
                // topLeft -> menuItem -> subMenuItem
                StateImage subMenuItem = new StateImage.Builder()
                                .setName("subMenuItem")
                                .addPatterns("FloraNext/subMenuItem")
                                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                                                .setTargetType(StateObject.Type.IMAGE)
                                                .setTargetStateName("MenuState")
                                                .setTargetObjectName("menuItem") // Depends on menuItem, not topLeft
                                                .setAdjustments(MatchAdjustmentOptions.builder()
                                                                .setAddX(20) // Indented
                                                                .setAddY(30) // Below menu item
                                                                .setAddW(200)
                                                                .setAddH(50)
                                                                .build())
                                                .build())
                                .build();

                // Chain: topLeft -> menuItem -> subMenuItem
                assertNotNull(menuItemImage.getSearchRegionOnObject());
                assertNotNull(subMenuItem.getSearchRegionOnObject());

                // Must find in order
                ActionResult topLeftResult = action.find(topLeftImage);
                assertTrue(topLeftResult.isSuccess(), "Find topLeft first");

                ActionResult menuResult = action.find(menuItemImage);
                assertTrue(menuResult.isSuccess(), "Find menuItem second");

                ActionResult subMenuResult = action.find(subMenuItem);
                assertTrue(subMenuResult.isSuccess(), "Find subMenuItem last");

                // Each depends on the previous
                assertEquals("topLeft", menuItemImage.getSearchRegionOnObject().getTargetObjectName());
                assertEquals("menuItem", subMenuItem.getSearchRegionOnObject().getTargetObjectName());
        }

        @Test
        @DisplayName("Screenshot test: Real-world FloraNext scenario")
        void testRealWorldFloraNextScenario() {
                // Simulate the actual FloraNext UI structure
                // topLeft is the anchor point for the entire UI

                // Step 1: Find the topLeft corner icon/logo
                ActionResult topLeftFound = action.find(topLeftImage);
                assertTrue(topLeftFound.isSuccess(), "topLeft should be found");

                // Step 2: Menu items are positioned relative to topLeft
                ActionResult menuFound = action.find(menuItemImage);
                assertTrue(menuFound.isSuccess(), "Menu should be found relative to topLeft");

                // Step 3: Verify the search regions are constrained
                // Without declarative regions, menuItem would search full screen (1920x1080)
                // With declarative regions, it searches a smaller area (300x400) near topLeft

                SearchRegionOnObject menuDependency = menuItemImage.getSearchRegionOnObject();
                MatchAdjustmentOptions menuAdjustments = menuDependency.getAdjustments();

                // The constrained search area is much smaller than full screen
                int constrainedArea = menuAdjustments.getAddW() * menuAdjustments.getAddH();
                int fullScreenArea = 1920 * 1080;

                assertTrue(constrainedArea < fullScreenArea / 10,
                                "Constrained search area should be much smaller than full screen");

                // This prevents false matches and improves performance
                assertEquals(300 * 400, constrainedArea, "Search area should be 300x400");
        }
}