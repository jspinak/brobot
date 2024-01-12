package io.github.jspinak.brobot.actions.methods.basicactions;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;

@Getter
public class TestData {

    Pattern screenshot = new Pattern.Builder()
            .setFilename("../screenshots/floranext0")
            .build();
    Pattern topL = new Pattern.Builder()
            .setFilename("topLeft")
            .addAnchor(Position.Name.TOPLEFT, Position.Name.BOTTOMLEFT)
            .build();
    Pattern bottomR = new Pattern.Builder()
            .setFilename("bottomR2")
            .addAnchor(Position.Name.BOTTOMRIGHT, Position.Name.TOPRIGHT)
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

}
