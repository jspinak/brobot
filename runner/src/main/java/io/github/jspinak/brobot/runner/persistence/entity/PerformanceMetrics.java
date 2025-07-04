package io.github.jspinak.brobot.runner.persistence.entity;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;

import lombok.Builder;

import jakarta.persistence.Embeddable;
import lombok.Getter;
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
        return "PerformanceMetrics{" +
                "actionDuration=" + actionDuration +
                ", pageLoadTime=" + pageLoadTime +
                ", transitionTime=" + transitionTime +
                ", totalTestDuration=" + totalTestDuration +
                '}';
    }
}
