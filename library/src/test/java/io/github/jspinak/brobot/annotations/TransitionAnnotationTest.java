package io.github.jspinak.brobot.annotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.TestCategories;

/**
 * Comprehensive test suite for @TransitionSet, @OutgoingTransition, and @IncomingTransition
 * annotations. Tests annotation attributes, relationships, method binding, and Spring integration.
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

        @OutgoingTransition(
                activate = {MenuState.class},
                pathCost = 1)
        public boolean toMenu() {
            return true;
        }

        @OutgoingTransition(activate = {HomepageState.class})
        public boolean toHomepage() {
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
            description = "All transitions from Contact page")
    static class ContactTransitions {

        @OutgoingTransition(
                activate = {MenuState.class},
                pathCost = 10,
                description = "Navigate to menu")
        public boolean toMenuWithPathCost() {
            return true;
        }

        @OutgoingTransition(activate = {PricingState.class})
        public boolean toPricing() {
            return true;
        }

        @IncomingTransition(description = "Verify contact form is visible", timeout = 15)
        public boolean verifyContactForm() {
            return true;
        }
    }

    // TransitionSet with multiple OutgoingTransitions to same target
    @TransitionSet(state = CheckoutState.class)
    static class CheckoutTransitions {

        @OutgoingTransition(
                activate = {PricingState.class},
                pathCost = 1)
        public boolean toPricingQuickCheckout() {
            return true;
        }

        @OutgoingTransition(
                activate = {PricingState.class},
                pathCost = 2)
        public boolean toPricingStandardCheckout() {
            return true;
        }

        @IncomingTransition
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
        void shouldExtractStateOutgoingTransitionSet() {
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
            assertEquals("All transitions from Contact page", annotation.description());
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
    @DisplayName("OutgoingTransition Annotation Tests")
    class OutgoingTransitionAnnotationTests {

        @Test
        @DisplayName("Should detect @OutgoingTransition on methods")
        void shouldDetectOutgoingTransitionOnMethods() throws NoSuchMethodException {
            Method toMenu = PricingTransitions.class.getMethod("toMenu");
            Method toHomepage = PricingTransitions.class.getMethod("toHomepage");

            assertTrue(toMenu.isAnnotationPresent(OutgoingTransition.class));
            assertTrue(toHomepage.isAnnotationPresent(OutgoingTransition.class));
        }

        @Test
        @DisplayName("Should extract target states from @OutgoingTransition")
        void shouldExtractTargetStates() throws NoSuchMethodException {
            Method toMenu = PricingTransitions.class.getMethod("toMenu");
            OutgoingTransition annotation = toMenu.getAnnotation(OutgoingTransition.class);

            assertNotNull(annotation);
            assertArrayEquals(new Class<?>[] {MenuState.class}, annotation.activate());
        }

        @Test
        @DisplayName("Should support pathCost attribute")
        void shouldSupportPathCost() throws NoSuchMethodException {
            Method toMenu = PricingTransitions.class.getMethod("toMenu");
            OutgoingTransition annotation = toMenu.getAnnotation(OutgoingTransition.class);

            assertEquals(1, annotation.pathCost());
        }

        @Test
        @DisplayName("Should have default pathCost of 1")
        void shouldHaveDefaultPathCost() throws NoSuchMethodException {
            Method toHomepage = PricingTransitions.class.getMethod("toHomepage");
            OutgoingTransition annotation = toHomepage.getAnnotation(OutgoingTransition.class);

            assertEquals(1, annotation.pathCost());
        }

        @Test
        @DisplayName("Should support custom description")
        void shouldSupportCustomDescription() throws NoSuchMethodException {
            Method toMenu = ContactTransitions.class.getMethod("toMenuWithPathCost");
            OutgoingTransition annotation = toMenu.getAnnotation(OutgoingTransition.class);

            assertNotNull(annotation);
            assertEquals("Navigate to menu", annotation.description());
        }

        @Test
        @DisplayName("Should find all OutgoingTransitions in a class")
        void shouldFindAllOutgoingTransitionsInClass() {
            Method[] methods = PricingTransitions.class.getDeclaredMethods();
            List<Method> fromTransitions =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(OutgoingTransition.class))
                            .collect(Collectors.toList());

            assertEquals(2, fromTransitions.size());
        }

        @Test
        @DisplayName("Should allow multiple transitions from same source state")
        void shouldAllowMultipleTransitionsFromSameSource() {
            Method[] methods = CheckoutTransitions.class.getDeclaredMethods();
            List<Method> fromPricing =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(OutgoingTransition.class))
                            .filter(
                                    m ->
                                            Arrays.asList(
                                                            m.getAnnotation(
                                                                            OutgoingTransition
                                                                                    .class)
                                                                    .activate())
                                                    .contains(PricingState.class))
                            .collect(Collectors.toList());

            assertEquals(2, fromPricing.size(), "Should have 2 transitions from PricingState");
        }

        @Test
        @DisplayName("Should sort transitions by pathCost")
        void shouldSortTransitionsByPathCost() throws NoSuchMethodException {
            Method quickCheckout = CheckoutTransitions.class.getMethod("toPricingQuickCheckout");
            Method standardCheckout =
                    CheckoutTransitions.class.getMethod("toPricingStandardCheckout");

            OutgoingTransition quick = quickCheckout.getAnnotation(OutgoingTransition.class);
            OutgoingTransition standard = standardCheckout.getAnnotation(OutgoingTransition.class);

            assertTrue(
                    quick.pathCost() < standard.pathCost(),
                    "Quick checkout should have lower path cost (preferred)");
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
        @DisplayName("Should find single IncomingTransition in a class")
        void shouldFindSingleIncomingTransitionInClass() {
            Method[] methods = PricingTransitions.class.getDeclaredMethods();
            List<Method> incomingTransitions =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(IncomingTransition.class))
                            .collect(Collectors.toList());

            assertEquals(
                    1, incomingTransitions.size(), "Should have exactly one IncomingTransition");
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
            List<Class<?>> targetStates =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(OutgoingTransition.class))
                            .map(m -> m.getAnnotation(OutgoingTransition.class).activate()[0])
                            .collect(Collectors.toList());

            assertTrue(targetStates.contains(MenuState.class));
            assertTrue(targetStates.contains(HomepageState.class));
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
                                .filter(m -> m.isAnnotationPresent(OutgoingTransition.class))
                                .filter(
                                        m ->
                                                Arrays.asList(
                                                                m.getAnnotation(
                                                                                OutgoingTransition
                                                                                        .class)
                                                                        .activate())
                                                        .contains(MenuState.class))
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
            List<Class<?>> targetStates =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(OutgoingTransition.class))
                            .map(m -> m.getAnnotation(OutgoingTransition.class).activate()[0])
                            .collect(Collectors.toList());

            // All source states should transition to PricingState
            assertFalse(targetStates.isEmpty());
            targetStates.forEach(
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
        @DisplayName("OutgoingTransition methods should return boolean")
        void fromTransitionMethodsShouldReturnBoolean() {
            Method[] methods = PricingTransitions.class.getDeclaredMethods();
            Arrays.stream(methods)
                    .filter(m -> m.isAnnotationPresent(OutgoingTransition.class))
                    .forEach(
                            m ->
                                    assertEquals(
                                            boolean.class,
                                            m.getReturnType(),
                                            "OutgoingTransition method should return boolean"));
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
        @DisplayName("Should not have @OutgoingTransition on non-methods")
        void shouldNotHaveOutgoingTransitionOnNonMethods() {
            assertFalse(
                    PricingTransitions.class.isAnnotationPresent(OutgoingTransition.class),
                    "OutgoingTransition should not be on class");
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
        @DisplayName("Should access OutgoingTransition annotation methods via reflection")
        void shouldAccessOutgoingTransitionMethods() throws NoSuchMethodException {
            assertNotNull(OutgoingTransition.class.getMethod("activate"));
            assertNotNull(OutgoingTransition.class.getMethod("pathCost"));
            assertNotNull(OutgoingTransition.class.getMethod("description"));
            assertNotNull(OutgoingTransition.class.getMethod("activate"));
            assertNotNull(OutgoingTransition.class.getMethod("exit"));
            assertNotNull(OutgoingTransition.class.getMethod("staysVisible"));

            assertEquals(
                    Class[].class, OutgoingTransition.class.getMethod("activate").getReturnType());
            assertEquals(int.class, OutgoingTransition.class.getMethod("pathCost").getReturnType());
            assertEquals(
                    String.class,
                    OutgoingTransition.class.getMethod("description").getReturnType());
            assertEquals(
                    Class[].class, OutgoingTransition.class.getMethod("activate").getReturnType());
            assertEquals(Class[].class, OutgoingTransition.class.getMethod("exit").getReturnType());
            assertEquals(
                    boolean.class,
                    OutgoingTransition.class.getMethod("staysVisible").getReturnType());
        }

        @Test
        @DisplayName("Should access IncomingTransition annotation methods via reflection")
        void shouldAccessIncomingTransitionMethods() throws NoSuchMethodException {
            assertNotNull(IncomingTransition.class.getMethod("description"));
            assertNotNull(IncomingTransition.class.getMethod("timeout"));

            assertEquals(
                    String.class,
                    IncomingTransition.class.getMethod("description").getReturnType());
            assertEquals(int.class, IncomingTransition.class.getMethod("timeout").getReturnType());
        }

        @Test
        @DisplayName("Should verify annotation types")
        void shouldVerifyAnnotationTypes() {
            assertTrue(TransitionSet.class.isAnnotation());
            assertTrue(OutgoingTransition.class.isAnnotation());
            assertTrue(IncomingTransition.class.isAnnotation());
            assertTrue(OutgoingTransition.class.isAnnotation());

            assertEquals("TransitionSet", TransitionSet.class.getSimpleName());
            assertEquals("OutgoingTransition", OutgoingTransition.class.getSimpleName());
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
        @DisplayName("Should work with Spring's annotation utilities for OutgoingTransition")
        void shouldWorkWithSpringAnnotationUtilsForOutgoingTransition()
                throws NoSuchMethodException {
            Method toMenu = PricingTransitions.class.getMethod("toMenu");
            OutgoingTransition annotation =
                    AnnotationUtils.findAnnotation(toMenu, OutgoingTransition.class);
            assertNotNull(annotation);
            assertArrayEquals(new Class<?>[] {MenuState.class}, annotation.activate());
        }

        @Test
        @DisplayName("Should work with Spring's annotation utilities for IncomingTransition")
        void shouldWorkWithSpringAnnotationUtilsForIncomingTransition()
                throws NoSuchMethodException {
            Method verifyArrival = PricingTransitions.class.getMethod("verifyArrival");
            IncomingTransition annotation =
                    AnnotationUtils.findAnnotation(verifyArrival, IncomingTransition.class);
            assertNotNull(annotation);
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
        @DisplayName("Should handle TransitionSet without OutgoingTransitions")
        void shouldHandleTransitionSetWithoutOutgoingTransitions() {
            @TransitionSet(state = MenuState.class)
            class OnlyIncomingTransition {
                @IncomingTransition
                public boolean verify() {
                    return true;
                }
            }

            TransitionSet annotation =
                    OnlyIncomingTransition.class.getAnnotation(TransitionSet.class);
            assertNotNull(annotation);

            Method[] methods = OnlyIncomingTransition.class.getDeclaredMethods();
            long fromCount =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(OutgoingTransition.class))
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
            class OnlyOutgoingTransitions {
                @OutgoingTransition(activate = {HomepageState.class})
                public boolean toHome() {
                    return true;
                }
            }

            TransitionSet annotation =
                    OnlyOutgoingTransitions.class.getAnnotation(TransitionSet.class);
            assertNotNull(annotation);

            Method[] methods = OnlyOutgoingTransitions.class.getDeclaredMethods();
            long fromCount =
                    Arrays.stream(methods)
                            .filter(m -> m.isAnnotationPresent(OutgoingTransition.class))
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
                    .filter(m -> m.isAnnotationPresent(OutgoingTransition.class))
                    .map(m -> m.getAnnotation(OutgoingTransition.class).activate()[0])
                    .forEach(
                            activateState ->
                                    assertTrue(
                                            activateState.isAnnotationPresent(State.class),
                                            activateState.getSimpleName()
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
                OutgoingTransition fromAnnotation = method.getAnnotation(OutgoingTransition.class);
                IncomingTransition incomingAnnotation =
                        method.getAnnotation(IncomingTransition.class);
                assertNull(fromAnnotation);
                assertNull(incomingAnnotation);
            } catch (NoSuchMethodException e) {
                fail("Method should exist");
            }
        }

        @Test
        @DisplayName("Should compare annotations for equality")
        void shouldCompareAnnotationsForEquality() throws NoSuchMethodException {
            Method toMenu1 = PricingTransitions.class.getMethod("toMenu");
            Method toMenu2 = PricingTransitions.class.getMethod("toMenu");
            Method toHomepage = PricingTransitions.class.getMethod("toHomepage");

            OutgoingTransition annotation1 = toMenu1.getAnnotation(OutgoingTransition.class);
            OutgoingTransition annotation2 = toMenu2.getAnnotation(OutgoingTransition.class);
            OutgoingTransition annotation3 = toHomepage.getAnnotation(OutgoingTransition.class);

            assertEquals(annotation1, annotation2);
            assertNotEquals(annotation1, annotation3);
        }
    }
}
