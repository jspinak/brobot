package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Comprehensive tests for the CrossStateAnchor class which provides enhanced 
 * anchoring capabilities across different states in the Brobot framework.
 */
@DisplayName("CrossStateAnchor Model Tests")
public class CrossStateAnchorTest extends BrobotTestBase {

    private CrossStateAnchor crossStateAnchor;
    private Region testRegion;
    private Location testLocation;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        crossStateAnchor = new CrossStateAnchor();
        testRegion = new Region(100, 100, 200, 150);
        testLocation = new Location(100, 100);
    }

    @Test
    @DisplayName("Should create CrossStateAnchor with default constructor")
    void testDefaultConstructor() {
        // When
        CrossStateAnchor anchor = new CrossStateAnchor();
        
        // Then
        assertNotNull(anchor);
        assertNotNull(anchor.getAdjustments());
        assertNull(anchor.getSourceStateName());
        assertNull(anchor.getSourceObjectName());
        assertNull(anchor.getSourceType());
        // Default adjustments should be 0,0
        assertEquals(0, anchor.getAdjustments().getAddX());
        assertEquals(0, anchor.getAdjustments().getAddY());
    }

    @Test
    @DisplayName("Should create CrossStateAnchor with position constructor")
    void testPositionConstructor() {
        // When
        CrossStateAnchor anchor = new CrossStateAnchor(
            Positions.Name.TOPLEFT, 
            Positions.Name.BOTTOMRIGHT
        );
        
        // Then
        assertEquals(Positions.Name.TOPLEFT, anchor.getAnchorInNewDefinedRegion());
        assertNotNull(anchor.getPositionInMatch());
        assertEquals(1.0, anchor.getPositionInMatch().getPercentW());
        assertEquals(1.0, anchor.getPositionInMatch().getPercentH());
        assertNotNull(anchor.getAdjustments());
    }

    @Test
    @DisplayName("Should inherit from Anchor class")
    void testInheritance() {
        // Then
        assertTrue(crossStateAnchor instanceof Anchor);
        assertNotNull(crossStateAnchor);
    }

    @Test
    @DisplayName("Should set and get source state name")
    void testSourceStateName() {
        // When
        crossStateAnchor.setSourceStateName("MainMenu");
        
        // Then
        assertEquals("MainMenu", crossStateAnchor.getSourceStateName());
    }

    @Test
    @DisplayName("Should set and get source object name")
    void testSourceObjectName() {
        // When
        crossStateAnchor.setSourceObjectName("SubmitButton");
        
        // Then
        assertEquals("SubmitButton", crossStateAnchor.getSourceObjectName());
    }

    @ParameterizedTest
    @EnumSource(StateObject.Type.class)
    @DisplayName("Should handle all StateObject types")
    void testSourceType(StateObject.Type type) {
        // When
        crossStateAnchor.setSourceType(type);
        
        // Then
        assertEquals(type, crossStateAnchor.getSourceType());
    }

    @Test
    @DisplayName("Should set and get adjustments")
    void testAdjustments() {
        // Given
        MatchAdjustmentOptions adjustments = MatchAdjustmentOptions.builder()
            .setAddX(10)
            .setAddY(-20)
            .build();
        
        // When
        crossStateAnchor.setAdjustments(adjustments);
        
        // Then
        assertSame(adjustments, crossStateAnchor.getAdjustments());
        assertEquals(10, crossStateAnchor.getAdjustments().getAddX());
        assertEquals(-20, crossStateAnchor.getAdjustments().getAddY());
    }

    @ParameterizedTest
    @CsvSource({
        "0.0,0.0,100,100,0,0",      // Top-left, no adjustment
        "1.0,1.0,300,250,0,0",      // Bottom-right, no adjustment
        "0.5,0.5,200,175,0,0",      // Center, no adjustment
        "0.5,0.5,200,175,10,-5",    // Center with adjustments
        "0.0,0.0,100,100,50,50",    // Top-left with positive adjustments
        "1.0,1.0,300,250,-10,-10"   // Bottom-right with negative adjustments
    })
    @DisplayName("Should calculate adjusted location correctly")
    void testGetAdjustedLocation(double percentW, double percentH, 
                                 int expectedX, int expectedY,
                                 int adjustX, int adjustY) {
        // Given
        crossStateAnchor.setPositionInMatch(new Position(percentW, percentH));
        crossStateAnchor.setAdjustments(MatchAdjustmentOptions.builder()
            .setAddX(adjustX)
            .setAddY(adjustY)
            .build());
        
        // When
        Location adjusted = crossStateAnchor.getAdjustedLocation(testLocation, testRegion);
        
        // Then
        assertEquals(expectedX + adjustX, adjusted.getX());
        assertEquals(expectedY + adjustY, adjusted.getY());
    }

    @Test
    @DisplayName("Should build with all properties using Builder")
    void testBuilderWithAllProperties() {
        // When
        CrossStateAnchor anchor = new CrossStateAnchor.Builder()
            .anchorInNewDefinedRegion(Positions.Name.TOPLEFT)
            .positionInMatch(Positions.Name.BOTTOMRIGHT)
            .sourceState("LoginPage")
            .sourceObject("UsernameField")
            .sourceType(StateObject.Type.IMAGE)
            .adjustX(25)
            .adjustY(-15)
            .build();
        
        // Then
        assertEquals(Positions.Name.TOPLEFT, anchor.getAnchorInNewDefinedRegion());
        assertNotNull(anchor.getPositionInMatch());
        assertEquals("LoginPage", anchor.getSourceStateName());
        assertEquals("UsernameField", anchor.getSourceObjectName());
        assertEquals(StateObject.Type.IMAGE, anchor.getSourceType());
        assertEquals(25, anchor.getAdjustments().getAddX());
        assertEquals(-15, anchor.getAdjustments().getAddY());
    }

    @Test
    @DisplayName("Should build with defaults when properties not set")
    void testBuilderWithDefaults() {
        // When
        CrossStateAnchor anchor = new CrossStateAnchor.Builder().build();
        
        // Then - Should have default values
        assertEquals(Positions.Name.MIDDLEMIDDLE, anchor.getAnchorInNewDefinedRegion());
        assertNotNull(anchor.getPositionInMatch());
        assertEquals(0.5, anchor.getPositionInMatch().getPercentW());
        assertEquals(0.5, anchor.getPositionInMatch().getPercentH());
        assertNull(anchor.getSourceStateName());
        assertNull(anchor.getSourceObjectName());
        assertNull(anchor.getSourceType());
    }

    @Test
    @DisplayName("Should update adjustments using Builder")
    void testBuilderAdjustmentMethods() {
        // Test individual adjustment methods
        CrossStateAnchor anchor1 = new CrossStateAnchor.Builder()
            .adjustX(30)
            .adjustY(40)
            .build();
        
        assertEquals(30, anchor1.getAdjustments().getAddX());
        assertEquals(40, anchor1.getAdjustments().getAddY());
        
        // Test combined adjustment method
        CrossStateAnchor anchor2 = new CrossStateAnchor.Builder()
            .adjustments(50, 60)
            .build();
        
        assertEquals(50, anchor2.getAdjustments().getAddX());
        assertEquals(60, anchor2.getAdjustments().getAddY());
    }

    @TestFactory
    @DisplayName("Cross-state anchor usage scenarios")
    Stream<DynamicTest> testCrossStateAnchorScenarios() {
        return Stream.of(
            dynamicTest("Anchor relative to object in parent state", () -> {
                CrossStateAnchor anchor = new CrossStateAnchor.Builder()
                    .sourceState("ParentDialog")
                    .sourceObject("HeaderLabel")
                    .sourceType(StateObject.Type.IMAGE)
                    .anchorInNewDefinedRegion(Positions.Name.TOPMIDDLE)
                    .positionInMatch(Positions.Name.BOTTOMMIDDLE)
                    .adjustY(10)
                    .build();
                
                assertEquals("ParentDialog", anchor.getSourceStateName());
                assertEquals("HeaderLabel", anchor.getSourceObjectName());
                assertEquals(10, anchor.getAdjustments().getAddY());
            }),
            
            dynamicTest("Anchor to menu item from different state", () -> {
                CrossStateAnchor anchor = new CrossStateAnchor.Builder()
                    .sourceState("MainMenu")
                    .sourceObject("FileMenuItem")
                    .sourceType(StateObject.Type.REGION)
                    .anchorInNewDefinedRegion(Positions.Name.TOPLEFT)
                    .positionInMatch(Positions.Name.BOTTOMLEFT)
                    .build();
                
                assertEquals("MainMenu", anchor.getSourceStateName());
                assertEquals(StateObject.Type.REGION, anchor.getSourceType());
            }),
            
            dynamicTest("Dynamic positioning based on previous state", () -> {
                CrossStateAnchor anchor = new CrossStateAnchor.Builder()
                    .sourceState("SearchResults")
                    .sourceObject("FirstResult")
                    .sourceType(StateObject.Type.LOCATION)
                    .positionInMatch(Positions.Name.MIDDLERIGHT)
                    .adjustX(20)
                    .build();
                
                assertEquals("SearchResults", anchor.getSourceStateName());
                assertEquals(20, anchor.getAdjustments().getAddX());
            })
        );
    }

    @Test
    @DisplayName("Should handle edge cases in location adjustment")
    void testLocationAdjustmentEdgeCases() {
        // Set position first
        crossStateAnchor.setPositionInMatch(new Position(0.5, 0.5));
        
        // Test with zero-sized region
        Region zeroRegion = new Region(50, 50, 0, 0);
        Location adjusted = crossStateAnchor.getAdjustedLocation(testLocation, zeroRegion);
        assertEquals(50, adjusted.getX());
        assertEquals(50, adjusted.getY());
        
        // Test with negative adjustments
        crossStateAnchor.setAdjustments(MatchAdjustmentOptions.builder()
            .setAddX(-100)
            .setAddY(-100)
            .build());
        adjusted = crossStateAnchor.getAdjustedLocation(testLocation, testRegion);
        // With 0.5,0.5 position in 100,100,200,150 region:
        // x = 100 + (200 * 0.5) = 200, then -100 = 100
        // y = 100 + (150 * 0.5) = 175, then -100 = 75
        assertEquals(100, adjusted.getX());
        assertEquals(75, adjusted.getY());
    }

    @Test
    @DisplayName("Should maintain anchor semantics from parent class")
    void testParentClassSemantics() {
        // Given
        Position position = new Position(0.25, 0.75);
        crossStateAnchor.setAnchorInNewDefinedRegion(Positions.Name.BOTTOMLEFT);
        crossStateAnchor.setPositionInMatch(position);
        
        // Then - Parent class properties work correctly
        assertEquals(Positions.Name.BOTTOMLEFT, crossStateAnchor.getAnchorInNewDefinedRegion());
        assertEquals(position, crossStateAnchor.getPositionInMatch());
        assertEquals(0.25, crossStateAnchor.getPositionInMatch().getPercentW());
        assertEquals(0.75, crossStateAnchor.getPositionInMatch().getPercentH());
    }

    @Test
    @DisplayName("Should support method chaining in Builder")
    void testBuilderMethodChaining() {
        // When - All methods should return Builder for chaining
        CrossStateAnchor anchor = new CrossStateAnchor.Builder()
            .anchorInNewDefinedRegion(Positions.Name.TOPLEFT)
            .positionInMatch(Positions.Name.TOPRIGHT)
            .sourceState("State1")
            .sourceObject("Object1")
            .sourceType(StateObject.Type.IMAGE)
            .adjustX(10)
            .adjustY(20)
            .adjustments(30, 40) // This overrides previous adjustX/adjustY
            .build();
        
        // Then
        assertNotNull(anchor);
        assertEquals(30, anchor.getAdjustments().getAddX());
        assertEquals(40, anchor.getAdjustments().getAddY());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullHandling() {
        // Given
        crossStateAnchor.setSourceStateName(null);
        crossStateAnchor.setSourceObjectName(null);
        crossStateAnchor.setSourceType(null);
        crossStateAnchor.setAdjustments(null);
        
        // Then
        assertNull(crossStateAnchor.getSourceStateName());
        assertNull(crossStateAnchor.getSourceObjectName());
        assertNull(crossStateAnchor.getSourceType());
        assertNull(crossStateAnchor.getAdjustments());
        
        // getAdjustedLocation should handle null adjustments
        crossStateAnchor.setPositionInMatch(new Position(0.5, 0.5));
        assertThrows(NullPointerException.class, () ->
            crossStateAnchor.getAdjustedLocation(testLocation, testRegion)
        );
    }

    @Test
    @DisplayName("Should validate required fields in Builder")
    void testBuilderValidation() {
        // When - Build without setting any fields
        CrossStateAnchor anchor = new CrossStateAnchor.Builder().build();
        
        // Then - Should have sensible defaults
        assertNotNull(anchor.getAnchorInNewDefinedRegion());
        assertNotNull(anchor.getPositionInMatch());
        assertEquals(Positions.Name.MIDDLEMIDDLE, anchor.getAnchorInNewDefinedRegion());
    }
}