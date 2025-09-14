package io.github.jspinak.brobot.annotations;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.TestCategories;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;

/**
 * Comprehensive test suite for @TransitionSet, @FromTransition, @OutgoingTransition, and @IncomingTransition annotations.
 * Tests annotation attributes, relationships, method binding, and Spring integration.
 */
@DisplayName("Transition Annotations Tests")
@Tag(TestCategories.UNIT)
@Tag(TestCategories.FAST)
@Tag("annotations")
public class TransitionAnnotationTest extends BrobotTestBase {

    // Test state classes
    @State
    static class MenuState {}

    @State
    static class HomepageState {}

    @State
    static class PricingState {}

    @State
    static class ContactState {}

    @State
    static class CheckoutState {}

    // Simple TransitionSet with basic transitions
    @TransitionSet(state = PricingState.class)
    static class PricingTransitions {

        @FromTransition(from = MenuState.class, priority = 1)
        public boolean fromMenu() {
            return true;
        }

        @FromTransition(from = HomepageState.class)
        public boolean fromHomepage() {
            return true;
        }

        @IncomingTransition
        public boolean verifyArrival() {
            return true;
        }
    }

    // TransitionSet with custom attributes
    @TransitionSet(
            state = ContactState.class,
            name = "contact_page",
            description = "All transitions to Contact page")
    static class ContactTransitions {

        @FromTransition(
                from = MenuState.class,
                priority = 10,
                description = "Navigate from menu",
                timeout = 20)
        public boolean fromMenuWithPriority() {
            return true;
        }

        @FromTransition(from = PricingState.class)
        public boolean fromPricing() {
            return true;
        }

        @IncomingTransition(description = "Verify contact form is visible", timeout = 15)
        public boolean verifyContactForm() {
            return true;
        }
    }

    // TransitionSet with multiple FromTransitions from same source
    @TransitionSet(state = CheckoutState.class)
    static class CheckoutTransitions {

        @FromTransition(from = PricingState.class, priority = 1)
        public boolean fromPricingQuickCheckout() {
            return true;
        }

        @FromTransition(from = PricingState.class, priority = 2)
        public boolean fromPricingStandardCheckout() {
            return true;
        }

        @IncomingTransition(required = false)
        public boolean verifyCheckoutPage() {
            return true;
        }
    }

    // Invalid TransitionSet - multiple IncomingTransitions (for testing validation)
    @TransitionSet(state = MenuState.class)
    static class InvalidMultipleIncomingTransitions {

        @IncomingTransition
        public boolean verifyMethod1() {
            return true;
        }

        @IncomingTransition
        public boolean verifyMethod2() {
            return true;
        }
    }

    // Class without annotations
    static class NonTransitionClass {
        public boolean someMethod() {
            return true;
        }
    }

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Nested
    @DisplayName("TransitionSet Annotation Tests")
    class TransitionSetAnnotationTests {

        @Test
        @DisplayName("Should detect @TransitionSet annotation on class")
        void shouldDetectTransitionSetAnnotation() {
            assertTrue(PricingTransitions.class.isAnnotationPresent(TransitionSet.class));
            assertTrue(ContactTransitions.class.isAnnotationPresent(TransitionSet.class));
            assertTrue(CheckoutTransitions.class.isAnnotationPresent(TransitionSet.class));
        }

        @Test
        @DisplayName("Should not detect @TransitionSet on non-annotated class")
        void shouldNotDetectTransitionSetOnNonAnnotated() {
            assertFalse(NonTransitionClass.class.isAnnotationPresent(TransitionSet.class));
        }

        @Test
        @DisplayName("Should extract state from @TransitionSet")
        void shouldExtractStateFromTransitionSet() {
            TransitionSet annotation = PricingTransitions.class.getAnnotation(TransitionSet.class);
            assertNotNull(annotation);
            assertEquals(PricingState.class, annotation.state());
        }

        @Test
        @DisplayName("Should support custom name and description")
        void shouldSupportCustomNameAndDescription() {
            TransitionSet annotation = ContactTransitions.class.getAnnotation(TransitionSet.class);
            assertNotNull(annotation);
            assertEquals("contact_page", annotation.name());
            assertEquals("All transitions to Contact page", annotation.description());
        }

