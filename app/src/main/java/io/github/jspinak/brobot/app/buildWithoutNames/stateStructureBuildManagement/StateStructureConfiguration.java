package io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StateStructureConfiguration {

    /*
    When not empty, will be used instead of scraping.
    Stored in the directory BrobotSettings.screenshotPath.
     */
    private List<Pattern> screenshots = new ArrayList<>(); //
    private Pattern topLeftBoundary;
    private Pattern bottomRightBoundary;
    private Region usableArea = new Region();
    private int minWidthBetweenImages;
    private int minImageArea;
    private double maxSimilarityForUniqueImage;
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
        private Region usableArea = new Region();
        private int minWidthBetweenImages;
        private int minImageArea = 25;
        private double maxSimilarityForUniqueImage = .98;
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

        public Builder setUsableArea(Region usableArea) {
            this.usableArea = usableArea;
            return this;
        }

        public Builder setMinWidthBetweenImages(int minWidthBetweenImages) {
            this.minWidthBetweenImages = minWidthBetweenImages;
            return this;
        }

        public Builder setMinImageArea(int minArea) {
            this.minImageArea = minArea;
            return this;
        }

        public Builder setMaxSimilarityForUniqueImage(double maxSimilarityForUniqueImage) {
            this.maxSimilarityForUniqueImage = maxSimilarityForUniqueImage;
            return this;
        }

        public Builder setMinimumChangedPixelsForNewScreen(int minimumChangedPixelsForNewScreen) {
            this.minimumChangedPixelsForNewScreen = minimumChangedPixelsForNewScreen;
            return this;
        }

        public StateStructureConfiguration build() {
            StateStructureConfiguration stateStructureConfiguration = new StateStructureConfiguration();
            stateStructureConfiguration.screenshots = screenshots;
            stateStructureConfiguration.topLeftBoundary = topLeftBoundary;
            stateStructureConfiguration.bottomRightBoundary = bottomRightBoundary;
            stateStructureConfiguration.usableArea = usableArea;
            stateStructureConfiguration.minWidthBetweenImages = minWidthBetweenImages;
            stateStructureConfiguration.minImageArea = minImageArea;
            stateStructureConfiguration.maxSimilarityForUniqueImage = maxSimilarityForUniqueImage;
            stateStructureConfiguration.minimumChangedPixelsForNewScreen = minimumChangedPixelsForNewScreen;
            return stateStructureConfiguration;
        }
    }
}
