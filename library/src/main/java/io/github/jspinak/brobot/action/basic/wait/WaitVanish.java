package io.github.jspinak.brobot.action.basic.wait;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

import org.springframework.stereotype.Component;

/**
 * Waits for visual elements to disappear from the screen in the Brobot model-based GUI automation framework.
 * 
 * <p>WaitVanish is a temporal action in the Action Model (α) that monitors the GUI for the 
 * absence of specified elements. It represents a critical synchronization primitive that enables 
 * automation scripts to wait for state transitions, loading completions, or dialog dismissals 
 * before proceeding with subsequent actions.</p>
 * 
 * <p>Success criteria:</p>
 * <ul>
 *   <li><b>Success</b>: Returns when none of the monitored objects are found on screen</li>
 *   <li><b>Failure</b>: Returns if at least one object persists throughout the entire wait period</li>
 *   <li><b>Match History</b>: The returned Matches object contains the last successful find results 
 *       before vanishing occurred</li>
 * </ul>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li><b>Continuous Monitoring</b>: Repeatedly searches for objects until they disappear</li>
 *   <li><b>Timeout Management</b>: Respects action lifecycle constraints and maximum wait times</li>
 *   <li><b>Single Collection Focus</b>: Processes only the first ObjectCollection for clarity</li>
 *   <li><b>State Synchronization</b>: Essential for detecting completed state transitions</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Waiting for loading screens or splash screens to disappear</li>
 *   <li>Ensuring dialogs or pop-ups have been dismissed</li>
 *   <li>Confirming completion of animations or transitions</li>
 *   <li>Verifying that temporary notifications have vanished</li>
 *   <li>Synchronizing with asynchronous operations</li>
 * </ul>
 * 
 * <p>In the model-based approach, WaitVanish actions are crucial for handling the dynamic 
 * nature of modern GUIs. They enable the framework to adapt to varying response times and 
 * ensure that actions are performed only when the GUI has reached the expected state. This 
 * temporal awareness is fundamental to creating robust automation that can handle the 
 * inherent variability in GUI behavior.</p>
 * 
 * <p>Implementation note: Uses Find.EACH mode to check for individual object presence, 
 * allowing for partial vanishing detection when monitoring multiple objects.</p>
 * 
 * @since 1.0
 * @see Find
 * @see VanishOptions
 * @see ActionLifecycleManagement
 * @see ObjectCollection
 */
@Component
public class WaitVanish implements ActionInterface {

    private final Find find;
    private final ActionLifecycleManagement actionLifecycleManagement;

    public WaitVanish(Find find, ActionLifecycleManagement actionLifecycleManagement) {
        this.find = find;
        this.actionLifecycleManagement = actionLifecycleManagement;
    }

    @Override
    public Type getActionType() {
        return Type.VANISH;
    }

    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration - VanishOptions extends BaseFindOptions
        ActionConfig config = matches.getActionConfig();
        double timeout = 10.0; // default timeout
        
        if (config instanceof VanishOptions) {
            VanishOptions vanishOptions = (VanishOptions) config;
            timeout = vanishOptions.getTimeout();
        }
        
        // Process only the first ObjectCollection
        if (objectCollections.length > 0) {
            ObjectCollection firstCollection = objectCollections[0];
            
            // Keep checking until objects vanish or timeout is reached
            long startTime = System.currentTimeMillis();
            while ((System.currentTimeMillis() - startTime) / 1000.0 < timeout &&
                   actionLifecycleManagement.isOkToContinueAction(matches, firstCollection.getStateImages().size())) {
                find.perform(matches, firstCollection);
                
                // If nothing was found, the objects have vanished - success!
                if (matches.getMatchLocations().isEmpty()) {
                    matches.setSuccess(true);
                    break;
                }
            }
        }
    }

}