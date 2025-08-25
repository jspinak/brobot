package io.github.jspinak.brobot.runner.json.utils;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for ActionConfigJsonUtils JSON serialization utilities.
 * Tests polymorphic serialization, deep copying, and type conversion for ActionConfig hierarchy.
 */
@DisplayName("ActionConfigJsonUtils Tests")
public class ActionConfigJsonUtilsTest extends BrobotTestBase {

    @Mock
    private JsonUtils mockJsonUtils;
    
    @Mock
    private ConfigurationParser mockJsonParser;
    
    private ActionConfigJsonUtils actionConfigJsonUtils;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        actionConfigJsonUtils = new ActionConfigJsonUtils(mockJsonUtils, mockJsonParser);
    }
    
    @Nested
    @DisplayName("Serialization")
    class Serialization {
        
        @Test
        @DisplayName("Should serialize ClickOptions with type information")
        public void testSerializeClickOptions() throws Exception {
            // Given
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .setPauseBeforeBegin(0.5)
                .setPauseAfterEnd(1.0)
                .build();
            
            // When
            String json = actionConfigJsonUtils.toJson(clickOptions);
            
            // Then
            assertNotNull(json);
            assertTrue(json.contains("ClickOptions") || json.contains("@type"));
            assertTrue(json.contains("2") || json.contains("numberOfClicks"));
        }
        
        @Test
        @DisplayName("Should serialize PatternFindOptions with type information")
        public void testSerializePatternFindOptions() throws Exception {
            // Given
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.95)
                .setMaxMatchesToActOn(5)
                .setPauseBeforeBegin(1.0)
                .build();
            
            // When
            String json = actionConfigJsonUtils.toJson(findOptions);
            
            // Then
            assertNotNull(json);
            assertTrue(json.contains("PatternFindOptions") || json.contains("@type"));
        }
        
        @Test
        @DisplayName("Should serialize TypeOptions with type information")
        public void testSerializeTypeOptions() throws Exception {
            // Given
            TypeOptions typeOptions = new TypeOptions.Builder()
                .setTypeDelay(0.1)
                .setModifiers("shift+")
                .build();
            
            // When
            String json = actionConfigJsonUtils.toJson(typeOptions);
            
            // Then
            assertNotNull(json);
            assertTrue(json.contains("TypeOptions") || json.contains("@type"));
        }
        
        @Test
        @DisplayName("Should add type information if not present")
        public void testAddTypeInformation() throws Exception {
            // Given
            MouseMoveOptions moveOptions = new MouseMoveOptions.Builder()
                .setMoveMouseDelay(0.5f)
                .build();
            
            // When
            String json = actionConfigJsonUtils.toJson(moveOptions);
            
            // Then
            assertNotNull(json);
            assertTrue(json.contains("MouseMoveOptions") || json.contains("@type"));
        }
        
        @Test
        @DisplayName("Should handle null ActionConfig")
        public void testSerializeNull() throws Exception {
            // When
            String json = actionConfigJsonUtils.toJson(null);
            
            // Then
            assertEquals("null", json);
        }
    }
    
    @Nested
    @DisplayName("Deserialization")
    class Deserialization {
        
        @Test
        @DisplayName("Should deserialize ClickOptions correctly")
        public void testDeserializeClickOptions() throws Exception {
            // Given
            String json = "{\"@type\":\"ClickOptions\",\"numberOfClicks\":2,\"pauseBeforeBegin\":0.5}";
            
            // When
            ActionConfig result = actionConfigJsonUtils.fromJson(json);
            
            // Then
            assertNotNull(result);
            assertInstanceOf(ClickOptions.class, result);
            ClickOptions clickOptions = (ClickOptions) result;
            assertEquals(2, clickOptions.getNumberOfClicks());
        }
        
        @Test
        @DisplayName("Should deserialize PatternFindOptions correctly")
        public void testDeserializePatternFindOptions() throws Exception {
            // Given
            String json = "{\"@type\":\"PatternFindOptions\",\"similarity\":0.95,\"maxMatchesToActOn\":5}";
            
            // When
            ActionConfig result = actionConfigJsonUtils.fromJson(json);
            
            // Then
            assertNotNull(result);
            assertInstanceOf(PatternFindOptions.class, result);
            PatternFindOptions findOptions = (PatternFindOptions) result;
            assertEquals(0.95, findOptions.getSimilarity(), 0.001);
            assertEquals(5, findOptions.getMaxMatchesToActOn());
        }
        
        @Test
        @DisplayName("Should handle invalid JSON")
        public void testDeserializeInvalidJson() {
            // Given
            String invalidJson = "{invalid json}";
            
            // When/Then
            assertThrows(ConfigurationException.class,
                () -> actionConfigJsonUtils.fromJson(invalidJson));
        }
        
        @Test
        @DisplayName("Should handle unknown type")
        public void testDeserializeUnknownType() {
            // Given
            String json = "{\"@type\":\"UnknownOptions\",\"someField\":\"value\"}";
            
            // When/Then
            assertThrows(ConfigurationException.class,
                () -> actionConfigJsonUtils.fromJson(json));
        }
    }
    
    @Nested
    @DisplayName("Deep Copy")
    class DeepCopy {
        
        @Test
        @DisplayName("Should create deep copy of ClickOptions")
        public void testDeepCopyClickOptions() throws Exception {
            // Given
            ClickOptions original = new ClickOptions.Builder()
                .setNumberOfClicks(3)
                .setPauseBeforeBegin(1.0)
                .setPressOptions(MousePressOptions.builder()
                    .setButton(MouseButton.RIGHT)
                    .build())
                .build();
            
            // When
            ClickOptions copy = actionConfigJsonUtils.deepCopy(original);
            
            // Then
            assertNotNull(copy);
            assertNotSame(original, copy);
            assertEquals(original.getNumberOfClicks(), copy.getNumberOfClicks());
            assertEquals(original.getPauseBeforeBegin(), copy.getPauseBeforeBegin(), 0.001);
            assertEquals(original.getMousePressOptions().getButton(), 
                        copy.getMousePressOptions().getButton());
        }
        
        @Test
        @DisplayName("Should create deep copy of PatternFindOptions")
        public void testDeepCopyPatternFindOptions() throws Exception {
            // Given
            PatternFindOptions original = new PatternFindOptions.Builder()
                .setSimilarity(0.85)
                .setMaxMatchesToActOn(10)
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            
            // When
            PatternFindOptions copy = actionConfigJsonUtils.deepCopy(original);
            
            // Then
            assertNotNull(copy);
            assertNotSame(original, copy);
            assertEquals(original.getSimilarity(), copy.getSimilarity(), 0.001);
            assertEquals(original.getMaxMatchesToActOn(), copy.getMaxMatchesToActOn());
            assertEquals(original.getStrategy(), copy.getStrategy());
        }
        
        @Test
        @DisplayName("Should preserve concrete type in deep copy")
        public void testDeepCopyPreservesType() throws Exception {
            // Given
            TypeOptions original = new TypeOptions.Builder()
                .setTypeDelay(0.2)
                .build();
            
            // When
            ActionConfig copy = actionConfigJsonUtils.deepCopy(original);
            
            // Then
            assertInstanceOf(TypeOptions.class, copy);
            assertEquals(original.getClass(), copy.getClass());
        }
        
        @Test
        @DisplayName("Should handle null in deep copy")
        public void testDeepCopyNull() throws Exception {
            // When/Then
            assertThrows(NullPointerException.class,
                () -> actionConfigJsonUtils.deepCopy(null));
        }
        
        @Test
        @DisplayName("Should create independent copy")
        public void testDeepCopyIndependence() throws Exception {
            // Given
            DragOptions original = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(0.5)
                .setDelayAfterDrag(1.0)
                .build();
            
            // When
            DragOptions copy = actionConfigJsonUtils.deepCopy(original);
            
            // Modify original - should not affect copy
            original = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(2.0)
                .build();
            
            // Then
            assertEquals(0.5, copy.getDelayBetweenMouseDownAndMove(), 0.001);
            assertEquals(1.0, copy.getDelayAfterDrag(), 0.001);
        }
    }
    
    @Nested
    @DisplayName("Type Conversion")
    class TypeConversion {
        
        @Test
        @DisplayName("Should convert between compatible ActionConfig types")
        public void testConvertCompatibleTypes() throws Exception {
            // Given
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setPauseBeforeBegin(1.0)
                .setPauseAfterEnd(2.0)
                .build();
            
            // When
            MouseMoveOptions moveOptions = actionConfigJsonUtils.convert(
                clickOptions, MouseMoveOptions.class);
            
            // Then
            assertNotNull(moveOptions);
            // Common fields should be copied
            assertEquals(clickOptions.getPauseBeforeBegin(), 
                        moveOptions.getPauseBeforeBegin(), 0.001);
            assertEquals(clickOptions.getPauseAfterEnd(),
                        moveOptions.getPauseAfterEnd(), 0.001);
        }
        
        @Test
        @DisplayName("Should ignore incompatible fields during conversion")
        public void testConvertIgnoresIncompatibleFields() throws Exception {
            // Given
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.95)
                .setMaxMatchesToActOn(5)
                .setPauseBeforeBegin(0.5)
                .build();
            
            // When
            TypeOptions typeOptions = actionConfigJsonUtils.convert(
                findOptions, TypeOptions.class);
            
            // Then
            assertNotNull(typeOptions);
            // Common field should be copied
            assertEquals(findOptions.getPauseBeforeBegin(),
                        typeOptions.getPauseBeforeBegin(), 0.001);
            // Specific fields should have default values
            assertEquals(0.0, typeOptions.getTypeDelay(), 0.001);
        }
        
        @Test
        @DisplayName("Should handle null source in conversion")
        public void testConvertNullSource() {
            // When/Then
            assertThrows(NullPointerException.class,
                () -> actionConfigJsonUtils.convert(null, ClickOptions.class));
        }
        
        @Test
        @DisplayName("Should handle null target class in conversion")
        public void testConvertNullTargetClass() {
            // Given
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            
            // When/Then
            assertThrows(NullPointerException.class,
                () -> actionConfigJsonUtils.convert(clickOptions, null));
        }
    }
    
    @Nested
    @DisplayName("Polymorphic Handling")
    class PolymorphicHandling {
        
        @ParameterizedTest
        @MethodSource("provideActionConfigs")
        @DisplayName("Should handle all ActionConfig subclasses")
        public void testPolymorphicSerialization(ActionConfig config, Class<?> expectedType) 
                throws Exception {
            // When
            String json = actionConfigJsonUtils.toJson(config);
            ActionConfig deserialized = actionConfigJsonUtils.fromJson(json);
            
            // Then
            assertNotNull(json);
            assertNotNull(deserialized);
            assertEquals(expectedType, deserialized.getClass());
        }
        
        private static Stream<Arguments> provideActionConfigs() {
            return Stream.of(
                Arguments.of(new ClickOptions.Builder().build(), ClickOptions.class),
                Arguments.of(new PatternFindOptions.Builder().build(), PatternFindOptions.class),
                Arguments.of(new TypeOptions.Builder().build(), TypeOptions.class),
                Arguments.of(new MouseMoveOptions.Builder().build(), MouseMoveOptions.class),
                Arguments.of(new DragOptions.Builder().build(), DragOptions.class)
            );
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle ActionConfig with all default values")
        public void testDefaultValues() throws Exception {
            // Given
            ClickOptions defaultOptions = new ClickOptions.Builder().build();
            
            // When
            String json = actionConfigJsonUtils.toJson(defaultOptions);
            ClickOptions deserialized = (ClickOptions) actionConfigJsonUtils.fromJson(json);
            
            // Then
            assertNotNull(deserialized);
            assertEquals(defaultOptions.getNumberOfClicks(), deserialized.getNumberOfClicks());
            assertEquals(defaultOptions.getPauseBeforeBegin(), 
                        deserialized.getPauseBeforeBegin(), 0.001);
        }
        
        @Test
        @DisplayName("Should handle ActionConfig with complex nested options")
        public void testComplexNestedOptions() throws Exception {
            // Given
            ClickOptions complex = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .setPressOptions(MousePressOptions.builder()
                    .setButton(MouseButton.MIDDLE)
                    .setPauseBeforeMouseDown(0.5)
                    .setPauseAfterMouseDown(0.6)
                    .setPauseBeforeMouseUp(0.7)
                    .setPauseAfterMouseUp(0.8)
                    .build())
                .setPauseBeforeBegin(1.0)
                .setPauseAfterEnd(2.0)
                .build();
            
            // When
            String json = actionConfigJsonUtils.toJson(complex);
            ClickOptions deserialized = (ClickOptions) actionConfigJsonUtils.fromJson(json);
            
            // Then
            assertNotNull(deserialized);
            assertEquals(complex.getNumberOfClicks(), deserialized.getNumberOfClicks());
            assertEquals(MouseButton.MIDDLE, 
                        deserialized.getMousePressOptions().getButton());
            assertEquals(0.5, deserialized.getMousePressOptions().getPauseBeforeMouseDown(), 0.001);
        }
        
        @Test
        @DisplayName("Should handle very large pause values")
        public void testLargePauseValues() throws Exception {
            // Given
            TypeOptions largeValues = new TypeOptions.Builder()
                .setTypeDelay(1000.0)
                .setPauseBeforeBegin(Double.MAX_VALUE)
                .build();
            
            // When
            String json = actionConfigJsonUtils.toJson(largeValues);
            TypeOptions deserialized = (TypeOptions) actionConfigJsonUtils.fromJson(json);
            
            // Then
            assertEquals(1000.0, deserialized.getTypeDelay(), 0.001);
            assertEquals(Double.MAX_VALUE, deserialized.getPauseBeforeBegin(), 0.001);
        }
        
        @Test
        @DisplayName("Should handle negative pause values")
        public void testNegativePauseValues() throws Exception {
            // Given - Negative values might be used for special behaviors
            MouseMoveOptions negativeValues = new MouseMoveOptions.Builder()
                .setMoveMouseDelay(-1.0f)
                .build();
            
            // When
            String json = actionConfigJsonUtils.toJson(negativeValues);
            MouseMoveOptions deserialized = (MouseMoveOptions) actionConfigJsonUtils.fromJson(json);
            
            // Then
            assertEquals(-1.0f, deserialized.getMoveMouseDelay(), 0.001);
        }
    }
}