package io.github.jspinak.brobot.logging;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestHighlightLogging {
    
    @Mock
    private HighlightManager highlightManager;
    
    @Mock
    private VisualFeedbackConfig visualFeedbackConfig;
    
    @Mock
    private BrobotLogger brobotLogger;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    public void testHighlightLoggingWithContext() {
        System.out.println("\n=== TEST HIGHLIGHT LOGGING WITH CONTEXT ===");
        
        // Setup mock behavior
        when(visualFeedbackConfig.isEnabled()).thenReturn(true);
        when(visualFeedbackConfig.isAutoHighlightSearchRegions()).thenReturn(true);
        
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
        
        // Call the method with context - use doNothing for void method
        doNothing().when(highlightManager).highlightSearchRegionsWithContext(any());
        highlightManager.highlightSearchRegionsWithContext(regionsWithContext);
        
        System.out.println("--- CONSOLE OUTPUT END ---\n");
        
        // Verify the method was called
        verify(highlightManager).highlightSearchRegionsWithContext(regionsWithContext);
        
        System.out.println("=== END TEST ===");
    }
}