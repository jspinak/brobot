package com.example.mrdoob.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State(initial = true)
@Getter
@Slf4j
public class Homepage {
    
    private final StateImage harmony;
    
    public Homepage() {
        harmony = new StateImage.Builder()
                .addPattern("harmonyIcon")
                .build();
        log.info("Homepage state created");
    }
}