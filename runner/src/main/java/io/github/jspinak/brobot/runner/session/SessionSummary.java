package io.github.jspinak.brobot.runner.session;

import java.time.LocalDateTime;

import lombok.Data;

/** A lightweight summary of a session for display in the UI */
@Data
public class SessionSummary {
    private String id;
    private String projectName;
    private String sessionName;
    private String configPath;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastSaved;
    private Boolean active;
    private Long fileSize;

    public String getFormattedDuration() {
        if (startTime == null) {
            return "Unknown";
        }

        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(startTime, end);

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    public String getStatus() {
        if (Boolean.TRUE.equals(active)) {
            return "Active";
        } else if (endTime != null) {
            return "Completed";
        } else {
            return "Unknown";
        }
    }
}
