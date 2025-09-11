package io.github.jspinak.brobot.runner.persistence.entity;

import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetrics {
    private long actionDuration;
    private long pageLoadTime;
    private long transitionTime;
    private long totalTestDuration;

    @Override
    public String toString() {
        return "PerformanceMetrics{"
                + "actionDuration="
                + actionDuration
                + ", pageLoadTime="
                + pageLoadTime
                + ", transitionTime="
                + transitionTime
                + ", totalTestDuration="
                + totalTestDuration
                + '}';
    }
}
