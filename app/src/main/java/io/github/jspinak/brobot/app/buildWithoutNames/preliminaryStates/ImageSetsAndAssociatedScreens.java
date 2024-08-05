package io.github.jspinak.brobot.app.buildWithoutNames.preliminaryStates;

import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.StatelessImage;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
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

    private List<StatelessImage> images = new ArrayList<>();;

    public ImageSetsAndAssociatedScreens(StatelessImage image) {
        this.images.add(image);
    }

    public Set<Pattern> getScreens() {
        if (images.isEmpty()) return new HashSet<>();
        return images.get(0).getScreensFound();
    }

    public boolean ifSameScreensAddImage(StatelessImage statelessImage) {
        if (!hasSameScreens(statelessImage)) return false;
        images.add(statelessImage);
        return true;
    }

    private boolean hasSameScreens(StatelessImage statelessImage) {
        // Check if both lists are not null and have the same size
        if (statelessImage.getScreensFound() == null || getScreens().size() != statelessImage.getScreensFound().size()) {
            return false;
        }
        for (Pattern pattern : statelessImage.getScreensFound()) {
            if (!getScreens().contains(pattern)) return false;
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
        for (int i=0; i<getScreens().size()-1; i++) {
            stringBuilder.append(getScreens().toArray()[i].toString()).append(",");
        }
        stringBuilder.append(getScreens().toArray()[getScreens().size()-1].toString()).append("]");
        return stringBuilder.toString();
    }

}