        @Test
        @DisplayName("Should have default values for optional attributes")
        void shouldHaveDefaultValuesForOptionalAttributes() {
            TransitionSet annotation = PricingTransitions.class.getAnnotation(TransitionSet.class);
            assertNotNull(annotation);
            assertEquals("", annotation.name());
            assertEquals("", annotation.description());
        }

        @Test
        @DisplayName("@TransitionSet should include @Component")
        void transitionSetShouldIncludeComponent() {
            Component component =
                    AnnotationUtils.findAnnotation(TransitionSet.class, Component.class);
            assertNotNull(component, "@TransitionSet should be meta-annotated with @Component");
        }

        @Test
        @DisplayName("Should make class a Spring component")
        void shouldMakeClassSpringComponent() {
            boolean hasComponent =
                    AnnotationUtils.findAnnotation(PricingTransitions.class, Component.class)
                            != null;
            assertTrue(hasComponent, "Class with @TransitionSet should be Spring component");
        }

        @Test
        @DisplayName("Should have runtime retention")
        void shouldHaveRuntimeRetention() {
            TransitionSet annotation = PricingTransitions.class.getAnnotation(TransitionSet.class);
            assertNotNull(annotation, "Runtime accessibility proves RUNTIME retention");
        }
    }

    @Nested
    @DisplayName("FromTransition Annotation Tests")
    class FromTransitionAnnotationTests {

        @Test
        @DisplayName("Should detect @FromTransition on methods")
        void shouldDetectFromTransitionOnMethods() throws NoSuchMethodException {
            Method fromMenu = PricingTransitions.class.getMethod("fromMenu");
            Method fromHomepage = PricingTransitions.class.getMethod("fromHomepage");

            assertTrue(fromMenu.isAnnotationPresent(FromTransition.class));
            assertTrue(fromHomepage.isAnnotationPresent(FromTransition.class));
        }

        @Test
        @DisplayName("Should extract source state from @FromTransition")
        void shouldExtractSourceState() throws NoSuchMethodException {
            Method fromMenu = PricingTransitions.class.getMethod("fromMenu");
            FromTransition annotation = fromMenu.getAnnotation(FromTransition.class);

            assertNotNull(annotation);
            assertEquals(MenuState.class, annotation.from());
        }

        @Test
        @DisplayName("Should support priority attribute")
        void shouldSupportPriority() throws NoSuchMethodException {
            Method fromMenu = PricingTransitions.class.getMethod("fromMenu");
            FromTransition annotation = fromMenu.getAnnotation(FromTransition.class);

            assertEquals(1, annotation.priority());
        }

        @Test
        @DisplayName("Should have default priority of 0")
        void shouldHaveDefaultPriority() throws NoSuchMethodException {
            Method fromHomepage = PricingTransitions.class.getMethod("fromHomepage");
            FromTransition annotation = fromHomepage.getAnnotation(FromTransition.class);

            assertEquals(0, annotation.priority());
        }

        @Test
        @DisplayName("Should support custom description and timeout")
        void shouldSupportCustomDescriptionAndTimeout() throws NoSuchMethodException {
            Method fromMenu = ContactTransitions.class.getMethod("fromMenuWithPriority");
            FromTransition annotation = fromMenu.getAnnotation(FromTransition.class);

            assertNotNull(annotation);
            assertEquals("Navigate from menu", annotation.description());
            assertEquals(20, annotation.timeout());
        }

        @Test
        @DisplayName("Should have default timeout of 10 seconds")
        void shouldHaveDefaultTimeout() throws NoSuchMethodException {
            Method fromMenu = PricingTransitions.class.getMethod("fromMenu");
            FromTransition annotation = fromMenu.getAnnotation(FromTransition.class);

            assertEquals(10, annotation.timeout());
        }

        @Test
        @DisplayName("Should find all FromTransitions in a class")
        void shouldFindAllFromTransitionsInClass() {
            Method[] methods = PricingTransitions.class.getDeclaredMethods();
            List<Method> fromTransitions =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(FromTransition.class))
                            .collect(Collectors.toList());

            assertEquals(2, fromTransitions.size());
        }

