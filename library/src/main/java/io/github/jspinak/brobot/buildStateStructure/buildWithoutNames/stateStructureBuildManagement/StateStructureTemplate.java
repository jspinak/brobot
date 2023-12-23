package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class StateStructureTemplate {

    /*
    When not empty, will be used instead of scraping.
    Stored in the directory BrobotSettings.screenshotPath.
     */
    private List<String> screenshots = new ArrayList<>(); //
    private Pattern topLeftBoundary;
    private Pattern bottomRightBoundary;
    private int minWidthBetweenImages;
    private boolean saveScreensWithMotionAndImages;
    private boolean saveScreenshots;
    private boolean saveMatchingImages;
    private boolean saveDecisionMats;
    private boolean saveStateIllustrations;
    private int minimumChangedPixelsForNewScreen = 20000;

    public static class Builder {
        private final List<String> screenshots = new ArrayList<>();
        private Pattern topLeftBoundary;
        private Pattern bottomRightBoundary;
        private int minWidthBetweenImages;
        private boolean saveScreensWithMotionAndImages;
        private boolean saveScreenshots;
        private boolean saveMatchingImages;
        private boolean saveDecisionMats;
        private boolean saveStateIllustrations;
        private int minimumChangedPixelsForNewScreen = 20000;

        public Builder addScreenshots(String... names) {
            this.screenshots.addAll(Arrays.asList(names));
            return this;
        }

        public Builder setBoundaryImages(String topLeft, String bottomRight) {
            this.topLeftBoundary = new Pattern.Builder()
                    .setFilename(topLeft)
                    .addAnchor(Position.Name.TOPLEFT, Position.Name.BOTTOMLEFT)
                    .build();
            this.bottomRightBoundary = new Pattern.Builder()
                    .setFilename(bottomRight)
                    .addAnchor(Position.Name.BOTTOMRIGHT, Position.Name.TOPRIGHT)
                    .build();
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
