import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Standalone test runner for Instance 5 tests.
 * This allows running the new tests without compiling all other tests.
 */
public class StandaloneTestRunner {
    
    public static void main(String[] args) {
        System.out.println("=== Instance 5 Test Runner ===");
        System.out.println("Running tests for analysis and logging packages...\n");
        
        // List of test classes to run
        String[] testClasses = {
            "io.github.jspinak.brobot.analysis.color.ColorAnalysisTest",
            "io.github.jspinak.brobot.analysis.color.ColorClassifierTest",
            "io.github.jspinak.brobot.analysis.histogram.HistogramComparatorTest",
            "io.github.jspinak.brobot.logging.modular.ActionLoggingServiceTest",
            "io.github.jspinak.brobot.logging.unified.BrobotLoggerTest",
            "io.github.jspinak.brobot.analysis.motion.MotionDetectorTest",
            "io.github.jspinak.brobot.analysis.motion.DynamicPixelFinderTest",
            "io.github.jspinak.brobot.analysis.scene.SceneCombinationGeneratorTest"
        };
        
        int totalTests = 0;
        int successfulTests = 0;
        int failedTests = 0;
        int skippedTests = 0;
        
        for (String className : testClasses) {
            System.out.println("\nRunning: " + className);
            System.out.println("-".repeat(50));
            
            try {
                Class<?> testClass = Class.forName(className);
                
                LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(selectClass(testClass))
                    .build();
                
                Launcher launcher = LauncherFactory.create();
                SummaryGeneratingListener listener = new SummaryGeneratingListener();
                
                launcher.registerTestExecutionListeners(listener);
                launcher.execute(request);
                
                TestExecutionSummary summary = listener.getSummary();
                
                System.out.println("Tests run: " + summary.getTestsFoundCount());
                System.out.println("Successful: " + summary.getTestsSucceededCount());
                System.out.println("Failed: " + summary.getTestsFailedCount());
                System.out.println("Skipped: " + summary.getTestsSkippedCount());
                
                totalTests += summary.getTestsFoundCount();
                successfulTests += summary.getTestsSucceededCount();
                failedTests += summary.getTestsFailedCount();
                skippedTests += summary.getTestsSkippedCount();
                
                // Print failures if any
                if (!summary.getFailures().isEmpty()) {
                    System.out.println("\nFailures:");
                    summary.getFailures().forEach(failure -> {
                        System.out.println("  - " + failure.getTestIdentifier().getDisplayName());
                        System.out.println("    " + failure.getException().getMessage());
                    });
                }
                
            } catch (ClassNotFoundException e) {
                System.out.println("ERROR: Test class not found - " + className);
                System.out.println("       This may be due to compilation issues.");
            } catch (Exception e) {
                System.out.println("ERROR: Failed to run tests - " + e.getMessage());
            }
        }
        
        // Print summary
        System.out.println("\n" + "=".repeat(60));
        System.out.println("OVERALL SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("Total test classes: " + testClasses.length);
        System.out.println("Total tests found: " + totalTests);
        System.out.println("Successful: " + successfulTests);
        System.out.println("Failed: " + failedTests);
        System.out.println("Skipped: " + skippedTests);
        
        if (totalTests == 0) {
            System.out.println("\n⚠️  No tests were executed. This is likely due to compilation issues.");
            System.out.println("   The test files exist but cannot be compiled due to dependencies.");
        } else if (failedTests == 0) {
            System.out.println("\n✅ All tests passed!");
        } else {
            System.out.println("\n❌ Some tests failed. See details above.");
        }
    }
}