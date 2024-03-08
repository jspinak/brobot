package com.brobot.app.buildWithoutNames.buildStateStructure;

import com.brobot.app.buildWithoutNames.screenObservations.TransitionImage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class ImageSetsAndAssociatedScreens {

    private List<Integer> images = new ArrayList<>(); // the int corresponds to the position in the TransitionImageRepo
    private List<Integer> screens = new ArrayList<>(); // the screen id. screens are fixed after creation.

    public ImageSetsAndAssociatedScreens(int image, List<Integer> screens) {
        this.images.add(image);
        this.screens = screens;
    }

    public void addImage(int image) {
        this.images.add(image);
    }

    public boolean ifSameScreensAddImage(TransitionImage transitionImage, int pos) {
        if (hasSameScreens(transitionImage.getScreensFound())) {
            addImage(pos);
            return true;
        }
        return false;
    }

    private boolean hasSameScreens(List<Integer> screens) {
        // Check if both lists are not null and have the same size
        if (screens == null || this.screens.size() != screens.size()) {
            return false;
        }
        // Compare elements one by one
        for (int i = 0; i < this.screens.size(); i++) {
            if (!Objects.equals(this.screens.get(i), screens.get(i))) {
                return false;
            }
        }
        // All elements match
        return true;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Images: ");
        images.forEach(img -> stringBuilder.append(img).append(","));
        stringBuilder.append(" ").append("Screens: ");
        screens.forEach(scr -> stringBuilder.append(scr).append(","));
        return stringBuilder.toString();
    }

}
