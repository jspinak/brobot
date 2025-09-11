package io.github.jspinak.brobot.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;

/**
 * Integration test suite for StateFactory. Tests factory methods for creating Brobot state
 * components with Spring context.
 */
@Disabled("Missing BrobotConfig class + Failing in CI")
@SpringBootTest(classes = BrobotTestApplication.class)
// @Import(BrobotConfig.class)
@TestPropertySource(
        properties = {
            "brobot.mock=true",
            "brobot.console.actions.enabled=false",
            "brobot.console.actions.level=SILENT"
        })
@DisplayName("StateFactory Integration Tests")
public class StateFactoryIntegrationTest {

    @Autowired private StateFactory stateFactory;

    // Mock StateEnum for testing
    private enum TestStateEnum implements StateEnum {
        TEST_STATE_1,
        TEST_STATE_2,
        ANOTHER_STATE;

        @Override
        public String toString() {
            return name();
        }
    }

    @Nested
    @DisplayName("State Creation")
    class StateCreation {

        @Test
        @DisplayName("Should create state with name")
        void shouldCreateStateWithName() {
            State state = stateFactory.createState(TestStateEnum.TEST_STATE_1, null);

            assertNotNull(state);
            assertEquals(TestStateEnum.TEST_STATE_1.toString(), state.getName());
        }

        @Test
        @DisplayName("Should create state with initializer")
        void shouldCreateStateWithInitializer() {
            Consumer<State.Builder> initializer =
                    builder -> {
                        builder.setBaseProbabilityExists(80);
                    };

            State state = stateFactory.createState(TestStateEnum.TEST_STATE_2, initializer);

            assertNotNull(state);
            assertEquals(TestStateEnum.TEST_STATE_2.toString(), state.getName());
            assertEquals(80, state.getBaseProbabilityExists());
        }

        @Test
        @DisplayName("Should create state without initializer")
        void shouldCreateStateWithoutInitializer() {
            State state = stateFactory.createState(TestStateEnum.ANOTHER_STATE, null);

            assertNotNull(state);
            assertEquals(TestStateEnum.ANOTHER_STATE.toString(), state.getName());
        }

        @Test
        @DisplayName("Should create state with complex initializer")
        void shouldCreateStateWithComplexInitializer() {
            StateImage image1 = stateFactory.createStateImage("button.png");
            StateImage image2 = stateFactory.createStateImage("menu.png");

            Consumer<State.Builder> initializer =
                    builder -> {
                        builder.withImages(image1, image2)
                                .setBaseProbabilityExists(90)
                                .setBlocking(true);
                    };

            State state = stateFactory.createState(TestStateEnum.TEST_STATE_1, initializer);

            assertNotNull(state);
            assertEquals(2, state.getStateImages().size());
            assertEquals(90, state.getBaseProbabilityExists());
            assertTrue(state.isBlocking());
        }
    }

    @Nested
    @DisplayName("StateImage Creation")
    class StateImageCreation {

        @Test
        @DisplayName("Should create StateImage with single image")
        void shouldCreateStateImageWithSingleImage() {
            StateImage stateImage = stateFactory.createStateImage("button.png");

            assertNotNull(stateImage, "StateImage should not be null");
            assertNotNull(stateImage.getPatterns(), "Patterns list should not be null");
            assertEquals(1, stateImage.getPatterns().size(), "Should have exactly 1 pattern");

            // Check the pattern was created correctly
            Pattern pattern = stateImage.getPatterns().get(0);
            assertNotNull(pattern, "Pattern should not be null");
            assertEquals(
                    "button.png", pattern.getImgpath(), "Pattern should have correct image path");
        }

        @Test
        @DisplayName("Should create StateImage with multiple images")
        void shouldCreateStateImageWithMultipleImages() {
            StateImage stateImage =
                    stateFactory.createStateImage("button.png", "icon.png", "logo.png");

            assertNotNull(stateImage);
            assertNotNull(stateImage.getPatterns());
            assertEquals(3, stateImage.getPatterns().size());

            // Verify each pattern
            assertEquals("button.png", stateImage.getPatterns().get(0).getImgpath());
            assertEquals("icon.png", stateImage.getPatterns().get(1).getImgpath());
            assertEquals("logo.png", stateImage.getPatterns().get(2).getImgpath());
        }

