package io.github.jspinak.brobot.actions.methods.basicactions;

import java.io.File;
import java.nio.file.Paths;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.Getter;

/**
 * Updated test data class using new ActionConfig API.
 *
 * <p>Key changes: - Uses DefineRegionOptions instead of DefineRegionOptions - Type-safe
 * configuration for region definition
 */
@Getter
public class TestDataUpdated {

    private static String screenshotsPath;
    private static String imagesPath;

    private Pattern screenshot;
    private Pattern topL;
    private Pattern bottomR;
    private StateImage topLeft;
    private StateImage bottomRight;

    // NEW API: Use DefineRegionOptions
    private DefineRegionOptions defineInsideAnchors;
    private ObjectCollection insideAnchorObjects;
    private Pattern floranext0;
    private Pattern floranext1;
    private Pattern floranext2;
    private Pattern floranext3;
    private Pattern floranext4;

    private boolean initialized = false;

    private void initPaths() {
        if (screenshotsPath == null) {
            // Determine the correct paths based on current working directory
            String currentDir = System.getProperty("user.dir");
            File imageDir = new File(currentDir, "images");
            File screenshotDir = new File(currentDir, "screenshots");

            // If running from project root, adjust paths
            if (!imageDir.exists()) {
                imageDir = new File(currentDir, "library-test/images");
                screenshotDir = new File(currentDir, "library-test/screenshots");
            }

            imagesPath = imageDir.getAbsolutePath();
            screenshotsPath = screenshotDir.getAbsolutePath();
        }
    }

    public TestDataUpdated() {
        initPaths();
    }

    private void init() {
        if (!initialized) {
            screenshot = new Pattern(Paths.get(screenshotsPath, "floranext1.png").toString());
            topL = new Pattern(Paths.get(imagesPath, "topLeft.png").toString());
            // Positions API has changed - PercentOfRegion may not exist
            // topL.getPositions().setPositionPercentOfRegion(new Positions.PercentOfRegion(30,
            // 15));
            bottomR = new Pattern(Paths.get(imagesPath, "bottomRight.png").toString());
            topLeft = new StateImage.Builder().addPattern(topL).build();
            bottomRight = new StateImage.Builder().addPattern(bottomR).build();

            // NEW API: Use DefineRegionOptions
            defineInsideAnchors =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.INSIDE_ANCHORS)
                            .build();

            insideAnchorObjects =
                    new ObjectCollection.Builder()
                            .withScenes(
                                    new Pattern(
                                            Paths.get(screenshotsPath, "floranext1.png")
                                                    .toString()))
                            .withImages(topLeft, bottomRight)
                            .build();

            floranext0 = new Pattern(Paths.get(screenshotsPath, "floranext0.png").toString());
            floranext1 = new Pattern(Paths.get(screenshotsPath, "floranext1.png").toString());
            floranext2 = new Pattern(Paths.get(screenshotsPath, "floranext2.png").toString());
            floranext3 = new Pattern(Paths.get(screenshotsPath, "floranext3.png").toString());
            floranext4 = new Pattern(Paths.get(screenshotsPath, "floranext4.png").toString());

            initialized = true;
        }
    }

    private void ensureInitialized() {
        if (!initialized) {
            init();
        }
    }

    public Pattern getScreenshot() {
        ensureInitialized();
        return screenshot;
    }

    public Pattern getTopL() {
        ensureInitialized();
        return topL;
    }

    public Pattern getBottomR() {
        ensureInitialized();
        return bottomR;
    }

    public StateImage getTopLeft() {
        ensureInitialized();
        return topLeft;
    }

    public StateImage getBottomRight() {
        ensureInitialized();
        return bottomRight;
    }

    public DefineRegionOptions getDefineInsideAnchors() {
        ensureInitialized();
        return defineInsideAnchors;
    }

    public ObjectCollection getInsideAnchorObjects() {
        ensureInitialized();
        return insideAnchorObjects;
    }

    public Pattern getFloranext0() {
        ensureInitialized();
        return floranext0;
    }

    public Pattern getFloranext1() {
        ensureInitialized();
        return floranext1;
    }

    public Pattern getFloranext2() {
        ensureInitialized();
        return floranext2;
    }

    public Pattern getFloranext3() {
        ensureInitialized();
        return floranext3;
    }

    public Pattern getFloranext4() {
        ensureInitialized();
        return floranext4;
    }

    /** Example of creating other DefineRegionOptions configurations */
    public static class DefineRegionExamples {

        public static DefineRegionOptions defineOutsideAnchors() {
            return new DefineRegionOptions.Builder()
                    .setDefineAs(DefineRegionOptions.DefineAs.OUTSIDE_ANCHORS)
                    .build();
        }

        public static DefineRegionOptions defineWithMatches() {
            return new DefineRegionOptions.Builder()
                    .setDefineAs(DefineRegionOptions.DefineAs.INCLUDING_MATCHES)
                    .build();
        }

        public static DefineRegionOptions defineWithWindow() {
            return new DefineRegionOptions.Builder()
                    .setDefineAs(DefineRegionOptions.DefineAs.FOCUSED_WINDOW)
                    .build();
        }

        public static DefineRegionOptions defineWithBorders(
                int top, int right, int bottom, int left) {
            return new DefineRegionOptions.Builder()
                    .setDefineAs(DefineRegionOptions.DefineAs.MATCH)
                    // Border methods don't exist in DefineRegionOptions
                    .build();
        }
    }
}
