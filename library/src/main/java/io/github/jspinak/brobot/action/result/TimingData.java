package io.github.jspinak.brobot.action.result;

import lombok.Data;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages timing information for action execution.
 * Tracks start time, end time, duration, and optional time segments.
 * 
 * This class encapsulates all timing-related functionality that was
 * previously embedded in ActionResult.
 * 
 * @since 2.0
 */
@Data
public class TimingData {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration totalDuration = Duration.ZERO;
    private List<TimeSegment> segments = new ArrayList<>();
    private Instant instantStart;
    private Instant instantEnd;
    
    /**
     * Creates TimingData with the current time as start.
     */
    public TimingData() {
        start();
    }
    
    /**
     * Creates TimingData with specified start time.
     * 
     * @param startTime The start time
     */
    public TimingData(LocalDateTime startTime) {
        this.startTime = startTime;
        this.instantStart = Instant.now();
    }
    
    /**
     * Starts or restarts timing.
     */
    public void start() {
        this.startTime = LocalDateTime.now();
        this.instantStart = Instant.now();
        this.endTime = null;
        this.instantEnd = null;
    }
    
    /**
     * Stops timing and calculates duration.
     */
    public void stop() {
        if (startTime != null) {
            if (endTime == null) {
                this.endTime = LocalDateTime.now();
                this.instantEnd = Instant.now();
            }
            calculateDuration();
        }
    }
    
    /**
     * Gets the elapsed duration.
     * If still running, calculates duration to current time.
     * 
     * @return The elapsed duration
     */
    public Duration getElapsed() {
        if (startTime == null) {
            return Duration.ZERO;
        }
        
        if (endTime == null) {
            // Still running, calculate to now
            if (instantStart != null) {
                return Duration.between(instantStart, Instant.now());
            }
            return Duration.between(startTime, LocalDateTime.now());
        }
        
        return totalDuration;
    }
    
    /**
     * Gets execution time in milliseconds.
     * 
     * @return Execution time in milliseconds
     */
    public long getExecutionTimeMs() {
        return getElapsed().toMillis();
    }
    
    /**
     * Adds a time segment for tracking sub-operations.
     * 
     * @param name Name of the segment
     * @param duration Duration of the segment
     */
    public void addSegment(String name, Duration duration) {
        segments.add(new TimeSegment(name, duration));
    }
    
    /**
     * Adds a time segment with start and end times.
     * 
     * @param name Name of the segment
     * @param segmentStart Start time of segment
     * @param segmentEnd End time of segment
     */
    public void addSegment(String name, Instant segmentStart, Instant segmentEnd) {
        Duration segmentDuration = Duration.between(segmentStart, segmentEnd);
        addSegment(name, segmentDuration);
    }
    
    /**
     * Merges timing data from another instance.
     * Adds the durations and combines segments.
     * 
     * @param other The TimingData to merge
     */
    public void merge(TimingData other) {
        if (other != null) {
            totalDuration = totalDuration.plus(other.getElapsed());
            segments.addAll(other.segments);
        }
    }
    
    /**
     * Checks if timing has started.
     * 
     * @return true if start time is set
     */
    public boolean hasStarted() {
        return startTime != null;
    }
    
    /**
     * Checks if timing has completed.
     * 
     * @return true if end time is set
     */
    public boolean hasCompleted() {
        return endTime != null;
    }
    
    /**
     * Gets the total duration of all segments.
     * 
     * @return Sum of all segment durations
     */
    public Duration getSegmentsDuration() {
        return segments.stream()
                .map(TimeSegment::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }
    
    /**
     * Gets overhead time (total duration minus segments).
     * Useful for identifying time spent outside tracked segments.
     * 
     * @return Overhead duration
     */
    public Duration getOverhead() {
        return totalDuration.minus(getSegmentsDuration());
    }
    
    /**
     * Formats the timing data as a string.
     * 
     * @return Formatted timing information
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Duration: ").append(formatDuration(getElapsed()));
        
        if (!segments.isEmpty()) {
            sb.append(" (");
            for (int i = 0; i < segments.size(); i++) {
                if (i > 0) sb.append(", ");
                TimeSegment segment = segments.get(i);
                sb.append(segment.getName()).append(": ")
                  .append(formatDuration(segment.getDuration()));
            }
            sb.append(")");
        }
        
        return sb.toString();
    }
    
    private void calculateDuration() {
        if (instantStart != null && instantEnd != null) {
            totalDuration = Duration.between(instantStart, instantEnd);
        } else if (startTime != null && endTime != null) {
            totalDuration = Duration.between(startTime, endTime);
        }
    }
    
    private String formatDuration(Duration duration) {
        long millis = duration.toMillis();
        if (millis < 1000) {
            return millis + "ms";
        } else if (millis < 60000) {
            return String.format("%.1fs", millis / 1000.0);
        } else {
            long minutes = millis / 60000;
            long seconds = (millis % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    /**
     * Represents a timed segment within the overall execution.
     */
    @Data
    public static class TimeSegment {
        private final String name;
        private final Duration duration;
        
        public TimeSegment(String name, Duration duration) {
            this.name = name;
            this.duration = duration != null ? duration : Duration.ZERO;
        }
    }
}