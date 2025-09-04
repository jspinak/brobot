package io.github.jspinak.brobot.integration.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Pattern;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test to demonstrate and diagnose the duplicate highlighting issue
 * when multiple patterns share the same search region.
 */
@Disabled("Missing ClaudeAutomatorApplication dependency")
@SpringBootTest // (classes = com.claude.automator.ClaudeAutomatorApplication.class)
public class HighlightDuplicationTest extends BrobotTestBase {
    
    @Test
    public void demonstrateDuplicateHighlightIssue() {
        System.out.println("\n" +
            "================================================================================\n" +
            "DUPLICATE HIGHLIGHT ISSUE DEMONSTRATION\n" +
            "================================================================================\n");
        
        // Create the PromptState which uses setFixedSearchRegion
        PromptState promptState = new PromptState();
        StateImage claudePrompt = promptState.getClaudePrompt();
        
        System.out.printf("StateImage '%s' has %d patterns%n", 
                claudePrompt.getName(), 
                claudePrompt.getPatterns().size());
        
        // Show that each pattern has the same search region
        System.out.println("\nINDIVIDUAL PATTERN SEARCH REGIONS:");
        System.out.println("-----------------------------------");
        int patternIndex = 0;
        Region firstRegion = null;
        
        for (Pattern pattern : claudePrompt.getPatterns()) {
            Region fixedRegion = pattern.getSearchRegions().getFixedRegion();
            System.out.printf("Pattern %d: %s -> Search Region: %s%n", 
                    patternIndex++, 
                    pattern.getName(), 
                    fixedRegion);
            
            if (firstRegion == null) {
                firstRegion = fixedRegion;
            } else {
                boolean isSame = (fixedRegion == firstRegion);
                boolean isEqual = regionsEqual(fixedRegion, firstRegion);
                System.out.printf("  Same object reference as first region? %s%n", isSame);
                System.out.printf("  Equal coordinates as first region? %s%n", isEqual);
            }
        }
        
        System.out.println("\nPROBLEM DIAGNOSIS:");
        System.out.println("------------------");
        System.out.println("When highlighting is enabled, EACH pattern's search region gets highlighted.");
        System.out.printf("Since all %d patterns have the same region coordinates,%n", claudePrompt.getPatterns().size());
        System.out.printf("we get %d overlapping highlights at the same location!%n", claudePrompt.getPatterns().size());
        System.out.println("This causes the visual artifacts where the highlight appears incorrect.\n");
        
        // Demonstrate the solution
        System.out.println("SOLUTION USING SingleRegionHighlighter:");
        System.out.println("----------------------------------------");
        SingleRegionHighlighter.debugSearchRegions(claudePrompt);
        
        System.out.println("\nThe SingleRegionHighlighter identifies and returns only unique regions,");
        System.out.println("preventing duplicate highlights even when multiple patterns share the same region.\n");
        
        System.out.println("================================================================================\n");
    }
    
    private boolean regionsEqual(Region r1, Region r2) {
        if (r1 == r2) return true;
        if (r1 == null || r2 == null) return false;
        
        return r1.getX() == r2.getX() && 
               r1.getY() == r2.getY() && 
               r1.getW() == r2.getW() && 
               r1.getH() == r2.getH();
    }
}