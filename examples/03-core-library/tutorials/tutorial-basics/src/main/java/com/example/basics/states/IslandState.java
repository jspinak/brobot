package com.example.basics.states;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;

import lombok.Getter;

/**
 * The ISLAND state - shows details of a specific island.
 *
 * <p>Demonstrates: - State with @State annotation - Declarative region definition using
 * SearchRegionOnObject - Dynamic region calculation relative to other objects - Island
 * identification and capture
 */
@State
@Component
@Getter
public class IslandState {

    private final StateImage islandName;
    private final StateImage islandCapture;
    private final StateImage backToWorldButton;

    public IslandState() {
        islandName = new StateImage.Builder().addPatterns("islandName").build();

        backToWorldButton =
                new StateImage.Builder()
                        .addPatterns("backToWorld", "worldMap", "exitIsland")
                        .setName("backToWorldButton")
                        .build();

        // Define the island capture region relative to the search button
        // The island appears near the search button, so we define the capture
        // region relative to its location
        islandCapture =
                new StateImage.Builder()
                        .setName("IslandCapture")
                        .setSearchRegionOnObject(
                                SearchRegionOnObject.builder()
                                        .setTargetType(StateObject.Type.IMAGE)
                                        .setTargetStateName(
                                                "World") // @State removes "State" suffix from class
                                        // name
                                        .setTargetObjectName("searchButton")
                                        .setAdjustments(
                                                MatchAdjustmentOptions.builder()
                                                        .setAddX(-50) // 50 pixels to the left of
                                                        // search button
                                                        .setAddY(-250) // 250 pixels above search
                                                        // button
                                                        .setAbsoluteW(200) // Fixed width of 200 pixels
                                                        .setAbsoluteH(
                                                                200) // Fixed height of 200 pixels
                                                        .build())
                                        .build())
                        .build();
    }
}
