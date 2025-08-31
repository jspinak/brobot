package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.test.config.OptimizedTestConfig;
import io.github.jspinak.brobot.test.config.TestActionConfig;
import io.github.jspinak.brobot.test.config.TestConfigurationManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

/**
 * Optimized base class for Brobot integration tests.
 * 
 * Key optimizations:
 * - Shared Spring context between tests (TestInstance.Lifecycle.PER_CLASS)
 * - Optimized mock settings for faster execution
 * - Reduced initialization overhead
 * - Configurable timeouts per test
 */
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class)
@ContextConfiguration(initializers = TestConfigurationManager.class)
@Import({TestActionConfig.class, OptimizedTestConfig.class})
@TestPropertySource(
    locations = "classpath:application-test.properties",
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.main.lazy-initialization=true",
        "logging.level.root=WARN",
        "logging.level.io.github.jspinak.brobot=INFO"
    }
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({SpringExtension.class, OptimizedTestConfig.class})
@Timeout(value = 5, unit = TimeUnit.MINUTES)  // Default 5-minute timeout per test
public abstract class OptimizedIntegrationTestBase {
    
    private static boolean globalSetupComplete = false;
    private static final Object SETUP_LOCK = new Object();
    
    @BeforeAll
    protected void globalSetup() {
        synchronized (SETUP_LOCK) {
            if (!globalSetupComplete) {
                // One-time global setup
                optimizeFrameworkSettings();
                globalSetupComplete = true;
            }
        }
    }
    
    @BeforeEach
    protected void testSetup() {
        // Minimal per-test setup
        ensureMockMode();
    }
    
    private void optimizeFrameworkSettings() {
        // Enable mock mode for all tests
        FrameworkSettings.mock = true;
        
        // Optimize timing for tests
        FrameworkSettings.mockTimeFindFirst = 0.005;
        FrameworkSettings.mockTimeFindAll = 0.01;
        FrameworkSettings.mockTimeClick = 0.005;
        FrameworkSettings.mockTimeMove = 0.005;
        FrameworkSettings.mockTimeDrag = 0.01;
        FrameworkSettings.mockTimeFindHistogram = 0.01;
        FrameworkSettings.mockTimeFindColor = 0.01;
        FrameworkSettings.mockTimeClassify = 0.015;
        
        // Set screenshot paths for tests
        FrameworkSettings.screenshotPath = "library-test/screenshots/";
    }
    
    private void ensureMockMode() {
        // Ensure mock mode is always enabled for integration tests
        if (!FrameworkSettings.mock) {
            FrameworkSettings.mock = true;
        }
    }
    
    /**
     * Check if test environment is headless.
     */
    protected boolean isHeadlessEnvironment() {
        return java.awt.GraphicsEnvironment.isHeadless() || 
               System.getenv("BROBOT_FORCE_HEADLESS") != null;
    }
    
    /**
     * Skip test if running in CI/CD environment.
     */
    protected boolean isCI() {
        return System.getenv("CI") != null || 
               System.getenv("GITHUB_ACTIONS") != null ||
               System.getenv("JENKINS_HOME") != null;
    }
    
    /**
     * Get timeout multiplier for slower environments.
     */
    protected double getTimeoutMultiplier() {
        if (isCI()) return 2.0;
        if (isHeadlessEnvironment()) return 1.5;
        return 1.0;
    }
}