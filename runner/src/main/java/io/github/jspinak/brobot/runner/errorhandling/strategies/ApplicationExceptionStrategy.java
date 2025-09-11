package io.github.jspinak.brobot.runner.errorhandling.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ApplicationException;
import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling application-specific exceptions. Provides detailed error information and
 * recovery actions based on the exception context.
 */
@Slf4j
public class ApplicationExceptionStrategy implements IErrorStrategy {

    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        ApplicationException appEx = (ApplicationException) error;

        log.debug(
                "Handling ApplicationException: {} - Recoverable: {}",
                appEx.getMessage(),
                appEx.isRecoverable());

        return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(appEx.isRecoverable())
                .userMessage(appEx.getDisplayMessage())
                .technicalDetails(appEx.getTechnicalDetails())
                .recoveryAction(createRecoveryAction(appEx))
                .build();
    }

    private Runnable createRecoveryAction(ApplicationException ex) {
        if (!ex.isRecoverable()) {
            return null;
        }

        // Create appropriate recovery action based on error category
        return switch (ex.getContext().getCategory()) {
            case FILE_IO ->
                    () -> {
                        log.info("Retrying file operation...");
                        // In a real implementation, this would retry the file operation
                    };
            case NETWORK ->
                    () -> {
                        log.info("Retrying network operation...");
                        // In a real implementation, this would retry the network request
                    };
            case VALIDATION ->
                    () -> {
                        log.info("Awaiting user correction...");
                        // In a real implementation, this would prompt for user input
                    };
            case DATABASE ->
                    () -> {
                        log.info("Reconnecting to database...");
                        // In a real implementation, this would attempt reconnection
                    };
            case CONFIGURATION ->
                    () -> {
                        log.info("Reloading configuration...");
                        // In a real implementation, this would reload config
                    };
            default -> null;
        };
    }
}
