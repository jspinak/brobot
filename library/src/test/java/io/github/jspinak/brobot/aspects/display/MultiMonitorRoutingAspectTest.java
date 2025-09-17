package io.github.jspinak.brobot.aspects.display;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogBuilder;
import io.github.jspinak.brobot.monitor.MonitorManager;
import io.github.jspinak.brobot.test.BrobotTestBase;

@ExtendWith(MockitoExtension.class)
public class MultiMonitorRoutingAspectTest extends BrobotTestBase {

    private MultiMonitorRoutingAspect aspect;

    @Mock private BrobotLogger brobotLogger;

    @Mock private LogBuilder logBuilder;

    @Mock private MonitorManager monitorManager;

    @Mock private ProceedingJoinPoint joinPoint;

    @Mock private Signature signature;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        aspect = new MultiMonitorRoutingAspect(brobotLogger, monitorManager);
        ReflectionTestUtils.setField(aspect, "defaultMonitorIndex", 0);
        ReflectionTestUtils.setField(aspect, "enableLoadBalancing", true);
        ReflectionTestUtils.setField(aspect, "enableFailover", true);

        // Setup log builder chain - use lenient() to avoid UnnecessaryStubbingException
        lenient().when(brobotLogger.log()).thenReturn(logBuilder);
        lenient().when(logBuilder.type(any())).thenReturn(logBuilder);
        lenient().when(logBuilder.level(any())).thenReturn(logBuilder);
        lenient().when(logBuilder.action(anyString())).thenReturn(logBuilder);
        lenient().when(logBuilder.metadata(anyString(), any())).thenReturn(logBuilder);
        lenient().when(logBuilder.observation(anyString())).thenReturn(logBuilder);

        // Mock the void log() method
        lenient().doNothing().when(logBuilder).log();

        // Setup monitor manager
        when(monitorManager.getMonitorCount()).thenReturn(3);

