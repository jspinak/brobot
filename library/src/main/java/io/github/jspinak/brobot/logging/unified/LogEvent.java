package io.github.jspinak.brobot.logging.unified;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable log event model representing a single logging entry.
 * 
 * <p>LogEvent encapsulates all information about a logged event in the Brobot framework.
 * It uses the builder pattern for construction and is immutable once created. This design
 * ensures thread safety and prevents accidental modification of log data as it flows
 * through the logging pipeline.</p>
 * 
 * <p>Event types:
 * <ul>
 *   <li>ACTION - User actions like clicks, types, hovers</li>
 *   <li>TRANSITION - State transitions in the application</li>
 *   <li>OBSERVATION - General observations about application state</li>
 *   <li>PERFORMANCE - Performance metrics and timings</li>
 *   <li>ERROR - Error conditions and exceptions</li>
 * </ul>
 * </p>
 * 
 * @since 2.0
 * @see BrobotLogger
 * @see LogBuilder
 */
public class LogEvent {
    
    /**
     * Types of log events.
     */
    public enum Type {
        ACTION,
        TRANSITION,
        OBSERVATION,
        PERFORMANCE,
        ERROR
    }
    
    /**
     * Log levels for severity.
     */
    public enum Level {
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }
    
    // Core fields
    private final Type type;
    private final Level level;
    private final String message;
    private final long timestamp;
    
    // Context fields
    private final String sessionId;
    private final String stateId;
    private final String action;
    private final String target;
    
    // State transition fields
    private final String fromState;
    private final String toState;
    
    // Result fields
    private final boolean success;
    private final Long duration;
    private final Throwable error;
    
    // Additional data
    private final Map<String, Object> metadata;
    
    /**
     * Private constructor - use builder pattern.
     */
    private LogEvent(Builder builder) {
        this.type = builder.type;
        this.level = builder.level != null ? builder.level : Level.INFO;
        this.message = builder.message;
        this.timestamp = builder.timestamp != null ? builder.timestamp : System.currentTimeMillis();
        this.sessionId = builder.sessionId;
        this.stateId = builder.stateId;
        this.action = builder.action;
        this.target = builder.target;
        this.fromState = builder.fromState;
        this.toState = builder.toState;
        this.success = builder.success;
        this.duration = builder.duration;
        this.error = builder.error;
        this.metadata = builder.metadata != null ? 
            new HashMap<>(builder.metadata) : new HashMap<>();
    }
    
    // Getters
    public Type getType() { return type; }
    public Level getLevel() { return level; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getSessionId() { return sessionId; }
    public String getStateId() { return stateId; }
    public String getAction() { return action; }
    public String getTarget() { return target; }
    public String getFromState() { return fromState; }
    public String getToState() { return toState; }
    public boolean isSuccess() { return success; }
    public Long getDuration() { return duration; }
    public Throwable getError() { return error; }
    public Map<String, Object> getMetadata() { 
        return new HashMap<>(metadata); 
    }
    
    /**
     * Gets a specific metadata value.
     * 
     * @param key The metadata key
     * @return The value or null if not present
     */
    public Object getMetadataValue(String key) {
        return metadata.get(key);
    }
    
    /**
     * Creates a new builder instance.
     * 
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for LogEvent instances.
     */
    public static class Builder {
        private Type type = Type.ACTION;
        private Level level;
        private String message;
        private Long timestamp;
        private String sessionId;
        private String stateId;
        private String action;
        private String target;
        private String fromState;
        private String toState;
        private boolean success = true;
        private Long duration;
        private Throwable error;
        private Map<String, Object> metadata;
        
        public Builder type(Type type) {
            this.type = type;
            return this;
        }
        
        public Builder level(Level level) {
            this.level = level;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder stateId(String stateId) {
            this.stateId = stateId;
            return this;
        }
        
        public Builder action(String action) {
            this.action = action;
            return this;
        }
        
        public Builder target(String target) {
            this.target = target;
            return this;
        }
        
        public Builder fromState(String fromState) {
            this.fromState = fromState;
            return this;
        }
        
        public Builder toState(String toState) {
            this.toState = toState;
            return this;
        }
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder duration(long duration) {
            this.duration = duration;
            return this;
        }
        
        public Builder error(Throwable error) {
            this.error = error;
            this.success = false; // Errors imply failure
            return this;
        }
        
        public Builder metadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.putAll(metadata);
            return this;
        }
        
        public LogEvent build() {
            return new LogEvent(this);
        }
    }
    
    /**
     * Formats the event as a human-readable string.
     * Used for console output.
     * 
     * @return A formatted string representation
     */
    public String toFormattedString() {
        return toFormattedString(false);
    }
    
    /**
     * Formats the event as a human-readable string with verbosity control.
     * 
     * @param verbose Whether to include verbose details
     * @return A formatted string representation
     */
    public String toFormattedString(boolean verbose) {
        StringBuilder sb = new StringBuilder();
        
        if (verbose) {
            // Verbose mode - include all details
            if (sessionId != null) {
                sb.append("[").append(sessionId).append("] ");
            }
        }
        
        // Format based on type
        switch (type) {
            case ACTION:
                sb.append(action != null ? action : "ACTION");
                if (target != null) {
                    sb.append(" → ").append(target);
                }
                if (verbose && duration != null) {
                    sb.append(" (").append(duration).append("ms)");
                }
                break;
                
            case TRANSITION:
                sb.append("STATE: ");
                sb.append(fromState != null ? fromState : "?");
                sb.append(" → ");
                sb.append(toState != null ? toState : "?");
                if (verbose && duration != null) {
                    sb.append(" [").append(duration).append("ms]");
                }
                break;
                
            case OBSERVATION:
                sb.append("OBSERVE: ").append(message);
                break;
                
            case PERFORMANCE:
                sb.append("PERF: ").append(message);
                if (duration != null) {
                    sb.append(" (").append(duration).append("ms)");
                }
                break;
                
            case ERROR:
                sb.append("ERROR: ").append(message);
                if (verbose && error != null) {
                    sb.append(" - ").append(error.getClass().getSimpleName());
                }
                break;
        }
        
        // Add success indicator for actions and transitions
        if ((type == Type.ACTION || type == Type.TRANSITION) && !success) {
            sb.append(" [FAILED]");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("LogEvent[type=%s, level=%s, message=%s, session=%s, state=%s, timestamp=%d]",
                type, level, message, sessionId, stateId, timestamp);
    }
}