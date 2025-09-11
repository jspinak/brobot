package io.github.jspinak.brobot.runner.performance.thread;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("ThreadMonitoringService Tests")
class ThreadMonitoringServiceTest {

    private ThreadMonitoringService service;

    @Mock private ThreadPoolManagementService mockPoolManagement;

    @Mock private ThreadMXBean mockThreadBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ThreadMonitoringService(mockPoolManagement);

        // Use reflection to inject mock ThreadMXBean for testing
        try {
            var field = ThreadMonitoringService.class.getDeclaredField("threadBean");
            field.setAccessible(true);
            field.set(service, mockThreadBean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Setup default behavior
        when(mockThreadBean.isThreadContentionMonitoringSupported()).thenReturn(true);
        when(mockThreadBean.getPeakThreadCount()).thenReturn(10);
    }

    @Test
    @DisplayName("Should initialize with contention monitoring enabled")
    void testInitialize() {
        // Act
        service.initialize();

        // Assert
        verify(mockThreadBean).setThreadContentionMonitoringEnabled(true);
    }

    @Test
    @DisplayName("Should handle unsupported contention monitoring")
    void testInitializeUnsupported() {
        // Arrange
        when(mockThreadBean.isThreadContentionMonitoringSupported()).thenReturn(false);

        // Act
        service.initialize();

        // Assert
        verify(mockThreadBean, never()).setThreadContentionMonitoringEnabled(anyBoolean());
    }

    @Test
    @DisplayName("Should get current statistics")
    void testGetCurrentStatistics() {
        // Arrange
        when(mockThreadBean.getThreadCount()).thenReturn(20);
        when(mockThreadBean.getDaemonThreadCount()).thenReturn(5);
        when(mockThreadBean.getTotalStartedThreadCount()).thenReturn(100L);
        when(mockPoolManagement.getAllPoolHealth()).thenReturn(Map.of());

        // Act
        var stats = service.getCurrentStatistics();

        // Assert
        assertThat(stats.getCurrentThreadCount()).isEqualTo(20);
        assertThat(stats.getDaemonThreadCount()).isEqualTo(5);
        assertThat(stats.getTotalStartedThreads()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should detect thread contention")
    void testDetectContention() {
        // Arrange
        when(mockThreadBean.isThreadContentionMonitoringEnabled()).thenReturn(true);

        ThreadInfo mockThreadInfo = mock(ThreadInfo.class);
        when(mockThreadInfo.getThreadId()).thenReturn(1L);
        when(mockThreadInfo.getThreadName()).thenReturn("test-thread");
        when(mockThreadInfo.getBlockedCount()).thenReturn(15L);
        when(mockThreadInfo.getWaitedCount()).thenReturn(20L);
        when(mockThreadInfo.getBlockedTime()).thenReturn(1000L);
        when(mockThreadInfo.getWaitedTime()).thenReturn(2000L);

        long[] threadIds = {1L};
        when(mockThreadBean.getAllThreadIds()).thenReturn(threadIds);
        when(mockThreadBean.getThreadInfo(threadIds, Integer.MAX_VALUE))
                .thenReturn(new ThreadInfo[] {mockThreadInfo});

        // Act
        List<ThreadMonitoringService.ThreadContentionInfo> contentions = service.detectContention();

        // Assert
        assertThat(contentions).hasSize(1);
        assertThat(contentions.get(0).getThreadName()).isEqualTo("test-thread");
        assertThat(contentions.get(0).getBlockedCount()).isEqualTo(15L);
    }

    @Test
    @DisplayName("Should return empty list when contention monitoring disabled")
    void testDetectContentionDisabled() {
        // Arrange
        when(mockThreadBean.isThreadContentionMonitoringEnabled()).thenReturn(false);

        // Act
        List<ThreadMonitoringService.ThreadContentionInfo> contentions = service.detectContention();

        // Assert
        assertThat(contentions).isEmpty();
    }

    @Test
    @DisplayName("Should detect deadlocks")
    void testDetectDeadlocks() {
        // Arrange
        long[] deadlockedIds = {1L, 2L};
        when(mockThreadBean.findDeadlockedThreads()).thenReturn(deadlockedIds);

        ThreadInfo thread1 = mock(ThreadInfo.class);
        when(thread1.getThreadName()).thenReturn("deadlocked-1");
        when(thread1.getThreadId()).thenReturn(1L);

        ThreadInfo thread2 = mock(ThreadInfo.class);
        when(thread2.getThreadName()).thenReturn("deadlocked-2");
        when(thread2.getThreadId()).thenReturn(2L);

        when(mockThreadBean.getThreadInfo(deadlockedIds))
                .thenReturn(new ThreadInfo[] {thread1, thread2});

        // Act
        List<ThreadInfo> deadlocks = service.detectDeadlocks();

        // Assert
        assertThat(deadlocks).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when no deadlocks")
    void testDetectNoDeadlocks() {
        // Arrange
        when(mockThreadBean.findDeadlockedThreads()).thenReturn(null);

        // Act
        List<ThreadInfo> deadlocks = service.detectDeadlocks();

        // Assert
        assertThat(deadlocks).isEmpty();
    }

    @Test
    @DisplayName("Should get thread details")
    void testGetThreadDetails() {
        // Arrange
        long[] threadIds = {1L, 2L};
        ThreadInfo thread1 = mock(ThreadInfo.class);
        when(thread1.getThreadId()).thenReturn(1L);

        ThreadInfo thread2 = mock(ThreadInfo.class);
        when(thread2.getThreadId()).thenReturn(2L);

        when(mockThreadBean.getThreadInfo(threadIds, Integer.MAX_VALUE))
                .thenReturn(new ThreadInfo[] {thread1, thread2});

        // Act
        Map<Long, ThreadInfo> details = service.getThreadDetails(threadIds);

        // Assert
        assertThat(details).hasSize(2);
        assertThat(details).containsKeys(1L, 2L);
    }

    @Test
    @DisplayName("Should enable contention monitoring")
    void testEnableContentionMonitoring() {
        // Arrange
        when(mockThreadBean.isThreadContentionMonitoringSupported()).thenReturn(true);

        // Act
        service.enableContentionMonitoring(true);

        // Assert
        verify(mockThreadBean).setThreadContentionMonitoringEnabled(true);
    }

    @Test
    @DisplayName("Should reset peak thread count")
    void testResetPeakThreadCount() {
        // Arrange
        when(mockThreadBean.getThreadCount()).thenReturn(15);

        // Act
        service.resetPeakThreadCount();

        // Assert
        verify(mockThreadBean).resetPeakThreadCount();
    }

    @Test
    @DisplayName("Should provide diagnostic info")
    void testDiagnosticInfo() {
        // Arrange
        when(mockThreadBean.getThreadCount()).thenReturn(20);
        when(mockThreadBean.getDaemonThreadCount()).thenReturn(5);
        when(mockThreadBean.getTotalStartedThreadCount()).thenReturn(100L);
        when(mockThreadBean.isThreadContentionMonitoringEnabled()).thenReturn(true);
        when(mockPoolManagement.getAllPoolHealth()).thenReturn(Map.of());

        // Act
        var diagnosticInfo = service.getDiagnosticInfo();

        // Assert
        assertThat(diagnosticInfo.getComponent()).isEqualTo("ThreadMonitoringService");
        assertThat(diagnosticInfo.getStates())
                .containsEntry("currentThreads", 20)
                .containsEntry("daemonThreads", 5)
                .containsEntry("totalStartedThreads", 100L)
                .containsEntry("contentionMonitoringEnabled", true);
    }

    @Test
    @DisplayName("Should detect high contention")
    void testHighContentionDetection() {
        // Arrange
        var contentionInfo =
                new ThreadMonitoringService.ThreadContentionInfo(
                        "test-thread", 1L, 150L, 200L, 5000L, 10000L, "lock", "owner");

        // Assert
        assertThat(contentionInfo.isHighContention()).isTrue();
    }

    @Test
    @DisplayName("Should detect low contention")
    void testLowContentionDetection() {
        // Arrange
        var contentionInfo =
                new ThreadMonitoringService.ThreadContentionInfo(
                        "test-thread", 1L, 5L, 10L, 100L, 200L, "lock", "owner");

        // Assert
        assertThat(contentionInfo.isHighContention()).isFalse();
    }
}
