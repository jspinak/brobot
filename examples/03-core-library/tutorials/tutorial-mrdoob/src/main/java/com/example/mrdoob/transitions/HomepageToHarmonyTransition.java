package com.example.mrdoob.transitions;

import io.github.jspinak.brobot.annotations.Transition;
import io.github.jspinak.brobot.action.Action;
import com.example.mrdoob.states.Homepage;
import com.example.mrdoob.states.Harmony;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Transition(from = Homepage.class, to = Harmony.class)
@RequiredArgsConstructor
@Slf4j
public class HomepageToHarmonyTransition {
    
    private final Action action;
    private final Homepage homepage;
    
    public boolean execute() {
        log.info("Transitioning from Homepage to Harmony");
        return action.click(homepage.getHarmony()).isSuccess();
    }
}