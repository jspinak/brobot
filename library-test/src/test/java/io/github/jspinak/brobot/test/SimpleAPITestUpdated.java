package io.github.jspinak.brobot.test;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.statemanagement.StateMemory;

/**
 * Simple test to verify the API usage patterns.
 *
 * <p>Demonstrates correct usage of: - Pattern and StateImage builders - ActionConfig classes
 * (PatternFindOptions, ClickOptions) - ActionResult configuration - Match history management -
 * StateMemory operations
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimpleAPITestUpdated extends BrobotIntegrationTestBase {

    @Autowired private StateMemory stateMemory;

    @BeforeEach
    void setUp() {
        super.setUpBrobotEnvironment();
        FrameworkSettings.mock = true;
    }

    @AfterEach
    void tearDown() {
        FrameworkSettings.mock = false;
    }

    @Test
    @Order(1)
    @DisplayName("Should create Pattern with name")
    void testPatternWithNameCreation() {
        Pattern pattern =
                new Pattern.Builder()
                        .setName("TestPattern")
                        .setBufferedImage(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
                        .build();

        assertNotNull(pattern);
        assertEquals("TestPattern", pattern.getName());
        assertNotNull(pattern.getBImage());
    }

    @Test
    @Order(2)
    @DisplayName("Should create StateImage with name")
    void testStateImageWithNameCreation() {
        Pattern pattern =
                new Pattern.Builder()
                        .setName("TestPattern")
                        .setBufferedImage(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
                        .build();

        StateImage stateImage =
                new StateImage.Builder().addPattern(pattern).setName("TestStateImage").build();

        assertNotNull(stateImage);
        assertEquals("TestStateImage", stateImage.getName());
        assertEquals(1, stateImage.getPatterns().size());
    }

    @Test
    @Order(3)
    @DisplayName("Should manage match history")
    void testMatchHistoryAccess() {
        Pattern pattern =
                new Pattern.Builder()
                        .setName("TestPattern")
                        .setBufferedImage(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
                        .build();

        // Create match snapshot
        ActionRecord snapshot = new ActionRecord();
        snapshot.setActionSuccess(true);
        snapshot.setDuration(0.5);

        // Set the action config
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .setSimilarity(0.9)
                        .build();
        snapshot.setActionConfig(findOptions);

        // Add match to snapshot
        Match match =
                new Match.Builder().setRegion(new Region(10, 10, 50, 50)).setSimScore(0.95).build();
        snapshot.addMatch(match);

        // Add snapshot to pattern's match history
        pattern.getMatchHistory().addSnapshot(snapshot);

        // Verify
        assertEquals(1, pattern.getMatchHistory().getSnapshots().size());
        assertTrue(pattern.getMatchHistory().getSnapshots().get(0).isActionSuccess());
        assertEquals(
                findOptions, pattern.getMatchHistory().getSnapshots().get(0).getActionConfig());
        assertEquals(0.5, pattern.getMatchHistory().getSnapshots().get(0).getDuration(), 0.01);
    }

    @Test
    @Order(4)
    @DisplayName("Should create ActionResult with different configs")
    void testActionResultCreation() {
        // Test with PatternFindOptions
        ActionResult findResult = new ActionResult();
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.85)
                        .build();
        findResult.setActionConfig(findOptions);

        assertNotNull(findResult);
        assertEquals(findOptions, findResult.getActionConfig());
        assertTrue(findResult.isEmpty());

        // Test with ClickOptions
        ActionResult clickResult = new ActionResult();
        MousePressOptions pressOptions =
                MousePressOptions.builder().setButton(MouseButton.RIGHT).build();
        ClickOptions clickOptions =
                new ClickOptions.Builder()
                        .setNumberOfClicks(1)
                        .setPressOptions(pressOptions)
                        .build();
        clickResult.setActionConfig(clickOptions);

        assertEquals(clickOptions, clickResult.getActionConfig());
        assertEquals(
                MouseButton.RIGHT,
                ((ClickOptions) clickResult.getActionConfig()).getMousePressOptions().getButton());
    }

    @Test
    @Order(5)
    @DisplayName("Should perform StateMemory operations")
    void testStateMemoryOperations() {
        assertNotNull(stateMemory, "StateMemory should be autowired");

        // Add active state
        Long testStateId = 1L;
        stateMemory.addActiveState(testStateId);

        // Check if state is active
        assertTrue(stateMemory.getActiveStates().contains(testStateId));

        // Remove inactive state
        stateMemory.removeInactiveState(testStateId);

        // Clean up
        stateMemory.removeAllStates();
        assertFalse(stateMemory.getActiveStates().contains(testStateId));
    }

    @Test
    @Order(6)
    @DisplayName("Should create various ActionConfig types")
    void testVariousActionConfigs() {
        // Test PatternFindOptions
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.BEST)
                        .setSimilarity(0.85)
                        .setMaxMatchesToActOn(5)
                        .setCaptureImage(true)
                        .build();

        assertEquals(PatternFindOptions.Strategy.BEST, findOptions.getStrategy());
        assertEquals(0.85, findOptions.getSimilarity(), 0.001);
        assertEquals(5, findOptions.getMaxMatchesToActOn());
        assertTrue(findOptions.isCaptureImage());

        // Test ClickOptions with proper configuration
        MousePressOptions pressOptions =
                MousePressOptions.builder()
                        .setButton(MouseButton.LEFT)
                        .setPauseBeforeMouseDown(0.1)
                        .setPauseAfterMouseUp(0.2)
                        .build();

        ClickOptions clickOptions =
                new ClickOptions.Builder()
                        .setNumberOfClicks(2) // Double-click
                        .setPressOptions(pressOptions)
                        .build();

        assertEquals(2, clickOptions.getNumberOfClicks());
        assertEquals(MouseButton.LEFT, clickOptions.getMousePressOptions().getButton());
        assertEquals(0.1, clickOptions.getMousePressOptions().getPauseBeforeMouseDown(), 0.001);

        // Test TypeOptions
        TypeOptions typeOptions = new TypeOptions.Builder().setTypeDelay(0.05).build();

        assertEquals(0.05, typeOptions.getTypeDelay(), 0.001);
        // Note: Text to type comes from StateString in ObjectCollection, not
        // TypeOptions
    }

    @Test
    @Order(7)
    @DisplayName("Should use factory methods for common configs")
    void testFactoryMethods() {
        // Test quick search preset
        PatternFindOptions quickFind = PatternFindOptions.forQuickSearch();
        assertEquals(PatternFindOptions.Strategy.FIRST, quickFind.getStrategy());
        assertEquals(0.7, quickFind.getSimilarity(), 0.001);
        assertEquals(1, quickFind.getMaxMatchesToActOn());

        // Test precise search preset
        PatternFindOptions preciseFind = PatternFindOptions.forPreciseSearch();
        assertEquals(PatternFindOptions.Strategy.BEST, preciseFind.getStrategy());
        assertEquals(0.9, preciseFind.getSimilarity(), 0.001);
        assertTrue(preciseFind.isCaptureImage());

        // Test all matches preset
        PatternFindOptions allMatches = PatternFindOptions.forAllMatches();
        assertEquals(PatternFindOptions.Strategy.ALL, allMatches.getStrategy());
        assertEquals(0.8, allMatches.getSimilarity(), 0.001);
        assertEquals(-1, allMatches.getMaxMatchesToActOn()); // No limit
    }

    @Test
    @Order(8)
    @DisplayName("Should handle Pattern with position offsets")
    void testPatternWithOffsets() {
        // Create pattern with target position offset
        Pattern pattern =
                new Pattern.Builder()
                        .setName("OffsetPattern")
                        .setBufferedImage(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
                        .setTargetPosition(
                                new Position(10, 20)) // Click 10 pixels right, 20 pixels down from
                        // center
                        .build();

        assertNotNull(pattern);
        assertNotNull(pattern.getTargetPosition());
        // Position stores percentages, not absolute coordinates
        // Position(10, 20) with integer constructor creates (0.1, 0.2) as percentages
        assertEquals(0.1, pattern.getTargetPosition().getPercentW(), 0.001);
        assertEquals(0.2, pattern.getTargetPosition().getPercentH(), 0.001);
    }

    @Test
    @Order(9)
    @DisplayName("Should create complex StateImage with multiple patterns")
    void testComplexStateImage() {
        BufferedImage img1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage img2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        Pattern pattern1 = new Pattern.Builder().setName("Pattern1").setBufferedImage(img1).build();

        Pattern pattern2 =
                new Pattern.Builder()
                        .setName("Pattern2")
                        .setBufferedImage(img2)
                        .addSearchRegion(new Region(0, 0, 500, 500))
                        .build();

        StateImage stateImage =
                new StateImage.Builder()
                        .addPattern(pattern1)
                        .addPattern(pattern2)
                        .setName("ComplexStateImage")
                        .setOwnerStateName("TestState")
                        // Note: setSearchRegionForAllPatterns might not exist
                        .build();

        assertEquals(2, stateImage.getPatterns().size());
        assertEquals("ComplexStateImage", stateImage.getName());
        assertEquals("TestState", stateImage.getOwnerStateName());
    }

    @Test
    @Order(10)
    @DisplayName("Should handle ActionResult with matches")
    void testActionResultWithMatches() {
        ActionResult result = new ActionResult();

        // Add some matches
        Match match1 =
                new Match.Builder()
                        .setRegion(new Region(10, 10, 50, 50))
                        .setSimScore(0.95)
                        .setName("Match1")
                        .build();

        Match match2 =
                new Match.Builder()
                        .setRegion(new Region(100, 100, 50, 50))
                        .setSimScore(0.88)
                        .setName("Match2")
                        .build();

        result.add(match1);
        result.add(match2);

        assertFalse(result.isEmpty());
        assertEquals(2, result.getMatchList().size());
        assertTrue(result.getBestMatch().isPresent());
        assertEquals(0.95, result.getBestMatch().get().getScore(), 0.001);
        assertTrue(result.isSuccess());
    }
}
