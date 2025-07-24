package io.github.jspinak.brobot.runner.ui.illustration.analytics;

import javafx.beans.property.*;

/**
 * JavaFX-compatible data model for action statistics in the analytics table.
 *
 * @see IllustrationAnalyticsDashboard
 */
public class ActionStatistic {
    
    private final StringProperty action;
    private final IntegerProperty count;
    private final StringProperty successRate;
    private final StringProperty avgTime;
    
    public ActionStatistic(String action, int count, double successRate, double avgTime) {
        this.action = new SimpleStringProperty(action);
        this.count = new SimpleIntegerProperty(count);
        this.successRate = new SimpleStringProperty(String.format("%.1f%%", successRate * 100));
        this.avgTime = new SimpleStringProperty(String.format("%.0fms", avgTime));
    }
    
    public StringProperty actionProperty() {
        return action;
    }
    
    public IntegerProperty countProperty() {
        return count;
    }
    
    public StringProperty successRateProperty() {
        return successRate;
    }
    
    public StringProperty avgTimeProperty() {
        return avgTime;
    }
}