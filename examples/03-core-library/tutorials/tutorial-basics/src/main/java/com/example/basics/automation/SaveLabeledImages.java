package com.example.basics.automation;

import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import static com.example.basics.StateNames.ISLAND;

@Slf4j
@Component
public class SaveLabeledImages {
    
    private StateTransitions stateTransitions;
    private GetNewIsland getNewIsland;
    private IslandRegion islandRegion;
    
    public SaveLabeledImages(StateTransitions stateTransitions,
                             GetNewIsland getNewIsland,
                             IslandRegion islandRegion) {
        this.stateTransitions = stateTransitions;
        this.getNewIsland = getNewIsland;
        this.islandRegion = islandRegion;
    }
    
    public void saveImages(int maxImages) {
        String directory = "labeledImages/";
        // Navigate to ISLAND state
        for (int i=0; i<maxImages; i++) {
            String newIslandType = getNewIsland.getIsland();
            log.info("text = {}", newIslandType);
            if (!newIslandType.isEmpty() && islandRegion.defined()) {
                // In a real implementation, you would save the region to file here
                log.info("Would save region to: {}{}", directory, newIslandType);
            }
        }
    }
}