package io.github.jspinak.brobot.runner.errorhandling.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Strategy for handling I/O exceptions.
 * These errors are often recoverable through retry.
 */
@Slf4j
public class IOExceptionStrategy implements IErrorStrategy {
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        IOException ioException = (IOException) error;
        
        log.error("I/O error in {}: {}", context.getOperation(), ioException.getMessage());
        
        String userMessage = createUserMessage(ioException);
        boolean recoverable = isRecoverable(ioException);
        
        return ErrorResult.builder()
            .errorId(context.getErrorId())
            .success(false)
            .recoverable(recoverable)
            .userMessage(userMessage)
            .technicalDetails(String.format(
                "IOException: %s\nCause: %s\nOperation: %s",
                ioException.getMessage(),
                ioException.getCause() != null ? ioException.getCause().getMessage() : "None",
                context.getOperation()
            ))
            .recoveryAction(recoverable ? createRecoveryAction(context) : null)
            .build();
    }
    
    private String createUserMessage(IOException exception) {
        String message = exception.getMessage();
        
        if (message == null) {
            return "File operation failed. Please check file permissions and availability.";
        } else if (message.contains("Permission denied")) {
            return "Access denied. Please check file permissions.";
        } else if (message.contains("No such file")) {
            return "File not found. Please check the file path.";
        } else if (message.contains("No space left")) {
            return "Insufficient disk space. Please free up some space and try again.";
        } else {
            return "File operation failed: " + message;
        }
    }
    
    private boolean isRecoverable(IOException exception) {
        String message = exception.getMessage();
        if (message == null) {
            return true; // Assume recoverable by default
        }
        
        // Non-recoverable conditions
        return !message.contains("Permission denied") && 
               !message.contains("Read-only file system") &&
               !message.contains("Invalid path");
    }
    
    private Runnable createRecoveryAction(ErrorContext context) {
        return () -> {
            log.info("Retrying file operation for {}", context.getOperation());
            // In a real implementation, this would retry the I/O operation
            // with exponential backoff and max attempts
        };
    }
}