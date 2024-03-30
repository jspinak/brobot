package io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StateStructureTemplate {

    /*
    When not empty, will be used instead of scraping.
    Stored in the directory BrobotSettings.screenshotPath.
     */
    private List<Pattern> screenshots = new ArrayList<>(); //
    private Pattern topLeftBoundary;
    private Pattern bottomRightBoundary;
    private int minWidthBetweenImages;
    private boolean saveScreensWithMotionAndImages;
    private boolean saveScreenshots;
    private boolean saveMatchingImages;
    private boolean saveDecisionMats;
    private boolean saveStateIllustrations;
    private int minimumChangedPixelsForNewScreen = 20000;

    public boolean isLive() {
        return screenshots.isEmpty();
    }

    public List<StateImage> getAllScreenshotsExcept_asStateImages(Pattern excludePattern) {
        List<StateImage> stateImages = new ArrayList<>();
        for (Pattern pattern : screenshots) {
            if (excludePattern != pattern) stateImages.add(pattern.inNullState());
        }
        return stateImages;
    }

    public static class Builder {
        private final List<Pattern> screenshots = new ArrayList<>();
        private Pattern topLeftBoundary;
        private Pattern bottomRightBoundary;
        private int minWidthBetweenImages;
        private boolean saveScreensWithMotionAndImages;
        private boolean saveScreenshots;
        private boolean saveMatchingImages;
        private boolean saveDecisionMats;
        private boolean saveStateIllustrations;
        private int minimumChangedPixelsForNewScreen = 20000;

        public Builder addImagesInScreenshotsFolder(String... names) {
            for (String name : names) {
                Pattern pattern = new Pattern.Builder()
                        .setFilename("../"+ BrobotSettings.screenshotPath + name)
                        .build();
                this.screenshots.add(pattern);
            }
            return this;
        }

        public Builder setBoundaryImages(String topLeft, String bottomRight) {
            this.topLeftBoundary = new Pattern.Builder()
                    .setFilename(topLeft)
                    .addAnchor(Positions.Name.TOPLEFT, Positions.Name.BOTTOMLEFT)
                    .build();
            this.bottomRightBoundary = new Pattern.Builder()
                    .setFilename(bottomRight)
                    .addAnchor(Positions.Name.BOTTOMRIGHT, Positions.Name.TOPRIGHT)
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
