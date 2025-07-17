package io.github.jspinak.brobot.config;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigurationDiagnostics focusing on issue detection and suggestions.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigurationDiagnosticsTest {
    
    private ConfigurationDiagnostics diagnostics;
    private BrobotConfiguration configuration;
    private ImagePathManager pathManager;
    private SmartImageLoader imageLoader;
    private ExecutionEnvironment environment;
    
    @BeforeEach
    void setup() {
        configuration = new BrobotConfiguration();
        pathManager = new ImagePathManager();
        environment = ExecutionEnvironment.builder().build();
        imageLoader = new SmartImageLoader(pathManager, environment);
        
        diagnostics = new ConfigurationDiagnostics(
            configuration, pathManager, imageLoader, environment
        );
    }
    
    @Test
    @DisplayName("Run full diagnostics report")
    void testFullDiagnosticsReport() {
        // Run diagnostics
        var report = diagnostics.runFullDiagnostics();
        
        assertNotNull(report);
        
        // Verify all sections are present
        assertNotNull(report.getSection("Environment Detection"));
        assertNotNull(report.getSection("Configuration Validation"));
        assertNotNull(report.getSection("Image Path Configuration"));
        assertNotNull(report.getSection("Runtime Capabilities"));
        assertNotNull(report.getSection("Common Issues"));
        
        // Test formatted output
        String formatted = report.toFormattedString();
        assertNotNull(formatted);
        assertTrue(formatted.contains("Brobot Configuration Diagnostics"));
        assertTrue(formatted.contains("Environment Detection"));
    }
    
    @Test
    @DisplayName("Environment detection diagnostics")
    void testEnvironmentDetection() {
        var report = diagnostics.runFullDiagnostics();
        var envSection = report.getSection("Environment Detection");
        
        assertNotNull(envSection);
        
        // Verify key environment info is captured
        assertNotNull(envSection.get("os.name"));
        assertNotNull(envSection.get("java.version"));
        assertNotNull(envSection.get("user.dir"));
        assertNotNull(envSection.get("display.available"));
        assertNotNull(envSection.get("mock.mode"));
        
        // Check environment variables section
        @SuppressWarnings("unchecked")
        var envVars = (Map<String, String>) envSection.get("environment.variables");
        assertNotNull(envVars);
    }
    
    @Test
    @DisplayName("Configuration validation diagnostics")
    void testConfigurationValidation() {
        // Set some configuration
        configuration.getCore().setImagePath("test-images");
        configuration.getEnvironment().setProfile("testing");
        
        var report = diagnostics.runFullDiagnostics();
        var configSection = report.getSection("Configuration Validation");
        
        assertNotNull(configSection);
        assertEquals("testing", configSection.get("active.profile"));
        assertEquals("test-images", configSection.get("image.path"));
        
        @SuppressWarnings("unchecked")
        var errors = (List<String>) configSection.get("validation.errors");
        assertNotNull(errors);
    }
    
    @Test
    @DisplayName("Image path validation diagnostics")
    void testImagePathDiagnostics() throws IOException {
        // Create test directory
        Path tempDir = Files.createTempDirectory("diag-test");
        pathManager.initialize(tempDir.toString());
        
        try {
            var report = diagnostics.runFullDiagnostics();
            var pathSection = report.getSection("Image Path Configuration");
            
            assertNotNull(pathSection);
            
            @SuppressWarnings("unchecked")
            var paths = (List<String>) pathSection.get("configured.paths");
            assertFalse(paths.isEmpty());
            
            assertNotNull(pathSection.get("path.status"));
            assertNotNull(pathSection.get("images.found"));
            
        } finally {
            Files.deleteIfExists(tempDir);
        }
    }
    
    @Test
    @DisplayName("Common issues detection - headless with screen capture")
    void testHeadlessWithScreenCaptureIssue() {
        // Create problematic configuration
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .forceHeadless(true)
            .allowScreenCapture(true)
            .build();
        
        configuration.getCore().setForceHeadless(true);
        configuration.getCore().setAllowScreenCapture(true);
        
        ConfigurationDiagnostics diag = new ConfigurationDiagnostics(
            configuration, pathManager, imageLoader, env
        );
        
        var report = diag.runFullDiagnostics();
        var issues = report.getSection("Common Issues");
        
        @SuppressWarnings("unchecked")
        var detectedIssues = (List<String>) issues.get("detected.issues");
        
        assertTrue(detectedIssues.stream()
            .anyMatch(issue -> issue.contains("Screen capture enabled but no display")));
    }
    
    @Test
    @DisplayName("Common issues detection - no image paths")
    void testNoImagePathsIssue() {
        // Don't initialize path manager
        var report = diagnostics.runFullDiagnostics();
        var issues = report.getSection("Common Issues");
        
        @SuppressWarnings("unchecked")
        var detectedIssues = (List<String>) issues.get("detected.issues");
        
        assertTrue(detectedIssues.stream()
            .anyMatch(issue -> issue.contains("No image paths configured")));
    }
    
    @Test
    @DisplayName("Suggestions generation")
    void testSuggestionsGeneration() {
        // Create configuration with issues
        configuration.getCore().setAllowScreenCapture(true);
        configuration.getCore().setForceHeadless(true);
        
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .forceHeadless(true)
            .build();
        
        ConfigurationDiagnostics diag = new ConfigurationDiagnostics(
            configuration, pathManager, imageLoader, env
        );
        
        var report = diag.runFullDiagnostics();
        
        // Should have suggestions
        String formatted = report.toFormattedString();
        assertTrue(formatted.contains("Suggestions"));
        assertTrue(formatted.contains("brobot.core.force-headless=true"));
    }
    
    @Test
    @DisplayName("WSL environment detection")
    void testWSLDetection() {
        // This test checks if WSL detection logic works
        var report = diagnostics.runFullDiagnostics();
        
        // If running in WSL, should detect it
        boolean isWSL = System.getenv("WSL_DISTRO_NAME") != null;
        
        if (isWSL) {
            var issues = report.getSection("Common Issues");
            @SuppressWarnings("unchecked")
            var detectedIssues = (List<String>) issues.get("detected.issues");
            
            // If no DISPLAY set, should detect issue
            if (System.getenv("DISPLAY") == null) {
                assertTrue(detectedIssues.stream()
                    .anyMatch(issue -> issue.contains("WSL but DISPLAY not set")));
            }
        }
    }
    
    @Test
    @DisplayName("Configuration validity check")
    void testConfigurationValidity() {
        // Valid configuration
        configuration.getCore().setImagePath("images");
        pathManager.initialize("images");
        
        assertTrue(diagnostics.isConfigurationValid());
        
        // Invalid configuration - create issues
        configuration.getCore().setAllowScreenCapture(true);
        configuration.getCore().setForceHeadless(true);
        
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .forceHeadless(true)
            .build();
        
        ConfigurationDiagnostics diag = new ConfigurationDiagnostics(
            configuration, pathManager, imageLoader, env
        );
        
        // Should detect issues
        assertFalse(diag.isConfigurationValid());
    }
    
    @Test
    @DisplayName("Runtime capabilities diagnostics")
    void testRuntimeCapabilities() {
        var report = diagnostics.runFullDiagnostics();
        var capabilities = report.getSection("Runtime Capabilities");
        
        assertNotNull(capabilities);
        
        // Check image loading capability
        assertNotNull(capabilities.get("image.loading"));
        
        // Check screen capture capability
        assertNotNull(capabilities.get("screen.capture"));
        
        // Check memory information
        assertNotNull(capabilities.get("memory.max.mb"));
        assertNotNull(capabilities.get("memory.total.mb"));
        assertNotNull(capabilities.get("memory.free.mb"));
    }
    
    @Test
    @DisplayName("Print diagnostic report")
    void testPrintDiagnosticReport() {
        // Capture output
        var originalOut = System.out;
        var baos = new java.io.ByteArrayOutputStream();
        var ps = new java.io.PrintStream(baos);
        System.setOut(ps);
        
        try {
            diagnostics.printDiagnosticReport();
            String output = baos.toString();
            
            // Verify output contains expected sections
            assertTrue(output.contains("Brobot Configuration Diagnostics"));
            assertTrue(output.contains("Environment Detection"));
            assertTrue(output.contains("Configuration Validation"));
            
        } finally {
            System.setOut(originalOut);
        }
    }
}