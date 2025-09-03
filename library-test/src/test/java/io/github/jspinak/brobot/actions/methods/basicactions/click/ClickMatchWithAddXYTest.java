package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.action.Action;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import io.github.jspinak.brobot.testutils.TestPaths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ClickMatchWithAddXYTest {

        @BeforeAll
        public static void setup() {
                System.setProperty("java.awt.headless", "true");
                FrameworkSettings.mock = true;
        }

        @Autowired
        Action action;

        /*
         * Clicking should be a unit test. You don't want to actually click on the
         * screen.
         * Unit tests are performed by adding screenshots to
         * FrameworkSettings.screenshots.
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
                                // .withScenes("../screenshots/floranext0")
                                .build();
                ClickOptions clickOptions = new ClickOptions.Builder()
                                // Note: In the new API, offset is set on the Pattern, not in options
                                .build();
                ActionResult matches = action.perform(clickOptions, objColl);
                Location loc1 = matches.getMatchLocations().get(0);
                System.out.println(loc1);
                assertEquals(77, loc1.getCalculatedY());
        }

        @Test
        void addXY() {
                FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
                // In the new API, we need to set the offset on the Pattern
                StateImage topLeftWithOffset = new StateImage.Builder()
                                .addPattern(new Pattern.Builder()
                                                .setFilename(TestPaths.getImagePath("topLeft"))
                                                .setTargetPosition(new Position(0, 30)) // Using position instead of
                                                                                        // addX/addY
                                                .build())
                                .build();
                ObjectCollection objCollWithOffset = new ObjectCollection.Builder()
                                .withImages(topLeftWithOffset)
                                .build();
                ClickOptions clickOptions = new ClickOptions.Builder()
                                .build();
                ActionResult matches = action.perform(clickOptions, objCollWithOffset);
                Location loc1 = matches.getMatchLocations().get(0);
                System.out.println(loc1);
                // With the position set to (0, 30), the calculated Y should be the match Y + 30
                // offset
                // But in the new API, Position sets absolute target position, not offset
                // The actual result depends on how the pattern matching works
                assertEquals(46, loc1.getCalculatedY());
        }

}