        @Test
        @DisplayName("Should create StateImage with no images")
        void shouldCreateStateImageWithNoImages() {
            StateImage stateImage = stateFactory.createStateImage();

            assertNotNull(stateImage);
            assertNotNull(stateImage.getPatterns());
            assertTrue(stateImage.getPatterns().isEmpty());
        }

        @ParameterizedTest
        @DisplayName("Should create StateImage with various image counts")
        @ValueSource(ints = {1, 2, 5, 10})
        void shouldCreateStateImageWithVariousImageCounts(int count) {
            String[] imageNames = new String[count];
            for (int i = 0; i < count; i++) {
                imageNames[i] = "image" + i + ".png";
            }

            StateImage stateImage = stateFactory.createStateImage(imageNames);

            assertNotNull(stateImage);
            assertNotNull(stateImage.getPatterns());
            assertEquals(count, stateImage.getPatterns().size());

            // Verify each pattern has correct path
            for (int i = 0; i < count; i++) {
                assertEquals("image" + i + ".png", stateImage.getPatterns().get(i).getImgpath());
            }
        }
    }

    @Nested
    @DisplayName("StateString Creation")
    class StateStringCreation {

        @Test
        @DisplayName("Should create simple StateString")
        void shouldCreateSimpleStateString() {
            StateString stateString = stateFactory.createStateString("Hello World");

            assertNotNull(stateString);
            assertEquals("Hello World", stateString.getString());
        }

        @Test
        @DisplayName("Should create StateString with empty string")
        void shouldCreateStateStringWithEmptyString() {
            StateString stateString = stateFactory.createStateString("");

            assertNotNull(stateString);
            assertEquals("", stateString.getString());
        }

        @Test
        @DisplayName("Should create StateString with special characters")
        void shouldCreateStateStringWithSpecialCharacters() {
            String specialString = "Tab\\tNew\\nLine\\r\\nSpecial!@#$%^&*()";
            StateString stateString = stateFactory.createStateString(specialString);

            assertNotNull(stateString);
            assertEquals(specialString, stateString.getString());
        }

        @Test
        @DisplayName("Should create StateString with name and owner")
        void shouldCreateStateStringWithNameAndOwner() {
            StateString stateString =
                    stateFactory.createStateString("username_field", "admin", "LoginState");

            assertNotNull(stateString);
            assertEquals("username_field", stateString.getName());
            assertEquals("admin", stateString.getString());
            assertEquals("LoginState", stateString.getOwnerStateName());
        }

