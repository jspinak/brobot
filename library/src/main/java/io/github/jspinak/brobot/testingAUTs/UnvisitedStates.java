package io.github.jspinak.brobot.testingAUTs;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.manageStates.PathFinder;
import io.github.jspinak.brobot.manageStates.Paths;
import io.github.jspinak.brobot.manageStates.StateMemory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UnvisitedStates {

    private final StateMemory stateMemory;
    private final AllStatesInProjectService allStatesInProjectService;
    private final PathFinder pathFinder;

    private TreeMap<Integer, Long> unvisitedDistance = new TreeMap<>();

    public UnvisitedStates(StateMemory stateMemory, AllStatesInProjectService allStatesInProjectService,
                           PathFinder pathFinder) {
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
        this.pathFinder = pathFinder;
    }

    public Set<Long> getUnvisitedStates() {
        Set<Long> unvisited = new HashSet<>();
        allStatesInProjectService.getAllStates().forEach(st -> {
            if (st.getTimesVisited() == 0) unvisited.add(st.getId());
        });
        return unvisited;
    }

    public Optional<Long> getClosestUnvisited(Set<Long> startStates) {
        Set<Long> unvisited = getUnvisitedStates();
        unvisitedDistance = new TreeMap<>();
        unvisited.forEach(state -> {
            Paths paths = pathFinder.getPathsToState(startStates, state);
            int score = paths.getBestScore();
            if (score > 0) unvisitedDistance.put(score, state);
        });
        if (unvisitedDistance.isEmpty()) return Optional.empty();
        return Optional.ofNullable(unvisitedDistance.firstEntry().getValue());
    }

    public Optional<Long> getClosestUnvisited() {
        return getClosestUnvisited(stateMemory.getActiveStates());
    }
}
