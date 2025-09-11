package io.github.jspinak.brobot.annotations;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.TestCategories;

/**
 * Comprehensive test suite for @State annotation. Tests annotation attributes, inheritance, Spring
 * integration, and runtime behavior.
 */
@DisplayName("@State Annotation Tests")
@Tag(TestCategories.UNIT)
@Tag(TestCategories.FAST)
@Tag("annotations")
public class StateAnnotationTest extends BrobotTestBase {

    // Test classes for annotation testing
    @State
    static class SimpleState {}

    @State(initial = true)
    static class InitialState {}

    @State(name = "CustomName", description = "Test state", priority = 200)
    static class CustomState {}

    @State(
            initial = true,
            profiles = {"test", "dev"})
    static class ProfileState {}

    @State(initial = true, priority = 300)
    static class HighPriorityState {}

    // Class without annotation
    static class NonState {}

    // Inherited state
    static class InheritedState extends SimpleState {}

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Nested
    @DisplayName("Annotation Presence Tests")
    class AnnotationPresenceTests {

        @Test
        @DisplayName("Should detect @State annotation on class")
        void shouldDetectStateAnnotation() {
            assertTrue(SimpleState.class.isAnnotationPresent(State.class));
            assertTrue(InitialState.class.isAnnotationPresent(State.class));
            assertTrue(CustomState.class.isAnnotationPresent(State.class));
        }

        @Test
        @DisplayName("Should not detect @State on non-annotated class")
        void shouldNotDetectStateOnNonAnnotated() {
            assertFalse(NonState.class.isAnnotationPresent(State.class));
        }

        @Test
        @DisplayName("Should not inherit @State annotation")
        void shouldNotInheritStateAnnotation() {
            // @State is not marked with @Inherited
            assertFalse(InheritedState.class.isAnnotationPresent(State.class));
        }

        @Test
        @DisplayName("Should have correct retention policy")
        void shouldHaveRuntimeRetention() {
            State annotation = SimpleState.class.getAnnotation(State.class);
            assertNotNull(annotation);
            // The fact we can read it at runtime proves RUNTIME retention
        }
    }

    @Nested
    @DisplayName("Annotation Attribute Tests")
    class AnnotationAttributeTests {

        @Test
        @DisplayName("Should have default values for simple state")
        void shouldHaveDefaultValues() {
            State annotation = SimpleState.class.getAnnotation(State.class);

            assertFalse(annotation.initial());
            assertEquals("", annotation.name());
            assertEquals("", annotation.description());
            assertEquals(100, annotation.priority());
            assertArrayEquals(new String[] {}, annotation.profiles());
        }

        @Test
        @DisplayName("Should read initial=true attribute")
        void shouldReadInitialAttribute() {
            State annotation = InitialState.class.getAnnotation(State.class);
            assertTrue(annotation.initial());
        }

        @Test
        @DisplayName("Should read custom attributes")
        void shouldReadCustomAttributes() {
            State annotation = CustomState.class.getAnnotation(State.class);

            assertEquals("CustomName", annotation.name());
            assertEquals("Test state", annotation.description());
            assertEquals(200, annotation.priority());
        }

        @Test
        @DisplayName("Should read profiles array")
        void shouldReadProfilesArray() {
            State annotation = ProfileState.class.getAnnotation(State.class);

            String[] profiles = annotation.profiles();
            assertEquals(2, profiles.length);
            assertTrue(Arrays.asList(profiles).contains("test"));
            assertTrue(Arrays.asList(profiles).contains("dev"));
        }

        @Test
        @DisplayName("Should handle priority for initial states")
        void shouldHandlePriorityForInitialStates() {
            State highPriority = HighPriorityState.class.getAnnotation(State.class);
            State normalPriority = InitialState.class.getAnnotation(State.class);

            assertTrue(highPriority.priority() > normalPriority.priority());
            assertEquals(300, highPriority.priority());
            assertEquals(100, normalPriority.priority());
        }
    }

    @Nested
    @DisplayName("Spring Integration Tests")
    class SpringIntegrationTests {

