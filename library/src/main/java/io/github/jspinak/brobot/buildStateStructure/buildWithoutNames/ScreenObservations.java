package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ScreenObservations {

    private Map<Integer, ScreenObservation> observations = new HashMap<>();

    public void addScreenObservation(int id, ScreenObservation screenObservation) {
        observations.put(id, screenObservation);
    }

    public Map<Integer, ScreenObservation> getAll() {
        return observations;
    }

    public Optional<ScreenObservation> get(int id) {
        if (observations.containsKey(id)) return Optional.of(observations.get(id));
        return Optional.empty();
    }

    public List<Integer> observationsWithUncheckedImages() {
        List<Integer> uncheckedObs = new ArrayList<>();
        for (ScreenObservation screenObservation : observations.values()) {
            if (screenObservation.hasUnvisitedImages()) uncheckedObs.add(screenObservation.getId());
        }
        return uncheckedObs;
    }

}
