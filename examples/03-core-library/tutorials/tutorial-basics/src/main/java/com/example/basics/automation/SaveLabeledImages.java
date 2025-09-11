package com.example.basics.automation;

import static com.example.basics.StateNames.ISLAND;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.navigation.transition.StateNavigator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SaveLabeledImages {

    private StateNavigator stateNavigator;
    private GetNewIsland getNewIsland;
    private IslandRegion islandRegion;

    public SaveLabeledImages(
            StateNavigator stateNavigator, GetNewIsland getNewIsland, IslandRegion islandRegion) {
        this.stateNavigator = stateNavigator;
        this.getNewIsland = getNewIsland;
        this.islandRegion = islandRegion;
    }

    public void saveImages(int maxImages) {
        String directory = "labeledImages/";
        // Navigate to ISLAND state
        stateNavigator.openState(ISLAND);
        for (int i = 0; i < maxImages; i++) {
            String newIslandType = getNewIsland.getIsland();
            log.info("text = {}", newIslandType);
            if (!newIslandType.isEmpty() && islandRegion.ensureRegionReady()) {
                // Capture the island image using the declarative region
                islandRegion.captureIsland();
                // In a real implementation, you would save the captured image to file here
                log.info("Would save captured island to: {}{}", directory, newIslandType);
            }
        }
    }
}
