package io.github.jspinak.brobot.runner.errorhandling;

import lombok.Builder;
import lombok.Getter;

/** Result of error handling, containing information about the outcome and any recovery actions. */
@Getter
@Builder
public class ErrorResult {

    private final String errorId;
    private final boolean success;
    private final boolean recoverable;
    private final String userMessage;
    private final String technicalDetails;
    private final Runnable recoveryAction;

    /** Create a result for an unrecoverable error. */
    public static ErrorResult unrecoverable(String userMessage, String errorId) {
        return ErrorResult.builder()
                .errorId(errorId)
                .success(false)
                .recoverable(false)
                .userMessage(userMessage)
                .build();
    }

    /** Create a result for a recoverable error. */
    public static ErrorResult recoverable(
            String userMessage, String errorId, Runnable recoveryAction) {
        return ErrorResult.builder()
                .errorId(errorId)
                .success(false)
                .recoverable(true)
                .userMessage(userMessage)
                .recoveryAction(recoveryAction)
                .build();
    }

    /** Create a result for a successfully handled error. */
    public static ErrorResult handled(String message, String errorId) {
        return ErrorResult.builder()
                .errorId(errorId)
                .success(true)
                .recoverable(true)
                .userMessage(message)
                .build();
    }
}
