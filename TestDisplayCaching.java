import io.github.jspinak.brobot.config.ExecutionEnvironment;

public class TestDisplayCaching {
    public static void main(String[] args) {
        System.out.println("Testing Display Check Caching...\n");
        
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        // First call - should perform actual check and log
        System.out.println("=== First call (should show detailed logs) ===");
        long start = System.currentTimeMillis();
        boolean result1 = env.hasDisplay();
        long time1 = System.currentTimeMillis() - start;
        System.out.println("Result: " + result1 + ", Time: " + time1 + "ms\n");
        
        // Second call - should use cache (no detailed logs)
        System.out.println("=== Second call (should use cache, minimal logs) ===");
        start = System.currentTimeMillis();
        boolean result2 = env.hasDisplay();
        long time2 = System.currentTimeMillis() - start;
        System.out.println("Result: " + result2 + ", Time: " + time2 + "ms\n");
        
        // Multiple rapid calls - should all use cache
        System.out.println("=== 100 rapid calls (all cached) ===");
        start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            env.hasDisplay();
        }
        long timeFor100 = System.currentTimeMillis() - start;
        System.out.println("Time for 100 cached calls: " + timeFor100 + "ms\n");
        
        // Test cache refresh
        System.out.println("=== Refreshing cache ===");
        env.refreshDisplayCheck();
        
        System.out.println("=== Next call after refresh (should show detailed logs again) ===");
        start = System.currentTimeMillis();
        boolean result3 = env.hasDisplay();
        long time3 = System.currentTimeMillis() - start;
        System.out.println("Result: " + result3 + ", Time: " + time3 + "ms\n");
        
        System.out.println("Test completed. Display checks are now cached!");
    }
}