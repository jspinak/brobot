package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
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

    private AllStatesInProjectService allStatesInProjectService;

    public PathManager(AllStatesInProjectService allStatesInProjectService) {
        this.allStatesInProjectService = allStatesInProjectService;
    }

    public void updateScore(Path path) {
        int score = 0;
        for (String stateName : path.getStates()) {
            Optional<State> optState = allStatesInProjectService.getState(stateName);
            if (optState.isPresent()) score += optState.get().getPathScore();
        }
        path.setScore(score);
    }

    public void updateScores(Paths paths) {
        paths.getPaths().forEach(this::updateScore);
        paths.sort();
    }

    public Paths getCleanPaths(Set<String> activeStates, Paths paths, String failedTransitionStart) {
        Paths cleanPaths = paths.cleanPaths(activeStates, failedTransitionStart);
        updateScores(cleanPaths);
        return cleanPaths;
    }
}
