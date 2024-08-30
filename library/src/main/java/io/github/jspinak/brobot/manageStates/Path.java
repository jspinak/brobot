package io.github.jspinak.brobot.manageStates;

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

    private List<Long> states = new ArrayList<>(); // all states in the path
    private List<IStateTransition> transitions = new ArrayList<>(); // transitions between the states in the path
    private int score; // lower scores are chosen first when selecting a path

    public boolean equals(Path path) {
        return this.states.equals(path.getStates());
    }

    public int size() {
        return states.size();
    }

    public boolean isEmpty() {
        return states.isEmpty();
    }

    public Long get(int i) {
        return states.get(i);
    }

    public void add(Long stateName) {
        states.add(stateName);
    }

    public void add(IStateTransition transition) {
        transitions.add(transition);
    }

    public boolean contains(Long stateId) {
        return states.contains(stateId);
    }

    public void reverse() {
        Collections.reverse(states);
    }

    public boolean remove(Long stateId) {
        return states.remove(stateId);
    }

    public Path getCopy() {
        Path p = new Path();
        p.setStates(new ArrayList<>(states));
        p.setScore(score);
        return p;
    }

    public void print() {
        System.out.format("(%d)", score);
        states.forEach(s -> System.out.format("-> %s ", s));
        System.out.println();
    }

    /**
     * If the Path contains a failed Transition, an empty Path will be returned. Otherwise,
     * the Path returned will begin at one of the activeStates.
     *
     * @param activeStates states that are currently visible.
     * @param failedTransitionStartState is a state corresponding to a path where the transition failed.
     * @return a Path object
     */
    public Path cleanPath(Set<Long> activeStates, Long failedTransitionStartState) {
        if (states.contains(failedTransitionStartState)) return new Path();
        return trimPath(activeStates);
    }

    public Path trimPath(Set<Long> activeStates) {
        Path trimmedPath = new Path();
        boolean addState = false;
        int i=0;
        for (Long stateName : states) {
            // start adding states at an active state
            if (activeStates.contains(stateName)) addState = true;
            if (addState) {
                trimmedPath.add(stateName);
                trimmedPath.add(transitions.get(i));
            }
            i++;
        }
        return trimmedPath;
    }

}
