package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StateStructureTemplate {

    private List<Image> screenshots = new ArrayList<>(); // when not empty, will be used instead of scraping
    private Image topLeftBoundary;
    private Image bottomRightBoundary;
    private int minWidthBetweenImages;
    private boolean saveScreensWithMotionAndImages;
    private boolean saveScreenshots;
    private boolean saveMatchingImages;
    private boolean saveDecisionMats;
    private boolean saveStateIllustrations;
    private int minimumChangedPixelsForNewScreen = 20000;

    public static class Builder {
        private final List<Image> screenshots = new ArrayList<>();
        private Image topLeftBoundary;
        private Image bottomRightBoundary;
        private int minWidthBetweenImages;
        private boolean saveScreensWithMotionAndImages;
        private boolean saveScreenshots;
        private boolean saveMatchingImages;
        private boolean saveDecisionMats;
        private boolean saveStateIllustrations;
        private int minimumChangedPixelsForNewScreen = 20000;

        public Builder addScreenshots(String... names) {
            for (String name : names) {
                Image image = new Image(name);
                this.screenshots.add(image);
            }
            return this;
        }

        public Builder setBoundaryImages(Image topLeft, Image bottomRight) {
            this.topLeftBoundary = topLeft;
            this.bottomRightBoundary = bottomRight;
            return this;
        }

        public Builder setMinWidthBetweenImages(int minWidthBetweenImages) {
            this.minWidthBetweenImages = minWidthBetweenImages;
            return this;
        }

        public Builder setSaveScreenWithMotionAndImages(boolean saveScreensWithMotionAndImages) {
            this.saveScreensWithMotionAndImages = saveScreensWithMotionAndImages;
            return this;
        }

        public Builder setSaveScreenshots(boolean saveScreenshots) {
            this.saveScreenshots = saveScreenshots;
            return this;
        }

        public Builder setSaveMatchingImages(boolean saveMatchingImages) {
            this.saveMatchingImages = saveMatchingImages;
            return this;
        }

        public Builder setSaveDecisionMats(boolean saveDecisionMats) {
            this.saveDecisionMats = saveDecisionMats;
            return this;
        }

        public Builder setSaveStateIllustrations(boolean saveStateIllustrations) {
            this.saveStateIllustrations = saveStateIllustrations;
            return this;
        }

        public Builder setMinimumChangedPixelsForNewScreen(int minimumChangedPixelsForNewScreen) {
            this.minimumChangedPixelsForNewScreen = minimumChangedPixelsForNewScreen;
            return this;
        }

        public StateStructureTemplate build() {
            StateStructureTemplate stateStructureTemplate = new StateStructureTemplate();
            stateStructureTemplate.screenshots = screenshots;
            stateStructureTemplate.topLeftBoundary = topLeftBoundary;
            stateStructureTemplate.bottomRightBoundary = bottomRightBoundary;
            stateStructureTemplate.minWidthBetweenImages = minWidthBetweenImages;
            stateStructureTemplate.saveScreensWithMotionAndImages = saveScreensWithMotionAndImages;
            stateStructureTemplate.saveScreenshots = saveScreenshots;
            stateStructureTemplate.saveMatchingImages = saveMatchingImages;
            stateStructureTemplate.saveDecisionMats = saveDecisionMats;
            stateStructureTemplate.saveStateIllustrations = saveStateIllustrations;
            stateStructureTemplate.minimumChangedPixelsForNewScreen = minimumChangedPixelsForNewScreen;
            return stateStructureTemplate;
        }
    }
}
