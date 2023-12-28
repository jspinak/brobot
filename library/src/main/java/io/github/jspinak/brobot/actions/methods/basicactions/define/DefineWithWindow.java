package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.App;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Defines a Region using the active Window as a reference.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class DefineWithWindow implements ActionInterface {

    private final App app;
    private final DefineHelper defineHelper;

    public DefineWithWindow(App app, DefineHelper defineHelper) {
        this.app = app;
        this.defineHelper = defineHelper;
    }

    // define with focused window
    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        Optional<Region> focusedWindow = app.focusedWindow();
        if (focusedWindow.isPresent()) {
            Region region = focusedWindow.get();
            defineHelper.adjust(region, actionOptions);
            matches.addDefinedRegion(region);
        } else matches.setSuccess(false);
    }
}
