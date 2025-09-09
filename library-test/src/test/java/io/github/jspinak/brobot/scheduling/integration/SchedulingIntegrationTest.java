package io.github.jspinak.brobot.scheduling.integration;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.model.state.*;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.tools.testing.mock.builders.MockSceneBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for scheduling and concurrent execution in Brobot.
 * Tests task execution, timing, and coordination.
 */
@SpringBootTest
@DisplayName("Scheduling Integration Tests")
class SchedulingIntegrationTest extends BrobotIntegrationTestBase {
    
    @Autowired(required = false)
    private Action action;
    
    private State testState;
    private StateImage testImage;
    private ObjectCollection testCollection;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutor;
    
    @BeforeEach
    public void setupTest() {
        setupTestData();
        executorService = Executors.newFixedThreadPool(5);
        scheduledExecutor = Executors.newScheduledThreadPool(3);
    }
    
    @AfterEach
    void tearDown() {
        executorService.shutdown();
        scheduledExecutor.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    void setupTestData() {
        testImage = new StateImage.Builder()
            .setName("test-image")
            .addPattern(MockSceneBuilder.createMockPattern())
            .build();
        
        testState = new State.Builder("TestState").build();
        testState.getStateImages().add(testImage);
        
        testCollection = new ObjectCollection.Builder()
            .withImages(testImage)
            .build();
    }
    
    @Nested
    @DisplayName("Basic Task Execution Tests")
    class BasicTaskExecutionTests {
        
        @Test
        @DisplayName("Should execute simple task")
        void shouldExecuteSimpleTask() throws InterruptedException, ExecutionException {
            // Given
            AtomicBoolean executed = new AtomicBoolean(false);
            
            Callable<ActionResult> task = () -> {
                executed.set(true);
                ActionResult result = new ActionResult();
                result.setSuccess(true);
                return result;
            };
            
            // When
            Future<ActionResult> future = executorService.submit(task);
            ActionResult result = future.get();
            
            // Then
            assertTrue(executed.get());
            assertNotNull(result);
            assertTrue(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should execute multiple concurrent tasks")
        void shouldExecuteMultipleConcurrentTasks() throws InterruptedException {
            // Given
            int taskCount = 10;
            CountDownLatch latch = new CountDownLatch(taskCount);
            AtomicInteger completedCount = new AtomicInteger(0);
            
            // When
            for (int i = 0; i < taskCount; i++) {
                executorService.submit(() -> {
                    try {
                        Thread.sleep(50);
                        completedCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // Then
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(taskCount, completedCount.get());
        }
        
        @Test
        @DisplayName("Should handle task with timeout")
        void shouldHandleTaskWithTimeout() {
            // Given
            Callable<ActionResult> longRunningTask = () -> {
                Thread.sleep(5000);
                return new ActionResult();
            };
            
            // When
            Future<ActionResult> future = executorService.submit(longRunningTask);
            
            // Then
            assertThrows(TimeoutException.class, () -> 
                future.get(100, TimeUnit.MILLISECONDS)
            );
            
            future.cancel(true);
        }
    }
    
    @Nested
    @DisplayName("Scheduled Task Tests")
    class ScheduledTaskTests {
        
        @Test
        @DisplayName("Should execute delayed task")
        void shouldExecuteDelayedTask() throws InterruptedException {
            // Given
            AtomicBoolean executed = new AtomicBoolean(false);
            long delayMillis = 200;
            long startTime = System.currentTimeMillis();
            
            // When
            ScheduledFuture<?> future = scheduledExecutor.schedule(
                () -> executed.set(true),
                delayMillis,
                TimeUnit.MILLISECONDS
            );
            
            // Wait for execution
            Thread.sleep(delayMillis + 100);
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Then
            assertTrue(executed.get());
            assertTrue(executionTime >= delayMillis);
        }
        
        @Test
        @DisplayName("Should execute periodic task")
        void shouldExecutePeriodicTask() throws InterruptedException {
            // Given
            AtomicInteger executionCount = new AtomicInteger(0);
            long periodMillis = 100;
            
            // When
            ScheduledFuture<?> future = scheduledExecutor.scheduleAtFixedRate(
                executionCount::incrementAndGet,
                0,
                periodMillis,
                TimeUnit.MILLISECONDS
            );
            
            // Let it run for a while
            Thread.sleep(450);
            future.cancel(false);
            
            // Then
            int count = executionCount.get();
            assertTrue(count >= 4 && count <= 5); // Should execute 4-5 times
        }
        
        @Test
        @DisplayName("Should execute task with fixed delay")
        void shouldExecuteTaskWithFixedDelay() throws InterruptedException {
            // Given
            AtomicInteger executionCount = new AtomicInteger(0);
            CountDownLatch startLatch = new CountDownLatch(1);
            
            // When
            ScheduledFuture<?> future = scheduledExecutor.scheduleWithFixedDelay(
                () -> {
                    startLatch.countDown();
                    executionCount.incrementAndGet();
                    try {
                        Thread.sleep(50); // Task takes 50ms
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                },
                0,
                100, // 100ms delay after each completion
                TimeUnit.MILLISECONDS
            );
            
            // Wait for some executions
            startLatch.await();
            Thread.sleep(400);
            future.cancel(false);
            
            // Then
            int count = executionCount.get();
            assertTrue(count >= 2 && count <= 4); // Should execute 2-4 times
        }
    }
    
    @Nested
    @DisplayName("Task Coordination Tests")
    class TaskCoordinationTests {
        
        @Test
        @DisplayName("Should coordinate dependent tasks")
        void shouldCoordinateDependentTasks() throws InterruptedException, ExecutionException {
            // Given
            CompletableFuture<String> firstTask = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "First";
            }, executorService);
            
            // When
            CompletableFuture<String> secondTask = firstTask.thenApplyAsync(result -> 
                result + " -> Second", executorService
            );
            
            CompletableFuture<String> thirdTask = secondTask.thenApplyAsync(result -> 
                result + " -> Third", executorService
            );
            
            // Then
            String finalResult = thirdTask.get();
            assertEquals("First -> Second -> Third", finalResult);
        }
        
        @Test
        @DisplayName("Should handle parallel task execution")
        void shouldHandleParallelTaskExecution() throws InterruptedException, ExecutionException {
            // Given
            List<CompletableFuture<Integer>> futures = new ArrayList<>();
            
            // When
            for (int i = 0; i < 5; i++) {
                final int taskId = i;
                CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return taskId * 2;
                }, executorService);
                futures.add(future);
            }
            
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allFutures.get();
            
            // Then
            for (int i = 0; i < 5; i++) {
                assertEquals(i * 2, futures.get(i).get());
            }
        }
        
        @Test
        @DisplayName("Should handle task cancellation")
        void shouldHandleTaskCancellation() throws InterruptedException {
            // Given
            AtomicBoolean taskCompleted = new AtomicBoolean(false);
            CountDownLatch startedLatch = new CountDownLatch(1);
            
            Future<?> future = executorService.submit(() -> {
                try {
                    startedLatch.countDown();
                    Thread.sleep(5000);
                    taskCompleted.set(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // When
            startedLatch.await();
            boolean cancelled = future.cancel(true);
            Thread.sleep(100);
            
            // Then
            assertTrue(cancelled);
            assertFalse(taskCompleted.get());
            assertTrue(future.isCancelled());
        }
    }
    
    @Nested
    @DisplayName("Action Integration Tests")
    class ActionIntegrationTests {
        
        @Test
        @DisplayName("Should execute action tasks concurrently")
        void shouldExecuteActionTasksConcurrently() throws InterruptedException {
            if (action == null) {
                // Skip if Action is not available
                return;
            }
            
            // Given
            int taskCount = 5;
            CountDownLatch latch = new CountDownLatch(taskCount);
            List<Future<ActionResult>> futures = new ArrayList<>();
            
            // When
            for (int i = 0; i < taskCount; i++) {
                Future<ActionResult> future = executorService.submit(() -> {
                    try {
                        ActionResult result = action.find(testCollection);
                        latch.countDown();
                        return result;
                    } catch (Exception e) {
                        latch.countDown();
                        return new ActionResult();
                    }
                });
                futures.add(future);
            }
            
            // Then
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(taskCount, futures.size());
            
            for (Future<ActionResult> future : futures) {
                assertNotNull(future);
                assertTrue(future.isDone());
            }
        }
        
        @Test
        @DisplayName("Should handle action timeouts")
        void shouldHandleActionTimeouts() {
            if (action == null) {
                // Skip if Action is not available
                return;
            }
            
            // Given
            Future<ActionResult> future = executorService.submit(() -> {
                // Simulate long-running action
                Thread.sleep(5000);
                return action.find(testCollection);
            });
            
            // When/Then
            assertThrows(TimeoutException.class, () -> 
                future.get(100, TimeUnit.MILLISECONDS)
            );
            
            future.cancel(true);
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle task exceptions")
        void shouldHandleTaskExceptions() {
            // Given
            Future<ActionResult> future = executorService.submit(() -> {
                throw new RuntimeException("Task failed");
            });
            
            // When/Then
            assertThrows(ExecutionException.class, future::get);
        }
        
        @Test
        @DisplayName("Should recover from task failures")
        void shouldRecoverFromTaskFailures() throws InterruptedException, ExecutionException {
            // Given
            AtomicInteger attemptCount = new AtomicInteger(0);
            
            Callable<ActionResult> taskWithRetry = () -> {
                int attempt = attemptCount.incrementAndGet();
                if (attempt < 3) {
                    throw new RuntimeException("Attempt " + attempt + " failed");
                }
                ActionResult result = new ActionResult();
                result.setSuccess(true);
                return result;
            };
            
            // When
            ActionResult result = null;
            Exception lastException = null;
            
            for (int i = 0; i < 3; i++) {
                try {
                    Future<ActionResult> future = executorService.submit(taskWithRetry);
                    result = future.get();
                    break;
                } catch (ExecutionException e) {
                    lastException = e;
                }
            }
            
            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(3, attemptCount.get());
        }
        
        @Test
        @DisplayName("Should handle rejected execution")
        void shouldHandleRejectedExecution() {
            // Given
            ExecutorService limitedExecutor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1)
            );
            
            try {
                // Fill the executor
                for (int i = 0; i < 3; i++) {
                    limitedExecutor.submit(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
                
                // When/Then - next submission should be rejected
                assertThrows(RejectedExecutionException.class, () -> 
                    limitedExecutor.submit(() -> new ActionResult())
                );
            } finally {
                limitedExecutor.shutdownNow();
            }
        }
    }
}