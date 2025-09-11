package io.github.jspinak.brobot.action.internal.utility;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.StateEnum;

import lombok.Setter;

/**
 * Determines the text description of an action given the action itself as well as the active states
 * before and after action execution. Text descriptions can be used to build a text-to-action neural
 * net, or in output or reports to give a friendlier way to review the functions of an automation
 * application.
 */
@Component
@Setter
public class ActionTextUtilities {

    /**
     * Set of states active before the action execution. Used to determine state transitions caused
     * by the action.
     */
    private Set<StateEnum> beforeStates = new HashSet<>();

    /**
     * Set of states active after the action execution. Compared with beforeStates to identify
     * opened/closed states.
     */
    private Set<StateEnum> afterStates = new HashSet<>();

    /**
     * If the 'before' and 'after' states are not all the same, the action is said to 'close' and/or
     * 'open' specific states. The description should not include exact values. For example, it
     * shouldn't say 'move the window 200 pixels to the right", but "move the window to the right".
     *
     * <p>Example 1: "close the open window". This would click on the X symbol at the top right of
     * the window. In order for the text to be generated automatically, there needs to be a text
     * description of the state image that says that clicking on it closes a window.
     *
     * <p>Describing the action introduces some complexities when building the model of the
     * environment. Due to these complexities, automating text descriptions of actions is left as a
     * TODO. The current solution, which is to add a text description manually when performing an
     * action, will be used first.
     *
     * @return the text description of the action performed
     */
    public String getActionText() {
        return "";
    }
}
