package com.example.basics.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.match.Match;
import org.sikuli.script.Pattern;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * The HOME state - the starting point of the demo application.
 * 
 * Demonstrates:
 * - State creation with @State annotation in v1.1.0
 * - Fixed position images with snapshots
 * - StateImage configuration
 * - ActionRecord initialization for mock testing
 */
@State(initial = true)  // Mark as initial state
@Component
@Getter
@Slf4j
public class HomeState {
    
    // Button to navigate to the World state
    private final StateImage toWorldButton;
    
    // Other UI elements  
    private final StateImage searchButton;
    
    public HomeState() {
        // Define UI elements with fluent builder pattern
        toWorldButton = new StateImage.Builder()
            .addPatterns("toWorldButton")
            // .setFixed(true)  // Method doesn't exist in current version
            .build();
            
        searchButton = new StateImage.Builder()
            .addPatterns("searchButton")
            // .setSearchRegion(new Region(0, 0, 500, 100))  // Method doesn't exist
            .build();
            
        // Initialize with action history for mock testing
        initializeActionHistory();
    }
    
    private void initializeActionHistory() {
        // Initialize action history would go here in real implementation
        // The getActionHistory() method doesn't exist on Pattern in current version
    }
    
    private ActionRecord createActionRecord(double similarity, int x, int y, int w, int h) {
        return new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(similarity)
                .build())
            .addMatch(new Match.Builder()
                .setRegion(x, y, w, h)
                .setSimScore(similarity)
                .build())
            .setActionSuccess(true)
            .build();
    }
}