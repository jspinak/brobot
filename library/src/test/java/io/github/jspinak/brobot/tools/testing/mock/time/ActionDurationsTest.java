package io.github.jspinak.brobot.tools.testing.mock.time;

import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Comprehensive tests for ActionDurations mock timing configuration.
 * Tests action duration mappings, strategy durations, and FrameworkSettings
 * integration.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ActionDurations Tests")
public class ActionDurationsTest extends BrobotTestBase {

    private ActionDurations actionDurations;

    // Store original FrameworkSettings values
    private static double originalMockTimeClick;
    private static double originalMockTimeDrag;
    private static double originalMockTimeMove;
    private static double originalMockTimeClassify;
    private static double originalMockTimeFindFirst;
    private static double originalMockTimeFindAll;

    @BeforeAll
    static void saveOriginalSettings() {
        originalMockTimeClick = FrameworkSettings.mockTimeClick;
        originalMockTimeDrag = FrameworkSettings.mockTimeDrag;
        originalMockTimeMove = FrameworkSettings.mockTimeMove;
        originalMockTimeClassify = FrameworkSettings.mockTimeClassify;
        originalMockTimeFindFirst = FrameworkSettings.mockTimeFindFirst;
        originalMockTimeFindAll = FrameworkSettings.mockTimeFindAll;
    }

    @AfterAll
    static void restoreOriginalSettings() {
        FrameworkSettings.mockTimeClick = originalMockTimeClick;
        FrameworkSettings.mockTimeDrag = originalMockTimeDrag;
        FrameworkSettings.mockTimeMove = originalMockTimeMove;
        FrameworkSettings.mockTimeClassify = originalMockTimeClassify;
        FrameworkSettings.mockTimeFindFirst = originalMockTimeFindFirst;
        FrameworkSettings.mockTimeFindAll = originalMockTimeFindAll;
    }

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Set known values for testing
        FrameworkSettings.mockTimeClick = 0.05;
        FrameworkSettings.mockTimeDrag = 0.3;
        FrameworkSettings.mockTimeMove = 0.1;
        FrameworkSettings.mockTimeClassify = 0.4;
        FrameworkSettings.mockTimeFindFirst = 0.1;
        FrameworkSettings.mockTimeFindAll = 0.2;

