package io.github.jspinak.brobot.action.internal.find.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Creates State objects from Match results for dynamic state generation.
 *
 * <p>This component enables the creation of new states based on what was found during execution,
 * supporting dynamic state discovery and adaptation. Each match is converted to a StateImage and
 * grouped by owner state to form complete State objects. This is particularly useful for:
 *
 * <ul>
 *   <li>Learning new states during exploration
 *   <li>Building state structures from discovered UI elements
 *   <li>Creating temporary states for complex workflows
 * </ul>
 *
 * <p>Note: The created states are not automatically added to the StateStructure. This allows for
 * validation or modification before integration.
 *
 * @see State
 * @see Match
 * @see StateImage
 * @see ActionResult
 */
@Component
public class MatchToStateConverter {

    /**
     * Creates states by converting Match objects to StateImages.
     *
     * <p>This method groups matches by their owner state names and creates a new State for each
     * unique owner. Each match is converted to a StateImage that preserves the location and
     * appearance of the found element. The resulting states can be used to build or extend the
     * automation model dynamically.
     *
     * <p>The states are created but not added to the StateStructure, allowing for review or
     * modification before integration into the automation framework.
     *
     * @param matches The ActionResult containing match objects to convert. Must not be null.
     * @return A list of newly created State objects, one for each unique owner state found in the
     *     matches. Returns an empty list if no matches are present.
     */
    public List<State> create(ActionResult matches) {
        List<State> states = new ArrayList<>();
        Set<String> uniqueStates = matches.getOwnerStateNames();
        uniqueStates.forEach(
                ownerStateName -> {
                    List<Match> stateMatchList =
                            matches.getMatchObjectsWithOwnerState(ownerStateName);
                    states.add(create(stateMatchList));
                });
        return states;
    }

    /**
     * Creates a single State from a list of matches belonging to the same owner state.
     *
     * <p>This method converts each match into a StateImage and combines them into a cohesive State
     * object. The state name is derived from the owner state of the first match in the list. If the
     * list is empty, an empty State is returned.
     *
     * <p><b>Important:</b> This method assumes all matches in the list belong to the same owner
     * state. Mixing matches from different states will result in incorrect state creation.
     *
     * @param matchList The match objects belonging to a single state. All matches should have the
     *     same owner state name.
     * @return A newly created State containing StateImages derived from the matches, or an empty
     *     State if the match list is empty
     */
    private State create(List<Match> matchList) {
        if (matchList.isEmpty()) return new State();
        String name = matchList.get(0).getOwnerStateName();
        List<StateImage> stateImages = new ArrayList<>();
        matchList.forEach(match -> stateImages.add(match.toStateImage()));
        return new State.Builder(name).withImages(stateImages).build();
    }
}
