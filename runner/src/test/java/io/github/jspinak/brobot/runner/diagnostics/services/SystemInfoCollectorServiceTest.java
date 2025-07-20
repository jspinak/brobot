package io.github.jspinak.brobot.runner.diagnostics.services;

import io.github.jspinak.brobot.runner.diagnostics.SystemInfo;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SystemInfoCollectorService.
 */
class SystemInfoCollectorServiceTest {
    
    private SystemInfoCollectorService service;
    
    @BeforeEach
    void setUp() {
        service = new SystemInfoCollectorService();
        service.initialize();
    }
    
    @Test
    @DisplayName("Should collect system information successfully")
    void testCollectSystemInfo() {
        // When
        SystemInfo systemInfo = service.collectSystemInfo();
        
        // Then
        assertNotNull(systemInfo);
        assertNotNull(systemInfo.osName());
        assertNotNull(systemInfo.osVersion());
        assertNotNull(systemInfo.osArch());
        assertNotNull(systemInfo.javaVersion());
        assertNotNull(systemInfo.javaVendor());
        assertNotNull(systemInfo.jvmName());
        assertNotNull(systemInfo.jvmVersion());
        assertTrue(systemInfo.availableProcessors() > 0);
        assertTrue(systemInfo.uptime() >= 0);
    }
    
    @Test
    @DisplayName("Should collect environment variables")
    void testCollectEnvironmentVariables() {
        // When
        Map<String, String> envVars = service.collectEnvironmentVariables();
        
        // Then
        assertNotNull(envVars);
        assertFalse(envVars.isEmpty());
        // Check for common environment variables
        assertTrue(envVars.containsKey("PATH") || envVars.containsKey("Path"));
    }
    
    @Test
    @DisplayName("Should collect system properties")
    void testCollectSystemProperties() {
        // When
        Map<String, String> sysProps = service.collectSystemProperties();
        
        // Then
        assertNotNull(sysProps);
        assertFalse(sysProps.isEmpty());
        assertTrue(sysProps.containsKey("java.version"));
        assertTrue(sysProps.containsKey("os.name"));
    }
    
    @Test
    @DisplayName("Should get CPU usage")
    void testGetCpuUsage() {
        // When
        double cpuUsage = service.getCpuUsage();
        
        // Then
        assertTrue(cpuUsage >= 0.0);
        assertTrue(cpuUsage <= 100.0);
    }
    
    @Test
    @DisplayName("Should get system load average")
    void testGetSystemLoadAverage() {
        // When
        double loadAverage = service.getSystemLoadAverage();
        
        // Then
        // Load average can be -1 if not available on the platform
        assertTrue(loadAverage >= -1.0);
    }
    
    @Test
    @DisplayName("Should get uptime")
    void testGetUptime() {
        // When
        long uptime = service.getUptime();
        
        // Then
        assertTrue(uptime > 0);
    }
    
    @Test
    @DisplayName("Should get start time")
    void testGetStartTime() {
        // When
        Date startTime = service.getStartTime();
        
        // Then
        assertNotNull(startTime);
        assertTrue(startTime.before(new Date()));
    }
    
    @Test
    @DisplayName("Should get JVM arguments")
    void testGetJvmArguments() {
        // When
        List<String> jvmArgs = service.getJvmArguments();
        
        // Then
        assertNotNull(jvmArgs);
        // May be empty depending on how the test is run
    }
    
    @Test
    @DisplayName("Should get class path")
    void testGetClassPath() {
        // When
        String classPath = service.getClassPath();
        
        // Then
        assertNotNull(classPath);
        assertFalse(classPath.isEmpty());
    }
    
    @Test
    @DisplayName("Should detect containerized environment")
    void testIsContainerized() {
        // When
        boolean isContainerized = service.isContainerized();
        
        // Then
        // We can't assert the value as it depends on the environment
        // Just ensure it doesn't throw an exception
        assertNotNull(isContainerized);
    }
    
    @Test
    @DisplayName("Should get available disk space")
    void testGetAvailableDiskSpace() {
        // When
        long availableSpace = service.getAvailableDiskSpace();
        
        // Then
        assertTrue(availableSpace >= 0);
    }
    
    @Test
    @DisplayName("Should get total disk space")
    void testGetTotalDiskSpace() {
        // When
        long totalSpace = service.getTotalDiskSpace();
        
        // Then
        assertTrue(totalSpace > 0);
    }
    
    @Test
    @DisplayName("Should enable and check diagnostic mode")
    void testDiagnosticMode() {
        // Given
        assertFalse(service.isDiagnosticModeEnabled());
        
        // When
        service.enableDiagnosticMode(true);
        
        // Then
        assertTrue(service.isDiagnosticModeEnabled());
        
        // When
        service.enableDiagnosticMode(false);
        
        // Then
        assertFalse(service.isDiagnosticModeEnabled());
    }
    
    @Test
    @DisplayName("Should provide diagnostic info")
    void testGetDiagnosticInfo() {
        // When
        DiagnosticInfo diagnosticInfo = service.getDiagnosticInfo();
        
        // Then
        assertNotNull(diagnosticInfo);
        assertEquals("SystemInfoCollectorService", diagnosticInfo.getComponent());
        assertNotNull(diagnosticInfo.getStates());
        
        Map<String, Object> states = diagnosticInfo.getStates();
        assertTrue(states.containsKey("os_name"));
        assertTrue(states.containsKey("java_version"));
        assertTrue(states.containsKey("available_processors"));
        assertTrue(states.containsKey("total_memory_mb"));
        assertTrue(states.containsKey("free_memory_mb"));
        assertTrue(states.containsKey("uptime_hours"));
        assertTrue(states.containsKey("is_containerized"));
    }
    
    @Test
    @DisplayName("Should handle concurrent access")
    void testConcurrentAccess() throws InterruptedException {
        // Given
        final int threadCount = 10;
        final boolean[] errors = new boolean[1];
        Thread[] threads = new Thread[threadCount];
        
        // When
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    SystemInfo info = service.collectSystemInfo();
                    assertNotNull(info);
                } catch (Exception e) {
                    errors[0] = true;
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then
        assertFalse(errors[0], "No errors should occur during concurrent access");
    }
}