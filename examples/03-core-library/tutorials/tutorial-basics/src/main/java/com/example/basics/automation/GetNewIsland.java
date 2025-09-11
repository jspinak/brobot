package com.example.basics.automation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.basics.states.IslandState;
import com.example.basics.transitions.WorldToIslandTransition;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.match.Match;

@Component
public class GetNewIsland {

    private final Action action;
    private final IslandState island;
    private final WorldToIslandTransition worldTransitions;

    private Map<String, String> islandTypes = new HashMap<>();

    public GetNewIsland(
            Action action, IslandState island, WorldToIslandTransition worldTransitions) {
        this.action = action;
        this.island = island;
        this.worldTransitions = worldTransitions;

        // Initialize the map
        islandTypes.put("Burg", "Castle");
        islandTypes.put("Mine", "Mines");
        islandTypes.put("Farm", "Farms");
        islandTypes.put("Moun", "Mountains");
        islandTypes.put("Fore", "Forest");
        islandTypes.put("Lake", "Lakes");
    }

    public String getIsland() {
        worldTransitions.execute();
        String textRead = getIslandType();
        for (Map.Entry<String, String> type : islandTypes.entrySet()) {
            if (textRead.contains(type.getKey())) return type.getValue();
        }
        return "";
    }

    private String getIslandType() {
        // Using modern PatternFindOptions with fluent API
        PatternFindOptions findText =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setPauseBeforeBegin(3.0)
                        .build();

        ActionResult result = action.perform(findText, island.getIslandName());
        // Combine all found words into a single string
        return result.getMatchList().stream().map(Match::getText).collect(Collectors.joining(" "));
    }
}
