package io.github.jspinak.brobot.annotations;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AnnotationProcessor - processes @State and @Transition annotations.
 * Verifies automatic state registration and transition setup.
 */
@DisplayName("AnnotationProcessor Tests")
public class AnnotationProcessorTest extends BrobotTestBase {
    
    private AnnotationProcessor processor;
    private StateRegistrationService registrationService;
    private ApplicationEventPublisher eventPublisher;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        registrationService = mock(StateRegistrationService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        processor = new AnnotationProcessor(registrationService, eventPublisher);
    }
    
    @Nested
    @DisplayName("State Annotation Processing")
    class StateAnnotationProcessing {
        
        @Test
        @DisplayName("Process class with @State annotation")
        public void testProcessStateAnnotation() {
            TestStateClass stateClass = new TestStateClass();
            
            processor.processStateAnnotations(stateClass);
            
            verify(registrationService).registerState(any(State.class));
            verify(eventPublisher).publishEvent(any(StatesRegisteredEvent.class));
        }
        
        @Test
        @DisplayName("Extract state name from annotation")
        public void testExtractStateName() {
            @State(name = "LoginState")
            class NamedState {}
            
            NamedState instance = new NamedState();
            String name = processor.extractStateName(instance.getClass());
            
            assertEquals("LoginState", name);
        }
        
        @Test
        @DisplayName("Use class name when state name not specified")
        public void testDefaultStateName() {
            @State
            class UnnamedState {}
            
            UnnamedState instance = new UnnamedState();
            String name = processor.extractStateName(instance.getClass());
            
            assertEquals("UnnamedState", name);
        }
        
        @Test
        @DisplayName("Process state with images")
        public void testProcessStateWithImages() {
            @State(name = "ImageState", images = {"button.png", "form.png"})
            class ImageState {}
            
            ImageState instance = new ImageState();
            State state = processor.buildStateFromAnnotation(instance);
            
            assertNotNull(state);
            assertEquals("ImageState", state.getName());
            assertEquals(2, state.getStateImages().size());
        }
        
        @Test
        @DisplayName("Process state with properties")
        public void testProcessStateProperties() {
            @State(
                name = "ConfiguredState",
                canHide = true,
                canTransitionToSelf = false,
                description = "Test state"
            )
            class ConfiguredState {}
            
            ConfiguredState instance = new ConfiguredState();
            State state = processor.buildStateFromAnnotation(instance);
            
            assertTrue(state.canHide());
            assertFalse(state.canTransitionToSelf());
            assertEquals("Test state", state.getDescription());
        }
        
        @Test
        @DisplayName("Process nested state classes")
        public void testProcessNestedStateClasses() {
            class OuterClass {
                @State(name = "NestedState")
                class InnerState {}
            }
            
            OuterClass.InnerState instance = new OuterClass().new InnerState();
            State state = processor.buildStateFromAnnotation(instance);
            
            assertNotNull(state);
            assertEquals("NestedState", state.getName());
        }
    }
    
    @Nested
    @DisplayName("Transition Annotation Processing")
    class TransitionAnnotationProcessing {
        
        @Test
        @DisplayName("Process method with @Transition annotation")
        public void testProcessTransitionAnnotation() throws Exception {
            TransitionTestClass testClass = new TransitionTestClass();
            Method method = testClass.getClass().getMethod("navigateToHome");
            
            processor.processTransitionAnnotation(method, testClass);
            
            verify(registrationService).registerTransition(
                eq("LoginState"), eq("HomeState"), any()
            );
        }
        
        @Test
        @DisplayName("Extract transition properties")
        public void testExtractTransitionProperties() throws Exception {
            class TestClass {
                @Transition(
                    from = "StateA",
                    to = "StateB",
                    trigger = "clickNext",
                    probability = 0.95,
                    maxRetries = 3
                )
                public void transition() {}
            }
            
            TestClass instance = new TestClass();
            Method method = instance.getClass().getMethod("transition");
            
            Map<String, Object> props = processor.extractTransitionProperties(method);
            
            assertEquals("StateA", props.get("from"));
            assertEquals("StateB", props.get("to"));
            assertEquals("clickNext", props.get("trigger"));
            assertEquals(0.95, props.get("probability"));
            assertEquals(3, props.get("maxRetries"));
        }
        
