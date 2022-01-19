package actions.methods.basicactions.define;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.sikuliWrappers.App;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.primitives.region.Region;
import com.brobot.multimodule.database.state.ObjectCollection;
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
