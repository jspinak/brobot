package io.github.jspinak.brobot.examples;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Example demonstrating the improved concise logging for Find operations.
 * 
 * This example shows how the new logging system:
 * - Deduplicates repeated pattern information
 * - Shows pattern details only once per search session
 * - Provides a clear summary at the end
 * - Reduces log verbosity while maintaining useful diagnostic information
 */
@SpringBootApplication
@Component
public class ConciseLoggingExample {

    @Autowired
    private Action action;
    
    @Autowired
    private LoggingVerbosityConfig loggingVerbosityConfig;
    
    public static void main(String[] args) {
        SpringApplication.run(ConciseLoggingExample.class, args);
    }
    
    @PostConstruct
    public void demonstrateConciseLogging() {
        System.out.println("\n========================================");
        System.out.println("Demonstrating Concise Find Logging");
        System.out.println("========================================\n");
        
        // Set verbosity to NORMAL to see concise output
        loggingVerbosityConfig.setVerbosity(LoggingVerbosityConfig.VerbosityLevel.NORMAL);
        
        // Create test StateImages with multiple patterns
        StateImage buttonImage = createTestStateImage("button", 3);
        StateImage promptImage = createTestStateImage("prompt", 3);
        
        // Create ObjectCollection
        ObjectCollection collection = new ObjectCollection();
        collection.getStateImages().add(buttonImage);
        collection.getStateImages().add(promptImage);
        
        System.out.println("\n--- BEFORE: Verbose repetitive logging ---");
        System.out.println("(Imagine 50+ lines of repeated pattern information here)\n");
        
        System.out.println("\n--- AFTER: Concise deduplicated logging ---\n");
        
        // Perform find operation - this will use the new concise logging
        ActionResult result = action.perform(ActionType.FIND, collection);
        
        System.out.println("\n--- Key improvements: ---");
        System.out.println("1. Pattern details shown only once, not repeated");
        System.out.println("2. Subsequent searches show '(repeat)' or similarity changes only");
        System.out.println("3. Session summary shows total patterns searched and time taken");
        System.out.println("4. Scene information consolidated on single line");
        System.out.println("5. Reduced output from ~50 lines to ~10 lines");
        
        System.out.println("\n========================================\n");
    }
    
    private StateImage createTestStateImage(String name, int patternCount) {
        StateImage stateImage = new StateImage();
        stateImage.setName(name);
        
        // Add multiple patterns to simulate repetitive searching
        for (int i = 1; i <= patternCount; i++) {
            Pattern pattern = new Pattern();
            pattern.setName(name + "-" + i);
            pattern.setImagePath("images/" + name + "-" + i + ".png");
            
            // Add a search region to demonstrate region logging
            Region searchRegion = new Region(0, 400, 800, 400);
            pattern.getRegions().add(searchRegion);
            
            stateImage.getPatterns().add(pattern);
        }
        
        return stateImage;
    }
}