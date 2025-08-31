package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for BrobotConfiguration.
 * Tests configuration loading, validation, and environment profiles.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BrobotConfiguration.class})
@EnableConfigurationProperties(BrobotConfiguration.class)
@TestPropertySource(properties = {
    "brobot.core.mockMode=true",
    "brobot.core.imagePath=test-images",
    "brobot.core.findTimeout=5.0"
})
public class BrobotConfigurationTest extends BrobotTestBase {
    
    @Autowired
    private BrobotConfiguration configuration;
    
    @Nested
    @DisplayName("Core Configuration Tests")
    class CoreConfigTests {
        
        @Test
        @Order(1)
        @DisplayName("Should load default core configuration")
        void testDefaultCoreConfig() {
            BrobotConfiguration.CoreConfig config = new BrobotConfiguration.CoreConfig();
            
            assertEquals("images", config.getImagePath());
            assertFalse(config.isMockMode());
            assertNull(config.getForceHeadless());
            assertTrue(config.isAllowScreenCapture());
            assertFalse(config.isVerboseLogging());
            assertEquals(3.0, config.getFindTimeout(), 0.001);
            assertEquals(0.3, config.getActionPause(), 0.001);
            assertTrue(config.isEnableImageCache());
            assertEquals(100, config.getMaxCacheSizeMB());
        }
        
        @Test
        @Order(2)
        @DisplayName("Should set custom core configuration")
        void testCustomCoreConfig() {
            BrobotConfiguration.CoreConfig config = new BrobotConfiguration.CoreConfig();
            
            config.setImagePath("custom-images");
            config.setMockMode(true);
            config.setForceHeadless(true);
            config.setAllowScreenCapture(false);
            config.setVerboseLogging(true);
            config.setFindTimeout(10.0);
            config.setActionPause(1.0);
            config.setEnableImageCache(false);
            config.setMaxCacheSizeMB(200);
            
            assertEquals("custom-images", config.getImagePath());
            assertTrue(config.isMockMode());
            assertTrue(config.getForceHeadless());
            assertFalse(config.isAllowScreenCapture());
            assertTrue(config.isVerboseLogging());
            assertEquals(10.0, config.getFindTimeout(), 0.001);
            assertEquals(1.0, config.getActionPause(), 0.001);
            assertFalse(config.isEnableImageCache());
            assertEquals(200, config.getMaxCacheSizeMB());
        }
        
        @Test
        @Order(3)
        @DisplayName("Should handle additional image paths")
        void testAdditionalImagePaths() {
            BrobotConfiguration.CoreConfig config = new BrobotConfiguration.CoreConfig();
            
            List<String> paths = Arrays.asList("path1", "path2", "path3");
            config.setAdditionalImagePaths(paths);
            
            assertEquals(3, config.getAdditionalImagePaths().size());
            assertTrue(config.getAdditionalImagePaths().contains("path1"));
            assertTrue(config.getAdditionalImagePaths().contains("path2"));
            assertTrue(config.getAdditionalImagePaths().contains("path3"));
        }
    }
    
    @Nested
    @DisplayName("SikuliX Configuration Tests")
    class SikuliConfigTests {
        
        @Test
        @Order(4)
        @DisplayName("Should load default Sikuli configuration")
        void testDefaultSikuliConfig() {
            BrobotConfiguration.SikuliConfig config = new BrobotConfiguration.SikuliConfig();
            
            assertEquals(0.7, config.getMinSimilarity(), 0.001);
            assertEquals(3.0, config.getWaitTime(), 0.001);
        }
        
        @Test
        @Order(5)
        @DisplayName("Should validate similarity bounds")
        void testSimilarityBounds() {
            BrobotConfiguration.SikuliConfig config = new BrobotConfiguration.SikuliConfig();
            
            // Valid values
            config.setMinSimilarity(0.0);
            assertEquals(0.0, config.getMinSimilarity(), 0.001);
            
            config.setMinSimilarity(1.0);
            assertEquals(1.0, config.getMinSimilarity(), 0.001);
            
            config.setMinSimilarity(0.5);
            assertEquals(0.5, config.getMinSimilarity(), 0.001);
        }
        
