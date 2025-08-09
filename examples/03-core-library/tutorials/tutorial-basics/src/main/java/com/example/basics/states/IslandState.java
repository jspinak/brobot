package com.example.basics.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * The ISLAND state - shows details of a specific island.
 * 
 * Demonstrates:
 * - State with @State annotation
 * - Declarative region definition using SearchRegionOnObject
 * - Dynamic region calculation relative to other objects
 * - Island identification and capture
 */
@State
@Component
@Getter
public class IslandState {
    
    private final StateImage islandName;
    private final StateImage islandCapture;
    
    public IslandState() {
        islandName = new StateImage.Builder()
            .addPatterns("islandName")
            .build();
            
        // Define the island capture region relative to the search button
        // The island appears near the search button, so we define the capture
        // region relative to its location
        islandCapture = new StateImage.Builder()
            .setName("IslandCapture")
            .setSearchRegionOnObject(SearchRegionOnObject.builder()
                .targetType(StateObject.Type.IMAGE)
                .targetStateName("World")  // @State removes "State" suffix from class name
                .targetObjectName("searchButton")
                .adjustments(MatchAdjustmentOptions.builder()
                    .addX(-50)      // 50 pixels to the left of search button
                    .addY(-250)     // 250 pixels above search button
                    .absoluteW(200) // Fixed width of 200 pixels
                    .absoluteH(200) // Fixed height of 200 pixels
                    .build())
                .build())
            .build();
    }
}