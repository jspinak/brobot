package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import io.github.jspinak.brobot.model.state.State;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StateIntegrationTest {
    
    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    private Pattern createTestPattern() {
        BufferedImage dummyImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        return new Pattern(dummyImage);
    }

    @Test
    void stateCanBeCreatedInSpringContext() {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(createTestPattern())
                .build();

        State state = new State.Builder("IntegrationTestState")
                .withImages(stateImage)
                .withRegions(new StateRegion.Builder().build())
                .setBlocking(true)
                .build();

        assertNotNull(state, "State should be created successfully in a Spring context.");
        assertEquals("IntegrationTestState", state.getName());
        assertTrue(state.isBlocking());
    }

    @Test
    void testGetBoundariesInSpringContext() {
        StateRegion stateRegion1 = new StateRegion.Builder().setSearchRegion(new Region(0, 10, 20, 20)).build();
        StateRegion stateRegion2 = new StateRegion.Builder().setSearchRegion(new Region(30, 10, 20, 20)).build();

        State state = new State.Builder("IntegrationBoundaryState")
                .withRegions(stateRegion1, stateRegion2)
                .build();

        Region boundaries = state.getBoundaries();

        assertEquals(0, boundaries.x());
        assertEquals(10, boundaries.y());
        assertEquals(50, boundaries.w());
        assertEquals(20, boundaries.h());
    }
}
