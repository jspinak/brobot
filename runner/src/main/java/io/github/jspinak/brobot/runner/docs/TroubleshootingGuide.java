package io.github.jspinak.brobot.runner.docs;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides troubleshooting guidance for common issues in Brobot Runner.
 */
@Slf4j
@Component
public class TroubleshootingGuide {

    private final Map<String, TroubleshootingEntry> troubleshootingDatabase = new HashMap<>();
    
    public TroubleshootingGuide() {
        initializeTroubleshootingDatabase();
    }
    
    /**
     * Get troubleshooting guidance for a specific error.
     */
    public Optional<TroubleshootingEntry> getTroubleshooting(String errorType, ErrorContext context) {
        // Try exact match first
        String key = generateKey(errorType, context);
        if (troubleshootingDatabase.containsKey(key)) {
            return Optional.of(troubleshootingDatabase.get(key));
        }
        
        // Try category match
        if (context != null && context.getCategory() != null) {
            String categoryKey = context.getCategory().name();
            if (troubleshootingDatabase.containsKey(categoryKey)) {
                return Optional.of(troubleshootingDatabase.get(categoryKey));
            }
        }
        
        // Try error type only
        if (troubleshootingDatabase.containsKey(errorType)) {
            return Optional.of(troubleshootingDatabase.get(errorType));
        }
        
        return Optional.empty();
    }
    
    /**
     * Search for troubleshooting entries by keyword.
     */
    public List<TroubleshootingEntry> searchByKeyword(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        
        return troubleshootingDatabase.values().stream()
            .filter(entry -> 
                entry.getTitle().toLowerCase().contains(lowerKeyword) ||
                entry.getDescription().toLowerCase().contains(lowerKeyword) ||
                entry.getSymptoms().stream().anyMatch(s -> s.toLowerCase().contains(lowerKeyword)) ||
                entry.getTags().stream().anyMatch(t -> t.toLowerCase().contains(lowerKeyword))
            )
            .collect(Collectors.toList());
    }
    
    /**
     * Get all troubleshooting entries by category.
     */
    public List<TroubleshootingEntry> getByCategory(ErrorContext.ErrorCategory category) {
        return troubleshootingDatabase.values().stream()
            .distinct() // Remove duplicates
            .filter(entry -> entry.getCategory() == category)
            .sorted(Comparator.comparing(TroubleshootingEntry::getSeverity))
            .collect(Collectors.toList());
    }
    
    /**
     * Get frequently occurring issues.
     */
    public List<TroubleshootingEntry> getCommonIssues() {
        return troubleshootingDatabase.values().stream()
            .filter(entry -> entry.getSeverity() == ErrorContext.ErrorSeverity.HIGH || 
                            entry.getTags().contains("common"))
            .sorted(Comparator.comparing(TroubleshootingEntry::getTitle))
            .collect(Collectors.toList());
    }
    
    private String generateKey(String errorType, ErrorContext context) {
        if (context == null) {
            return errorType;
        }
        return errorType + "_" + context.getCategory();
    }
    
