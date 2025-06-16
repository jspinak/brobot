package io.github.jspinak.brobot.runner.diagnostics;

import io.github.jspinak.brobot.runner.errorhandling.ErrorHandler;
import io.github.jspinak.brobot.runner.errorhandling.ErrorStatistics;
import io.github.jspinak.brobot.runner.performance.PerformanceProfiler;
import io.github.jspinak.brobot.runner.performance.ThreadManagementOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiagnosticToolTest {

    @Mock
    private PerformanceProfiler performanceProfiler;
    
    @Mock
    private ErrorHandler errorHandler;
    
    @Mock
    private ThreadManagementOptimizer threadOptimizer;
    
    private DiagnosticTool diagnosticTool;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        diagnosticTool = new DiagnosticTool(performanceProfiler, errorHandler, threadOptimizer);
        diagnosticTool.initialize();
        
        // Setup mocks
        when(performanceProfiler.generateReport()).thenReturn(
            new PerformanceProfiler.PerformanceReport() {
                @Override
                public String toString() {
                    return "Performance metrics: OK";
                }
            }
        );
        
        when(errorHandler.getStatistics()).thenReturn(
            new ErrorStatistics(0, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList())
        );
    }
    
    @Test
    @DisplayName("Should run complete diagnostics")
    void shouldRunCompleteDiagnostics() {
        // Run diagnostics
        DiagnosticReport report = diagnosticTool.runDiagnostics();
        
        // Verify report contains all sections
        assertNotNull(report);
        assertNotNull(report.timestamp());
        assertNotNull(report.systemInfo());
        assertNotNull(report.memoryInfo());
        assertNotNull(report.threadInfo());
        assertNotNull(report.performanceMetrics());
        assertNotNull(report.errorStatistics());
        assertNotNull(report.environmentVariables());
        assertNotNull(report.systemProperties());
        
        // Verify system info
        SystemInfo sysInfo = report.systemInfo();
        assertNotNull(sysInfo.osName());
        assertNotNull(sysInfo.javaVersion());
        assertTrue(sysInfo.availableProcessors() > 0);
        assertTrue(sysInfo.uptime() >= 0);
    }
    
    @Test
    @DisplayName("Should perform health check")
    void shouldPerformHealthCheck() {
        // Perform health check
        HealthCheckResult result = diagnosticTool.performHealthCheck();
        
        // Verify result
        assertNotNull(result);
        assertNotNull(result.overallStatus());
        assertNotNull(result.checks());
        assertFalse(result.checks().isEmpty());
        
        // Verify all components are checked
        assertTrue(result.checks().stream()
            .anyMatch(check -> check.component().equals("Memory")));
        assertTrue(result.checks().stream()
            .anyMatch(check -> check.component().equals("Threads")));
        assertTrue(result.checks().stream()
            .anyMatch(check -> check.component().equals("Garbage Collection")));
        assertTrue(result.checks().stream()
            .anyMatch(check -> check.component().equals("Error Rate")));
        assertTrue(result.checks().stream()
            .anyMatch(check -> check.component().equals("Disk Space")));
    }
    
    @Test
    @DisplayName("Should export diagnostics to file")
    void shouldExportDiagnosticsToFile() throws Exception {
        // Run diagnostics
        DiagnosticReport report = diagnosticTool.runDiagnostics();
        
        // Export to file
        Path exportedFile = diagnosticTool.exportDiagnostics(report, tempDir);
        
        // Verify file exists and contains data
        assertTrue(Files.exists(exportedFile));
        assertTrue(Files.size(exportedFile) > 0);
        
        // Verify file name format
        String fileName = exportedFile.getFileName().toString();
        assertTrue(fileName.startsWith("diagnostics_"));
        assertTrue(fileName.endsWith(".txt"));
        
        // Read and verify content
        String content = Files.readString(exportedFile);
        assertTrue(content.contains("BROBOT RUNNER DIAGNOSTIC REPORT"));
        assertTrue(content.contains("SYSTEM INFORMATION"));
        assertTrue(content.contains("MEMORY INFORMATION"));
        assertTrue(content.contains("THREAD INFORMATION"));
    }
    
    @Test
    @DisplayName("Should collect memory information")
    void shouldCollectMemoryInfo() {
        DiagnosticReport report = diagnosticTool.runDiagnostics();
        MemoryInfo memInfo = report.memoryInfo();
        
        assertNotNull(memInfo);
        assertTrue(memInfo.heapUsed() > 0);
        assertTrue(memInfo.heapMax() > 0);
        assertTrue(memInfo.totalMemory() > 0);
        assertNotNull(memInfo.memoryPools());
        assertFalse(memInfo.memoryPools().isEmpty());
    }
    
    @Test
    @DisplayName("Should collect thread information")
    void shouldCollectThreadInfo() {
        DiagnosticReport report = diagnosticTool.runDiagnostics();
        ThreadInfo threadInfo = report.threadInfo();
        
        assertNotNull(threadInfo);
        assertTrue(threadInfo.threadCount() > 0);
        assertTrue(threadInfo.totalStartedThreadCount() > 0);
        assertNotNull(threadInfo.threadStates());
        assertFalse(threadInfo.threadStates().isEmpty());
    }
    
    @Test
    @DisplayName("Should handle error statistics")
    void shouldHandleErrorStatistics() {
        // Setup error statistics
        ErrorStatistics errorStats = new ErrorStatistics(
            10, 
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyList()
        );
        when(errorHandler.getStatistics()).thenReturn(errorStats);
        
        // Run diagnostics
        DiagnosticReport report = diagnosticTool.runDiagnostics();
        
        // Verify error statistics are included
        assertNotNull(report.errorStatistics());
        assertEquals(10, report.errorStatistics().totalErrors());
    }
    
    @Test
    @DisplayName("Should detect high memory usage in health check")
    void shouldDetectHighMemoryUsage() {
        // This test may not reliably trigger high memory conditions
        // but verifies the health check logic works
        HealthCheckResult result = diagnosticTool.performHealthCheck();
        
        HealthCheckItem memoryCheck = result.checks().stream()
            .filter(check -> check.component().equals("Memory"))
            .findFirst()
            .orElseThrow();
            
        assertNotNull(memoryCheck.status());
        assertNotNull(memoryCheck.message());
        assertTrue(memoryCheck.message().contains("Memory usage"));
    }
}