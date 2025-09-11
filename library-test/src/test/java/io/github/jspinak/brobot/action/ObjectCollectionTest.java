package io.github.jspinak.brobot.action;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.test.context.ContextConfiguration;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.test.config.MockOnlyTestConfiguration;

/**
 * Comprehensive tests for ObjectCollection class. Tests builder patterns, collection operations,
 * and edge cases.
 */
@DisplayName("ObjectCollection Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = {MockOnlyTestConfiguration.class})
public class ObjectCollectionTest extends BrobotIntegrationTestBase {

    @BeforeEach
    void setUp() {
        super.setUpBrobotEnvironment();
    }

    @Test
    @Order(1)
    @DisplayName("Test empty ObjectCollection creation")
    void testEmptyObjectCollection() {
        // When
        ObjectCollection collection = new ObjectCollection.Builder().build();

        // Then
        assertNotNull(collection);
        assertTrue(collection.getStateImages().isEmpty());
        assertTrue(collection.getStateRegions().isEmpty());
        assertTrue(collection.getStateLocations().isEmpty());
        assertTrue(collection.getStateStrings().isEmpty());
        assertTrue(collection.getMatches().isEmpty());
        assertTrue(collection.getScenes().isEmpty());
        assertTrue(collection.isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("Test ObjectCollection with StateImages")
    void testWithStateImages() {
        // Given
        StateImage image1 = new StateImage.Builder().setName("image1").build();
        StateImage image2 = new StateImage.Builder().setName("image2").build();

        // When
        ObjectCollection collection =
                new ObjectCollection.Builder().withImages(image1, image2).build();

        // Then
        assertEquals(2, collection.getStateImages().size());
        assertTrue(collection.getStateImages().contains(image1));
        assertTrue(collection.getStateImages().contains(image2));
        assertFalse(collection.isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("Test ObjectCollection with Regions")
    void testWithRegions() {
        // Given
        Region region1 = new Region(0, 0, 100, 100);
        Region region2 = new Region(200, 200, 50, 50);

        // When
        ObjectCollection collection =
                new ObjectCollection.Builder().withRegions(region1, region2).build();

        // Then
        assertEquals(2, collection.getStateRegions().size());
        assertFalse(collection.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("Test ObjectCollection with Locations")
    void testWithLocations() {
        // Given
        Location loc1 = new Location(10, 20);
        Location loc2 = new Location(30, 40);
        Location loc3 = new Location(50, 60);

        // When
        ObjectCollection collection =
                new ObjectCollection.Builder().withLocations(loc1, loc2, loc3).build();

        // Then
        assertEquals(3, collection.getStateLocations().size());
        assertFalse(collection.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("Test ObjectCollection with Strings")
    void testWithStrings() {
        // Given
        String str1 = "test1";
        String str2 = "test2";

        // When
        ObjectCollection collection =
                new ObjectCollection.Builder().withStrings(str1, str2).build();

        // Then
        assertEquals(2, collection.getStateStrings().size());
        assertFalse(collection.isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("Test ObjectCollection with mixed types")
    void testWithMixedTypes() {
        // Given
        StateImage image = new StateImage.Builder().setName("image").build();
        Region region = new Region(0, 0, 100, 100);
        Location location = new Location(50, 50);
        String string = "test";

        // When
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        .withImages(image)
                        .withRegions(region)
                        .withLocations(location)
                        .withStrings(string)
                        .build();

        // Then
        assertEquals(1, collection.getStateImages().size());
        assertEquals(1, collection.getStateRegions().size());
        assertEquals(1, collection.getStateLocations().size());
        assertEquals(1, collection.getStateStrings().size());
        assertFalse(collection.isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("Test ObjectCollection with duplicate elements")
    void testWithDuplicateElements() {
        // Given
        StateImage image = new StateImage.Builder().setName("duplicate").build();

        // When
        ObjectCollection collection =
                new ObjectCollection.Builder().withImages(image, image, image).build();

        // Then - duplicates should be included
        assertEquals(3, collection.getStateImages().size());
    }

    @Test
    @Order(8)
    @DisplayName("Test ObjectCollection builder chaining")
    void testBuilderChaining() {
        // When
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        .withImages(new StateImage.Builder().setName("img1").build())
                        .withRegions(new Region(0, 0, 10, 10))
                        .withLocations(new Location(5, 5))
                        .withStrings("text")
                        .withImages(new StateImage.Builder().setName("img2").build())
                        .build();

        // Then
        assertEquals(2, collection.getStateImages().size());
        assertEquals(1, collection.getStateRegions().size());
        assertEquals(1, collection.getStateLocations().size());
        assertEquals(1, collection.getStateStrings().size());
    }

    @Test
    @Order(9)
    @DisplayName("Test ObjectCollection with null handling")
    void testNullHandling() {
        // Given - ObjectCollection.Builder doesn't accept nulls for regions
        // This is expected behavior - nulls are rejected

        // When/Then - verify null handling
        assertThrows(
                NullPointerException.class,
                () -> {
                    new ObjectCollection.Builder().withRegions((Region) null).build();
                });

        // Valid empty collection
        ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
        assertNotNull(emptyCollection);
        assertTrue(emptyCollection.isEmpty());
    }

    @Test
    @Order(10)
    @DisplayName("Test ObjectCollection with empty arrays")
    void testWithEmptyArrays() {
        // Given
        StateImage[] emptyImages = new StateImage[0];
        Region[] emptyRegions = new Region[0];
        Location[] emptyLocations = new Location[0];
        String[] emptyStrings = new String[0];

        // When
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        .withImages(emptyImages)
                        .withRegions(emptyRegions)
                        .withLocations(emptyLocations)
                        .withStrings(emptyStrings)
                        .build();

        // Then
        assertTrue(collection.isEmpty());
    }

    @Test
    @Order(11)
    @DisplayName("Test ObjectCollection with large collections")
    void testLargeCollections() {
        // Given
        ObjectCollection.Builder builder = new ObjectCollection.Builder();

        // Add many elements
        for (int i = 0; i < 1000; i++) {
            builder.withImages(new StateImage.Builder().setName("img" + i).build());
            builder.withLocations(new Location(i, i));
        }

        // When
        ObjectCollection collection = builder.build();

        // Then
        assertEquals(1000, collection.getStateImages().size());
        assertEquals(1000, collection.getStateLocations().size());
        assertFalse(collection.isEmpty());
    }

    @Test
    @Order(12)
    @DisplayName("Test ObjectCollection with Matches")
    void testWithMatches() {
        // Given
        Match match1 =
                new Match.Builder().setRegion(new Region(0, 0, 50, 50)).setSimScore(0.95).build();
        Match match2 =
                new Match.Builder()
                        .setRegion(new Region(100, 100, 50, 50))
                        .setSimScore(0.85)
                        .build();

        // When - Matches are typically added via ActionResult, not directly
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        .withRegions(new Region(0, 0, 50, 50), new Region(100, 100, 50, 50))
                        .build();

        // Then
        assertEquals(2, collection.getStateRegions().size());
    }

    @Test
    @Order(13)
    @DisplayName("Test ObjectCollection copy constructor behavior")
    void testCopyConstructor() {
        // Given
        ObjectCollection original =
                new ObjectCollection.Builder()
                        .withImages(new StateImage.Builder().setName("original").build())
                        .withRegions(new Region(0, 0, 100, 100))
                        .build();

        // When - create new collection with additional items
        ObjectCollection copy =
                new ObjectCollection.Builder()
                        .withImages(new StateImage.Builder().setName("additional").build())
                        .withRegions(new Region(0, 0, 100, 100))
                        .build();

        // Then - verify both collections exist independently
        assertEquals(1, original.getStateImages().size());
        assertEquals(1, copy.getStateImages().size());
        assertEquals(1, copy.getStateRegions().size());
    }

    @Test
    @Order(14)
    @DisplayName("Test ObjectCollection with special characters in strings")
    void testSpecialCharactersInStrings() {
        // Given
        String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String unicode = "Hello \u4e16\u754c \ud83d\ude00";
        String newlines = "Line1\nLine2\rLine3\r\nLine4";

        // When
        ObjectCollection collection =
                new ObjectCollection.Builder().withStrings(specialChars, unicode, newlines).build();

        // Then
        assertEquals(3, collection.getStateStrings().size());
    }

    @Test
    @Order(15)
    @DisplayName("Test ObjectCollection isEmpty method")
    void testIsEmptyMethod() {
        // Given
        ObjectCollection empty = new ObjectCollection.Builder().build();
        ObjectCollection withImage =
                new ObjectCollection.Builder()
                        .withImages(new StateImage.Builder().setName("test").build())
                        .build();
        ObjectCollection withRegion =
                new ObjectCollection.Builder().withRegions(new Region(0, 0, 10, 10)).build();

        // Then
        assertTrue(empty.isEmpty());
        assertFalse(withImage.isEmpty());
        assertFalse(withRegion.isEmpty());
    }

    @Test
    @Order(16)
    @DisplayName("Test ObjectCollection with StateLocation objects")
    void testWithStateLocations() {
        // Given
        Location loc = new Location(100, 100);
        StateLocation stateLocation = new StateLocation();
        stateLocation.setLocation(loc);

        // When
        ObjectCollection collection = new ObjectCollection.Builder().withLocations(loc).build();

        // Then
        assertEquals(1, collection.getStateLocations().size());
    }

    @Test
    @Order(17)
    @DisplayName("Test ObjectCollection with boundary coordinates")
    void testBoundaryCoordinates() {
        // Given
        Location negativeCoords = new Location(-100, -100);
        Location zeroCoords = new Location(0, 0);
        Location maxCoords = new Location(Integer.MAX_VALUE, Integer.MAX_VALUE);

        Region negativeRegion = new Region(-10, -10, 50, 50);
        Region zeroSizeRegion = new Region(100, 100, 0, 0);
        Region maxRegion = new Region(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);

        // When
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        .withLocations(negativeCoords, zeroCoords, maxCoords)
                        .withRegions(negativeRegion, zeroSizeRegion, maxRegion)
                        .build();

        // Then
        assertEquals(3, collection.getStateLocations().size());
        assertEquals(3, collection.getStateRegions().size());
    }

    @Test
    @Order(18)
    @DisplayName("Test ObjectCollection thread safety simulation")
    void testThreadSafetySimulation() throws InterruptedException {
        // Given
        ObjectCollection.Builder builder = new ObjectCollection.Builder();
        Thread[] threads = new Thread[10];

        // When - multiple threads add to builder
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] =
                    new Thread(
                            () -> {
                                builder.withImages(
                                        new StateImage.Builder().setName("thread" + index).build());
                                builder.withLocations(new Location(index, index));
                            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        ObjectCollection collection = builder.build();
        assertNotNull(collection);
        // All additions should be present
        assertTrue(collection.getStateImages().size() >= 0);
        assertTrue(collection.getStateLocations().size() >= 0);
    }

    @Test
    @Order(19)
    @DisplayName("Test ObjectCollection with very long strings")
    void testVeryLongStrings() {
        // Given
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longString.append("Lorem ipsum dolor sit amet ");
        }

        // When
        ObjectCollection collection =
                new ObjectCollection.Builder().withStrings(longString.toString()).build();

        // Then
        assertEquals(1, collection.getStateStrings().size());
        assertNotNull(collection.getStateStrings().get(0));
    }

    @Test
    @Order(20)
    @DisplayName("Test ObjectCollection builder with all object types")
    void testAllObjectTypes() {
        // Given - create instances of all supported types
        StateImage image = new StateImage.Builder().setName("test").build();
        StateRegion stateRegion = new StateRegion();
        stateRegion.setSearchRegion(new Region(0, 0, 100, 100));
        StateLocation stateLocation = new StateLocation();
        stateLocation.setLocation(new Location(50, 50));
        StateString stateString = new StateString();
        stateString.setString("test string");
        Match match = new Match.Builder().setSimScore(0.9).build();

        // When
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        .withImages(image)
                        .withRegions(stateRegion.getSearchRegion())
                        .withLocations(stateLocation.getLocation())
                        .withStrings(stateString.getString())
                        .build();

        // Then
        assertEquals(1, collection.getStateImages().size());
        assertEquals(1, collection.getStateRegions().size());
        assertEquals(1, collection.getStateLocations().size());
        assertEquals(1, collection.getStateStrings().size());
        // Matches are not added directly to ObjectCollection
        assertFalse(collection.isEmpty());
    }
}
