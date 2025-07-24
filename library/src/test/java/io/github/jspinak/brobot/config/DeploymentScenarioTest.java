package io.github.jspinak.brobot.config;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for different deployment scenarios.
 * Tests the enhanced Brobot configuration system across various environments.
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeploymentScenarioTest {
    
    private Path testImageDir;
    private Path testJarDir;
    
    @BeforeAll
    void setup() throws IOException {
        // Create test directories
        testImageDir = Files.createTempDirectory("brobot-test-images");
        testJarDir = Files.createTempDirectory("brobot-test-jar");
        
        // Create test images
        createTestImage(testImageDir.resolve("test.png"));
        createTestImage(testImageDir.resolve("button.png"));
    }
    
    @AfterAll
    void cleanup() throws IOException {
        // Clean up test directories
        deleteDirectory(testImageDir);
        deleteDirectory(testJarDir);
    }
    
    @Test
    @DisplayName("Development Environment - IDE with relative paths")
    void testDevelopmentEnvironment() {
        // Setup development environment
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .mockMode(false)
            .forceHeadless(false)
            .verboseLogging(true)
            .build();
        ExecutionEnvironment.setInstance(env);
        
        BrobotConfiguration config = new BrobotConfiguration();
        config.getCore().setImagePath("images");
        config.getEnvironment().setProfile("development");
        config.validate();
        
        // Verify environment detection
        assertEquals("development", config.getEnvironment().getProfile());
        assertTrue(config.getCore().isVerboseLogging());
        assertTrue(config.getSikuli().isVisualDebugging());
        
        // Test image path resolution
        ImagePathManager pathManager = new ImagePathManager();
        pathManager.initialize("images");
        
        assertFalse(pathManager.getConfiguredPaths().isEmpty());
    }
    
    @Test
    @DisplayName("CI/CD Environment - Headless with fast timeouts")
    void testCICDEnvironment() {
        // Simulate CI environment
        System.setProperty("CI", "true");
        
        try {
            BrobotConfiguration config = new BrobotConfiguration();
            config.validate(); // Should auto-detect CI
            
            // Verify CI configuration
            assertEquals("ci", config.getEnvironment().getProfile());
            assertTrue(config.getEnvironment().isCiMode());
            assertEquals(1.0, config.getCore().getFindTimeout());
            assertEquals(0.1, config.getCore().getActionPause());
            
            // Verify execution environment
            ExecutionEnvironment env = config.getExecutionEnvironment();
            assertTrue(env.hasDisplay() == false || env.isMockMode());
            
        } finally {
            System.clearProperty("CI");
        }
    }
    
    @Test
    @DisplayName("JAR Deployment - External image directory")
    void testJarDeployment() throws IOException {
        // Simulate JAR deployment scenario
        ImagePathManager pathManager = new ImagePathManager();
        SmartImageLoader imageLoader = new SmartImageLoader(pathManager, ExecutionEnvironment.getInstance());
        
        // Test external directory next to JAR
        Path externalImages = testJarDir.resolve("images");
        Files.createDirectories(externalImages);
        createTestImage(externalImages.resolve("app-icon.png"));
        
        pathManager.initialize(externalImages.toString());
        
        // Verify image can be loaded
        assertTrue(pathManager.validatePaths());
        assertEquals(1, pathManager.getConfiguredPaths().size());
    }
    
    @Test
    @DisplayName("Mock Mode - Testing without real images")
    void testMockMode() {
        // Setup mock environment
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .mockMode(true)
            .build();
        ExecutionEnvironment.setInstance(env);
        
        // Verify mock mode behavior
        assertTrue(env.isMockMode());
        assertFalse(env.canCaptureScreen());
        assertFalse(env.useRealFiles());
        assertTrue(env.shouldSkipSikuliX());
        
        // Test image loading in mock mode
        SmartImageLoader imageLoader = new SmartImageLoader(
            new ImagePathManager(), 
            env
        );
        
        // Should return placeholder image
        var image = imageLoader.loadImageDirect("non-existent.png");
        assertNotNull(image);
        assertEquals(100, image.getWidth());
        assertEquals(100, image.getHeight());
    }
    
    @Test
    @DisplayName("Windows Environment - Display detection")
    void testWindowsEnvironment() {
        // This test checks Windows-specific logic
        String originalOS = System.getProperty("os.name");
        System.setProperty("os.name", "Windows 10");
        
        try {
            ExecutionEnvironment env = ExecutionEnvironment.builder()
                .fromEnvironment()
                .build();
            
            // On Windows (not WSL), display should be available unless in CI
            if (!isRunningInCI() && !isWSL()) {
                assertTrue(env.hasDisplay());
            }
            
        } finally {
            System.setProperty("os.name", originalOS);
        }
    }
    
    @Test
    @DisplayName("WSL Environment - DISPLAY variable check")
    void testWSLEnvironment() {
        // Simulate WSL environment
        Map<String, String> env = System.getenv();
        boolean isActuallyWSL = env.containsKey("WSL_DISTRO_NAME");
        
        if (isActuallyWSL) {
            ExecutionEnvironment execEnv = ExecutionEnvironment.builder()
                .fromEnvironment()
                .build();
            
            // In WSL, display availability depends on DISPLAY variable
            String display = System.getenv("DISPLAY");
            assertEquals(display != null && !display.isEmpty(), execEnv.hasDisplay());
        }
    }
    
    @Test
    @DisplayName("Docker Environment - Container detection")
    void testDockerEnvironment() {
        // Check if running in Docker
        boolean inDocker = Files.exists(Paths.get("/.dockerenv"));
        
        BrobotConfiguration config = new BrobotConfiguration();
        config.validate();
        
        assertEquals(inDocker, config.getEnvironment().isDockerMode());
    }
    
    @Test
    @DisplayName("Configuration Diagnostics - Issue detection")
    void testConfigurationDiagnostics() {
        // Setup problematic configuration
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .forceHeadless(true)
            .allowScreenCapture(true) // Incompatible!
            .build();
        
        BrobotConfiguration config = new BrobotConfiguration();
        config.getCore().setAllowScreenCapture(true);
        config.getCore().setForceHeadless(true);
        
        ImagePathManager pathManager = new ImagePathManager();
        SmartImageLoader imageLoader = new SmartImageLoader(pathManager, env);
        
        ConfigurationDiagnostics diagnostics = new ConfigurationDiagnostics(
            config, pathManager, imageLoader, env
        );
        
        // Run diagnostics
        var report = diagnostics.runFullDiagnostics();
        
        // Should detect the incompatible configuration
        assertNotNull(report);
        
        // Check for common issues
        Map<String, Object> commonIssues = report.getSection("Common Issues");
        assertNotNull(commonIssues);
    }
    
    @Test
    @DisplayName("Image Path Resolution - Multiple strategies")
    void testImagePathResolution() throws IOException {
        ImagePathManager pathManager = new ImagePathManager();
        
        // Test 1: Absolute path
        Path absolutePath = testImageDir.toAbsolutePath();
        pathManager.initialize(absolutePath.toString());
        assertTrue(pathManager.validatePaths());
        
        // Test 2: Relative path
        pathManager = new ImagePathManager();
        pathManager.initialize("src/test/resources/images");
        // May or may not exist, but should not throw exception
        
        // Test 3: Classpath
        pathManager = new ImagePathManager();
        pathManager.initialize("classpath:images");
        // Should handle gracefully even if not found
        
        // Test 4: Multiple paths
        pathManager = new ImagePathManager();
        pathManager.initialize(testImageDir.toString());
        pathManager.addPath(testImageDir.toString());
        assertEquals(1, pathManager.getConfiguredPaths().size()); // Should not duplicate
    }
    
    @Test
    @DisplayName("Smart Image Loader - Caching and fallback")
    void testSmartImageLoader() throws IOException {
        ImagePathManager pathManager = new ImagePathManager();
        pathManager.initialize(testImageDir.toString());
        
        SmartImageLoader imageLoader = new SmartImageLoader(
            pathManager,
            ExecutionEnvironment.getInstance()
        );
        
        // Test 1: Load existing image
        var image1 = imageLoader.loadImage("test.png");
        assertNotNull(image1);
        
        // Test 2: Load from cache (should be fast)
        long start = System.currentTimeMillis();
        var image2 = imageLoader.loadImage("test.png");
        long loadTime = System.currentTimeMillis() - start;
        
        assertNotNull(image2);
        assertTrue(loadTime < 10); // Should be very fast from cache
        
        // Test 3: Non-existent image (should return placeholder)
        var placeholder = imageLoader.loadImage("non-existent.png");
        assertNotNull(placeholder);
        
        // Test 4: Get diagnostics
        Map<String, Object> diagnostics = imageLoader.getDiagnostics();
        assertNotNull(diagnostics);
        assertTrue((int) diagnostics.get("cachedImages") >= 2);
    }
    
    @Test
    @DisplayName("Lifecycle Management - Initialization order")
    void testLifecycleManagement() {
        BrobotConfiguration config = new BrobotConfiguration();
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        ImagePathManager pathManager = new ImagePathManager();
        SmartImageLoader imageLoader = new SmartImageLoader(pathManager, env);
        ConfigurationDiagnostics diagnostics = new ConfigurationDiagnostics(
            config, pathManager, imageLoader, env
        );
        
        BrobotLifecycleManager lifecycle = new BrobotLifecycleManager(
            config, env, pathManager, imageLoader, diagnostics
        );
        
        // Test initialization
        assertFalse(lifecycle.isRunning());
        
        lifecycle.start();
        assertTrue(lifecycle.isRunning());
        assertTrue(lifecycle.isFrameworkReady());
        
        // Get status
        var status = lifecycle.getStatus();
        assertNotNull(status);
        assertTrue(status.isRunning());
        
        // Test shutdown
        lifecycle.stop();
        assertFalse(lifecycle.isRunning());
    }
    
    @Test
    @DisplayName("Environment Profiles - Auto-switching")
    void testEnvironmentProfiles() {
        BrobotConfiguration config = new BrobotConfiguration();
        
        // Test profile switching
        config.applyProfile("testing");
        assertEquals("testing", config.getEnvironment().getProfile());
        assertTrue(config.getCore().isMockMode());
        assertEquals(0.5, config.getCore().getFindTimeout());
        
        config.applyProfile("production");
        assertEquals("production", config.getEnvironment().getProfile());
        assertFalse(config.getCore().isVerboseLogging());
        assertEquals(5, config.getPerformance().getMaxRetryAttempts());
        
        config.applyProfile("development");
        assertEquals("development", config.getEnvironment().getProfile());
        assertTrue(config.getCore().isVerboseLogging());
        assertTrue(config.getSikuli().isVisualDebugging());
    }
    
    // Helper methods
    
    private void createTestImage(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        Files.write(path, new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A // PNG header
        });
    }
    
    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }
    
    private boolean isRunningInCI() {
        return System.getenv("CI") != null ||
               System.getenv("CONTINUOUS_INTEGRATION") != null ||
               System.getenv("GITHUB_ACTIONS") != null;
    }
    
    private boolean isWSL() {
        return System.getenv("WSL_DISTRO_NAME") != null ||
               System.getenv("WSL_INTEROP") != null;
    }
}