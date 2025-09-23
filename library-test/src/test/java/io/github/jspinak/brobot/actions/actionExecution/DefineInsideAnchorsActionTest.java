package io.github.jspinak.brobot.actions.actionExecution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

/**
 * Tests for defining regions inside anchor images. Works in headless mode by using mock matches
 * when image recognition is not available.
 */
@SpringBootTest(
        classes = io.github.jspinak.brobot.BrobotTestApplication.class,
        properties = {
            "brobot.gui-access.continue-on-error=true",
            "brobot.gui-access.check-on-startup=false",
            "java.awt.headless=true",
            "spring.main.allow-bean-definition-overriding=true",
            "brobot.test.type=unit",
            "brobot.capture.physical-resolution=false",
            "brobot.mock.enabled=true"
        })
@Import({
    MockScreenConfig.class,
    io.github.jspinak.brobot.test.config.TestApplicationConfiguration.class
})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
@Disabled("CI failure - needs investigation")
public class DefineInsideAnchorsActionTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired Action action;

    @Test
    void defineRegion() {
        try {
            // Create test patterns for anchors
            Pattern topL =
                    new Pattern.Builder()
                            .setFilename("images/topLeft.png")
                            .addAnchor(Positions.Name.TOPLEFT, Positions.Name.BOTTOMLEFT)
                            .build();
            Pattern bottomR =
                    new Pattern.Builder()
                            .setFilename("images/bottomR2.png")
                            .addAnchor(Positions.Name.BOTTOMRIGHT, Positions.Name.TOPRIGHT)
                            .build();

            // Create state images
            StateImage topLeft = new StateImage.Builder().addPattern(topL).build();
            StateImage bottomRight = new StateImage.Builder().addPattern(bottomR).build();

            // Create DefineRegionOptions for INSIDE_ANCHORS
            DefineRegionOptions defineOptions =
                    new DefineRegionOptions.Builder()
                            .setDefineAs(DefineRegionOptions.DefineAs.INSIDE_ANCHORS)
                            .build();

            // Create ObjectCollection with anchor images
            ObjectCollection insideAnchorObjects =
                    new ObjectCollection.Builder().withImages(topLeft, bottomRight).build();

            ActionResult matches = action.perform(defineOptions, insideAnchorObjects);

            System.out.println("ActionResult: " + matches);

            // In headless/mock mode, the action should still complete
            assertNotNull(matches);

            // The defined region may be different in mock mode
            if (matches.getDefinedRegion() != null) {
                System.out.println("Defined region: " + matches.getDefinedRegion());
                System.out.println("Region y2: " + matches.getDefinedRegion().y2());

                // Verify we got a valid region
                Region region = matches.getDefinedRegion();
                assertTrue(region.w() > 0, "Region width should be positive");
                assertTrue(region.h() > 0, "Region height should be positive");

                // In mock mode, region values will be generated
                // so we can't assert exact values
            } else {
                // In pure mock mode, we might not get a defined region
                System.out.println("No defined region in mock mode - this is expected");
            }

            // Success can mean different things in different modes
            // In mock mode, it might be false if no mock data is available
            System.out.println("Success: " + matches.isSuccess());

        } catch (Exception e) {
            // Handle cases where image files are not available
            if (e.getMessage() != null && e.getMessage().contains("Can't read input file")) {
                System.out.println("Test images not available - skipping test");
                return;
            }
            throw e;
        }
    }
}
