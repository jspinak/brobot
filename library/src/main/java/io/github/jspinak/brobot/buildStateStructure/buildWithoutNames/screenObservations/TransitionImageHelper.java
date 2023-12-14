package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import lombok.Getter;
import lombok.Setter;
import org.sikuli.script.Match;

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