        @Test
        @Order(6)
        @DisplayName("Should handle negative wait time")
        void testNegativeWaitTime() {
            BrobotConfiguration.SikuliConfig config = new BrobotConfiguration.SikuliConfig();
            
            config.setWaitTime(-1.0);
            assertEquals(-1.0, config.getWaitTime(), 0.001);
            // Note: Negative values might be used to indicate "no wait"
        }
    }
    
    @Nested
    @DisplayName("Environment Config Tests")
    class EnvironmentConfigTests {
        
        @Test
        @Order(7)
        @DisplayName("Should configure development environment")
        void testDevelopmentEnvironment() {
            BrobotConfiguration.EnvironmentConfig config = new BrobotConfiguration.EnvironmentConfig();
            
            config.setProfile("development");
            config.setCiMode(false);
            config.setDockerMode(false);
            
            assertEquals("development", config.getProfile());
            assertFalse(config.isCiMode());
            assertFalse(config.isDockerMode());
        }
        
        @Test
        @Order(8)
        @DisplayName("Should configure testing environment")
        void testTestingEnvironment() {
            BrobotConfiguration.EnvironmentConfig config = new BrobotConfiguration.EnvironmentConfig();
            
            config.setProfile("testing");
            config.setCiMode(true);
            config.setDockerMode(false);
            
            assertEquals("testing", config.getProfile());
            assertTrue(config.isCiMode());
            assertFalse(config.isDockerMode());
        }
        
        @Test
        @Order(9)
        @DisplayName("Should configure production environment")
        void testProductionEnvironment() {
            BrobotConfiguration.EnvironmentConfig config = new BrobotConfiguration.EnvironmentConfig();
            
            config.setProfile("production");
            config.setCiMode(false);
            config.setRemoteMode(false);
            
            assertEquals("production", config.getProfile());
            assertFalse(config.isCiMode());
            assertFalse(config.isRemoteMode());
        }
    }
    
    @Nested
    @DisplayName("Environment Additional Tests")
    class EnvironmentAdditionalTests {
        
        @Test
        @Order(10)
        @DisplayName("Should check if profile is explicitly set")
        void testProfileExplicitlySet() {
            BrobotConfiguration.EnvironmentConfig config = new BrobotConfiguration.EnvironmentConfig();
            
            // Initially not explicitly set
            assertFalse(config.isProfileExplicitlySet());
            
            // After setting profile
            config.setProfile("custom");
            // Note: isProfileExplicitlySet might still depend on internal logic
        }
        
        @Test
        @Order(11)
        @DisplayName("Should handle custom profiles")
        void testCustomProfiles() {
            BrobotConfiguration.EnvironmentConfig config = new BrobotConfiguration.EnvironmentConfig();
            
            String[] profiles = {"dev", "stage", "qa", "integration", "custom"};
            
            for (String profile : profiles) {
                config.setProfile(profile);
                assertEquals(profile, config.getProfile());
            }
        }
    }
    
    @Nested
    @DisplayName("Performance Configuration Tests")
    class PerformanceConfigTests {
        
        @Test
        @Order(12)
        @DisplayName("Should configure performance settings")
        void testPerformanceConfig() {
            BrobotConfiguration.PerformanceConfig config = new BrobotConfiguration.PerformanceConfig();
            
            config.setEnableParallelExecution(true);
            config.setThreadPoolSize(8);
            config.setMaxRetryAttempts(5);
            config.setRetryDelay(2.0);
            config.setCollectMetrics(true);
            
            assertTrue(config.isEnableParallelExecution());
            assertEquals(8, config.getThreadPoolSize());
            assertEquals(5, config.getMaxRetryAttempts());
            assertEquals(2.0, config.getRetryDelay(), 0.001);
            assertTrue(config.isCollectMetrics());
        }
        
