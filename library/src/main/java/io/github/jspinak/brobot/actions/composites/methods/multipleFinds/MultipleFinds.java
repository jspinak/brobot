package io.github.jspinak.brobot.actions.composites.methods.multipleFinds;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

@Component
public class MultipleFinds implements ActionInterface {

    private final NestedFinds nestedFinds;
    private final ConfirmedFinds confirmedFinds;

    public MultipleFinds(NestedFinds nestedFinds, ConfirmedFinds confirmedFinds) {
        this.nestedFinds = nestedFinds;
        this.confirmedFinds = confirmedFinds;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        if (actionOptions.isKeepLargerMatches()) {
            confirmedFinds.perform(matches, objectCollections);
            return;
        }
        nestedFinds.perform(matches, objectCollections);
    }
}