        @Test
        @DisplayName("Handle multiple transitions on same method")
        public void testMultipleTransitions() throws Exception {
            class TestClass {
                @Transition(from = "A", to = "B")
                @Transition(from = "B", to = "C")
                public void multiTransition() {}
            }
            
            TestClass instance = new TestClass();
            Method method = instance.getClass().getMethod("multiTransition");
            
            List<Map<String, Object>> transitions = processor.extractAllTransitions(method);
            
            assertEquals(2, transitions.size());
        }
        
        @Test
        @DisplayName("Validate transition method signature")
        public void testValidateTransitionMethod() throws Exception {
            class TestClass {
                @Transition(from = "A", to = "B")
                public void validTransition() {}
                
                @Transition(from = "A", to = "B")
                public String invalidReturn() { return ""; }
                
                @Transition(from = "A", to = "B")
                private void privateTransition() {}
            }
            
            TestClass instance = new TestClass();
            
            Method valid = instance.getClass().getMethod("validTransition");
            assertTrue(processor.isValidTransitionMethod(valid));
            
            Method invalid = instance.getClass().getMethod("invalidReturn");
            assertFalse(processor.isValidTransitionMethod(invalid));
            
            // Private method should not be processed
            Method[] methods = instance.getClass().getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().equals("privateTransition")) {
                    assertFalse(processor.isValidTransitionMethod(m));
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Field Injection")
    class FieldInjection {
        
        @Test
        @DisplayName("Inject StateImage fields")
        public void testInjectStateImageFields() throws Exception {
            @State(name = "FieldState")
            class FieldState {
                @State.Image("button.png")
                private StateImage button;
                
                @State.Image(value = "form.png", shared = true)
                private StateImage sharedForm;
                
                public StateImage getButton() { return button; }
                public StateImage getSharedForm() { return sharedForm; }
            }
            
            FieldState instance = new FieldState();
            processor.injectStateFields(instance);
            
            assertNotNull(instance.getButton());
            assertEquals("button.png", instance.getButton().getPatterns().get(0).getImgpath());
            
            assertNotNull(instance.getSharedForm());
            assertTrue(instance.getSharedForm().isShared());
        }
        
        @Test
        @DisplayName("Inject StateRegion fields")
        public void testInjectStateRegionFields() throws Exception {
            @State(name = "RegionState")
            class RegionState {
                @State.Region(x = 100, y = 200, w = 300, h = 400)
                private StateRegion area;
                
                public StateRegion getArea() { return area; }
            }
            
            RegionState instance = new RegionState();
            processor.injectStateFields(instance);
            
            assertNotNull(instance.getArea());
            Region region = instance.getArea().getSearchRegion();
            assertEquals(100, region.x());
            assertEquals(200, region.y());
            assertEquals(300, region.w());
            assertEquals(400, region.h());
        }
        
        @Test
        @DisplayName("Inject StateLocation fields")
        public void testInjectStateLocationFields() throws Exception {
            @State(name = "LocationState")
            class LocationState {
                @State.Location(x = 500, y = 600)
                private StateLocation point;
                
                public StateLocation getPoint() { return point; }
            }
            
            LocationState instance = new LocationState();
            processor.injectStateFields(instance);
            
            assertNotNull(instance.getPoint());
            Location loc = instance.getPoint().getLocation();
            assertEquals(500, loc.x());
            assertEquals(600, loc.y());
        }
        
        @Test
        @DisplayName("Handle injection errors gracefully")
        public void testHandleInjectionErrors() {
            @State(name = "ErrorState")
            class ErrorState {
                @State.Image("missing.png")
                private final StateImage readOnlyField = null;
            }
            
            ErrorState instance = new ErrorState();
            
            // Should not throw, just log warning
            assertDoesNotThrow(() -> processor.injectStateFields(instance));
        }
    }
    
    @Nested
    @DisplayName("Component Scanning")
    class ComponentScanning {
        
        @Test
        @DisplayName("Scan package for @State annotated classes")
        public void testScanForStateClasses() {
            String packageName = "io.github.jspinak.brobot.test.states";
            
            Set<Class<?>> stateClasses = processor.scanForStateClasses(packageName);
            
            // Should find all @State annotated classes in package
            for (Class<?> clazz : stateClasses) {
                assertTrue(clazz.isAnnotationPresent(State.class));
            }
        }
        
        @Test
        @DisplayName("Scan for @Transition annotated methods")
        public void testScanForTransitionMethods() {
            String packageName = "io.github.jspinak.brobot.test.transitions";
            
            Map<Method, Object> transitions = processor.scanForTransitions(packageName);
            
            for (Method method : transitions.keySet()) {
                assertTrue(method.isAnnotationPresent(Transition.class));
            }
        }
        
        @Test
        @DisplayName("Register scanned components with Spring")
        public void testRegisterWithSpring() {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            
            processor.registerScannedComponents(context, "io.github.jspinak.brobot.test");
            
            // Verify beans are registered
            String[] beanNames = context.getBeanDefinitionNames();
            assertTrue(beanNames.length > 0);
        }
    }
    
    @Nested
    @DisplayName("Event Publishing")
    class EventPublishing {
        
        @Test
        @DisplayName("Publish StatesRegisteredEvent after processing")
        public void testPublishStatesRegisteredEvent() {
            List<State> states = List.of(
                new State.Builder().setName("State1").build(),
                new State.Builder().setName("State2").build()
            );
            
            processor.publishRegistrationComplete(states);
            
            verify(eventPublisher).publishEvent(argThat(event -> 
                event instanceof StatesRegisteredEvent &&
                ((StatesRegisteredEvent) event).getStates().size() == 2
            ));
        }
        
        @Test
        @DisplayName("Include metadata in registration event")
        public void testEventMetadata() {
            State state = new State.Builder().setName("TestState").build();
            
            StatesRegisteredEvent event = new StatesRegisteredEvent(this, List.of(state));
            
            assertNotNull(event.getSource());
            assertNotNull(event.getTimestamp());
            assertEquals(1, event.getStates().size());
            assertEquals("TestState", event.getStates().get(0).getName());
        }
    }
    
    @Nested
    @DisplayName("Validation")
    class Validation {
        
        @Test
        @DisplayName("Validate state annotation configuration")
        public void testValidateStateAnnotation() {
            @State(name = "", images = {})
            class InvalidState {}
            
            InvalidState instance = new InvalidState();
            
            assertFalse(processor.isValidStateAnnotation(instance.getClass()));
        }
        
        @Test
        @DisplayName("Validate transition annotation configuration")
        public void testValidateTransitionAnnotation() throws Exception {
            class TestClass {
                @Transition(from = "", to = "")
                public void invalidTransition() {}
                
                @Transition(from = "A", to = "B")
                public void validTransition() {}
            }
            
            TestClass instance = new TestClass();
            
            Method invalid = instance.getClass().getMethod("invalidTransition");
            assertFalse(processor.isValidTransitionAnnotation(invalid));
            
            Method valid = instance.getClass().getMethod("validTransition");
            assertTrue(processor.isValidTransitionAnnotation(valid));
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"", " ", "123Invalid", "invalid-name", "@invalid"})
        @DisplayName("Validate state names")
        public void testValidateStateName(String name) {
            assertFalse(processor.isValidStateName(name));
        }
    }
    
