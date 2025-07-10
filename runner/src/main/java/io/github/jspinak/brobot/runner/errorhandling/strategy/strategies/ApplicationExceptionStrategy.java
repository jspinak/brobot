package io.github.jspinak.brobot.runner.errorhandling.strategy.strategies;

import io.github.jspinak.brobot.runner.errorhandling.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling application-specific exceptions.
 * 
 * This strategy knows how to extract meaningful information from ApplicationException
 * and create appropriate recovery actions.
 */
@Slf4j
public class ApplicationExceptionStrategy implements IErrorStrategy {
    
    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        ApplicationException appEx = (ApplicationException) error;
        
        log.debug("Handling ApplicationException: {}", appEx.getDisplayMessage());
        
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
            case FILE_IO -> () -> log.info("Retrying file operation...");
            case NETWORK -> () -> log.info("Retrying network operation...");
            case VALIDATION -> () -> log.info("Awaiting user correction...");
            default -> null;
        };
    }
}