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
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.EmptySource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Comprehensive tests for the SearchRegionOnObject class which configures 
 * search regions derived from other state objects' matches.
 */
@DisplayName("SearchRegionOnObject Configuration Tests")
public class SearchRegionOnObjectTest extends BrobotTestBase {

    private SearchRegionOnObject searchRegionOnObject;
    private MatchAdjustmentOptions testAdjustments;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        testAdjustments = MatchAdjustmentOptions.builder()
            .setAddX(10)
            .setAddY(-20)
            .build();
    }

    @Test
    @DisplayName("Should create with builder using all properties")
    void testBuilderWithAllProperties() {
        // When
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setTargetType(StateObject.Type.IMAGE)
            .setTargetStateName("MainMenu")
            .setTargetObjectName("SubmitButton")
            .setAdjustments(testAdjustments)
            .build();
        
        // Then
        assertEquals(StateObject.Type.IMAGE, searchRegionOnObject.getTargetType());
        assertEquals("MainMenu", searchRegionOnObject.getTargetStateName());
        assertEquals("SubmitButton", searchRegionOnObject.getTargetObjectName());
        assertSame(testAdjustments, searchRegionOnObject.getAdjustments());
    }

    @Test
    @DisplayName("Should create with builder using minimal properties")
    void testBuilderMinimalProperties() {
        // When
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setTargetObjectName("MinimalObject")
            .build();
        
        // Then
        assertNull(searchRegionOnObject.getTargetType());
        assertNull(searchRegionOnObject.getTargetStateName());
        assertEquals("MinimalObject", searchRegionOnObject.getTargetObjectName());
        assertNull(searchRegionOnObject.getAdjustments());
    }

    @Test
    @DisplayName("Should create with empty builder")
    void testEmptyBuilder() {
        // When
        searchRegionOnObject = SearchRegionOnObject.builder().build();
        
        // Then
        assertNull(searchRegionOnObject.getTargetType());
        assertNull(searchRegionOnObject.getTargetStateName());
        assertNull(searchRegionOnObject.getTargetObjectName());
        assertNull(searchRegionOnObject.getAdjustments());
    }

    @ParameterizedTest
    @EnumSource(StateObject.Type.class)
    @DisplayName("Should handle all StateObject types")
    void testAllStateObjectTypes(StateObject.Type type) {
        // When
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setTargetType(type)
            .build();
        
        // Then
        assertEquals(type, searchRegionOnObject.getTargetType());
    }

    @Test
    @DisplayName("Should get and set target type")
    void testGetSetTargetType() {
        // Given
        searchRegionOnObject = SearchRegionOnObject.builder().build();
        
        // When
        searchRegionOnObject.setTargetType(StateObject.Type.REGION);
        
        // Then
        assertEquals(StateObject.Type.REGION, searchRegionOnObject.getTargetType());
    }

    @Test
    @DisplayName("Should get and set target state name")
    void testGetSetTargetStateName() {
        // Given
        searchRegionOnObject = SearchRegionOnObject.builder().build();
        
        // When
        searchRegionOnObject.setTargetStateName("LoginPage");
        
        // Then
        assertEquals("LoginPage", searchRegionOnObject.getTargetStateName());
    }

    @Test
    @DisplayName("Should get and set target object name")
    void testGetSetTargetObjectName() {
        // Given
        searchRegionOnObject = SearchRegionOnObject.builder().build();
        
        // When
        searchRegionOnObject.setTargetObjectName("UsernameField");
        
        // Then
        assertEquals("UsernameField", searchRegionOnObject.getTargetObjectName());
    }

    @Test
    @DisplayName("Should get and set adjustments")
    void testGetSetAdjustments() {
        // Given
        searchRegionOnObject = SearchRegionOnObject.builder().build();
        
        // When
        searchRegionOnObject.setAdjustments(testAdjustments);
        
        // Then
        assertSame(testAdjustments, searchRegionOnObject.getAdjustments());
        assertEquals(10, searchRegionOnObject.getAdjustments().getAddX());
        assertEquals(-20, searchRegionOnObject.getAdjustments().getAddY());
    }

    @Test
    @DisplayName("Should support toBuilder pattern")
    void testToBuilderPattern() {
        // Given
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setTargetType(StateObject.Type.IMAGE)
            .setTargetStateName("OriginalState")
            .setTargetObjectName("OriginalObject")
            .setAdjustments(testAdjustments)
            .build();
        
        // When - Create modified copy
        SearchRegionOnObject modified = searchRegionOnObject.toBuilder()
            .setTargetStateName("ModifiedState")
            .build();
        
        // Then - Original unchanged
        assertEquals("OriginalState", searchRegionOnObject.getTargetStateName());
        
        // Modified has new value but preserves other properties
        assertEquals("ModifiedState", modified.getTargetStateName());
        assertEquals(StateObject.Type.IMAGE, modified.getTargetType());
        assertEquals("OriginalObject", modified.getTargetObjectName());
        assertSame(testAdjustments, modified.getAdjustments());
    }

    @ParameterizedTest
    @CsvSource({
        "MainMenu,FileButton,IMAGE",
        "Dialog,OKButton,LOCATION",
        "SearchResults,FirstResult,REGION",
        "Settings,CheckBox,STRING",
        "HelpScreen,CloseButton,TEXT"
    })
    @DisplayName("Should handle various target configurations")
    void testVariousTargetConfigurations(String stateName, String objectName, String typeName) {
        // Given
        StateObject.Type type = StateObject.Type.valueOf(typeName);
        
        // When
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setTargetStateName(stateName)
            .setTargetObjectName(objectName)
            .setTargetType(type)
            .build();
        
        // Then
        assertEquals(stateName, searchRegionOnObject.getTargetStateName());
        assertEquals(objectName, searchRegionOnObject.getTargetObjectName());
        assertEquals(type, searchRegionOnObject.getTargetType());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @DisplayName("Should handle null and empty state names")
    void testNullAndEmptyStateNames(String stateName) {
        // When
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setTargetStateName(stateName)
            .build();
        
        // Then
        assertEquals(stateName, searchRegionOnObject.getTargetStateName());
    }

    @Test
    @DisplayName("Should handle complex adjustments")
    void testComplexAdjustments() {
        // Given
        MatchAdjustmentOptions complexAdjustments = MatchAdjustmentOptions.builder()
            .setAddX(100)
            .setAddY(-50)
            .setAddW(200)
            .setAddH(150)
            .build();
        
        // When
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setAdjustments(complexAdjustments)
            .build();
        
        // Then
        assertEquals(100, searchRegionOnObject.getAdjustments().getAddX());
        assertEquals(-50, searchRegionOnObject.getAdjustments().getAddY());
        assertEquals(200, searchRegionOnObject.getAdjustments().getAddW());
        assertEquals(150, searchRegionOnObject.getAdjustments().getAddH());
    }

    @TestFactory
    @DisplayName("Dynamic search region scenarios")
    Stream<DynamicTest> testDynamicSearchRegionScenarios() {
        return Stream.of(
            dynamicTest("Search relative to menu item", () -> {
                SearchRegionOnObject config = SearchRegionOnObject.builder()
                    .setTargetStateName("MainMenu")
                    .setTargetObjectName("FileMenu")
                    .setTargetType(StateObject.Type.IMAGE)
                    .setAdjustments(MatchAdjustmentOptions.builder()
                        .setAddY(30)  // Below menu
                        .setAddH(200) // Dropdown height
                        .build())
                    .build();
                
                assertEquals("MainMenu", config.getTargetStateName());
                assertEquals(30, config.getAdjustments().getAddY());
            }),
            
            dynamicTest("Search in dialog content area", () -> {
                SearchRegionOnObject config = SearchRegionOnObject.builder()
                    .setTargetStateName("Dialog")
                    .setTargetObjectName("DialogFrame")
                    .setTargetType(StateObject.Type.REGION)
                    .setAdjustments(MatchAdjustmentOptions.builder()
                        .setAddX(10)   // Inset from frame
                        .setAddY(40)   // Below title bar
                        .setAddW(-20)  // Account for borders
                        .setAddH(-60)  // Remove title and button areas
                        .build())
                    .build();
                
                assertEquals("Dialog", config.getTargetStateName());
                assertEquals(-20, config.getAdjustments().getAddW());
            }),
            
            dynamicTest("Search adjacent to found element", () -> {
                SearchRegionOnObject config = SearchRegionOnObject.builder()
                    .setTargetStateName("Form")
                    .setTargetObjectName("Label")
                    .setTargetType(StateObject.Type.STRING)
                    .setAdjustments(MatchAdjustmentOptions.builder()
                        .setAddX(100)  // Right of label
                        .setAddW(200)  // Input field width
                        .build())
                    .build();
                
                assertEquals("Label", config.getTargetObjectName());
                assertEquals(100, config.getAdjustments().getAddX());
            })
        );
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Given
        SearchRegionOnObject config1 = SearchRegionOnObject.builder()
            .setTargetType(StateObject.Type.IMAGE)
            .setTargetStateName("State1")
            .setTargetObjectName("Object1")
            .setAdjustments(testAdjustments)
            .build();
            
        SearchRegionOnObject config2 = SearchRegionOnObject.builder()
            .setTargetType(StateObject.Type.IMAGE)
            .setTargetStateName("State1")
            .setTargetObjectName("Object1")
            .setAdjustments(testAdjustments)
            .build();
            
        SearchRegionOnObject config3 = SearchRegionOnObject.builder()
            .setTargetType(StateObject.Type.REGION)
            .setTargetStateName("State1")
            .setTargetObjectName("Object1")
            .setAdjustments(testAdjustments)
            .build();
        
        // Then - Reflexive
        assertEquals(config1, config1);
        assertEquals(config1.hashCode(), config1.hashCode());
        
        // Symmetric
        assertEquals(config1, config2);
        assertEquals(config2, config1);
        assertEquals(config1.hashCode(), config2.hashCode());
        
        // Different type
        assertNotEquals(config1, config3);
        
        // Null safety
        assertNotEquals(config1, null);
        assertNotEquals(config1, "not a SearchRegionOnObject");
    }

    @Test
    @DisplayName("Should provide meaningful toString")
    void testToString() {
        // Given
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setTargetType(StateObject.Type.IMAGE)
            .setTargetStateName("TestState")
            .setTargetObjectName("TestObject")
            .build();
        
        // When
        String toString = searchRegionOnObject.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("IMAGE"));
        assertTrue(toString.contains("TestState"));
        assertTrue(toString.contains("TestObject"));
    }

    @Test
    @DisplayName("Should handle cross-state references")
    void testCrossStateReferences() {
        // Given - Reference to object in different state
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setTargetStateName("ParentState")
            .setTargetObjectName("NavigationBar")
            .setTargetType(StateObject.Type.REGION)
            .build();
        
        // Then - Can reference objects from other states
        assertEquals("ParentState", searchRegionOnObject.getTargetStateName());
        assertNotEquals("CurrentState", searchRegionOnObject.getTargetStateName());
    }

    @Test
    @DisplayName("Should support same-state references")
    void testSameStateReferences() {
        // Given - Reference to object in same state (null state name)
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setTargetStateName(null) // Same state
            .setTargetObjectName("LocalObject")
            .setTargetType(StateObject.Type.LOCATION)
            .build();
        
        // Then
        assertNull(searchRegionOnObject.getTargetStateName());
        assertEquals("LocalObject", searchRegionOnObject.getTargetObjectName());
    }

    @Test
    @DisplayName("Should handle builder method chaining")
    void testBuilderMethodChaining() {
        // When - All builder methods should return builder
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setTargetType(StateObject.Type.IMAGE)
            .setTargetStateName("ChainedState")
            .setTargetObjectName("ChainedObject")
            .setAdjustments(testAdjustments)
            .build();
        
        // Then
        assertNotNull(searchRegionOnObject);
        assertEquals("ChainedState", searchRegionOnObject.getTargetStateName());
        assertEquals("ChainedObject", searchRegionOnObject.getTargetObjectName());
    }

    @Test
    @DisplayName("Should handle null adjustments")
    void testNullAdjustments() {
        // Given
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setTargetObjectName("TestObject")
            .setAdjustments(null)
            .build();
        
        // Then
        assertNull(searchRegionOnObject.getAdjustments());
    }

    @Test
    @DisplayName("Should maintain immutability of adjustments reference")
    void testAdjustmentsReferenceImmutability() {
        // Given
        MatchAdjustmentOptions original = MatchAdjustmentOptions.builder()
            .setAddX(10)
            .build();
            
        searchRegionOnObject = SearchRegionOnObject.builder()
            .setAdjustments(original)
            .build();
        
        // When - Modify through setter
        MatchAdjustmentOptions modified = MatchAdjustmentOptions.builder()
            .setAddX(20)
            .build();
        searchRegionOnObject.setAdjustments(modified);
        
        // Then - Original unchanged
        assertEquals(10, original.getAddX());
        assertEquals(20, searchRegionOnObject.getAdjustments().getAddX());
    }
}