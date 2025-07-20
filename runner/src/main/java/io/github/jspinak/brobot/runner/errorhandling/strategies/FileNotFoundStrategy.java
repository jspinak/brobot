package io.github.jspinak.brobot.runner.errorhandling.strategies;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.NoSuchFileException;

/**
 * Strategy for handling file not found exceptions.
 * These errors are typically not recoverable without user intervention.
 */
@Slf4j
public class FileNotFoundStrategy implements IErrorStrategy {
    
    @Override
    public ErrorResult handle(Throwable error, ErrorContext context) {
        String fileName = extractFileName(error);
        
        log.error("File not found in {}: {}", context.getOperation(), fileName);
        
        return ErrorResult.builder()
            .errorId(context.getErrorId())
            .success(false)
            .recoverable(false)
            .userMessage("File not found: " + fileName)
            .technicalDetails(String.format(
                "NoSuchFileException: %s\nOperation: %s\nComponent: %s\nHint: Check file path and ensure file exists",
                error.getMessage(),
                context.getOperation(),
                context.getComponent()
            ))
            .build();
    }
    
    private String extractFileName(Throwable error) {
        if (error instanceof NoSuchFileException) {
            NoSuchFileException nsfe = (NoSuchFileException) error;
            return nsfe.getFile() != null ? nsfe.getFile() : "Unknown file";
        }
        
        String message = error.getMessage();
        if (message != null && !message.isEmpty()) {
            return message;
        }
        
        return "Unknown file";
    }
}