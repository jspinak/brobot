package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.TransitionImage;
import lombok.Getter;

import java.util.*;

@Getter
public class ImageSetsAndAssociatedScreens {

    private List<Integer> images = new ArrayList<>(); // the int corresponds to the position in the TransitionImageRepo
    private Set<Integer> screens = new HashSet<>(); // the screen id. screens are fixed after creation.

    public ImageSetsAndAssociatedScreens(int indexInRepo, Set<Integer> screens) {
        this.images.add(indexInRepo);
        this.screens = screens;
    }

    public void addImage(int indexInRepo) {
        this.images.add(indexInRepo);
    }

    public boolean ifSameScreensAddImage(TransitionImage transitionImage) {
        if (hasSameScreens(transitionImage.getScreensFound())) {
            addImage(transitionImage.getIndexInRepo());
            return true;
        }
        return false;
    }

    private boolean hasSameScreens(Set<Integer> screens) {
        // Check if both lists are not null and have the same size
        if (screens == null || this.screens.size() != screens.size()) {
            return false;
        }
        // Compare elements one by one
        for (int screen : screens) {
            if (!this.screens.contains(screen)) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Images: ");
        images.forEach(img -> stringBuilder.append(img).append(","));
        stringBuilder.append(" ").append("Screens: [");
        for (int i=0; i<screens.size()-1; i++) {
            stringBuilder.append(screens.toArray()[i]).append(",");
        }
        stringBuilder.append(screens.toArray()[screens.size()-1]).append("]");
        return stringBuilder.toString();
    }

}
