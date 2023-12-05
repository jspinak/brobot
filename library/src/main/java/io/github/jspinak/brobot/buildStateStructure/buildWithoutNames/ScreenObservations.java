package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ScreenObservations {

    private List<ScreenObservation> observations = new ArrayList<>();

    public void addScreenObservation(ScreenObservation screenObservation) {
        int id = observations.size();
        screenObservation.setId(id);
        observations.add(screenObservation);
    }

    public List<ScreenObservation> getAll() {
        return observations;
    }

    public Optional<ScreenObservation> get(int id) {
        if (observations.size() > id && id >= 0) return Optional.of(observations.get(id));
        return Optional.empty();
    }

    public List<Integer> observationsWithUncheckedImages() {
        List<Integer> uncheckedObs = new ArrayList<>();
        for (ScreenObservation screenObservation : observations) {
            if (screenObservation.hasUnvisitedImages()) uncheckedObs.add(screenObservation.getId());
        }
        return uncheckedObs;
    }

}
