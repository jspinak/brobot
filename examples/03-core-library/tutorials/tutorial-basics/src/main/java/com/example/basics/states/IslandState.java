package com.example.basics.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.model.element.Region;
import lombok.Getter;

/**
 * The ISLAND state - shows details of a specific island.
 * 
 * Demonstrates:
 * - State with @State annotation
 * - StateString for text/OCR regions
 * - Resource counters
 * - Dynamic content reading
 */
@State
@Getter
public class IslandState {
    
    // Navigation
    private final StateImage backToWorldButton;
    
    // Island identification
    private final StateString islandNameRegion;
    
    // Resource counters as text regions
    private final StateString goldCounter;
    private final StateString woodCounter;
    private final StateString stoneCounter;
    
    // Action buttons
    private final StateImage collectButton;
    private final StateImage upgradeButton;
    
    public IslandState() {
        // Navigation button
        backToWorldButton = new StateImage.Builder()
            .setName("BackToWorldButton")
            .addPatterns("island/back_to_world")
            .build();
        
        // Island name at top of screen (for OCR)
        islandNameRegion = new StateString.Builder()
            .setName("IslandName")
            .setSearchRegion(new Region(700, 50, 520, 60))  // Center top
            .build();
        
        // Resource counters (for OCR)
        goldCounter = new StateString.Builder()
            .setName("GoldCounter")
            .setSearchRegion(new Region(1600, 100, 200, 40))
            .build();
        
        woodCounter = new StateString.Builder()
            .setName("WoodCounter")
            .setSearchRegion(new Region(1600, 150, 200, 40))
            .build();
        
        stoneCounter = new StateString.Builder()
            .setName("StoneCounter")
            .setSearchRegion(new Region(1600, 200, 200, 40))
            .build();
        
        // Action buttons
        collectButton = new StateImage.Builder()
            .setName("CollectButton")
            .addPatterns("island/collect_button")
            .build();
        
        upgradeButton = new StateImage.Builder()
            .setName("UpgradeButton")
            .addPatterns("island/upgrade_button")
            .build();
    }
}