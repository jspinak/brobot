package com.example.mrdoob;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.navigation.transition.StateNavigator;

@Component
public class AutomationInstructions {

    private final StateNavigator stateNavigator;

    public AutomationInstructions(StateNavigator stateNavigator) {
        this.stateNavigator = stateNavigator;
    }

    public void doAutomation() {
        stateNavigator.openState("about");
    }
}
