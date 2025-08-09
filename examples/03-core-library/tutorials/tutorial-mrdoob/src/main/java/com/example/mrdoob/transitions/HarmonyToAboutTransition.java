package com.example.mrdoob.transitions;

import io.github.jspinak.brobot.annotations.Transition;
import io.github.jspinak.brobot.action.Action;
import com.example.mrdoob.states.Harmony;
import com.example.mrdoob.states.About;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Transition(from = Harmony.class, to = About.class)
@RequiredArgsConstructor
@Slf4j
public class HarmonyToAboutTransition {
    
    private final Action action;
    private final Harmony harmony;
    
    public boolean execute() {
        log.info("Transitioning from Harmony to About");
        return action.click(harmony.getAbout()).isSuccess();
    }
}