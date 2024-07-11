package io.github.jspinak.brobot.test.actions.methods.basicactions.click;

import io.github.jspinak.brobot.actions.BrobotSettings;
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

@SpringBootTest
public class ClickMatchWithAddXYTest {

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "false");
        BrobotSettings.mock = true;
    }

    @Autowired
    Action action;

    /*
    Clicking should be a unit test. You don't want to actually click on the screen.
    Unit tests are performed by adding screenshots to BrobotSettings.screenshots.
     */
    @Test
    void setPosition() {
        BrobotSettings.screenshots.add("floranext0");
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setName("topLeft")
                        .setTargetPosition(new Position(100, 100))
                        .build())
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                //.withScenes("../screenshots/floranext0")
                .build();
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                //.setAddX(0)
                //.setAddY(30)
                .build();
        Matches matches = action.perform(actionOptions, objColl);
        Location loc1 = matches.getMatchLocations().get(0);
        System.out.println(loc1);
        assertEquals(77, loc1.getY());
    }

    @Test
    void addXY() {
        BrobotSettings.screenshots.add("floranext0");
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setName("topLeft")
                        //.setPosition(new Position(100, 100))
                        .build())
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                //.withScenes("../screenshots/floranext0")
                .build();
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setAddX(0)
                .setAddY(30)
                .build();
        Matches matches = action.perform(actionOptions, objColl);
        Location loc1 = matches.getMatchLocations().get(0);
        System.out.println(loc1);
        assertEquals(85, loc1.getY());
    }

}
