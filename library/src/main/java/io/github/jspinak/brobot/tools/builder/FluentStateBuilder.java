package io.github.jspinak.brobot.tools.builder;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.FrameworkInitializer;
import io.github.jspinak.brobot.statemanagement.InitialStates;

@Component
public class FluentStateBuilder {

    private final StateStructureBuilder stateAndTransitionBuilder;
    private final InitialStates initialStates;
    private final FrameworkInitializer init;

    public FluentStateBuilder(
            StateStructureBuilder stateBuilder,
            InitialStates initialStates,
            FrameworkInitializer init) {
        this.stateAndTransitionBuilder = stateBuilder;
        this.initialStates = initialStates;
        this.init = init;
    }

    public FluentStateBuilder newState(String name, String image, String toState) {
        stateAndTransitionBuilder.init(name).addTransitionImage(image, toState).build();
        return this;
    }

    public FluentStateBuilder newState(String name, String image) {
        stateAndTransitionBuilder.init(name).addImage(image).build();
        return this;
    }

    public FluentStateBuilder setStartStates(String... startStates) {
        initialStates.addStateSet(100, startStates);
        return this;
    }

    public void build() {
        init.initializeStateStructure();
        initialStates.findInitialStates();
    }
}