    private void initializeTroubleshootingDatabase() {
        // Configuration Issues
        addEntry(TroubleshootingEntry.builder()
            .id("CONFIG_001")
            .title("Configuration File Not Found")
            .category(ErrorContext.ErrorCategory.CONFIGURATION)
            .severity(ErrorContext.ErrorSeverity.HIGH)
            .description("The specified configuration file cannot be located or accessed.")
            .symptoms(List.of(
                "Error message: 'Configuration file not found'",
                "Application fails to start",
                "File path appears in error message"
            ))
            .possibleCauses(List.of(
                "File path is incorrect",
                "File has been moved or deleted",
                "Insufficient permissions to access file",
                "Wrong file extension or format"
            ))
            .solutions(List.of(
                new Solution("Verify the file path is correct and absolute", 
                    "Check that the full path to the configuration file is correct"),
                new Solution("Check file permissions", 
                    "Ensure the application has read access to the file"),
                new Solution("Use File > Open to browse for the file", 
                    "Use the file browser to locate and select the configuration")
            ))
            .preventionTips(List.of(
                "Always use absolute paths for configuration files",
                "Keep configuration files in a dedicated directory",
                "Use the application's file browser for file selection"
            ))
            .relatedErrors(List.of("CONFIG_002", "VALIDATION_001"))
            .tags(List.of("common", "startup", "configuration"))
            .build());
            
        addEntry(TroubleshootingEntry.builder()
            .id("CONFIG_002")
            .title("Invalid JSON Configuration")
            .category(ErrorContext.ErrorCategory.CONFIGURATION)
            .severity(ErrorContext.ErrorSeverity.HIGH)
            .description("The configuration file contains invalid JSON syntax.")
            .symptoms(List.of(
                "JSON parse error messages",
                "Line and column numbers in error",
                "Configuration fails to load"
            ))
            .possibleCauses(List.of(
                "Missing or extra commas",
                "Unclosed brackets or braces",
                "Invalid quotes or escape sequences",
                "Trailing commas in arrays or objects"
            ))
            .solutions(List.of(
                new Solution("Use a JSON validator", 
                    "Paste your JSON into an online validator like jsonlint.com"),
                new Solution("Check the error line number", 
                    "Look at the specific line mentioned in the error message"),
                new Solution("Use a JSON-aware editor", 
                    "Edit the file in VS Code or another editor with JSON support")
            ))
            .preventionTips(List.of(
                "Use a JSON-aware text editor",
                "Validate JSON before saving",
                "Use the built-in configuration editor when available"
            ))
            .relatedErrors(List.of("CONFIG_001", "VALIDATION_002"))
            .tags(List.of("common", "json", "configuration"))
            .build());
            
        // Validation Issues
        addEntry(TroubleshootingEntry.builder()
            .id("VALIDATION_001")
            .title("Missing Required Fields")
            .category(ErrorContext.ErrorCategory.VALIDATION)
            .severity(ErrorContext.ErrorSeverity.MEDIUM)
            .description("The configuration is missing required fields.")
            .symptoms(List.of(
                "Validation error listing missing fields",
                "Configuration loads but cannot execute",
                "Specific field names in error message"
            ))
            .possibleCauses(List.of(
                "Incomplete configuration file",
                "Using outdated configuration format",
                "Copy-paste errors",
                "Manual editing mistakes"
            ))
            .solutions(List.of(
                new Solution("Add the missing fields", 
                    "Add the required fields listed in the error message"),
                new Solution("Use a template", 
                    "Start from a working configuration template"),
                new Solution("Check documentation", 
                    "Refer to the configuration format documentation")
            ))
            .preventionTips(List.of(
                "Always start from a template",
                "Use the configuration validator before running",
                "Keep configurations under version control"
            ))
            .relatedErrors(List.of("VALIDATION_002", "CONFIG_002"))
            .tags(List.of("validation", "configuration"))
            .build());
            
        // System Issues
        addEntry(TroubleshootingEntry.builder()
            .id("SYSTEM_001")
            .title("Out of Memory Error")
            .category(ErrorContext.ErrorCategory.SYSTEM)
            .severity(ErrorContext.ErrorSeverity.CRITICAL)
            .description("The application has run out of available memory.")
            .symptoms(List.of(
                "OutOfMemoryError in logs",
                "Application becomes unresponsive",
                "Sudden application termination"
            ))
            .possibleCauses(List.of(
                "Processing very large configurations",
                "Memory leak in custom code",
                "Insufficient heap size",
                "Too many concurrent operations"
            ))
            .solutions(List.of(
                new Solution("Increase heap size", 
                    "Add -Xmx2G to JVM arguments to increase memory to 2GB"),
                new Solution("Process smaller batches", 
                    "Break large configurations into smaller parts"),
                new Solution("Close unused configurations", 
                    "Close configurations you're not actively using")
            ))
            .preventionTips(List.of(
                "Monitor memory usage in the status bar",
                "Set appropriate heap size for your workload",
                "Regularly close unused configurations"
            ))
            .relatedErrors(List.of("SYSTEM_002", "PERFORMANCE_001"))
            .tags(List.of("memory", "performance", "system"))
            .build());
            
        // Network Issues
        addEntry(TroubleshootingEntry.builder()
            .id("NETWORK_001")
            .title("Connection Timeout")
            .category(ErrorContext.ErrorCategory.NETWORK)
            .severity(ErrorContext.ErrorSeverity.HIGH)
            .description("Network operation timed out while connecting to remote service.")
            .symptoms(List.of(
                "Timeout error messages",
                "Long delays before error",
                "Cannot reach remote services"
            ))
            .possibleCauses(List.of(
                "Network connectivity issues",
                "Firewall blocking connections",
                "Proxy configuration required",
                "Remote service unavailable"
            ))
            .solutions(List.of(
                new Solution("Check network connection", 
                    "Verify you have internet connectivity"),
                new Solution("Configure proxy settings", 
                    "Set proxy in Settings > Network if required"),
                new Solution("Check firewall", 
                    "Ensure the application is allowed through firewall")
            ))
            .preventionTips(List.of(
                "Configure network settings before use",
                "Test connectivity in Settings",
                "Have offline alternatives ready"
            ))
            .relatedErrors(List.of("NETWORK_002"))
            .tags(List.of("network", "connectivity"))
            .build());
            
        // UI Issues
        addEntry(TroubleshootingEntry.builder()
            .id("UI_001")
            .title("UI Freezing or Unresponsive")
            .category(ErrorContext.ErrorCategory.UI)
            .severity(ErrorContext.ErrorSeverity.HIGH)
            .description("The user interface becomes unresponsive or freezes.")
            .symptoms(List.of(
                "Clicking buttons has no effect",
                "Window cannot be moved or resized",
                "Progress indicators stuck"
            ))
            .possibleCauses(List.of(
                "Long-running operation on UI thread",
                "Large file being processed",
                "Deadlock in event handling"
            ))
            .solutions(List.of(
                new Solution("Wait for operation to complete", 
                    "Some operations may take time, check the progress bar"),
                new Solution("Force quit and restart", 
                    "Use Task Manager (Windows) or Force Quit (macOS)"),
                new Solution("Check logs", 
                    "Look for errors in the application logs")
            ))
            .preventionTips(List.of(
                "Save work frequently",
                "Monitor operation progress",
                "Process large files in smaller chunks"
            ))
            .relatedErrors(List.of("PERFORMANCE_001"))
            .tags(List.of("ui", "performance", "common"))
            .build());
            
        // Security Issues
        addEntry(TroubleshootingEntry.builder()
            .id("SECURITY_001")
            .title("Permission Denied")
            .category(ErrorContext.ErrorCategory.AUTHORIZATION)
            .severity(ErrorContext.ErrorSeverity.HIGH)
            .description("Insufficient permissions to perform the requested operation.")
            .symptoms(List.of(
                "Permission denied errors",
                "Cannot write to directory",
                "Cannot execute operations"
            ))
            .possibleCauses(List.of(
                "Running without required privileges",
                "File system permissions",
                "Security software blocking",
                "Operating system restrictions"
            ))
            .solutions(List.of(
                new Solution("Run as administrator", 
                    "Right-click and select 'Run as administrator' (Windows)"),
                new Solution("Check file permissions", 
                    "Ensure you have write access to the working directory"),
                new Solution("Check security software", 
                    "Temporarily disable antivirus to test")
            ))
            .preventionTips(List.of(
                "Install to a user-writable location",
                "Configure security exceptions",
                "Use appropriate user account"
            ))
            .relatedErrors(List.of("SYSTEM_003"))
            .tags(List.of("security", "permissions"))
            .build());
            
        // Performance Issues
        addEntry(TroubleshootingEntry.builder()
            .id("PERFORMANCE_001")
            .title("Slow Application Performance")
            .category(ErrorContext.ErrorCategory.SYSTEM)
            .severity(ErrorContext.ErrorSeverity.MEDIUM)
            .description("The application is running slower than expected.")
            .symptoms(List.of(
                "Slow response to user actions",
                "High CPU usage",
                "Operations take excessive time"
            ))
            .possibleCauses(List.of(
                "Insufficient system resources",
                "Too many background operations",
                "Large configuration files",
                "Antivirus scanning"
            ))
            .solutions(List.of(
                new Solution("Close other applications", 
                    "Free up system resources by closing unnecessary programs"),
                new Solution("Optimize configurations", 
                    "Simplify complex configurations where possible"),
                new Solution("Check background tasks", 
                    "View running tasks in the status bar")
            ))
            .preventionTips(List.of(
                "Keep configurations reasonably sized",
                "Regular system maintenance",
                "Monitor resource usage"
            ))
            .relatedErrors(List.of("SYSTEM_001", "UI_001"))
            .tags(List.of("performance", "common"))
            .build());
    }
    
