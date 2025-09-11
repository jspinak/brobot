package io.github.jspinak.brobot.model.state.special;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for the StateText class which represents persistent text that uniquely
 * identifies a state in GUI automation.
 */
@DisplayName("StateText Model Tests")
public class StateTextTest extends BrobotTestBase {

    private StateText stateText;
    private Region testRegion;
    private ObjectMapper objectMapper;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        testRegion = new Region(100, 200, 300, 400);
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should create StateText with default constructor")
    void testDefaultConstructor() {
        // When
        stateText = new StateText();

        // Then
        assertNotNull(stateText);
        assertEquals(StateObject.Type.TEXT, stateText.getObjectType());
        assertNull(stateText.getName());
        assertNull(stateText.getSearchRegion());
        assertEquals("null", stateText.getOwnerStateName());
        assertNull(stateText.getText());
    }

    @Test
    @DisplayName("Should create StateText with Builder using all properties")
    void testBuilderWithAllProperties() {
        // When
        stateText =
                new StateText.Builder()
                        .setName("WindowTitle")
                        .setSearchRegion(testRegion)
                        .setOwnerStateName("MainWindow")
                        .setText("Settings - Application")
                        .build();

        // Then
        assertEquals("WindowTitle", stateText.getName());
        assertSame(testRegion, stateText.getSearchRegion());
        assertEquals("MainWindow", stateText.getOwnerStateName());
        assertEquals("Settings - Application", stateText.getText());
        assertEquals(StateObject.Type.TEXT, stateText.getObjectType());
    }

    @Test
    @DisplayName("Should create StateText with Builder using minimal properties")
    void testBuilderWithMinimalProperties() {
        // When
        stateText = new StateText.Builder().setText("Minimal Text").build();

        // Then
        assertNull(stateText.getName());
        assertNull(stateText.getSearchRegion());
        assertNull(stateText.getOwnerStateName());
        assertEquals("Minimal Text", stateText.getText());
    }

    @Test
    @DisplayName("Should generate unique ID correctly")
    void testGetId() {
        // Given
        stateText =
                new StateText.Builder()
                        .setName("TestName")
                        .setSearchRegion(testRegion)
                        .setText("Test Text")
                        .build();

        // When
        String id = stateText.getId();

        // Then
        assertNotNull(id);
        assertTrue(id.contains("TEXT"));
        assertTrue(id.contains("TestName"));
        assertTrue(id.contains("100200300400")); // Region coordinates
        assertTrue(id.contains("Test Text"));
    }

    @Test
    @DisplayName("Should generate ID with null region")
    void testGetIdWithNullRegion() {
        // Given
        stateText = new StateText.Builder().setName("NoRegion").setText("Text Only").build();

        // When
        String id = stateText.getId();

        // Then
        assertNotNull(id);
        assertTrue(id.contains("TEXT"));
        assertTrue(id.contains("NoRegion"));
        assertTrue(id.contains("nullRegion"));
        assertTrue(id.contains("Text Only"));
    }

    @Test
    @DisplayName("Should check if defined correctly")
    void testDefined() {
        // Test undefined (null text)
        stateText = new StateText();
        assertFalse(stateText.defined());

        // Test undefined (empty text)
        stateText.setText("");
        assertFalse(stateText.defined());

        // Test defined
        stateText.setText("Some Text");
        assertTrue(stateText.defined());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("Should handle various empty text values")
    void testVariousEmptyTextValues(String text) {
        // When
        stateText = new StateText.Builder().setText(text).build();

        // Then
        assertEquals(text, stateText.getText());
        if (text == null || text.isEmpty()) {
            assertFalse(stateText.defined());
        } else {
            assertTrue(stateText.defined()); // Whitespace is considered defined
        }
    }

    @Test
    @DisplayName("Should get and set properties correctly")
    void testGettersAndSetters() {
        // Given
        stateText = new StateText();

        // When
        stateText.setName("UpdatedName");
        stateText.setSearchRegion(testRegion);
        stateText.setOwnerStateName("UpdatedState");
        stateText.setText("Updated Text");
        stateText.setObjectType(StateObject.Type.TEXT); // Should remain TEXT

        // Then
        assertEquals("UpdatedName", stateText.getName());
        assertSame(testRegion, stateText.getSearchRegion());
        assertEquals("UpdatedState", stateText.getOwnerStateName());
        assertEquals("Updated Text", stateText.getText());
        assertEquals(StateObject.Type.TEXT, stateText.getObjectType());
    }

    @Test
    @DisplayName("Should serialize and deserialize to/from JSON")
    void testJacksonSerialization() throws JsonProcessingException {
        // Given
        stateText =
                new StateText.Builder()
                        .setName("SerializedText")
                        .setSearchRegion(testRegion)
                        .setOwnerStateName("SerializedState")
                        .setText("Serialization Test")
                        .build();

        // When - Serialize
        String json = objectMapper.writeValueAsString(stateText);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"objectType\":\"TEXT\""));
        assertTrue(json.contains("\"name\":\"SerializedText\""));
        assertTrue(json.contains("\"ownerStateName\":\"SerializedState\""));
        assertTrue(json.contains("\"text\":\"Serialization Test\""));

