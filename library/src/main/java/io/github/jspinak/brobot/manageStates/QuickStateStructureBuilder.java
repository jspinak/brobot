package io.github.jspinak.brobot.manageStates;

import org.springframework.stereotype.Component;

@Component
public class QuickStateStructureBuilder {

    private final QuickStateAndTransitionsBuilder stateBuilder;
    private final InitialStates initialStates;

    public QuickStateStructureBuilder(QuickStateAndTransitionsBuilder stateBuilder,
                                      InitialStates initialStates) {
        this.stateBuilder = stateBuilder;
        this.initialStates = initialStates;
    }

    public QuickStateStructureBuilder newState(String name, String image, String toState) {
        stateBuilder
                .init(name)
                .addTransitionImage(image, toState)
                .build();
        return this;
    }

    public QuickStateStructureBuilder newState(String name, String image) {
        stateBuilder
                .init(name)
                .addImage(image)
                .build();
        return this;
    }

    public QuickStateStructureBuilder setStartStates(String... startStates) {
        initialStates.addStateSet(100, startStates);
        return this;
    }

    public void findStartStates() {
        initialStates.findIntialStates();
    }
}
