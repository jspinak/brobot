package io.github.jspinak.brobot.datatypes.state;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ObjectCollectionIntegrationTest {

    private Pattern createTestPattern(String name) {
        BufferedImage dummyImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Pattern pattern = new Pattern(dummyImage);
        pattern.setName(name);
        return pattern;
    }

    @Test
    void testCreationInContext() {
        ObjectCollection collection = new ObjectCollection.Builder()
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
        StateImage stateImage1 = new StateImage.Builder().addPattern(createTestPattern("p1")).build();
        stateImage1.setShared(false);
        StateImage stateImage2 = new StateImage.Builder().addPattern(createTestPattern("p2")).build();
        stateImage2.setShared(true); // one shared, one not

        State state = new State.Builder("TestState")
                .withImages(stateImage1, stateImage2)
                .build();

        // 2. Build ObjectCollections from the State
        ObjectCollection allImages = new ObjectCollection.Builder()
                .withAllStateImages(state)
                .build();

        ObjectCollection nonSharedImages = new ObjectCollection.Builder()
                .withNonSharedImages(state)
                .build();

        // 3. Assertions
        assertEquals(2, allImages.getStateImages().size(), "Should contain all images from the state.");
        assertEquals(1, nonSharedImages.getStateImages().size(), "Should contain only non-shared images.");
        assertEquals("p1", nonSharedImages.getStateImages().get(0).getName());
    }
}
