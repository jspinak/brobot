package io.github.jspinak.brobot.runner.listener;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
// import io.github.jspinak.brobot.events.ActionExecutedEvent; // TODO: This event doesn't exist yet
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.runner.service.ActionRecordingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens to action execution events from the brobot library and
 * records them using the ActionRecordingService.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BrobotActionListener {
    
    private final ActionRecordingService recordingService;
    
    /**
     * Handle action execution events from the brobot library
     */
    /* TODO: Enable when ActionExecutedEvent is available
    @EventListener
    @Async
    public void onActionExecuted(ActionExecutedEvent event) {
        if (!recordingService.isRecording()) {
            return;
        }
        
        try {
            // Extract data from the event
            ActionResult result = event.getActionResult();
            ActionConfig config = event.getActionConfig();
            StateObject stateObject = event.getStateObject();
            
            // Convert to ActionRecord
            ActionRecord record = createActionRecord(result, config);
            
            // Determine StateImage if applicable
            StateImage stateImage = null;
            if (stateObject instanceof StateImage) {
                stateImage = (StateImage) stateObject;
            }
            
            // Record the action
            recordingService.recordAction(record, stateImage);
            
            log.debug("Recorded action: {} - Success: {}", 
                config.getClass().getSimpleName(), 
                result.isSuccess());
            
        } catch (Exception e) {
            log.error("Error recording action from event: {}", e.getMessage(), e);
        }
    }
    */
    
    /**
     * Create ActionRecord from ActionResult and ActionConfig
     */
    private ActionRecord createActionRecord(ActionResult result, ActionConfig config) {
        ActionRecord.Builder builder = new ActionRecord.Builder();
        
        // Set configuration
        builder.setActionConfig(config);
        
        // Set result data
        builder.setActionSuccess(result.isSuccess());
        if (result.getDuration() != null) {
            builder.setDuration(result.getDuration().toMillis() / 1000.0);
        }
        
        // Add text if present
        if (result.getText() != null && !result.getText().isEmpty()) {
            // Use the first string from the text collection
            builder.setText(result.getText().get(0));
        }
        
        // Add matches
        if (result.getMatchList() != null && !result.getMatchList().isEmpty()) {
            result.getMatchList().forEach(builder::addMatch);
        }
        
        // Set state ID if available - ActionResult doesn't have getStateId
        // if (result.getStateId() != null) {
        //     builder.setStateId(result.getStateId());
        // }
        
        return builder.build();
    }
}