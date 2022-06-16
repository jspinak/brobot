package io.github.jspinak.brobot.actions.composites.methods.multipleFinds;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

@Component
public class MultipleFinds implements ActionInterface {

    private NestedFinds nestedFinds;
    private ConfirmedFinds confirmedFinds;

    public MultipleFinds(NestedFinds nestedFinds, ConfirmedFinds confirmedFinds) {
        this.nestedFinds = nestedFinds;
        this.confirmedFinds = confirmedFinds;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (actionOptions.isKeepLargerMatches())
            return confirmedFinds.perform(actionOptions, objectCollections);
        return nestedFinds.perform(actionOptions, objectCollections);
    }
}
