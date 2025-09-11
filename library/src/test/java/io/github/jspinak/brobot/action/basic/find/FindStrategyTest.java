package io.github.jspinak.brobot.action.basic.find;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Comprehensive test suite for FindStrategy enum. Tests all find strategy types and their
 * categorization.
 */
@DisplayName("FindStrategy Tests")
@DisabledInCI
public class FindStrategyTest extends BrobotTestBase {

    @Nested
    @DisplayName("Pattern-Based Strategies")
    class PatternBasedStrategies {

        @Test
        @DisplayName("FIRST strategy exists")
        public void testFirstStrategy() {
            FindStrategy strategy = FindStrategy.FIRST;
            assertNotNull(strategy);
            assertEquals("FIRST", strategy.name());
        }

        @Test
        @DisplayName("EACH strategy exists")
        public void testEachStrategy() {
            FindStrategy strategy = FindStrategy.EACH;
            assertNotNull(strategy);
            assertEquals("EACH", strategy.name());
        }

        @Test
        @DisplayName("ALL strategy exists")
        public void testAllStrategy() {
            FindStrategy strategy = FindStrategy.ALL;
            assertNotNull(strategy);
            assertEquals("ALL", strategy.name());
        }

        @Test
        @DisplayName("BEST strategy exists")
        public void testBestStrategy() {
            FindStrategy strategy = FindStrategy.BEST;
            assertNotNull(strategy);
            assertEquals("BEST", strategy.name());
        }
    }

    @Nested
    @DisplayName("Special Strategies")
    class SpecialStrategies {

        @Test
        @DisplayName("UNIVERSAL strategy exists")
        public void testUniversalStrategy() {
            FindStrategy strategy = FindStrategy.UNIVERSAL;
            assertNotNull(strategy);
            assertEquals("UNIVERSAL", strategy.name());
        }

        @Test
        @DisplayName("CUSTOM strategy exists")
        public void testCustomStrategy() {
            FindStrategy strategy = FindStrategy.CUSTOM;
            assertNotNull(strategy);
            assertEquals("CUSTOM", strategy.name());
        }
    }

    @Nested
    @DisplayName("Color-Based Strategies")
    class ColorBasedStrategies {

        @Test
        @DisplayName("COLOR strategy exists")
        public void testColorStrategy() {
            FindStrategy strategy = FindStrategy.COLOR;
            assertNotNull(strategy);
            assertEquals("COLOR", strategy.name());
        }
    }

    @Nested
    @DisplayName("Histogram-Based Strategies")
    class HistogramBasedStrategies {

        @Test
        @DisplayName("HISTOGRAM strategy exists")
        public void testHistogramStrategy() {
            FindStrategy strategy = FindStrategy.HISTOGRAM;
            assertNotNull(strategy);
            assertEquals("HISTOGRAM", strategy.name());
        }
    }

    @Nested
    @DisplayName("Motion-Based Strategies")
    class MotionBasedStrategies {

        @Test
        @DisplayName("MOTION strategy exists")
        public void testMotionStrategy() {
            FindStrategy strategy = FindStrategy.MOTION;
            assertNotNull(strategy);
            assertEquals("MOTION", strategy.name());
        }

        @Test
        @DisplayName("REGIONS_OF_MOTION strategy exists")
        public void testRegionsOfMotionStrategy() {
            FindStrategy strategy = FindStrategy.REGIONS_OF_MOTION;
            assertNotNull(strategy);
            assertEquals("REGIONS_OF_MOTION", strategy.name());
        }

        @Test
        @DisplayName("FIXED_PIXELS strategy exists")
        public void testFixedPixelsStrategy() {
            FindStrategy strategy = FindStrategy.FIXED_PIXELS;
            assertNotNull(strategy);
            assertEquals("FIXED_PIXELS", strategy.name());
        }

        @Test
        @DisplayName("DYNAMIC_PIXELS strategy exists")
        public void testDynamicPixelsStrategy() {
            FindStrategy strategy = FindStrategy.DYNAMIC_PIXELS;
            assertNotNull(strategy);
            assertEquals("DYNAMIC_PIXELS", strategy.name());
        }
    }

    @Nested
    @DisplayName("Text-Based Strategies")
    class TextBasedStrategies {

        @Test
        @DisplayName("ALL_WORDS strategy exists")
        public void testAllWordsStrategy() {
            FindStrategy strategy = FindStrategy.ALL_WORDS;
            assertNotNull(strategy);
            assertEquals("ALL_WORDS", strategy.name());
        }
    }

    @Nested
    @DisplayName("Image Comparison Strategies")
    class ImageComparisonStrategies {

        @Test
        @DisplayName("SIMILAR_IMAGES strategy exists")
        public void testSimilarImagesStrategy() {
            FindStrategy strategy = FindStrategy.SIMILAR_IMAGES;
            assertNotNull(strategy);
            assertEquals("SIMILAR_IMAGES", strategy.name());
        }
    }

    @Nested
    @DisplayName("State Analysis Strategies")
    class StateAnalysisStrategies {

