package io.github.jspinak.brobot.actions.methods.basicactions.capture.replay;

import org.springframework.stereotype.Component;

@Component
public class ReplayActionsXml {

    private ReplayAction replayAction;

    public ReplayActionsXml(ReplayAction replayAction) {
        this.replayAction = replayAction;
    }


}
