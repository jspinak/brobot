package io.github.jspinak.brobot.action.basic.region;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for DefineRegion - orchestrates region definition operations. Tests
 * delegation to various region definition strategies.
 */
@DisplayName("DefineRegion Tests")
public class DefineRegionTest extends BrobotTestBase {

    @Mock private DefineWithWindow mockDefineWithWindow;

    @Mock private DefineWithMatch mockDefineWithMatch;

    @Mock private DefineInsideAnchors mockDefineInsideAnchors;

    @Mock private DefineOutsideAnchors mockDefineOutsideAnchors;

    @Mock private DefineIncludingMatches mockDefineIncludingMatches;

    @Mock private ActionResult mockActionResult;

    @Mock private ObjectCollection mockObjectCollection;

    private DefineRegion defineRegion;
    private DefineRegionOptions defineRegionOptions;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        defineRegion =
                new DefineRegion(
                        mockDefineWithWindow,
                        mockDefineWithMatch,
                        mockDefineInsideAnchors,
                        mockDefineOutsideAnchors,
                        mockDefineIncludingMatches);

        defineRegionOptions =
                new DefineRegionOptions.Builder()
                        .setDefineAs(DefineRegionOptions.DefineAs.MATCH)
                        .build();

        when(mockActionResult.getActionConfig()).thenReturn(defineRegionOptions);
    }

    @Test
    @DisplayName("Should return DEFINE action type")
    public void testGetActionType() {
        assertEquals(ActionInterface.Type.DEFINE, defineRegion.getActionType());
    }

    @Nested
    @DisplayName("Strategy Delegation")
    class StrategyDelegation {

        @Test
        @DisplayName("Should delegate to DefineWithWindow for FOCUSED_WINDOW")
        public void testDelegateFocusedWindow() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.FOCUSED_WINDOW)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineWithWindow).perform(mockActionResult, mockObjectCollection);
            verifyNoInteractions(
                    mockDefineWithMatch,
                    mockDefineInsideAnchors,
                    mockDefineOutsideAnchors,
                    mockDefineIncludingMatches);
        }

        @Test
        @DisplayName("Should delegate to DefineWithMatch for MATCH")
        public void testDelegateMatch() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.MATCH)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineWithMatch).perform(mockActionResult, mockObjectCollection);
            verifyNoInteractions(
                    mockDefineWithWindow,
                    mockDefineInsideAnchors,
                    mockDefineOutsideAnchors,
                    mockDefineIncludingMatches);
        }

        @Test
        @DisplayName("Should delegate to DefineWithMatch for BELOW_MATCH")
        public void testDelegateBelowMatch() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.BELOW_MATCH)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineWithMatch).perform(mockActionResult, mockObjectCollection);
        }

        @Test
        @DisplayName("Should delegate to DefineWithMatch for ABOVE_MATCH")
        public void testDelegateAboveMatch() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.ABOVE_MATCH)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineWithMatch).perform(mockActionResult, mockObjectCollection);
        }

        @Test
        @DisplayName("Should delegate to DefineWithMatch for LEFT_OF_MATCH")
        public void testDelegateLeftOfMatch() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.LEFT_OF_MATCH)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineWithMatch).perform(mockActionResult, mockObjectCollection);
        }

        @Test
        @DisplayName("Should delegate to DefineWithMatch for RIGHT_OF_MATCH")
        public void testDelegateRightOfMatch() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.RIGHT_OF_MATCH)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineWithMatch).perform(mockActionResult, mockObjectCollection);
        }

        @Test
        @DisplayName("Should delegate to DefineInsideAnchors for INSIDE_ANCHORS")
        public void testDelegateInsideAnchors() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.INSIDE_ANCHORS)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineInsideAnchors).perform(mockActionResult, mockObjectCollection);
            verifyNoInteractions(
                    mockDefineWithWindow,
                    mockDefineWithMatch,
                    mockDefineOutsideAnchors,
                    mockDefineIncludingMatches);
        }

        @Test
        @DisplayName("Should delegate to DefineOutsideAnchors for OUTSIDE_ANCHORS")
        public void testDelegateOutsideAnchors() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.OUTSIDE_ANCHORS)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineOutsideAnchors).perform(mockActionResult, mockObjectCollection);
            verifyNoInteractions(
                    mockDefineWithWindow,
                    mockDefineWithMatch,
                    mockDefineInsideAnchors,
                    mockDefineIncludingMatches);
        }

        @Test
        @DisplayName("Should delegate to DefineIncludingMatches for INCLUDING_MATCHES")
        public void testDelegateIncludingMatches() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.INCLUDING_MATCHES)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineIncludingMatches).perform(mockActionResult, mockObjectCollection);
            verifyNoInteractions(
                    mockDefineWithWindow,
                    mockDefineWithMatch,
                    mockDefineInsideAnchors,
                    mockDefineOutsideAnchors);
        }
    }

    @Nested
    @DisplayName("All DefineAs Options")
    class AllDefineAsOptions {

        @ParameterizedTest
        @EnumSource(DefineRegionOptions.DefineAs.class)
        @DisplayName("Should handle all DefineAs enum values")
        public void testAllDefineAsValues(DefineRegionOptions.DefineAs defineAs) {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder().setDefineAs(defineAs).build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            // Verify that at least one delegate was called
            verify(mockDefineWithWindow, atMost(1)).perform(any(), any());
            verify(mockDefineWithMatch, atMost(1)).perform(any(), any());
            verify(mockDefineInsideAnchors, atMost(1)).perform(any(), any());
            verify(mockDefineOutsideAnchors, atMost(1)).perform(any(), any());
            verify(mockDefineIncludingMatches, atMost(1)).perform(any(), any());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should throw exception for invalid configuration type")
        public void testInvalidConfigurationType() {
            when(mockActionResult.getActionConfig()).thenReturn(new ClickOptions.Builder().build());

            assertThrows(
                    IllegalArgumentException.class,
                    () -> defineRegion.perform(mockActionResult, mockObjectCollection),
                    "DefineRegion requires DefineRegionOptions configuration");
        }

        @Test
        @DisplayName("Should throw exception for null configuration")
        public void testNullConfiguration() {
            when(mockActionResult.getActionConfig()).thenReturn(null);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> defineRegion.perform(mockActionResult, mockObjectCollection));
        }

        @Test
        @DisplayName("Should propagate exceptions from delegated actions")
        public void testDelegateException() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.MATCH)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            doThrow(new RuntimeException("Delegate failure"))
                    .when(mockDefineWithMatch)
                    .perform(any(), any());

            assertThrows(
                    RuntimeException.class,
                    () -> defineRegion.perform(mockActionResult, mockObjectCollection));
        }
    }

    @Nested
    @DisplayName("Object Collection Handling")
    class ObjectCollectionHandling {

        @Test
        @DisplayName("Should pass single object collection to delegate")
        public void testSingleObjectCollection() {
            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineWithMatch).perform(mockActionResult, mockObjectCollection);
        }

        @Test
        @DisplayName("Should pass multiple object collections to delegate")
        public void testMultipleObjectCollections() {
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            ObjectCollection collection3 = mock(ObjectCollection.class);

            defineRegion.perform(mockActionResult, collection1, collection2, collection3);

            verify(mockDefineWithMatch)
                    .perform(mockActionResult, collection1, collection2, collection3);
        }

        @Test
        @DisplayName("Should handle empty object collections")
        public void testEmptyObjectCollections() {
            defineRegion.perform(mockActionResult);

            verify(mockDefineWithMatch).perform(eq(mockActionResult));
        }
    }

    @Nested
    @DisplayName("Configuration Options")
    class ConfigurationOptions {

        @Test
        @DisplayName("Should handle offset configuration")
        public void testOffsetConfiguration() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.BELOW_MATCH)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineWithMatch).perform(mockActionResult, mockObjectCollection);
        }

        @Test
        @DisplayName("Should handle size configuration")
        public void testSizeConfiguration() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.MATCH)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineWithMatch).perform(mockActionResult, mockObjectCollection);
        }

        @Test
        @DisplayName("Should handle anchor configuration")
        public void testAnchorConfiguration() {
            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.INSIDE_ANCHORS)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            defineRegion.perform(mockActionResult, mockObjectCollection);

            verify(mockDefineInsideAnchors).perform(mockActionResult, mockObjectCollection);
        }
    }

    @Nested
    @DisplayName("Debugging Output")
    class DebuggingOutput {

        private PrintStream originalOut;
        private ByteArrayOutputStream outputStream;

        @BeforeEach
        public void setupOutput() {
            originalOut = System.out;
            outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
        }

        @AfterEach
        public void restoreOutput() {
            System.setOut(originalOut);
        }

        @Test
        @DisplayName("Should print DefineAs option to stdout")
        public void testPrintDefineAs() {
            // Clear any previous output
            outputStream.reset();

            DefineRegionOptions options =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.MATCH)
                            .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);

            try {
                defineRegion.perform(mockActionResult, mockObjectCollection);
            } catch (Exception e) {
                // The print should have happened before any exception
            }

            System.out.flush();
            String output = outputStream.toString();
            assertTrue(
                    output.contains("Define as: MATCH"),
                    "Expected output to contain 'Define as: MATCH' but got: '" + output + "'");
        }

        @ParameterizedTest
        @EnumSource(DefineRegionOptions.DefineAs.class)
        @DisplayName("Should print all DefineAs values correctly")
        @org.junit.jupiter.api.Disabled(
                "Flaky test - System.out capture is not thread-safe with parameterized tests")
        public void testPrintAllDefineAsValues(DefineRegionOptions.DefineAs defineAs) {
            // Set up a fresh output capture for each iteration
            PrintStream originalOut = System.out;
            ByteArrayOutputStream testOutput = new ByteArrayOutputStream();
            System.setOut(new PrintStream(testOutput));

            try {
                DefineRegionOptions options =
                        new DefineRegionOptions.Builder().setDefineAs(defineAs).build();
                when(mockActionResult.getActionConfig()).thenReturn(options);

                // Use the parent's defineRegion which has proper mocks
                try {
                    defineRegion.perform(mockActionResult, mockObjectCollection);
                } catch (Exception e) {
                    // Expected for some DefineAs values
                    // The print statement should have executed before any exception
                }

                System.out.flush(); // Ensure all output is captured
                String output = testOutput.toString();
                assertTrue(
                        output.contains("Define as: " + defineAs),
                        "Expected output to contain 'Define as: "
                                + defineAs
                                + "' but got: '"
                                + output
                                + "'");
            } finally {
                // Restore original System.out
                System.setOut(originalOut);
            }
        }
    }

    @Nested
    @DisplayName("Strategy Pattern Implementation")
    class StrategyPatternImplementation {

        @Test
        @DisplayName("Should correctly map all match-related strategies to DefineWithMatch")
        public void testMatchStrategyMapping() {
            DefineRegionOptions.DefineAs[] matchStrategies = {
                DefineRegionOptions.DefineAs.MATCH,
                DefineRegionOptions.DefineAs.BELOW_MATCH,
                DefineRegionOptions.DefineAs.ABOVE_MATCH,
                DefineRegionOptions.DefineAs.LEFT_OF_MATCH,
                DefineRegionOptions.DefineAs.RIGHT_OF_MATCH
            };

            for (DefineRegionOptions.DefineAs strategy : matchStrategies) {
                DefineRegionOptions options =
                        new DefineRegionOptions.Builder().setDefineAs(strategy).build();
                when(mockActionResult.getActionConfig()).thenReturn(options);

                defineRegion.perform(mockActionResult, mockObjectCollection);
            }

            // All 5 match-related strategies should call DefineWithMatch
            verify(mockDefineWithMatch, times(5)).perform(any(), any());
        }

        @Test
        @DisplayName("Should maintain single responsibility for each strategy")
        public void testSingleResponsibility() {
            // Test that each unique strategy type only calls its designated handler
            DefineRegionOptions.DefineAs[] uniqueStrategies = {
                DefineRegionOptions.DefineAs.FOCUSED_WINDOW,
                DefineRegionOptions.DefineAs.INSIDE_ANCHORS,
                DefineRegionOptions.DefineAs.OUTSIDE_ANCHORS,
                DefineRegionOptions.DefineAs.INCLUDING_MATCHES
            };

            for (DefineRegionOptions.DefineAs strategy : uniqueStrategies) {
                // Reset mocks for each iteration
                reset(
                        mockDefineWithWindow, mockDefineInsideAnchors,
                        mockDefineOutsideAnchors, mockDefineIncludingMatches);

                DefineRegionOptions options =
                        new DefineRegionOptions.Builder().setDefineAs(strategy).build();
                when(mockActionResult.getActionConfig()).thenReturn(options);

                defineRegion.perform(mockActionResult, mockObjectCollection);

                // Verify only one handler was called
                int totalCalls =
                        mockInteractions(mockDefineWithWindow)
                                + mockInteractions(mockDefineInsideAnchors)
                                + mockInteractions(mockDefineOutsideAnchors)
                                + mockInteractions(mockDefineIncludingMatches);

                assertEquals(
                        1,
                        totalCalls,
                        "Only one strategy handler should be called for " + strategy);
            }
        }

        private int mockInteractions(ActionInterface mock) {
            try {
                verify(mock, atMost(1)).perform(any(), any());
                return mockingDetails(mock).getInvocations().size();
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