        actionDurations = new ActionDurations();
    }

    @Nested
    @DisplayName("ActionType Duration Tests")
    class ActionTypeDurationTests {

        @Test
        @DisplayName("Should return correct duration for CLICK action")
        void shouldReturnClickDuration() {
            double duration = actionDurations.getActionDuration(ActionType.CLICK);
            assertEquals(FrameworkSettings.mockTimeClick, duration);
            assertEquals(0.05, duration);
        }

        @Test
        @DisplayName("Should return correct duration for DRAG action")
        void shouldReturnDragDuration() {
            double duration = actionDurations.getActionDuration(ActionType.DRAG);
            assertEquals(FrameworkSettings.mockTimeDrag, duration);
            assertEquals(0.3, duration);
        }

        @Test
        @DisplayName("Should return correct duration for MOVE action")
        void shouldReturnMoveDuration() {
            double duration = actionDurations.getActionDuration(ActionType.MOVE);
            assertEquals(FrameworkSettings.mockTimeMove, duration);
            assertEquals(0.1, duration);
        }

        @Test
        @DisplayName("Should return correct duration for CLASSIFY action")
        void shouldReturnClassifyDuration() {
            double duration = actionDurations.getActionDuration(ActionType.CLASSIFY);
            assertEquals(FrameworkSettings.mockTimeClassify, duration);
            assertEquals(0.4, duration);
        }

        @Test
        @DisplayName("Should return 0.0 for unmapped action types")
        void shouldReturnZeroForUnmappedActionTypes() {
            // Test action types that aren't in the map
            ActionType[] unmappedTypes = {
                    ActionType.FIND,
                    ActionType.TYPE,
                    ActionType.VANISH,
                    ActionType.HIGHLIGHT,
                    ActionType.SCROLL_DOWN,
                    ActionType.DEFINE
            };

            for (ActionType actionType : unmappedTypes) {
                double duration = actionDurations.getActionDuration(actionType);
                assertEquals(0.0, duration,
                        actionType + " should return 0.0 when not mapped");
            }
        }

        @Test
        @DisplayName("Should handle null action type")
        void shouldHandleNullActionType() {
            double duration = actionDurations.getActionDuration(null);
            assertEquals(0.0, duration, "Null action type should return 0.0");
        }

        @ParameterizedTest
        @EnumSource(ActionType.class)
        @DisplayName("Should return non-negative duration for all action types")
        void shouldReturnNonNegativeDurationForAllTypes(ActionType actionType) {
            double duration = actionDurations.getActionDuration(actionType);
            assertTrue(duration >= 0,
                    actionType + " should have non-negative duration");
        }
    }

    @Nested
    @DisplayName("Strategy Duration Tests")
    class StrategyDurationTests {

        @Test
        @DisplayName("Should return correct duration for FIRST strategy")
        void shouldReturnFirstStrategyDuration() {
            double duration = actionDurations.getFindStrategyDuration(
                    PatternFindOptions.Strategy.FIRST);
            assertEquals(FrameworkSettings.mockTimeFindFirst, duration);
            assertEquals(0.1, duration);
        }

        @Test
        @DisplayName("Should return correct duration for EACH strategy")
        void shouldReturnEachStrategyDuration() {
            double duration = actionDurations.getFindStrategyDuration(
                    PatternFindOptions.Strategy.EACH);
            assertEquals(FrameworkSettings.mockTimeFindFirst, duration);
            assertEquals(0.1, duration);
        }

        @Test
        @DisplayName("Should return correct duration for ALL strategy")
        void shouldReturnAllStrategyDuration() {
            double duration = actionDurations.getFindStrategyDuration(
                    PatternFindOptions.Strategy.ALL);
            assertEquals(FrameworkSettings.mockTimeFindAll, duration);
            assertEquals(0.2, duration);
        }

        @Test
        @DisplayName("Should return correct duration for BEST strategy")
        void shouldReturnBestStrategyDuration() {
            double duration = actionDurations.getFindStrategyDuration(
                    PatternFindOptions.Strategy.BEST);
            assertEquals(FrameworkSettings.mockTimeFindAll, duration);
            assertEquals(0.2, duration);
        }

        @Test
        @DisplayName("Should return 0.0 for null strategy")
        void shouldReturnZeroForNullStrategy() {
            double duration = actionDurations.getFindStrategyDuration(null);
            assertEquals(0.0, duration, "Null strategy should return 0.0");
        }

        @ParameterizedTest
        @EnumSource(PatternFindOptions.Strategy.class)
        @DisplayName("Should return non-negative duration for all strategies")
        void shouldReturnNonNegativeDurationForAllStrategies(
                PatternFindOptions.Strategy strategy) {
            double duration = actionDurations.getFindStrategyDuration(strategy);
            assertTrue(duration >= 0,
                    strategy + " should have non-negative duration");
        }

        @Test
        @DisplayName("Should map FIRST and EACH to same duration")
        void shouldMapFirstAndEachToSameDuration() {
            double firstDuration = actionDurations.getFindStrategyDuration(
                    PatternFindOptions.Strategy.FIRST);
            double eachDuration = actionDurations.getFindStrategyDuration(
                    PatternFindOptions.Strategy.EACH);

            assertEquals(firstDuration, eachDuration,
                    "FIRST and EACH should use same duration");
        }

        @Test
        @DisplayName("Should map ALL and BEST to same duration")
        void shouldMapAllAndBestToSameDuration() {
            double allDuration = actionDurations.getFindStrategyDuration(
                    PatternFindOptions.Strategy.ALL);
            double bestDuration = actionDurations.getFindStrategyDuration(
                    PatternFindOptions.Strategy.BEST);

            assertEquals(allDuration, bestDuration,
                    "ALL and BEST should use same duration");
        }
    }

    @Nested
    @DisplayName("FrameworkSettings Integration")
    class FrameworkSettingsIntegration {

        @Test
        @DisplayName("Should use FrameworkSettings values at initialization")
        void shouldUseFrameworkSettingsValuesAtInitialization() {
            // Set custom values
            FrameworkSettings.mockTimeClick = 1.5;
            FrameworkSettings.mockTimeDrag = 2.5;
            FrameworkSettings.mockTimeMove = 3.5;
            FrameworkSettings.mockTimeClassify = 4.5;

            // Create new instance
            ActionDurations newDurations = new ActionDurations();

            // Verify it uses the custom values
            assertEquals(1.5, newDurations.getActionDuration(ActionType.CLICK));
            assertEquals(2.5, newDurations.getActionDuration(ActionType.DRAG));
            assertEquals(3.5, newDurations.getActionDuration(ActionType.MOVE));
            assertEquals(4.5, newDurations.getActionDuration(ActionType.CLASSIFY));
        }

        @Test
        @DisplayName("Should use FrameworkSettings find durations")
        void shouldUseFrameworkSettingsFindDurations() {
            // Set custom values
            FrameworkSettings.mockTimeFindFirst = 5.5;
            FrameworkSettings.mockTimeFindAll = 6.5;

            // Create new instance
            ActionDurations newDurations = new ActionDurations();

            // Verify find strategies use the custom values
            assertEquals(5.5, newDurations.getFindStrategyDuration(
                    PatternFindOptions.Strategy.FIRST));
            assertEquals(5.5, newDurations.getFindStrategyDuration(
                    PatternFindOptions.Strategy.EACH));
            assertEquals(6.5, newDurations.getFindStrategyDuration(
                    PatternFindOptions.Strategy.ALL));
            assertEquals(6.5, newDurations.getFindStrategyDuration(
                    PatternFindOptions.Strategy.BEST));
        }

        @Test
        @DisplayName("Should not be affected by later FrameworkSettings changes")
        void shouldNotBeAffectedByLaterFrameworkSettingsChanges() {
            // Get initial durations
            double initialClick = actionDurations.getActionDuration(ActionType.CLICK);

            // Change FrameworkSettings after creation
            FrameworkSettings.mockTimeClick = 99.9;

            // Duration should remain unchanged
            double afterChangeClick = actionDurations.getActionDuration(ActionType.CLICK);
            assertEquals(initialClick, afterChangeClick,
                    "Duration should not change after FrameworkSettings modification");
            assertNotEquals(99.9, afterChangeClick);
        }
    }

    @Nested
    @DisplayName("Map Initialization Tests")
    class MapInitializationTests {

        @Test
        @DisplayName("Should initialize action type map correctly")
        void shouldInitializeActionTypeMap() throws Exception {
            // Use reflection to access private map
            Field mapField = ActionDurations.class.getDeclaredField("actionTypeDurations");
            mapField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<ActionType, Double> map = (Map<ActionType, Double>) mapField.get(actionDurations);

            assertNotNull(map);
            assertEquals(4, map.size(), "Should have exactly 4 mapped action types");
            assertTrue(map.containsKey(ActionType.CLICK));
            assertTrue(map.containsKey(ActionType.DRAG));
            assertTrue(map.containsKey(ActionType.MOVE));
            assertTrue(map.containsKey(ActionType.CLASSIFY));
        }

        @Test
        @DisplayName("Should initialize strategy map correctly")
        void shouldInitializeStrategyMap() throws Exception {
            // Use reflection to access private map
            Field mapField = ActionDurations.class.getDeclaredField("strategyDurations");
            mapField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<PatternFindOptions.Strategy, Double> map = (Map<PatternFindOptions.Strategy, Double>) mapField
                    .get(actionDurations);

            assertNotNull(map);
            assertEquals(4, map.size(), "Should have exactly 4 mapped strategies");
            assertTrue(map.containsKey(PatternFindOptions.Strategy.FIRST));
            assertTrue(map.containsKey(PatternFindOptions.Strategy.EACH));
            assertTrue(map.containsKey(PatternFindOptions.Strategy.ALL));
            assertTrue(map.containsKey(PatternFindOptions.Strategy.BEST));
        }

        @Test
        @DisplayName("Should have consistent initialization order")
        void shouldHaveConsistentInitializationOrder() {
            // Create multiple instances and verify consistency
            ActionDurations durations1 = new ActionDurations();
            ActionDurations durations2 = new ActionDurations();
            ActionDurations durations3 = new ActionDurations();

            // All should return same values
            assertEquals(
                    durations1.getActionDuration(ActionType.CLICK),
                    durations2.getActionDuration(ActionType.CLICK));
            assertEquals(
                    durations2.getActionDuration(ActionType.CLICK),
                    durations3.getActionDuration(ActionType.CLICK));
        }
    }

    @Nested
    @DisplayName("Component Annotation Tests")
    class ComponentAnnotationTests {

        @Test
        @DisplayName("Should be annotated as Spring Component")
        void shouldBeAnnotatedAsSpringComponent() {
            assertTrue(ActionDurations.class.isAnnotationPresent(
                    org.springframework.stereotype.Component.class),
                    "ActionDurations should be annotated with @Component");
        }

        @Test
        @DisplayName("Should be suitable for dependency injection")
        void shouldBeSuitableForDependencyInjection() {
            // Verify it has a no-args constructor (required for Spring)
            try {
                ActionDurations.class.getDeclaredConstructor();
                assertTrue(true, "Has no-args constructor");
            } catch (NoSuchMethodException e) {
                fail("ActionDurations should have a no-args constructor for Spring");
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundaries")
    class EdgeCasesAndBoundaries {

        @Test
        @DisplayName("Should handle zero duration values")
        void shouldHandleZeroDurationValues() {
            FrameworkSettings.mockTimeClick = 0.0;
            ActionDurations zeroDurations = new ActionDurations();

            double duration = zeroDurations.getActionDuration(ActionType.CLICK);
            assertEquals(0.0, duration);
        }

        @Test
        @DisplayName("Should handle negative duration values from FrameworkSettings")
        void shouldHandleNegativeDurationValues() {
            // This shouldn't happen in practice, but test defensive behavior
            FrameworkSettings.mockTimeClick = -1.0;
            ActionDurations negativeDurations = new ActionDurations();

            double duration = negativeDurations.getActionDuration(ActionType.CLICK);
            assertEquals(-1.0, duration,
                    "Should pass through negative values without modification");
        }

        @Test
        @DisplayName("Should handle very large duration values")
        void shouldHandleVeryLargeDurationValues() {
            FrameworkSettings.mockTimeClick = Double.MAX_VALUE;
            ActionDurations largeDurations = new ActionDurations();

            double duration = largeDurations.getActionDuration(ActionType.CLICK);
            assertEquals(Double.MAX_VALUE, duration);
        }

        @Test
        @DisplayName("Should handle NaN duration values")
        void shouldHandleNaNDurationValues() {
            FrameworkSettings.mockTimeClick = Double.NaN;
            ActionDurations nanDurations = new ActionDurations();

            double duration = nanDurations.getActionDuration(ActionType.CLICK);
            assertTrue(Double.isNaN(duration));
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle rapid lookups efficiently")
        void shouldHandleRapidLookupsEfficiently() {
            int lookupCount = 1_000_000;

            long startTime = System.nanoTime();

            for (int i = 0; i < lookupCount; i++) {
                actionDurations.getActionDuration(ActionType.CLICK);
            }

            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;

            assertTrue(durationMs < 100,
                    "Should handle 1M lookups in less than 100ms");
        }

        @Test
        @DisplayName("Should handle mixed lookups efficiently")
        void shouldHandleMixedLookupsEfficiently() {
            ActionType[] types = ActionType.values();
            PatternFindOptions.Strategy[] strategies = PatternFindOptions.Strategy.values();

            long startTime = System.nanoTime();

            for (int i = 0; i < 100_000; i++) {
                ActionType type = types[i % types.length];
                actionDurations.getActionDuration(type);

                PatternFindOptions.Strategy strategy = strategies[i % strategies.length];
                actionDurations.getFindStrategyDuration(strategy);
            }

            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;

            assertTrue(durationMs < 100,
                    "Should handle mixed lookups efficiently");
        }
    }

    @Nested
    @DisplayName("Completeness Tests")
    class CompletenessTests {

        @Test
        @DisplayName("Should document which ActionTypes are supported")
        void shouldDocumentSupportedActionTypes() {
            Set<ActionType> supportedTypes = new HashSet<>();

            for (ActionType type : ActionType.values()) {
                if (actionDurations.getActionDuration(type) > 0) {
                    supportedTypes.add(type);
                }
            }

            // Document which types have durations
            assertThat(supportedTypes, hasItems(
                    ActionType.CLICK,
                    ActionType.DRAG,
                    ActionType.MOVE,
                    ActionType.CLASSIFY));

            // These should NOT have durations (return 0.0)
            assertThat(supportedTypes, not(hasItems(
                    ActionType.FIND,
                    ActionType.TYPE,
                    ActionType.VANISH)));
        }

        @Test
        @DisplayName("Should cover all PatternFindOptions strategies")
        void shouldCoverAllStrategies() {
            // All strategies should have a duration (even if 0)
            for (PatternFindOptions.Strategy strategy : PatternFindOptions.Strategy.values()) {
                double duration = actionDurations.getFindStrategyDuration(strategy);
                assertTrue(duration >= 0,
                        strategy + " should have non-negative duration");
            }
        }
    }
}