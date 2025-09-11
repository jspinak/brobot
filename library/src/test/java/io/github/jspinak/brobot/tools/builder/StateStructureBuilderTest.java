package io.github.jspinak.brobot.tools.builder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Unit tests for StateStructureBuilder. Tests state structure creation and transition setup. */
@DisplayName("StateStructureBuilder Tests")
class StateStructureBuilderTest extends BrobotTestBase {

    private StateStructureBuilder builder;

    @Mock private StateService mockStateService;

    @Mock private StateTransitionStore mockTransitionStore;

    @Mock private Action mockAction;

    @Mock private ActionResult mockActionResult;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        builder = new StateStructureBuilder(mockStateService, mockTransitionStore, mockAction);
    }

    @Nested
    @DisplayName("State Initialization")
    class StateInitialization {

        @Test
        @DisplayName("Should initialize with state name")
        void testInitWithStateName() {
            StateStructureBuilder result = builder.init("TestState");

            assertSame(builder, result); // Fluent interface

            // Build and verify state was created with correct name
            builder.build();

            ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
            verify(mockStateService).save(stateCaptor.capture());

            State savedState = stateCaptor.getValue();
            assertEquals("TestState", savedState.getName());
        }

        @Test
        @DisplayName("Should create StateTransitions with default finish")
        void testInitCreatesTransitions() {
            builder.init("TestState");
            builder.build();

            ArgumentCaptor<StateTransitions> transitionsCaptor =
                    ArgumentCaptor.forClass(StateTransitions.class);
            verify(mockTransitionStore).add(transitionsCaptor.capture());

            StateTransitions transitions = transitionsCaptor.getValue();
            assertEquals("TestState", transitions.getStateName());
            assertNotNull(transitions.getTransitionFinish());
        }
    }

    @Nested
    @DisplayName("Image Management")
    class ImageManagement {

        @Test
        @DisplayName("Should add image to state")
        void testAddImage() {
            builder.init("TestState").addImage("image1.png").addImage("image2.png").build();

            ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
            verify(mockStateService).save(stateCaptor.capture());

            State savedState = stateCaptor.getValue();
            assertEquals(2, savedState.getStateImages().size());
        }

        @Test
        @DisplayName("Should create StateImage with pattern")
        void testStateImageCreation() {
            builder.init("TestState").addImage("test.png").build();

            ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
            verify(mockStateService).save(stateCaptor.capture());

            State savedState = stateCaptor.getValue();
            Set<StateImage> stateImages = savedState.getStateImages();
            assertEquals(1, stateImages.size());
            StateImage stateImage = stateImages.iterator().next();
            assertNotNull(stateImage);
            assertFalse(stateImage.getPatterns().isEmpty());
        }
    }

    @Nested
    @DisplayName("Transition Management")
    class TransitionManagement {

        @Test
        @DisplayName("Should add transition image with click action")
        void testAddTransitionImage() {
            when(mockActionResult.isSuccess()).thenReturn(true);
            when(mockAction.perform(eq(ActionType.CLICK), any(StateImage.class)))
                    .thenReturn(mockActionResult);

            builder.init("FromState").addTransitionImage("button.png", "ToState").build();

            // Verify state has the transition image
            ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
            verify(mockStateService).save(stateCaptor.capture());
            State savedState = stateCaptor.getValue();
            assertEquals(1, savedState.getStateImages().size());

            // Verify transition was added
            ArgumentCaptor<StateTransitions> transitionsCaptor =
                    ArgumentCaptor.forClass(StateTransitions.class);
            verify(mockTransitionStore).add(transitionsCaptor.capture());
            StateTransitions transitions = transitionsCaptor.getValue();
            assertEquals(1, transitions.getTransitions().size());
        }

        @Test
        @DisplayName("Should create JavaStateTransition with click function")
        void testTransitionFunction() {
            when(mockActionResult.isSuccess()).thenReturn(true);
            when(mockAction.perform(eq(ActionType.CLICK), any(StateImage.class)))
                    .thenReturn(mockActionResult);

            builder.init("FromState").addTransitionImage("button.png", "ToState").build();

            ArgumentCaptor<StateTransitions> transitionsCaptor =
                    ArgumentCaptor.forClass(StateTransitions.class);
            verify(mockTransitionStore).add(transitionsCaptor.capture());

            StateTransitions transitions = transitionsCaptor.getValue();
            JavaStateTransition javaTransition =
                    (JavaStateTransition) transitions.getTransitions().get(0);

            // Test the transition function
            boolean result = javaTransition.getTransitionFunction().getAsBoolean();

            assertTrue(result);
            verify(mockAction).perform(eq(ActionType.CLICK), any(StateImage.class));
        }

        @Test
        @DisplayName("Should handle multiple transitions")
        void testMultipleTransitions() {
            when(mockActionResult.isSuccess()).thenReturn(true);
            when(mockAction.perform(eq(ActionType.CLICK), any(StateImage.class)))
                    .thenReturn(mockActionResult);

            builder.init("FromState")
                    .addTransitionImage("button1.png", "State1")
                    .addTransitionImage("button2.png", "State2")
                    .addTransitionImage("button3.png", "State3")
                    .build();

            ArgumentCaptor<StateTransitions> transitionsCaptor =
                    ArgumentCaptor.forClass(StateTransitions.class);
            verify(mockTransitionStore).add(transitionsCaptor.capture());

            StateTransitions transitions = transitionsCaptor.getValue();
            assertEquals(3, transitions.getTransitions().size());
        }
    }

    @Nested
    @DisplayName("Fluent Interface")
    class FluentInterface {

        @Test
        @DisplayName("Should support method chaining")
        void testMethodChaining() {
            when(mockActionResult.isSuccess()).thenReturn(true);
            when(mockAction.perform(eq(ActionType.CLICK), any(StateImage.class)))
                    .thenReturn(mockActionResult);

            StateStructureBuilder result =
                    builder.init("ChainState")
                            .addImage("image1.png")
                            .addTransitionImage("button.png", "NextState")
                            .addImage("image2.png");

            assertSame(builder, result);

            result.build();

            ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
            verify(mockStateService).save(stateCaptor.capture());

            State savedState = stateCaptor.getValue();
            assertEquals(3, savedState.getStateImages().size());
        }
    }

    @Nested
    @DisplayName("Build Process")
    class BuildProcess {

        @Test
        @DisplayName("Should save state and transitions on build")
        void testBuildSavesStateAndTransitions() {
            builder.init("TestState").build();

            verify(mockStateService, times(1)).save(any(State.class));
            verify(mockTransitionStore, times(1)).add(any(StateTransitions.class));
        }

        @Test
        @DisplayName("Should build empty state with no images")
        void testBuildEmptyState() {
            builder.init("EmptyState").build();

            ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
            verify(mockStateService).save(stateCaptor.capture());

            State savedState = stateCaptor.getValue();
            assertTrue(savedState.getStateImages().isEmpty());
        }

        @Test
        @DisplayName("Should handle complex state structure")
        void testComplexStateStructure() {
            when(mockActionResult.isSuccess()).thenReturn(true);
            when(mockAction.perform(eq(ActionType.CLICK), any(StateImage.class)))
                    .thenReturn(mockActionResult);

            builder.init("ComplexState")
                    .addImage("background.png")
                    .addImage("logo.png")
                    .addTransitionImage("homeButton.png", "HomePage")
                    .addTransitionImage("settingsButton.png", "SettingsPage")
                    .addImage("footer.png")
                    .addTransitionImage("logoutButton.png", "LoginPage")
                    .build();

            ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
            verify(mockStateService).save(stateCaptor.capture());

            State savedState = stateCaptor.getValue();
            assertEquals(6, savedState.getStateImages().size());

            ArgumentCaptor<StateTransitions> transitionsCaptor =
                    ArgumentCaptor.forClass(StateTransitions.class);
            verify(mockTransitionStore).add(transitionsCaptor.capture());

            StateTransitions transitions = transitionsCaptor.getValue();
            assertEquals(3, transitions.getTransitions().size());
        }
    }

    @Nested
    @DisplayName("Error Scenarios")
    class ErrorScenarios {

        @Test
        @DisplayName("Should handle action failure in transition")
        void testActionFailureInTransition() {
            when(mockActionResult.isSuccess()).thenReturn(false);
            when(mockAction.perform(eq(ActionType.CLICK), any(StateImage.class)))
                    .thenReturn(mockActionResult);

            builder.init("FromState").addTransitionImage("button.png", "ToState").build();

            ArgumentCaptor<StateTransitions> transitionsCaptor =
                    ArgumentCaptor.forClass(StateTransitions.class);
            verify(mockTransitionStore).add(transitionsCaptor.capture());

            StateTransitions transitions = transitionsCaptor.getValue();
            JavaStateTransition javaTransition =
                    (JavaStateTransition) transitions.getTransitions().get(0);

            // Test the transition function returns false when action fails
            boolean result = javaTransition.getTransitionFunction().getAsBoolean();

            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle multiple init calls")
        void testMultipleInitCalls() {
            builder.init("FirstState").addImage("first.png");

            // Second init should reset
            builder.init("SecondState").addImage("second.png").build();

            ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
            verify(mockStateService).save(stateCaptor.capture());

            State savedState = stateCaptor.getValue();
            assertEquals("SecondState", savedState.getName());
            assertEquals(1, savedState.getStateImages().size());
        }
    }
}
