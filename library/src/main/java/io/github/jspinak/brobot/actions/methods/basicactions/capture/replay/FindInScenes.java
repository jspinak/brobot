package io.github.jspinak.brobot.actions.methods.basicactions.capture.replay;

import io.github.jspinak.brobot.actions.methods.basicactions.capture.WriteXmlDomScenes;
import org.springframework.stereotype.Component;

@Component
public class FindInScenes {

    private WriteXmlDomScenes writeXmlDomScenes;

    public FindInScenes(WriteXmlDomScenes writeXmlDomScenes) {
        this.writeXmlDomScenes = writeXmlDomScenes;
    }
    public void find() {
        writeXmlDomScenes.initDocument();

    }
}
