package io.github.jspinak.brobot.runner.diagnostics;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.diagnostics.services.*;
import io.github.jspinak.brobot.runner.errorhandling.ErrorHandler;
import io.github.jspinak.brobot.runner.errorhandling.ErrorStatistics;

/** Unit tests for DiagnosticTool. */
class DiagnosticToolTest {

    @Mock private SystemInfoCollectorService systemInfoService;

    @Mock private MemoryDiagnosticService memoryService;

    @Mock private ThreadDiagnosticService threadService;

    @Mock private PerformanceDiagnosticService performanceService;

    @Mock private HealthCheckService healthCheckService;

    @Mock private DiagnosticReportService reportService;

    @Mock private ErrorHandler errorHandler;

    private DiagnosticTool diagnosticTool;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        diagnosticTool =
                new DiagnosticTool(
                        systemInfoService,
                        memoryService,
                        threadService,
                        performanceService,
                        healthCheckService,
                        reportService,
                        errorHandler);
    }

    @Test
    @DisplayName("Should initialize diagnostic tool")
    void testInitialize() {
        // When
        diagnosticTool.initialize();

        // Then
        // No exception should be thrown
    }

    @Test
    @DisplayName("Should run diagnostics successfully")
    void testRunDiagnostics() {
        // Given
        SystemInfo mockSystemInfo =
                SystemInfo.builder()
                        .osName("TestOS")
                        .osVersion("1.0")
                        .osArch("x64")
                        .javaVersion("11")
                        .javaVendor("Test")
                        .jvmName("TestJVM")
                        .jvmVersion("1.0")
                        .availableProcessors(4)
                        .systemLoadAverage(1.5)
                        .uptime(1000L)
                        .build();

        MemoryInfo mockMemoryInfo =
                MemoryInfo.builder()
                        .heapUsed(1000L)
                        .heapMax(2000L)
                        .heapCommitted(1500L)
                        .nonHeapUsed(500L)
                        .nonHeapMax(1000L)
                        .nonHeapCommitted(750L)
                        .memoryPools(new HashMap<>())
                        .build();

        ThreadDiagnosticInfo mockThreadInfo =
                ThreadDiagnosticInfo.builder()
                        .threadCount(10)
                        .peakThreadCount(15)
                        .daemonThreadCount(5)
                        .totalStartedThreadCount(100L)
                        .deadlockedThreads(0)
                        .threadStates(new HashMap<>())
                        .build();

        PerformanceMetrics mockPerformanceMetrics =
                PerformanceMetrics.builder()
                        .performanceReport("Test Report")
                        .gcTotalCount(10L)
                        .gcTotalTime(100L)
                        .gcDetails(new HashMap<>())
                        .cpuUsage(50.0)
                        .build();

        ErrorStatistics mockErrorStats =
                new ErrorStatistics(
                        0L, new HashMap<>(), Collections.emptyList(), Collections.emptyList());

        HealthCheckResult mockHealthResult =
                new HealthCheckResult(
                        HealthStatus.HEALTHY,
                        List.of(new HealthCheckItem("Test", HealthStatus.HEALTHY, "All good")));

        when(systemInfoService.collectSystemInfo()).thenReturn(mockSystemInfo);
        when(memoryService.collectMemoryInfo()).thenReturn(mockMemoryInfo);
        when(threadService.collectThreadInfo()).thenReturn(mockThreadInfo);
        when(performanceService.collectPerformanceMetrics()).thenReturn(mockPerformanceMetrics);
        when(errorHandler.getStatistics()).thenReturn(mockErrorStats);
        when(healthCheckService.performHealthCheck()).thenReturn(mockHealthResult);

        // When
        DiagnosticReport report = diagnosticTool.runDiagnostics();

        // Then
        assertNotNull(report);
        assertNotNull(report.timestamp());
        assertEquals(mockSystemInfo, report.systemInfo());
        assertEquals(mockMemoryInfo, report.memoryInfo());
        assertEquals(mockThreadInfo, report.threadInfo());
        assertEquals(mockPerformanceMetrics, report.performanceMetrics());
        assertEquals(mockErrorStats, report.errorStatistics());
        assertEquals(mockHealthResult, report.healthCheckResult());
        assertNotNull(report.environmentVariables());
        assertNotNull(report.systemProperties());

        verify(systemInfoService).collectSystemInfo();
        verify(memoryService).collectMemoryInfo();
        verify(threadService).collectThreadInfo();
        verify(performanceService).collectPerformanceMetrics();
        verify(errorHandler).getStatistics();
        verify(healthCheckService).performHealthCheck();
    }

    @Test
    @DisplayName("Should perform health check")
    void testPerformHealthCheck() {
        // Given
        HealthCheckResult mockResult =
                new HealthCheckResult(
                        HealthStatus.WARNING,
                        List.of(
                                new HealthCheckItem("Memory", HealthStatus.WARNING, "High usage"),
                                new HealthCheckItem("CPU", HealthStatus.HEALTHY, "Normal")));
        when(healthCheckService.performHealthCheck()).thenReturn(mockResult);

        // When
        HealthCheckResult result = diagnosticTool.performHealthCheck();

        // Then
        assertNotNull(result);
        assertEquals(HealthStatus.WARNING, result.overallStatus());
        assertEquals(2, result.checks().size());
        verify(healthCheckService).performHealthCheck();
    }

    @Test
    @DisplayName("Should export diagnostics to file")
    void testExportDiagnostics() throws IOException {
        // Given
        DiagnosticReport mockReport = createMockReport();
        Path expectedPath = tempDir.resolve("diagnostic-report.txt");
        when(reportService.saveReport(mockReport, tempDir)).thenReturn(expectedPath);

        // When
        Path resultPath = diagnosticTool.exportDiagnostics(mockReport, tempDir);

        // Then
        assertEquals(expectedPath, resultPath);
        verify(reportService).saveReport(mockReport, tempDir);
    }

    @Test
    @DisplayName("Should get system summary")
    void testGetSystemSummary() {
        // Given
        setupMocksForRunDiagnostics();
        String expectedSummary = "System Summary: All systems operational";
        when(reportService.generateSummary(any())).thenReturn(expectedSummary);

        // When
        String summary = diagnosticTool.getSystemSummary();

        // Then
        assertEquals(expectedSummary, summary);
        verify(reportService).generateSummary(any());
    }

    @Test
    @DisplayName("Should get formatted report")
    void testGetFormattedReport() {
        // Given
        setupMocksForRunDiagnostics();
        String expectedReport = "=== DIAGNOSTIC REPORT ===\nAll systems operational";
        when(reportService.formatReport(any())).thenReturn(expectedReport);

        // When
        String report = diagnosticTool.getFormattedReport();

        // Then
        assertEquals(expectedReport, report);
        verify(reportService).formatReport(any());
    }

    @Test
    @DisplayName("Should check if system is healthy")
    void testIsSystemHealthy() {
        // Given
        HealthCheckResult healthyResult =
                new HealthCheckResult(
                        HealthStatus.HEALTHY,
                        List.of(new HealthCheckItem("All", HealthStatus.HEALTHY, "Good")));
        when(healthCheckService.performHealthCheck()).thenReturn(healthyResult);

        // When
        boolean isHealthy = diagnosticTool.isSystemHealthy();

        // Then
        assertTrue(isHealthy);

        // Given unhealthy
        HealthCheckResult unhealthyResult =
                new HealthCheckResult(
                        HealthStatus.WARNING,
                        List.of(new HealthCheckItem("Memory", HealthStatus.WARNING, "High")));
        when(healthCheckService.performHealthCheck()).thenReturn(unhealthyResult);

        // When
        boolean isUnhealthy = diagnosticTool.isSystemHealthy();

        // Then
        assertFalse(isUnhealthy);
    }

    @Test
    @DisplayName("Should get component health")
    void testGetComponentHealth() {
        // Given
        HealthCheckItem memoryHealth =
                new HealthCheckItem("Memory", HealthStatus.HEALTHY, "Normal");
        when(healthCheckService.getComponentHealth("memory")).thenReturn(memoryHealth);

        // When
        HealthCheckItem result = diagnosticTool.getComponentHealth("memory");

        // Then
        assertEquals(memoryHealth, result);
        verify(healthCheckService).getComponentHealth("memory");
    }

    @Test
    @DisplayName("Should force garbage collection")
    void testForceGarbageCollection() {
        // When
        diagnosticTool.forceGarbageCollection();

        // Then
        verify(memoryService).runGarbageCollection();
    }

    @Test
    @DisplayName("Should get memory usage percentage")
    void testGetMemoryUsagePercentage() {
        // Given
        when(memoryService.getHeapUsagePercentage()).thenReturn(75.5);

        // When
        double usage = diagnosticTool.getMemoryUsagePercentage();

        // Then
        assertEquals(75.5, usage);
        verify(memoryService).getHeapUsagePercentage();
    }

    @Test
    @DisplayName("Should get thread count")
    void testGetThreadCount() {
        // Given
        ThreadDiagnosticInfo mockInfo =
                ThreadDiagnosticInfo.builder()
                        .threadCount(25)
                        .peakThreadCount(30)
                        .daemonThreadCount(10)
                        .totalStartedThreadCount(100L)
                        .deadlockedThreads(0)
                        .threadStates(new HashMap<>())
                        .build();
        when(threadService.collectThreadInfo()).thenReturn(mockInfo);

        // When
        int count = diagnosticTool.getThreadCount();

        // Then
        assertEquals(25, count);
        verify(threadService).collectThreadInfo();
    }

    @Test
    @DisplayName("Should check for deadlocked threads")
    void testHasDeadlockedThreads() {
        // Given - no deadlocks
        ThreadDiagnosticInfo noDeadlocks =
                ThreadDiagnosticInfo.builder().threadCount(25).deadlockedThreads(0).build();
        when(threadService.collectThreadInfo()).thenReturn(noDeadlocks);

        // When/Then
        assertFalse(diagnosticTool.hasDeadlockedThreads());

        // Given - has deadlocks
        ThreadDiagnosticInfo hasDeadlocks =
                ThreadDiagnosticInfo.builder().threadCount(25).deadlockedThreads(2).build();
        when(threadService.collectThreadInfo()).thenReturn(hasDeadlocks);

        // When/Then
        assertTrue(diagnosticTool.hasDeadlockedThreads());
    }

    @Test
    @DisplayName("Should get GC overhead percentage")
    void testGetGCOverheadPercentage() {
        // Given
        when(performanceService.getGCOverheadPercentage()).thenReturn(3.5);

        // When
        double overhead = diagnosticTool.getGCOverheadPercentage();

        // Then
        assertEquals(3.5, overhead);
        verify(performanceService).getGCOverheadPercentage();
    }

    @Test
    @DisplayName("Should provide diagnostic info")
    void testGetDiagnosticInfo() {
        // Given
        when(memoryService.getHeapUsagePercentage()).thenReturn(60.0);
        when(performanceService.getGCOverheadPercentage()).thenReturn(2.0);

        ThreadDiagnosticInfo mockThreadInfo =
                ThreadDiagnosticInfo.builder().threadCount(20).deadlockedThreads(0).build();
        when(threadService.collectThreadInfo()).thenReturn(mockThreadInfo);

        HealthCheckResult healthResult =
                new HealthCheckResult(HealthStatus.HEALTHY, Collections.emptyList());
        when(healthCheckService.performHealthCheck()).thenReturn(healthResult);

        // When
        DiagnosticInfo info = diagnosticTool.getDiagnosticInfo();

        // Then
        assertNotNull(info);
        assertEquals("DiagnosticTool", info.getComponent());

        Map<String, Object> states = info.getStates();
        assertEquals(60.0, states.get("memory_usage_percent"));
        assertEquals(20, states.get("thread_count"));
        assertEquals(false, states.get("has_deadlocks"));
        assertEquals(2.0, states.get("gc_overhead_percent"));
        assertEquals(true, states.get("system_healthy"));

        @SuppressWarnings("unchecked")
        Map<String, Boolean> services = (Map<String, Boolean>) states.get("services_available");
        assertTrue(services.get("systemInfo"));
        assertTrue(services.get("memory"));
        assertTrue(services.get("thread"));
        assertTrue(services.get("performance"));
        assertTrue(services.get("health"));
        assertTrue(services.get("report"));
    }

    private void setupMocksForRunDiagnostics() {
        when(systemInfoService.collectSystemInfo()).thenReturn(SystemInfo.builder().build());
        when(memoryService.collectMemoryInfo()).thenReturn(MemoryInfo.builder().build());
        when(threadService.collectThreadInfo()).thenReturn(ThreadDiagnosticInfo.builder().build());
        when(performanceService.collectPerformanceMetrics())
                .thenReturn(PerformanceMetrics.builder().build());
        when(errorHandler.getStatistics())
                .thenReturn(
                        new ErrorStatistics(
                                0L,
                                new HashMap<>(),
                                Collections.emptyList(),
                                Collections.emptyList()));
        when(healthCheckService.performHealthCheck())
                .thenReturn(new HealthCheckResult(HealthStatus.HEALTHY, Collections.emptyList()));
    }

    private DiagnosticReport createMockReport() {
        return DiagnosticReport.builder()
                .timestamp(LocalDateTime.now())
                .systemInfo(SystemInfo.builder().build())
                .memoryInfo(MemoryInfo.builder().build())
                .threadInfo(ThreadDiagnosticInfo.builder().build())
                .performanceMetrics(PerformanceMetrics.builder().build())
                .errorStatistics(
                        new ErrorStatistics(
                                0L,
                                new HashMap<>(),
                                Collections.emptyList(),
                                Collections.emptyList()))
                .healthCheckResult(
                        new HealthCheckResult(HealthStatus.HEALTHY, Collections.emptyList()))
                .environmentVariables(new HashMap<>())
                .systemProperties(new HashMap<>())
                .build();
    }
}
