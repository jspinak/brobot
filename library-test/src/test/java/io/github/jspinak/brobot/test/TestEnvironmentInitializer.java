package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.config.FrameworkSettings;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Paths;

/**
 * Initializes the test environment before Spring context loads.
 * This ensures BrobotEnvironment is configured before any beans are created.
 */
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
public class TestEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        // Get test type from system property or environment
        String testType = System.getProperty("brobot.test.type", 
                         environment.getProperty("brobot.test.type", "unit"));
        
        // Configure paths for test resources
        String projectRoot = Paths.get("").toAbsolutePath().toString();
        String testResourcePath = projectRoot + "/src/test/resources/";
        
        // Set paths in BrobotSettings
        FrameworkSettings.screenshotPath = "screenshots/";
        
        // Configure BrobotEnvironment based on test type
        ExecutionEnvironment env;
        
        if ("integration".equals(testType)) {
            System.out.println("Initializing INTEGRATION test environment");
            
            env = ExecutionEnvironment.builder()
                .mockMode(false)  // Use real files
                .forceHeadless(true)  // Force headless for CI/WSL
                .allowScreenCapture(false)
                .verboseLogging(true)
                .build();
            
            FrameworkSettings.mock = false;
            
            // Set ImagePath bundle path if not in full mock mode
            if (!env.shouldSkipSikuliX()) {
                try {
                    org.sikuli.script.ImagePath.setBundlePath("images");
                } catch (Exception e) {
                    System.err.println("Could not set ImagePath: " + e.getMessage());
                }
            }
            
        } else {
            System.out.println("Initializing UNIT test environment");
            
            env = ExecutionEnvironment.builder()
                .mockMode(true)  // Use mock data
                .forceHeadless(true)
                .allowScreenCapture(false)
                .build();
            
            FrameworkSettings.mock = true;
        }
        
        ExecutionEnvironment.setInstance(env);
        
        // Prevent SikuliX initialization
        System.setProperty("sikuli.Debug", "0");
        System.setProperty("sikuli.AutoDetectFromIDE", "false");
        System.setProperty("java.awt.headless", "true");
        
        System.out.println("Test environment initialized: " + env.getEnvironmentInfo());
        System.out.println("Working directory: " + projectRoot);
        System.out.println("FrameworkSettings.mock: " + FrameworkSettings.mock);
    }
}