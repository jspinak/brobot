package com.example.mrdoob.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State
@Getter
@Slf4j
public class Harmony {
    
    private final StateImage about;
    
    public Harmony() {
        about = new StateImage.Builder()
                .addPattern("aboutButton")
                .build();
        log.info("Harmony state created");
    }
}