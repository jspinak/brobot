package io.github.jspinak.brobot.runner.docs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.diagnostics.DiagnosticTool;
import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorHandler;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Provides automated resolution suggestions for common issues. */
@Slf4j
@Component
@RequiredArgsConstructor
@Data
public class CommonIssuesResolver {

    private final TroubleshootingGuide troubleshootingGuide;
    private final DiagnosticTool diagnosticTool;
    private final ErrorHandler errorHandler;

    /** Analyze an error and provide resolution suggestions. */
    public ResolutionSuggestions analyzeAndSuggest(Throwable error, ErrorContext context) {
        log.debug(
                "Analyzing error for resolution suggestions: {}", error.getClass().getSimpleName());

        List<String> suggestions = new ArrayList<>();
        List<String> automatedFixes = new ArrayList<>();
        List<String> manualSteps = new ArrayList<>();

        // Get troubleshooting guidance
        Optional<TroubleshootingGuide.TroubleshootingEntry> guidance =
                troubleshootingGuide.getTroubleshooting(error.getClass().getSimpleName(), context);

        if (guidance.isPresent()) {
            TroubleshootingGuide.TroubleshootingEntry entry = guidance.get();

            // Add solutions
            entry.getSolutions()
                    .forEach(
                            solution -> {
                                suggestions.add(solution.getTitle() + ": " + solution.getSteps());
                            });

            // Add prevention tips
            manualSteps.addAll(entry.getPreventionTips());
        }

        // Add context-specific suggestions
        if (context != null) {
            addContextSpecificSuggestions(error, context, suggestions, automatedFixes);
        }

        // Add error-type specific suggestions
        addErrorSpecificSuggestions(error, suggestions, automatedFixes);

        return new ResolutionSuggestions(
                suggestions,
                automatedFixes,
                manualSteps,
                guidance.map(TroubleshootingGuide.TroubleshootingEntry::getId).orElse(null));
    }

    /** Check if an issue can be automatically resolved. */
    public boolean canAutoResolve(Throwable error, ErrorContext context) {
        // Check for known auto-resolvable issues
        if (error instanceof OutOfMemoryError) {
            return canAutoResolveMemoryIssue();
        }

        if (context != null && context.getCategory() == ErrorContext.ErrorCategory.CONFIGURATION) {
            return canAutoResolveConfigurationIssue(error);
        }

        return false;
    }

    /** Attempt to automatically resolve an issue. */
    public Optional<String> attemptAutoResolution(Throwable error, ErrorContext context) {
        log.info("Attempting auto-resolution for: {}", error.getClass().getSimpleName());

        try {
            if (error instanceof OutOfMemoryError) {
                return attemptMemoryResolution();
            }

            if (context != null
                    && context.getCategory() == ErrorContext.ErrorCategory.CONFIGURATION) {
                return attemptConfigurationResolution(error, context);
            }

            return Optional.empty();

        } catch (Exception e) {
            log.error("Auto-resolution failed", e);
            return Optional.empty();
        }
    }

    private void addContextSpecificSuggestions(
            Throwable error,
            ErrorContext context,
            List<String> suggestions,
            List<String> automatedFixes) {
        switch (context.getCategory()) {
            case CONFIGURATION:
                suggestions.add("Verify your configuration file format is correct");
                suggestions.add("Check that all required fields are present");
                automatedFixes.add("Validate configuration syntax");
                break;

            case NETWORK:
                suggestions.add("Check your internet connection");
                suggestions.add("Verify proxy settings if behind a corporate firewall");
                automatedFixes.add("Test network connectivity");
                break;

            case SYSTEM:
                suggestions.add("Check available system resources");
                suggestions.add("Close unnecessary applications");
                automatedFixes.add("Run system diagnostics");
                break;

            case UI:
                suggestions.add("Try resizing the window");
                suggestions.add("Reset UI layout in Settings");
                automatedFixes.add("Reset to default UI settings");
                break;

            case VALIDATION:
                suggestions.add("Review the validation error details");
                suggestions.add("Correct any invalid values");
                break;

            case AUTHORIZATION:
                suggestions.add("Check file and directory permissions");
                suggestions.add("Run application with appropriate privileges");
                break;
        }
    }

    private void addErrorSpecificSuggestions(
            Throwable error, List<String> suggestions, List<String> automatedFixes) {
        if (error instanceof OutOfMemoryError) {
            suggestions.add("Increase JVM heap size with -Xmx flag");
            suggestions.add("Process configurations in smaller batches");
            automatedFixes.add("Clear caches and temporary data");
        }

        if (error instanceof StackOverflowError) {
            suggestions.add("Check for circular references in configuration");
            suggestions.add("Reduce nesting depth in configurations");
        }

        if (error.getMessage() != null) {
            String message = error.getMessage().toLowerCase();

            if (message.contains("permission") || message.contains("access denied")) {
                suggestions.add("Run as administrator/root");
                suggestions.add("Check antivirus software settings");
            }

            if (message.contains("file not found")) {
                suggestions.add("Verify file path is correct and absolute");
                suggestions.add("Check file exists and is readable");
            }

            if (message.contains("timeout")) {
                suggestions.add("Increase timeout values in settings");
                suggestions.add("Check network connectivity");
            }
        }
    }

    private boolean canAutoResolveMemoryIssue() {
        // Check if we can free up memory
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();

        // Can resolve if we have room to grow or can free up memory
        return totalMemory < maxMemory || freeMemory < totalMemory * 0.1;
    }

    private boolean canAutoResolveConfigurationIssue(Throwable error) {
        // Check if it's a simple formatting issue we can fix
        return error.getMessage() != null
                && (error.getMessage().contains("trailing comma")
                        || error.getMessage().contains("missing quote"));
    }

    private Optional<String> attemptMemoryResolution() {
        log.info("Attempting to resolve memory issue");

        // Force garbage collection
        System.gc();

        // Clear caches if possible
        // This would integrate with actual cache managers

        // Wait a moment for GC
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();

        if (freeMemory > totalMemory * 0.2) {
            return Optional.of("Freed up memory through garbage collection");
        }

        return Optional.empty();
    }

    private Optional<String> attemptConfigurationResolution(Throwable error, ErrorContext context) {
        // This would integrate with actual configuration fixing logic
        // For now, return empty
        return Optional.empty();
    }

    /** Container for resolution suggestions. */
    public record ResolutionSuggestions(
            List<String> generalSuggestions,
            List<String> automatedFixes,
            List<String> manualSteps,
            String troubleshootingId) {}
}
