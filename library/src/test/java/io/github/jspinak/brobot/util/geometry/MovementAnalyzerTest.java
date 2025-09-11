package io.github.jspinak.brobot.util.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for MovementAnalyzer - movement pattern detection utility. Tests
 * displacement calculation, voting algorithm, and measurement tolerance.
 */
@DisplayName("MovementAnalyzer Tests")
public class MovementAnalyzerTest extends BrobotTestBase {

    private MovementAnalyzer analyzer;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        analyzer = new MovementAnalyzer();
    }

    // Helper method to create a mock Match with specific coordinates
    private Match createMatch(int x, int y) {
        Pattern mockPattern = mock(Pattern.class);
        Region mockRegion = mock(Region.class);
        Match match = new Match(mockRegion, 0.95);
        match.x = x;
        match.y = y;
        match.w = 50;
        match.h = 50;
        return match;
    }

    @Nested
    @DisplayName("Basic Movement Detection")
    class BasicMovementDetection {

        @Test
        @DisplayName("Detect simple rightward movement")
        public void testRightwardMovement() {
            List<Match> firstMatches = Arrays.asList(createMatch(100, 100), createMatch(200, 200));

            List<Match> secondMatches =
                    Arrays.asList(
                            createMatch(150, 100), // Moved 50 pixels right
                            createMatch(250, 200) // Moved 50 pixels right
                            );

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            assertEquals(
                    -50,
                    movement.get(0)
                            .getCalculatedX()); // Negative because displacement is first - second
            assertEquals(0, movement.get(0).getCalculatedY());
        }

        @Test
        @DisplayName("Detect simple downward movement")
        public void testDownwardMovement() {
            List<Match> firstMatches = Arrays.asList(createMatch(100, 100), createMatch(200, 100));

            List<Match> secondMatches =
                    Arrays.asList(
                            createMatch(100, 150), // Moved 50 pixels down
                            createMatch(200, 150) // Moved 50 pixels down
                            );

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            assertEquals(0, movement.get(0).getCalculatedX());
            assertEquals(
                    -50,
                    movement.get(0)
                            .getCalculatedY()); // Negative because displacement is first - second
        }

        @Test
        @DisplayName("Detect diagonal movement")
        public void testDiagonalMovement() {
            List<Match> firstMatches =
                    Arrays.asList(
                            createMatch(100, 100), createMatch(200, 200), createMatch(300, 300));

            List<Match> secondMatches =
                    Arrays.asList(
                            createMatch(120, 120), // Moved 20 right, 20 down
                            createMatch(220, 220), // Moved 20 right, 20 down
                            createMatch(320, 320) // Moved 20 right, 20 down
                            );

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            assertEquals(-20, movement.get(0).getCalculatedX());
            assertEquals(-20, movement.get(0).getCalculatedY());
        }

        @Test
        @DisplayName("No movement detected")
        public void testNoMovement() {
            List<Match> firstMatches = Arrays.asList(createMatch(100, 100), createMatch(200, 200));

            List<Match> secondMatches =
                    Arrays.asList(
                            createMatch(100, 100), // Same position
                            createMatch(200, 200) // Same position
                            );

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            assertEquals(0, movement.get(0).getCalculatedX());
            assertEquals(0, movement.get(0).getCalculatedY());
        }
    }

    @Nested
    @DisplayName("Measurement Accuracy")
    class MeasurementAccuracy {

        @Test
        @DisplayName("Exact accuracy groups identical movements")
        public void testExactAccuracy() {
            List<Match> firstMatches =
                    Arrays.asList(
                            createMatch(100, 100), createMatch(200, 200), createMatch(300, 300));

            List<Match> secondMatches =
                    Arrays.asList(
                            createMatch(110, 100), // Moved 10 right
                            createMatch(210, 200), // Moved 10 right
                            createMatch(310, 300) // Moved 10 right
                            );

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            assertEquals(-10, movement.get(0).getCalculatedX());
        }

        @Test
        @DisplayName("Tolerance groups similar movements")
        public void testWithTolerance() {
            List<Match> firstMatches =
                    Arrays.asList(
                            createMatch(100, 100), createMatch(200, 200), createMatch(300, 300));

            List<Match> secondMatches =
                    Arrays.asList(
                            createMatch(110, 100), // Moved 10 right
                            createMatch(212, 200), // Moved 12 right (within tolerance)
                            createMatch(309, 300) // Moved 9 right (within tolerance)
                            );

            List<Location> movement = analyzer.getMovement(3, firstMatches, secondMatches);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            // Should group all as similar movement
            assertTrue(Math.abs(movement.get(0).getCalculatedX() + 10) <= 3);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 5, 10})
        @DisplayName("Various accuracy thresholds")
        public void testVariousAccuracyThresholds(int accuracy) {
            List<Match> firstMatches = Arrays.asList(createMatch(100, 100), createMatch(200, 200));

            List<Match> secondMatches =
                    Arrays.asList(
                            createMatch(100 + accuracy, 100), // Moved by accuracy amount
                            createMatch(200 + accuracy + 1, 200) // Moved slightly more
                            );

            List<Location> movement = analyzer.getMovement(accuracy, firstMatches, secondMatches);

            assertNotNull(movement);
            assertFalse(movement.isEmpty());
        }
    }

    @Nested
    @DisplayName("Voting Algorithm")
    class VotingAlgorithm {

        @Test
        @DisplayName("Most frequent movement wins")
        public void testMostFrequentWins() {
            List<Match> firstMatches =
                    Arrays.asList(
                            createMatch(100, 100),
                            createMatch(200, 200),
                            createMatch(300, 300),
                            createMatch(400, 400));

            List<Match> secondMatches =
                    Arrays.asList(
                            createMatch(120, 100), // Moved 20 right
                            createMatch(220, 200), // Moved 20 right
                            createMatch(320, 300), // Moved 20 right
                            createMatch(450, 400) // Moved 50 right (outlier)
                            );

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            // Most common movement should be detected
            boolean found20Movement =
                    movement.stream()
                            .anyMatch(
                                    loc ->
                                            loc.getCalculatedX() == -20
                                                    && loc.getCalculatedY() == 0);
            assertTrue(found20Movement);
        }

        @Test
        @DisplayName("Tie between movements returns all")
        public void testTieReturnsMultiple() {
            List<Match> firstMatches = Arrays.asList(createMatch(100, 100), createMatch(200, 200));

            List<Match> secondMatches =
                    Arrays.asList(
                            createMatch(110, 100), // Moved 10 right
                            createMatch(190, 200) // Moved -10 left
                            );

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            // With 2 matches and 2 different movements, could have multiple results
            assertFalse(movement.isEmpty());
        }

        @Test
        @DisplayName("Multiple matches reinforce consensus")
        public void testConsensusBuilding() {
            List<Match> firstMatches = new ArrayList<>();
            List<Match> secondMatches = new ArrayList<>();

            // Add 10 matches with consistent movement
            for (int i = 0; i < 10; i++) {
                firstMatches.add(createMatch(i * 100, i * 100));
                secondMatches.add(
                        createMatch(i * 100 + 25, i * 100 + 15)); // All moved 25 right, 15 down
            }

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            assertEquals(-25, movement.get(0).getCalculatedX());
            assertEquals(-15, movement.get(0).getCalculatedY());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Empty first matches returns empty")
        public void testEmptyFirstMatches() {
            List<Match> firstMatches = new ArrayList<>();
            List<Match> secondMatches = Arrays.asList(createMatch(100, 100));

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            assertTrue(movement.isEmpty());
        }

        @Test
        @DisplayName("Empty second matches returns empty")
        public void testEmptySecondMatches() {
            List<Match> firstMatches = Arrays.asList(createMatch(100, 100));
            List<Match> secondMatches = new ArrayList<>();

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            assertTrue(movement.isEmpty());
        }

        @Test
        @DisplayName("Both empty returns empty")
        public void testBothEmpty() {
            List<Match> firstMatches = new ArrayList<>();
            List<Match> secondMatches = new ArrayList<>();

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            assertTrue(movement.isEmpty());
        }

        @Test
        @DisplayName("Single match pair")
        public void testSingleMatchPair() {
            List<Match> firstMatches = Arrays.asList(createMatch(100, 100));
            List<Match> secondMatches = Arrays.asList(createMatch(150, 120));

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            assertEquals(-50, movement.get(0).getCalculatedX());
            assertEquals(-20, movement.get(0).getCalculatedY());
        }

        @Test
        @DisplayName("Negative coordinates")
        public void testNegativeCoordinates() {
            List<Match> firstMatches =
                    Arrays.asList(createMatch(-100, -100), createMatch(-200, -200));

            List<Match> secondMatches =
                    Arrays.asList(createMatch(-80, -90), createMatch(-180, -190));

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            assertEquals(-20, movement.get(0).getCalculatedX());
            assertEquals(-10, movement.get(0).getCalculatedY());
        }

        @Test
        @DisplayName("Very large movements")
        public void testLargeMovements() {
            List<Match> firstMatches = Arrays.asList(createMatch(0, 0), createMatch(100, 100));

            List<Match> secondMatches =
                    Arrays.asList(createMatch(10000, 5000), createMatch(10100, 5100));

            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            assertEquals(-10000, movement.get(0).getCalculatedX());
            assertEquals(-5000, movement.get(0).getCalculatedY());
        }
    }

    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Game map scrolling detection")
        public void testGameMapScrolling() {
            // Simulate landmarks on a game map before scrolling
            List<Match> beforeScroll =
                    Arrays.asList(
                            createMatch(100, 200), // Building A
                            createMatch(300, 250), // Building B
                            createMatch(500, 400), // Building C
                            createMatch(200, 500) // Building D
                            );

            // After scrolling right by 150 pixels
            List<Match> afterScroll =
                    Arrays.asList(
                            createMatch(250, 200), // Building A moved
                            createMatch(450, 250), // Building B moved
                            createMatch(650, 400), // Building C moved
                            createMatch(350, 500) // Building D moved
                            );

            List<Location> movement = analyzer.getMovement(0, beforeScroll, afterScroll);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            assertEquals(-150, movement.get(0).getCalculatedX()); // Map scrolled right
            assertEquals(0, movement.get(0).getCalculatedY());
        }

        @Test
        @DisplayName("Parallax scrolling with multiple layers")
        public void testParallaxScrolling() {
            // Foreground objects
            List<Match> foregroundBefore =
                    Arrays.asList(
                            createMatch(100, 300), createMatch(200, 300), createMatch(300, 300));

            // Background moves slower (parallax effect)
            List<Match> foregroundAfter =
                    Arrays.asList(
                            createMatch(150, 300), // Moved 50 pixels
                            createMatch(250, 300), // Moved 50 pixels
                            createMatch(330, 300) // Moved 30 pixels (measurement error)
                            );

            List<Location> movement = analyzer.getMovement(25, foregroundBefore, foregroundAfter);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            // Should detect primary movement despite some variation
            assertTrue(Math.abs(movement.get(0).getCalculatedX() + 50) <= 25);
        }

        @Test
        @DisplayName("Camera pan in video application")
        public void testCameraPan() {
            // Track features during camera pan
            List<Match> frameBefore =
                    Arrays.asList(
                            createMatch(640, 360), // Center feature
                            createMatch(320, 180), // Top-left feature
                            createMatch(960, 540), // Bottom-right feature
                            createMatch(320, 540), // Bottom-left feature
                            createMatch(960, 180) // Top-right feature
                            );

            // Camera panned diagonally
            List<Match> frameAfter =
                    Arrays.asList(
                            createMatch(590, 310), // All features moved
                            createMatch(270, 130),
                            createMatch(910, 490),
                            createMatch(270, 490),
                            createMatch(910, 130));

            List<Location> movement = analyzer.getMovement(0, frameBefore, frameAfter);

            assertNotNull(movement);
            assertEquals(1, movement.size());
            assertEquals(50, movement.get(0).getCalculatedX()); // Camera moved left (objects right)
            assertEquals(50, movement.get(0).getCalculatedY()); // Camera moved up (objects down)
        }

        @Test
        @DisplayName("Noisy detection with outliers")
        public void testNoisyDetection() {
            List<Match> firstMatches = new ArrayList<>();
            List<Match> secondMatches = new ArrayList<>();

            // Add consistent movement for most matches
            for (int i = 0; i < 8; i++) {
                firstMatches.add(createMatch(i * 100, i * 50));
                secondMatches.add(
                        createMatch(i * 100 + 30, i * 50)); // Consistent 30 pixel movement
            }

            // Add outliers (false detections)
            firstMatches.add(createMatch(800, 400));
            secondMatches.add(createMatch(900, 450)); // Different movement

            firstMatches.add(createMatch(900, 450));
            secondMatches.add(createMatch(700, 350)); // Very different movement

            List<Location> movement = analyzer.getMovement(5, firstMatches, secondMatches);

            assertNotNull(movement);
            // Should still detect the consistent movement despite outliers
            boolean foundConsistentMovement =
                    movement.stream()
                            .anyMatch(
                                    loc ->
                                            Math.abs(loc.getCalculatedX() + 30) <= 5
                                                    && Math.abs(loc.getCalculatedY()) <= 5);
            assertTrue(foundConsistentMovement);
        }
    }

    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceCharacteristics {

        @Test
        @DisplayName("Handle large number of matches")
        public void testLargeMatchSets() {
            List<Match> firstMatches = new ArrayList<>();
            List<Match> secondMatches = new ArrayList<>();

            // Create 100 matches with consistent movement
            for (int i = 0; i < 100; i++) {
                firstMatches.add(createMatch(i * 10, i * 10));
                secondMatches.add(createMatch(i * 10 + 5, i * 10 + 3));
            }

            long startTime = System.currentTimeMillis();
            List<Location> movement = analyzer.getMovement(0, firstMatches, secondMatches);
            long endTime = System.currentTimeMillis();

            assertNotNull(movement);
            assertEquals(1, movement.size());
            assertEquals(-5, movement.get(0).getCalculatedX());
            assertEquals(-3, movement.get(0).getCalculatedY());

            // Should complete in reasonable time
            assertTrue(endTime - startTime < 1000, "Analysis took " + (endTime - startTime) + "ms");
        }

        @RepeatedTest(5)
        @DisplayName("Consistent results for same input")
        public void testConsistentResults() {
            List<Match> firstMatches =
                    Arrays.asList(
                            createMatch(100, 100), createMatch(200, 200), createMatch(300, 300));

            List<Match> secondMatches =
                    Arrays.asList(
                            createMatch(125, 115), createMatch(225, 215), createMatch(325, 315));

            List<Location> movement1 = analyzer.getMovement(0, firstMatches, secondMatches);
            List<Location> movement2 = analyzer.getMovement(0, firstMatches, secondMatches);

            assertEquals(movement1.size(), movement2.size());
            if (!movement1.isEmpty()) {
                assertEquals(movement1.get(0).getCalculatedX(), movement2.get(0).getCalculatedX());
                assertEquals(movement1.get(0).getCalculatedY(), movement2.get(0).getCalculatedY());
            }
        }
    }

    @Nested
    @DisplayName("Complex Movement Patterns")
    class ComplexMovementPatterns {

        @Test
        @DisplayName("Rotation detection (should show varied movements)")
        public void testRotationDetection() {
            // Objects rotating around center point
            List<Match> before =
                    Arrays.asList(
                            createMatch(200, 100), // Top
                            createMatch(300, 200), // Right
                            createMatch(200, 300), // Bottom
                            createMatch(100, 200) // Left
                            );

            // After 90-degree rotation
            List<Match> after =
                    Arrays.asList(
                            createMatch(300, 200), // Top -> Right
                            createMatch(200, 300), // Right -> Bottom
                            createMatch(100, 200), // Bottom -> Left
                            createMatch(200, 100) // Left -> Top
                            );

            List<Location> movement = analyzer.getMovement(0, before, after);

            assertNotNull(movement);
            // Rotation produces multiple different movements
            // This test documents that rotation is not detected as single movement
            assertFalse(movement.isEmpty());
        }

        @Test
        @DisplayName("Zoom detection (radial movement)")
        public void testZoomDetection() {
            // Objects around center before zoom
            List<Match> before =
                    Arrays.asList(
                            createMatch(200, 200), // Center stays
                            createMatch(150, 150), // Top-left
                            createMatch(250, 150), // Top-right
                            createMatch(150, 250), // Bottom-left
                            createMatch(250, 250) // Bottom-right
                            );

            // After zoom out (objects move toward center)
            List<Match> after =
                    Arrays.asList(
                            createMatch(200, 200), // Center stays
                            createMatch(175, 175), // Moved toward center
                            createMatch(225, 175), // Moved toward center
                            createMatch(175, 225), // Moved toward center
                            createMatch(225, 225) // Moved toward center
                            );

            List<Location> movement = analyzer.getMovement(5, before, after);

            assertNotNull(movement);
            // Zoom produces varied movements, not a single consistent one
            // This test shows current algorithm limitation with zoom
            assertFalse(movement.isEmpty());
        }
    }
}