    private void addEntry(TroubleshootingEntry entry) {
        // Add by ID
        troubleshootingDatabase.put(entry.getId(), entry);
        
        // Add by category
        troubleshootingDatabase.put(entry.getCategory().name(), entry);
        
        // Add by common error types
        if (entry.getId().startsWith("CONFIG")) {
            troubleshootingDatabase.put("ConfigurationException", entry);
        } else if (entry.getId().startsWith("VALIDATION")) {
            troubleshootingDatabase.put("ValidationException", entry);
        } else if (entry.getId().equals("SYSTEM_001")) {
            troubleshootingDatabase.put("OutOfMemoryError", entry);
        }
    }
    
    /**
     * Troubleshooting entry containing guidance for resolving issues.
     */
    @Data
    @Builder
    public static class TroubleshootingEntry {
        private String id;
        private String title;
        private ErrorContext.ErrorCategory category;
        private ErrorContext.ErrorSeverity severity;
        private String description;
        private List<String> symptoms;
        private List<String> possibleCauses;
        private List<Solution> solutions;
        private List<String> preventionTips;
        private List<String> relatedErrors;
        private List<String> tags;
    }
    
    /**
     * A specific solution with steps.
     */
    @Data
    @Builder
    public static class Solution {
        private String title;
        private String steps;
        
        public Solution(String title, String steps) {
            this.title = title;
            this.steps = steps;
        }
    }
}