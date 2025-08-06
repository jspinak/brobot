package com.example.basics.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import lombok.Getter;

/**
 * The WORLD state - shows a map with various islands.
 * 
 * Demonstrates:
 * - State with @State annotation
 * - Multiple similar objects (islands)
 * - Search regions
 * - Dynamic content
 */
@State
@Getter
public class WorldState {
    
    // Navigation elements
    private final StateImage homeButton;
    
    // Island types
    private final StateImage castleIsland;
    private final StateImage minesIsland;
    
    // Map area
    private final Region mapRegion;
    
    public WorldState() {
        // Navigation button
        homeButton = new StateImage.Builder()
            .setName("HomeButton")
            .addPatterns("world/home_button")
            .build();
        
        // Island types - can appear multiple times
        castleIsland = new StateImage.Builder()
            .setName("CastleIsland")
            .addPatterns("world/castle_island_1", "world/castle_island_2")
            .build();
        
        minesIsland = new StateImage.Builder()
            .setName("MinesIsland")
            .addPatterns("world/mines_island_1", "world/mines_island_2")
            .build();
        
        // Define the map area where islands appear
        mapRegion = new Region(100, 150, 1720, 800);
    }
}