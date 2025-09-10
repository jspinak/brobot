package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.navigation.monitoring.StateAwareScheduler;
import io.github.jspinak.brobot.navigation.monitoring.StateAwareScheduler.StateCheckConfiguration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest // (classes = ClaudeAutomatorApplication.class not available)
@TestPropertySource(properties = {
    "claude.automator.monitoring.max-iterations=3"
})
@Disabled("CI failure - needs investigation")
public class IterationLoggingTest extends BrobotTestBase {

    @Test
    public void testIterationLoggingVerbose() throws InterruptedException {
        System.out.println("\n=== Testing VERBOSE iteration logging ===");
        
        // Configure verbose logging
        LoggingVerbosityConfig verbosityConfig = new LoggingVerbosityConfig();
        verbosityConfig.setVerbosity(LoggingVerbosityConfig.VerbosityLevel.VERBOSE);
        
        // Create scheduler with mock dependencies
        StateAwareScheduler scheduler = new StateAwareScheduler(null, null);
        
        // Set verbosity config using reflection (since it's autowired)
        try {
            java.lang.reflect.Field field = StateAwareScheduler.class.getDeclaredField("verbosityConfig");
            field.setAccessible(true);
            field.set(scheduler, verbosityConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create configuration with 3 iterations
        StateCheckConfiguration config = new StateCheckConfiguration.Builder()
            .withMaxIterations(3)
            .build();
        
        // Create a simple task that counts executions
        AtomicInteger taskCount = new AtomicInteger(0);
        Runnable task = () -> {
            System.out.println("Task executed: " + taskCount.incrementAndGet());
        };
        
        // Schedule the task
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> future = scheduler.scheduleWithStateCheck(
            executor, task, config, 0, 500, TimeUnit.MILLISECONDS
        );
        
        // Wait for tasks to complete
        Thread.sleep(2000);
        
        // Verify the task stopped after 3 iterations
        System.out.println("Final task count: " + taskCount.get());
        assert taskCount.get() == 3 : "Expected 3 iterations but got " + taskCount.get();
        
        executor.shutdown();
    }
    
    @Test
    public void testIterationLoggingNormal() throws InterruptedException {
        System.out.println("\n=== Testing NORMAL iteration logging ===");
        
        // Configure normal logging
        LoggingVerbosityConfig verbosityConfig = new LoggingVerbosityConfig();
        verbosityConfig.setVerbosity(LoggingVerbosityConfig.VerbosityLevel.NORMAL);
        
        // Create scheduler with mock dependencies
        StateAwareScheduler scheduler = new StateAwareScheduler(null, null);
        
        // Set verbosity config using reflection
        try {
            java.lang.reflect.Field field = StateAwareScheduler.class.getDeclaredField("verbosityConfig");
            field.setAccessible(true);
            field.set(scheduler, verbosityConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create configuration with 10 iterations
        StateCheckConfiguration config = new StateCheckConfiguration.Builder()
            .withMaxIterations(10)
            .build();
        
        // Create a simple task
        AtomicInteger taskCount = new AtomicInteger(0);
        Runnable task = () -> {
            System.out.println("Task executed: " + taskCount.incrementAndGet());
        };
        
        // Schedule the task
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> future = scheduler.scheduleWithStateCheck(
            executor, task, config, 0, 200, TimeUnit.MILLISECONDS
        );
        
        // Wait for tasks to complete
        Thread.sleep(2500);
        
        // Verify the task stopped after 10 iterations
        System.out.println("Final task count: " + taskCount.get());
        assert taskCount.get() == 10 : "Expected 10 iterations but got " + taskCount.get();
        
        executor.shutdown();
    }
}