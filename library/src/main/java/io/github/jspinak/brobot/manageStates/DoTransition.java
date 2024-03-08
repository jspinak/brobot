package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.reports.Output;
import io.github.jspinak.brobot.reports.Report;
import io.github.jspinak.brobot.services.StateTransitionsService;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Completes a whole Transition (FromTransition and ToTransition),
 * including all States to activate and exit. It's possible to
 * have many ToTransitions, as States to activate require ToTransitions,
 * which themselves can include other States to activate. There will
 * only be 1 FromTransition.
 */
@Component
public class DoTransition {

    private final StateTransitionsService stateTransitionsService;
    private final StateTransitionsJointTable stateTransitionsJointTable;
    private final SetHiddenStates setHiddenStates;
    private final StateMemory stateMemory;
    private final AllStatesInProjectService allStatesInProjectService;
    private final TransitionFetcher transitionFetcher;

    public DoTransition(StateTransitionsService stateTransitionsService, StateTransitionsJointTable stateTransitionsJointTable,
                        SetHiddenStates setHiddenStates, StateMemory stateMemory, AllStatesInProjectService allStatesInProjectService,
                        TransitionFetcher transitionFetcher) {
        this.stateTransitionsService = stateTransitionsService;
        this.stateTransitionsJointTable = stateTransitionsJointTable;
        this.setHiddenStates = setHiddenStates;
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
        this.transitionFetcher = transitionFetcher;
    }

    // previous State should be set with the Transition
    // toStateName will never be PREVIOUS here: FindPath needs to know which state PREVIOUS stands for when
    // it makes the path. in order for the transition to occur, the named previous state must be
    // given as PREVIOUS to the transitionFrom method.
    public boolean go(String fromStateName, String toStateName) {
        if (doTransitions(fromStateName, toStateName)) {
            Report.format(Output.check+" Transition %s->%s successful. \n", fromStateName, toStateName);
            return true;
        }
        Report.format(Output.fail+" Transition %s->%s not successful. \n", fromStateName, toStateName);
        return false;
    }

    /**
     * A FromTransition is the first half of a transition from StateA to StateB (ToTransition is the 2nd half).
     * It takes care of everything that needs to happen in StateA. This includes any Actions that need to occur
     * while StateA is active, and may also include confirmation of the transition (for example,
     * if StateA vanishes after the actions are performed). NOTE: THIS CAN BE IMPLEMENTED AUTOMATICALLY,
     * ANY STATE THAT SHOULD VANISH CAN BE CHECKED WITH A VANISH ACTION, BUT THERE SHOULD BE AN OPTION TO
     * DISABLE THE CHECKING SINCE IT COULD SLOW DOWN EXECUTION
     * The FromTransition consists of:
     *   1. Actions to take to make StateB appear
     *   2. A set of States that should appear when (1) is successful (includes StateB)
     *   3. A set of States that should disappear when (1) is successful (may include StateA)
     * The ToTransition to StateA is independent of any FromTransition and does not know which State Transition
     * called it. States that appear or vanish when StateA becomes active will always appear or vanish
     * when StateA becomes active, regardless of how it was made active. The ToTransition consists of:
     *   1. Actions to recognize that StateA is active. If StateA is already in StateMemory as an
     *      active state, these actions are not executed.
     *   2. A set of States that should appear when (1) is successful (does not include StateA)
     *   3. A set of States that should disappear when (1) is successful
     * Once (1) is successful (for the FromTransition, this can be determined by a successful Vanish
     * operation on disappearing States), INITIALIZE THE FromTransition IN THE StateTransitions OBJECT,
     * SINCE THERE WE KNOW IF A StateTransition is a From or a To Transition.
     *   1. Execute ToTransitions for each of the States to appear. The success of the overall transition
     *      (FromTransition StateA + ToTransition StateB) is determined by the success of the FromTransition and the success
     *      of the ToTransition for the target State B. All other ToTransitions will be performed but their success
     *      or failure will not affect the success or failure of the overall transition being called.
     *   2. States that vanish will be checked with a Vanish operation if they are in StateMemory as an
     *      active State. This is an option that can be
     *      disabled for faster execution. If disabled, the States to vanish will be simply removed from
     *      StateMemory as active States.
     *   2. All States to activate will have ToTransitions, which also may contain States to appear and
     *      vanish. States that are already in StateMemory as active States will not attempt to recognize the
     *      State and assume that it is there. This avoids the potential for an infinite loop of State
     *      recognition in the case that StateA includes StateB as a State to activate and StateB includes
     *      StateA as a State to activate. The same applies to States that should exit after a successful
     *      transition.
     *
     * When to include States to activate and exit in the FromTransition and when to include them in the
     * ToTransition:
     * Scenario #1 with States A,B,C,D,E
     * A Transition from StateA causes StateC and StateD to appear.
     * A Transition from StateB causes StateC and StateD to appear.
     * A Transition from StateE causes StateD to appear.
     * Transitions
     * StateD always appears when StateC appears. Therefore, the ToTransition for StateC should include
     *   StateD in the States to activate. Of course, if this conceptually does not make sense
     *   (for example, currently StateD always appears when StateC appears but you can imagine
     *   scenarios when StateC would appear by itself without StateD), then this is not the correct solution.
     * The FromTransition from StateA -> StateC does not have additional States to activate
     *   (StateD is activated from StateC's ToTransition)
     * The FromTransition from StateA -> StateD also activates StateC
     * The FromTransitions from StateB are configured in the same way as those for StateA
     * FromTransition for StateE -> StateD has no other activations.
     *
     * @param from an active State
     * @param to a target State
     * @return true when both the FromTransition and the ToTransition for the target State are successful.
     */
    private boolean doTransitions(String from, String to) {
        if (!stateMemory.getActiveStates().contains(from)) return false; // the 'from' State is not active
        Optional<TransitionFetcher> transitionsOpt = transitionFetcher.getTransitions(from, to);
        if (transitionsOpt.isEmpty()) return false; // couldn't find one of the needed Transitions
        TransitionFetcher transitions = transitionsOpt.get();
        if (!transitions.getFromTransitionFunction().getAsBoolean()) return false; // the FromTransition didn't succeed
        Set<String> statesToActivate = getStatesToActivate(transitions, to);
        statesToActivate.forEach(stateName ->
                allStatesInProjectService.getState(stateName).ifPresent(State::setProbabilityToBaseProbability));
        StateTransition fromTrsn = transitions.getFromTransition();
        statesToActivate.forEach(this::doTransitionTo); // do all ToTransitions
        fromTrsn.getExit().forEach(this::exitState); // exit all States to exit
        if (!stateMemory.getActiveStates().contains(to)) return false;
        if (!transitions.getFromTransitions().stateStaysVisible(to)) exitState(from); // exit 'from' State
        return true;
    }