        // When - Deserialize
        StateText deserialized = objectMapper.readValue(json, StateText.class);

        // Then
        assertEquals(stateText.getName(), deserialized.getName());
        assertEquals(stateText.getOwnerStateName(), deserialized.getOwnerStateName());
        assertEquals(stateText.getText(), deserialized.getText());
        assertEquals(stateText.getObjectType(), deserialized.getObjectType());
    }

    @Test
    @DisplayName("Should handle JsonIgnoreProperties annotation")
    void testJsonIgnoreUnknownProperties() throws JsonProcessingException {
        // Given - JSON with unknown property
        String jsonWithUnknown =
                "{\"objectType\":\"TEXT\",\"name\":\"Test\",\"text\":\"TestText\","
                        + "\"unknownProperty\":\"unknown value\"}";

        // When - Should not throw exception
        StateText deserialized = objectMapper.readValue(jsonWithUnknown, StateText.class);

        // Then
        assertEquals("Test", deserialized.getName());
        assertEquals("TestText", deserialized.getText());
    }

    @TestFactory
    @DisplayName("StateText usage scenarios")
    Stream<DynamicTest> testUsageScenarios() {
        return Stream.of(
                dynamicTest(
                        "Window title identification",
                        () -> {
                            StateText windowTitle =
                                    new StateText.Builder()
                                            .setName("WindowTitle")
                                            .setText("Settings - Application Name")
                                            .setSearchRegion(new Region(0, 0, 1920, 30))
                                            .setOwnerStateName("SettingsWindow")
                                            .build();

                            assertTrue(windowTitle.defined());
                            assertTrue(windowTitle.getId().contains("Settings - Application Name"));
                        }),
                dynamicTest(
                        "Page header recognition",
                        () -> {
                            StateText pageHeader =
                                    new StateText.Builder()
                                            .setName("DashboardHeader")
                                            .setText("User Account Dashboard")
                                            .setOwnerStateName("Dashboard")
                                            .build();

                            assertEquals("User Account Dashboard", pageHeader.getText());
                            assertTrue(pageHeader.defined());
                        }),
                dynamicTest(
                        "Status message verification",
                        () -> {
                            StateText statusMsg =
                                    new StateText.Builder()
                                            .setName("StatusIndicator")
                                            .setText("Processing...")
                                            .setSearchRegion(new Region(500, 500, 200, 50))
                                            .build();

                            assertEquals("Processing...", statusMsg.getText());
                            assertNotNull(statusMsg.getSearchRegion());
                        }),
                dynamicTest(
                        "Unique label identification",
                        () -> {
                            StateText uniqueLabel =
                                    new StateText.Builder()
                                            .setName("AdvancedOptionsLabel")
                                            .setText("Advanced Options")
                                            .setOwnerStateName("ConfigurationDialog")
                                            .build();

                            assertEquals("ConfigurationDialog", uniqueLabel.getOwnerStateName());
                            assertTrue(uniqueLabel.defined());
                        }));
    }

    @ParameterizedTest
    @CsvSource({
        "LoginPrompt,Username:,LoginDialog,true",
        "ErrorMessage,Invalid credentials,ErrorDialog,true",
        "EmptyLabel,'',MainWindow,false",
        "NullText,,AnyState,false"
    })
    @DisplayName("Should handle various text scenarios")
    void testVariousTextScenarios(
            String name, String text, String stateName, boolean shouldBeDefined) {
        // When
        stateText =
                new StateText.Builder()
                        .setName(name)
                        .setText(text)
                        .setOwnerStateName(stateName)
                        .build();

        // Then
        assertEquals(name, stateText.getName());
        assertEquals(text, stateText.getText());
        assertEquals(stateName, stateText.getOwnerStateName());
        assertEquals(shouldBeDefined, stateText.defined());
    }

    @Test
    @DisplayName("Should maintain consistent object type")
    void testConsistentObjectType() {
        // Given
        stateText = new StateText();

        // Then - Should always be TEXT type
        assertEquals(StateObject.Type.TEXT, stateText.getObjectType());

        // When - Try to set different type
        stateText.setObjectType(StateObject.Type.IMAGE);

        // Then - Can be changed but not recommended
        assertEquals(StateObject.Type.IMAGE, stateText.getObjectType());
    }

    @Test
    @DisplayName("Should handle equals and hashCode")
    void testEqualsAndHashCode() {
        // Given
        StateText text1 =
                new StateText.Builder()
                        .setName("Text1")
                        .setText("Same Text")
                        .setOwnerStateName("State1")
                        .build();

        StateText text2 =
                new StateText.Builder()
                        .setName("Text1")
                        .setText("Same Text")
                        .setOwnerStateName("State1")
                        .build();

        StateText text3 =
                new StateText.Builder()
                        .setName("Text2")
                        .setText("Different Text")
                        .setOwnerStateName("State1")
                        .build();

        // Then - Reflexive
        assertEquals(text1, text1);
        assertEquals(text1.hashCode(), text1.hashCode());

        // Symmetric
        assertEquals(text1, text2);
        assertEquals(text2, text1);
        assertEquals(text1.hashCode(), text2.hashCode());

        // Different properties
        assertNotEquals(text1, text3);

        // Null safety
        assertNotEquals(text1, null);
        assertNotEquals(text1, "not a StateText");
    }

    @Test
    @DisplayName("Should generate different IDs for different texts")
    void testUniqueIds() {
        // Given
        StateText text1 = new StateText.Builder().setName("Same").setText("Text1").build();

        StateText text2 = new StateText.Builder().setName("Same").setText("Text2").build();

        // When
        String id1 = text1.getId();
        String id2 = text2.getId();

        // Then
        assertNotEquals(id1, id2);
        assertTrue(id1.contains("Text1"));
        assertTrue(id2.contains("Text2"));
    }

    @Test
    @DisplayName("Should handle Builder method chaining")
    void testBuilderMethodChaining() {
        // When - All methods should return Builder
        stateText =
                new StateText.Builder()
                        .setName("Chained")
                        .setSearchRegion(testRegion)
                        .setOwnerStateName("ChainedState")
                        .setText("Chained Text")
                        .build();

        // Then
        assertNotNull(stateText);
        assertEquals("Chained", stateText.getName());
        assertEquals("Chained Text", stateText.getText());
    }

    @Test
    @DisplayName("Should handle region with extreme values")
    void testRegionWithExtremeValues() {
        // Given
        Region extremeRegion = new Region(Integer.MAX_VALUE, Integer.MIN_VALUE, 0, -1);

        // When
        stateText =
                new StateText.Builder().setSearchRegion(extremeRegion).setText("Extreme").build();

        // Then
        String id = stateText.getId();
        assertNotNull(id);
        assertTrue(id.contains("TEXT"));
        // ID should handle extreme values without error
    }

    @Test
    @DisplayName("Should provide default owner state name")
    void testDefaultOwnerStateName() {
        // When
        stateText = new StateText();

        // Then
        assertEquals("null", stateText.getOwnerStateName());
    }

    @Test
    @DisplayName("Should handle internationalized text")
    void testInternationalizedText() {
        // Given - Various language texts
        String[] texts = {
            "Hello World", // English
            "你好世界", // Chinese
            "مرحبا بالعالم", // Arabic
            "Здравствуй мир", // Russian
            "こんにちは世界", // Japanese
            "🎉 Unicode Emoji 🎉" // Emoji
        };

        // When/Then
        for (String text : texts) {
            StateText st = new StateText.Builder().setText(text).build();

            assertEquals(text, st.getText());
            assertTrue(st.defined());
            assertTrue(st.getId().contains(text));
        }
    }
}
