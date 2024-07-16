package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This image may be a transition from one screen to another.
 * In an environment with static states, there may be many images that transition to a given screenshot.
 * The same image might exist in different screens and transition to different screens.
 */
@Getter
@Setter
public class StatelessImage {

    private int indexInRepo; // index in the TransitionImageRepo
    /*
    The image should be clicked on every new screen, since it can lead to different target screens on different originating
    screens. TODO: make it a Map.
     */
    private boolean checked; // when checked, if fromScreenToScreen is empty then it doesn't transition anywhere
    private Map<Integer, Integer> fromScreenToScreen = new HashMap<>(); // transitions from and to the screenshots with these ids
    private Set<Integer> screensFound = new HashSet<>();
    private Match match; // the match found when the image was created
    private int ownerState; // the owner state. there is only one owner state.
    private Set<Integer> transitionsTo = new HashSet<>(); // all states transitioned to
    private String text; // search again for text after regions have merged

    public StatelessImage(Match match, int screenshotIndex) {
        this.match = match;
        this.screensFound.add(screenshotIndex);
    }

    public String getName() {
        return match.getName();
    }

    public Region getRegion() {
        return match.getRegion();
    }

    public Pattern toPattern() {
        return new Pattern(match);
    }

    public StateImage toStateImage() {
        return match.toStateImage();
    }

    public void addScreenFound(int screenId) {
        screensFound.add(screenId);
    }

    public void setOwnerState(int ownerState) {
        this.ownerState = ownerState;
        match.getStateObjectData().setOwnerStateName(String.valueOf(ownerState));
    }

}
