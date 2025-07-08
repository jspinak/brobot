package io.github.jspinak.brobot.runner.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class StartupProgressEvent extends BrobotEvent {
    private final String taskName;
    private final int completedTasks;
    private final int totalTasks;
    private final double progressPercentage;
    
    public StartupProgressEvent(String taskName, int completedTasks, int totalTasks, double progressPercentage) {
        super(EventType.SYSTEM_STARTUP, taskName);
        this.taskName = taskName;
        this.completedTasks = completedTasks;
        this.totalTasks = totalTasks;
        this.progressPercentage = progressPercentage;
    }
}