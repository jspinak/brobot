import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class RunPatternFindOptionsTest {
    public static void main(String[] args) throws Exception {
        // Create a test discovery request
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass("io.github.jspinak.brobot.action.basic.find.PatternFindOptionsTest"))
            .build();

        // Create test execution listeners
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        
        // Create and execute the launcher
        try (LauncherSession session = LauncherFactory.openSession()) {
            Launcher launcher = session.getLauncher();
            launcher.registerTestExecutionListeners(summaryListener);
            launcher.execute(request);
        }

        // Print test results
        TestExecutionSummary summary = summaryListener.getSummary();
        PrintWriter writer = new PrintWriter(System.out);
        
        summary.printTo(writer);
        writer.flush();
        
        // Exit with appropriate code
        System.exit(summary.getTotalFailureCount() > 0 ? 1 : 0);
    }
}