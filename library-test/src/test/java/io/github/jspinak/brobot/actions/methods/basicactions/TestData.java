package io.github.jspinak.brobot.actions.methods.basicactions;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TestData {
    
    private static String screenshotsPath;
    private static String imagesPath;
    
    private Pattern screenshot;
    private Pattern topL;
    private Pattern bottomR;
    private StateImage topLeft;
    private StateImage bottomRight;
    private ActionOptions defineInsideAnchors;
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
    
    private void ensureInitialized() {
        if (!initialized) {
            initPaths();
            
            screenshot = new Pattern.Builder()
                    .setFilename(Paths.get(screenshotsPath, "floranext0.png").toString())
                    .build();
            topL = new Pattern.Builder()
                    .setFilename(Paths.get(imagesPath, "topLeft.png").toString())
                    .addAnchor(Positions.Name.TOPLEFT, Positions.Name.BOTTOMLEFT)
                    .build();
            bottomR = new Pattern.Builder()
                    .setFilename(Paths.get(imagesPath, "bottomR2.png").toString())
                    .addAnchor(Positions.Name.BOTTOMRIGHT, Positions.Name.TOPRIGHT)
                    .build();
            topLeft = new StateImage.Builder()
                    .addPattern(topL)
                    .build();
            bottomRight = new StateImage.Builder()
                    .addPattern(bottomR)
                    .build();
            defineInsideAnchors = new ActionOptions.Builder()
                    .setAction(ActionOptions.Action.DEFINE)
                    .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
                    .build();
            insideAnchorObjects = new ObjectCollection.Builder()
                    .withScenes(new Pattern(Paths.get(screenshotsPath, "floranext1.png").toString()))
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
    
    public ActionOptions getDefineInsideAnchors() {
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

    public List<Pattern> getPatterns() {
        ensureInitialized();
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(floranext0);
        patterns.add(floranext1);
        patterns.add(floranext2);
        patterns.add(floranext3);
        patterns.add(floranext4);
        return patterns;
    }

    public List<Pattern> getPatterns(Integer... indices) {
        ensureInitialized();
        List<Pattern> patterns = new ArrayList<>();
        if (List.of(indices).contains(0)) patterns.add(floranext0);
        if (List.of(indices).contains(1)) patterns.add(floranext1);
        if (List.of(indices).contains(2)) patterns.add(floranext2);
        if (List.of(indices).contains(3)) patterns.add(floranext3);
        if (List.of(indices).contains(4)) patterns.add(floranext4);
        return patterns;
    }

    public List<StateImage> getStateImages() {
        ensureInitialized();
        List<StateImage> images = new ArrayList<>();
        images.add(topLeft);
        images.add(bottomRight);
        return images;
    }

}
