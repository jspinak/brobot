package io.github.jspinak.brobot.testingAUTs;

import io.github.jspinak.brobot.manageStates.PathFinder;
import io.github.jspinak.brobot.manageStates.Paths;
import io.github.jspinak.brobot.manageStates.StateMemory;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.services.StateService;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UnvisitedStates {

    private final StateMemory stateMemory;
    private final StateService stateService;
    private PathFinder pathFinder;

    private TreeMap<Integer, StateEnum> unvisitedDistance = new TreeMap<>();

    public UnvisitedStates(StateMemory stateMemory, StateService stateService,
                           PathFinder pathFinder) {
        this.stateMemory = stateMemory;
        this.stateService = stateService;
        this.pathFinder = pathFinder;
    }

    public Set<StateEnum> getUnvisitedStates() {
        Set<StateEnum> unvisited = new HashSet<>();
        stateService.findAllStates().forEach(st -> {
            if (st.getTimesVisited() == 0) unvisited.add(st.getName());
        });
        return unvisited;
    }

    public Optional<StateEnum> getClosestUnvisited(Set<StateEnum> startStates) {
        Set<StateEnum> unvisited = getUnvisitedStates();
        unvisitedDistance = new TreeMap<>();
        unvisited.forEach(state -> {
            Paths paths = pathFinder.getPathsToState(startStates, state);
            int score = paths.getBestScore();
            if (score > 0) unvisitedDistance.put(score, state);
        });
        if (unvisitedDistance.isEmpty()) return Optional.empty();
        return Optional.ofNullable(unvisitedDistance.firstEntry().getValue());
    }

    public Optional<StateEnum> getClosestUnvisited() {
        return getClosestUnvisited(stateMemory.getActiveStates());
    }
}
