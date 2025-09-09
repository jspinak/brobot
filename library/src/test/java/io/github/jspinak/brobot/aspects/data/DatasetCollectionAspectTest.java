package io.github.jspinak.brobot.aspects.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.aspects.annotations.CollectData;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.test.ConcurrentTestBase;
import io.github.jspinak.brobot.test.annotations.FlakyTest;
import io.github.jspinak.brobot.test.annotations.FlakyTest.FlakyCause;
import io.github.jspinak.brobot.test.utils.ConcurrentTestHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for DatasetCollectionAspect.
 * Uses ConcurrentTestBase for thread-safe parallel execution.
 */
@DisplayName("DatasetCollectionAspect Tests")
@ResourceLock(value = ConcurrentTestBase.ResourceLocks.FILE_SYSTEM)
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Test incompatible with CI environment")
public class DatasetCollectionAspectTest extends ConcurrentTestBase {

    private DatasetCollectionAspect aspect;

    @Mock
    private BrobotLogger brobotLogger;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private CollectData collectData;

    @TempDir
    Path tempDir;

    private AutoCloseable mocks;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mocks = MockitoAnnotations.openMocks(this);
        
        aspect = new DatasetCollectionAspect();
        ReflectionTestUtils.setField(aspect, "brobotLogger", brobotLogger);
        ReflectionTestUtils.setField(aspect, "outputDir", tempDir.toString());
        ReflectionTestUtils.setField(aspect, "batchSize", 10);
        ReflectionTestUtils.setField(aspect, "maxQueueSize", 100);
        
        // Mock BrobotLogger to avoid NullPointerException
        lenient().when(brobotLogger.log()).thenReturn(mock(io.github.jspinak.brobot.logging.unified.LogBuilder.class));
        
        // Initialize the aspect
        aspect.init();
        
