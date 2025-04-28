package io.github.jspinak.brobot.manageStates;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.services.Init;

@Component
public class QuickStateStructureBuilder {

    private final QuickStateAndTransitionsBuilder stateAndTransitionBuilder;
    private final InitialStates initialStates;
    private final Init init;

    public QuickStateStructureBuilder(QuickStateAndTransitionsBuilder stateBuilder,
            InitialStates initialStates, Init init) {
        this.stateAndTransitionBuilder = stateBuilder;
        this.initialStates = initialStates;
        this.init = init;
    }

    public QuickStateStructureBuilder newState(String name, String image, String toState) {
        stateAndTransitionBuilder
                .init(name)
                .addTransitionImage(image, toState)
                .build();
        return this;
    }

    public QuickStateStructureBuilder newState(String name, String image) {
        stateAndTransitionBuilder
                .init(name)
                .addImage(image)
                .build();
        return this;
    }

    public QuickStateStructureBuilder setStartStates(String... startStates) {
        initialStates.addStateSet(100, startStates);
        return this;
    }

    public void build() {
        init.initializeStateStructure();
        initialStates.findIntialStates();
    }
}
