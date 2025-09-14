package io.github.jspinak.brobot.annotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.statemanagement.InitialStates;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.TestCategories;

/**
 * Test suite for AnnotationProcessor. Tests the runtime processing of @State and @TransitionSet
 * annotations.
 */
@DisplayName("AnnotationProcessor Tests")
@Tag(TestCategories.UNIT)
@Tag(TestCategories.FAST)
@Tag("annotations")
@Tag("processor")
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
public class AnnotationProcessorTest extends BrobotTestBase {

    @Mock private ApplicationContext applicationContext;

    @Mock private StateTransitionsJointTable jointTable;

    @Mock private StateService stateService;

    @Mock private StateTransitionService transitionService;

    @Mock private InitialStates initialStates;

    @Mock private AnnotatedStateBuilder stateBuilder;

    @Mock private StateRegistrationService registrationService;

    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private Environment environment;

    @Mock private StateAnnotationBeanPostProcessor stateBeanPostProcessor;

    @Mock private TransitionAnnotationBeanPostProcessor transitionBeanPostProcessor;

    @Mock private TransitionSetProcessor transitionSetProcessor;

    private AnnotationProcessor processor;
    private AutoCloseable mocks;

    // Test state classes
    @io.github.jspinak.brobot.annotations.State(name = "TestState", initial = true, priority = 200)
    static class TestState {}

    @io.github.jspinak.brobot.annotations.State
    static class SimpleState {}

    @io.github.jspinak.brobot.annotations.State(profiles = {"test", "dev"})
    static class ProfileState {}

    // Test transition class using TransitionSet
    @io.github.jspinak.brobot.annotations.TransitionSet(state = SimpleState.class)
    static class TestTransition {
        @io.github.jspinak.brobot.annotations.FromTransition(from = TestState.class)
        public boolean execute() {
            return true;
        }

        @io.github.jspinak.brobot.annotations.ToTransition
        public boolean verifyArrival() {
            return true;
        }
    }

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mocks = MockitoAnnotations.openMocks(this);

        processor =
                new AnnotationProcessor(
                        applicationContext,
                        jointTable,
                        stateService,
                        transitionService,
                        initialStates,
                        stateBuilder,
                        registrationService,
                        eventPublisher,
                        stateBeanPostProcessor,
                        transitionBeanPostProcessor,
                        transitionSetProcessor);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Nested
    @DisplayName("State Processing Tests")
    class StateProcessingTests {

        @Test
        @DisplayName("Should process states with @State annotation")
        void shouldProcessStatesWithAnnotation() {
            // Setup
            Map<String, Object> stateBeans = new HashMap<>();
            TestState testStateBean = new TestState();
            stateBeans.put("testState", testStateBean);

            io.github.jspinak.brobot.model.state.State mockState =
                    mock(io.github.jspinak.brobot.model.state.State.class);
            when(mockState.getName()).thenReturn("TestState");

            when(applicationContext.getBeansWithAnnotation(
                            io.github.jspinak.brobot.annotations.State.class))
                    .thenReturn(stateBeans);
            when(applicationContext.getEnvironment()).thenReturn(environment);
            when(environment.getActiveProfiles()).thenReturn(new String[] {});
            when(stateBuilder.buildState(any(), any())).thenReturn(mockState);
            when(registrationService.registerState(any())).thenReturn(true);
            when(registrationService.getRegisteredStateCount()).thenReturn(1);

            // Execute
            processor.processAnnotations();

            // Verify
            verify(stateBuilder)
                    .buildState(
                            testStateBean,
                            TestState.class.getAnnotation(
                                    io.github.jspinak.brobot.annotations.State.class));
            verify(registrationService).registerState(mockState);
            verify(initialStates).addStateSet(200, "TestState"); // Priority 200 from annotation
        }

        @Test
        @DisplayName("Should handle initial states with priorities")
        void shouldHandleInitialStatesWithPriorities() {
            // Setup
            Map<String, Object> stateBeans = new HashMap<>();
            TestState testStateBean = new TestState();
            stateBeans.put("testState", testStateBean);

            io.github.jspinak.brobot.model.state.State mockState =
                    mock(io.github.jspinak.brobot.model.state.State.class);
            when(mockState.getName()).thenReturn("TestState");

            when(applicationContext.getBeansWithAnnotation(
                            io.github.jspinak.brobot.annotations.State.class))
                    .thenReturn(stateBeans);
            when(applicationContext.getEnvironment()).thenReturn(environment);
            when(environment.getActiveProfiles()).thenReturn(new String[] {});
            when(stateBuilder.buildState(any(), any())).thenReturn(mockState);
            when(registrationService.registerState(any())).thenReturn(true);
            when(registrationService.getRegisteredStateCount()).thenReturn(1);

            // Execute
            processor.processAnnotations();

            // Verify initial state was added with correct priority
            verify(initialStates).addStateSet(200, "TestState");
        }

