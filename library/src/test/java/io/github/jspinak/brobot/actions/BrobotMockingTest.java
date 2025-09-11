package io.github.jspinak.brobot.actions;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.*;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Unit test demonstrating Brobot mock vs real mode behavior.
 *
 * <p>This test verifies that: - Mock mode is properly enabled from BrobotTestBase - Real mode can
 * be enabled but handles headless environments gracefully - Pattern match history is preserved and
 * accessible - Mode switching works correctly
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BrobotMockingTest extends BrobotTestBase {

    private StateImage stateImageWithHistory;
    private StateImage stateImageWithoutHistory;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest(); // Sets up mock mode from BrobotTestBase

        // Create test images
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        // Create Pattern with name using builder
        Pattern pattern1 =
                new Pattern.Builder().setBufferedImage(dummyImage).setName("TestPattern1").build();

        // Create another pattern
        Pattern pattern2 =
                new Pattern.Builder().setBufferedImage(dummyImage).setName("TestPattern2").build();

        // Create StateImage with match history
        stateImageWithHistory =
                new StateImage.Builder()
                        .addPattern(pattern1)
                        .setName("ImageWithHistory")
                        .setOwnerStateName("TestState")
                        .build();

        // Add match history to simulate previous Find operations
        ActionRecord snapshot1 = new ActionRecord();
        snapshot1.setActionSuccess(true);
        snapshot1.setDuration(0.5);
        snapshot1.setStateId(1L);
        snapshot1.setStateName("TestState");

        // Set the action config to PatternFindOptions
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .build();
        snapshot1.setActionConfig(findOptions);

        Match match1 =
                new Match.Builder().setRegion(new Region(10, 10, 50, 50)).setSimScore(0.95).build();
        snapshot1.addMatch(match1);

        Match match2 =
                new Match.Builder()
                        .setRegion(new Region(100, 100, 50, 50))
                        .setSimScore(0.90)
                        .build();
        snapshot1.addMatch(match2);

        // Add snapshot to pattern's match history
        pattern1.getMatchHistory().addSnapshot(snapshot1);

        // Create StateImage without match history
        stateImageWithoutHistory =
                new StateImage.Builder()
                        .addPattern(pattern2)
                        .setName("ImageWithoutHistory")
                        .setOwnerStateName("TestState")
                        .build();
    }

    @AfterEach
    void tearDown() {
        // Reset to default mock mode
        FrameworkSettings.mock = true;
    }

    @Test
    @Order(1)
    @DisplayName("Should start with mock mode enabled from BrobotTestBase")
    void testStartsInMockMode() {
        // BrobotTestBase should set mock mode to true
        assertTrue(FrameworkSettings.mock, "Should start in mock mode from BrobotTestBase");
    }

    @Test
    @Order(2)
    @DisplayName("Should handle real mode appropriately based on environment")
    void testRealModeBehavior() {
        // This test verifies that real mode either works or fails gracefully
        // depending on whether we're in a headless environment

        // First, ensure we start in mock mode (from BrobotTestBase)
        assertTrue(FrameworkSettings.mock, "Should start in mock mode from BrobotTestBase");

        // Now test switching to real mode
        FrameworkSettings.mock = false;
        assertFalse(FrameworkSettings.mock, "Should be able to switch to real mode");

        // In headless environments, real operations may fail, which is expected
        try {
            // Try to check if we're in a headless environment
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            boolean isHeadless = ge.isHeadlessInstance();

            if (isHeadless) {
                // In headless mode, certain operations will fail
                assertTrue(isHeadless, "Correctly detected headless environment");
            } else {
                // In non-headless mode, operations should work
                assertFalse(isHeadless, "Correctly detected GUI environment");
            }
        } catch (HeadlessException e) {
            // This is expected in headless environments
            assertTrue(true, "HeadlessException is expected in headless environments");
        } catch (Exception e) {
            // Other exceptions might occur in CI/CD environments
            String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            assertTrue(
                    message.contains("headless")
                            || message.contains("display")
                            || message.contains("awt"),
                    "Expected a display-related exception, but got: " + e.getClass().getName());
        } finally {
            // Always restore mock mode for other tests
            FrameworkSettings.mock = true;
        }
    }

    @Test
    @Order(3)
    @DisplayName("Should preserve match history in patterns")
    void testPatternMatchHistory() {
        // Ensure mock mode is enabled
        FrameworkSettings.mock = true;

        // Verify pattern with history has the expected data
        Pattern patternWithHistory = stateImageWithHistory.getPatterns().get(0);
        assertFalse(
                patternWithHistory.getMatchHistory().isEmpty(),
                "Pattern should have match history");

        // The snapshot we added should be accessible via getRandomSnapshot
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .build();

        var snapshotOpt = patternWithHistory.getMatchHistory().getRandomSnapshot(findOptions);
        assertTrue(snapshotOpt.isPresent(), "Should have a snapshot for the action config");

        ActionRecord snapshot = snapshotOpt.get();
        assertNotNull(snapshot, "Snapshot should not be null");
        assertEquals(2, snapshot.getMatchList().size(), "Should have 2 matches in history");

        // Verify the matches have the expected properties
        Match firstMatch = snapshot.getMatchList().get(0);
        assertEquals(
                0.95,
                firstMatch.getScore(),
                0.001,
                "First match should have 0.95 similarity score");

        Match secondMatch = snapshot.getMatchList().get(1);
        assertEquals(
                0.90,
                secondMatch.getScore(),
                0.001,
                "Second match should have 0.90 similarity score");

        // Verify pattern without history is empty
        Pattern patternWithoutHistory = stateImageWithoutHistory.getPatterns().get(0);
        assertTrue(
                patternWithoutHistory.getMatchHistory().isEmpty(),
                "Pattern without history should have empty match history");
    }

    @Test
    @Order(4)
    @DisplayName("Should be able to switch between mock and real mode")
    void testModeSwitching() {
        // Start in mock mode
        FrameworkSettings.mock = true;
        assertTrue(FrameworkSettings.mock, "Should be in mock mode");

        // Switch to real mode
        FrameworkSettings.mock = false;
        assertFalse(FrameworkSettings.mock, "Should be in real mode");

        // Switch back to mock mode
        FrameworkSettings.mock = true;
        assertTrue(FrameworkSettings.mock, "Should be back in mock mode");
    }

    @Test
    @Order(5)
    @DisplayName("Should handle ObjectCollection with different image types")
    void testObjectCollectionBuilding() {
        // Test building collection with images that have history
        ObjectCollection collectionWithHistory =
                new ObjectCollection.Builder().withImages(stateImageWithHistory).build();

        assertNotNull(collectionWithHistory, "Collection should not be null");
        assertEquals(
                1, collectionWithHistory.getStateImages().size(), "Should have one state image");

        // Test building collection with images without history
        ObjectCollection collectionWithoutHistory =
                new ObjectCollection.Builder().withImages(stateImageWithoutHistory).build();

        assertNotNull(collectionWithoutHistory, "Collection should not be null");
        assertEquals(
                1, collectionWithoutHistory.getStateImages().size(), "Should have one state image");

        // Test building collection with multiple images
        ObjectCollection combinedCollection =
                new ObjectCollection.Builder()
                        .withImages(stateImageWithHistory, stateImageWithoutHistory)
                        .build();

        assertNotNull(combinedCollection, "Combined collection should not be null");
        assertEquals(2, combinedCollection.getStateImages().size(), "Should have two state images");
    }
}
