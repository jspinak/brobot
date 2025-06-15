package io.github.jspinak.brobot.runner.errorhandling;

import java.util.List;
import java.util.Map;

/**
 * Statistics about errors in the application.
 */
public record ErrorStatistics(
    long totalErrors,
    Map<ErrorContext.ErrorCategory, Long> errorsByCategory,
    List<ErrorHistory.ErrorRecord> recentErrors,
    List<ErrorHistory.ErrorFrequency> mostFrequentErrors
) {
    
    /**
     * Get a formatted summary of the error statistics.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Error Statistics:\n");
        summary.append("  Total Errors: ").append(totalErrors).append("\n");
        
        summary.append("  By Category:\n");
        errorsByCategory.forEach((category, count) -> 
            summary.append("    ").append(category.getDisplayName())
                  .append(": ").append(count).append("\n")
        );
        
        summary.append("  Most Frequent:\n");
        mostFrequentErrors.forEach(freq -> 
            summary.append("    ").append(freq.errorKey())
                  .append(": ").append(freq.count()).append(" times\n")
        );
        
        return summary.toString();
    }
}