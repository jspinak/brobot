package com.example.basics.states;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateRegion;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * The WORLD state - shows a map with various islands.
 *
 * <p>Demonstrates: - State with @State annotation - Multiple UI elements - StateRegion for
 * area-based interactions - Search regions
 */
@State
@Component
@Getter
@Slf4j
public class WorldState {

    private final StateImage minimap;
    private final StateImage castle;
    private final StateImage farms;
    private final StateImage mines;
    private final StateImage searchButton;
    private final StateImage backButton;
    private final StateImage island1;
    private final StateRegion gameArea;

    public WorldState() {
        minimap =
                new StateImage.Builder()
                        .addPatterns("minimap")
                        // .setFixed(true)  // Method doesn't exist in current API
                        .setSearchRegionForAllPatterns(new Region(900, 0, 124, 124))
                        .build();

        castle = new StateImage.Builder().addPatterns("castle").build();

        farms = new StateImage.Builder().addPatterns("farms").build();

        mines = new StateImage.Builder().addPatterns("mines").build();

        searchButton =
                new StateImage.Builder()
                        .addPatterns("searchButton")
                        .setName("searchButton")
                        .build();

        backButton =
                new StateImage.Builder()
                        .addPatterns("backButton", "homeButton")
                        .setName("backButton")
                        .build();

        island1 =
                new StateImage.Builder()
                        .addPatterns("island1", "firstIsland")
                        .setName("island1")
                        .build();

        // Define regions for area-based interactions
        gameArea =
                new StateRegion.Builder().setSearchRegion(new Region(100, 100, 800, 600)).build();
    }
}
