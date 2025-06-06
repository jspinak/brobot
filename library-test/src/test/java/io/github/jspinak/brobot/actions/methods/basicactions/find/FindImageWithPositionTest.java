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
public class FindImageWithPositionTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    Action action;

    @Test
    void findImageWithPositionTest() {
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename("topLeft")
                        .setTargetPosition(new Position(100, 100))
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
                        .setTargetPosition(new Position(0,0))
                        .build())
                .build();
        ObjectCollection objColl2 = new ObjectCollection.Builder()
                .withImages(topLeft2)
                .withScenes("../screenshots/floranext0")
                .build();
        Matches matches2 = action.perform(ActionOptions.Action.FIND, objColl2);
        Location loc2 = matches2.getMatchLocations().get(0);

        assertNotEquals(loc1.getCalculatedX(), loc2.getCalculatedX());
    }

    /*
    The Position set in ActionOptions should override the Position in topLeft2.
     */
    @Test
    void findWithPositionInActionOptions() {
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename("topLeft")
                        .setTargetPosition(new Position(100, 100))
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
                        .setTargetPosition(new Position(0,0))
                        .build())
                .build();
        ObjectCollection objColl2 = new ObjectCollection.Builder()
                .withImages(topLeft2)
                .withScenes("../screenshots/floranext0")
                .build();
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setTargetPosition(100, 100)
                .build();
        Matches matches2 = action.perform(actionOptions, objColl2);
        Location loc2 = matches2.getMatchLocations().get(0);

        System.out.println(loc1);
        System.out.println(loc2);
        assertEquals(loc1.getCalculatedX(), loc2.getCalculatedX());
        assertEquals(loc1.getCalculatedY(), loc2.getCalculatedY());
    }
}