        @Test
        @DisplayName("Should respect profile filtering")
        void shouldRespectProfileFiltering() {
            // Setup
            Map<String, Object> stateBeans = new HashMap<>();
            ProfileState profileStateBean = new ProfileState();
            stateBeans.put("profileState", profileStateBean);

            io.github.jspinak.brobot.model.state.State mockState =
                    mock(io.github.jspinak.brobot.model.state.State.class);
            when(mockState.getName()).thenReturn("ProfileState");

            when(applicationContext.getBeansWithAnnotation(
                            io.github.jspinak.brobot.annotations.State.class))
                    .thenReturn(stateBeans);
            when(applicationContext.getEnvironment()).thenReturn(environment);
            when(environment.getActiveProfiles()).thenReturn(new String[] {"production"});
            when(stateBuilder.buildState(any(), any())).thenReturn(mockState);
            when(registrationService.registerState(any())).thenReturn(true);
            when(registrationService.getRegisteredStateCount()).thenReturn(1);

            // Execute
            processor.processAnnotations();

            // Verify state was registered but not marked as initial (wrong profile)
            verify(registrationService).registerState(mockState);
            verify(initialStates, never()).addStateSet(anyInt(), anyString());
        }
    }

    @Nested
    @DisplayName("Transition Processing Tests")
    class TransitionProcessingTests {

        @Test
        @DisplayName("Should process transitions with @TransitionSet annotation")
        void shouldProcessTransitionsWithAnnotation() {
            // Setup states first
            Map<String, Object> stateBeans = new HashMap<>();
            stateBeans.put("testState", new TestState());
            stateBeans.put("simpleState", new SimpleState());

            // Setup transitions
            Map<String, Object> transitionBeans = new HashMap<>();
            TestTransition testTransition = new TestTransition();
            transitionBeans.put("testTransition", testTransition);

            io.github.jspinak.brobot.model.state.State mockState1 =
                    mock(io.github.jspinak.brobot.model.state.State.class);
            when(mockState1.getName()).thenReturn("TestState");
            when(mockState1.getId()).thenReturn(1L);
            io.github.jspinak.brobot.model.state.State mockState2 =
                    mock(io.github.jspinak.brobot.model.state.State.class);
            when(mockState2.getName()).thenReturn("SimpleState");
            when(mockState2.getId()).thenReturn(2L);

            when(applicationContext.getBeansWithAnnotation(
                            io.github.jspinak.brobot.annotations.State.class))
                    .thenReturn(stateBeans);
            when(applicationContext.getBeansWithAnnotation(TransitionSet.class))
                    .thenReturn(transitionBeans);

            // Mock the BeanPostProcessors
            when(transitionBeanPostProcessor.getTransitionSetBeans()).thenReturn(transitionBeans);
            when(applicationContext.getEnvironment()).thenReturn(environment);
            when(environment.getActiveProfiles()).thenReturn(new String[] {});
            when(stateBuilder.buildState(any(), any()))
                    .thenReturn(mockState1)
                    .thenReturn(mockState2);
            when(registrationService.registerState(any())).thenReturn(true);
            when(registrationService.getRegisteredStateCount()).thenReturn(2);

            // Mock stateService to return the states when requested
            when(stateService.getState("TestState")).thenReturn(Optional.of(mockState1));
            when(stateService.getState("SimpleState")).thenReturn(Optional.of(mockState2));
            when(stateService.getState("Test")).thenReturn(Optional.of(mockState1));
            when(stateService.getState("Simple")).thenReturn(Optional.of(mockState2));

            // Mock transitionService
            when(transitionService.getTransitions(anyLong())).thenReturn(Optional.empty());
            when(transitionService.getStateTransitionsRepository())
                    .thenReturn(
                            mock(
                                    io.github.jspinak.brobot.model.transition.StateTransitionStore
                                            .class));

            // Execute
            processor.processAnnotations();

            // Verify transition was registered
            verify(jointTable, atLeastOnce()).addToJointTable(any());
        }

        @Test
        @DisplayName("Should find transition method by name")
        void shouldFindTransitionMethodByName() throws NoSuchMethodException {
            Method executeMethod = TestTransition.class.getMethod("execute");
            assertNotNull(executeMethod);
            assertEquals("execute", executeMethod.getName());
            assertEquals(boolean.class, executeMethod.getReturnType());
        }
    }

    @Nested
    @DisplayName("Event Publishing Tests")
    class EventPublishingTests {

