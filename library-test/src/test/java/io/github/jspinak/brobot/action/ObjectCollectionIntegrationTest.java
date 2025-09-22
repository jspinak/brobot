package io.github.jspinak.brobot.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

@SpringBootTest(
        classes = io.github.jspinak.brobot.BrobotTestApplication.class,
        properties = {
            "brobot.gui-access.continue-on-error=true",
            "brobot.gui-access.check-on-startup=false",
            "java.awt.headless=true",
            "spring.main.allow-bean-definition-overriding=true",
            "brobot.test.type=unit",
            "brobot.capture.physical-resolution=false",
            "brobot.mock.enabled=true",
            "brobot.console.actions.level=QUIET"
        })
@Import({
    MockGuiAccessConfig.class,
    MockGuiAccessMonitor.class,
    MockScreenConfig.class,
    io.github.jspinak.brobot.test.config.TestApplicationConfiguration.class
})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Integration test requires non-CI environment")
public class ObjectCollectionIntegrationTest {

    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    private Pattern createTestPattern(String name) {
        BufferedImage dummyImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Pattern pattern = new Pattern(dummyImage);
        pattern.setNameWithoutExtension(name);
        return pattern;
    }

    @Test
    void testCreationInContext() {
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        .withRegions(new Region(0, 0, 10, 10))
                        .withPatterns(createTestPattern("testPattern"))
                        .build();

        assertNotNull(collection, "Collection should be created successfully in Spring context.");
        assertEquals(1, collection.getStateRegions().size());
        assertEquals(1, collection.getStateImages().size());
    }

    @Test
    void testBuilderWithStateObjectInContext() {
        // 1. Create a State object with some images
        StateImage stateImage1 =
                new StateImage.Builder().addPattern(createTestPattern("p1")).build();
        stateImage1.setShared(false);
        StateImage stateImage2 =
                new StateImage.Builder().addPattern(createTestPattern("p2")).build();
        stateImage2.setShared(true); // one shared, one not

        State state = new State.Builder("TestState").withImages(stateImage1, stateImage2).build();

        // 2. Build ObjectCollections from the State
        ObjectCollection allImages =
                new ObjectCollection.Builder().withAllStateImages(state).build();

        ObjectCollection nonSharedImages =
                new ObjectCollection.Builder().withNonSharedImages(state).build();

        // 3. Assertions
        assertEquals(
                2, allImages.getStateImages().size(), "Should contain all images from the state.");
        assertEquals(
                1,
                nonSharedImages.getStateImages().size(),
                "Should contain only non-shared images.");
        assertEquals("p1", nonSharedImages.getStateImages().get(0).getName());
    }
}
