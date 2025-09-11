package com.example.mrdoob.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@State
@Getter
@Slf4j
public class About {

    private final StateImage aboutText;

    public About() {
        aboutText = new StateImage.Builder().addPattern("aboutText").build();
        log.info("About state created");
    }
}