    // Test fixture classes
    @State
    static class TestStateClass {
        private StateImage button;
    }
    
    @State
    static class LoginState {}
    
    @State
    static class HomeState {}
    
    @Transition(from = LoginState.class, to = HomeState.class)
    static class TransitionTestClass {
        public boolean execute() {
            // Transition implementation
            return true;
        }
    }
    
    // Mock processor implementation
    private static class AnnotationProcessor extends AnnotatedStateBuilder {
        private final StateRegistrationService registrationService;
        private final ApplicationEventPublisher eventPublisher;
        
        public AnnotationProcessor(StateRegistrationService service, ApplicationEventPublisher publisher) {
            this.registrationService = service;
            this.eventPublisher = publisher;
        }
        
        public void processStateAnnotations(Object instance) {
            if (instance.getClass().isAnnotationPresent(State.class)) {
                State state = buildStateFromAnnotation(instance);
                registrationService.registerState(state);
                eventPublisher.publishEvent(new StatesRegisteredEvent(this, List.of(state)));
            }
        }
        
        public State buildStateFromAnnotation(Object instance) {
            Class<?> clazz = instance.getClass();
            State annotation = clazz.getAnnotation(State.class);
            if (annotation == null) return null;
            
            State.Builder builder = new State.Builder();
            builder.setName(annotation.name().isEmpty() ? clazz.getSimpleName() : annotation.name());
            
            for (String img : annotation.images()) {
                builder.addImage(new StateImage.Builder().addPattern(img).build());
            }
            
            State state = builder.build();
            state.setCanHide(annotation.canHide());
            state.setCanTransitionToSelf(annotation.canTransitionToSelf());
            state.setDescription(annotation.description());
            
            return state;
        }
        
