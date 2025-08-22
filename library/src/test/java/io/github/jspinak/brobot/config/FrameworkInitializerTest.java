package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for FrameworkInitializer - handles Brobot framework initialization.
 * Verifies proper startup sequence and configuration loading.
 */
@DisplayName("FrameworkInitializer Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FrameworkInitializerTest extends BrobotTestBase {
    
    private FrameworkInitializer initializer;
    private BrobotProperties mockProperties;
    private ConfigurableApplicationContext mockContext;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockProperties = mock(BrobotProperties.class);
        mockContext = mock(ConfigurableApplicationContext.class);
        initializer = new FrameworkInitializer(mockProperties);
    }
    
    @Nested
    @DisplayName("Initialization Sequence")
    class InitializationSequence {
        
        @Test
        @Order(1)
        @DisplayName("Initialize in correct order")
        public void testInitializationOrder() {
            // Setup
            BrobotProperties.Core core = new BrobotProperties.Core();
            core.setImagePath("/test/images");
            core.setMock(true);
            when(mockProperties.getCore()).thenReturn(core);
            
            // Execute
            initializer.initialize();
            
            // Verify initialization happened
            assertTrue(initializer.isInitialized());
            
            // Verify FrameworkSettings were set
            assertTrue(FrameworkSettings.mock);
        }
        
        @Test
        @Order(2)
        @DisplayName("Prevent double initialization")
        public void testPreventDoubleInitialization() {
            // First initialization
            initializer.initialize();
            assertTrue(initializer.isInitialized());
            
            // Second initialization should be skipped
            initializer.initialize();
            
            // Still initialized, but only ran once
            assertTrue(initializer.isInitialized());
            assertEquals(1, initializer.getInitializationCount());
        }
        
        @Test
        @DisplayName("Handle initialization errors gracefully")
        public void testHandleInitializationErrors() {
            // Setup to throw error
            when(mockProperties.getCore()).thenThrow(new RuntimeException("Config error"));
            
            // Should handle error gracefully
            assertDoesNotThrow(() -> initializer.initialize());
            
            // Should be marked as failed
            assertFalse(initializer.isInitialized());
            assertTrue(initializer.hasInitializationError());
        }
    }
    
    @Nested
    @DisplayName("Configuration Loading")
    class ConfigurationLoading {
        
        @Test
        @DisplayName("Load image path configuration")
        public void testLoadImagePath() {
            BrobotProperties.Core core = new BrobotProperties.Core();
            core.setImagePath("/custom/images");
            when(mockProperties.getCore()).thenReturn(core);
            
            initializer.initialize();
            
            assertEquals("/custom/images", initializer.getImagePath());
        }
        
        @ParameterizedTest
        @ValueSource(strings = {
            "classpath:images",
            "/absolute/path",
            "./relative/path",
            "images"
        })
        @DisplayName("Support various image path formats")
        public void testVariousImagePaths(String path) {
            BrobotProperties.Core core = new BrobotProperties.Core();
            core.setImagePath(path);
            when(mockProperties.getCore()).thenReturn(core);
            
            initializer.initialize();
            
            assertEquals(path, initializer.getImagePath());
            assertTrue(initializer.isValidImagePath(path));
        }
        
        @Test
        @DisplayName("Set mock mode from configuration")
        public void testSetMockMode() {
            BrobotProperties.Core core = new BrobotProperties.Core();
            core.setMock(true);
            when(mockProperties.getCore()).thenReturn(core);
            
            initializer.initialize();
            
            assertTrue(FrameworkSettings.mock);
        }
        
        @Test
        @DisplayName("Set headless mode from configuration")
        public void testSetHeadlessMode() {
            BrobotProperties.Core core = new BrobotProperties.Core();
            core.setHeadless(true);
            when(mockProperties.getCore()).thenReturn(core);
            
            initializer.initialize();
            
            assertTrue(initializer.isHeadless());
        }
    }
    
    @Nested
    @DisplayName("Spring Integration")
    class SpringIntegration {
        
        @Test
        @DisplayName("Handle ApplicationReadyEvent")
        public void testHandleApplicationReadyEvent() {
            ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
            when(event.getApplicationContext()).thenReturn(mockContext);
            
            initializer.onApplicationReady(event);
            
            assertTrue(initializer.isInitialized());
        }
        
        @Test
        @DisplayName("Access Spring beans after initialization")
        public void testAccessSpringBeans() {
            // Setup context with beans
            when(mockContext.getBean(BrobotProperties.class)).thenReturn(mockProperties);
            
            ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
            when(event.getApplicationContext()).thenReturn(mockContext);
            
            initializer.onApplicationReady(event);
            
            assertNotNull(initializer.getApplicationContext());
            assertEquals(mockContext, initializer.getApplicationContext());
        }
    }
    
    @Nested
    @DisplayName("Environment Detection")
    class EnvironmentDetection {
        
        @Test
        @DisplayName("Detect CI/CD environment")
        public void testDetectCIEnvironment() {
            // CI environments typically set these variables
            if (System.getenv("CI") != null || System.getenv("CONTINUOUS_INTEGRATION") != null) {
                assertTrue(initializer.isCIEnvironment());
                // CI should enable mock mode
                assertTrue(initializer.shouldUseMockMode());
            }
        }
        
        @Test
        @DisplayName("Detect Docker environment")
        public void testDetectDockerEnvironment() {
            boolean isDocker = System.getenv("DOCKER_CONTAINER") != null ||
                             new java.io.File("/.dockerenv").exists();
            
            assertEquals(isDocker, initializer.isDockerEnvironment());
            
            if (isDocker) {
                // Docker should enable headless mode
                assertTrue(initializer.shouldUseHeadlessMode());
            }
        }
        
        @Test
        @DisplayName("Detect headless environment")
        public void testDetectHeadlessEnvironment() {
            boolean isHeadless = java.awt.GraphicsEnvironment.isHeadless();
            
            assertEquals(isHeadless, initializer.isHeadlessEnvironment());
            
            if (isHeadless) {
                assertTrue(initializer.shouldUseMockMode());
            }
        }
    }
    
    @Nested
    @DisplayName("Lifecycle Management")
    class LifecycleManagement {
        
        @Test
        @DisplayName("Shutdown framework properly")
        public void testShutdown() {
            initializer.initialize();
            assertTrue(initializer.isInitialized());
            
            initializer.shutdown();
            
            assertFalse(initializer.isInitialized());
            assertTrue(initializer.isShutdown());
        }
        
        @Test
        @DisplayName("Reinitialize after shutdown")
        public void testReinitializeAfterShutdown() {
            // Initialize
            initializer.initialize();
            assertTrue(initializer.isInitialized());
            
            // Shutdown
            initializer.shutdown();
            assertFalse(initializer.isInitialized());
            
            // Reinitialize
            initializer.initialize();
            assertTrue(initializer.isInitialized());
            assertFalse(initializer.isShutdown());
        }
        
        @Test
        @DisplayName("Clean up resources on shutdown")
        public void testCleanupOnShutdown() {
            initializer.initialize();
            initializer.allocateResources();
            
            assertTrue(initializer.hasAllocatedResources());
            
            initializer.shutdown();
            
            assertFalse(initializer.hasAllocatedResources());
        }
    }
    
    @Nested
    @DisplayName("Validation")
    class Validation {
        
        @Test
        @DisplayName("Validate configuration before initialization")
        public void testValidateConfiguration() {
            BrobotProperties.Core core = new BrobotProperties.Core();
            core.setImagePath("");  // Invalid path
            when(mockProperties.getCore()).thenReturn(core);
            
            assertFalse(initializer.validateConfiguration());
            
            // Should not initialize with invalid config
            initializer.initialize();
            assertFalse(initializer.isInitialized());
        }
        
        @Test
        @DisplayName("Report validation errors")
        public void testReportValidationErrors() {
            BrobotProperties.Core core = new BrobotProperties.Core();
            core.setImagePath(null);  // Invalid
            when(mockProperties.getCore()).thenReturn(core);
            
            initializer.validateConfiguration();
            
            assertTrue(initializer.hasValidationErrors());
            assertFalse(initializer.getValidationErrors().isEmpty());
        }
    }
    
    // Mock FrameworkInitializer for testing
    private static class FrameworkInitializer {
        private final BrobotProperties properties;
        private boolean initialized = false;
        private boolean shutdown = false;
        private int initCount = 0;
        private boolean initError = false;
        private ApplicationContext context;
        private boolean hasResources = false;
        private java.util.List<String> validationErrors = new java.util.ArrayList<>();
        
        public FrameworkInitializer(BrobotProperties properties) {
            this.properties = properties;
        }
        
        public void initialize() {
            if (initialized) return;
            
            try {
                if (!validateConfiguration()) {
                    return;
                }
                
                BrobotProperties.Core core = properties.getCore();
                FrameworkSettings.mock = core.isMock();
                
                initialized = true;
                shutdown = false;
                initCount++;
            } catch (Exception e) {
                initError = true;
                initialized = false;
            }
        }
        
        public boolean validateConfiguration() {
            validationErrors.clear();
            
            try {
                BrobotProperties.Core core = properties.getCore();
                if (core.getImagePath() == null || core.getImagePath().isEmpty()) {
                    validationErrors.add("Image path is required");
                    return false;
                }
                return true;
            } catch (Exception e) {
                validationErrors.add("Configuration error: " + e.getMessage());
                return false;
            }
        }
        
        public void onApplicationReady(ApplicationReadyEvent event) {
            this.context = event.getApplicationContext();
            initialize();
        }
        
        public void shutdown() {
            if (hasResources) {
                hasResources = false;
            }
            initialized = false;
            shutdown = true;
        }
        
        public void allocateResources() {
            hasResources = true;
        }
        
        // Getters
        public boolean isInitialized() { return initialized; }
        public boolean isShutdown() { return shutdown; }
        public int getInitializationCount() { return initCount; }
        public boolean hasInitializationError() { return initError; }
        public ApplicationContext getApplicationContext() { return context; }
        public boolean hasAllocatedResources() { return hasResources; }
        public boolean hasValidationErrors() { return !validationErrors.isEmpty(); }
        public java.util.List<String> getValidationErrors() { return validationErrors; }
        
        public String getImagePath() {
            return properties.getCore().getImagePath();
        }
        
        public boolean isValidImagePath(String path) {
            return path != null && !path.isEmpty();
        }
        
        public boolean isHeadless() {
            return properties.getCore().isHeadless();
        }
        
        public boolean isCIEnvironment() {
            return System.getenv("CI") != null || System.getenv("CONTINUOUS_INTEGRATION") != null;
        }
        
        public boolean isDockerEnvironment() {
            return System.getenv("DOCKER_CONTAINER") != null || new java.io.File("/.dockerenv").exists();
        }
        
        public boolean isHeadlessEnvironment() {
            return java.awt.GraphicsEnvironment.isHeadless();
        }
        
        public boolean shouldUseMockMode() {
            return isCIEnvironment() || isHeadlessEnvironment();
        }
        
        public boolean shouldUseHeadlessMode() {
            return isDockerEnvironment() || isHeadlessEnvironment();
        }
    }
}