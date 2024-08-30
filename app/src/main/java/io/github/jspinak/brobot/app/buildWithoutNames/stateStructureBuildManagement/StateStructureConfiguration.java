package io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class StateStructureConfiguration {

    /*
    When not empty, will be used instead of scraping.
    Stored in the directory BrobotSettings.screenshotPath.
     */
    private List<Scene> scenes = new ArrayList<>();
    private Pattern topLeftBoundary;
    private Pattern bottomRightBoundary;
    private Region usableArea = new Region();
    private int minWidthBetweenImages;
    private int minImageArea;
    private double maxSimilarityForUniqueImage;
    private int minimumChangedPixelsForNewScreen = 20000;

    public boolean isLive() {
        return scenes.isEmpty();
    }

    public static class Builder {
        private final List<Scene> scenes = new ArrayList<>();
        private Pattern topLeftBoundary;
        private Pattern bottomRightBoundary;
        private Region usableArea = new Region();
        private int minWidthBetweenImages;
        private int minImageArea = 25;
        private double maxSimilarityForUniqueImage = .98;
        private int minimumChangedPixelsForNewScreen = 20000;

        public Builder addScenes(List<Scene> scenes) {
            this.scenes.addAll(scenes);
            return this;
        }

        public Builder addScenes(Scene... scenes) {
            Collections.addAll(this.scenes, scenes);
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
            stateStructureConfiguration.scenes = scenes;
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
