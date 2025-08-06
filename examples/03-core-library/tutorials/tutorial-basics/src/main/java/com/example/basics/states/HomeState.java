package com.example.basics.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import lombok.Getter;

/**
 * The HOME state - the starting point of the demo application.
 * 
 * Demonstrates:
 * - State creation with @State annotation in v1.1.0
 * - Fixed position images with snapshots
 * - StateImage configuration
 */
@State(initial = true)  // Mark as initial state
@Getter
public class HomeState {
    
    // Button to navigate to the World state
    private final StateImage toWorldButton;
    
    // Other UI elements
    private final StateImage logo;
    private final StateImage menuBar;
    
    public HomeState() {
        // Create state images
        toWorldButton = new StateImage.Builder()
            .setName("ToWorldButton")
            .addPatterns("home/to_world_button")  // No .png extension needed
            .build();
        toWorldButton.setFixedSearchRegion(new Region(220, 600, 20, 20));
        
        logo = new StateImage.Builder()
            .setName("HomeLogo")
            .addPatterns("home/home_logo")
            .build();
        logo.setFixedSearchRegion(new Region(500, 100, 200, 100));
        
        menuBar = new StateImage.Builder()
            .setName("MenuBar")
            .addPatterns("home/menu_bar")
            .build();
    }
}