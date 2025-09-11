package com.example.basics.states;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * The HOME state - the starting point of the demo application.
 *
 * <p>Demonstrates: - State creation with @State annotation in v1.1.0 - Fixed position images with
 * snapshots - StateImage configuration - ActionRecord initialization for mock testing
 */
@State(initial = true) // Mark as initial state
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
        toWorldButton =
                new StateImage.Builder()
                        .addPatterns("toWorldButton")
                        // .setFixed(true)  // Method doesn't exist in current API
                        .build();

        searchButton =
                new StateImage.Builder()
                        .addPatterns("searchButton")
                        .setSearchRegionForAllPatterns(new Region(0, 0, 500, 100)) // Top area only
                        .build();

        // Initialize with action history for mock testing
        initializeActionHistory();
    }

    private void initializeActionHistory() {
        // Add mock action records for testing using the utility class
        // TODO: ActionHistory API needs to be updated for current version
        // toWorldButton.getPatterns().get(0).getActionHistory()
        //     .addSnapshot(ActionRecordTestUtils.createActionRecord(0.95, 220, 600, 20, 20));

        // searchButton.getPatterns().get(0).getActionHistory()
        //     .addSnapshot(ActionRecordTestUtils.createActionRecord(0.92, 250, 50, 100, 30));
    }
}