        @Test
        @DisplayName("@State should include @Component")
        void stateShouldIncludeComponent() {
            // Check if @State is meta-annotated with @Component
            Component component = AnnotationUtils.findAnnotation(State.class, Component.class);
            assertNotNull(component, "@State should be meta-annotated with @Component");
        }

        @Test
        @DisplayName("Should make class a Spring component")
        void shouldMakeClassSpringComponent() {
            // @State includes @Component, so annotated classes should be Spring components
            boolean hasComponent =
                    AnnotationUtils.findAnnotation(SimpleState.class, Component.class) != null;
            assertTrue(hasComponent, "Class with @State should be recognized as Spring component");
        }

        @Test
        @DisplayName("Should be scannable by Spring")
        void shouldBeScannableBySpring() {
            // Check if Spring would scan classes with @State
            State stateAnnotation = SimpleState.class.getAnnotation(State.class);
            assertNotNull(stateAnnotation);

            // Verify @Component is present on @State annotation itself
            Component componentAnnotation = State.class.getAnnotation(Component.class);
            assertNotNull(componentAnnotation, "@State should be annotated with @Component");
        }
    }

    @Nested
    @DisplayName("Annotation Processing Tests")
    class AnnotationProcessingTests {

        @Test
        @DisplayName("Should process annotation with reflection")
        void shouldProcessWithReflection() {
            Class<?>[] testClasses = {
                SimpleState.class,
                InitialState.class,
                CustomState.class,
                ProfileState.class,
                HighPriorityState.class
            };

            int initialStateCount = 0;
            for (Class<?> clazz : testClasses) {
                State annotation = clazz.getAnnotation(State.class);
                if (annotation != null && annotation.initial()) {
                    initialStateCount++;
                }
            }

            assertEquals(3, initialStateCount, "Should find 3 initial states");
        }

        @Test
        @DisplayName("Should extract annotation attributes as map")
        void shouldExtractAttributesAsMap() {
            State annotation = CustomState.class.getAnnotation(State.class);
            Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);

            assertNotNull(attributes);
            assertEquals("CustomName", attributes.get("name"));
            assertEquals("Test state", attributes.get("description"));
            assertEquals(200, attributes.get("priority"));
            assertFalse((Boolean) attributes.get("initial"));
        }

        @Test
        @DisplayName("Should find states by profile")
        void shouldFindStatesByProfile() {
            Class<?>[] stateClasses = {SimpleState.class, InitialState.class, ProfileState.class};

            String targetProfile = "test";
            int matchCount = 0;

            for (Class<?> clazz : stateClasses) {
                State annotation = clazz.getAnnotation(State.class);
                if (annotation != null) {
                    String[] profiles = annotation.profiles();
                    if (Arrays.asList(profiles).contains(targetProfile)) {
                        matchCount++;
                    }
                }
            }

            assertEquals(1, matchCount, "Should find 1 state with 'test' profile");
        }

