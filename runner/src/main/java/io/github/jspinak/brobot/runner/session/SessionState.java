package io.github.jspinak.brobot.runner.session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents the state of an application session. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionState {
    private String sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastSaveTime;
    private boolean endedNormally;
    private String applicationVersion;

    // Open configurations
    private List<String> openConfigurations;

    // Window states
    private Map<String, WindowState> windowStates;

    // User preferences
    private Map<String, String> userPreferences;

    // Execution history
    private List<ExecutionRecord> executionHistory;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class WindowState {
    private double x;
    private double y;
    private double width;
    private double height;
    private boolean maximized;
    private boolean minimized;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ExecutionRecord {
    private String executionId;
    private String configurationName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean completed;
    private String status;
}

record RecoverySuggestions(List<String> suggestions, List<String> warnings) {}
