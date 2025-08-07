package com.example.basics.automation;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Region;
import com.example.basics.states.WorldState;
import com.example.basics.states.IslandState;
import org.springframework.stereotype.Component;

@Component
public class IslandRegion {
    
    private final Action action;
    private final WorldState world;
    private final IslandState island;
    
    public IslandRegion(Action action, WorldState world, IslandState island) {
        this.action = action;
        this.world = world;
        this.island = island;
    }
    
    public boolean defined() {
        if (island.getIslandRegion().defined()) return true;
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .build();
        ObjectCollection searchButton = new ObjectCollection.Builder()
                .withImages(world.getSearchButton())
                .build();
        // In modern Brobot, region definition is handled differently
        // For now, just return if the region is defined
        return island.getIslandRegion().defined();
    }
    
    public Region getRegion() {
        return island.getIslandRegion().getSearchRegion();
    }
}