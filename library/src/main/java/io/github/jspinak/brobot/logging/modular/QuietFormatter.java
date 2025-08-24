package io.github.jspinak.brobot.logging.modular;
import io.github.jspinak.brobot.action.ActionType;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.stereotype.Component;

/**
 * Formatter for QUIET verbosity level.
 * 
 * Produces minimal single-line output in the format:
 * ✓ Find Working.ClaudeIcon • 234ms
 * ✗ Find Working.ClaudeIcon • 234ms
 * 
 * Only logs completed actions (success or failure), skips start events.
 */
@Component
public class QuietFormatter implements ActionLogFormatter {
    
    @Override
    public String format(ActionResult actionResult) {
        if (!shouldLog(actionResult)) {
            return null;
        }
        
        // Extract information from ActionResult
        ActionResult.ActionExecutionContext context = actionResult.getExecutionContext();
        if (context == null) {
            return null;
        }
        
        StringBuilder formatted = new StringBuilder();
        
        // Success/failure symbol
        formatted.append(context.isSuccess() ? "✓" : "✗");
        formatted.append(" ");
        
        // Action type
        String actionType = context.getActionType();
        if (actionType != null && !actionType.isEmpty()) {
            // Clean up action type (remove _COMPLETE, _FAILED suffixes)
            actionType = cleanActionType(actionType);
            formatted.append(actionType);
        } else {
            formatted.append("Action");
        }
        
        // Target information
        String target = buildTargetInfo(context);
        if (target != null && !target.isEmpty()) {
            formatted.append(" ").append(target);
        }
        
        // Duration - always show it
        if (context.getExecutionDuration() != null) {
            formatted.append(" • ").append(context.getExecutionDuration().toMillis()).append("ms");
        } else {
            // If duration is missing, show 0ms
            formatted.append(" • 0ms");
        }
        
        return formatted.toString();
    }
    
    @Override
    public boolean shouldLog(ActionResult actionResult) {
        if (actionResult == null) {
            return false;
        }
        
        ActionResult.ActionExecutionContext context = actionResult.getExecutionContext();
        if (context == null) {
            return false;
        }
        
        // Only log completed actions (success or failure)
        // Skip start events and incomplete actions
        return context.getEndTime() != null;
    }
    
    @Override
    public VerbosityLevel getVerbosityLevel() {
        return VerbosityLevel.QUIET;
    }
    
    /**
     * Clean action type by removing suffixes like _COMPLETE, _FAILED, _START
     */
    private String cleanActionType(String actionType) {
        if (actionType == null) {
            return "";
        }
        
        // Remove common suffixes
        actionType = actionType.replaceAll("_(COMPLETE|FAILED|START)$", "");
        
        // Capitalize first letter, lowercase rest
        if (actionType.length() > 0) {
            return actionType.substring(0, 1).toUpperCase() + 
                   actionType.substring(1).toLowerCase();
        }
        
        return actionType;
    }
    
    /**
     * Build target information string from ActionExecutionContext
     */
    private String buildTargetInfo(ActionResult.ActionExecutionContext context) {
        StringBuilder targetInfo = new StringBuilder();
        
        // Priority order: StateImages, then Strings, then Regions
        if (!context.getTargetImages().isEmpty()) {
            // For single image, show State.Object format
            if (context.getTargetImages().size() == 1) {
                StateImage stateImage = context.getTargetImages().get(0);
                String imageName = stateImage.getName();
                String ownerState = stateImage.getOwnerStateName();
                
                if (ownerState != null && !ownerState.isEmpty() && !ownerState.equals("null")) {
                    targetInfo.append(ownerState).append(".");
                }
                
                if (imageName != null && !imageName.isEmpty()) {
                    targetInfo.append(imageName);
                } else {
                    targetInfo.append("Image");
                }
            } else {
                // For multiple images, show count and first name
                targetInfo.append("Images[").append(context.getTargetImages().size()).append("]");
                StateImage firstImage = context.getTargetImages().get(0);
                String firstName = firstImage.getName();
                if (firstName != null && !firstName.isEmpty()) {
                    targetInfo.append(": ").append(firstName).append("...");
                }
            }
        } else if (!context.getTargetStrings().isEmpty()) {
            if (context.getTargetStrings().size() == 1) {
                String firstString = context.getTargetStrings().get(0);
                if (firstString != null && firstString.length() <= 50) {
                    targetInfo.append("\"").append(firstString).append("\"");
                } else if (firstString != null) {
                    targetInfo.append("\"").append(firstString.substring(0, 47)).append("...\"");
                }
            } else {
                targetInfo.append("Strings[").append(context.getTargetStrings().size()).append("]");
            }
        } else if (!context.getTargetRegions().isEmpty()) {
            targetInfo.append("Regions[").append(context.getTargetRegions().size()).append("]");
        } else {
            // Fallback to primary target name if available
            String primaryTarget = context.getPrimaryTargetName();
            if (primaryTarget != null && !primaryTarget.isEmpty()) {
                targetInfo.append(primaryTarget);
            }
        }
        
        return targetInfo.toString();
    }
}