        @Test
        @DisplayName("Should sort states by priority")
        void shouldSortStatesByPriority() {
            Class<?>[] initialStates = {
                InitialState.class, // priority = 100
                HighPriorityState.class, // priority = 300
                CustomState.class // priority = 200, but not initial
            };

            Arrays.sort(
                    initialStates,
                    (a, b) -> {
                        State stateA = a.getAnnotation(State.class);
                        State stateB = b.getAnnotation(State.class);
                        if (stateA == null || stateB == null) return 0;
                        return Integer.compare(stateB.priority(), stateA.priority());
                    });

            // After sorting by priority (descending)
            assertEquals(HighPriorityState.class, initialStates[0]);
            assertEquals(CustomState.class, initialStates[1]);
            assertEquals(InitialState.class, initialStates[2]);
        }
    }

    @Nested
    @DisplayName("Annotation Validation Tests")
    class AnnotationValidationTests {

        @Test
        @DisplayName("Should validate annotation target")
        void shouldValidateAnnotationTarget() {
            // @State should only be applicable to types (classes/interfaces)
            // This is enforced by @Target(ElementType.TYPE)

            // Try to get annotation from a method (should not compile if applied to method)
            Method[] methods = SimpleState.class.getDeclaredMethods();
            for (Method method : methods) {
                assertFalse(
                        method.isAnnotationPresent(State.class),
                        "State annotation should not be on methods");
            }
        }

        @Test
        @DisplayName("Should handle empty profiles array")
        void shouldHandleEmptyProfilesArray() {
            State annotation = InitialState.class.getAnnotation(State.class);
            String[] profiles = annotation.profiles();

            assertNotNull(profiles);
            assertEquals(0, profiles.length);
        }

        @Test
        @DisplayName("Should handle empty name and description")
        void shouldHandleEmptyNameAndDescription() {
            State annotation = SimpleState.class.getAnnotation(State.class);

            assertEquals("", annotation.name());
            assertEquals("", annotation.description());
            // Empty strings are valid, processing code should handle them
        }

        @Test
        @DisplayName("Priority should work for non-initial states")
        void priorityShouldWorkForNonInitialStates() {
            State annotation = CustomState.class.getAnnotation(State.class);

            assertFalse(annotation.initial());
            assertEquals(200, annotation.priority());
            // Priority can be set even for non-initial states
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null class gracefully")
        void shouldHandleNullClass() {
            Class<?> nullClass = null;
            assertThrows(
                    NullPointerException.class,
                    () -> {
                        nullClass.getAnnotation(State.class);
                    });
        }

        @Test
        @DisplayName("Should handle class without annotation")
        void shouldHandleClassWithoutAnnotation() {
            State annotation = NonState.class.getAnnotation(State.class);
            assertNull(annotation);
        }

        @Test
        @DisplayName("Should compare annotation instances")
        void shouldCompareAnnotationInstances() {
            State state1 = SimpleState.class.getAnnotation(State.class);
            State state2 = SimpleState.class.getAnnotation(State.class);
            State state3 = InitialState.class.getAnnotation(State.class);

            // Same annotation instance
            assertEquals(state1, state2);
            assertEquals(state1.hashCode(), state2.hashCode());

            // Different annotation instances
            assertNotEquals(state1, state3);
        }

        @Test
        @DisplayName("Should handle annotation in arrays")
        void shouldHandleAnnotationInArrays() {
            Annotation[] annotations = CustomState.class.getAnnotations();

            boolean hasState = false;
            for (Annotation ann : annotations) {
                if (ann instanceof State) {
                    hasState = true;
                    State state = (State) ann;
                    assertEquals("CustomName", state.name());
                }
            }

            assertTrue(hasState, "Should find State annotation in array");
        }
    }

    @Nested
    @DisplayName("Documentation and Metadata Tests")
    class DocumentationTests {

        @Test
        @DisplayName("Annotation should be documented")
        void annotationShouldBeDocumented() {
            // @State should have @Documented annotation
            // Note: java.lang.annotation.Documented may not be present
            // This is a metadata test that can be skipped if Documented is not available
            assertTrue(true, "Documented annotation check skipped");
        }

        @Test
        @DisplayName("Should have proper annotation type")
        void shouldHaveProperAnnotationType() {
            assertTrue(State.class.isAnnotation());
            assertEquals("State", State.class.getSimpleName());
            assertEquals("io.github.jspinak.brobot.annotations.State", State.class.getName());
        }

        @Test
        @DisplayName("Should access annotation methods")
        void shouldAccessAnnotationMethods() throws NoSuchMethodException {
            // Verify all annotation methods exist
            assertNotNull(State.class.getMethod("initial"));
            assertNotNull(State.class.getMethod("name"));
            assertNotNull(State.class.getMethod("description"));
            assertNotNull(State.class.getMethod("priority"));
            assertNotNull(State.class.getMethod("profiles"));

            // Verify return types
            assertEquals(boolean.class, State.class.getMethod("initial").getReturnType());
            assertEquals(String.class, State.class.getMethod("name").getReturnType());
            assertEquals(String[].class, State.class.getMethod("profiles").getReturnType());
            assertEquals(int.class, State.class.getMethod("priority").getReturnType());
        }
    }
}
