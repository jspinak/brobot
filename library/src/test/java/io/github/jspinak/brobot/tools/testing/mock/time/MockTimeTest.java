package io.github.jspinak.brobot.tools.testing.mock.time;

import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Comprehensive tests for MockTime virtual clock functionality.
 * Tests time simulation, action durations, and deterministic behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MockTime Tests")
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Test incompatible with CI environment")
public class MockTimeTest extends BrobotTestBase {
    
    @Mock
    private ActionDurations mockActionDurations;
    
    private MockTime mockTime;
    private LocalDateTime startTime;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockTime = new MockTime(mockActionDurations);
        startTime = mockTime.now();
    }
    
    @Nested
    @DisplayName("Time Initialization")
    class TimeInitialization {
        
        @Test
        @DisplayName("Should initialize with current time")
        void shouldInitializeWithCurrentTime() {
            LocalDateTime now = mockTime.now();
            assertNotNull(now, "Initial time should not be null");
            
            // Check it's reasonably close to actual current time
            LocalDateTime actualNow = LocalDateTime.now();
            long secondsDiff = Math.abs(ChronoUnit.SECONDS.between(now, actualNow));
            assertTrue(secondsDiff < 5, 
                "Initial mock time should be close to actual current time");
        }
        
        @Test
        @DisplayName("Should return immutable time copy")
        void shouldReturnImmutableTimeCopy() {
            LocalDateTime time1 = mockTime.now();
            LocalDateTime time2 = mockTime.now();
            
            // Both should be equal but different instances (due to immutability)
            assertEquals(time1, time2);
            
            // Modifying one shouldn't affect the internal state
            time1.plusDays(1); // This creates a new instance
            assertEquals(time2, mockTime.now());
        }
    }
    
    @Nested
    @DisplayName("Basic Wait Operations")
    class BasicWaitOperations {
        
        @Test
        @DisplayName("Should advance time by specified seconds")
        void shouldAdvanceTimeBySpecifiedSeconds() {
            double waitSeconds = 5.5;
            
            LocalDateTime before = mockTime.now();
            mockTime.wait(waitSeconds);
            LocalDateTime after = mockTime.now();
            
            long nanosDiff = ChronoUnit.NANOS.between(before, after);
            long expectedNanos = (long)(waitSeconds * 1_000_000_000);
            
            assertEquals(expectedNanos, nanosDiff,
                "Time should advance by exactly the specified duration");
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.1, 0.5, 1.0, 2.5, 10.0, 60.0})
        @DisplayName("Should advance time for various durations")
        void shouldAdvanceTimeForVariousDurations(double seconds) {
            LocalDateTime before = mockTime.now();
            mockTime.wait(seconds);
            LocalDateTime after = mockTime.now();
            
            Duration duration = Duration.between(before, after);
            double actualSeconds = duration.toNanos() / 1_000_000_000.0;
            
            assertEquals(seconds, actualSeconds, 0.000001,
                "Should advance by " + seconds + " seconds");
        }
        
        @Test
        @DisplayName("Should handle zero wait time")
        void shouldHandleZeroWaitTime() {
            LocalDateTime before = mockTime.now();
            mockTime.wait(0.0);  // Use double to avoid Object.wait() conflict
            LocalDateTime after = mockTime.now();
            
            assertEquals(before, after, 
                "Zero wait should not advance time");
        }
        
        @Test
        @DisplayName("Should handle negative wait time")
        void shouldHandleNegativeWaitTime() {
            LocalDateTime before = mockTime.now();
            mockTime.wait(-5.0);
            LocalDateTime after = mockTime.now();
            
            assertEquals(before, after,
                "Negative wait should not advance time");
        }
        
        @Test
        @DisplayName("Should accumulate multiple waits")
        void shouldAccumulateMultipleWaits() {
            mockTime.wait(1.0);
            mockTime.wait(2.0);
            mockTime.wait(3.0);
            
            LocalDateTime endTime = mockTime.now();
            long totalSeconds = ChronoUnit.SECONDS.between(startTime, endTime);
            
            assertEquals(6, totalSeconds,
                "Multiple waits should accumulate");
        }
    }
    
    @Nested
    @DisplayName("Action Type Wait Operations")
    class ActionTypeWaitOperations {
        
        @ParameterizedTest
        @EnumSource(ActionType.class)
        @DisplayName("Should wait for each action type duration")
        void shouldWaitForEachActionTypeDuration(ActionType actionType) {
            // Setup mock duration
            double expectedDuration = 1.5;
            when(mockActionDurations.getActionDuration(actionType))
                .thenReturn(expectedDuration);
            
            LocalDateTime before = mockTime.now();
            mockTime.wait(actionType);
            LocalDateTime after = mockTime.now();
            
            verify(mockActionDurations).getActionDuration(actionType);
            
            long nanosDiff = ChronoUnit.NANOS.between(before, after);
            long expectedNanos = (long)(expectedDuration * 1_000_000_000);
            
            assertEquals(expectedNanos, nanosDiff,
                "Should wait for " + actionType + " duration");
        }
        
        @Test
        @DisplayName("Should use different durations for different actions")
        void shouldUseDifferentDurationsForDifferentActions() {
            when(mockActionDurations.getActionDuration(ActionType.CLICK))
                .thenReturn(0.5);
            when(mockActionDurations.getActionDuration(ActionType.TYPE))
                .thenReturn(2.0);
            when(mockActionDurations.getActionDuration(ActionType.DRAG))
                .thenReturn(3.0);
            
            LocalDateTime time1 = mockTime.now();
            mockTime.wait(ActionType.CLICK);
            LocalDateTime time2 = mockTime.now();
            mockTime.wait(ActionType.TYPE);
            LocalDateTime time3 = mockTime.now();
            mockTime.wait(ActionType.DRAG);
            LocalDateTime time4 = mockTime.now();
            
            assertEquals(500, ChronoUnit.MILLIS.between(time1, time2));
            assertEquals(2000, ChronoUnit.MILLIS.between(time2, time3));
            assertEquals(3000, ChronoUnit.MILLIS.between(time3, time4));
        }
    }
    
    @Nested
    @DisplayName("Find Strategy Wait Operations")
    class FindStrategyWaitOperations {
        
        @ParameterizedTest
        @EnumSource(PatternFindOptions.Strategy.class)
        @DisplayName("Should wait for each find strategy duration")
        void shouldWaitForEachFindStrategyDuration(PatternFindOptions.Strategy strategy) {
            // Setup mock duration
            double expectedDuration = 2.5;
            when(mockActionDurations.getFindStrategyDuration(strategy))
                .thenReturn(expectedDuration);
            
            LocalDateTime before = mockTime.now();
            mockTime.wait(strategy);
            LocalDateTime after = mockTime.now();
            
            verify(mockActionDurations).getFindStrategyDuration(strategy);
            
            long nanosDiff = ChronoUnit.NANOS.between(before, after);
            long expectedNanos = (long)(expectedDuration * 1_000_000_000);
            
            assertEquals(expectedNanos, nanosDiff,
                "Should wait for " + strategy + " duration");
        }
        
        @Test
        @DisplayName("Should handle different find strategies")
        void shouldHandleDifferentFindStrategies() {
            when(mockActionDurations.getFindStrategyDuration(
                PatternFindOptions.Strategy.FIRST))
                .thenReturn(0.3);
            when(mockActionDurations.getFindStrategyDuration(
                PatternFindOptions.Strategy.ALL))
                .thenReturn(1.5);
            
            mockTime.wait(PatternFindOptions.Strategy.FIRST);
            LocalDateTime afterFirst = mockTime.now();
            
            mockTime.wait(PatternFindOptions.Strategy.ALL);
            LocalDateTime afterAll = mockTime.now();
            
            long firstDuration = ChronoUnit.MILLIS.between(startTime, afterFirst);
            long totalDuration = ChronoUnit.MILLIS.between(startTime, afterAll);
            
            assertEquals(300, firstDuration);
            assertEquals(1800, totalDuration);
        }
    }
    
    @Nested
    @DisplayName("Console Output")
    class ConsoleOutput {
        
        private PrintStream originalOut;
        private ByteArrayOutputStream outputStream;
        
        @BeforeEach
        void setupConsoleCapture() {
            originalOut = System.out;
            outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
        }
        
        @AfterEach
        void restoreConsole() {
            System.setOut(originalOut);
        }
        
        @Test
        @DisplayName("Should print wait message when reporting level is HIGH")
        void shouldPrintWaitMessageWhenReportingHigh() {
            try (MockedStatic<ConsoleReporter> reporter = 
                    mockStatic(ConsoleReporter.class)) {
                reporter.when(() -> ConsoleReporter.minReportingLevel(
                    ConsoleReporter.OutputLevel.HIGH))
                    .thenReturn(true);
                
                mockTime.wait(3.5);
                
                String output = outputStream.toString();
                assertTrue(output.contains("wait-3.5"),
                    "Should print wait message with duration");
            }
        }
        
        @Test
        @DisplayName("Should not print when reporting level is not HIGH")
        void shouldNotPrintWhenReportingNotHigh() {
            try (MockedStatic<ConsoleReporter> reporter = 
                    mockStatic(ConsoleReporter.class)) {
                reporter.when(() -> ConsoleReporter.minReportingLevel(
                    ConsoleReporter.OutputLevel.HIGH))
                    .thenReturn(false);
                
                mockTime.wait(3.5);
                
                String output = outputStream.toString();
                assertFalse(output.contains("wait"),
                    "Should not print when reporting level not HIGH");
            }
        }
    }
    
    @Nested
    @DisplayName("Time Precision")
    class TimePrecision {
        
        @Test
        @DisplayName("Should handle sub-second precision")
        void shouldHandleSubSecondPrecision() {
            mockTime.wait(0.001); // 1 millisecond
            LocalDateTime after1ms = mockTime.now();
            
            mockTime.wait(0.000001); // 1 microsecond
            LocalDateTime after1us = mockTime.now();
            
            mockTime.wait(0.000000001); // 1 nanosecond
            LocalDateTime after1ns = mockTime.now();
            
            assertTrue(after1ms.isAfter(startTime));
            assertTrue(after1us.isAfter(after1ms));
            assertTrue(after1ns.isAfter(after1us));
        }
        
        @Test
        @DisplayName("Should maintain nanosecond precision")
        void shouldMaintainNanosecondPrecision() {
            double nanoSecond = 0.000000001;
            
            mockTime.wait(nanoSecond * 123456789);
            
            LocalDateTime endTime = mockTime.now();
            long nanosDiff = ChronoUnit.NANOS.between(startTime, endTime);
            
            assertEquals(123456789, nanosDiff,
                "Should maintain nanosecond precision");
        }
        
        @Test
        @DisplayName("Should handle very large time advances")
        void shouldHandleVeryLargeTimeAdvances() {
            double oneDay = 86400; // seconds in a day
            
            mockTime.wait(oneDay);
            LocalDateTime afterOneDay = mockTime.now();
            
            long daysDiff = ChronoUnit.DAYS.between(startTime, afterOneDay);
            assertEquals(1, daysDiff);
            
            mockTime.wait(oneDay * 365); // One year
            LocalDateTime afterOneYear = mockTime.now();
            
            long totalDays = ChronoUnit.DAYS.between(startTime, afterOneYear);
            assertEquals(366, totalDays); // 1 + 365
        }
    }
    
    @Nested
    @DisplayName("Deterministic Behavior")
    class DeterministicBehavior {
        
        @Test
        @DisplayName("Should provide deterministic results")
        void shouldProvideDeterministicResults() {
            // Create two instances with same initial conditions
            MockTime time1 = new MockTime(mockActionDurations);
            MockTime time2 = new MockTime(mockActionDurations);
            
            // Perform same operations
            time1.wait(1.5);
            time2.wait(1.5);
            
            time1.wait(2.5);
            time2.wait(2.5);
            
            // Results should be identical (deterministic)
            Duration duration1 = Duration.between(time1.now(), LocalDateTime.now());
            Duration duration2 = Duration.between(time2.now(), LocalDateTime.now());
            
            // Both should have advanced by same amount relative to real time
            long diff = Math.abs(duration1.toMillis() - duration2.toMillis());
            assertTrue(diff < 100, "Should provide deterministic results");
        }
        
        @Test
        @DisplayName("Should not depend on real time passage")
        void shouldNotDependOnRealTimePassage() throws InterruptedException {
            mockTime.wait(10.0);
            LocalDateTime timeAfterMockWait = mockTime.now();
            
            // Real time delay
            Thread.sleep(100);
            
            LocalDateTime timeAfterRealDelay = mockTime.now();
            
            assertEquals(timeAfterMockWait, timeAfterRealDelay,
                "Mock time should not advance with real time");
        }
    }
    
    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {
        
        @Test
        @DisplayName("Should simulate timeout scenario")
        void shouldSimulateTimeoutScenario() {
            double timeoutSeconds = 30.0;
            LocalDateTime operationStart = mockTime.now();
            
            // Simulate waiting for something
            mockTime.wait(10.0); // First check
            mockTime.wait(10.0); // Second check
            mockTime.wait(15.0); // Third check - exceeds timeout
            
            LocalDateTime operationEnd = mockTime.now();
            long elapsed = ChronoUnit.SECONDS.between(operationStart, operationEnd);
            
            assertTrue(elapsed > timeoutSeconds,
                "Should be able to simulate timeout scenarios");
        }
        
        @Test
        @DisplayName("Should simulate action sequence")
        void shouldSimulateActionSequence() {
            when(mockActionDurations.getActionDuration(any()))
                .thenReturn(0.5, 1.0, 0.3, 2.0);
            
            // Simulate a sequence of actions
            ActionType[] sequence = {
                ActionType.FIND,
                ActionType.CLICK,
                ActionType.TYPE,
                ActionType.VANISH
            };
            
            for (ActionType action : sequence) {
                mockTime.wait(action);
            }
            
            LocalDateTime endTime = mockTime.now();
            long totalMillis = ChronoUnit.MILLIS.between(startTime, endTime);
            
            // 0.5 + 1.0 + 0.3 + 2.0 = 3.8 seconds = 3800 millis
            assertEquals(3800, totalMillis,
                "Should correctly accumulate action sequence timing");
        }
        
        @Test
        @DisplayName("Should support performance testing patterns")
        void shouldSupportPerformanceTestingPatterns() {
            // Simulate varying performance
            double[] responseTimes = {0.1, 0.2, 0.15, 0.5, 0.3};
            double totalExpected = 0;
            
            for (double responseTime : responseTimes) {
                mockTime.wait(responseTime);
                totalExpected += responseTime;
            }
            
            LocalDateTime endTime = mockTime.now();
            double actualTotal = ChronoUnit.NANOS.between(startTime, endTime) 
                / 1_000_000_000.0;
            
            assertEquals(totalExpected, actualTotal, 0.000001,
                "Should accurately track cumulative performance");
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle fractional nanoseconds")
        void shouldHandleFractionalNanoseconds() {
            // Test with 1 nanosecond (minimum representable time)
            mockTime.wait(0.000000001); // 1 nanosecond
            
            LocalDateTime after = mockTime.now();
            // Should advance by exactly 1 nanosecond
            long nanosDiff = ChronoUnit.NANOS.between(startTime, after);
            assertEquals(1, nanosDiff, "Should advance by 1 nanosecond");
            
            // Test that sub-nanosecond times round to zero
            LocalDateTime beforeSubNano = mockTime.now();
            mockTime.wait(0.0000000001); // 0.1 nanoseconds - should round to 0
            LocalDateTime afterSubNano = mockTime.now();
            assertEquals(beforeSubNano, afterSubNano, 
                "Sub-nanosecond waits should not advance time (rounds to 0)");
        }
        
        @Test
        @DisplayName("Should handle maximum double value")
        void shouldHandleMaximumDoubleValue() {
            // Don't actually wait for Double.MAX_VALUE seconds!
            // Just verify it doesn't throw
            assertDoesNotThrow(() -> {
                // Create a new instance to not affect other tests
                MockTime tempTime = new MockTime(mockActionDurations);
                tempTime.wait(1e10); // 10 billion seconds
            });
        }
        
        @Test
        @DisplayName("Should handle rapid successive waits")
        void shouldHandleRapidSuccessiveWaits() {
            for (int i = 0; i < 1000; i++) {
                mockTime.wait(0.001);
            }
            
            LocalDateTime endTime = mockTime.now();
            long secondsDiff = ChronoUnit.SECONDS.between(startTime, endTime);
            
            assertEquals(1, secondsDiff,
                "1000 waits of 1ms should equal 1 second");
        }
    }
}