    private Set<String> getStatesToActivate(TransitionFetcher transitions, String to) {
        // initialize the States to activate with the States to activate in the FromTransition
        Set<String> statesToActivate = new HashSet<>(transitions.getFromTransition().getActivate());
        // if the 'from' State exits, add its hidden States to the States to activate
        if (!transitions.getFromTransitions().stateStaysVisible(to))
            statesToActivate.addAll(transitions.getFromState().getHidden());
        statesToActivate.remove(StateMemory.Enum.PREVIOUS.toString()); // previous can't be activated, so get rid of it
        System.out.println("states to activate: "+statesToActivate);
        return statesToActivate;
    }

    private boolean doTransitionTo(String toStateName) {
        if (stateMemory.getActiveStates().contains(toStateName)) return true; // State is already active
        Optional<State> toStateOpt = allStatesInProjectService.getState(toStateName);
        if (toStateOpt.isEmpty()) return false; // State doesn't exist
        State toState = toStateOpt.get();
        toState.setProbabilityToBaseProbability();
        Optional<StateTransitions> stateTransitions = stateTransitionsService.getTransitions(toStateName);
        if (stateTransitions.isEmpty()) return false; // transition doesn't exist
        StateTransition transition = stateTransitions.get().getTransitionFinish();
        if (!transition.getAsBoolean()) { // transition failed
            toState.setProbabilityExists(0); // mock assumes State is not present
            return false;
        }
        toState.setProbabilityExists(100); // State found
        setHiddenStates.set(toStateName);
        stateTransitionsJointTable.addTransitionsToHiddenStates(toState);
        transition.getActivate().forEach(this::doTransitionTo);
        transition.getExit().forEach(this::exitState);
        return stateMemory.getActiveStates().contains(toStateName);
    }

    // THIS SHOULD INCLUDE A CHECK WITH THE VANISH OPERATION
    private boolean exitState(String stateToExit) {
        Optional<State> stateOpt = allStatesInProjectService.getState(stateToExit);
        if (stateOpt.isEmpty()) return false; // state doesn't exist
        stateTransitionsJointTable.removeTransitionsToHiddenStates(stateOpt.get());
        stateMemory.removeInactiveState(stateToExit);
        stateOpt.get().resetHidden();
        return true;
    }

}
