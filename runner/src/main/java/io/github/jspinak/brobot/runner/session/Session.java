package io.github.jspinak.brobot.runner.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a persisted automation session
 */
@Data
public class Session {
    private String id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String projectName;
    private String configPath;
    private String imagePath;

    private boolean active;

    // Application state
    private List<StateTransitions> stateTransitions;
    private Set<Long> activeStateIds;

    // Session data
    private Map<String, Object> stateData = new HashMap<>();
    private List<SessionEvent> events = new ArrayList<>();

    public void addEvent(SessionEvent event) {
        if (events == null) {
            events = new ArrayList<>();
        }
        events.add(event);
    }

    public void addStateData(String key, Object value) {
        if (stateData == null) {
            stateData = new HashMap<>();
        }
        stateData.put(key, value);
    }

    @JsonIgnore
    public Duration getDuration() {
        if (startTime == null) {
            return Duration.ZERO;
        }

        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end);
    }
}