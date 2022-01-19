package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.primatives.enums.StateEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A single path from a start State to a target State.
 * Paths with lower scores will be chosen before those with higher scores.
 */
@Getter
@Setter
public class Path {

    private List<StateEnum> path = new ArrayList<>();
    private int score; // lower scores are chosen first when selecting a path

    public boolean equals(Path path) {
        return this.path.equals(path.getPath());
    }

    public int size() {
        return path.size();
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }

    public StateEnum get(int i) {
        return path.get(i);
    }

    public void add(StateEnum stateEnum) {
        path.add(stateEnum);
    }

    public boolean contains(StateEnum stateEnum) {
        return path.contains(stateEnum);
    }

    public void reverse() {
        Collections.reverse(path);
    }

    public boolean remove(StateEnum stateEnum) {
        return path.remove(stateEnum);
    }

    public Path getCopy() {
        Path p = new Path();
        p.setPath(new ArrayList<>(path));
        p.setScore(score);
        return p;
    }

    public void print() {
        System.out.format("(%d)", score);
        path.forEach(stateEnum -> System.out.format("-> %s ", stateEnum));
        System.out.println();
    }

    /**
     * If the Path contains a failed Transition, an empty Path will be returned. Otherwise,
     * the Path returned will begin at one of the activeStates.
     *
     * @param activeStates
     * @param failedTransitionStartState
     * @return a Path object
     */
    public Path cleanPath(Set<StateEnum> activeStates, StateEnum failedTransitionStartState) {
        if (path.contains(failedTransitionStartState)) return new Path();
        return trimPath(activeStates);
    }

    public Path trimPath(Set<StateEnum> activeStates) {
        Path trimmedPath = new Path();
        boolean addState = false;
        for (StateEnum stateEnum : path) {
            if (activeStates.contains(stateEnum)) addState = true;
            if (addState) trimmedPath.add(stateEnum);
        }
        return trimmedPath;
    }

}