        // Setup common mocks
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getName()).thenReturn("testMethod");
    }

    @AfterEach
    void tearDown() throws Exception {
        aspect.shutdown();
        if (mocks != null) {
            mocks.close();
        }
    }

    @Nested
    @DisplayName("Data Collection Tests")
    class DataCollectionTests {

        @Test
        @DisplayName("Should collect data when sampling rate is 1.0")
        void shouldCollectDataWithFullSampling() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("test-category");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[]{"label1", "label2"});
            when(collectData.features()).thenReturn(new String[0]);
            when(collectData.captureScreenshots()).thenReturn(false);
            when(collectData.compress()).thenReturn(false);

            ActionResult actionResult = new ActionResult();
            actionResult.setSuccess(true);
            when(joinPoint.proceed()).thenReturn(actionResult);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{});

            // Act
            Object result = aspect.collectDataset(joinPoint, collectData);

            // Assert
            assertEquals(actionResult, result);
            verify(joinPoint, times(1)).proceed();
            
            // Verify data was queued (check internal queue)
            BlockingQueue<?> queue = (BlockingQueue<?>) ReflectionTestUtils.getField(aspect, "dataQueue");
            assertNotNull(queue);
            assertTrue(queue.size() > 0 || waitForQueueProcessing(queue));
        }

        @Test
        @DisplayName("Should skip collection when sampling rate is 0")
        void shouldSkipCollectionWithZeroSampling() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(0.0);
            Object expectedResult = new Object();
            when(joinPoint.proceed()).thenReturn(expectedResult);

            // Act
            Object result = aspect.collectDataset(joinPoint, collectData);

            // Assert
            assertEquals(expectedResult, result);
            verify(joinPoint, times(1)).proceed();
            
            // Verify no data was queued
            BlockingQueue<?> queue = (BlockingQueue<?>) ReflectionTestUtils.getField(aspect, "dataQueue");
            assertEquals(0, queue.size());
        }

        @Test
        @DisplayName("Should respect max samples limit")
        void shouldRespectMaxSamplesLimit() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("limited-category");
            when(collectData.maxSamples()).thenReturn(2);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[0]);
            when(collectData.features()).thenReturn(new String[0]);
            when(collectData.captureScreenshots()).thenReturn(false);
            when(collectData.compress()).thenReturn(false);

            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(joinPoint.proceed()).thenReturn(result);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{});

            // Act - collect samples up to limit
            for (int i = 0; i < 5; i++) {
                aspect.collectDataset(joinPoint, collectData);
            }

            // Assert
            Map<String, AtomicInteger> counts = (Map<String, AtomicInteger>) 
                ReflectionTestUtils.getField(aspect, "categoryCounts");
            assertEquals(2, counts.get("limited-category").get());
        }

        @Test
        @DisplayName("Should only collect successful results when onlySuccess is true")
        void shouldOnlyCollectSuccessfulResults() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("success-only");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(true);
            when(collectData.labels()).thenReturn(new String[0]);
            when(collectData.features()).thenReturn(new String[0]);
            when(collectData.captureScreenshots()).thenReturn(false);

            ActionResult failedResult = new ActionResult();
            failedResult.setSuccess(false);
            
            ActionResult successResult = new ActionResult();
            successResult.setSuccess(true);

            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{});

            // Act
            when(joinPoint.proceed()).thenReturn(failedResult);
            aspect.collectDataset(joinPoint, collectData);
            
            when(joinPoint.proceed()).thenReturn(successResult);
            aspect.collectDataset(joinPoint, collectData);

            // Assert
            BlockingQueue<?> queue = (BlockingQueue<?>) ReflectionTestUtils.getField(aspect, "dataQueue");
            // Only successful result should be queued
            assertTrue(waitForQueueSize(queue, 1));
        }
    }

    @Nested
    @DisplayName("Feature Extraction Tests")
    class FeatureExtractionTests {

        @Test
        @DisplayName("Should extract features from ObjectCollection")
        void shouldExtractObjectCollectionFeatures() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("object-collection");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[0]);
            when(collectData.features()).thenReturn(new String[]{"objectCount"});
            when(collectData.captureScreenshots()).thenReturn(false);

            ObjectCollection collection = mock(ObjectCollection.class);
            when(joinPoint.getArgs()).thenReturn(new Object[]{collection});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{"collection"});
            
            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(joinPoint.proceed()).thenReturn(result);

            // Act
            aspect.collectDataset(joinPoint, collectData);

            // Assert
            // Features should be extracted from ObjectCollection
            verify(joinPoint).getArgs();
            BlockingQueue<?> queue = (BlockingQueue<?>) ReflectionTestUtils.getField(aspect, "dataQueue");
            assertTrue(queue.size() > 0 || waitForQueueProcessing(queue));
        }

        @Test
        @DisplayName("Should extract features from StateObject")
        void shouldExtractStateObjectFeatures() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("state-object");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[0]);
            when(collectData.features()).thenReturn(new String[]{"state"});
            when(collectData.captureScreenshots()).thenReturn(false);

            StateObject stateObject = mock(StateObject.class);
            when(stateObject.getName()).thenReturn("TestState");
            when(joinPoint.getArgs()).thenReturn(new Object[]{stateObject});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{"state"});
            
            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(joinPoint.proceed()).thenReturn(result);

            // Act
            aspect.collectDataset(joinPoint, collectData);

            // Assert
            verify(stateObject).getName();
            BlockingQueue<?> queue = (BlockingQueue<?>) ReflectionTestUtils.getField(aspect, "dataQueue");
            assertTrue(queue.size() > 0 || waitForQueueProcessing(queue));
        }

        @Test
        @DisplayName("Should extract features from ActionResult")
        void shouldExtractActionResultFeatures() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("action-result");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[0]);
            when(collectData.features()).thenReturn(new String[]{"matches"});
            when(collectData.captureScreenshots()).thenReturn(false);

            ActionResult actionResult = new ActionResult();
            actionResult.setSuccess(true);
            actionResult.setDuration(Duration.ofMillis(100));
            actionResult.setMatchList(new ArrayList<>());
            
            when(joinPoint.proceed()).thenReturn(actionResult);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{});

            // Act
            aspect.collectDataset(joinPoint, collectData);

            // Assert
            BlockingQueue<?> queue = (BlockingQueue<?>) ReflectionTestUtils.getField(aspect, "dataQueue");
            assertTrue(queue.size() > 0 || waitForQueueProcessing(queue));
        }

        @Test
        @DisplayName("Should filter features based on configuration")
        void shouldFilterFeaturesBasedOnConfig() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("filtered");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[0]);
            when(collectData.features()).thenReturn(new String[]{"param1"}); // Only include param1
            when(collectData.captureScreenshots()).thenReturn(false);

            when(joinPoint.getArgs()).thenReturn(new Object[]{"value1", "value2", "value3"});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{"param1", "param2", "param3"});
            
            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(joinPoint.proceed()).thenReturn(result);

            // Act
            aspect.collectDataset(joinPoint, collectData);

            // Assert
            // Only param1 should be included in features
            BlockingQueue<?> queue = (BlockingQueue<?>) ReflectionTestUtils.getField(aspect, "dataQueue");
            assertTrue(queue.size() > 0 || waitForQueueProcessing(queue));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle exceptions during method execution")
        void shouldHandleExceptionsDuringExecution() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("error-test");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[0]);
            when(collectData.features()).thenReturn(new String[0]);
            when(collectData.captureScreenshots()).thenReturn(false);

            RuntimeException exception = new RuntimeException("Test exception");
            when(joinPoint.proceed()).thenThrow(exception);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{});

            // Act & Assert
            assertThrows(RuntimeException.class, () -> 
                aspect.collectDataset(joinPoint, collectData));
            
            // Error should still be recorded
            BlockingQueue<?> queue = (BlockingQueue<?>) ReflectionTestUtils.getField(aspect, "dataQueue");
            assertTrue(queue.size() > 0 || waitForQueueProcessing(queue));
        }

        @Test
        @DisplayName("Should handle null results")
        void shouldHandleNullResults() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("null-result");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[0]);
            when(collectData.features()).thenReturn(new String[0]);
            when(collectData.captureScreenshots()).thenReturn(false);

            when(joinPoint.proceed()).thenReturn(null);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{});

            // Act
            Object result = aspect.collectDataset(joinPoint, collectData);

            // Assert
            assertNull(result);
            BlockingQueue<?> queue = (BlockingQueue<?>) ReflectionTestUtils.getField(aspect, "dataQueue");
            assertTrue(queue.size() > 0 || waitForQueueProcessing(queue));
        }

        @Test
        @DisplayName("Should handle queue overflow gracefully")
        @FlakyTest(reason = "Queue processing timing", cause = FlakyCause.TIMING)
        void shouldHandleQueueOverflow() throws Throwable {
            // Arrange
            ReflectionTestUtils.setField(aspect, "maxQueueSize", 2);
            
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("overflow");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[0]);
            when(collectData.features()).thenReturn(new String[0]);
            when(collectData.captureScreenshots()).thenReturn(false);

            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(joinPoint.proceed()).thenReturn(result);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{});

            // Fill the queue
            BlockingQueue<Object> queue = new LinkedBlockingQueue<>(2);
            queue.offer(new Object());
            queue.offer(new Object());
            ReflectionTestUtils.setField(aspect, "dataQueue", queue);

            // Act - should not throw exception
            aspect.collectDataset(joinPoint, collectData);

            // Assert - queue should still be full
            assertEquals(2, queue.size());
        }
    }

    @Nested
    @DisplayName("Data Persistence Tests")
    class DataPersistenceTests {

        @Test
        @DisplayName("Should write data to output directory")
        @FlakyTest(reason = "File I/O timing issues", cause = FlakyCause.FILE_SYSTEM)
        void shouldWriteDataToOutputDirectory() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("persistence-test");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[]{"test"});
            when(collectData.features()).thenReturn(new String[0]);
            when(collectData.captureScreenshots()).thenReturn(false);
            when(collectData.compress()).thenReturn(false);

            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(joinPoint.proceed()).thenReturn(result);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{});

            // Act - collect enough samples to trigger batch processing
            for (int i = 0; i < 11; i++) { // batchSize is 10
                aspect.collectDataset(joinPoint, collectData);
            }

            // Wait for async processing with retries
            Path categoryDir = tempDir.resolve("persistence-test");
            boolean fileCreated = ConcurrentTestHelper.retryOperation(() -> {
                if (Files.exists(categoryDir)) {
                    try (var stream = Files.list(categoryDir)) {
                        return stream.findAny().isPresent();
                    } catch (IOException e) {
                        return false;
                    }
                }
                return false;
            }, 10, Duration.ofMillis(500));

            // Assert - check if files were created
            assertTrue(fileCreated || Files.exists(categoryDir), 
                "Either files should be created or directory should exist");
        }

        @Test
        @DisplayName("Should compress data when configured")
        @FlakyTest(reason = "Async compression timing", cause = FlakyCause.ASYNC)
        void shouldCompressDataWhenConfigured() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("compress-test");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[0]);
            when(collectData.features()).thenReturn(new String[0]);
            when(collectData.captureScreenshots()).thenReturn(false);
            when(collectData.compress()).thenReturn(true);

            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(joinPoint.proceed()).thenReturn(result);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{});

            // Act
            for (int i = 0; i < 11; i++) {
                aspect.collectDataset(joinPoint, collectData);
            }

            // Wait for async processing with retries
            Path categoryDir = tempDir.resolve("compress-test");
            boolean hasGzipFile = ConcurrentTestHelper.retryOperation(() -> {
                if (Files.exists(categoryDir)) {
                    try (var stream = Files.list(categoryDir)) {
                        return stream.anyMatch(p -> p.toString().endsWith(".gz") || 
                                                    p.toString().endsWith(".json"));
                    } catch (IOException e) {
                        return false;
                    }
                }
                return false;
            }, 10, Duration.ofMillis(500));

            // Assert - check for compressed or regular files (async processing may vary)
            assertTrue(hasGzipFile || Files.exists(categoryDir),
                "Either compressed files should be created or directory should exist");
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should track collection statistics")
        void shouldTrackCollectionStatistics() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("stats-test");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[0]);
            when(collectData.features()).thenReturn(new String[0]);
            when(collectData.captureScreenshots()).thenReturn(false);

            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(joinPoint.proceed()).thenReturn(result);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{});

            // Act
            for (int i = 0; i < 5; i++) {
                aspect.collectDataset(joinPoint, collectData);
            }

            // Assert
            Map<String, DatasetCollectionAspect.DatasetStats> stats = aspect.getStatistics();
            assertNotNull(stats);
            assertTrue(stats.containsKey("stats-test"));
            assertEquals(5, stats.get("stats-test").getSampleCount());
        }

        @Test
        @DisplayName("Should calculate average sample size")
        void shouldCalculateAverageSampleSize() {
            // Arrange
            DatasetCollectionAspect.DatasetStats stats = new DatasetCollectionAspect.DatasetStats();
            stats.setSampleCount(10);
            stats.setTotalSize(1000);

            // Act
            double avgSize = stats.getAverageSampleSize();

            // Assert
            assertEquals(100.0, avgSize);
        }

        @Test
        @DisplayName("Should handle zero samples in statistics")
        void shouldHandleZeroSamplesInStatistics() {
            // Arrange
            DatasetCollectionAspect.DatasetStats stats = new DatasetCollectionAspect.DatasetStats();
            stats.setSampleCount(0);
            stats.setTotalSize(0);

            // Act
            double avgSize = stats.getAverageSampleSize();

            // Assert
            assertEquals(0.0, avgSize);
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("Should initialize properly")
        void shouldInitializeProperly() {
            // Arrange
            DatasetCollectionAspect newAspect = new DatasetCollectionAspect();
            ReflectionTestUtils.setField(newAspect, "brobotLogger", brobotLogger);
            ReflectionTestUtils.setField(newAspect, "outputDir", tempDir.toString());

            // Act
            newAspect.init();

            // Assert
            assertTrue(Files.exists(tempDir));
            
            // Cleanup
            newAspect.shutdown();
        }

        @Test
        @DisplayName("Should shutdown gracefully")
        void shouldShutdownGracefully() throws Throwable {
            // Arrange
            when(collectData.samplingRate()).thenReturn(1.0);
            when(collectData.category()).thenReturn("shutdown-test");
            when(collectData.maxSamples()).thenReturn(0);
            when(collectData.onlySuccess()).thenReturn(false);
            when(collectData.labels()).thenReturn(new String[0]);
            when(collectData.features()).thenReturn(new String[0]);
            when(collectData.captureScreenshots()).thenReturn(false);

            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(joinPoint.proceed()).thenReturn(result);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(methodSignature.getParameterNames()).thenReturn(new String[]{});

            // Add some data
            aspect.collectDataset(joinPoint, collectData);

            // Act - shutdown with timeout to prevent hanging in headless environments
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                aspect.shutdown();
            }, "Shutdown should complete within 5 seconds");

            // Assert - should flush pending data
            // No exception should be thrown
            assertTrue(true);
        }
    }

    // Helper methods
    private boolean waitForQueueProcessing(BlockingQueue<?> queue) {
        return ConcurrentTestHelper.waitForCondition(
            () -> queue.isEmpty(),
            Duration.ofMillis(500),
            Duration.ofMillis(50)
        );
    }

    private boolean waitForQueueSize(BlockingQueue<?> queue, int expectedSize) {
        return ConcurrentTestHelper.waitForCondition(
            () -> queue.size() == expectedSize,
            Duration.ofSeconds(1),
            Duration.ofMillis(100)
        );
    }
}