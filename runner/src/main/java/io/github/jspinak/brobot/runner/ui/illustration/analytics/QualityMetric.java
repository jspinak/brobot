package io.github.jspinak.brobot.runner.ui.illustration.analytics;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * JavaFX-compatible data model for quality metrics in the analytics table.
 *
 * @see IllustrationAnalyticsDashboard
 */
public class QualityMetric {
    
    private final StringProperty metricName;
    private final StringProperty value;
    private final StringProperty trend;
    
    public QualityMetric(String metricName, String value, String trend) {
        this.metricName = new SimpleStringProperty(metricName);
        this.value = new SimpleStringProperty(value);
        this.trend = new SimpleStringProperty(trend);
    }
    
    public StringProperty metricNameProperty() {
        return metricName;
    }
    
    public StringProperty valueProperty() {
        return value;
    }
    
    public StringProperty trendProperty() {
        return trend;
    }
}