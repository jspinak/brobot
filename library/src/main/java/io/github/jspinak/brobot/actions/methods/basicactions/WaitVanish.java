package io.github.jspinak.brobot.actions.methods.basicactions;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Waits for visual elements to disappear from the screen in the Brobot model-based GUI automation framework.
 * 
 * <p>WaitVanish is a temporal action in the Action Model (α) that monitors the GUI for the 
 * absence of specified elements. It represents a critical synchronization primitive that enables 
 * automation scripts to wait for state transitions, loading completions, or dialog dismissals 
 * before proceeding with subsequent actions.</p>
 * 
 * <p>Success criteria:
 * <ul>
 *   <li><b>Success</b>: Returns when none of the monitored objects are found on screen</li>
 *   <li><b>Failure</b>: Returns if at least one object persists throughout the entire wait period</li>
 *   <li><b>Match History</b>: The returned Matches object contains the last successful find results 
 *       before vanishing occurred</li>
 * </ul>
 * </p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Continuous Monitoring</b>: Repeatedly searches for objects until they disappear</li>
 *   <li><b>Timeout Management</b>: Respects action lifecycle constraints and maximum wait times</li>
 *   <li><b>Single Collection Focus</b>: Processes only the first ObjectCollection for clarity</li>
 *   <li><b>State Synchronization</b>: Essential for detecting completed state transitions</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Waiting for loading screens or splash screens to disappear</li>
 *   <li>Ensuring dialogs or pop-ups have been dismissed</li>
 *   <li>Confirming completion of animations or transitions</li>
 *   <li>Verifying that temporary notifications have vanished</li>
 *   <li>Synchronizing with asynchronous operations</li>
 * </ul>
 * </p>
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
 * @see ActionOptions
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

    public void perform(Matches matches, ObjectCollection[] objectCollections) {
        matches.getActionOptions().setFind(ActionOptions.Find.EACH);
        while (actionLifecycleManagement.isOkToContinueAction(matches, objectCollections[0].getStateImages().size())) {
            find.perform(matches, objectCollections[0]);
        }
    }

}