package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * A list of Path objects comprising the possible ways to go from
 * start States to the target State.
 */
@Getter
public class Paths {

    private List<Path> paths = new ArrayList<>();

    public Paths() {}
    public Paths(List<Path> pathList) {
        paths = pathList;
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
        for (int i=0; i<this.paths.size(); i++) {
            if (!this.paths.get(i).equals(paths.getPaths().get(i))) return false;
        }
        return true;
    }

    public Paths cleanPaths(Set<String> activeStates, String failedTransitionStartState) {
        Paths newPaths = new Paths();
        paths.forEach(path -> newPaths.addPath(path.cleanPath(activeStates, failedTransitionStartState)));
        return newPaths;
    }

    public int getBestScore() {
        if (paths.isEmpty()) return 0;
        int best = paths.get(0).getScore();
        for (int i = 1; i<paths.size(); i++) {
            int newScore = paths.get(i).getScore();
            if (newScore > best) best = newScore;
        }
        return best;
    }

    public void print() {
        if (paths.isEmpty()) return;
        Report.println("_(score)_Paths Found_");
        paths.forEach(Path::print);
    }
}
