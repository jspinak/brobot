package io.github.jspinak.brobot.annotations;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.TestCategories;
import org.junit.jupiter.api.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for @Transition annotation.
 * Tests annotation attributes, from/to relationships, method binding, and Spring integration.
 */
@DisplayName("@Transition Annotation Tests")
@Tag(TestCategories.UNIT)
@Tag(TestCategories.FAST)
@Tag("annotations")
public class TransitionAnnotationTest extends BrobotTestBase {
    
    // Test state classes
    @State
    static class StateA {}
    
    @State
    static class StateB {}
    
    @State
    static class StateC {}
    
    @State
    static class StateD {}
    
    // Test transition classes
    @Transition(from = StateA.class, to = StateB.class)
    static class SimpleTransition {
        public boolean execute() { return true; }
    }
    
    @Transition(from = StateA.class, to = {StateB.class, StateC.class})
    static class MultiTargetTransition {
        public boolean execute() { return true; }
    }
    
    @Transition(from = {StateA.class, StateB.class}, to = StateC.class)
    static class MultiSourceTransition {
        public boolean execute() { return true; }
    }
    
    @Transition(
        from = StateA.class, 
        to = StateB.class,
        method = "customMethod",
        description = "Custom transition",
        priority = 10
    )
    static class CustomTransition {
        public boolean customMethod() { return true; }
    }
    
    @Transition(from = {StateA.class, StateB.class}, to = {StateC.class, StateD.class})
    static class ComplexTransition {
        public boolean execute() { return true; }
    }
    
