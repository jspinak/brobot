package io.github.jspinak.brobot.test.extensions;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.*;

import io.github.jspinak.brobot.test.annotations.Flaky;

/**
 * JUnit 5 extension that provides retry capability for flaky tests. Tests annotated with @Flaky
 * will be retried the specified number of times.
 */
public class RetryExtension implements TestExecutionExceptionHandler, BeforeEachCallback {

    private static final String RETRY_COUNT_KEY = "retryCount";
    private static final String MAX_RETRIES_KEY = "maxRetries";

    @Override
    public void beforeEach(ExtensionContext context) {
        // Reset retry count for each test
        getStore(context).put(RETRY_COUNT_KEY, 0);

        // Check for FlakyTest annotation and set max retries
        Optional<Method> testMethod = context.getTestMethod();
        if (testMethod.isPresent()) {
            Flaky flakyTest = testMethod.get().getAnnotation(Flaky.class);
            if (flakyTest != null) {
                getStore(context).put(MAX_RETRIES_KEY, flakyTest.retries());
            } else {
                getStore(context).put(MAX_RETRIES_KEY, 0);
            }
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable)
            throws Throwable {

        ExtensionContext.Store store = getStore(context);
        int retryCount = store.get(RETRY_COUNT_KEY, Integer.class);
        int maxRetries = store.get(MAX_RETRIES_KEY, Integer.class);

        if (retryCount < maxRetries) {
            retryCount++;
            store.put(RETRY_COUNT_KEY, retryCount);

            System.out.println(
                    String.format(
                            "[RETRY] Test %s failed (attempt %d/%d). Retrying...",
                            context.getDisplayName(), retryCount, maxRetries + 1));

            // Log the failure reason
            System.out.println("[RETRY] Failure reason: " + throwable.getMessage());

            // Wait a bit before retrying to let system settle
            Thread.sleep(500);

            // Re-execute the test
            try {
                Method testMethod =
                        context.getTestMethod()
                                .orElseThrow(
                                        () -> new IllegalStateException("Test method not found"));
                Object testInstance =
                        context.getTestInstance()
                                .orElseThrow(
                                        () -> new IllegalStateException("Test instance not found"));

                // Invoke the test method again
                testMethod.invoke(testInstance);

                System.out.println(
                        String.format(
                                "[RETRY] Test %s passed on retry %d",
                                context.getDisplayName(), retryCount));

            } catch (Exception retryException) {
                // If this was the last retry, throw the exception
                if (retryCount >= maxRetries) {
                    System.out.println(
                            String.format(
                                    "[RETRY] Test %s failed after %d retries",
                                    context.getDisplayName(), retryCount));
                    throw extractCause(retryException);
                } else {
                    // Otherwise, handle it recursively
                    handleTestExecutionException(context, extractCause(retryException));
                }
            }
        } else {
            // No more retries, throw the original exception
            throw throwable;
        }
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(
                ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

    private Throwable extractCause(Exception exception) {
        if (exception.getCause() != null) {
            return exception.getCause();
        }
        return exception;
    }
}
