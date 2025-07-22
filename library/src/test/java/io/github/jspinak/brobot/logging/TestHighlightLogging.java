package io.github.jspinak.brobot.logging;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@TestPropertySource(properties = {
    "brobot.highlight.enabled=true",
    "brobot.highlight.auto-highlight-search-regions=true",
    "brobot.logging.verbosity=NORMAL"
})
public class TestHighlightLogging {
    
    @Autowired
    private HighlightManager highlightManager;
    
    @Autowired
    private VisualFeedbackConfig visualFeedbackConfig;
    
    @Autowired
    private BrobotLogger brobotLogger;
    
    @Test
    public void testHighlightLoggingWithContext() {
        System.out.println("\n=== TEST HIGHLIGHT LOGGING WITH CONTEXT ===");
        
        // Enable highlighting
        visualFeedbackConfig.setEnabled(true);
        visualFeedbackConfig.setAutoHighlightSearchRegions(true);
        
        // Create test regions with context
        List<HighlightManager.RegionWithContext> regionsWithContext = Arrays.asList(
            new HighlightManager.RegionWithContext(
                new Region(100, 100, 200, 150),
                "WorkingState",
                "ClaudeIcon"
            ),
            new HighlightManager.RegionWithContext(
                new Region(300, 200, 150, 100),
                "PromptState",
                "ClaudePrompt"
            ),
            new HighlightManager.RegionWithContext(
                new Region(500, 400, 100, 80),
                "WorkingState",
                "SubmitButton"
            )
        );
        
        System.out.println("\n--- CONSOLE OUTPUT START ---");
        
        // Call the method with context
        highlightManager.highlightSearchRegionsWithContext(regionsWithContext);
        
        System.out.println("--- CONSOLE OUTPUT END ---\n");
        
        System.out.println("=== END TEST ===");
    }
}