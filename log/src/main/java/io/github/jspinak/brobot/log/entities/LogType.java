package io.github.jspinak.brobot.log.entities;

public enum LogType {
    ACTION, // For action logs (FIND, CLICK, etc)
    TRANSITION, // For state transitions
    STATE_IMAGE, // For state image logs
    ERROR, // For errors
    SESSION, // For session start/end
    VIDEO, // For video recordings
    METRICS // For performance metrics
}
