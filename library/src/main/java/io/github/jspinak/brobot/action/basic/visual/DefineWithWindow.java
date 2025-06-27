package io.github.jspinak.brobot.action.basic.visual;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.app.ApplicationWindowProvider;
import io.github.jspinak.brobot.action.internal.capture.RegionDefinitionHelper;
import io.github.jspinak.brobot.model.element.Region;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Defines a region based on the boundaries of the currently focused window.
 * 
 * <p>This class provides window-based region definition functionality, which is particularly
 * useful in multi-window applications or when automating specific application windows.
 * It captures the bounds of the active window and creates a region that can be adjusted
 * based on ActionOptions parameters.</p>
 * 
 * <p>Common use cases include:
 * <ul>
 *   <li>Limiting automation scope to a specific application window</li>
 *   <li>Creating regions relative to window boundaries</li>
 *   <li>Ensuring actions stay within application bounds</li>
 * </ul>
 * </p>
 * 
 * <p>If no focused window is found, the operation fails and sets the success flag
 * to false in the ActionResult.</p>
 * 
 * @see DefineRegion
 * @see RegionDefinitionHelper
 * @see ApplicationWindowProvider#focusedWindow()
 */
@Component
public class DefineWithWindow implements ActionInterface {

    private final ApplicationWindowProvider app;
    private final RegionDefinitionHelper defineHelper;

    public DefineWithWindow(ApplicationWindowProvider app, RegionDefinitionHelper defineHelper) {
        this.app = app;
        this.defineHelper = defineHelper;
    }

    /**
     * Defines a region using the boundaries of the currently focused window.
     * 
     * <p>This method retrieves the focused window bounds and creates a region from it.
     * The region is then adjusted according to any position or dimension modifications
     * specified in the ActionOptions (addX, addY, addW, addH, absoluteW, absoluteH).</p>
     * 
     * <p>The method follows these steps:
     * <ol>
     *   <li>Attempts to get the focused window bounds via {@link ApplicationWindowProvider#focusedWindow()}</li>
     *   <li>If successful, creates a region from the window bounds</li>
     *   <li>Applies adjustments based on ActionOptions</li>
     *   <li>Adds the defined region to the ActionResult</li>
     *   <li>If no focused window is found, sets success to false</li>
     * </ol>
     * </p>
     * 
     * @param matches The ActionResult containing the ActionOptions for adjustments.
     *                If a window is found, the defined region is added to this object.
     *                If no window is found, the success flag is set to false.
     * @param objectCollections Not used by this implementation as window bounds are
     *                          determined by the system, not by object matching.
     */
    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        Optional<Region> focusedWindow = app.focusedWindow();
        if (focusedWindow.isPresent()) {
            Region region = focusedWindow.get();
            defineHelper.adjust(region, actionOptions);
            matches.addDefinedRegion(region);
        } else matches.setSuccess(false);
    }
}
