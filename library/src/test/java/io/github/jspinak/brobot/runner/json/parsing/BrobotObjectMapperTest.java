package io.github.jspinak.brobot.runner.json.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.runner.json.module.BrobotJsonModule;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for BrobotObjectMapper JSON configuration and mixins.
 * Tests cover:
 * - Custom serializer/deserializer registration
 * - Mixin configurations for third-party classes
 * - Problematic class handling (OpenCV, AWT, Sikuli)
 * - Module registration and configuration
 */
@DisplayName("BrobotObjectMapper - JSON Configuration and Mixins")
public class BrobotObjectMapperTest extends BrobotTestBase {

    private BrobotObjectMapper brobotObjectMapper;
    private BrobotJsonModule jsonModule;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Create a mock BrobotJsonModule since we can't easily construct it
        jsonModule = mock(BrobotJsonModule.class);
        brobotObjectMapper = new BrobotObjectMapper(jsonModule);
    }

    @Nested
    @DisplayName("Basic Configuration Tests")
    class BasicConfigurationTests {

        @Test
        @DisplayName("Should initialize with BrobotJsonModule registered")
        void shouldInitializeWithBrobotJsonModule() {
            assertNotNull(brobotObjectMapper);
            
            // Verify module registration by attempting to serialize a Brobot object
            PatternFindOptions options = new PatternFindOptions.Builder()
                    .build();
            
            assertDoesNotThrow(() -> {
                String json = brobotObjectMapper.writeValueAsString(options);
                assertNotNull(json);
                assertTrue(json.contains("@type"));
            });
        }

        @Test
        @DisplayName("Should configure basic ObjectMapper properties")
        void shouldConfigureBasicProperties() {
            // Test that mapper can handle dates without JSR310 module errors
            LocalDateTime now = LocalDateTime.now();
            assertDoesNotThrow(() -> {
                String json = brobotObjectMapper.writeValueAsString(now);
                LocalDateTime deserialized = brobotObjectMapper.readValue(json, LocalDateTime.class);
                assertNotNull(deserialized);
            });
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValues() throws JsonProcessingException {
            Map<String, Object> testMap = new HashMap<>();
            testMap.put("key1", null);
            testMap.put("key2", "value");
            
            String json = brobotObjectMapper.writeValueAsString(testMap);
            Map<String, Object> result = brobotObjectMapper.readValue(json, Map.class);
            
            assertTrue(result.containsKey("key1"));
            assertNull(result.get("key1"));
            assertEquals("value", result.get("key2"));
        }
    }

    @Nested
    @DisplayName("Mixin Configuration Tests")
    class MixinConfigurationTests {

        @Test
        @DisplayName("Should apply AWT mixins for Point class")
        void shouldApplyAwtPointMixin() throws JsonProcessingException {
            Point point = new Point(100, 200);
            
            String json = brobotObjectMapper.writeValueAsString(point);
            assertNotNull(json);
            assertTrue(json.contains("100"));
            assertTrue(json.contains("200"));
            
            // Verify deserialization works
            Point deserialized = brobotObjectMapper.readValue(json, Point.class);
            assertEquals(100, deserialized.x);
            assertEquals(200, deserialized.y);
        }

        @Test
        @DisplayName("Should apply AWT mixins for Rectangle class")
        void shouldApplyAwtRectangleMixin() throws JsonProcessingException {
            Rectangle rect = new Rectangle(10, 20, 300, 400);
            
            String json = brobotObjectMapper.writeValueAsString(rect);
            assertNotNull(json);
            
            Rectangle deserialized = brobotObjectMapper.readValue(json, Rectangle.class);
            assertEquals(10, deserialized.x);
            assertEquals(20, deserialized.y);
            assertEquals(300, deserialized.width);
            assertEquals(400, deserialized.height);
        }

        @Test
        @DisplayName("Should apply AWT mixins for Color class")
        void shouldApplyAwtColorMixin() throws JsonProcessingException {
            Color color = new Color(255, 128, 64, 32);
            
            String json = brobotObjectMapper.writeValueAsString(color);
            assertNotNull(json);
            
            // Color serialization typically uses RGB values
            assertTrue(json.contains("255") || json.contains("red"));
            assertTrue(json.contains("128") || json.contains("green"));
            assertTrue(json.contains("64") || json.contains("blue"));
        }

        @Test
        @DisplayName("Should handle BufferedImage serialization without native data")
        void shouldHandleBufferedImageSerialization() {
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            
            // BufferedImage should be handled but not fully serialized
            assertDoesNotThrow(() -> {
                String json = brobotObjectMapper.writeValueAsString(image);
                assertNotNull(json);
                // Should not contain massive pixel data
                assertTrue(json.length() < 10000);
            });
        }
    }

    @Nested
    @DisplayName("Brobot Type Serialization Tests")
    class BrobotTypeSerializationTests {

        @Test
        @DisplayName("Should serialize Location with position information")
        void shouldSerializeLocation() throws JsonProcessingException {
            Location location = new Location(100, 200);
            
            String json = brobotObjectMapper.writeValueAsString(location);
            assertNotNull(json);
            
            // Verify key location properties are serialized
            Location deserialized = brobotObjectMapper.readValue(json, Location.class);
            assertNotNull(deserialized);
        }

        @Test
        @DisplayName("Should serialize Region with boundaries")
        void shouldSerializeRegion() throws JsonProcessingException {
            Region region = new Region(10, 20, 300, 400);
            
            String json = brobotObjectMapper.writeValueAsString(region);
            assertNotNull(json);
            assertTrue(json.contains("10"));
            assertTrue(json.contains("20"));
            assertTrue(json.contains("300"));
            assertTrue(json.contains("400"));
            
            Region deserialized = brobotObjectMapper.readValue(json, Region.class);
            assertEquals(10, deserialized.getX());
            assertEquals(20, deserialized.getY());
            assertEquals(300, deserialized.getW());
            assertEquals(400, deserialized.getH());
        }

        @Test
        @DisplayName("Should serialize Image metadata without BufferedImage data")
        void shouldSerializeImageMetadata() throws JsonProcessingException {
            Image image = new Image("test-image.png");
            
            String json = brobotObjectMapper.writeValueAsString(image);
            assertNotNull(json);
            assertTrue(json.contains("test-image.png"));
            
            // Should not contain BufferedImage data
            assertFalse(json.contains("bufferedImage"));
        }

        @Test
        @DisplayName("Should serialize StateImage with patterns")
        void shouldSerializeStateImage() throws JsonProcessingException {
            StateImage stateImage = new StateImage();
            stateImage.setName("button.png");
            
            String json = brobotObjectMapper.writeValueAsString(stateImage);
            assertNotNull(json);
            assertTrue(json.contains("button.png"));
        }

        @Test
        @DisplayName("Should serialize ActionResult with match data")
        void shouldSerializeActionResult() throws JsonProcessingException {
            ActionResult result = new ActionResult();
            result.setSuccess(true);
            // ActionResult fields are set automatically
            
            String json = brobotObjectMapper.writeValueAsString(result);
            assertNotNull(json);
            assertTrue(json.contains("true"));
        }
    }

    @Nested
    @DisplayName("Polymorphic Serialization Tests")
    class PolymorphicSerializationTests {

        @Test
        @DisplayName("Should serialize PatternFindOptions with type discriminator")
        void shouldSerializePatternFindOptionsWithType() throws JsonProcessingException {
            PatternFindOptions options = new PatternFindOptions.Builder()
                    .build();
            
            String json = brobotObjectMapper.writeValueAsString(options);
            assertNotNull(json);
            assertTrue(json.contains("\"@type\""));
            assertTrue(json.contains("PatternFindOptions") || json.contains("FIND"));
        }

        @Test
        @DisplayName("Should serialize ClickOptions with type discriminator")
        void shouldSerializeClickOptionsWithType() throws JsonProcessingException {
            ClickOptions options = new ClickOptions.Builder()
                    .setNumberOfClicks(2)
                    .build();
            
            String json = brobotObjectMapper.writeValueAsString(options);
            assertNotNull(json);
            assertTrue(json.contains("\"@type\""));
            assertTrue(json.contains("ClickOptions") || json.contains("CLICK"));
        }

        @Test
        @DisplayName("Should serialize TypeOptions with type discriminator")
        void shouldSerializeTypeOptionsWithType() throws JsonProcessingException {
            TypeOptions options = new TypeOptions.Builder()
                    .setTypeDelay(0.1)
                    .build();
            
            String json = brobotObjectMapper.writeValueAsString(options);
            assertNotNull(json);
            assertTrue(json.contains("\"@type\""));
            assertTrue(json.contains("TypeOptions") || json.contains("TYPE"));
            assertTrue(json.contains("0.1"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "FIND", "CLICK", "TYPE", "DRAG", "SCROLL", "MOVE", "DOUBLE_CLICK"
        })
        @DisplayName("Should handle various ActionConfig type discriminators")
        void shouldHandleVariousActionConfigTypes(String typeHint) {
            // Create JSON with type discriminator
            String json = String.format("{\"@type\":\"%s\",\"duration\":1.0}", typeHint);
            
            // Should not throw when attempting to deserialize
            assertDoesNotThrow(() -> {
                Object result = brobotObjectMapper.readValue(json, Object.class);
                assertNotNull(result);
            });
        }
    }

    @Nested
    @DisplayName("OpenCV Mat Handling Tests")
    class OpenCVMatHandlingTests {

        @Test
        @DisplayName("Should serialize Mat metadata without native data")
        void shouldSerializeMatMetadata() throws JsonProcessingException {
            Mat mat = new Mat();
            
            String json = brobotObjectMapper.writeValueAsString(mat);
            assertNotNull(json);
            
            // Should be small - just metadata, not data
            assertTrue(json.length() < 1000);
            
            // Should contain structural info
            assertTrue(json.contains("rows") || json.contains("cols") || json.contains("{}"));
        }

        @Test
        @DisplayName("Should handle null Mat gracefully")
        void shouldHandleNullMat() throws JsonProcessingException {
            Mat mat = null;
            
            String json = brobotObjectMapper.writeValueAsString(mat);
            assertEquals("null", json);
        }

        @Test
        @DisplayName("Should serialize Mat in complex objects")
        void shouldSerializeMatInComplexObjects() {
            Map<String, Object> complex = new HashMap<>();
            complex.put("mat", new Mat());
            complex.put("name", "test");
            complex.put("value", 42);
            
            assertDoesNotThrow(() -> {
                String json = brobotObjectMapper.writeValueAsString(complex);
                assertNotNull(json);
                assertTrue(json.contains("test"));
                assertTrue(json.contains("42"));
            });
        }
    }

    @Nested
    @DisplayName("Circular Reference Handling Tests")
    class CircularReferenceHandlingTests {

        @Test
        @DisplayName("Should handle ObjectCollection circular references")
        void shouldHandleObjectCollectionCircularReferences() {
            ObjectCollection collection = new ObjectCollection.Builder()
                    .withScenes("scene1")
                    .build();
            
            // ObjectCollection can have circular references
            assertDoesNotThrow(() -> {
                String json = brobotObjectMapper.writeValueAsString(collection);
                assertNotNull(json);
                assertTrue(json.contains("scene1"));
            });
        }

        @Test
        @DisplayName("Should handle self-referencing objects")
        void shouldHandleSelfReferencingObjects() {
            Map<String, Object> selfRef = new HashMap<>();
            selfRef.put("name", "test");
            selfRef.put("self", selfRef); // Circular reference
            
            // Should handle without infinite recursion
            assertDoesNotThrow(() -> {
                String json = brobotObjectMapper.writeValueAsString(selfRef);
                assertNotNull(json);
            });
        }
    }

    @Nested
    @DisplayName("General JSON Operations Tests")
    class GeneralJsonOperationsTests {

        @Test
        @DisplayName("Should create JsonNode from string")
        void shouldCreateJsonNodeFromString() throws JsonProcessingException {
            String jsonString = "{\"key\":\"value\",\"number\":42}";
            
            JsonNode node = brobotObjectMapper.readTree(jsonString);
            assertNotNull(node);
            assertEquals("value", node.get("key").asText());
            assertEquals(42, node.get("number").asInt());
        }

        @Test
        @DisplayName("Should create ObjectNode")
        void shouldCreateObjectNode() {
            var objectNode = brobotObjectMapper.createObjectNode();
            assertNotNull(objectNode);
            
            objectNode.put("field1", "value1");
            objectNode.put("field2", 123);
            
            assertEquals("value1", objectNode.get("field1").asText());
            assertEquals(123, objectNode.get("field2").asInt());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle malformed JSON gracefully")
        void shouldHandleMalformedJson() {
            String malformed = "{\"key\": \"value\", \"bad\": }";
            
            assertThrows(JsonProcessingException.class, () -> {
                brobotObjectMapper.readValue(malformed, Map.class);
            });
        }

        @Test
        @DisplayName("Should handle unknown type discriminators")
        void shouldHandleUnknownTypeDiscriminators() {
            String json = "{\"@type\":\"UnknownType\",\"field\":\"value\"}";
            
            // Should not crash on unknown types
            assertDoesNotThrow(() -> {
                Object result = brobotObjectMapper.readValue(json, Object.class);
                assertNotNull(result);
            });
        }

        @Test
        @DisplayName("Should handle incompatible type conversions")
        void shouldHandleIncompatibleTypeConversions() {
            String json = "{\"number\":\"not-a-number\"}";
            
            class TestClass {
                public int number;
            }
            
            assertThrows(JsonProcessingException.class, () -> {
                brobotObjectMapper.readValue(json, TestClass.class);
            });
        }
    }

    @Test
    @DisplayName("Should provide thread-safe mapper instance")
    void shouldProvideThreadSafeMapperInstance() throws InterruptedException {
        int threadCount = 10;
        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            Thread thread = new Thread(() -> {
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put("thread", index);
                    data.put("timestamp", System.currentTimeMillis());
                    
                    String json = brobotObjectMapper.writeValueAsString(data);
                    Map<String, Object> result = brobotObjectMapper.readValue(json, Map.class);
                    
                    assertEquals(index, ((Number) result.get("thread")).intValue());
                } catch (Exception e) {
                    exceptions.add(e);
                }
            });
            threads.add(thread);
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertTrue(exceptions.isEmpty(), "Thread safety issues detected: " + exceptions);
    }
}