package io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This image may be a transition from one screen to another.
 * In an environment with static states, there may be many images that transition to a given screenshot.
 * The same image might exist in different screens and transition to different screens.
 */
@Getter
@Setter
public class StatelessImage {

    private int indexInRepo; // index in the StatelessImageRepo
    /*
    The image should be clicked on every new screen, since it can lead to different target screens on different originating
    screens. TODO: make it a Map.
     */
    private boolean checked; // when checked, if fromScreenToScreen is empty then it doesn't transition anywhere
    private Map<Integer, Integer> fromScreenToScreen = new HashMap<>(); // transitions from and to the screenshots with these ids
    private Set<Scene> scenesFound = new HashSet<>();
    private List<Match> matchList = new ArrayList<>();
    private int ownerState; // the owner state. there is only one owner state.
    private Set<Integer> transitionsTo = new HashSet<>(); // all states transitioned to
    private String text; // search again for text after regions have merged

    public StatelessImage(Match match, Scene scene) {
        this.matchList.add(match);
        this.scenesFound.add(scene);
    }

    public String getName() {
        if (matchList.isEmpty()) return "";
        return matchList.get(0).getName();
    }

    public Region getFirstRegion() {
        return matchList.get(0).getRegion();
    }

    public List<Region> getRegions() {
        return matchList.stream()
                .map(Match::getRegion)
                .collect(Collectors.toList());
    }

    /**
     * Uses the first Match
     * @return Pattern from the first Match
     */
    public Pattern toPattern() {
        return new Pattern(matchList.get(0));
    }

    /**
     * Uses the first Match.
     * @return the StateImage from the first Match.
     */
    public StateImage toStateImage() {
        return matchList.get(0).toStateImage();
    }

    public void setOwnerState(int ownerState) {
        this.ownerState = ownerState;
        matchList.forEach(match -> match.getStateObjectData().setOwnerStateName(String.valueOf(ownerState)));
    }

}
