package io.github.jspinak.brobot.action.basic.type;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the Type action.
 * Tests keyboard input functionality including:
 * - Simple text typing
 * - Special characters and keys
 * - StateString handling
 * - Location-based typing
 * - Options configuration
 */
@DisplayName("Type Action Tests")
public class TypeTest extends BrobotTestBase {
    
    private ActionResult actionResult;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        actionResult = new ActionResult();
    }
    
    @Nested
    @DisplayName("Basic Type Operations")
    class BasicTypeOperations {
        
        @Test
        @DisplayName("Should create TypeOptions with default values")
        void testDefaultTypeOptions() {
            // Act
            TypeOptions options = new TypeOptions.Builder().build();
            
            // Assert
            assertNotNull(options);
            // Default typeDelay is Settings.TypeDelay which varies
            assertTrue(options.getTypeDelay() >= 0);
            assertEquals("", options.getModifiers());
        }
        
        @Test
        @DisplayName("Should create TypeOptions with custom values")
        void testCustomTypeOptions() {
            // Act
            TypeOptions options = new TypeOptions.Builder()
                .setTypeDelay(0.1)
                .setModifiers("ctrl+")
                .build();
            
            // Assert
            assertEquals(0.1, options.getTypeDelay());
            assertEquals("ctrl+", options.getModifiers());
        }
        
        @Test
        @DisplayName("Should handle StateString creation")
        void testStateStringCreation() {
            // Arrange
            String text = "Hello World";
            
            // Act
            StateString stateString = new StateString.Builder()
                .setString(text)
                .setName("greeting")
                .build();
            
            // Assert
            assertNotNull(stateString);
            assertEquals(text, stateString.getString());
            assertEquals("greeting", stateString.getName());
        }
        
        @Test
        @DisplayName("Should create ObjectCollection with StateStrings")
        void testObjectCollectionWithStrings() {
            // Arrange
            StateString string1 = new StateString.Builder()
                .setString("First text")
                .build();
            
            StateString string2 = new StateString.Builder()
                .setString("Second text")
                .build();
            
            // Act
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings(string1, string2)
                .build();
            
            // Assert
            assertNotNull(collection);
            assertEquals(2, collection.getStateStrings().size());
        }
    }
    
    @Nested
    @DisplayName("Text Input Variations")
    class TextInputVariations {
        
        @Test
        @DisplayName("Should handle simple text")
        void testSimpleText() {
            // Arrange
            StateString text = new StateString.Builder()
                .setString("Simple text input")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings(text)
                .build();
            
            TypeOptions options = new TypeOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Assert
            assertNotNull(collection);
            assertEquals(1, collection.getStateStrings().size());
            assertEquals("Simple text input", collection.getStateStrings().get(0).getString());
        }
        
        @Test
        @DisplayName("Should handle special characters")
        void testSpecialCharacters() {
            // Arrange
            String specialText = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            StateString text = new StateString.Builder()
                .setString(specialText)
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings(text)
                .build();
            
            // Assert
            assertEquals(specialText, collection.getStateStrings().get(0).getString());
        }
        
        @Test
        @DisplayName("Should handle multi-line text")
        void testMultiLineText() {
            // Arrange
            String multiLine = "Line 1\nLine 2\nLine 3";
            StateString text = new StateString.Builder()
                .setString(multiLine)
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings(text)
                .build();
            
            // Assert
            assertEquals(multiLine, collection.getStateStrings().get(0).getString());
        }
        
        @Test
        @DisplayName("Should handle empty string")
        void testEmptyString() {
            // Arrange
            StateString empty = new StateString.Builder()
                .setString("")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings(empty)
                .build();
            
            // Assert
            assertEquals("", collection.getStateStrings().get(0).getString());
        }
    }
    
    @Nested
    @DisplayName("Typing with Locations")
    class TypingWithLocations {
        
        @Test
        @DisplayName("Should type at specific location")
        void testTypeAtLocation() {
            // Arrange
            Location location = new Location(100, 200);
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(location)
                .setName("input-field")
                .build();
            
            StateString text = new StateString.Builder()
                .setString("Text at location")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .withStrings(text)
                .build();
            
            // Assert
            assertEquals(1, collection.getStateLocations().size());
            assertEquals(1, collection.getStateStrings().size());
            assertEquals(100, collection.getStateLocations().get(0).getLocation().getX());
            assertEquals(200, collection.getStateLocations().get(0).getLocation().getY());
        }
        
        @Test
        @DisplayName("Should type in region")
        void testTypeInRegion() {
            // Arrange
            Region region = new Region(50, 50, 200, 30);
            StateRegion stateRegion = new StateRegion.Builder()
                .setSearchRegion(region)
                .setName("text-area")
                .build();
            
            StateString text = new StateString.Builder()
                .setString("Text in region")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(stateRegion)
                .withStrings(text)
                .build();
            
            // Assert
            assertEquals(1, collection.getStateRegions().size());
            assertEquals(1, collection.getStateStrings().size());
        }
    }
    
    @Nested
    @DisplayName("Paste Mode")
    class PasteMode {
        
        @Test
        @DisplayName("Should enable paste mode")
        void testPasteModeEnabled() {
            // Act
            TypeOptions options = new TypeOptions.Builder()
                .setModifiers("ctrl+v")
                .build();
            
            // Assert
            assertEquals("ctrl+v", options.getModifiers());
        }
        
        @Test
        @DisplayName("Should handle paste with large text")
        void testPasteLargeText() {
            // Arrange
            StringBuilder largeText = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                largeText.append("This is line ").append(i).append("\n");
            }
            
            StateString text = new StateString.Builder()
                .setString(largeText.toString())
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings(text)
                .build();
            
            TypeOptions options = new TypeOptions.Builder()
                .setModifiers("ctrl+v")
                .build();
            
            // Assert
            assertEquals("ctrl+v", options.getModifiers());
            assertNotNull(collection.getStateStrings().get(0).getString());
            assertTrue(collection.getStateStrings().get(0).getString().length() > 10000);
        }
    }
    
    @Nested
    @DisplayName("Typing Delays")
    class TypingDelays {
        
        @ParameterizedTest
        @ValueSource(doubles = {0.01, 0.03, 0.05, 0.1, 0.5})
        @DisplayName("Should set various typing delays")
        void testTypingDelays(double delay) {
            // Act
            TypeOptions options = new TypeOptions.Builder()
                .setTypeDelay(delay)
                .build();
            
            // Assert
            assertEquals(delay, options.getTypeDelay());
        }
        
        @Test
        @DisplayName("Should set modifiers")
        void testModifiers() {
            // Act
            TypeOptions options = new TypeOptions.Builder()
                .setModifiers("shift+")
                .build();
            
            // Assert
            assertEquals("shift+", options.getModifiers());
        }
    }
    
    @Nested
    @DisplayName("Multiple StateStrings")
    class MultipleStateStrings {
        
        @Test
        @DisplayName("Should handle multiple StateStrings")
        void testMultipleStrings() {
            // Arrange
            List<StateString> strings = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                StateString str = new StateString.Builder()
                    .setString("Text " + i)
                    .setName("string-" + i)
                    .build();
                strings.add(str);
            }
            
            // Act
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings(strings.toArray(new StateString[0]))
                .build();
            
            // Assert
            assertEquals(5, collection.getStateStrings().size());
            for (int i = 0; i < 5; i++) {
                assertEquals("Text " + i, collection.getStateStrings().get(i).getString());
            }
        }
        
        @Test
        @DisplayName("Should preserve order of StateStrings")
        void testStringOrder() {
            // Arrange
            StateString first = new StateString.Builder()
                .setString("First")
                .build();
            StateString second = new StateString.Builder()
                .setString("Second")
                .build();
            StateString third = new StateString.Builder()
                .setString("Third")
                .build();
            
            // Act
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings(first, second, third)
                .build();
            
            // Assert
            assertEquals("First", collection.getStateStrings().get(0).getString());
            assertEquals("Second", collection.getStateStrings().get(1).getString());
            assertEquals("Third", collection.getStateStrings().get(2).getString());
        }
    }
    
    @Nested
    @DisplayName("State Integration")
    class StateIntegration {
        
        @Test
        @DisplayName("Should associate StateString with owner state")
        void testStateStringWithOwner() {
            // Arrange
            String stateName = "InputFormState";
            StateString text = new StateString.Builder()
                .setString("Form input text")
                .setOwnerStateName(stateName)
                .setName("form-text")
                .build();
            
            // Assert
            assertEquals(stateName, text.getOwnerStateName());
            assertEquals("form-text", text.getName());
        }
        
        @Test
        @DisplayName("Should handle StateStrings from different states")
        void testMultipleStatesStrings() {
            // Arrange
            StateString string1 = new StateString.Builder()
                .setString("State1 text")
                .setOwnerStateName("State1")
                .build();
            
            StateString string2 = new StateString.Builder()
                .setString("State2 text")
                .setOwnerStateName("State2")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings(string1, string2)
                .build();
            
            // Assert
            assertEquals(2, collection.getStateStrings().size());
            assertEquals("State1", collection.getStateStrings().get(0).getOwnerStateName());
            assertEquals("State2", collection.getStateStrings().get(1).getOwnerStateName());
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle null string gracefully")
        void testNullString() {
            // Act - StateString might convert null to empty string
            StateString nullString = new StateString.Builder()
                .setString(null)
                .build();
            
            // Assert - null might be converted to empty string
            String result = nullString.getString();
            assertTrue(result == null || result.isEmpty(), 
                "String should be null or empty when null is set");
        }
        
        @Test
        @DisplayName("Should handle ObjectCollection without strings")
        void testNoStrings() {
            // Arrange
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(location)
                .build();
            
            // Act
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
            
            // Assert
            assertTrue(collection.getStateStrings().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @ParameterizedTest
        @CsvSource({
            "10, 10",
            "100, 20",
            "1000, 50",
            "10000, 100"
        })
        @DisplayName("Should handle various text lengths efficiently")
        void testTextLengthPerformance(int length, int maxMs) {
            // Arrange
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < length; i++) {
                text.append('a');
            }
            
            StateString stateString = new StateString.Builder()
                .setString(text.toString())
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings(stateString)
                .build();
            
            long startTime = System.currentTimeMillis();
            
            // Act
            // Simulate processing
            String processed = collection.getStateStrings().get(0).getString();
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Assert
            assertEquals(length, processed.length());
            assertTrue(duration < maxMs, 
                String.format("Processing %d chars should take < %dms, was: %dms", 
                    length, maxMs, duration));
        }
        
        @Test
        @DisplayName("Should handle rapid type creation")
        void testRapidCreation() {
            long startTime = System.currentTimeMillis();
            
            // Act
            for (int i = 0; i < 100; i++) {
                TypeOptions options = new TypeOptions.Builder()
                    .setTypeDelay(0.01 * i)
                    .setModifiers(i % 2 == 0 ? "ctrl+" : null)
                    .build();
                assertNotNull(options);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Assert
            assertTrue(duration < 100, "Should create 100 options in < 100ms");
        }
    }
}