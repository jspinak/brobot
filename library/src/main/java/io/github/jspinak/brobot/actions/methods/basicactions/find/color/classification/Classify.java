package io.github.jspinak.brobot.actions.methods.basicactions.find.color.classification;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Classify implements ActionInterface {

    private final FindColor findColor;

    public Classify(FindColor findColor) {
        this.findColor = findColor;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        findColor.find(matches, List.of(objectCollections));
    }
}
