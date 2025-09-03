package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Debug test to verify highlighting is working correctly.
 */
@SpringBootTest
@ActiveProfiles({"test"})
public class HighlightDebugTest extends BrobotTestBase {
    
    private static final Logger log = LoggerFactory.getLogger(HighlightDebugTest.class);

    @Autowired
    private Action action;
    
    @Autowired
    private PromptState promptState;
    
    @Autowired
    private WorkingState workingState;
    
    @Autowired(required = false)
    private VisualFeedbackConfig visualFeedbackConfig;
    
    @Value("${brobot.highlight.enabled:false}")
    private boolean highlightEnabled;
    
    @Value("${brobot.highlighting.enabled:false}")
    private boolean highlightingEnabled;
    
    @Value("${brobot.highlight.auto-highlight-finds:false}")
    private boolean autoHighlightFinds;
    
    @Value("${brobot.highlight.find.duration:1.0}")
    private double highlightDuration;
    
    @Value("${brobot.core.mock:true}")
    private boolean mockMode;
    
    @BeforeEach
    public void setup() {
        log.info("==================================================");
        log.info("         HIGHLIGHT DEBUGGING INFORMATION         ");
        log.info("==================================================");
        log.info("Mock mode: {}", mockMode);
        log.info("brobot.highlight.enabled: {}", highlightEnabled);
        log.info("brobot.highlighting.enabled: {}", highlightingEnabled);
        log.info("brobot.highlight.auto-highlight-finds: {}", autoHighlightFinds);
        log.info("brobot.highlight.find.duration: {} seconds", highlightDuration);
        
        if (visualFeedbackConfig != null) {
            log.info("VisualFeedbackConfig loaded successfully:");
            log.info("  - enabled: {}", visualFeedbackConfig.isEnabled());
            log.info("  - autoHighlightFinds: {}", visualFeedbackConfig.isAutoHighlightFinds());
            log.info("  - autoHighlightSearchRegions: {}", visualFeedbackConfig.isAutoHighlightSearchRegions());
        } else {
            log.warn("VisualFeedbackConfig is NULL - highlighting will not work!");
        }
        
        log.info("==================================================");
        
        if (mockMode) {
            log.warn("Running in MOCK MODE - actual screen highlighting will not be visible");
            log.info("In mock mode, highlighting operations are simulated but not displayed");
        }
    }

    @Test
    public void testHighlightConfiguration() {
        log.info("=== Testing Highlight Configuration ===");
        
        // Check that the configuration is loaded
        assertNotNull(visualFeedbackConfig, "VisualFeedbackConfig should be loaded");
        
        // Check the properties
        assertTrue(highlightEnabled, "brobot.highlight.enabled should be true");
        assertTrue(highlightingEnabled, "brobot.highlighting.enabled should be true");
        assertTrue(autoHighlightFinds, "brobot.highlight.auto-highlight-finds should be true");
        
        // Check VisualFeedbackConfig
        assertTrue(visualFeedbackConfig.isEnabled(), "VisualFeedbackConfig.enabled should be true");
        assertTrue(visualFeedbackConfig.isAutoHighlightFinds(), "VisualFeedbackConfig.autoHighlightFinds should be true");
        
        log.info("✓ All highlighting configuration checks passed");
    }

    @Test
    public void testFindWithHighlighting() {
        log.info("=== Testing Find with Highlighting ===");
        
        if (!highlightEnabled || !autoHighlightFinds) {
            log.warn("Highlighting is disabled in configuration - skipping test");
            return;
        }
        
        // Configure find options
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSearchDuration(2)
                .setSimilarity(0.70)
                .build();
        
        // Test finding the Claude prompt
        ObjectCollection promptCollection = new ObjectCollection.Builder()
                .withImages(promptState.getClaudePrompt())
                .build();
        
        log.info("Performing find operation...");
        log.info("If highlighting is working correctly:");
        log.info("  - In REAL mode: You should see a green border around found images");
        log.info("  - In MOCK mode: Highlighting is simulated (no visual output)");
        
        ActionResult result = action.perform(findOptions, promptCollection);
        
        if (result.isSuccess()) {
            log.info("✓ Find successful - {} matches found", result.getMatchList().size());
            log.info("Highlighting should have been triggered for the found matches");
            
            // Give time to see the highlight
            try {
                Thread.sleep((long)(highlightDuration * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            if (mockMode) {
                log.info("No matches found (expected in mock mode)");
            } else {
                log.warn("No matches found - highlighting could not be tested");
            }
        }
    }
    
    @Test
    public void debugHighlightPath() {
        log.info("=== Debugging Highlight Execution Path ===");
        
        // This test traces through the highlighting logic
        log.info("1. Configuration loaded: {}", visualFeedbackConfig != null);
        log.info("2. Highlight enabled: {}", highlightEnabled);
        log.info("3. Highlighting enabled: {}", highlightingEnabled);
        log.info("4. Auto-highlight finds: {}", autoHighlightFinds);
        
        if (visualFeedbackConfig != null) {
            log.info("5. VisualFeedbackConfig.enabled: {}", visualFeedbackConfig.isEnabled());
            log.info("6. VisualFeedbackConfig.autoHighlightFinds: {}", visualFeedbackConfig.isAutoHighlightFinds());
        }
        
        log.info("7. Mock mode: {}", mockMode);
        
        // Check if all conditions for highlighting are met
        boolean shouldHighlight = highlightingEnabled && 
                                 visualFeedbackConfig != null &&
                                 visualFeedbackConfig.isEnabled() &&
                                 visualFeedbackConfig.isAutoHighlightFinds();
        
        log.info("8. Should highlight (based on FindPipeline logic): {}", shouldHighlight);
        
        if (!shouldHighlight) {
            log.warn("Highlighting will NOT occur because one or more conditions are not met");
            if (!highlightingEnabled) {
                log.warn("  - brobot.highlighting.enabled is false");
            }
            if (visualFeedbackConfig == null) {
                log.warn("  - VisualFeedbackConfig is null");
            } else {
                if (!visualFeedbackConfig.isEnabled()) {
                    log.warn("  - VisualFeedbackConfig.enabled is false");
                }
                if (!visualFeedbackConfig.isAutoHighlightFinds()) {
                    log.warn("  - VisualFeedbackConfig.autoHighlightFinds is false");
                }
            }
        } else {
            log.info("✓ All conditions for highlighting are met");
        }
    }
}