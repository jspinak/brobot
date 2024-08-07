package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.ScreenObservation;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.ScreenObservationManager;
import io.github.jspinak.brobot.app.buildWithoutNames.buildLive.ScreenObservations;
import io.github.jspinak.brobot.manageStates.StateMemory;
import io.github.jspinak.brobot.manageStates.StateTransitionsManagement;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class UncheckedImageHunter {

    private final ScreenObservations screenObservations;
    private final StateMemory stateMemory;
    private final StateTransitionsManagement stateTransitionsManagement;
    private final ScreenObservationManager screenObservationManager;

    public UncheckedImageHunter(ScreenObservations screenObservations, StateMemory stateMemory,
                                StateTransitionsManagement stateTransitionsManagement,
                                ScreenObservationManager screenObservationManager) {
        this.screenObservations = screenObservations;
        this.stateMemory = stateMemory;
        this.stateTransitionsManagement = stateTransitionsManagement;
        this.screenObservationManager = screenObservationManager;
    }

    /**
     * @return the names of all states with unchecked images.
     */
    public Set<String> getUncheckedStates(List<ScreenObservation> observations) {
        Set<String> uncheckedStates = new HashSet<>();
        for (ScreenObservation screenObservation : observations) {
            if (screenObservation.hasUnvisitedImages()) {
                screenObservation.getStates().forEach(state -> uncheckedStates.add(Integer.toString(state)));
            }
        }
        return uncheckedStates;
    }

    public void setActiveStates(int currentScreenId, List<ScreenObservation> observations) {
        Optional<ScreenObservation> screenObservation = screenObservations.get(currentScreenId, observations);
        if (screenObservation.isEmpty()) return; // this screen id doesn't exist in the repo
        Set<Integer> states = screenObservation.get().getStates();
        stateMemory.removeAllStates();
        states.forEach(state -> stateMemory.addActiveState(Integer.toString(state)));
    }

    public boolean setActiveStatesAndGoToUncheckedState(Set<String> uncheckedStates, List<ScreenObservation> observations) {
        setActiveStates(screenObservationManager.getCurrentScreenId(), observations);
        for (String state : uncheckedStates) if (stateTransitionsManagement.openState(state)) return true;
        return false;
    }

    public int getScreenIdFromActiveStates(List<ScreenObservation> observations) {
        Set<String> activeStates = stateMemory.getActiveStates();
        List<Integer> statesAsInt = new ArrayList<>();
        activeStates.forEach(s -> statesAsInt.add(Integer.getInteger(s)));
        for (ScreenObservation screenObservation : observations) {
            if (isScreen(screenObservation, statesAsInt)) return screenObservation.getId();
        }
        return -1;
    }

    private boolean isScreen(ScreenObservation screenObservation, List<Integer> states) {
        for (Integer i : states) if (!screenObservation.getStates().contains(i)) return false;
        return true;
    }

}
