package io.github.jspinak.brobot.navigation.path;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import io.github.jspinak.brobot.navigation.service.StateService;

import lombok.Getter;

/**
 * Collection of navigation paths in the Brobot model-based GUI automation framework.
 *
 * <p>Paths manages multiple Path objects representing all discovered routes from a set of starting
 * states to a target state. This collection is the output of pathfinding algorithms and serves as
 * the input for path selection and execution strategies in the Path Traversal Model (ξ).
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Multiple Routes</b>: Stores all valid paths, not just the optimal one
 *   <li><b>Score-based Sorting</b>: Orders paths by quality for intelligent selection
 *   <li><b>Path Cleaning</b>: Removes invalid paths based on active states and failures
 *   <li><b>Best Score Tracking</b>: Identifies the highest quality path available
 * </ul>
 *
 * <p>Path management operations:
 *
 * <ul>
 *   <li><b>Sorting</b>: Arranges paths by score (ascending) for optimal selection
 *   <li><b>Cleaning</b>: Filters out paths containing failed transitions or unreachable states
 *   <li><b>Comparison</b>: Determines equivalence between path collections
 *   <li><b>Reporting</b>: Provides formatted output for debugging and analysis
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Storing pathfinding results for later execution
 *   <li>Selecting optimal paths based on current conditions
 *   <li>Providing fallback options when primary paths fail
 *   <li>Analyzing navigation complexity in the state graph
 * </ul>
 *
 * <p>In the model-based approach, Paths represents the framework's understanding of all possible
 * ways to achieve a navigation goal. This comprehensive view enables robust automation that can
 * adapt to failures, choose optimal routes, and provide insights into the application's
 * navigational structure.
 *
 * <p>The ability to maintain multiple paths is crucial for handling the stochastic nature of GUI
 * applications, where the optimal path may vary based on current conditions or where primary paths
 * may occasionally fail due to timing or state variations.
 *
 * @since 1.0
 * @see Path
 * @see PathFinder
 * @see PathManager
 * @see PathTraverser
 */
@Getter
public class Paths {

    private List<Path> paths = new ArrayList<>();

    public Paths() {}

    public Paths(List<Path> pathList) {
        paths = new ArrayList<>(pathList); // Create mutable copy
    }

    public boolean isEmpty() {
        return paths.isEmpty();
    }

    public void addPath(Path path) {
        if (!path.isEmpty()) paths.add(path);
    }

    public void sort() {
        paths.sort(Comparator.comparing(Path::getScore));
    }

    public boolean equals(Paths paths) {
        if (this.paths.size() != paths.getPaths().size()) return false;
        for (int i = 0; i < this.paths.size(); i++) {
            if (!this.paths.get(i).equals(paths.getPaths().get(i))) return false;
        }
        return true;
    }

    public Paths cleanPaths(Set<Long> activeStates, Long failedTransitionStartState) {
        Paths newPaths = new Paths();
        paths.forEach(
                path -> newPaths.addPath(path.cleanPath(activeStates, failedTransitionStartState)));
        return newPaths;
    }

    public int getBestScore() {
        if (paths.isEmpty()) return 0;
        int best = paths.get(0).getScore();
        for (int i = 1; i < paths.size(); i++) {
            int newScore = paths.get(i).getScore();
            if (newScore > best) best = newScore;
        }
        return best;
    }

    public void print() {
        if (paths.isEmpty()) return;
        System.out.println("\n=== Paths Found (score) ===");
        paths.forEach(Path::print);
    }

    public void print(StateService stateService) {
        if (paths.isEmpty()) return;
        System.out.println("\n=== Paths Found (score) ===");
        paths.forEach(path -> path.print(stateService));
    }
}
