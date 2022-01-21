package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.App;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Defines a Region using the active Window as a reference.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class DefineWithWindow implements ActionInterface {

    private App app;
    private DefineHelper defineHelper;

    public DefineWithWindow(App app, DefineHelper defineHelper) {
        this.app = app;
        this.defineHelper = defineHelper;
    }

    // define with focused window
    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = new Matches();
        Optional<Region> focusedWindow = app.focusedWindow();
        if (focusedWindow.isPresent()) {
            Region region = focusedWindow.get();
            defineHelper.adjust(region, actionOptions);
            matches.addDefinedRegion(region);
        } else matches.setSuccess(false);
        return matches;
    }
}