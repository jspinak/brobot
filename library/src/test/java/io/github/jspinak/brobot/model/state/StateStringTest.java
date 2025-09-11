package io.github.jspinak.brobot.model.state;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;

@DisplayName("StateString Tests")
public class StateStringTest extends BrobotTestBase {

    private StateString stateString;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        stateString = new StateString();
    }

    @Nested
    @DisplayName("Basic Properties")
    class BasicProperties {

        @Test
        @DisplayName("Should have correct default values")
        public void testDefaultValues() {
            assertEquals(StateObject.Type.STRING, stateString.getObjectType());
            assertNull(stateString.getName());
            assertNull(stateString.getSearchRegion());
            assertEquals("null", stateString.getOwnerStateName());
            assertNull(stateString.getOwnerStateId());
            assertEquals(0, stateString.getTimesActedOn());
            assertNull(stateString.getString());
        }

        @Test
        @DisplayName("Should set and get basic properties")
        public void testBasicProperties() {
            stateString.setName("TestString");
            stateString.setString("Hello World");
            stateString.setOwnerStateName("TestState");
            stateString.setOwnerStateId(123L);
            stateString.setTimesActedOn(5);

            assertEquals("TestString", stateString.getName());
            assertEquals("Hello World", stateString.getString());
            assertEquals("TestState", stateString.getOwnerStateName());
            assertEquals(123L, stateString.getOwnerStateId());
            assertEquals(5, stateString.getTimesActedOn());
        }

        @Test
        @DisplayName("Should always have STRING object type")
        public void testObjectType() {
            assertEquals(StateObject.Type.STRING, stateString.getObjectType());

            // Even after setting other properties
            stateString.setString("test");
            assertEquals(StateObject.Type.STRING, stateString.getObjectType());
        }
    }

    @Nested
    @DisplayName("String Content")
    class StringContent {

        @Test
        @DisplayName("Should handle normal strings")
        public void testNormalStrings() {
            stateString.setString("Normal text");
            assertEquals("Normal text", stateString.getString());
            assertTrue(stateString.defined());
        }

        @Test
        @DisplayName("Should handle special characters")
        public void testSpecialCharacters() {
            String special = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            stateString.setString(special);
            assertEquals(special, stateString.getString());
            assertTrue(stateString.defined());
        }

        @Test
        @DisplayName("Should handle unicode characters")
        public void testUnicodeCharacters() {
            String unicode = "Hello 世界 🌍 مرحبا";
            stateString.setString(unicode);
            assertEquals(unicode, stateString.getString());
            assertTrue(stateString.defined());
        }

        @Test
        @DisplayName("Should handle multiline strings")
        public void testMultilineStrings() {
            String multiline = "Line 1\nLine 2\nLine 3";
            stateString.setString(multiline);
            assertEquals(multiline, stateString.getString());
            assertTrue(stateString.defined());
        }

        @Test
        @DisplayName("Should handle empty string")
        public void testEmptyString() {
            stateString.setString("");
            assertEquals("", stateString.getString());
            assertFalse(stateString.defined());
        }

        @Test
        @DisplayName("Should handle null string")
        public void testNullString() {
            stateString.setString(null);
            assertNull(stateString.getString());
            assertFalse(stateString.defined());
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "  ", "\t", "\n", " \t\n "})
        @DisplayName("Should handle whitespace strings")
        public void testWhitespaceStrings(String whitespace) {
            stateString.setString(whitespace);
            assertEquals(whitespace, stateString.getString());
            assertTrue(stateString.defined());
        }
    }

    @Nested
    @DisplayName("Search Region")
    class SearchRegionTests {

        @Test
        @DisplayName("Should handle search region")
        public void testSearchRegion() {
            Region region = new Region(10, 20, 100, 50);
            stateString.setSearchRegion(region);

            assertEquals(region, stateString.getSearchRegion());
            assertEquals(10, stateString.getSearchRegion().getX());
            assertEquals(20, stateString.getSearchRegion().getY());
            assertEquals(100, stateString.getSearchRegion().getW());
            assertEquals(50, stateString.getSearchRegion().getH());
        }

        @Test
        @DisplayName("Should handle null search region")
        public void testNullSearchRegion() {
            stateString.setSearchRegion(null);
            assertNull(stateString.getSearchRegion());
        }

        @Test
        @DisplayName("Should update search region")
        public void testUpdateSearchRegion() {
            Region region1 = new Region(0, 0, 50, 50);
            Region region2 = new Region(100, 100, 200, 200);

            stateString.setSearchRegion(region1);
            assertEquals(region1, stateString.getSearchRegion());

            stateString.setSearchRegion(region2);
            assertEquals(region2, stateString.getSearchRegion());
        }
    }

    @Nested
    @DisplayName("ID Generation")
    class IDGeneration {

        @Test
        @DisplayName("Should generate ID with all properties")
        public void testFullID() {
            stateString.setName("TestName");
            stateString.setString("TestString");
            stateString.setSearchRegion(new Region(10, 20, 30, 40));

            String id = stateString.getId();

            assertTrue(id.contains("STRING"));
            assertTrue(id.contains("TestName"));
            assertTrue(id.contains("10203040")); // Region coordinates
            assertTrue(id.contains("TestString"));
        }

        @Test
        @DisplayName("Should generate ID without search region")
        public void testIDWithoutRegion() {
            stateString.setName("TestName");
            stateString.setString("TestString");
            stateString.setSearchRegion(null);

            String id = stateString.getId();

            assertTrue(id.contains("STRING"));
            assertTrue(id.contains("TestName"));
            assertTrue(id.contains("nullRegion"));
            assertTrue(id.contains("TestString"));
        }

        @Test
        @DisplayName("Should generate ID with null name")
        public void testIDWithNullName() {
            stateString.setName(null);
            stateString.setString("TestString");

            String id = stateString.getId();

            assertTrue(id.contains("STRING"));
            assertTrue(id.contains("null")); // null name becomes "null"
            assertTrue(id.contains("TestString"));
        }

        @Test
        @DisplayName("Should generate unique IDs for different strings")
        public void testUniqueIDs() {
            StateString string1 = new StateString();
            string1.setName("Name1");
            string1.setString("String1");

            StateString string2 = new StateString();
            string2.setName("Name2");
            string2.setString("String2");

            assertNotEquals(string1.getId(), string2.getId());
        }

        @Test
        @DisplayName("Should generate same ID for identical properties")
        public void testIdenticalIDs() {
            StateString string1 = new StateString();
            string1.setName("SameName");
            string1.setString("SameString");
            string1.setSearchRegion(new Region(1, 2, 3, 4));

            StateString string2 = new StateString();
            string2.setName("SameName");
            string2.setString("SameString");
            string2.setSearchRegion(new Region(1, 2, 3, 4));

            assertEquals(string1.getId(), string2.getId());
        }
    }

    @Nested
    @DisplayName("InNullState Factory")
    class InNullStateFactory {

        @Test
        @DisplayName("Should create StateString in null state")
        public void testInNullState() {
            StateString.InNullState factory = new StateString.InNullState();
            StateString nullStateString = factory.withString("Test String");

            assertEquals("Test String", nullStateString.getString());
            assertEquals("null", nullStateString.getOwnerStateName());
            assertNull(nullStateString.getOwnerStateId());
            assertTrue(nullStateString.defined());
        }

        @Test
        @DisplayName("Should create with empty string")
        public void testInNullStateEmptyString() {
            StateString.InNullState factory = new StateString.InNullState();
            StateString nullStateString = factory.withString("");

            assertEquals("", nullStateString.getString());
            assertEquals("null", nullStateString.getOwnerStateName());
            assertFalse(nullStateString.defined());
        }

        @Test
        @DisplayName("Should create with null string")
        public void testInNullStateNullString() {
            StateString.InNullState factory = new StateString.InNullState();
            StateString nullStateString = factory.withString(null);

            assertNull(nullStateString.getString());
            assertEquals("null", nullStateString.getOwnerStateName());
            assertFalse(nullStateString.defined());
        }
    }

    @Nested
    @DisplayName("Times Acted On")
    class TimesActedOn {

        @Test
        @DisplayName("Should increment times acted on")
        public void testIncrementTimesActedOn() {
            assertEquals(0, stateString.getTimesActedOn());

            stateString.addTimesActedOn();
            assertEquals(1, stateString.getTimesActedOn());

            stateString.addTimesActedOn();
            assertEquals(2, stateString.getTimesActedOn());

            stateString.addTimesActedOn();
            assertEquals(3, stateString.getTimesActedOn());
        }

        @Test
        @DisplayName("Should set times acted on")
        public void testSetTimesActedOn() {
            stateString.setTimesActedOn(10);
            assertEquals(10, stateString.getTimesActedOn());

            stateString.setTimesActedOn(0);
            assertEquals(0, stateString.getTimesActedOn());

            stateString.setTimesActedOn(100);
            assertEquals(100, stateString.getTimesActedOn());
        }

        @Test
        @DisplayName("Should handle negative times acted on")
        public void testNegativeTimesActedOn() {
            // Should allow setting negative (might be used for special cases)
            stateString.setTimesActedOn(-1);
            assertEquals(-1, stateString.getTimesActedOn());
        }
    }

    @Nested
    @DisplayName("Defined Method")
    class DefinedMethod {

        @Test
        @DisplayName("Should return true for non-empty strings")
        public void testDefinedWithNonEmptyStrings() {
            stateString.setString("a");
            assertTrue(stateString.defined());

            stateString.setString("Hello World");
            assertTrue(stateString.defined());

            stateString.setString(" "); // Whitespace is still defined
            assertTrue(stateString.defined());
        }

        @Test
        @DisplayName("Should return false for empty string")
        public void testDefinedWithEmptyString() {
            stateString.setString("");
            assertFalse(stateString.defined());
        }

        @Test
        @DisplayName("Should return false for null string")
        public void testDefinedWithNullString() {
            stateString.setString(null);
            assertFalse(stateString.defined());
        }

        @Test
        @DisplayName("Should return false by default")
        public void testDefinedByDefault() {
            StateString newString = new StateString();
            assertFalse(newString.defined());
        }
    }

    @Nested
    @DisplayName("State Ownership")
    class StateOwnership {

        @Test
        @DisplayName("Should handle state ownership")
        public void testStateOwnership() {
            stateString.setOwnerStateName("LoginState");
            stateString.setOwnerStateId(42L);

            assertEquals("LoginState", stateString.getOwnerStateName());
            assertEquals(42L, stateString.getOwnerStateId());
        }

        @Test
        @DisplayName("Should default to null state")
        public void testDefaultNullState() {
            assertEquals("null", stateString.getOwnerStateName());
            assertNull(stateString.getOwnerStateId());
        }

        @Test
        @DisplayName("Should update state ownership")
        public void testUpdateStateOwnership() {
            stateString.setOwnerStateName("State1");
            stateString.setOwnerStateId(1L);

            assertEquals("State1", stateString.getOwnerStateName());
            assertEquals(1L, stateString.getOwnerStateId());

            stateString.setOwnerStateName("State2");
            stateString.setOwnerStateId(2L);

            assertEquals("State2", stateString.getOwnerStateName());
            assertEquals(2L, stateString.getOwnerStateId());
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("Should handle form field scenario")
        public void testFormFieldScenario() {
            // Simulate a form field that needs to be clicked before typing
            Region fieldRegion = new Region(100, 200, 300, 30);

            stateString.setName("UsernameField");
            stateString.setString("john.doe@example.com");
            stateString.setSearchRegion(fieldRegion);
            stateString.setOwnerStateName("LoginForm");
            stateString.setOwnerStateId(10L);

            assertTrue(stateString.defined());
            assertNotNull(stateString.getSearchRegion());
            assertEquals("LoginForm", stateString.getOwnerStateName());

            // Simulate acting on the field
            stateString.addTimesActedOn();
            assertEquals(1, stateString.getTimesActedOn());
        }

        @Test
        @DisplayName("Should handle password scenario")
        public void testPasswordScenario() {
            stateString.setName("PasswordField");
            stateString.setString("SecureP@ssw0rd!");
            stateString.setSearchRegion(new Region(100, 250, 300, 30));
            stateString.setOwnerStateName("LoginForm");

            assertTrue(stateString.defined());
            assertEquals("SecureP@ssw0rd!", stateString.getString());

            // Verify ID contains all components
            String id = stateString.getId();
            assertTrue(id.contains("PasswordField"));
            assertTrue(id.contains("SecureP@ssw0rd!"));
        }

        @Test
        @DisplayName("Should handle command line scenario")
        public void testCommandLineScenario() {
            stateString.setName("CommandInput");
            stateString.setString("git status --porcelain");
            stateString.setSearchRegion(null); // No specific region for terminal
            stateString.setOwnerStateName("Terminal");

            assertTrue(stateString.defined());
            assertNull(stateString.getSearchRegion());
            assertEquals("git status --porcelain", stateString.getString());

            // ID should handle null region
            String id = stateString.getId();
            assertTrue(id.contains("nullRegion"));
        }

        @Test
        @DisplayName("Should handle search query scenario")
        public void testSearchQueryScenario() {
            Region searchBox = new Region(500, 100, 400, 40);

            stateString.setName("SearchBox");
            stateString.setString("Brobot automation framework");
            stateString.setSearchRegion(searchBox);
            stateString.setOwnerStateName("SearchPage");

            // Track multiple searches
            for (int i = 0; i < 5; i++) {
                stateString.addTimesActedOn();
            }

            assertEquals(5, stateString.getTimesActedOn());
            assertTrue(stateString.defined());
        }
    }
}
