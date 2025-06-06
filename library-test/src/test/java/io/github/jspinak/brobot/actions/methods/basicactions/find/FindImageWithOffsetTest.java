package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
public class FindImageWithOffsetTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    Action action;

    @Test
    void findImageWithOffsetTest() {
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename("topLeft")
                        .setTargetOffset(10, 10)
                        .build())
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .withScenes("../screenshots/floranext0")
                .build();
        Matches matches = action.perform(ActionOptions.Action.FIND, objColl);
        Location loc1 = matches.getMatchLocations().get(0);

        StateImage topLeft2 = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename("topLeft")
                        .build())
                .build();
        ObjectCollection objColl2 = new ObjectCollection.Builder()
                .withImages(topLeft2)
                .withScenes("../screenshots/floranext0")
                .build();
        Matches matches2 = action.perform(ActionOptions.Action.FIND, objColl2);
        Location loc2 = matches2.getMatchLocations().get(0);

        System.out.println(loc1);
        System.out.println(loc2);
        assertEquals(loc2.getCalculatedX() + 10, loc1.getCalculatedX());
    }

    /*
    The Position set in ActionOptions should override the Position in topLeft2.
     */
    @Test
    void findWithPositionAndOffsetInActionOptions() {
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename("topLeft")
                        .setTargetPosition(new Position(100, 100))
                        .setTargetOffset(-10,0)
                        .build())
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .withScenes("../screenshots/floranext0")
                .build();
        Matches matches = action.perform(ActionOptions.Action.FIND, objColl);
        Location loc1 = matches.getMatchLocations().get(0);

        StateImage topLeft2 = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename("topLeft")
                        .setTargetPosition(0,0)
                        .build())
                .build();
        ObjectCollection objColl2 = new ObjectCollection.Builder()
                .withImages(topLeft2)
                .withScenes("../screenshots/floranext0")
                .build();
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setTargetPosition(100, 100)
                .setTargetOffset(-10,0)
                .build();
        Matches matches2 = action.perform(actionOptions, objColl2);
        Location loc2 = matches2.getMatchLocations().get(0);

        System.out.println(loc1);
        System.out.println(loc2);
        assertEquals(97, loc1.getCalculatedX());
        assertEquals(loc1.getCalculatedX(), loc2.getCalculatedX());
        assertEquals(loc1.getCalculatedY(), loc2.getCalculatedY());
    }

    /*
    The position and offset in the pattern should be overwritten with the methods in the StateImage Builder.
     */
    @Test
    void setOffsetWithStateImage() {
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename("topLeft")
                        .setTargetPosition(100, 100)
                        .setTargetOffset(-10,0)
                        .build())
                .setPositionForAllPatterns(0, 0)
                .setOffsetForAllPatterns(10, 0)
                .build();
        assertEquals(10, topLeft.getPatterns().get(0).getTargetOffset().getCalculatedX());

        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .withScenes("../screenshots/floranext0")
                .build();
        Matches matches = action.perform(ActionOptions.Action.FIND, objColl);
        Location loc1 = matches.getMatchLocations().get(0);

        System.out.println(loc1);
        assertEquals(10, loc1.getCalculatedX());
    }
}