        @Test
        @DisplayName("Should allow multiple transitions from same source state")
        void shouldAllowMultipleTransitionsFromSameSource() {
            Method[] methods = CheckoutTransitions.class.getDeclaredMethods();
            List<Method> fromPricing =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(FromTransition.class))
                            .filter(
                                    m ->
                                            m.getAnnotation(FromTransition.class).from()
                                                    == PricingState.class)
                            .collect(Collectors.toList());

            assertEquals(2, fromPricing.size(), "Should have 2 transitions from PricingState");
        }

        @Test
        @DisplayName("Should sort transitions by priority")
        void shouldSortTransitionsByPriority() throws NoSuchMethodException {
            Method quickCheckout = CheckoutTransitions.class.getMethod("fromPricingQuickCheckout");
            Method standardCheckout =
                    CheckoutTransitions.class.getMethod("fromPricingStandardCheckout");

            FromTransition quick = quickCheckout.getAnnotation(FromTransition.class);
            FromTransition standard = standardCheckout.getAnnotation(FromTransition.class);

            assertTrue(
                    quick.priority() < standard.priority(),
                    "Quick checkout should have higher priority (lower number)");
        }
    }

    @Nested
    @DisplayName("IncomingTransition Annotation Tests")
    class IncomingTransitionAnnotationTests {

        @Test
        @DisplayName("Should detect @IncomingTransition on method")
        void shouldDetectIncomingTransitionOnMethod() throws NoSuchMethodException {
            Method verifyArrival = PricingTransitions.class.getMethod("verifyArrival");
            assertTrue(verifyArrival.isAnnotationPresent(IncomingTransition.class));
        }

        @Test
        @DisplayName("Should have default values for optional attributes")
        void shouldHaveDefaultValuesForOptionalAttributes() throws NoSuchMethodException {
            Method verifyArrival = PricingTransitions.class.getMethod("verifyArrival");
            IncomingTransition annotation = verifyArrival.getAnnotation(IncomingTransition.class);

            assertNotNull(annotation);
            assertEquals("", annotation.description());
            assertEquals(5, annotation.timeout());
            assertTrue(annotation.required());
        }

        @Test
        @DisplayName("Should support custom description and timeout")
        void shouldSupportCustomDescriptionAndTimeout() throws NoSuchMethodException {
            Method verifyContact = ContactTransitions.class.getMethod("verifyContactForm");
            IncomingTransition annotation = verifyContact.getAnnotation(IncomingTransition.class);

            assertNotNull(annotation);
            assertEquals("Verify contact form is visible", annotation.description());
            assertEquals(15, annotation.timeout());
        }

        @Test
        @DisplayName("Should support required attribute")
        void shouldSupportRequiredAttribute() throws NoSuchMethodException {
            Method verifyCheckout = CheckoutTransitions.class.getMethod("verifyCheckoutPage");
            IncomingTransition annotation = verifyCheckout.getAnnotation(IncomingTransition.class);

            assertNotNull(annotation);
            assertFalse(annotation.required());
        }

        @Test
        @DisplayName("Should find single IncomingTransition in a class")
        void shouldFindSingleIncomingTransitionInClass() {
            Method[] methods = PricingTransitions.class.getDeclaredMethods();
            List<Method> incomingTransitions =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(IncomingTransition.class))
                            .collect(Collectors.toList());

            assertEquals(1, incomingTransitions.size(), "Should have exactly one IncomingTransition");
        }

        @Test
        @DisplayName("Should detect multiple IncomingTransitions (validation scenario)")
        void shouldDetectMultipleIncomingTransitions() {
            Method[] methods = InvalidMultipleIncomingTransitions.class.getDeclaredMethods();
            List<Method> incomingTransitions =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(IncomingTransition.class))
                            .collect(Collectors.toList());

            assertTrue(
                    incomingTransitions.size() > 1,
                    "Should detect multiple IncomingTransitions for validation purposes");
        }
    }

    @Nested
    @DisplayName("Transition Relationship Tests")
    class TransitionRelationshipTests {

        @Test
        @DisplayName("Should identify all transitions to a specific state")
        void shouldIdentifyAllTransitionsToState() {
            TransitionSet annotation = PricingTransitions.class.getAnnotation(TransitionSet.class);
            assertEquals(PricingState.class, annotation.state());

            Method[] methods = PricingTransitions.class.getDeclaredMethods();
            List<Class<?>> sourceStates =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(FromTransition.class))
                            .map(m -> m.getAnnotation(FromTransition.class).from())
                            .collect(Collectors.toList());

            assertTrue(sourceStates.contains(MenuState.class));
            assertTrue(sourceStates.contains(HomepageState.class));
        }

        @Test
        @DisplayName("Should identify all transitions from a specific state")
        void shouldIdentifyAllTransitionsFromState() {
            // Find all TransitionSets
            Class<?>[] transitionSets = {
                PricingTransitions.class, ContactTransitions.class, CheckoutTransitions.class
            };

            // Count transitions from MenuState
            int fromMenuCount = 0;
            for (Class<?> tsClass : transitionSets) {
                Method[] methods = tsClass.getDeclaredMethods();
                fromMenuCount +=
                        Arrays.stream(methods)
                                .filter(m -> m.isAnnotationPresent(FromTransition.class))
                                .filter(
                                        m ->
                                                m.getAnnotation(FromTransition.class).from()
                                                        == MenuState.class)
                                .count();
            }

            assertEquals(2, fromMenuCount, "Should find 2 transitions from MenuState");
        }

        @Test
        @DisplayName("Should map source states to target states")
        void shouldMapSourceStatesToTargetStates() {
            TransitionSet tsAnnotation =
                    PricingTransitions.class.getAnnotation(TransitionSet.class);
            Class<?> targetState = tsAnnotation.state();

            Method[] methods = PricingTransitions.class.getDeclaredMethods();
            List<Class<?>> sourceStates =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(FromTransition.class))
                            .map(m -> m.getAnnotation(FromTransition.class).from())
                            .collect(Collectors.toList());

            // All source states should transition to PricingState
            assertFalse(sourceStates.isEmpty());
            sourceStates.forEach(
                    source -> {
                        assertNotNull(source, "Source state should not be null");
                        // Conceptually, all these source states lead to targetState (PricingState)
                    });
            assertEquals(PricingState.class, targetState);
        }
    }

    @Nested
    @DisplayName("Method Signature Tests")
    class MethodSignatureTests {

        @Test
        @DisplayName("FromTransition methods should return boolean")
        void fromTransitionMethodsShouldReturnBoolean() {
            Method[] methods = PricingTransitions.class.getDeclaredMethods();
            Arrays.stream(methods)
                    .filter(m -> m.isAnnotationPresent(FromTransition.class))
                    .forEach(
                            m ->
                                    assertEquals(
                                            boolean.class,
                                            m.getReturnType(),
                                            "FromTransition method should return boolean"));
        }

        @Test
        @DisplayName("IncomingTransition methods should return boolean")
        void incomingTransitionMethodsShouldReturnBoolean() {
            Method[] methods = PricingTransitions.class.getDeclaredMethods();
            Arrays.stream(methods)
                    .filter(m -> m.isAnnotationPresent(IncomingTransition.class))
                    .forEach(
                            m ->
                                    assertEquals(
                                            boolean.class,
                                            m.getReturnType(),
                                            "IncomingTransition method should return boolean"));
        }

        @Test
        @DisplayName("Should not have @FromTransition on non-methods")
        void shouldNotHaveFromTransitionOnNonMethods() {
            assertFalse(
                    PricingTransitions.class.isAnnotationPresent(FromTransition.class),
                    "FromTransition should not be on class");
        }

        @Test
        @DisplayName("Should not have @IncomingTransition on class")
        void shouldNotHaveIncomingTransitionOnClass() {
            assertFalse(
                    PricingTransitions.class.isAnnotationPresent(IncomingTransition.class),
                    "IncomingTransition should not be on class");
        }
    }

    @Nested
    @DisplayName("Annotation Metadata Tests")
    class AnnotationMetadataTests {

        @Test
        @DisplayName("Should access TransitionSet annotation methods via reflection")
        void shouldAccessTransitionSetMethods() throws NoSuchMethodException {
            assertNotNull(TransitionSet.class.getMethod("state"));
            assertNotNull(TransitionSet.class.getMethod("name"));
            assertNotNull(TransitionSet.class.getMethod("description"));

            assertEquals(Class.class, TransitionSet.class.getMethod("state").getReturnType());
            assertEquals(String.class, TransitionSet.class.getMethod("name").getReturnType());
            assertEquals(
                    String.class, TransitionSet.class.getMethod("description").getReturnType());
        }

        @Test
        @DisplayName("Should access FromTransition annotation methods via reflection")
        void shouldAccessFromTransitionMethods() throws NoSuchMethodException {
            assertNotNull(FromTransition.class.getMethod("from"));
            assertNotNull(FromTransition.class.getMethod("priority"));
            assertNotNull(FromTransition.class.getMethod("description"));
            assertNotNull(FromTransition.class.getMethod("timeout"));

            assertEquals(Class.class, FromTransition.class.getMethod("from").getReturnType());
            assertEquals(int.class, FromTransition.class.getMethod("priority").getReturnType());
            assertEquals(
                    String.class, FromTransition.class.getMethod("description").getReturnType());
            assertEquals(int.class, FromTransition.class.getMethod("timeout").getReturnType());
        }

        @Test
        @DisplayName("Should access IncomingTransition annotation methods via reflection")
        void shouldAccessIncomingTransitionMethods() throws NoSuchMethodException {
            assertNotNull(IncomingTransition.class.getMethod("description"));
            assertNotNull(IncomingTransition.class.getMethod("timeout"));
            assertNotNull(IncomingTransition.class.getMethod("required"));

            assertEquals(String.class, IncomingTransition.class.getMethod("description").getReturnType());
            assertEquals(int.class, IncomingTransition.class.getMethod("timeout").getReturnType());
            assertEquals(boolean.class, IncomingTransition.class.getMethod("required").getReturnType());
        }

        @Test
        @DisplayName("Should verify annotation types")
        void shouldVerifyAnnotationTypes() {
            assertTrue(TransitionSet.class.isAnnotation());
            assertTrue(FromTransition.class.isAnnotation());
            assertTrue(IncomingTransition.class.isAnnotation());
            assertTrue(OutgoingTransition.class.isAnnotation());

            assertEquals("TransitionSet", TransitionSet.class.getSimpleName());
            assertEquals("FromTransition", FromTransition.class.getSimpleName());
            assertEquals("IncomingTransition", IncomingTransition.class.getSimpleName());
            assertEquals("OutgoingTransition", OutgoingTransition.class.getSimpleName());
        }
    }

    @Nested
    @DisplayName("Spring Integration Tests")
    class SpringIntegrationTests {

        @Test
        @DisplayName("Should work with Spring's annotation utilities for TransitionSet")
        void shouldWorkWithSpringAnnotationUtilsForTransitionSet() {
            TransitionSet annotation =
                    AnnotationUtils.findAnnotation(PricingTransitions.class, TransitionSet.class);
            assertNotNull(annotation);
            assertEquals(PricingState.class, annotation.state());
        }

        @Test
        @DisplayName("Should work with Spring's annotation utilities for FromTransition")
        void shouldWorkWithSpringAnnotationUtilsForFromTransition() throws NoSuchMethodException {
            Method fromMenu = PricingTransitions.class.getMethod("fromMenu");
            FromTransition annotation =
                    AnnotationUtils.findAnnotation(fromMenu, FromTransition.class);
            assertNotNull(annotation);
            assertEquals(MenuState.class, annotation.from());
        }

        @Test
        @DisplayName("Should work with Spring's annotation utilities for IncomingTransition")
        void shouldWorkWithSpringAnnotationUtilsForIncomingTransition() throws NoSuchMethodException {
            Method verifyArrival = PricingTransitions.class.getMethod("verifyArrival");
            IncomingTransition annotation =
                    AnnotationUtils.findAnnotation(verifyArrival, IncomingTransition.class);
            assertNotNull(annotation);
            assertTrue(annotation.required());
        }

        @Test
        @DisplayName("TransitionSet classes should be discoverable as Spring components")
        void transitionSetClassesShouldBeDiscoverableAsSpringComponents() {
            // Verify that classes with @TransitionSet are treated as Spring components
            Component component =
                    AnnotationUtils.findAnnotation(PricingTransitions.class, Component.class);
            assertNotNull(component, "TransitionSet classes should be Spring components");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation Tests")
    class EdgeCasesAndValidationTests {

        @Test
        @DisplayName("Should handle TransitionSet without FromTransitions")
        void shouldHandleTransitionSetWithoutFromTransitions() {
            @TransitionSet(state = MenuState.class)
            class OnlyIncomingTransition {
                @IncomingTransition
                public boolean verify() {
                    return true;
                }
            }

            TransitionSet annotation = OnlyIncomingTransition.class.getAnnotation(TransitionSet.class);
            assertNotNull(annotation);

            Method[] methods = OnlyIncomingTransition.class.getDeclaredMethods();
            long fromCount =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(FromTransition.class))
                            .count();
            long incomingCount =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(IncomingTransition.class))
                            .count();

            assertEquals(0, fromCount);
            assertEquals(1, incomingCount);
        }

        @Test
        @DisplayName("Should handle TransitionSet without IncomingTransition")
        void shouldHandleTransitionSetWithoutIncomingTransition() {
            @TransitionSet(state = MenuState.class)
            class OnlyFromTransitions {
                @FromTransition(from = HomepageState.class)
                public boolean fromHome() {
                    return true;
                }
            }

            TransitionSet annotation = OnlyFromTransitions.class.getAnnotation(TransitionSet.class);
            assertNotNull(annotation);

            Method[] methods = OnlyFromTransitions.class.getDeclaredMethods();
            long fromCount =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(FromTransition.class))
                            .count();
            long incomingCount =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(IncomingTransition.class))
                            .count();

            assertEquals(1, fromCount);
            assertEquals(0, incomingCount);
        }

        @Test
        @DisplayName("Should validate state classes have @State annotation")
        void shouldValidateStateClassesHaveStateAnnotation() {
            TransitionSet tsAnnotation =
                    PricingTransitions.class.getAnnotation(TransitionSet.class);
            Class<?> targetState = tsAnnotation.state();

            assertTrue(
                    targetState.isAnnotationPresent(State.class),
                    "Target state should be annotated with @State");

            Method[] methods = PricingTransitions.class.getDeclaredMethods();
            Arrays.stream(methods)
                    .filter(m -> m.isAnnotationPresent(FromTransition.class))
                    .map(m -> m.getAnnotation(FromTransition.class).from())
                    .forEach(
                            sourceState ->
                                    assertTrue(
                                            sourceState.isAnnotationPresent(State.class),
                                            sourceState.getSimpleName()
                                                    + " should be annotated with @State"));
        }

        @Test
        @DisplayName("Should handle null gracefully")
        void shouldHandleNullGracefully() {
            TransitionSet tsAnnotation =
                    NonTransitionClass.class.getAnnotation(TransitionSet.class);
            assertNull(tsAnnotation);

            try {
                Method method = NonTransitionClass.class.getMethod("someMethod");
                FromTransition fromAnnotation = method.getAnnotation(FromTransition.class);
                IncomingTransition incomingAnnotation = method.getAnnotation(IncomingTransition.class);
                assertNull(fromAnnotation);
                assertNull(incomingAnnotation);
            } catch (NoSuchMethodException e) {
                fail("Method should exist");
            }
        }

        @Test
        @DisplayName("Should compare annotations for equality")
        void shouldCompareAnnotationsForEquality() throws NoSuchMethodException {
            Method fromMenu1 = PricingTransitions.class.getMethod("fromMenu");
            Method fromMenu2 = PricingTransitions.class.getMethod("fromMenu");
            Method fromHomepage = PricingTransitions.class.getMethod("fromHomepage");

            FromTransition annotation1 = fromMenu1.getAnnotation(FromTransition.class);
            FromTransition annotation2 = fromMenu2.getAnnotation(FromTransition.class);
            FromTransition annotation3 = fromHomepage.getAnnotation(FromTransition.class);

            assertEquals(annotation1, annotation2);
            assertNotEquals(annotation1, annotation3);
        }
    }
}
