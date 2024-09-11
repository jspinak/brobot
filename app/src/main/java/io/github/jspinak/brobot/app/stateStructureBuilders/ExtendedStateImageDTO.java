package io.github.jspinak.brobot.app.stateStructureBuilders;

import io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.attributes.ImageAttributes;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ExtendedStateImageDTO {
    private StateImage stateImage;

    /**
     * The following variables are primarily used when creating state structures with names.
     */
    /*
    Attributes learned from the image filename and initial screenshots of the environment.
    These values determine MatchHistories, SearchRegions, and other variables when
    building the initial State structure (States and Transitions) from image filenames and screenshots.
    */
    private ImageAttributes attributes = new ImageAttributes();

    /**
     * The following variables are primarily used when creating state structures without names.
     */
    /*
    Captures the screen to which the image transitions with a click. Used when building the state structure live.
    When the state structure is built with screenshots, there are no transitions, and transitions need to be determined
    after the basic state structure is in place. Once there is a basic state structure, observations after clicking
    new StateImage objects can identify current states (or no states) and do not rely on screens anymore.
     */
    private Integer transitionsToScreen;
    private Set<String> statesToEnter = new HashSet<>();
    private Set<String> statesToExit = new HashSet<>();
    /*
    This stores the ids of transitions to other states for which this StateImage is involved.
    Transition ids are first recorded when transitions are saved to the database.
     */
    private Set<Long> involvedTransitionIds = new HashSet<>();

    public ExtendedStateImageDTO(StateImage stateImage) {
        this.stateImage = stateImage;
    }

}
