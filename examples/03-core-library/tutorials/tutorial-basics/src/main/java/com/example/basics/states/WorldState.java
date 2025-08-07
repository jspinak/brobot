package com.example.basics.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.element.Region;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * The WORLD state - shows a map with various islands.
 * 
 * Demonstrates:
 * - State with @State annotation
 * - Multiple UI elements
 * - StateRegion for area-based interactions
 * - Search regions
 */
@State
@Component
@Getter
public class WorldState {
    
    private final StateImage minimap;
    private final StateImage castle;
    private final StateImage farms;
    private final StateImage mines;
    private final StateImage searchButton;
    private final StateRegion gameArea;
    
    public WorldState() {
        minimap = new StateImage.Builder()
            .addPatterns("minimap")
            // .setFixed(true) // Method doesn't exist in current version
            // .setSearchRegion(new Region(900, 0, 124, 124)) // Method doesn't exist
            .build();
            
        castle = new StateImage.Builder()
            .addPatterns("castle")
            .build();
            
        farms = new StateImage.Builder()
            .addPatterns("farms")
            .build();
            
        mines = new StateImage.Builder()
            .addPatterns("mines")
            .build();
            
        searchButton = new StateImage.Builder()
            .addPatterns("searchButton")
            .build();
            
        // Define regions for area-based interactions
        gameArea = new StateRegion.Builder()
            .setSearchRegion(new Region(100, 100, 800, 600))
            .build();
    }
}