        @Test
        @DisplayName("STATES strategy exists")
        public void testStatesStrategy() {
            FindStrategy strategy = FindStrategy.STATES;
            assertNotNull(strategy);
            assertEquals("STATES", strategy.name());
        }
    }

    @Nested
    @DisplayName("Enum Operations")
    class EnumOperations {

        @Test
        @DisplayName("valueOf returns correct strategy")
        public void testValueOf() {
            FindStrategy strategy = FindStrategy.valueOf("FIRST");
            assertEquals(FindStrategy.FIRST, strategy);

            FindStrategy motion = FindStrategy.valueOf("MOTION");
            assertEquals(FindStrategy.MOTION, motion);
        }

        @Test
        @DisplayName("valueOf throws for invalid name")
        public void testValueOfInvalid() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        FindStrategy.valueOf("INVALID_STRATEGY");
                    });
        }

        @Test
        @DisplayName("values returns all strategies")
        public void testValues() {
            FindStrategy[] strategies = FindStrategy.values();

            assertNotNull(strategies);
            assertTrue(strategies.length > 0);

            // Check specific strategies are included
            boolean hasFirst = false;
            boolean hasMotion = false;
            boolean hasColor = false;

            for (FindStrategy strategy : strategies) {
                if (strategy == FindStrategy.FIRST) hasFirst = true;
                if (strategy == FindStrategy.MOTION) hasMotion = true;
                if (strategy == FindStrategy.COLOR) hasColor = true;
            }

            assertTrue(hasFirst, "Should include FIRST");
            assertTrue(hasMotion, "Should include MOTION");
            assertTrue(hasColor, "Should include COLOR");
        }

        @Test
        @DisplayName("ordinal values are stable")
        public void testOrdinals() {
            // Ordinals should be consistent
            assertTrue(FindStrategy.FIRST.ordinal() >= 0);
            assertTrue(FindStrategy.EACH.ordinal() >= 0);
            assertTrue(FindStrategy.ALL.ordinal() >= 0);
            assertTrue(FindStrategy.BEST.ordinal() >= 0);

            // Different strategies have different ordinals
            assertNotEquals(FindStrategy.FIRST.ordinal(), FindStrategy.BEST.ordinal());
        }

        @ParameterizedTest
        @EnumSource(FindStrategy.class)
        @DisplayName("All strategies have non-null names")
        public void testAllStrategiesHaveNames(FindStrategy strategy) {
            assertNotNull(strategy);
            assertNotNull(strategy.name());
            assertFalse(strategy.name().isEmpty());
        }

        @ParameterizedTest
        @EnumSource(FindStrategy.class)
        @DisplayName("toString returns strategy name")
        public void testToString(FindStrategy strategy) {
            assertEquals(strategy.name(), strategy.toString());
        }
    }

    @Nested
    @DisplayName("Strategy Categories")
    class StrategyCategories {

        @Test
        @DisplayName("Pattern-based strategies group")
        public void testPatternBasedGroup() {
            FindStrategy[] patternBased = {
                FindStrategy.FIRST, FindStrategy.EACH, FindStrategy.ALL, FindStrategy.BEST
            };

            for (FindStrategy strategy : patternBased) {
                assertNotNull(strategy);
                // These are used by PatternFindOptions
            }
        }

        @Test
        @DisplayName("Motion detection strategies group")
        public void testMotionDetectionGroup() {
            FindStrategy[] motionBased = {
                FindStrategy.MOTION,
                FindStrategy.REGIONS_OF_MOTION,
                FindStrategy.FIXED_PIXELS,
                FindStrategy.DYNAMIC_PIXELS
            };

            for (FindStrategy strategy : motionBased) {
                assertNotNull(strategy);
                // These are used for motion detection
            }
        }

        @Test
        @DisplayName("Analysis strategies group")
        public void testAnalysisGroup() {
            FindStrategy[] analysisBased = {
                FindStrategy.COLOR,
                FindStrategy.HISTOGRAM,
                FindStrategy.SIMILAR_IMAGES,
                FindStrategy.STATES
            };

            for (FindStrategy strategy : analysisBased) {
                assertNotNull(strategy);
                // These are used for various analysis operations
            }
        }
    }

    @Nested
    @DisplayName("Compatibility")
    class Compatibility {

        @Test
        @DisplayName("Strategies compatible with PatternFindOptions")
        public void testPatternFindCompatibility() {
            // These strategies should work with PatternFindOptions
            PatternFindOptions first =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.FIRST)
                            .build();
            assertEquals(FindStrategy.FIRST, first.getFindStrategy());

            PatternFindOptions best =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.BEST)
                            .build();
            assertEquals(FindStrategy.BEST, best.getFindStrategy());

            PatternFindOptions all =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.ALL)
                            .build();
            assertEquals(FindStrategy.ALL, all.getFindStrategy());

            PatternFindOptions each =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.EACH)
                            .build();
            assertEquals(FindStrategy.EACH, each.getFindStrategy());
        }
    }
}
