import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("Testing newly created test classes...\n");
        
        // List of test classes to run
        Class<?>[] testClasses = {
            io.github.jspinak.brobot.action.basic.mouse.MouseDownTest.class,
            io.github.jspinak.brobot.action.basic.mouse.MouseUpTest.class,
            io.github.jspinak.brobot.action.basic.type.KeyDownTest.class,
            io.github.jspinak.brobot.action.basic.type.KeyUpTest.class
        };
        
        for (Class<?> testClass : testClasses) {
            System.out.println("Running: " + testClass.getSimpleName());
            System.out.println("-".repeat(50));
            
            try {
                LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(selectClass(testClass))
                    .build();
                
                Launcher launcher = LauncherFactory.create();
                SummaryGeneratingListener listener = new SummaryGeneratingListener();
                
                launcher.registerTestExecutionListeners(listener);
                launcher.execute(request);
                
                TestExecutionSummary summary = listener.getSummary();
                System.out.println("Tests run: " + summary.getTestsStartedCount());
                System.out.println("Tests passed: " + summary.getTestsSucceededCount());
                System.out.println("Tests failed: " + summary.getTestsFailedCount());
                
                if (summary.getTestsFailedCount() > 0) {
                    System.out.println("\nFailures:");
                    summary.getFailures().forEach(failure -> {
                        System.out.println("  - " + failure.getTestIdentifier().getDisplayName());
                        System.out.println("    " + failure.getException().getMessage());
                    });
                }
                
            } catch (Exception e) {
                System.out.println("Error running test: " + e.getMessage());
            }
            
            System.out.println();
        }
    }
}