package io.github.jspinak.brobot.model.transition;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for the TransitionFunction functional interface which defines the contract
 * for executable state transitions.
 */
@DisplayName("TransitionFunction Interface Tests")
public class TransitionFunctionTest extends BrobotTestBase {

    private Action mockAction;
    private ActionResult mockResult;
    private StateImage mockStateImage;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockAction = mock(Action.class);
        mockResult = mock(ActionResult.class);
        mockStateImage = mock(StateImage.class);
    }

    @Test
    @DisplayName("Should create TransitionFunction with lambda")
    void testLambdaCreation() {
        // Given - Lambda implementation
        TransitionFunction function = (action, context) -> true;

        // When
        boolean result = function.execute(mockAction);

        // Then
        assertTrue(result);
        assertNotNull(function);
    }

    @Test
    @DisplayName("Should create TransitionFunction with method reference")
    void testMethodReferenceCreation() {
        // Given - Method reference
        TransitionFunction function = this::alwaysSucceed;

        // When
        boolean result = function.execute(mockAction);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should handle context parameters")
    void testContextParameters() {
        // Given
        TransitionFunction function =
                (action, context) -> {
                    // Verify context is passed
                    assertNotNull(context);
                    return context.length > 0;
                };

        // When - With context
        boolean withContext = function.execute(mockAction, "param1", 42, true);

        // When - Without context
        boolean withoutContext = function.execute(mockAction);

        // Then
        assertTrue(withContext);
        assertFalse(withoutContext);
    }

    @Test
    @DisplayName("Should access context values")
    void testAccessContextValues() {
        // Given
        TransitionFunction function =
                (action, context) -> {
                    if (context.length >= 3) {
                        String first = (String) context[0];
                        Integer second = (Integer) context[1];
                        Boolean third = (Boolean) context[2];

                        return "test".equals(first) && second == 100 && third;
                    }
                    return false;
                };

        // When
        boolean result = function.execute(mockAction, "test", 100, true);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should use Action parameter")
    void testActionParameter() {
        // Given
        when(mockAction.find(any(StateImage.class))).thenReturn(mockResult);
        when(mockResult.isSuccess()).thenReturn(true);

        TransitionFunction function =
                (action, context) -> {
                    ActionResult result = action.find(mockStateImage);
                    return result.isSuccess();
                };

        // When
        boolean result = function.execute(mockAction);

        // Then
        assertTrue(result);
        verify(mockAction).find(mockStateImage);
    }

    @Test
    @DisplayName("Should handle exceptions in function")
    void testExceptionHandling() {
        // Given - Function that throws exception
        TransitionFunction function =
                (action, context) -> {
                    throw new RuntimeException("Test exception");
                };

        // When/Then
        assertThrows(RuntimeException.class, () -> function.execute(mockAction));
    }

    @Test
    @DisplayName("Should support stateful transitions")
    void testStatefulTransition() {
        // Given - Stateful transition with counter
        AtomicInteger counter = new AtomicInteger(0);

        TransitionFunction function =
                (action, context) -> {
                    int count = counter.incrementAndGet();
                    return count >= 3; // Succeed on third attempt
                };

        // When - Execute multiple times
        boolean first = function.execute(mockAction);
        boolean second = function.execute(mockAction);
        boolean third = function.execute(mockAction);
        boolean fourth = function.execute(mockAction);

        // Then
        assertFalse(first);
        assertFalse(second);
        assertTrue(third);
        assertTrue(fourth);
        assertEquals(4, counter.get());
    }

    @TestFactory
    @DisplayName("TransitionFunction usage patterns")
    Stream<DynamicTest> testUsagePatterns() {
        return Stream.of(
                dynamicTest(
                        "Simple success transition",
                        () -> {
                            TransitionFunction function = (action, context) -> true;
                            assertTrue(function.execute(mockAction));
                        }),
                dynamicTest(
                        "Simple failure transition",
                        () -> {
                            TransitionFunction function = (action, context) -> false;
                            assertFalse(function.execute(mockAction));
                        }),
                dynamicTest(
                        "Conditional transition based on context",
                        () -> {
                            TransitionFunction function =
                                    (action, context) -> {
                                        if (context.length > 0 && context[0] instanceof String) {
                                            return "proceed".equals(context[0]);
                                        }
                                        return false;
                                    };

                            assertTrue(function.execute(mockAction, "proceed"));
                            assertFalse(function.execute(mockAction, "stop"));
                            assertFalse(function.execute(mockAction));
                        }),
                dynamicTest(
                        "Action-based transition",
                        () -> {
                            when(mockAction.find(any(StateImage.class))).thenReturn(mockResult);
                            when(mockResult.isSuccess()).thenReturn(true, false);

                            TransitionFunction function =
                                    (action, context) -> action.find(mockStateImage).isSuccess();

                            assertTrue(function.execute(mockAction)); // First call
                            assertFalse(function.execute(mockAction)); // Second call
                        }),
                dynamicTest(
                        "Complex multi-condition transition",
                        () -> {
                            AtomicBoolean flag = new AtomicBoolean(false);

                            TransitionFunction function =
                                    (action, context) -> {
                                        boolean hasContext = context.length > 0;
                                        boolean flagSet = flag.get();
                                        return hasContext && flagSet;
                                    };

                            assertFalse(function.execute(mockAction, "data")); // Flag not set
                            flag.set(true);
                            assertTrue(function.execute(mockAction, "data")); // Flag set
                            assertFalse(function.execute(mockAction)); // No context
                        }));
    }

    @Test
    @DisplayName("Should compose TransitionFunctions")
    void testComposition() {
        // Given
        TransitionFunction first = (action, context) -> context.length > 0;
        TransitionFunction second =
                (action, context) -> {
                    if (context.length > 0) {
                        return (Integer) context[0] > 10;
                    }
                    return false;
                };

        // Compose with AND logic
        TransitionFunction combined =
                (action, context) ->
                        first.execute(action, context) && second.execute(action, context);

        // When
        boolean result1 = combined.execute(mockAction, 20);
        boolean result2 = combined.execute(mockAction, 5);
        boolean result3 = combined.execute(mockAction);

        // Then
        assertTrue(result1); // Has context and > 10
        assertFalse(result2); // Has context but <= 10
        assertFalse(result3); // No context
    }

    @Test
    @DisplayName("Should chain TransitionFunctions")
    void testChaining() {
        // Given - Chain of transitions
        TransitionFunction validate = (action, context) -> context.length > 0;
        TransitionFunction process =
                (action, context) -> {
                    // Only process if validation passed
                    if (validate.execute(action, context)) {
                        return "valid".equals(context[0]);
                    }
                    return false;
                };

        // When
        boolean valid = process.execute(mockAction, "valid");
        boolean invalid = process.execute(mockAction, "invalid");
        boolean noContext = process.execute(mockAction);

        // Then
        assertTrue(valid);
        assertFalse(invalid);
        assertFalse(noContext);
    }

    @Test
    @DisplayName("Should use as functional interface")
    void testFunctionalInterface() {
        // TransitionFunction is a functional interface
        // Can be used in functional programming contexts

        // As parameter
        boolean result = executeTransition((action, context) -> true, mockAction);
        assertTrue(result);

        // In collections
        TransitionFunction[] functions = {
            (action, context) -> true,
            (action, context) -> false,
            (action, context) -> context.length > 0
        };

        assertEquals(3, functions.length);
        assertTrue(functions[0].execute(mockAction));
        assertFalse(functions[1].execute(mockAction));
    }

    @Test
    @DisplayName("Should handle null parameters safely")
    void testNullSafety() {
        // Given
        TransitionFunction nullSafe =
                (action, context) -> {
                    return action != null;
                };

        // When
        boolean withAction = nullSafe.execute(mockAction);
        boolean withNull = nullSafe.execute(null);

        // Then
        assertTrue(withAction);
        assertFalse(withNull);
    }

    @Test
    @DisplayName("Should support retry logic")
    void testRetryLogic() {
        // Given - Transition with retry
        AtomicInteger attempts = new AtomicInteger(0);

        TransitionFunction retryable =
                (action, context) -> {
                    int attempt = attempts.incrementAndGet();

                    // Fail first 2 attempts, succeed on 3rd
                    if (attempt < 3) {
                        return false;
                    }
                    return true;
                };

        // When - Retry until success
        boolean result = false;
        int maxRetries = 5;
        for (int i = 0; i < maxRetries && !result; i++) {
            result = retryable.execute(mockAction);
        }

        // Then
        assertTrue(result);
        assertEquals(3, attempts.get());
    }

    @Test
    @DisplayName("Should support timeout logic")
    void testTimeoutLogic() {
        // Given
        long startTime = System.currentTimeMillis();
        long timeout = 1000; // 1 second

        TransitionFunction timedTransition =
                (action, context) -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    return elapsed < timeout;
                };

        // When - Immediate execution
        boolean immediate = timedTransition.execute(mockAction);

        // Then
        assertTrue(immediate);
    }

    @Test
    @DisplayName("Should verify functional interface annotation")
    void testFunctionalInterfaceAnnotation() {
        // TransitionFunction should be annotated as @FunctionalInterface
        assertTrue(TransitionFunction.class.isAnnotationPresent(FunctionalInterface.class));

        // Should have exactly one abstract method
        long abstractMethods =
                Stream.of(TransitionFunction.class.getMethods())
                        .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                        .count();

        assertEquals(1, abstractMethods);
    }

    @Test
    @DisplayName("Should support logging in transitions")
    void testLoggingTransition() {
        // Given
        StringBuilder log = new StringBuilder();

        TransitionFunction logged =
                (action, context) -> {
                    log.append("Executing transition");
                    if (context.length > 0) {
                        log.append(" with context: ").append(context[0]);
                    }
                    log.append("\n");
                    return true;
                };

        // When
        logged.execute(mockAction, "test-data");

        // Then
        assertTrue(log.toString().contains("Executing transition"));
        assertTrue(log.toString().contains("test-data"));
    }

    // Helper method for testing
    private boolean alwaysSucceed(Action action, Object... context) {
        return true;
    }

    private boolean executeTransition(TransitionFunction function, Action action) {
        return function.execute(action);
    }
}
