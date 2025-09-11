package io.github.jspinak.brobot.runner.errorhandling.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling index out of bounds exceptions. These indicate attempts to access invalid
 * array or list indices.
 */
@Slf4j
public class IndexOutOfBoundsStrategy implements IErrorStrategy {

    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        String details = extractIndexDetails(error);

        log.error("Index out of bounds in {}: {}", context.getOperation(), details);

        return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(false)
                .userMessage("Invalid index access: " + details)
                .technicalDetails(
                        String.format(
                                "IndexOutOfBoundsException: %s\n"
                                        + "Operation: %s\n"
                                        + "Component: %s\n"
                                        + "Stack: %s",
                                error.getMessage(),
                                context.getOperation(),
                                context.getComponent(),
                                getRelevantStackTrace(error)))
                .build();
    }

    private String extractIndexDetails(Throwable error) {
        String message = error.getMessage();
        if (message != null && !message.isEmpty()) {
            return message;
        }

        if (error instanceof ArrayIndexOutOfBoundsException) {
            return "Array index out of bounds";
        } else if (error instanceof StringIndexOutOfBoundsException) {
            return "String index out of bounds";
        }

        return "Index out of bounds";
    }

    private String getRelevantStackTrace(Throwable error) {
        StackTraceElement[] stackTrace = error.getStackTrace();
        if (stackTrace.length > 0) {
            // Find the first non-library stack frame
            for (StackTraceElement element : stackTrace) {
                if (element.getClassName().startsWith("io.github.jspinak")) {
                    return String.format(
                            "%s.%s(%s:%d)",
                            element.getClassName(),
                            element.getMethodName(),
                            element.getFileName(),
                            element.getLineNumber());
                }
            }
            // If no project-specific frame found, return the first frame
            StackTraceElement element = stackTrace[0];
            return String.format(
                    "%s.%s(%s:%d)",
                    element.getClassName(),
                    element.getMethodName(),
                    element.getFileName(),
                    element.getLineNumber());
        }
        return "No stack trace available";
    }
}