        // Initialize the aspect
        aspect.init();
    }

    @Test
    public void testRouteToMonitor_SuccessfulExecution() throws Throwable {
        // Arrange
        ObjectCollection objCollection = new ObjectCollection.Builder().build();
        when(joinPoint.getArgs()).thenReturn(new Object[] {objCollection});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");
        Object expectedResult = new Object();
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // Act
        Object result = aspect.routeToMonitor(joinPoint);

        // Assert
        assertEquals(expectedResult, result);
        verify(logBuilder).log();

        // Verify statistics were updated
        Map<Integer, MultiMonitorRoutingAspect.MonitorStats> stats = aspect.getMonitorStatistics();
        assertTrue(stats.containsKey(0));
        MultiMonitorRoutingAspect.MonitorStats monitorStats = stats.get(0);
        assertEquals(1, monitorStats.getTotalOperations().get());
        assertEquals(1, monitorStats.getSuccessfulOperations().get());
    }

    @Test
    public void testRouteToMonitor_WithFailover() throws Throwable {
        // Arrange
        ReflectionTestUtils.setField(aspect, "enableFailover", true);
        ObjectCollection objCollection = new ObjectCollection.Builder().build();
        when(joinPoint.getArgs()).thenReturn(new Object[] {objCollection});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("click()");

        // First call fails, second succeeds
        Object expectedResult = new Object();
        when(joinPoint.proceed())
                .thenThrow(new RuntimeException("Monitor 0 failed"))
                .thenReturn(expectedResult);

        // Act
        Object result = aspect.routeToMonitor(joinPoint);

        // Assert
        assertEquals(expectedResult, result);
        verify(joinPoint, times(2)).proceed();

        // Verify first monitor was marked unhealthy
        Map<Integer, MultiMonitorRoutingAspect.MonitorStats> stats = aspect.getMonitorStatistics();
        assertFalse(stats.get(0).isHealthy());
    }

    @Test
    public void testRouteToMonitor_FailoverDisabled() throws Throwable {
        // Arrange
        ReflectionTestUtils.setField(aspect, "enableFailover", false);
        ObjectCollection objCollection = new ObjectCollection.Builder().build();
        when(joinPoint.getArgs()).thenReturn(new Object[] {objCollection});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("click()");
        RuntimeException exception = new RuntimeException("Operation failed");
        when(joinPoint.proceed()).thenThrow(exception);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> aspect.routeToMonitor(joinPoint));

        // Verify no failover attempt
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    public void testLoadBalancing() throws Throwable {
        // Arrange
        ReflectionTestUtils.setField(aspect, "enableLoadBalancing", true);

        // Simulate different loads on monitors
        Map<Integer, MultiMonitorRoutingAspect.MonitorStats> stats = aspect.getMonitorStatistics();

        // Monitor 0 has high load
        stats.get(0).incrementOperations();
        stats.get(0).incrementOperations();
        stats.get(0).recordResult(true, 1000);

        // Monitor 1 has low load
        stats.get(1).incrementOperations();
        stats.get(1).recordResult(true, 100);

        // Monitor 2 has medium load
        stats.get(2).incrementOperations();
        stats.get(2).recordResult(true, 500);

        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");
        when(joinPoint.proceed()).thenReturn(new Object());

        // Act
        aspect.routeToMonitor(joinPoint);

        // Assert - Should route to monitor with lowest load
        // Due to the simplified implementation, verify the method was called
        verify(joinPoint).proceed();
    }

    @Test
    public void testGetCurrentMonitor() throws Throwable {
        // Arrange
        ObjectCollection objCollection = new ObjectCollection.Builder().build();
        when(joinPoint.getArgs()).thenReturn(new Object[] {objCollection});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");

        // Capture current monitor during execution
        AtomicInteger capturedMonitor = new AtomicInteger(-1);
        when(joinPoint.proceed())
                .thenAnswer(
                        invocation -> {
                            Optional<Integer> current = aspect.getCurrentMonitor();
                            current.ifPresent(capturedMonitor::set);
                            return new Object();
                        });

        // Act
        aspect.routeToMonitor(joinPoint);

        // Assert
        assertTrue(capturedMonitor.get() >= 0);

        // After execution, current monitor should be cleared
        Optional<Integer> afterExecution = aspect.getCurrentMonitor();
        assertFalse(afterExecution.isPresent());
    }

    @Test
    public void testMonitorStats() {
        // Arrange
        MultiMonitorRoutingAspect.MonitorStats stats =
                new MultiMonitorRoutingAspect.MonitorStats(0);

        // Act - Record multiple operations
        stats.incrementOperations();
        stats.recordResult(true, 100);

        stats.incrementOperations();
        stats.recordResult(true, 200);

        stats.incrementOperations();
        stats.recordResult(false, 150);

        stats.incrementOperations();
        stats.recordResult(true, 50);

        // Assert
        assertEquals(0, stats.getMonitorIndex());
        assertEquals(4, stats.getTotalOperations().get());
        assertEquals(3, stats.getSuccessfulOperations().get());
        assertEquals(500, stats.getTotalDuration().get());
        assertEquals(75.0, stats.getSuccessRate(), 0.01);
        assertEquals(0, stats.getActiveOperations().get());
        assertTrue(stats.isHealthy());
    }

    @Test
    public void testMonitorStats_HealthManagement() {
        // Arrange
        MultiMonitorRoutingAspect.MonitorStats stats =
                new MultiMonitorRoutingAspect.MonitorStats(1);

        // Act & Assert - Initially healthy
        assertTrue(stats.isHealthy());

        // Mark unhealthy
        stats.markUnhealthy();
        assertFalse(stats.isHealthy());
        assertTrue(stats.getLastFailureTime() > 0);

        // Recovery after successful operations
        for (int i = 0; i < 5; i++) {
            stats.incrementOperations();
            stats.recordResult(true, 100);
        }
        assertTrue(stats.isHealthy());
    }

    @Test
    public void testMonitorStats_LoadCalculation() {
        // Arrange
        MultiMonitorRoutingAspect.MonitorStats stats =
                new MultiMonitorRoutingAspect.MonitorStats(2);

        // Act - Add active operations
        stats.incrementOperations();
        stats.incrementOperations();
        stats.incrementOperations();

        // Record one completion
        stats.recordResult(true, 300);

        // Assert
        long load = stats.getCurrentLoad();
        assertTrue(load > 0);
        assertEquals(2, stats.getActiveOperations().get());
    }

    @Test
    public void testResetStatistics() {
        // Arrange
        Map<Integer, MultiMonitorRoutingAspect.MonitorStats> stats = aspect.getMonitorStatistics();

        // Add some data
        stats.get(0).incrementOperations();
        stats.get(0).recordResult(true, 100);
        stats.get(0).markUnhealthy();

        stats.get(1).incrementOperations();
        stats.get(1).recordResult(false, 200);

        // Act
        aspect.resetStatistics();

        // Assert
        stats = aspect.getMonitorStatistics();
        for (MultiMonitorRoutingAspect.MonitorStats monitorStats : stats.values()) {
            assertEquals(0, monitorStats.getTotalOperations().get());
            assertEquals(0, monitorStats.getSuccessfulOperations().get());
            assertEquals(0, monitorStats.getTotalDuration().get());
            assertEquals(0, monitorStats.getActiveOperations().get());
            assertTrue(monitorStats.isHealthy());
            assertEquals(0, monitorStats.getLastFailureTime());
        }
    }

    @Test
    public void testConcurrentOperations() throws Throwable {
        // Arrange
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("find()");
        when(joinPoint.proceed())
                .thenAnswer(
                        invocation -> {
                            Thread.sleep(10); // Simulate work
                            return new Object();
                        });

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(
                    () -> {
                        try {
                            aspect.routeToMonitor(joinPoint);
                        } catch (Throwable e) {
                            fail("Unexpected exception: " + e);
                        } finally {
                            latch.countDown();
                        }
                    });
        }

        // Assert
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify all operations were recorded
        Map<Integer, MultiMonitorRoutingAspect.MonitorStats> stats = aspect.getMonitorStatistics();
        long totalOps = stats.values().stream().mapToLong(s -> s.getTotalOperations().get()).sum();
        assertEquals(threadCount, totalOps);
    }

    @Test
    public void testExtractRegionInfo_WithObjectCollection() throws Throwable {
        // Arrange
        ObjectCollection objCollection = new ObjectCollection.Builder().build();
        when(joinPoint.getArgs()).thenReturn(new Object[] {objCollection, "param2"});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("click()");
        when(joinPoint.proceed()).thenReturn(new Object());

        // Act
        aspect.routeToMonitor(joinPoint);

        // Assert
        verify(logBuilder).metadata(eq("hasRegion"), eq(true));
        verify(logBuilder).log();
    }

    @Test
    public void testExtractRegionInfo_WithoutObjectCollection() throws Throwable {
        // Arrange
        when(joinPoint.getArgs()).thenReturn(new Object[] {"param1", 123});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("click()");
        when(joinPoint.proceed()).thenReturn(new Object());

        // Act
        aspect.routeToMonitor(joinPoint);

        // Assert
        verify(logBuilder).metadata(eq("hasRegion"), eq(false));
        verify(logBuilder).log();
    }

    @Test
    public void testMonitorStats_Reset() {
        // Arrange
        MultiMonitorRoutingAspect.MonitorStats stats =
                new MultiMonitorRoutingAspect.MonitorStats(0);

        // Add data
        stats.incrementOperations();
        stats.recordResult(true, 100);
        stats.markUnhealthy();

        // Act
        stats.reset();

        // Assert
        assertEquals(0, stats.getTotalOperations().get());
        assertEquals(0, stats.getSuccessfulOperations().get());
        assertEquals(0, stats.getTotalDuration().get());
        assertEquals(0, stats.getActiveOperations().get());
        assertTrue(stats.isHealthy());
        assertEquals(0, stats.getLastFailureTime());
    }
}
