package io.github.jspinak.brobot.logging.modular;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Formatter for VERBOSE verbosity level.
 * 
 * Produces detailed output with full metadata, environment info, and timing details.
 * Multi-line format with comprehensive information for debugging and analysis.
 */
@Component
public class VerboseFormatter implements ActionLogFormatter {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    // Cache environment information that doesn't change during execution
    private static String cachedOsName = null;
    private static String cachedJavaVersion = null;
    private static Boolean cachedHeadlessMode = null;
    private static boolean environmentCached = false;
    
    @Override
    public String format(ActionResult actionResult) {
        if (!shouldLog(actionResult)) {
            return null;
        }
        
        ActionResult.ActionExecutionContext context = actionResult.getExecutionContext();
        if (context == null) {
            return null;
        }
        
        StringBuilder formatted = new StringBuilder();
        
        // Header with timestamp and action ID
        formatted.append("=== ACTION EXECUTION ===\n");
        if (context.getStartTime() != null) {
            formatted.append("Started:    ").append(context.getStartTime().atZone(java.time.ZoneId.systemDefault()).format(TIMESTAMP_FORMATTER)).append("\n");
        }
        if (context.getEndTime() != null) {
            formatted.append("Completed:  ").append(context.getEndTime().atZone(java.time.ZoneId.systemDefault()).format(TIMESTAMP_FORMATTER)).append("\n");
        }
        formatted.append("Action ID:  ").append(context.getActionId() != null ? context.getActionId() : "N/A").append("\n");
        formatted.append("Thread:     ").append(context.getExecutingThread() != null ? context.getExecutingThread() : Thread.currentThread().getName()).append("\n");
        
        // Action details
        formatted.append("\n--- ACTION DETAILS ---\n");
        formatted.append("Type:       ").append(context.getActionType() != null ? context.getActionType() : "Unknown").append("\n");
        formatted.append("Status:     ").append(context.isSuccess() ? "SUCCESS ✓" : "FAILED ✗").append("\n");
        
        if (context.getExecutionDuration() != null && !context.getExecutionDuration().isZero()) {
            formatted.append("Duration:   ").append(context.getExecutionDuration().toMillis()).append("ms\n");
        }
        
        // Target information
        if (hasTargets(context)) {
            formatted.append("\n--- TARGETS ---\n");
            
            if (!context.getTargetImages().isEmpty()) {
                formatted.append("Images (").append(context.getTargetImages().size()).append("):\n");
                for (int i = 0; i < context.getTargetImages().size(); i++) {
                    StateImage img = context.getTargetImages().get(i);
                    formatted.append("  [").append(i + 1).append("] ");
                    if (img.getOwnerStateName() != null && !img.getOwnerStateName().isEmpty()) {
                        formatted.append(img.getOwnerStateName()).append(".");
                    }
                    formatted.append(img.getName() != null ? img.getName() : "Unnamed").append("\n");
                }
            }
            
            if (!context.getTargetStrings().isEmpty()) {
                formatted.append("Strings (").append(context.getTargetStrings().size()).append("):\n");
                for (int i = 0; i < context.getTargetStrings().size(); i++) {
                    formatted.append("  [").append(i + 1).append("] \"").append(context.getTargetStrings().get(i)).append("\"\n");
                }
            }
            
            if (!context.getTargetRegions().isEmpty()) {
                formatted.append("Regions (").append(context.getTargetRegions().size()).append("):\n");
                for (int i = 0; i < context.getTargetRegions().size(); i++) {
                    formatted.append("  [").append(i + 1).append("] ").append(context.getTargetRegions().get(i).toString()).append("\n");
                }
            }
            
            if (context.getPrimaryTargetName() != null && !context.getPrimaryTargetName().isEmpty()) {
                formatted.append("Primary:    ").append(context.getPrimaryTargetName()).append("\n");
            }
        }
        
        // Results
        if (!context.getResultMatches().isEmpty()) {
            formatted.append("\n--- RESULTS ---\n");
            formatted.append("Matches:    ").append(context.getResultMatches().size()).append("\n");
            for (int i = 0; i < Math.min(context.getResultMatches().size(), 5); i++) {
                var match = context.getResultMatches().get(i);
                formatted.append("  [").append(i + 1).append("] Score: ").append(String.format("%.3f", match.getScore()))
                          .append(" Region: ").append(match.getRegion().toString()).append("\n");
            }
            if (context.getResultMatches().size() > 5) {
                formatted.append("  ... and ").append(context.getResultMatches().size() - 5).append(" more matches\n");
            }
        } else if (context.getEndTime() != null) {
            formatted.append("\n--- RESULTS ---\n");
            formatted.append("Matches:    0 (No matches found)\n");
        }
        
        // Error information
        if (context.getExecutionError() != null) {
            formatted.append("\n--- ERROR ---\n");
            formatted.append("Exception:  ").append(context.getExecutionError().getClass().getSimpleName()).append("\n");
            formatted.append("Message:    ").append(context.getExecutionError().getMessage() != null ? context.getExecutionError().getMessage() : "No message").append("\n");
            if (context.getExecutionError().getStackTrace().length > 0) {
                formatted.append("Location:   ").append(context.getExecutionError().getStackTrace()[0].toString()).append("\n");
            }
        }
        
        // Environment information - cache static values and only log once
        ActionResult.EnvironmentSnapshot env = actionResult.getEnvironmentSnapshot();
        if (env != null) {
            // Cache static environment values on first run
            if (!environmentCached && env.getOsName() != null) {
                cachedOsName = env.getOsName();
                cachedJavaVersion = env.getJavaVersion();
                cachedHeadlessMode = env.isHeadlessMode();
                environmentCached = true;
                
                // Log full environment info on first run
                formatted.append("\n--- ENVIRONMENT (Initial) ---\n");
                formatted.append("OS:         ").append(cachedOsName != null ? cachedOsName : "Unknown").append("\n");
                formatted.append("Java:       ").append(cachedJavaVersion != null ? cachedJavaVersion : "Unknown").append("\n");
                formatted.append("Headless:   ").append(cachedHeadlessMode != null ? cachedHeadlessMode : false).append("\n");
                if (env.getMonitors() != null && !env.getMonitors().isEmpty()) {
                    formatted.append("Monitors:   ").append(env.getMonitors().size()).append("\n");
                }
            } else if (!environmentCached) {
                // Only include environment section if not yet cached
                formatted.append("\n--- ENVIRONMENT ---\n");
                formatted.append("OS:         ").append(env.getOsName() != null ? env.getOsName() : "Unknown").append("\n");
                formatted.append("Java:       ").append(env.getJavaVersion() != null ? env.getJavaVersion() : "Unknown").append("\n");
                formatted.append("Headless:   ").append(env.isHeadlessMode()).append("\n");
            }
            // Always include capture time if available (this changes)
            if (env.getCaptureTime() != null && !environmentCached) {
                if (!formatted.toString().contains("--- ENVIRONMENT")) {
                    formatted.append("\n--- CAPTURE ---\n");
                }
                formatted.append("Captured:   ").append(env.getCaptureTime().atZone(java.time.ZoneId.systemDefault()).format(TIMESTAMP_FORMATTER)).append("\n");
            }
        }
        
        // Performance metrics
        ActionResult.ActionMetrics metrics = actionResult.getActionMetrics();
        if (metrics != null) {
            formatted.append("\n--- METRICS ---\n");
            formatted.append("Execution Time:   ").append(metrics.getExecutionTimeMs()).append("ms\n");
            formatted.append("Match Count:      ").append(metrics.getMatchCount()).append("\n");
            formatted.append("Best Match Score: ").append(String.format("%.3f", metrics.getBestMatchConfidence())).append("\n");
            if (metrics.getThreadName() != null) {
                formatted.append("Thread:           ").append(metrics.getThreadName()).append("\n");
            }
            if (metrics.getActionId() != null) {
                formatted.append("Action ID:        ").append(metrics.getActionId()).append("\n");
            }
        }
        
        formatted.append("========================\n");
        
        return formatted.toString();
    }
    
    @Override
    public boolean shouldLog(ActionResult actionResult) {
        if (actionResult == null) {
            return false;
        }
        
        ActionResult.ActionExecutionContext context = actionResult.getExecutionContext();
        
        // Verbose mode logs everything - start events, completion, failures
        return context != null && 
               (context.getStartTime() != null || context.getEndTime() != null);
    }
    
    @Override
    public VerbosityLevel getVerbosityLevel() {
        return VerbosityLevel.VERBOSE;
    }
    
    /**
     * Check if the context has any target information
     */
    private boolean hasTargets(ActionResult.ActionExecutionContext context) {
        return !context.getTargetImages().isEmpty() || 
               !context.getTargetStrings().isEmpty() || 
               !context.getTargetRegions().isEmpty() ||
               (context.getPrimaryTargetName() != null && !context.getPrimaryTargetName().isEmpty());
    }
}