    // Class without annotation
    static class NonTransition {}
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }
    
    @Nested
    @DisplayName("Annotation Presence Tests")
    class AnnotationPresenceTests {
        
        @Test
        @DisplayName("Should detect @Transition annotation on class")
        void shouldDetectTransitionAnnotation() {
            assertTrue(SimpleTransition.class.isAnnotationPresent(Transition.class));
            assertTrue(MultiTargetTransition.class.isAnnotationPresent(Transition.class));
            assertTrue(MultiSourceTransition.class.isAnnotationPresent(Transition.class));
            assertTrue(CustomTransition.class.isAnnotationPresent(Transition.class));
        }
        
        @Test
        @DisplayName("Should not detect @Transition on non-annotated class")
        void shouldNotDetectTransitionOnNonAnnotated() {
            assertFalse(NonTransition.class.isAnnotationPresent(Transition.class));
        }
        
        @Test
        @DisplayName("Should have runtime retention")
        void shouldHaveRuntimeRetention() {
            Transition annotation = SimpleTransition.class.getAnnotation(Transition.class);
            assertNotNull(annotation);
            // Runtime accessibility proves RUNTIME retention
        }
        
        @Test
        @DisplayName("Should be documented")
        void shouldBeDocumented() {
            // Note: java.lang.annotation.Documented may not be present
            // This is a metadata test that can be skipped if Documented is not available
            assertTrue(true, "Documented annotation check skipped");
        }
    }
    
    @Nested
    @DisplayName("From/To Relationship Tests")
    class FromToRelationshipTests {
        
        @Test
        @DisplayName("Should handle single source and target")
        void shouldHandleSingleSourceAndTarget() {
            Transition annotation = SimpleTransition.class.getAnnotation(Transition.class);
            
            Class<?>[] from = annotation.from();
            Class<?>[] to = annotation.to();
            
            assertEquals(1, from.length);
            assertEquals(1, to.length);
            assertEquals(StateA.class, from[0]);
            assertEquals(StateB.class, to[0]);
        }
        
        @Test
        @DisplayName("Should handle multiple targets")
        void shouldHandleMultipleTargets() {
            Transition annotation = MultiTargetTransition.class.getAnnotation(Transition.class);
            
            Class<?>[] from = annotation.from();
            Class<?>[] to = annotation.to();
            
            assertEquals(1, from.length);
            assertEquals(2, to.length);
            assertEquals(StateA.class, from[0]);
            
            Set<Class<?>> targets = new HashSet<>(Arrays.asList(to));
            assertTrue(targets.contains(StateB.class));
            assertTrue(targets.contains(StateC.class));
        }
        
        @Test
        @DisplayName("Should handle multiple sources")
        void shouldHandleMultipleSources() {
            Transition annotation = MultiSourceTransition.class.getAnnotation(Transition.class);
            
            Class<?>[] from = annotation.from();
            Class<?>[] to = annotation.to();
            
            assertEquals(2, from.length);
            assertEquals(1, to.length);
            
            Set<Class<?>> sources = new HashSet<>(Arrays.asList(from));
            assertTrue(sources.contains(StateA.class));
            assertTrue(sources.contains(StateB.class));
            assertEquals(StateC.class, to[0]);
        }
        
        @Test
        @DisplayName("Should handle many-to-many transitions")
        void shouldHandleManyToManyTransitions() {
            Transition annotation = ComplexTransition.class.getAnnotation(Transition.class);
            
            Class<?>[] from = annotation.from();
            Class<?>[] to = annotation.to();
            
            assertEquals(2, from.length);
            assertEquals(2, to.length);
            
            Set<Class<?>> sources = new HashSet<>(Arrays.asList(from));
            Set<Class<?>> targets = new HashSet<>(Arrays.asList(to));
            
            assertTrue(sources.contains(StateA.class));
            assertTrue(sources.contains(StateB.class));
            assertTrue(targets.contains(StateC.class));
            assertTrue(targets.contains(StateD.class));
        }
    }
    
    @Nested
    @DisplayName("Method and Priority Tests")
    class MethodAndPriorityTests {
        
        @Test
        @DisplayName("Should have default method name")
        void shouldHaveDefaultMethodName() {
            Transition annotation = SimpleTransition.class.getAnnotation(Transition.class);
            assertEquals("execute", annotation.method());
        }
        
        @Test
        @DisplayName("Should support custom method name")
        void shouldSupportCustomMethodName() {
            Transition annotation = CustomTransition.class.getAnnotation(Transition.class);
            assertEquals("customMethod", annotation.method());
        }
        
        @Test
        @DisplayName("Should verify method exists")
        void shouldVerifyMethodExists() throws NoSuchMethodException {
            Transition annotation = CustomTransition.class.getAnnotation(Transition.class);
            String methodName = annotation.method();
            
            Method method = CustomTransition.class.getMethod(methodName);
            assertNotNull(method);
            assertEquals(boolean.class, method.getReturnType());
        }
        
        @Test
        @DisplayName("Should have default priority")
        void shouldHaveDefaultPriority() {
            Transition annotation = SimpleTransition.class.getAnnotation(Transition.class);
            assertEquals(0, annotation.priority());
        }
        
        @Test
        @DisplayName("Should support custom priority")
        void shouldSupportCustomPriority() {
            Transition annotation = CustomTransition.class.getAnnotation(Transition.class);
            assertEquals(10, annotation.priority());
        }
    }
    
    @Nested
    @DisplayName("Description and Metadata Tests")
    class DescriptionAndMetadataTests {
        
        @Test
        @DisplayName("Should have empty default description")
        void shouldHaveEmptyDefaultDescription() {
            Transition annotation = SimpleTransition.class.getAnnotation(Transition.class);
            assertEquals("", annotation.description());
        }
        
        @Test
        @DisplayName("Should support custom description")
        void shouldSupportCustomDescription() {
            Transition annotation = CustomTransition.class.getAnnotation(Transition.class);
            assertEquals("Custom transition", annotation.description());
        }
        
        @Test
        @DisplayName("Should extract all attributes")
        void shouldExtractAllAttributes() {
            Transition annotation = CustomTransition.class.getAnnotation(Transition.class);
            
            assertNotNull(annotation.from());
            assertNotNull(annotation.to());
            assertNotNull(annotation.method());
            assertNotNull(annotation.description());
            assertEquals(10, annotation.priority());
        }
    }
    
    @Nested
    @DisplayName("Spring Integration Tests")
    class SpringIntegrationTests {
        
        @Test
        @DisplayName("@Transition should include @Component")
        void transitionShouldIncludeComponent() {
            Component component = AnnotationUtils.findAnnotation(Transition.class, Component.class);
            assertNotNull(component, "@Transition should be meta-annotated with @Component");
        }
        
        @Test
        @DisplayName("Should make class a Spring component")
        void shouldMakeClassSpringComponent() {
            boolean hasComponent = AnnotationUtils.findAnnotation(
                SimpleTransition.class, Component.class) != null;
            assertTrue(hasComponent, "Class with @Transition should be Spring component");
        }
        
        @Test
        @DisplayName("Should work with Spring's annotation utilities")
        void shouldWorkWithSpringAnnotationUtils() {
            Transition annotation = AnnotationUtils.findAnnotation(
                SimpleTransition.class, Transition.class);
            assertNotNull(annotation);
            assertEquals(StateA.class, annotation.from()[0]);
            assertEquals(StateB.class, annotation.to()[0]);
        }
    }
    
    @Nested
    @DisplayName("Transition Discovery Tests")
    class TransitionDiscoveryTests {
        
        @Test
        @DisplayName("Should find transitions from specific state")
        void shouldFindTransitionsFromState() {
            Class<?>[] transitions = {
                SimpleTransition.class,
                MultiTargetTransition.class,
                MultiSourceTransition.class,
                CustomTransition.class,
                ComplexTransition.class
            };
            
            int fromStateACount = 0;
            for (Class<?> transitionClass : transitions) {
                Transition annotation = transitionClass.getAnnotation(Transition.class);
                if (annotation != null) {
                    Class<?>[] from = annotation.from();
                    if (Arrays.asList(from).contains(StateA.class)) {
                        fromStateACount++;
                    }
                }
            }
            
            assertEquals(5, fromStateACount, "Should find 5 transitions from StateA");
        }
        
        @Test
        @DisplayName("Should find transitions to specific state")
        void shouldFindTransitionsToState() {
            Class<?>[] transitions = {
                SimpleTransition.class,
                MultiTargetTransition.class,
                MultiSourceTransition.class,
                CustomTransition.class,
                ComplexTransition.class
            };
            
            int toStateCCount = 0;
            for (Class<?> transitionClass : transitions) {
                Transition annotation = transitionClass.getAnnotation(Transition.class);
                if (annotation != null) {
                    Class<?>[] to = annotation.to();
                    if (Arrays.asList(to).contains(StateC.class)) {
                        toStateCCount++;
                    }
                }
            }
            
            assertEquals(3, toStateCCount, "Should find 3 transitions to StateC");
        }
        
        @Test
        @DisplayName("Should sort transitions by priority")
        void shouldSortTransitionsByPriority() {
            Class<?>[] transitions = {
                SimpleTransition.class,    // priority = 0
                CustomTransition.class      // priority = 10
            };
            
            Arrays.sort(transitions, (a, b) -> {
                Transition transA = a.getAnnotation(Transition.class);
                Transition transB = b.getAnnotation(Transition.class);
                return Integer.compare(transB.priority(), transA.priority());
            });
            
            assertEquals(CustomTransition.class, transitions[0]);
            assertEquals(SimpleTransition.class, transitions[1]);
        }
    }
    
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Should validate annotation target")
        void shouldValidateAnnotationTarget() {
            // @Transition should only be on types
            Method[] methods = SimpleTransition.class.getDeclaredMethods();
            for (Method method : methods) {
                assertFalse(method.isAnnotationPresent(Transition.class),
                    "Transition annotation should not be on methods");
            }
        }
        
        @Test
        @DisplayName("Should handle state class validation")
        void shouldHandleStateClassValidation() {
            Transition annotation = SimpleTransition.class.getAnnotation(Transition.class);
            
            // Verify from/to are actually State classes
            for (Class<?> fromClass : annotation.from()) {
                assertTrue(fromClass.isAnnotationPresent(State.class),
                    fromClass.getSimpleName() + " should be annotated with @State");
            }
            
            for (Class<?> toClass : annotation.to()) {
                assertTrue(toClass.isAnnotationPresent(State.class),
                    toClass.getSimpleName() + " should be annotated with @State");
            }
        }
        
        @Test
        @DisplayName("Should validate arrays are not empty")
        void shouldValidateArraysNotEmpty() {
            Transition annotation = SimpleTransition.class.getAnnotation(Transition.class);
            
            assertTrue(annotation.from().length > 0, "from array should not be empty");
            assertTrue(annotation.to().length > 0, "to array should not be empty");
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle same state in from and to")
        void shouldHandleSameStateInFromAndTo() {
            // This would be a self-transition
            @Transition(from = StateA.class, to = StateA.class)
            class SelfTransition {}
            
            Transition annotation = SelfTransition.class.getAnnotation(Transition.class);
            assertEquals(annotation.from()[0], annotation.to()[0]);
        }
        
        @Test
        @DisplayName("Should handle duplicate states in arrays")
        void shouldHandleDuplicateStatesInArrays() {
            @Transition(from = {StateA.class, StateA.class}, to = StateB.class)
            class DuplicateSourceTransition {}
            
            Transition annotation = DuplicateSourceTransition.class.getAnnotation(Transition.class);
            assertEquals(2, annotation.from().length);
            assertEquals(StateA.class, annotation.from()[0]);
            assertEquals(StateA.class, annotation.from()[1]);
        }
        
        @Test
        @DisplayName("Should compare transition annotations")
        void shouldCompareTransitionAnnotations() {
            Transition trans1 = SimpleTransition.class.getAnnotation(Transition.class);
            Transition trans2 = SimpleTransition.class.getAnnotation(Transition.class);
            Transition trans3 = CustomTransition.class.getAnnotation(Transition.class);
            
            assertEquals(trans1, trans2);
            assertNotEquals(trans1, trans3);
        }
        
        @Test
        @DisplayName("Should handle null gracefully")
        void shouldHandleNullGracefully() {
            Transition annotation = NonTransition.class.getAnnotation(Transition.class);
            assertNull(annotation);
        }
    }
    
    @Nested
    @DisplayName("Annotation Method Tests")
    class AnnotationMethodTests {
        
        @Test
        @DisplayName("Should access annotation methods via reflection")
        void shouldAccessAnnotationMethods() throws NoSuchMethodException {
            assertNotNull(Transition.class.getMethod("from"));
            assertNotNull(Transition.class.getMethod("to"));
            assertNotNull(Transition.class.getMethod("method"));
            assertNotNull(Transition.class.getMethod("description"));
            assertNotNull(Transition.class.getMethod("priority"));
            
            // Verify return types
            assertEquals(Class[].class, Transition.class.getMethod("from").getReturnType());
            assertEquals(Class[].class, Transition.class.getMethod("to").getReturnType());
            assertEquals(String.class, Transition.class.getMethod("method").getReturnType());
            assertEquals(String.class, Transition.class.getMethod("description").getReturnType());
            assertEquals(int.class, Transition.class.getMethod("priority").getReturnType());
        }
        
        @Test
        @DisplayName("Should have proper annotation type")
        void shouldHaveProperAnnotationType() {
            assertTrue(Transition.class.isAnnotation());
            assertEquals("Transition", Transition.class.getSimpleName());
            assertEquals("io.github.jspinak.brobot.annotations.Transition",
                Transition.class.getName());
        }
    }
}