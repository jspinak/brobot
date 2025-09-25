package io.github.jspinak.brobot.tools.testing.wrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.action.internal.find.scene.ScenePatternMatcher;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.config.environment.ExecutionMode;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.action.MockFind;

/**
 * Comprehensive test suite for FindWrapper. Tests routing between mock and live find
 * implementations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FindWrapper Tests")
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
public class FindWrapperTest extends BrobotTestBase {

    @Mock private ExecutionMode executionMode;

    @Mock private MockFind mockFind;

    @Mock private ScenePatternMatcher scenePatternMatcher;

    private FindWrapper findWrapper;

    @Mock private Pattern pattern;

    @Mock private Scene scene;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        findWrapper = new FindWrapper(executionMode, mockFind, scenePatternMatcher);
    }

    @Nested
    @DisplayName("Mock Mode Routing")
    class MockModeRouting {

        @BeforeEach
        void setupMockMode() {
            when(executionMode.isMock()).thenReturn(true);
        }

        @Test
        @DisplayName("Should route findAll to mock implementation in mock mode")
        void shouldRouteFindAllToMockInMockMode() {
            List<Match> mockMatches = Arrays.asList(mock(Match.class), mock(Match.class));
            when(mockFind.getMatches(pattern)).thenReturn(mockMatches);

            List<Match> result = findWrapper.findAll(pattern, scene);

            assertEquals(mockMatches, result);
            verify(mockFind).getMatches(pattern);
            verify(scenePatternMatcher, never()).findAllInScene(any(), any());
        }

        @Test
        @DisplayName("Should route findAllWords to mock implementation in mock mode")
        void shouldRouteFindAllWordsToMockInMockMode() {
            List<Match> mockWordMatches =
                    Arrays.asList(mock(Match.class), mock(Match.class), mock(Match.class));
            when(mockFind.getWordMatches()).thenReturn(mockWordMatches);

            List<Match> result = findWrapper.findAllWords(scene);

            assertEquals(mockWordMatches, result);
            verify(mockFind).getWordMatches();
            verify(scenePatternMatcher, never()).getWordMatches(any());
        }

        @Test
        @DisplayName("Should handle empty results from mock")
        void shouldHandleEmptyResultsFromMock() {
            when(mockFind.getMatches(pattern)).thenReturn(Collections.emptyList());
            when(mockFind.getWordMatches()).thenReturn(Collections.emptyList());

            List<Match> patternResult = findWrapper.findAll(pattern, scene);
            List<Match> wordResult = findWrapper.findAllWords(scene);

            assertTrue(patternResult.isEmpty());
            assertTrue(wordResult.isEmpty());
        }

        @Test
        @DisplayName("Should handle null pattern in mock mode")
        void shouldHandleNullPatternInMockMode() {
            when(mockFind.getMatches(null)).thenReturn(Collections.emptyList());

            List<Match> result = findWrapper.findAll(null, scene);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(mockFind).getMatches(null);
        }
    }

    @Nested
    @DisplayName("Live Mode Routing")
    class LiveModeRouting {

        @BeforeEach
        void setupLiveMode() {
            when(executionMode.isMock()).thenReturn(false);
        }

        @Test
        @DisplayName("Should route findAll to live implementation in live mode")
        void shouldRouteFindAllToLiveInLiveMode() {
            List<Match> liveMatches = Arrays.asList(mock(Match.class), mock(Match.class));
            when(scenePatternMatcher.findAllInScene(pattern, scene)).thenReturn(liveMatches);

            List<Match> result = findWrapper.findAll(pattern, scene);

            assertEquals(liveMatches, result);
            verify(scenePatternMatcher).findAllInScene(pattern, scene);
            verify(mockFind, never()).getMatches(any());
        }

        @Test
        @DisplayName("Should route findAllWords to live implementation in live mode")
        void shouldRouteFindAllWordsToLiveInLiveMode() {
            List<Match> liveWordMatches =
                    Arrays.asList(mock(Match.class), mock(Match.class), mock(Match.class));
            when(scenePatternMatcher.getWordMatches(scene)).thenReturn(liveWordMatches);

            List<Match> result = findWrapper.findAllWords(scene);

            assertEquals(liveWordMatches, result);
            verify(scenePatternMatcher).getWordMatches(scene);
            verify(mockFind, never()).getWordMatches();
        }

        @Test
        @DisplayName("Should handle empty results from live implementation")
        void shouldHandleEmptyResultsFromLive() {
            when(scenePatternMatcher.findAllInScene(pattern, scene))
                    .thenReturn(Collections.emptyList());
            when(scenePatternMatcher.getWordMatches(scene)).thenReturn(Collections.emptyList());

            List<Match> patternResult = findWrapper.findAll(pattern, scene);
            List<Match> wordResult = findWrapper.findAllWords(scene);

            assertTrue(patternResult.isEmpty());
            assertTrue(wordResult.isEmpty());
        }

        @Test
        @DisplayName("Should handle null scene in live mode")
        void shouldHandleNullSceneInLiveMode() {
            when(scenePatternMatcher.findAllInScene(pattern, null))
                    .thenReturn(Collections.emptyList());
            when(scenePatternMatcher.getWordMatches(null)).thenReturn(Collections.emptyList());

            List<Match> patternResult = findWrapper.findAll(pattern, null);
            List<Match> wordResult = findWrapper.findAllWords(null);

            assertNotNull(patternResult);
            assertNotNull(wordResult);
            verify(scenePatternMatcher).findAllInScene(pattern, null);
            verify(scenePatternMatcher).getWordMatches(null);
        }
    }

    @Nested
    @DisplayName("Mode Switching")
    class ModeSwitching {

        @Test
        @DisplayName("Should switch between mock and live modes correctly")
        void shouldSwitchBetweenModes() {
            // Setup mock matches
            List<Match> mockMatches = Arrays.asList(mock(Match.class));
            when(mockFind.getMatches(pattern)).thenReturn(mockMatches);

            // Setup live matches
            List<Match> liveMatches = Arrays.asList(mock(Match.class), mock(Match.class));
            when(scenePatternMatcher.findAllInScene(pattern, scene)).thenReturn(liveMatches);

            // First call in mock mode
            when(executionMode.isMock()).thenReturn(true);
            List<Match> mockResult = findWrapper.findAll(pattern, scene);
            assertEquals(1, mockResult.size());

            // Switch to live mode
            when(executionMode.isMock()).thenReturn(false);
            List<Match> liveResult = findWrapper.findAll(pattern, scene);
            assertEquals(2, liveResult.size());

            // Verify both implementations were called
            verify(mockFind).getMatches(pattern);
            verify(scenePatternMatcher).findAllInScene(pattern, scene);
        }

        @Test
        @DisplayName("Should handle rapid mode switches")
        void shouldHandleRapidModeSwitches() {
            List<Match> mockMatches = Collections.singletonList(mock(Match.class));
            List<Match> liveMatches = Arrays.asList(mock(Match.class), mock(Match.class));

            when(mockFind.getMatches(pattern)).thenReturn(mockMatches);
            when(scenePatternMatcher.findAllInScene(pattern, scene)).thenReturn(liveMatches);

            // Rapidly switch modes
            for (int i = 0; i < 10; i++) {
                boolean isMock = i % 2 == 0;
                when(executionMode.isMock()).thenReturn(isMock);

                List<Match> result = findWrapper.findAll(pattern, scene);

                if (isMock) {
                    assertEquals(1, result.size());
                } else {
                    assertEquals(2, result.size());
                }
            }

            // Verify call counts
            verify(mockFind, times(5)).getMatches(pattern);
            verify(scenePatternMatcher, times(5)).findAllInScene(pattern, scene);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle exception from mock implementation")
        void shouldHandleExceptionFromMock() {
            when(executionMode.isMock()).thenReturn(true);
            when(mockFind.getMatches(pattern)).thenThrow(new RuntimeException("Mock error"));

            assertThrows(RuntimeException.class, () -> findWrapper.findAll(pattern, scene));
        }

        @Test
        @DisplayName("Should handle exception from live implementation")
        void shouldHandleExceptionFromLive() {
            when(executionMode.isMock()).thenReturn(false);
            when(scenePatternMatcher.findAllInScene(pattern, scene))
                    .thenThrow(new RuntimeException("Live error"));

            assertThrows(RuntimeException.class, () -> findWrapper.findAll(pattern, scene));
        }

        @Test
        @DisplayName("Should propagate null pointer exceptions")
        void shouldPropagateNullPointerExceptions() {
            when(executionMode.isMock()).thenReturn(true);
            when(mockFind.getMatches(any())).thenThrow(new NullPointerException());

            assertThrows(NullPointerException.class, () -> findWrapper.findAll(pattern, scene));
        }
    }

    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceCharacteristics {

        @Test
        @DisplayName("Mock mode should be faster than live mode simulation")
        void mockModeShouldBeFaster() {
            // Skip performance tests in WSL/headless environments due to unreliable timing
            if (System.getenv("WSL_DISTRO_NAME") != null
                    || System.getProperty("java.awt.headless", "false").equals("true")
                    || System.getenv("CI") != null) {
                return; // Skip test in environments with unpredictable performance
            }

            // Setup mock to return immediately
            when(executionMode.isMock()).thenReturn(true);
            when(mockFind.getMatches(pattern)).thenReturn(Collections.emptyList());

            long startMock = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                findWrapper.findAll(pattern, scene);
            }
            long mockTime = System.nanoTime() - startMock;
            double mockTimePerOp = mockTime / 1000.0;

            // Setup live to simulate slower execution
            when(executionMode.isMock()).thenReturn(false);
            when(scenePatternMatcher.findAllInScene(pattern, scene))
                    .thenAnswer(
                            invocation -> {
                                Thread.sleep(1); // Simulate processing time
                                return Collections.emptyList();
                            });

            long startLive = System.nanoTime();
            for (int i = 0; i < 10; i++) { // Fewer iterations due to sleep
                findWrapper.findAll(pattern, scene);
            }
            long liveTime = System.nanoTime() - startLive;
            double liveTimePerOp = liveTime / 10.0;

            // Mock should be significantly faster per operation
            assertTrue(
                    mockTimePerOp < liveTimePerOp / 10,
                    String.format(
                            "Mock time per op (%.2f ns) should be at least 10x faster than live"
                                    + " (%.2f ns)",
                            mockTimePerOp, liveTimePerOp));
        }
    }

    @Nested
    @DisplayName("Concurrency Safety")
    class ConcurrencySafety {

        @Test
        @DisplayName("Should handle concurrent calls safely")
        void shouldHandleConcurrentCallsSafely() throws InterruptedException {
            List<Match> matches = Collections.synchronizedList(new ArrayList<>());
            Match match1 = mock(Match.class);
            Match match2 = mock(Match.class);

            when(executionMode.isMock()).thenReturn(true);
            when(mockFind.getMatches(pattern)).thenReturn(Arrays.asList(match1, match2));

            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                threads[i] =
                        new Thread(
                                () -> {
                                    List<Match> result = findWrapper.findAll(pattern, scene);
                                    matches.addAll(result);
                                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertEquals(20, matches.size()); // 10 threads * 2 matches each
            verify(mockFind, times(10)).getMatches(pattern);
        }
    }

    @Nested
    @DisplayName("Integration with Framework")
    class FrameworkIntegration {

        @Test
        @DisplayName("Should respect BrobotProperties mock configuration")
        void shouldRespectBrobotProperties() {
            // Create BrobotProperties mock
            BrobotProperties mockBrobotProperties = mock(BrobotProperties.class);
            BrobotProperties.Core core = new BrobotProperties.Core();
            BrobotProperties.Screenshot screenshot = new BrobotProperties.Screenshot();
            screenshot.setTestScreenshots(Collections.emptyList());

            when(mockBrobotProperties.getCore()).thenReturn(core);
            when(mockBrobotProperties.getScreenshot()).thenReturn(screenshot);

            // Create real ExecutionMode to test integration
            ExecutionMode realExecutionMode = new ExecutionMode(mockBrobotProperties);
            FindWrapper realWrapper =
                    new FindWrapper(realExecutionMode, mockFind, scenePatternMatcher);

            // Setup mock matches
            when(mockFind.getMatches(pattern))
                    .thenReturn(Collections.singletonList(mock(Match.class)));
            when(scenePatternMatcher.findAllInScene(pattern, scene))
                    .thenReturn(Collections.emptyList());

            // Test with mock mode enabled
            core.setMock(true);
            List<Match> mockResult = realWrapper.findAll(pattern, scene);
            assertEquals(1, mockResult.size());

            // Test with mock mode disabled
            core.setMock(false);
            List<Match> liveResult = realWrapper.findAll(pattern, scene);
            assertEquals(0, liveResult.size());
        }
    }
}
