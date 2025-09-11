package io.github.jspinak.brobot.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.state.*;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for ConditionalActionWrapper - conditional action execution wrapper.
 * Tests chain building, action execution, and StateObject handling.
 */
@DisplayName("ConditionalActionWrapper Tests")
public class ConditionalActionWrapperTest extends BrobotTestBase {

    private ConditionalActionWrapper wrapper;

    @Mock private Action mockAction;

    @Mock private ActionResult mockActionResult;

    @Mock private StateImage mockStateImage;

    @Mock private StateRegion mockStateRegion;

    @Mock private StateLocation mockStateLocation;

    @Mock private StateString mockStateString;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        wrapper = new ConditionalActionWrapper(mockAction);

        // Setup default mock behaviors
        when(mockActionResult.isSuccess()).thenReturn(true);
        when(mockStateImage.getName()).thenReturn("TestImage");
        when(mockStateRegion.getName()).thenReturn("TestRegion");
        when(mockStateLocation.getName()).thenReturn("TestLocation");
        when(mockStateString.getName()).thenReturn("TestString");
    }

    @Nested
    @DisplayName("Find and Click Operations")
    class FindAndClickOperations {

        @Test
        @DisplayName("Find and click StateImage")
        public void testFindAndClickStateImage() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);

                ActionResult result = wrapper.findAndClick(mockStateImage);

                assertNotNull(result);
                assertTrue(result.isSuccess());
                verify(mockChain).perform(eq(mockAction), any(ObjectCollection.class));
            }
        }

        @Test
        @DisplayName("Find and click StateRegion")
        public void testFindAndClickStateRegion() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);

                ActionResult result = wrapper.findAndClick(mockStateRegion);

                assertNotNull(result);
                assertTrue(result.isSuccess());
                verify(mockChain).perform(eq(mockAction), any(ObjectCollection.class));
            }
        }

        @Test
        @DisplayName("Find and click StateLocation")
        public void testFindAndClickStateLocation() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);

                ActionResult result = wrapper.findAndClick(mockStateLocation);

                assertNotNull(result);
                assertTrue(result.isSuccess());
            }
        }

        @Test
        @DisplayName("Find and click with failure result")
        public void testFindAndClickFailure() {
            when(mockActionResult.isSuccess()).thenReturn(false);

            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);

                ActionResult result = wrapper.findAndClick(mockStateImage);

                assertNotNull(result);
                assertFalse(result.isSuccess());
            }
        }
    }

    @Nested
    @DisplayName("Find and Type Operations")
    class FindAndTypeOperations {

        @Test
        @DisplayName("Find and type in StateImage")
        public void testFindAndTypeStateImage() {
            String textToType = "test text";

            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);
                when(mockChain.ifFound(any(TypeOptions.class))).thenReturn(mockChain);

                ActionResult result = wrapper.findAndType(mockStateImage, textToType);

                assertNotNull(result);
                assertTrue(result.isSuccess());

                ArgumentCaptor<ObjectCollection> collectionCaptor =
                        ArgumentCaptor.forClass(ObjectCollection.class);
                verify(mockChain).perform(eq(mockAction), collectionCaptor.capture());

                ObjectCollection capturedCollection = collectionCaptor.getValue();
                assertFalse(capturedCollection.getStateStrings().isEmpty());
            }
        }

        @Test
        @DisplayName("Find and type empty string")
        public void testFindAndTypeEmptyString() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);
                when(mockChain.ifFound(any(TypeOptions.class))).thenReturn(mockChain);

                ActionResult result = wrapper.findAndType(mockStateRegion, "");

                assertNotNull(result);
            }
        }

        @Test
        @DisplayName("Find and type special characters")
        public void testFindAndTypeSpecialCharacters() {
            String specialText = "!@#$%^&*()_+-=[]{}|;':\",./<>?";

            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);
                when(mockChain.ifFound(any(TypeOptions.class))).thenReturn(mockChain);

                ActionResult result = wrapper.findAndType(mockStateImage, specialText);

                assertNotNull(result);

                ArgumentCaptor<ObjectCollection> collectionCaptor =
                        ArgumentCaptor.forClass(ObjectCollection.class);
                verify(mockChain).perform(eq(mockAction), collectionCaptor.capture());

                ObjectCollection capturedCollection = collectionCaptor.getValue();
                assertEquals(specialText, capturedCollection.getStateStrings().get(0).getString());
            }
        }

        @Test
        @DisplayName("Find and type multiline text")
        public void testFindAndTypeMultiline() {
            String multilineText = "Line 1\nLine 2\nLine 3";

            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);
                when(mockChain.ifFound(any(TypeOptions.class))).thenReturn(mockChain);

                ActionResult result = wrapper.findAndType(mockStateLocation, multilineText);

                assertNotNull(result);
            }
        }
    }

    @Nested
    @DisplayName("Chain Builder")
    class ChainBuilderTests {

        @Test
        @DisplayName("Simple chain with find and click")
        public void testSimpleChain() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ActionConfig.class))).thenReturn(mockChain);

                ActionResult result =
                        wrapper.createChain()
                                .find(mockStateImage)
                                .ifFound(ConditionalActionWrapper.click())
                                .execute();

                assertNotNull(result);
                assertTrue(result.isSuccess());
            }
        }

        @Test
        @DisplayName("Chain with if-not-found action")
        public void testChainWithIfNotFound() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ActionConfig.class))).thenReturn(mockChain);
                when(mockChain.ifNotFound(any(ActionConfig.class))).thenReturn(mockChain);

                ActionResult result =
                        wrapper.createChain()
                                .find(mockStateImage)
                                .ifFound(ConditionalActionWrapper.click())
                                .ifNotFound(ConditionalActionWrapper.click())
                                .execute();

                assertNotNull(result);
                verify(mockChain).ifNotFound(any(ActionConfig.class));
            }
        }

        @Test
        @DisplayName("Chain with always action")
        public void testChainWithAlways() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.always(any(ActionConfig.class))).thenReturn(mockChain);

                ActionResult result =
                        wrapper.createChain()
                                .find(mockStateRegion)
                                .always(ConditionalActionWrapper.type())
                                .execute();

                assertNotNull(result);
                verify(mockChain).always(any(ActionConfig.class));
            }
        }

        @Test
        @DisplayName("Chain with logging")
        public void testChainWithLogging() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFoundLog(anyString())).thenReturn(mockChain);
                when(mockChain.ifNotFoundLog(anyString())).thenReturn(mockChain);

                ActionResult result =
                        wrapper.createChain()
                                .find(mockStateImage)
                                .ifFoundLog("Found the element")
                                .ifNotFoundLog("Element not found")
                                .execute();

                assertNotNull(result);
                verify(mockChain).ifFoundLog("Found the element");
                verify(mockChain).ifNotFoundLog("Element not found");
            }
        }

        @Test
        @DisplayName("Chain with additional collections")
        public void testChainWithAdditionalCollections() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection[].class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);

                ObjectCollection additional =
                        new ObjectCollection.Builder().withStrings(mockStateString).build();

                ActionResult result =
                        wrapper.createChain().find(mockStateImage).execute(additional);

                assertNotNull(result);
                verify(mockChain).perform(eq(mockAction), any(ObjectCollection[].class));
            }
        }

        @Test
        @DisplayName("Chain without find throws exception")
        public void testChainWithoutFind() {
            ConditionalActionWrapper.ChainBuilder builder = wrapper.createChain();

            assertThrows(
                    IllegalStateException.class,
                    () -> {
                        builder.ifFound(ConditionalActionWrapper.click());
                    });
        }

        @Test
        @DisplayName("Execute without initialization throws exception")
        public void testExecuteWithoutInit() {
            ConditionalActionWrapper.ChainBuilder builder = wrapper.createChain();

            assertThrows(
                    IllegalStateException.class,
                    () -> {
                        builder.execute();
                    });
        }
    }

    @Nested
    @DisplayName("Static Factory Methods")
    class StaticFactoryMethods {

        @Test
        @DisplayName("Create ClickOptions")
        public void testCreateClickOptions() {
            ClickOptions options = ConditionalActionWrapper.click();

            assertNotNull(options);
            assertTrue(options instanceof ClickOptions);
        }

        @Test
        @DisplayName("Create TypeOptions")
        public void testCreateTypeOptions() {
            TypeOptions options = ConditionalActionWrapper.type();

            assertNotNull(options);
            assertTrue(options instanceof TypeOptions);
        }

        @Test
        @DisplayName("Create PatternFindOptions")
        public void testCreateFindOptions() {
            PatternFindOptions options = ConditionalActionWrapper.find();

            assertNotNull(options);
            assertTrue(options instanceof PatternFindOptions);
        }
    }

    @Nested
    @DisplayName("ObjectCollection Creation")
    class ObjectCollectionCreation {

        @Test
        @DisplayName("Create collection from StateImage")
        public void testCollectionFromStateImage() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                ArgumentCaptor<ObjectCollection> collectionCaptor =
                        ArgumentCaptor.forClass(ObjectCollection.class);

                when(mockChain.perform(any(Action.class), collectionCaptor.capture()))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);

                wrapper.findAndClick(mockStateImage);

                ObjectCollection captured = collectionCaptor.getValue();
                assertFalse(captured.getStateImages().isEmpty());
            }
        }

        @Test
        @DisplayName("Create collection from StateRegion")
        public void testCollectionFromStateRegion() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                ArgumentCaptor<ObjectCollection> collectionCaptor =
                        ArgumentCaptor.forClass(ObjectCollection.class);

                when(mockChain.perform(any(Action.class), collectionCaptor.capture()))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);

                wrapper.findAndClick(mockStateRegion);

                ObjectCollection captured = collectionCaptor.getValue();
                assertFalse(captured.getStateRegions().isEmpty());
            }
        }

        @Test
        @DisplayName("Create collection from StateLocation")
        public void testCollectionFromStateLocation() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                ArgumentCaptor<ObjectCollection> collectionCaptor =
                        ArgumentCaptor.forClass(ObjectCollection.class);

                when(mockChain.perform(any(Action.class), collectionCaptor.capture()))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);

                wrapper.findAndClick(mockStateLocation);

                ObjectCollection captured = collectionCaptor.getValue();
                assertFalse(captured.getStateLocations().isEmpty());
            }
        }

        @Test
        @DisplayName("Create collection from StateImage with find and click")
        public void testCollectionFromStateImageWithFindAndClick() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                ArgumentCaptor<ObjectCollection> collectionCaptor =
                        ArgumentCaptor.forClass(ObjectCollection.class);

                when(mockChain.perform(any(Action.class), collectionCaptor.capture()))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);

                wrapper.findAndClick(mockStateImage);

                ObjectCollection captured = collectionCaptor.getValue();
                assertFalse(captured.getStateImages().isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("Complex Chain Scenarios")
    class ComplexChainScenarios {

        @Test
        @DisplayName("Multi-step form filling")
        public void testMultiStepFormFilling() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ActionConfig.class))).thenReturn(mockChain);
                when(mockChain.ifFoundLog(anyString())).thenReturn(mockChain);

                // Simulate form filling: find field, click, type
                ActionResult result =
                        wrapper.createChain()
                                .find(mockStateImage)
                                .ifFound(ConditionalActionWrapper.click())
                                .ifFound(ConditionalActionWrapper.type())
                                .ifFoundLog("Form field filled")
                                .execute();

                assertNotNull(result);
                verify(mockChain, times(2)).ifFound(any(ActionConfig.class));
            }
        }

        @Test
        @DisplayName("Error recovery chain")
        public void testErrorRecoveryChain() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ActionConfig.class))).thenReturn(mockChain);
                when(mockChain.ifNotFound(any(ActionConfig.class))).thenReturn(mockChain);
                when(mockChain.ifNotFoundLog(anyString())).thenReturn(mockChain);

                ActionResult result =
                        wrapper.createChain()
                                .find(mockStateImage)
                                .ifFound(ConditionalActionWrapper.click())
                                .ifNotFoundLog("Primary target not found, trying fallback")
                                .ifNotFound(ConditionalActionWrapper.find())
                                .execute();

                assertNotNull(result);
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Find and type with null text")
        public void testFindAndTypeNullText() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);
                when(mockChain.ifFound(any(TypeOptions.class))).thenReturn(mockChain);

                ActionResult result = wrapper.findAndType(mockStateImage, null);

                assertNotNull(result);
            }
        }

        @Test
        @DisplayName("Very long text input")
        public void testVeryLongTextInput() {
            String longText = "a".repeat(10000);

            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);
                when(mockChain.ifFound(any(TypeOptions.class))).thenReturn(mockChain);

                ActionResult result = wrapper.findAndType(mockStateRegion, longText);

                assertNotNull(result);
            }
        }

        @Test
        @DisplayName("Chain with null StateObject")
        public void testChainWithNullStateObject() {
            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);

                ConditionalActionWrapper.ChainBuilder builder = wrapper.createChain();
                builder.find(null);

                assertThrows(
                        IllegalStateException.class,
                        () -> {
                            builder.execute();
                        });
            }
        }
    }

    @Nested
    @DisplayName("Real-World Use Cases")
    class RealWorldUseCases {

        @Test
        @DisplayName("Login form automation")
        public void testLoginFormAutomation() {
            StateImage usernameField = mock(StateImage.class);
            StateImage passwordField = mock(StateImage.class);
            StateImage submitButton = mock(StateImage.class);

            when(usernameField.getName()).thenReturn("UsernameField");
            when(passwordField.getName()).thenReturn("PasswordField");
            when(submitButton.getName()).thenReturn("SubmitButton");

            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ClickOptions.class))).thenReturn(mockChain);
                when(mockChain.ifFound(any(TypeOptions.class))).thenReturn(mockChain);

                // Fill username
                ActionResult result1 = wrapper.findAndType(usernameField, "testuser");
                assertTrue(result1.isSuccess());

                // Fill password
                ActionResult result2 = wrapper.findAndType(passwordField, "password123");
                assertTrue(result2.isSuccess());

                // Click submit
                ActionResult result3 = wrapper.findAndClick(submitButton);
                assertTrue(result3.isSuccess());
            }
        }

        @Test
        @DisplayName("Menu navigation")
        public void testMenuNavigation() {
            StateImage fileMenu = mock(StateImage.class);
            StateImage openOption = mock(StateImage.class);

            when(fileMenu.getName()).thenReturn("FileMenu");
            when(openOption.getName()).thenReturn("OpenOption");

            try (MockedStatic<ConditionalActionChain> chainMock =
                    mockStatic(ConditionalActionChain.class)) {
                ConditionalActionChain mockChain = mock(ConditionalActionChain.class);
                when(mockChain.perform(any(Action.class), any(ObjectCollection.class)))
                        .thenReturn(mockActionResult);

                chainMock
                        .when(() -> ConditionalActionChain.find(any(PatternFindOptions.class)))
                        .thenReturn(mockChain);
                when(mockChain.ifFound(any(ActionConfig.class))).thenReturn(mockChain);
                when(mockChain.ifNotFoundLog(anyString())).thenReturn(mockChain);

                // Navigate menu: File -> Open
                ActionResult result =
                        wrapper.createChain()
                                .find(fileMenu)
                                .ifFound(ConditionalActionWrapper.click())
                                .ifNotFoundLog("File menu not found")
                                .execute();

                assertTrue(result.isSuccess());

                // Then click Open
                ActionResult openResult = wrapper.findAndClick(openOption);
                assertTrue(openResult.isSuccess());
            }
        }
    }
}
