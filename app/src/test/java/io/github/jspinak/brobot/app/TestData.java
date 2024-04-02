package io.github.jspinak.brobot.app;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TestData {

    Pattern screenshot = new Pattern.Builder()
            .setFilename("../screenshots/floranext0")
            .build();
    Pattern topL = new Pattern.Builder()
            .setFilename("topLeft")
            .addAnchor(Positions.Name.TOPLEFT, Positions.Name.BOTTOMLEFT)
            .build();
    Pattern bottomR = new Pattern.Builder()
            .setFilename("bottomR2")
            .addAnchor(Positions.Name.BOTTOMRIGHT, Positions.Name.TOPRIGHT)
            .build();
    StateImage topLeft = new StateImage.Builder()
            .addPattern(topL)
            .build();
    StateImage bottomRight = new StateImage.Builder()
            .addPattern(bottomR)
            .build();
    ActionOptions defineInsideAnchors = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.DEFINE)
            .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
            .build();
    ObjectCollection insideAnchorObjects = new ObjectCollection.Builder()
            .withScenes(new Pattern("../screenshots/FloraNext1"))
            .withImages(topLeft, bottomRight)
            .build();

    Pattern floranext0 = new Pattern("../screenshots/floranext0");
    Pattern floranext1 = new Pattern("../screenshots/floranext1");
    Pattern floranext2 = new Pattern("../screenshots/floranext2");
    Pattern floranext3 = new Pattern("../screenshots/floranext3");
    Pattern floranext4 = new Pattern("../screenshots/floranext4");

    public List<Pattern> getPatterns() {
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(floranext0);
        patterns.add(floranext1);
        patterns.add(floranext2);
        patterns.add(floranext3);
        patterns.add(floranext4);
        return patterns;
    }

    public List<Pattern> getPatterns(Integer... indices) {
        List<Pattern> patterns = new ArrayList<>();
        if (List.of(indices).contains(0)) patterns.add(floranext0);
        if (List.of(indices).contains(1)) patterns.add(floranext1);
        if (List.of(indices).contains(2)) patterns.add(floranext2);
        if (List.of(indices).contains(3)) patterns.add(floranext3);
        if (List.of(indices).contains(4)) patterns.add(floranext4);
        return patterns;
    }

    public List<StateImage> getStateImages() {
        List<StateImage> images = new ArrayList<>();
        images.add(topLeft);
        images.add(bottomRight);
        return images;
    }

}
