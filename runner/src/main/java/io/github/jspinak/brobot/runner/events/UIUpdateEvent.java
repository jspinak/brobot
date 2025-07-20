package io.github.jspinak.brobot.runner.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Event for UI component updates.
 * This event is used to decouple UI event handlers from specific UI components.
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UIUpdateEvent extends BrobotEvent {
    
    private final String updateType;
    private final Object updateData;
    
    public UIUpdateEvent(Object source, String updateType, Object updateData) {
        super(EventType.UI_STATE_CHANGED, source);
        this.updateType = updateType;
        this.updateData = updateData;
    }
    
    public String getMessage() {
        return String.format("UI Update: %s", updateType);
    }
}