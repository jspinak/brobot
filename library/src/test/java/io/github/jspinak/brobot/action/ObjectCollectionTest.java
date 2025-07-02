package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.ObjectCollection;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectCollectionTest {

    private Pattern createTestPattern(String name) {
        BufferedImage dummyImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Pattern pattern = new Pattern(dummyImage);
        pattern.setName(name);
        return pattern;
    }

    @Test
    void testBuilder() {
        ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(new Region(0,0,10,10))
                .withPatterns(createTestPattern("testPattern"))
                .withStrings("testString")
                .build();

        assertFalse(collection.isEmpty());
        assertEquals(1, collection.getStateRegions().size());
        assertEquals(1, collection.getStateImages().size());
        assertEquals(1, collection.getStateStrings().size());
    }

    @Test
    void testIsEmpty() {
        ObjectCollection emptyCollection = new ObjectCollection();
        assertTrue(emptyCollection.isEmpty(), "A new collection should be empty.");

        ObjectCollection nonEmptyCollection = new ObjectCollection.Builder()
                .withStrings("not empty")
                .build();
        assertFalse(nonEmptyCollection.isEmpty(), "Collection with an item should not be empty.");
    }

    @Test
    void testResetTimesActedOn() {
        StateImage stateImage = new StateImage.Builder().build();
        stateImage.setTimesActedOn(5);

        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();

        collection.resetTimesActedOn();

        assertEquals(0, collection.getStateImages().get(0).getTimesActedOn());
    }

    @Test
    void testGetFirstObjectName() {
        // Test empty
        assertEquals("", new ObjectCollection().getFirstObjectName());

        // Test with StateImage (name has priority)
        StateImage imgWithName = new StateImage.Builder().setName("imageName").build();
        assertEquals("imageName", new ObjectCollection.Builder().withImages(imgWithName).build().getFirstObjectName());

        // Test with StateRegion
        StateRegion regionWithName = new StateRegion.Builder().setName("regionName").build();
        assertEquals("regionName", new ObjectCollection.Builder().withRegions(regionWithName).build().getFirstObjectName());

        // Test with StateLocation
        StateLocation locationWithName = new StateLocation.Builder().setName("locationName").build();
        assertEquals("locationName", new ObjectCollection.Builder().withLocations(locationWithName).build().getFirstObjectName());

        // Test precedence (Image > Region)
        ObjectCollection mixed = new ObjectCollection.Builder()
                .withImages(imgWithName)
                .withRegions(regionWithName)
                .build();
        assertEquals("imageName", mixed.getFirstObjectName());
    }

    @Test
    void testGetAllOwnerStates() {
        StateImage img = new StateImage.Builder().setOwnerStateName("StateA").build();
        StateRegion rgn = new StateRegion.Builder().setOwnerStateName("StateB").build();
        StateLocation loc = new StateLocation.Builder().setOwnerStateName("StateA").build();

        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(img)
                .withRegions(rgn)
                .withLocations(loc)
                .build();

        Set<String> ownerStates = collection.getAllOwnerStates();

        assertEquals(2, ownerStates.size());
        assertTrue(ownerStates.contains("StateA"));
        assertTrue(ownerStates.contains("StateB"));
    }

    @Test
    void testBuilderWithMatchObjects() {
        Match match1 = new Match.Builder().setRegion(0, 0, 10, 10).setName("match1").build();
        Match match2 = new Match.Builder().setRegion(20, 20, 5, 5).setName("match2").build();

        ObjectCollection collectionAsRegions = new ObjectCollection.Builder()
                .withMatchObjectsAsRegions(match1, match2)
                .build();
        assertEquals(2, collectionAsRegions.getStateRegions().size());
        assertEquals(10, collectionAsRegions.getStateRegions().get(0).w());

        ObjectCollection collectionAsImages = new ObjectCollection.Builder()
                .withMatchObjectsAsStateImages(match1, match2)
                .build();
        assertEquals(2, collectionAsImages.getStateImages().size());
        assertEquals("match1", collectionAsImages.getStateImages().get(0).getName());
    }
}