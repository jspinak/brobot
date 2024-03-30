package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class ObservedStates {

    private List<ObservedState> allStates = new ArrayList<>();

    public void addObservedState(ObservedState state) {
        this.allStates.add(state);
    }
}
