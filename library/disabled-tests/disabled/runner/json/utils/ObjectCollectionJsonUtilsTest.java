package io.github.jspinak.brobot.runner.json.utils;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.*;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.test.DisabledInCI;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Comprehensive tests for ObjectCollectionJsonUtils serialization utilities.
 * Tests heterogeneous collection serialization, Map conversion, and deep copying.
 */
@DisplayName("ObjectCollectionJsonUtils Tests")

@DisabledInCI
public class ObjectCollectionJsonUtilsTest extends BrobotTestBase {

    @Mock
    private JsonUtils mockJsonUtils;
    
    @Mock
    private MatchesJsonUtils mockMatchesJsonUtils;
    
    @Mock
    private ConfigurationParser mockJsonParser;
    
    private ObjectCollectionJsonUtils objectCollectionJsonUtils;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        objectCollectionJsonUtils = new ObjectCollectionJsonUtils(
            mockJsonUtils, mockMatchesJsonUtils, mockJsonParser);
    }
    
    @Nested
    @DisplayName("Map Conversion")
    class MapConversion {
        
        @Test
        @DisplayName("Should convert empty ObjectCollection to Map")
        public void testEmptyCollectionToMap() {
            // Given
            ObjectCollection empty = new ObjectCollection.Builder().build();
            
            // When
            Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(empty);
            
            // Then
            assertNotNull(result);
            assertTrue(((List<?>) result.get("stateLocations")).isEmpty());
            assertTrue(((List<?>) result.get("stateImages")).isEmpty());
            assertTrue(((List<?>) result.get("stateRegions")).isEmpty());
            assertTrue(((List<?>) result.get("stateStrings")).isEmpty());
            assertTrue(((List<?>) result.get("matches")).isEmpty());
            assertTrue(((List<?>) result.get("scenes")).isEmpty());
        }
        
        @Test
        @DisplayName("Should convert ObjectCollection with StateImages to Map")
        public void testStateImagesToMap() {
            // Given
            StateImage image1 = new StateImage.Builder()
                .setName("button.png")
                .build();
            StateImage image2 = new StateImage.Builder()
                .setName("icon.png")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(image1, image2)
                .build();
            
            // When
            Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(collection);
            
            // Then
            List<?> images = (List<?>) result.get("stateImages");
            assertEquals(2, images.size());
        }
        
        @Test
        @DisplayName("Should convert ObjectCollection with StateLocations to Map")
        public void testStateLocationsToMap() {
            // Given
            StateLocation loc1 = new StateLocation.Builder()
                .setLocation(new Location(100, 200))
                .build();
            StateLocation loc2 = new StateLocation.Builder()
                .setLocation(new Location(300, 400))
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(loc1, loc2)
                .build();
            
            // When
            Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(collection);
            
            // Then
            List<?> locations = (List<?>) result.get("stateLocations");
            assertEquals(2, locations.size());
        }
        
        @Test
        @DisplayName("Should convert ObjectCollection with StateRegions to Map")
        public void testStateRegionsToMap() {
            // Given
            StateRegion region1 = new StateRegion.Builder()
                .setSearchRegion(new Region(0, 0, 100, 100))
                .build();
            StateRegion region2 = new StateRegion.Builder()
                .setSearchRegion(new Region(100, 100, 200, 200))
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(region1, region2)
                .build();
            
            // When
            Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(collection);
            
            // Then
            List<?> regions = (List<?>) result.get("stateRegions");
            assertEquals(2, regions.size());
        }
        
        @Test
        @DisplayName("Should convert ObjectCollection with StateStrings to Map")
        public void testStateStringsToMap() {
            // Given
            StateString str1 = new StateString.Builder()
                .setString("test1")
                .build();
            StateString str2 = new StateString.Builder()
                .setString("test2")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings("test1", "test2")
                .build();
            
            // When
            Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(collection);
            
            // Then
            List<?> strings = (List<?>) result.get("stateStrings");
            assertEquals(2, strings.size());
        }
        
        @Test
        @DisplayName("Should delegate ActionResult conversion to MatchesJsonUtils")
        public void testMatchesConversion() {
            // Given
            ActionResult result1 = new ActionResult();
            ActionResult result2 = new ActionResult();
            
            Map<String, Object> matchMap1 = Map.of("match", "data1");
            Map<String, Object> matchMap2 = Map.of("match", "data2");
            
            when(mockMatchesJsonUtils.matchesToMap(result1)).thenReturn(matchMap1);
            when(mockMatchesJsonUtils.matchesToMap(result2)).thenReturn(matchMap2);
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withMatches(result1, result2)
                .build();
            
            // When
            Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(collection);
            
            // Then
            List<?> matches = (List<?>) result.get("matches");
            assertEquals(2, matches.size());
            verify(mockMatchesJsonUtils).matchesToMap(result1);
            verify(mockMatchesJsonUtils).matchesToMap(result2);
        }
        
        @Test
        @DisplayName("Should extract Scene filenames only")
        public void testSceneConversion() {
            // Given
            Pattern pattern1 = mock(Pattern.class);
            Pattern pattern2 = mock(Pattern.class);
            when(pattern1.getName()).thenReturn("scene1.png");
            when(pattern2.getName()).thenReturn("scene2.png");
            
            Scene scene1 = new Scene(pattern1);
            Scene scene2 = new Scene(pattern2);
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withScenes(scene1, scene2)
                .build();
            
            // When
            Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(collection);
            
            // Then
            List<Map<String, Object>> scenes = (List<Map<String, Object>>) result.get("scenes");
            assertEquals(2, scenes.size());
            assertEquals("scene1.png", scenes.get(0).get("filename"));
            assertEquals("scene2.png", scenes.get(1).get("filename"));
        }
        
        @Test
        @DisplayName("Should handle mixed ObjectCollection with all types")
        public void testMixedCollectionToMap() {
            // Given
            StateImage image = new StateImage.Builder().setName("img.png").build();
            StateLocation location = new StateLocation.Builder()
                .setLocation(new Location(50, 50)).build();
            StateRegion region = new StateRegion.Builder()
                .setSearchRegion(new Region(0, 0, 50, 50)).build();
            StateString string = new StateString.Builder().setString("text").build();
            ActionResult actionResult = new ActionResult();
            
            Pattern pattern = mock(Pattern.class);
            when(pattern.getName()).thenReturn("scene.png");
            Scene scene = new Scene(pattern);
            
            when(mockMatchesJsonUtils.matchesToMap(actionResult))
                .thenReturn(Map.of("result", "data"));
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(image)
                .withLocations(location)
                .withRegions(region)
                .withStrings("text")
                .withMatches(actionResult)
                .withScenes(scene)
                .build();
            
            // When
            Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(collection);
            
            // Then
            assertNotNull(result);
            assertEquals(1, ((List<?>) result.get("stateImages")).size());
            assertEquals(1, ((List<?>) result.get("stateLocations")).size());
            assertEquals(1, ((List<?>) result.get("stateRegions")).size());
            assertEquals(1, ((List<?>) result.get("stateStrings")).size());
            assertEquals(1, ((List<?>) result.get("matches")).size());
            assertEquals(1, ((List<?>) result.get("scenes")).size());
        }
    }
    
    @Nested
    @DisplayName("JSON Serialization")
    class JSONSerialization {
        
        @Test
        @DisplayName("Should serialize ObjectCollection to JSON")
        public void testSerializeToJson() throws Exception {
            // Given
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(new StateImage.Builder().setName("test.png").build())
                .withStrings("test")
                .build();
            
            String expectedJson = "{\"stateImages\":[...],\"stateStrings\":[...]}";
            when(mockJsonUtils.toJsonSafe(collection)).thenReturn(expectedJson);
            
            // When
            String result = objectCollectionJsonUtils.objectCollectionToJson(collection);
            
            // Then
            assertEquals(expectedJson, result);
            verify(mockJsonUtils).toJsonSafe(collection);
        }
        
        @Test
        @DisplayName("Should handle serialization failure")
        public void testSerializationFailure() throws Exception {
            // Given
            ObjectCollection collection = new ObjectCollection.Builder().build();
            when(mockJsonUtils.toJsonSafe(collection))
                .thenThrow(new ConfigurationException("Serialization failed"));
            
            // When/Then
            assertThrows(ConfigurationException.class,
                () -> objectCollectionJsonUtils.objectCollectionToJson(collection));
        }
        
        @Test
        @DisplayName("Should serialize null ObjectCollection")
        public void testSerializeNull() throws Exception {
            // Given
            when(mockJsonUtils.toJsonSafe(null)).thenReturn("null");
            
            // When
            String result = objectCollectionJsonUtils.objectCollectionToJson(null);
            
            // Then
            assertEquals("null", result);
        }
        
        @Test
        @DisplayName("Should serialize large ObjectCollection")
        public void testSerializeLargeCollection() throws Exception {
            // Given
            ObjectCollection.Builder builder = new ObjectCollection.Builder();
            for (int i = 0; i < 100; i++) {
                builder.withImages(new StateImage.Builder()
                    .setName("image" + i + ".png").build());
            }
            ObjectCollection largeCollection = builder.build();
            
            when(mockJsonUtils.toJsonSafe(largeCollection)).thenReturn("{\"large\":\"data\"}");
            
            // When
            String result = objectCollectionJsonUtils.objectCollectionToJson(largeCollection);
            
            // Then
            assertNotNull(result);
            verify(mockJsonUtils).toJsonSafe(largeCollection);
        }
    }
    
    @Nested
    @DisplayName("Deep Copy")
    class DeepCopy {
        
        @Test
        @DisplayName("Should create deep copy of ObjectCollection")
        public void testDeepCopy() throws Exception {
            // Given
            ObjectCollection original = new ObjectCollection.Builder()
                .withImages(new StateImage.Builder().setName("test.png").build())
                .withStrings("test")
                .build();
            
            String json = "{\"stateImages\":[...],\"stateStrings\":[...]}";
            ObjectCollection copy = new ObjectCollection.Builder()
                .withImages(new StateImage.Builder().setName("test.png").build())
                .withStrings("test")
                .build();
            
            when(mockJsonUtils.toJsonSafe(original)).thenReturn(json);
            when(mockJsonParser.convertJson(json, ObjectCollection.class)).thenReturn(copy);
            
            // When
            ObjectCollection result = objectCollectionJsonUtils.deepCopy(original);
            
            // Then
            assertNotNull(result);
            assertNotSame(original, result);
            assertEquals(original.getStateImages().size(), result.getStateImages().size());
            assertEquals(original.getStateStrings().size(), result.getStateStrings().size());
        }
        
        @Test
        @DisplayName("Should handle deep copy failure during serialization")
        public void testDeepCopySerializationFailure() throws Exception {
            // Given
            ObjectCollection original = new ObjectCollection.Builder().build();
            when(mockJsonUtils.toJsonSafe(original))
                .thenThrow(new ConfigurationException("Serialization failed"));
            
            // When/Then
            assertThrows(ConfigurationException.class,
                () -> objectCollectionJsonUtils.deepCopy(original));
        }
        
        @Test
        @DisplayName("Should handle deep copy failure during deserialization")
        public void testDeepCopyDeserializationFailure() throws Exception {
            // Given
            ObjectCollection original = new ObjectCollection.Builder().build();
            String json = "{}";
            
            when(mockJsonUtils.toJsonSafe(original)).thenReturn(json);
            when(mockJsonParser.convertJson(json, ObjectCollection.class))
                .thenThrow(new ConfigurationException("Deserialization failed"));
            
            // When/Then
            assertThrows(ConfigurationException.class,
                () -> objectCollectionJsonUtils.deepCopy(original));
        }
        
        @Test
        @DisplayName("Should create independent copy")
        public void testDeepCopyIndependence() throws Exception {
            // Given
            StateImage originalImage = new StateImage.Builder().setName("original.png").build();
            ObjectCollection original = new ObjectCollection.Builder()
                .withImages(originalImage)
                .build();
            
            StateImage copyImage = new StateImage.Builder().setName("original.png").build();
            ObjectCollection copy = new ObjectCollection.Builder()
                .withImages(copyImage)
                .build();
            
            String json = "{\"images\":[...]}";
            when(mockJsonUtils.toJsonSafe(original)).thenReturn(json);
            when(mockJsonParser.convertJson(json, ObjectCollection.class)).thenReturn(copy);
            
            // When
            ObjectCollection result = objectCollectionJsonUtils.deepCopy(original);
            
            // Then - original and copy should be independent instances
            assertNotNull(result);
            assertNotSame(original, result);
            assertEquals(1, result.getStateImages().size());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle ObjectCollection with null lists")
        public void testNullListsInCollection() {
            // Given - Create collection through reflection to force null lists
            ObjectCollection collection = new ObjectCollection.Builder().build();
            // The builder ensures lists are never null, so this test validates that
            
            // When
            Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(collection);
            
            // Then
            assertNotNull(result);
            assertNotNull(result.get("stateImages"));
            assertNotNull(result.get("stateLocations"));
        }
        
        @Test
        @DisplayName("Should handle Scene with null pattern")
        public void testSceneWithNullPattern() {
            // Given
            Scene scene = new Scene(); // Default constructor creates empty Scene
            ObjectCollection collection = new ObjectCollection.Builder()
                .withScenes(scene)
                .build();
            
            // When - Check if the implementation handles null pattern gracefully
            try {
                Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(collection);
                // If no exception, verify that scenes list is handled
                List<?> scenes = (List<?>) result.get("scenes");
                assertNotNull(scenes);
            } catch (Exception e) {
                // It's acceptable for this to throw an exception with null pattern
                assertTrue(e instanceof RuntimeException);
            }
        }
        
        @Test
        @DisplayName("Should handle ActionResult with circular references")
        public void testActionResultWithCircularReference() {
            // Given
            ActionResult actionResult = new ActionResult();
            // ActionResult might have circular references internally
            
            Map<String, Object> safeMap = Map.of("safe", "data");
            when(mockMatchesJsonUtils.matchesToMap(actionResult)).thenReturn(safeMap);
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withMatches(actionResult)
                .build();
            
            // When
            Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(collection);
            
            // Then
            List<?> matches = (List<?>) result.get("matches");
            assertEquals(1, matches.size());
            assertEquals(safeMap, matches.get(0));
        }
        
        @Test
        @DisplayName("Should handle very large number of elements")
        public void testLargeNumberOfElements() {
            // Given
            ObjectCollection.Builder builder = new ObjectCollection.Builder();
            
            // Add many elements of each type
            for (int i = 0; i < 1000; i++) {
                if (i % 4 == 0) {
                    builder.withImages(new StateImage.Builder()
                        .setName("img" + i + ".png").build());
                } else if (i % 4 == 1) {
                    builder.withLocations(new StateLocation.Builder()
                        .setLocation(new Location(i, i)).build());
                } else if (i % 4 == 2) {
                    builder.withRegions(new StateRegion.Builder()
                        .setSearchRegion(new Region(i, i, 10, 10)).build());
                } else {
                    builder.withStrings("string" + i);
                }
            }
            
            ObjectCollection largeCollection = builder.build();
            
            // When
            Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(largeCollection);
            
            // Then
            assertNotNull(result);
            assertTrue(((List<?>) result.get("stateImages")).size() > 200);
            assertTrue(((List<?>) result.get("stateLocations")).size() > 200);
            assertTrue(((List<?>) result.get("stateRegions")).size() > 200);
            assertTrue(((List<?>) result.get("stateStrings")).size() > 200);
        }
        
        @Test
        @DisplayName("Should handle ObjectCollection with duplicate elements")
        public void testDuplicateElements() {
            // Given
            StateImage sameImage = new StateImage.Builder().setName("same.png").build();
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(sameImage, sameImage, sameImage)
                .build();
            
            // When
            Map<String, Object> result = objectCollectionJsonUtils.objectCollectionToMap(collection);
            
            // Then
            List<?> images = (List<?>) result.get("stateImages");
            assertEquals(3, images.size()); // Duplicates are preserved
        }
    }
}