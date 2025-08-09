package io.github.jspinak.brobot.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that display checks are cached and not performed repeatedly.
 */
public class ExecutionEnvironmentCacheTest {
    
    private ExecutionEnvironment env;
    
    @BeforeEach
    public void setUp() {
        // Create a fresh environment for each test
        env = ExecutionEnvironment.builder().build();
        ExecutionEnvironment.setInstance(env);
    }
    
    @Test
    public void testDisplayCheckIsCached() {
        // First call - should perform actual check
        long startTime = System.currentTimeMillis();
        boolean firstResult = env.hasDisplay();
        long firstCallTime = System.currentTimeMillis() - startTime;
        
        // Immediate second call - should use cache
        startTime = System.currentTimeMillis();
        boolean secondResult = env.hasDisplay();
        long secondCallTime = System.currentTimeMillis() - startTime;
        
        // Results should be the same
        assertEquals(firstResult, secondResult, "Display check results should be consistent");
        
        // Second call should be much faster (cached)
        // We can't assert exact timing, but cached calls should typically be < 1ms
        System.out.println("First call time: " + firstCallTime + "ms");
        System.out.println("Second call time: " + secondCallTime + "ms");
        
        // Multiple calls should all return the same cached result
        for (int i = 0; i < 100; i++) {
            assertEquals(firstResult, env.hasDisplay(), 
                "All subsequent calls should return the same cached result");
        }
    }
    
    @Test
    public void testCacheRefresh() {
        // Get initial result
        boolean initialResult = env.hasDisplay();
        
        // Force cache refresh
        env.refreshDisplayCheck();
        
        // Next call should perform a fresh check (result should still be the same though)
        boolean refreshedResult = env.hasDisplay();
        
        // Results should still be consistent (environment hasn't changed)
        assertEquals(initialResult, refreshedResult, 
            "Display availability shouldn't change, only cache is refreshed");
    }
    
    @Test
    public void testMultipleCallsPerformance() {
        // Warm up - first call does actual check
        env.hasDisplay();
        
        // Measure time for 1000 calls
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            env.hasDisplay();
        }
        long totalTime = System.currentTimeMillis() - startTime;
        
        // With caching, 1000 calls should be very fast (< 100ms total)
        System.out.println("Time for 1000 cached calls: " + totalTime + "ms");
        assertTrue(totalTime < 100, 
            "1000 cached display checks should complete in less than 100ms, but took " + totalTime + "ms");
    }
}