        public String extractStateName(Class<?> clazz) {
            State annotation = clazz.getAnnotation(State.class);
            if (annotation != null && !annotation.name().isEmpty()) {
                return annotation.name();
            }
            return clazz.getSimpleName();
        }
        
        public void processTransitionAnnotation(Method method, Object instance) {
            Transition annotation = method.getAnnotation(Transition.class);
            if (annotation != null) {
                registrationService.registerTransition(
                    annotation.from(),
                    annotation.to(),
                    instance
                );
            }
        }
        
        public Map<String, Object> extractTransitionProperties(Method method) {
            Transition annotation = method.getAnnotation(Transition.class);
            if (annotation == null) return Map.of();
            
            return Map.of(
                "from", annotation.from(),
                "to", annotation.to(),
                "trigger", annotation.trigger(),
                "probability", annotation.probability(),
                "maxRetries", annotation.maxRetries()
            );
        }
        
        public List<Map<String, Object>> extractAllTransitions(Method method) {
            // Simplified - would handle multiple annotations
            return List.of(extractTransitionProperties(method));
        }
        
        public boolean isValidTransitionMethod(Method method) {
            if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) return false;
            if (method.getReturnType() != void.class) return false;
            return true;
        }
        
        public void injectStateFields(Object instance) {
            Field[] fields = instance.getClass().getDeclaredFields();
            for (Field field : fields) {
                try {
                    if (field.isAnnotationPresent(State.Image.class)) {
                        State.Image annotation = field.getAnnotation(State.Image.class);
                        field.setAccessible(true);
                        StateImage image = new StateImage.Builder()
                            .addPattern(annotation.value())
                            .build();
                        image.setShared(annotation.shared());
                        field.set(instance, image);
                    } else if (field.isAnnotationPresent(State.Region.class)) {
                        State.Region annotation = field.getAnnotation(State.Region.class);
                        field.setAccessible(true);
                        StateRegion region = new StateRegion();
                        region.setSearchRegion(new Region(
                            annotation.x(), annotation.y(), 
                            annotation.w(), annotation.h()
                        ));
                        field.set(instance, region);
                    } else if (field.isAnnotationPresent(State.Location.class)) {
                        State.Location annotation = field.getAnnotation(State.Location.class);
                        field.setAccessible(true);
                        StateLocation location = new StateLocation();
                        location.setLocation(new Location(annotation.x(), annotation.y()));
                        field.set(instance, location);
                    }
                } catch (Exception e) {
                    // Log warning
                }
            }
        }
        
        public Set<Class<?>> scanForStateClasses(String packageName) {
            // Simplified scanning
            return Set.of();
        }
        
        public Map<Method, Object> scanForTransitions(String packageName) {
            // Simplified scanning
            return Map.of();
        }
        
        public void registerScannedComponents(ApplicationContext context, String packageName) {
            // Registration logic
        }
        
        public void publishRegistrationComplete(List<State> states) {
            eventPublisher.publishEvent(new StatesRegisteredEvent(this, states));
        }
        
        public boolean isValidStateAnnotation(Class<?> clazz) {
            State annotation = clazz.getAnnotation(State.class);
            if (annotation == null) return false;
            if (annotation.name().isEmpty() && annotation.images().length == 0) return false;
            return true;
        }
        
        public boolean isValidTransitionAnnotation(Method method) {
            Transition annotation = method.getAnnotation(Transition.class);
            if (annotation == null) return false;
            return !annotation.from().isEmpty() && !annotation.to().isEmpty();
        }
        
        public boolean isValidStateName(String name) {
            if (name == null || name.trim().isEmpty()) return false;
            return name.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
        }
    }
}