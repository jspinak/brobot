package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CreateStatesFromMatches {

    /**
     * Creates states by converting Match objects to StateImages.
     * Does not add the states to the StateStructure.
     * @param matches contains the match objects to use
     * @return all new states created with the match objects
     */
    public List<State> create(Matches matches) {
        List<State> states = new ArrayList<>();
        Set<String> uniqueStates = matches.getOwnerStateNames();
        uniqueStates.forEach(ownerStateName -> {
            List<Match> stateMatchList = matches.getMatchObjectsWithOwnerState(ownerStateName);
            states.add(create(stateMatchList));
        });
        return states;
    }

    /**
     * Creates a State using all Match objects in the parameter list.
     * Use this method only with the Match objects that belong to a single state.
     * @param matchList the match objects belonging to a single state
     * @return a newly created state
     */
    private State create(List<Match> matchList) {
        if (matchList.isEmpty()) return new State();
        String name = matchList.get(0).getOwnerStateName();
        List<StateImage> stateImages = new ArrayList<>();
        matchList.forEach(match -> stateImages.add(match.toStateImage()));
        return new State.Builder(name)
                .withImages(stateImages)
                .build();
    }
}
