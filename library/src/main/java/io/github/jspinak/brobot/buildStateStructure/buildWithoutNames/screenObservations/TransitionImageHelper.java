package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import lombok.Getter;
import lombok.Setter;

/**
 * Helper class for building TransitionImage objects
 */
@Getter
@Setter
public class TransitionImageHelper {

    private int firstIndex;
    private int lastIndex;
    private TransitionImage transitionImage;

    public TransitionImageHelper(int firstIndex, Match firstImage) {
        this.firstIndex = firstIndex;
        this.lastIndex = firstIndex;
        this.transitionImage = new TransitionImage();
        this.transitionImage.addWordMatch(firstImage);
    }

    public void addPotentialLink(int index, Match image) {
        this.lastIndex = index;
        this.transitionImage.addWordMatch(image);
    }
}
