package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.state.state.State;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.services.StateService;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * If a Path is unsuccessfully traversed, we find ourselves somewhere in the middle of
 * the Path. When this happens, we don't need to search again for Path objects, but we do
 * need to get rid of some Path objects and truncate others. Also, we will need to recalculate
 * Path scores.
 */
@Component
public class PathManager {

    private StateService stateService;

    public PathManager(StateService stateService) {
        this.stateService = stateService;
    }

    public void updateScore(Path path) {
        int score = 0;
        for (StateEnum stateEnum : path.getPath()) {
            Optional<State> optState = stateService.findByName(stateEnum);
            if (optState.isPresent()) score += optState.get().getPathScore();
        }
        path.setScore(score);
    }

    public void updateScores(Paths paths) {
        paths.getPaths().forEach(this::updateScore);
        paths.sort();
    }

    public Paths getCleanPaths(Set<StateEnum> activeStates, Paths paths, StateEnum failedTransitionStart) {
        Paths cleanPaths = paths.cleanPaths(activeStates, failedTransitionStart);
        updateScores(cleanPaths);
        return cleanPaths;
    }
}