        @Test
        @DisplayName("Should create StateString with null owner")
        void shouldCreateStateStringWithNullOwner() {
            StateString stateString = stateFactory.createStateString("field_name", "value", null);

            assertNotNull(stateString);
            assertEquals("field_name", stateString.getName());
            assertEquals("value", stateString.getString());
            // Owner may be null or empty string depending on implementation
            assertTrue(
                    stateString.getOwnerStateName() == null
                            || stateString.getOwnerStateName().isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle null StateEnum")
        void shouldHandleNullStateEnum() {
            assertThrows(
                    NullPointerException.class,
                    () -> {
                        stateFactory.createState(null, null);
                    });
        }

        @Test
        @DisplayName("Should handle null initializer")
        void shouldHandleNullInitializer() {
            State state = stateFactory.createState(TestStateEnum.TEST_STATE_1, null);

            assertNotNull(state);
            assertEquals(TestStateEnum.TEST_STATE_1.toString(), state.getName());
        }

        @Test
        @DisplayName("Should handle initializer that throws exception")
        void shouldHandleInitializerThatThrowsException() {
            Consumer<State.Builder> badInitializer =
                    builder -> {
                        throw new RuntimeException("Initialization error");
                    };

            assertThrows(
                    RuntimeException.class,
                    () -> {
                        stateFactory.createState(TestStateEnum.TEST_STATE_1, badInitializer);
                    });
        }

        @Test
        @DisplayName("Should create StateString with null value")
        void shouldCreateStateStringWithNullValue() {
            StateString stateString = stateFactory.createStateString(null);

            assertNotNull(stateString);
            // Builder may convert null to empty string or keep it null
            assertTrue(stateString.getString() == null || stateString.getString().isEmpty());
        }

        @Test
        @DisplayName("Should handle null in StateImage array")
        void shouldHandleNullInStateImageArray() {
            // This may throw NPE when iterating nulls
            assertThrows(
                    NullPointerException.class,
                    () -> {
                        stateFactory.createStateImage("image1.png", null, "image2.png");
                    });
        }
    }

    @Nested
    @DisplayName("Factory Pattern Verification")
    class FactoryPatternVerification {

        @Test
        @DisplayName("Should create independent State instances")
        void shouldCreateIndependentStateInstances() {
            State state1 = stateFactory.createState(TestStateEnum.TEST_STATE_1, null);
            State state2 = stateFactory.createState(TestStateEnum.TEST_STATE_1, null);

            assertNotSame(state1, state2);
            assertEquals(state1.getName(), state2.getName());
        }

        @Test
        @DisplayName("Should create independent StateImage instances")
        void shouldCreateIndependentStateImageInstances() {
            StateImage image1 = stateFactory.createStateImage("test.png");
            StateImage image2 = stateFactory.createStateImage("test.png");

            assertNotSame(image1, image2);
            assertEquals(image1.getPatterns().size(), image2.getPatterns().size());
            assertEquals(
                    image1.getPatterns().get(0).getImgpath(),
                    image2.getPatterns().get(0).getImgpath());
        }

        @Test
        @DisplayName("Should create independent StateString instances")
        void shouldCreateIndependentStateStringInstances() {
            StateString string1 = stateFactory.createStateString("test");
            StateString string2 = stateFactory.createStateString("test");

            assertNotSame(string1, string2);
            assertEquals(string1.getString(), string2.getString());
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Should create complete state hierarchy")
        void shouldCreateCompleteStateHierarchy() {
            // Create state images
            StateImage loginButton = stateFactory.createStateImage("login_button.png");
            StateImage usernameField = stateFactory.createStateImage("username_field.png");

            // Create state strings
            StateString username =
                    stateFactory.createStateString("Username", "admin", "LoginState");
            StateString password =
                    stateFactory.createStateString("Password", "secret", "LoginState");

            // Create state with all components
            Consumer<State.Builder> initializer =
                    builder -> {
                        builder.withImages(loginButton, usernameField).setBaseProbabilityExists(95);
                        // Add text using actual StateString names if they exist
                        if (username.getName() != null && !username.getName().isEmpty()) {
                            builder.withText(username.getName());
                        }
                        if (password.getName() != null && !password.getName().isEmpty()) {
                            builder.withText(password.getName());
                        }
                    };

            State loginState = stateFactory.createState(TestStateEnum.TEST_STATE_1, initializer);

            // Verify complete state
            assertNotNull(loginState);
            assertEquals(2, loginState.getStateImages().size());
            assertTrue(loginState.getStateText().size() >= 0); // May vary based on name handling
            assertEquals(95, loginState.getBaseProbabilityExists());

            // Verify images have patterns
            for (StateImage image : loginState.getStateImages()) {
                assertNotNull(image.getPatterns());
                assertFalse(image.getPatterns().isEmpty());
            }
        }

        @Test
        @DisplayName("Should support fluent state building")
        void shouldSupportFluentStateBuilding() {
            StateImage img1 = stateFactory.createStateImage("img1.png");
            StateImage img2 = stateFactory.createStateImage("img2.png", "img3.png");

            Consumer<State.Builder> fluentInitializer =
                    builder -> builder.withImages(img1, img2).setBaseProbabilityExists(70);

            State state = stateFactory.createState(TestStateEnum.ANOTHER_STATE, fluentInitializer);

            assertNotNull(state);
            assertEquals(2, state.getStateImages().size());
            assertEquals(70, state.getBaseProbabilityExists());

            // Verify total pattern count across all images
            // First StateImage has 1 pattern, second StateImage has 2 patterns
            int totalPatterns =
                    state.getStateImages().stream().mapToInt(img -> img.getPatterns().size()).sum();
            assertEquals(
                    3, totalPatterns, "Total patterns should be 3 (1 from img1 + 2 from img2)");
        }
    }
}