        @Test
        @DisplayName("Should publish StatesRegisteredEvent after processing")
        void shouldPublishStatesRegisteredEvent() {
            // Setup
            Map<String, Object> stateBeans = new HashMap<>();
            stateBeans.put("testState", new TestState());

            io.github.jspinak.brobot.model.state.State mockState =
                    mock(io.github.jspinak.brobot.model.state.State.class);
            when(mockState.getName()).thenReturn("TestState");

            when(applicationContext.getBeansWithAnnotation(
                            io.github.jspinak.brobot.annotations.State.class))
                    .thenReturn(stateBeans);
            when(applicationContext.getBeansWithAnnotation(TransitionSet.class))
                    .thenReturn(new HashMap<>());
            when(applicationContext.getEnvironment()).thenReturn(environment);
            when(environment.getActiveProfiles()).thenReturn(new String[] {});
            when(stateBuilder.buildState(any(), any())).thenReturn(mockState);
            when(registrationService.registerState(any())).thenReturn(true);
            when(registrationService.getRegisteredStateCount()).thenReturn(1);

            // Execute
            processor.processAnnotations();

            // Verify event was published
            verify(eventPublisher).publishEvent(any(StatesRegisteredEvent.class));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle missing transition method gracefully")
        void shouldHandleMissingTransitionMethod() {
            // Setup transition with invalid method name
            @TransitionSet(state = SimpleState.class)
            class InvalidTransition {
                @FromTransition(from = TestState.class)
                public boolean nonExistentMethod() {
                    return false;
                }
            }

            Map<String, Object> transitionBeans = new HashMap<>();
            transitionBeans.put("invalidTransition", new InvalidTransition());

            when(applicationContext.getBeansWithAnnotation(
                            io.github.jspinak.brobot.annotations.State.class))
                    .thenReturn(new HashMap<>());
            when(applicationContext.getBeansWithAnnotation(TransitionSet.class))
                    .thenReturn(transitionBeans);

            // Mock the BeanPostProcessors
            when(transitionBeanPostProcessor.getTransitionSetBeans()).thenReturn(transitionBeans);
            when(applicationContext.getEnvironment()).thenReturn(environment);
            when(environment.getActiveProfiles()).thenReturn(new String[] {});
            when(registrationService.getRegisteredStateCount()).thenReturn(0);

            // Execute - should not throw exception
            assertDoesNotThrow(() -> processor.processAnnotations());

            // Verify no transition was added
            verify(jointTable, never()).addToJointTable(any());
        }

        @Test
        @DisplayName("Should handle registration failure gracefully")
        void shouldHandleRegistrationFailureGracefully() {
            // Setup
            Map<String, Object> stateBeans = new HashMap<>();
            stateBeans.put("testState", new TestState());

            io.github.jspinak.brobot.model.state.State mockState =
                    mock(io.github.jspinak.brobot.model.state.State.class);
            when(mockState.getName()).thenReturn("TestState");

            when(applicationContext.getBeansWithAnnotation(
                            io.github.jspinak.brobot.annotations.State.class))
                    .thenReturn(stateBeans);
            when(applicationContext.getEnvironment()).thenReturn(environment);
            when(environment.getActiveProfiles()).thenReturn(new String[] {});
            when(stateBuilder.buildState(any(), any())).thenReturn(mockState);
            when(registrationService.registerState(any())).thenReturn(false); // Registration fails
            when(registrationService.getRegisteredStateCount()).thenReturn(0);

            // Execute - should not throw exception
            assertDoesNotThrow(() -> processor.processAnnotations());

            // Verify initial state was not added
            verify(initialStates, never()).addStateSet(anyInt(), anyString());
        }
    }

    @Nested
    @DisplayName("State Name Resolution Tests")
    class StateNameResolutionTests {

        @Test
        @DisplayName("Should use custom name from annotation")
        void shouldUseCustomNameFromAnnotation() {
            // Setup
            Map<String, Object> stateBeans = new HashMap<>();
            TestState testStateBean = new TestState();
            stateBeans.put("testState", testStateBean);

            io.github.jspinak.brobot.model.state.State mockState =
                    mock(io.github.jspinak.brobot.model.state.State.class);
            when(mockState.getName()).thenReturn("TestState");

            when(applicationContext.getBeansWithAnnotation(
                            io.github.jspinak.brobot.annotations.State.class))
                    .thenReturn(stateBeans);
            when(applicationContext.getEnvironment()).thenReturn(environment);
            when(environment.getActiveProfiles()).thenReturn(new String[] {});
            when(stateBuilder.buildState(any(), any())).thenReturn(mockState);
            when(registrationService.registerState(any())).thenReturn(true);
            when(registrationService.getRegisteredStateCount()).thenReturn(1);

            // Execute
            processor.processAnnotations();

            // Verify custom name was used
            verify(initialStates).addStateSet(200, "TestState");
        }

        @Test
        @DisplayName("Should derive name from class when not specified")
        void shouldDeriveNameFromClass() {
            // Setup
            Map<String, Object> stateBeans = new HashMap<>();
            SimpleState simpleStateBean = new SimpleState();
            stateBeans.put("simpleState", simpleStateBean);

            io.github.jspinak.brobot.model.state.State mockState =
                    mock(io.github.jspinak.brobot.model.state.State.class);
            when(mockState.getName()).thenReturn("Simple"); // "State" suffix removed

            when(applicationContext.getBeansWithAnnotation(
                            io.github.jspinak.brobot.annotations.State.class))
                    .thenReturn(stateBeans);
            when(applicationContext.getEnvironment()).thenReturn(environment);
            when(environment.getActiveProfiles()).thenReturn(new String[] {});
            when(stateBuilder.buildState(any(), any())).thenReturn(mockState);
            when(registrationService.registerState(any())).thenReturn(true);
            when(registrationService.getRegisteredStateCount()).thenReturn(1);

            // Execute
            processor.processAnnotations();

            // Verify registration happened
            verify(registrationService).registerState(mockState);
        }
    }
}
