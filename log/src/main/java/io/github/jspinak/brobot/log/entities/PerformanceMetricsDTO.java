package io.github.jspinak.brobot.log.entities;

import lombok.Data;

@Data
public class PerformanceMetricsDTO {
    private long actionDuration;
    private long pageLoadTime;
    private long transitionTime;
    private long totalTestDuration;
}
