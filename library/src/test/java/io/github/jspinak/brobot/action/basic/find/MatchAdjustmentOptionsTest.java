package io.github.jspinak.brobot.action.basic.find;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for MatchAdjustmentOptions.
 * Tests configuration for adjusting match positions and dimensions.
 */
@DisplayName("MatchAdjustmentOptions Tests")
public class MatchAdjustmentOptionsTest extends BrobotTestBase {
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        objectMapper = new ObjectMapper();
    }
    
    @Nested
    @DisplayName("Default Configuration")
    class DefaultConfiguration {
        
        @Test
        @DisplayName("Default builder creates valid configuration")
        public void testDefaultBuilder() {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder().build();
            
            assertNotNull(options);
            assertNull(options.getTargetPosition());
            assertNull(options.getTargetOffset());
            assertEquals(0, options.getAddW());
            assertEquals(0, options.getAddH());
            assertEquals(-1, options.getAbsoluteW());
            assertEquals(-1, options.getAbsoluteH());
            assertEquals(0, options.getAddX());
            assertEquals(0, options.getAddY());
        }
        
        @Test
        @DisplayName("Default values mean no adjustment")
        public void testDefaultNoAdjustment() {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder().build();
            
            // All adjustments are zero or disabled
            assertEquals(0, options.getAddW());
            assertEquals(0, options.getAddH());
            assertEquals(0, options.getAddX());
            assertEquals(0, options.getAddY());
            assertTrue(options.getAbsoluteW() < 0); // Disabled
            assertTrue(options.getAbsoluteH() < 0); // Disabled
        }
    }
    
    @Nested
    @DisplayName("Target Position Configuration")
    class TargetPositionConfiguration {
        
        @Test
        @DisplayName("Set target position to center")
        public void testCenterPosition() {
            Position center = new Position(50, 50); // Center
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setTargetPosition(center)
                .build();
            
            assertEquals(center, options.getTargetPosition());
        }
        
        @Test
        @DisplayName("Set target position to corner")
        public void testCornerPosition() {
            Position topLeft = new Position(0, 0);
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setTargetPosition(topLeft)
                .build();
            
            assertEquals(topLeft, options.getTargetPosition());
        }
        
        @Test
        @DisplayName("Null target position is allowed")
        public void testNullTargetPosition() {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setTargetPosition(null)
                .build();
            
            assertNull(options.getTargetPosition());
        }
    }
    
    @Nested
    @DisplayName("Target Offset Configuration")
    class TargetOffsetConfiguration {
        
        @Test
        @DisplayName("Set positive target offset")
        public void testPositiveOffset() {
            Location offset = new Location(10, 20);
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setTargetOffset(offset)
                .build();
            
            assertEquals(offset, options.getTargetOffset());
        }
        
        @Test
        @DisplayName("Set negative target offset")
        public void testNegativeOffset() {
            Location offset = new Location(-15, -25);
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setTargetOffset(offset)
                .build();
            
            assertEquals(offset, options.getTargetOffset());
        }
        
        @Test
        @DisplayName("Set zero target offset")
        public void testZeroOffset() {
            Location offset = new Location(0, 0);
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setTargetOffset(offset)
                .build();
            
            assertEquals(offset, options.getTargetOffset());
        }
    }
    
    @Nested
    @DisplayName("Size Adjustment Configuration")
    class SizeAdjustmentConfiguration {
        
        @Test
        @DisplayName("Add to width and height")
        public void testAddToSize() {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddW(20)
                .setAddH(15)
                .build();
            
            assertEquals(20, options.getAddW());
            assertEquals(15, options.getAddH());
        }
        
        @Test
        @DisplayName("Subtract from width and height")
        public void testSubtractFromSize() {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddW(-10)
                .setAddH(-5)
                .build();
            
            assertEquals(-10, options.getAddW());
            assertEquals(-5, options.getAddH());
        }
        
        @ParameterizedTest
        @ValueSource(ints = {-100, -10, 0, 10, 50, 100})
        @DisplayName("Various size adjustments")
        public void testVariousSizeAdjustments(int adjustment) {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddW(adjustment)
                .setAddH(adjustment)
                .build();
            
            assertEquals(adjustment, options.getAddW());
            assertEquals(adjustment, options.getAddH());
        }
        
        @Test
        @DisplayName("Set absolute width and height")
        public void testAbsoluteSize() {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAbsoluteW(200)
                .setAbsoluteH(150)
                .build();
            
            assertEquals(200, options.getAbsoluteW());
            assertEquals(150, options.getAbsoluteH());
        }
        
        @Test
        @DisplayName("Disable absolute size with negative values")
        public void testDisableAbsoluteSize() {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAbsoluteW(-1)
                .setAbsoluteH(-1)
                .build();
            
            assertEquals(-1, options.getAbsoluteW());
            assertEquals(-1, options.getAbsoluteH());
        }
    }
    
    @Nested
    @DisplayName("Position Adjustment Configuration")
    class PositionAdjustmentConfiguration {
        
        @Test
        @DisplayName("Add to X and Y coordinates")
        public void testAddToPosition() {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddX(30)
                .setAddY(40)
                .build();
            
            assertEquals(30, options.getAddX());
            assertEquals(40, options.getAddY());
        }
        
        @Test
        @DisplayName("Subtract from X and Y coordinates")
        public void testSubtractFromPosition() {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddX(-20)
                .setAddY(-30)
                .build();
            
            assertEquals(-20, options.getAddX());
            assertEquals(-30, options.getAddY());
        }
        
        @ParameterizedTest
        @CsvSource({
            "0, 0",
            "10, 20",
            "-10, -20",
            "100, -100",
            "-50, 50"
        })
        @DisplayName("Various position adjustments")
        public void testVariousPositionAdjustments(int x, int y) {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddX(x)
                .setAddY(y)
                .build();
            
            assertEquals(x, options.getAddX());
            assertEquals(y, options.getAddY());
        }
    }
    
    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {
        
        @Test
        @DisplayName("Builder chaining works correctly")
        public void testBuilderChaining() {
            Location offset = new Location(5, 5);
            Position position = new Position(25, 25);
            
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setTargetPosition(position)
                .setTargetOffset(offset)
                .setAddW(10)
                .setAddH(15)
                .setAbsoluteW(300)
                .setAbsoluteH(200)
                .setAddX(20)
                .setAddY(25)
                .build();
            
            assertNotNull(options);
            assertEquals(position, options.getTargetPosition());
            assertEquals(offset, options.getTargetOffset());
            assertEquals(10, options.getAddW());
            assertEquals(15, options.getAddH());
            assertEquals(300, options.getAbsoluteW());
            assertEquals(200, options.getAbsoluteH());
            assertEquals(20, options.getAddX());
            assertEquals(25, options.getAddY());
        }
        
        @Test
        @DisplayName("toBuilder creates modifiable copy")
        public void testToBuilder() {
            MatchAdjustmentOptions original = MatchAdjustmentOptions.builder()
                .setAddW(10)
                .setAddH(20)
                .build();
            
            MatchAdjustmentOptions modified = original.toBuilder()
                .setAddX(30)
                .setAddY(40)
                .build();
            
            // Original unchanged
            assertEquals(0, original.getAddX());
            assertEquals(0, original.getAddY());
            
            // Modified has new values
            assertEquals(30, modified.getAddX());
            assertEquals(40, modified.getAddY());
            
            // Other values preserved
            assertEquals(original.getAddW(), modified.getAddW());
            assertEquals(original.getAddH(), modified.getAddH());
        }
    }
    
    @Nested
    @DisplayName("JSON Serialization")
    class JsonSerialization {
        
        @Test
        @DisplayName("Serialize and deserialize with default values")
        public void testDefaultSerialization() throws JsonProcessingException {
            MatchAdjustmentOptions original = MatchAdjustmentOptions.builder().build();
            
            String json = objectMapper.writeValueAsString(original);
            assertNotNull(json);
            
            MatchAdjustmentOptions deserialized = objectMapper.readValue(json, MatchAdjustmentOptions.class);
            assertNotNull(deserialized);
            assertEquals(original.getAddW(), deserialized.getAddW());
            assertEquals(original.getAddH(), deserialized.getAddH());
        }
        
        @Test
        @DisplayName("Serialize and deserialize with custom values")
        public void testCustomSerialization() throws JsonProcessingException {
            Location offset = new Location(10, 15);
            Position position = new Position(30, 40);
            
            MatchAdjustmentOptions original = MatchAdjustmentOptions.builder()
                .setTargetPosition(position)
                .setTargetOffset(offset)
                .setAddW(20)
                .setAddH(25)
                .setAbsoluteW(400)
                .setAbsoluteH(300)
                .setAddX(35)
                .setAddY(45)
                .build();
            
            String json = objectMapper.writeValueAsString(original);
            MatchAdjustmentOptions deserialized = objectMapper.readValue(json, MatchAdjustmentOptions.class);
            
            assertEquals(original.getTargetPosition(), deserialized.getTargetPosition());
            assertEquals(original.getTargetOffset(), deserialized.getTargetOffset());
            assertEquals(original.getAddW(), deserialized.getAddW());
            assertEquals(original.getAddH(), deserialized.getAddH());
            assertEquals(original.getAbsoluteW(), deserialized.getAbsoluteW());
            assertEquals(original.getAbsoluteH(), deserialized.getAbsoluteH());
            assertEquals(original.getAddX(), deserialized.getAddX());
            assertEquals(original.getAddY(), deserialized.getAddY());
        }
        
        @Test
        @DisplayName("Deserialize with unknown properties")
        public void testDeserializeWithUnknownProperties() throws JsonProcessingException {
            String json = "{\"addW\":10,\"addH\":20,\"unknownField\":\"value\"}";
            
            MatchAdjustmentOptions options = objectMapper.readValue(json, MatchAdjustmentOptions.class);
            assertNotNull(options);
            assertEquals(10, options.getAddW());
            assertEquals(20, options.getAddH());
        }
    }
    
    @Nested
    @DisplayName("Use Cases")
    class UseCases {
        
        @Test
        @DisplayName("Configuration for expanding click area")
        public void testExpandClickArea() {
            // Expand the match region for a larger click target
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddW(20)
                .setAddH(20)
                .setAddX(-10) // Center the expansion
                .setAddY(-10)
                .build();
            
            assertEquals(20, options.getAddW());
            assertEquals(20, options.getAddH());
            assertEquals(-10, options.getAddX());
            assertEquals(-10, options.getAddY());
        }
        
        @Test
        @DisplayName("Configuration for clicking button edge")
        public void testClickButtonEdge() {
            // Click on the right edge of a button
            Position rightEdge = new Position(100, 50); // Right edge, middle
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setTargetPosition(rightEdge)
                .build();
            
            assertEquals(rightEdge, options.getTargetPosition());
        }
        
        @Test
        @DisplayName("Configuration for fixed-size region")
        public void testFixedSizeRegion() {
            // Force all matches to be exactly 100x50 pixels
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAbsoluteW(100)
                .setAbsoluteH(50)
                .build();
            
            assertEquals(100, options.getAbsoluteW());
            assertEquals(50, options.getAbsoluteH());
        }
        
        @Test
        @DisplayName("Configuration for offset clicking")
        public void testOffsetClicking() {
            // Click 10 pixels below and to the right of the match center
            Location offset = new Location(10, 10);
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setTargetOffset(offset)
                .build();
            
            assertEquals(offset, options.getTargetOffset());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Very large adjustments")
        public void testLargeAdjustments() {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddW(Integer.MAX_VALUE)
                .setAddH(Integer.MAX_VALUE)
                .setAddX(Integer.MAX_VALUE)
                .setAddY(Integer.MAX_VALUE)
                .build();
            
            assertEquals(Integer.MAX_VALUE, options.getAddW());
            assertEquals(Integer.MAX_VALUE, options.getAddH());
            assertEquals(Integer.MAX_VALUE, options.getAddX());
            assertEquals(Integer.MAX_VALUE, options.getAddY());
        }
        
        @Test
        @DisplayName("Very small (negative) adjustments")
        public void testSmallAdjustments() {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddW(Integer.MIN_VALUE)
                .setAddH(Integer.MIN_VALUE)
                .setAddX(Integer.MIN_VALUE)
                .setAddY(Integer.MIN_VALUE)
                .build();
            
            assertEquals(Integer.MIN_VALUE, options.getAddW());
            assertEquals(Integer.MIN_VALUE, options.getAddH());
            assertEquals(Integer.MIN_VALUE, options.getAddX());
            assertEquals(Integer.MIN_VALUE, options.getAddY());
        }
        
        @Test
        @DisplayName("Mix of adjustments and absolutes")
        public void testMixedAdjustments() {
            // Both relative and absolute adjustments
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddW(10)     // Relative
                .setAbsoluteH(200) // Absolute
                .build();
            
            assertEquals(10, options.getAddW());
            assertEquals(200, options.getAbsoluteH());
            assertEquals(0, options.getAddH()); // Not affected by absolute
            assertEquals(-1, options.getAbsoluteW()); // Not set
        }
    }
}