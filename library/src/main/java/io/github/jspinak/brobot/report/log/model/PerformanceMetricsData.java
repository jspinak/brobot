package io.github.jspinak.brobot.report.log.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformanceMetricsData {
    private long actionDuration;
    private long pageLoadTime;
    private long transitionTime;
    private long totalTestDuration;

    @Override
    public String toString() {
        return "PerformanceMetrics{" +
                "actionDuration=" + actionDuration +
                ", pageLoadTime=" + pageLoadTime +
                ", transitionTime=" + transitionTime +
                ", totalTestDuration=" + totalTestDuration +
                '}';
    }
}
