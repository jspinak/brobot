package com.example.basics.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.element.Region;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * The ISLAND state - shows details of a specific island.
 * 
 * Demonstrates:
 * - State with @State annotation
 * - StateRegion for region-based operations
 * - Island identification and capture
 */
@State
@Component
@Getter
public class IslandState {
    
    private final StateImage islandName;
    private final StateRegion islandRegion;
    
    public IslandState() {
        islandName = new StateImage.Builder()
            .addPatterns("islandName")
            .build();
            
        islandRegion = new StateRegion.Builder()
            .build(); // Will be defined dynamically based on search button location
    }
}