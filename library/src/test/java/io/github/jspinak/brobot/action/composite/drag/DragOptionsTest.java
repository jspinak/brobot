package io.github.jspinak.brobot.action.composite.drag;

import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DragOptions Tests")
public class DragOptionsTest extends BrobotTestBase {
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        objectMapper = new ObjectMapper();
    }
    
    @Nested
    @DisplayName("Builder Construction")
    class BuilderConstruction {
        
        @Test
        @DisplayName("Should create default DragOptions")
        public void testDefaultDragOptions() {
            DragOptions options = DragOptions.builder().build();
            
            assertNotNull(options);
            assertNotNull(options.getMousePressOptions());
            assertEquals(0.5, options.getDelayBetweenMouseDownAndMove());
            assertEquals(0.5, options.getDelayAfterDrag());
        }
        
        @Test
        @DisplayName("Should set custom mouse press options")
        public void testCustomMousePressOptions() {
            MousePressOptions mousePressOptions = MousePressOptions.builder()
                .setButton(MouseButton.RIGHT)
                .build();
            
            DragOptions options = DragOptions.builder()
                .setMousePressOptions(mousePressOptions)
                .build();
            
            assertEquals(mousePressOptions, options.getMousePressOptions());
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.1, 0.5, 1.0, 2.5, 10.0})
        @DisplayName("Should set various delay between mouse down and move")
        public void testVariousDelayBetweenMouseDownAndMove(double delay) {
            DragOptions options = DragOptions.builder()
                .setDelayBetweenMouseDownAndMove(delay)
                .build();
            
            assertEquals(delay, options.getDelayBetweenMouseDownAndMove());
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.2, 0.5, 1.5, 5.0})
        @DisplayName("Should set various delay after drag")
        public void testVariousDelayAfterDrag(double delay) {
            DragOptions options = DragOptions.builder()
                .setDelayAfterDrag(delay)
                .build();
            
            assertEquals(delay, options.getDelayAfterDrag());
        }
        
        @Test
        @DisplayName("Should chain builder methods")
        public void testBuilderChaining() {
            MousePressOptions mousePressOptions = MousePressOptions.builder()
                .setButton(MouseButton.MIDDLE)
                .build();
            
            DragOptions options = DragOptions.builder()
                .setMousePressOptions(mousePressOptions)
                .setDelayBetweenMouseDownAndMove(1.0)
                .setDelayAfterDrag(2.0)
                .build();
            
            assertEquals(mousePressOptions, options.getMousePressOptions());
            assertEquals(1.0, options.getDelayBetweenMouseDownAndMove());
            assertEquals(2.0, options.getDelayAfterDrag());
        }
    }
    
    @Nested
    @DisplayName("Mouse Button Configuration")
    class MouseButtonConfiguration {
        
        @ParameterizedTest
        @EnumSource(MouseButton.class)
        @DisplayName("Should handle all mouse button types")
        public void testAllMouseButtons(MouseButton button) {
            MousePressOptions mousePressOptions = MousePressOptions.builder()
                .setButton(button)
                .build();
            
            DragOptions options = DragOptions.builder()
                .setMousePressOptions(mousePressOptions)
                .build();
            
            assertEquals(button, options.getMousePressOptions().getButton());
        }
        
        @Test
        @DisplayName("Should default to left mouse button")
        public void testDefaultMouseButton() {
            DragOptions options = DragOptions.builder().build();
            
            assertEquals(MouseButton.LEFT, options.getMousePressOptions().getButton());
        }
    }
    
    @Nested
    @DisplayName("JSON Serialization")
    class JsonSerialization {
        
        @Test
        @DisplayName("Should serialize to JSON")
        public void testSerialization() throws Exception {
            DragOptions options = DragOptions.builder()
                .setDelayBetweenMouseDownAndMove(1.5)
                .setDelayAfterDrag(2.5)
                .build();
            
            String json = objectMapper.writeValueAsString(options);
            
            assertNotNull(json);
            assertTrue(json.contains("delayBetweenMouseDownAndMove"));
            assertTrue(json.contains("delayAfterDrag"));
            assertTrue(json.contains("1.5"));
            assertTrue(json.contains("2.5"));
        }
        
        @Test
        @DisplayName("Should deserialize from JSON")
        public void testDeserialization() throws Exception {
            String json = "{\"delayBetweenMouseDownAndMove\":3.0,\"delayAfterDrag\":4.0}";
            
            DragOptions options = objectMapper.readValue(json, DragOptions.class);
            
            assertNotNull(options);
            assertEquals(3.0, options.getDelayBetweenMouseDownAndMove());
            assertEquals(4.0, options.getDelayAfterDrag());
        }
        
        @Test
        @DisplayName("Should handle round-trip serialization")
        public void testRoundTripSerialization() throws Exception {
            MousePressOptions mousePressOptions = MousePressOptions.builder()
                .setButton(MouseButton.RIGHT)
                .build();
            
            DragOptions original = DragOptions.builder()
                .setMousePressOptions(mousePressOptions)
                .setDelayBetweenMouseDownAndMove(1.25)
                .setDelayAfterDrag(2.75)
                .build();
            
            String json = objectMapper.writeValueAsString(original);
            DragOptions deserialized = objectMapper.readValue(json, DragOptions.class);
            
            assertEquals(original.getDelayBetweenMouseDownAndMove(), 
                        deserialized.getDelayBetweenMouseDownAndMove());
            assertEquals(original.getDelayAfterDrag(), 
                        deserialized.getDelayAfterDrag());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle negative delays")
        public void testNegativeDelays() {
            DragOptions options = DragOptions.builder()
                .setDelayBetweenMouseDownAndMove(-1.0)
                .setDelayAfterDrag(-2.0)
                .build();
            
            // Should accept negative values (validation happens elsewhere)
            assertEquals(-1.0, options.getDelayBetweenMouseDownAndMove());
            assertEquals(-2.0, options.getDelayAfterDrag());
        }
        
        @Test
        @DisplayName("Should handle very large delays")
        public void testVeryLargeDelays() {
            DragOptions options = DragOptions.builder()
                .setDelayBetweenMouseDownAndMove(Double.MAX_VALUE)
                .setDelayAfterDrag(Double.MAX_VALUE)
                .build();
            
            assertEquals(Double.MAX_VALUE, options.getDelayBetweenMouseDownAndMove());
            assertEquals(Double.MAX_VALUE, options.getDelayAfterDrag());
        }
        
        @Test
        @DisplayName("Should handle null mouse press options")
        public void testNullMousePressOptions() {
            DragOptions options = DragOptions.builder()
                .setMousePressOptions(null)
                .build();
            
            assertNull(options.getMousePressOptions());
        }
        
        @Test
        @DisplayName("Should handle zero delays")
        public void testZeroDelays() {
            DragOptions options = DragOptions.builder()
                .setDelayBetweenMouseDownAndMove(0.0)
                .setDelayAfterDrag(0.0)
                .build();
            
            assertEquals(0.0, options.getDelayBetweenMouseDownAndMove());
            assertEquals(0.0, options.getDelayAfterDrag());
        }
    }
    
    @Nested
    @DisplayName("Practical Use Cases")
    class PracticalUseCases {
        
        @Test
        @DisplayName("Should configure for fast drag operation")
        public void testFastDragConfiguration() {
            DragOptions options = DragOptions.builder()
                .setDelayBetweenMouseDownAndMove(0.1)
                .setDelayAfterDrag(0.1)
                .build();
            
            assertTrue(options.getDelayBetweenMouseDownAndMove() < 0.5);
            assertTrue(options.getDelayAfterDrag() < 0.5);
        }
        
        @Test
        @DisplayName("Should configure for slow precise drag")
        public void testSlowPreciseDragConfiguration() {
            DragOptions options = DragOptions.builder()
                .setDelayBetweenMouseDownAndMove(2.0)
                .setDelayAfterDrag(1.5)
                .build();
            
            assertTrue(options.getDelayBetweenMouseDownAndMove() > 1.0);
            assertTrue(options.getDelayAfterDrag() > 1.0);
        }
        
        @Test
        @DisplayName("Should configure for right-click drag")
        public void testRightClickDragConfiguration() {
            MousePressOptions rightClick = MousePressOptions.builder()
                .setButton(MouseButton.RIGHT)
                .build();
            
            DragOptions options = DragOptions.builder()
                .setMousePressOptions(rightClick)
                .build();
            
            assertEquals(MouseButton.RIGHT, options.getMousePressOptions().getButton());
        }
        
        @Test
        @DisplayName("Should configure for middle-click drag")
        public void testMiddleClickDragConfiguration() {
            MousePressOptions middleClick = MousePressOptions.builder()
                .setButton(MouseButton.MIDDLE)
                .build();
            
            DragOptions options = DragOptions.builder()
                .setMousePressOptions(middleClick)
                .build();
            
            assertEquals(MouseButton.MIDDLE, options.getMousePressOptions().getButton());
        }
    }
}