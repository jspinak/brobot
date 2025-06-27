package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import io.github.jspinak.brobot.testutils.TestPaths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ClickMatchWithAddXYTest {

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "false");
        FrameworkSettings.mock = true;
    }

    @Autowired
    Action action;

    /*
    Clicking should be a unit test. You don't want to actually click on the screen.
    Unit tests are performed by adding screenshots to BrobotSettings.screenshots.
     */
    @Test
    void setPosition() {
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
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
        ActionResult matches = action.perform(actionOptions, objColl);
        Location loc1 = matches.getMatchLocations().get(0);
        System.out.println(loc1);
        assertEquals(77, loc1.getCalculatedY());
    }

    @Test
    void addXY() {
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
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
        ActionResult matches = action.perform(actionOptions, objColl);
        Location loc1 = matches.getMatchLocations().get(0);
        System.out.println(loc1);
        assertEquals(85, loc1.getCalculatedY());
    }

}
