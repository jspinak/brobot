package io.github.jspinak.brobot.app.buildWithoutNames.preliminaryStates;

import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.StatelessImage;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * These images are all associated with the included screens. These images define a state and the state appears
 * in each of the associated screens.
 */
@Getter
public class ImageSetsAndAssociatedScreens {

    private final List<StatelessImage> images = new ArrayList<>();;

    public ImageSetsAndAssociatedScreens(StatelessImage image) {
        this.images.add(image);
    }

    public Set<Scene> getScenes() {
        if (images.isEmpty()) return new HashSet<>();
        return images.get(0).getScenesFound();
    }

    public boolean ifSameScreensAddImage(StatelessImage statelessImage) {
        if (!hasSameScenes(statelessImage)) return false;
        images.add(statelessImage);
        return true;
    }

    private boolean hasSameScenes(StatelessImage statelessImage) {
        // Check if both lists are not null and have the same size
        if (statelessImage.getScenesFound() == null || getScenes().size() != statelessImage.getScenesFound().size()) {
            return false;
        }
        for (Scene scene : statelessImage.getScenesFound()) {
            if (!getScenes().contains(scene)) return false;
        }
        return true;
    }

    public List<StateImage> getStateImages() {
        return images.stream()
                .map(StatelessImage::toStateImage)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Images: ");
        images.forEach(img -> stringBuilder.append(img).append(","));
        stringBuilder.append(" ").append("Screens: ");
        for (int i = 0; i< getScenes().size()-1; i++) {
            stringBuilder.append(getScenes().toArray()[i].toString()).append(",");
        }
        stringBuilder.append(getScenes().toArray()[getScenes().size()-1].toString()).append("]");
        return stringBuilder.toString();
    }

}