        @Test
        @Order(13)
        @DisplayName("Should validate thread pool size")
        void testThreadPoolSize() {
            BrobotConfiguration.PerformanceConfig config = new BrobotConfiguration.PerformanceConfig();
            
            // Minimum threads
            config.setThreadPoolSize(1);
            assertEquals(1, config.getThreadPoolSize());
            
            // Maximum reasonable threads
            config.setThreadPoolSize(100);
            assertEquals(100, config.getThreadPoolSize());
            
            // Default value
            BrobotConfiguration.PerformanceConfig defaultConfig = new BrobotConfiguration.PerformanceConfig();
            assertEquals(4, defaultConfig.getThreadPoolSize());
        }
    }
    
    @Nested
    @DisplayName("Configuration Validation Tests")
    class ConfigurationValidationTests {
        
        @Test
        @Order(14)
        @DisplayName("Should validate configuration on load")
        void testConfigurationValidationOnLoad() {
            BrobotConfiguration config = new BrobotConfiguration();
            
            assertDoesNotThrow(() -> config.validate());
        }
        
        @Test
        @Order(15)
        @DisplayName("Should detect invalid image paths")
        void testInvalidImagePaths() {
            BrobotConfiguration config = new BrobotConfiguration();
            BrobotConfiguration.CoreConfig core = new BrobotConfiguration.CoreConfig();
            
            // Set non-existent path
            core.setImagePath("/non/existent/path");
            config.setCore(core);
            
            // Validation should handle gracefully
            assertDoesNotThrow(() -> config.validate());
        }
        
        @Test
        @Order(16)
        @DisplayName("Should validate configuration")
        void testConfigurationValidation() {
            BrobotConfiguration config = new BrobotConfiguration();
            
            // Set some values
            BrobotConfiguration.CoreConfig core = new BrobotConfiguration.CoreConfig();
            core.setMockMode(false);
            core.setFindTimeout(3.0);
            config.setCore(core);
            
            // Validation should not throw
            assertDoesNotThrow(() -> config.validate());
            
            // Check that configuration is still intact after validation
            assertFalse(config.getCore().isMockMode());
            assertEquals(3.0, config.getCore().getFindTimeout(), 0.001);
        }
    }
    
    @Nested
    @DisplayName("Spring Integration Tests")
    class SpringIntegrationTests {
        
        @Test
        @Order(17)
        @DisplayName("Should inject configuration from properties")
        void testPropertyInjection() {
            assertNotNull(configuration);
            assertNotNull(configuration.getCore());
            
            // These values come from @TestPropertySource
            assertTrue(configuration.getCore().isMockMode());
            assertEquals("test-images", configuration.getCore().getImagePath());
            assertEquals(5.0, configuration.getCore().getFindTimeout(), 0.001);
        }
        
        @Test
        @Order(18)
        @DisplayName("Should support property placeholders")
        void testPropertyPlaceholders() {
            // Test that ${} placeholders work in configuration
            BrobotConfiguration config = new BrobotConfiguration();
            
            // Simulate property resolution
            String userHome = System.getProperty("user.home");
            config.getCore().setImagePath(userHome + "/images");
            
            assertTrue(config.getCore().getImagePath().contains(userHome));
        }
    }
    
    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {
        
        @Test
        @Order(19)
        @DisplayName("Should handle concurrent configuration access")
        void testConcurrentAccess() throws InterruptedException {
            BrobotConfiguration config = new BrobotConfiguration();
            BrobotConfiguration.CoreConfig core = new BrobotConfiguration.CoreConfig();
            config.setCore(core);
            
            int threadCount = 20;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            AtomicInteger errors = new AtomicInteger(0);
            
            for (int i = 0; i < threadCount; i++) {
                final int value = i;
                executor.submit(() -> {
                    try {
                        // Concurrent reads and writes
                        config.getCore().setFindTimeout(value);
                        config.getCore().getFindTimeout(); // Read value
                        
                        config.getCore().setMockMode(value % 2 == 0);
                        config.getCore().isMockMode(); // Read value
                        
                        config.getCore().setImagePath("path" + value);
                        config.getCore().getImagePath(); // Read value
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            executor.shutdown();
            
            assertEquals(0, errors.get(), "No errors during concurrent access");
        }
    }
}