package io.github.jspinak.brobot.runner.ui.execution;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import io.github.jspinak.brobot.runner.ui.components.base.BrobotCard;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Panel component for displaying performance metrics and charts.
 *
 * <p>This panel displays:
 *
 * <ul>
 *   <li>Total actions executed
 *   <li>Average action duration
 *   <li>Success rate percentage
 *   <li>Peak memory usage
 *   <li>Performance charts for action duration and match time
 * </ul>
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class PerformanceMetricsPanel extends BrobotCard {

    // Metrics labels
    @Getter private Label totalActionsLabel;
    @Getter private Label avgActionDurationLabel;
    @Getter private Label successRateLabel;
    @Getter private Label peakMemoryLabel;

    // Charts
    @Getter private LineChart<Number, Number> performanceChart;
    @Getter private XYChart.Series<Number, Number> actionDurationSeries;
    @Getter private XYChart.Series<Number, Number> matchTimeSeries;

    // Data tracking
    private final Queue<PerformanceMetric> performanceMetrics = new ConcurrentLinkedQueue<>();
    private final AtomicLong totalActions = new AtomicLong(0);
    private final AtomicLong successfulActions = new AtomicLong(0);
    private final AtomicLong totalDuration = new AtomicLong(0);
    private long peakMemory = 0;

    // Constants
    private static final int MAX_CHART_POINTS = 100;

    /** Creates a new PerformanceMetricsPanel. */
    public PerformanceMetricsPanel() {
        super("Performance Metrics");
        initializeContent();
    }

    private void initializeContent() {
        VBox performancePanel = new VBox(15);
        performancePanel.setPadding(new Insets(16));
        setupUI(performancePanel);
        addContent(performancePanel);
    }

    private void setupUI(VBox performancePanel) {
        // Performance metrics summary
        GridPane metricsGrid = createMetricsGrid();

        // Performance chart
        createPerformanceChart();

        performancePanel.getChildren().addAll(metricsGrid, performanceChart);
    }

    private GridPane createMetricsGrid() {
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(20);
        metricsGrid.setVgap(5);
        metricsGrid.setPadding(new Insets(10));
        metricsGrid.setStyle(
                "-fx-background-color: #f0f0f0; -fx-border-color: #ddd; -fx-border-radius: 5;");

        metricsGrid.add(new Label("Total Actions:"), 0, 0);
        totalActionsLabel = new Label("0");
        metricsGrid.add(totalActionsLabel, 1, 0);

        metricsGrid.add(new Label("Avg Action Duration:"), 0, 1);
        avgActionDurationLabel = new Label("0 ms");
        metricsGrid.add(avgActionDurationLabel, 1, 1);

        metricsGrid.add(new Label("Success Rate:"), 2, 0);
        successRateLabel = new Label("0%");
        metricsGrid.add(successRateLabel, 3, 0);

        metricsGrid.add(new Label("Peak Memory:"), 2, 1);
        peakMemoryLabel = new Label("0 MB");
        metricsGrid.add(peakMemoryLabel, 3, 1);

        return metricsGrid;
    }

    private void createPerformanceChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        performanceChart = new LineChart<>(xAxis, yAxis);
        performanceChart.setTitle("Performance Metrics");
        performanceChart.setAnimated(false);
        performanceChart.setCreateSymbols(false);
        performanceChart.setPrefHeight(200);

        xAxis.setLabel("Action #");
        yAxis.setLabel("Time (ms)");

        actionDurationSeries = new XYChart.Series<>();
        actionDurationSeries.setName("Action Duration");

        matchTimeSeries = new XYChart.Series<>();
        matchTimeSeries.setName("Match Time");

        performanceChart.getData().addAll(actionDurationSeries, matchTimeSeries);
    }

    /**
     * Adds a performance metric to the panel.
     *
     * @param actionNumber The action number
     * @param actionDuration The duration of the action in milliseconds
     * @param matchTime The time spent matching in milliseconds
     * @param successful Whether the action was successful
     */
    public void addPerformanceMetric(
            long actionNumber, long actionDuration, long matchTime, boolean successful) {
        // Update counters
        totalActions.incrementAndGet();
        totalDuration.addAndGet(actionDuration);
        if (successful) {
            successfulActions.incrementAndGet();
        }

        // Add to chart
        actionDurationSeries.getData().add(new XYChart.Data<>(actionNumber, actionDuration));
        matchTimeSeries.getData().add(new XYChart.Data<>(actionNumber, matchTime));

        // Limit chart points
        if (actionDurationSeries.getData().size() > MAX_CHART_POINTS) {
            actionDurationSeries.getData().remove(0);
        }
        if (matchTimeSeries.getData().size() > MAX_CHART_POINTS) {
            matchTimeSeries.getData().remove(0);
        }

        // Update labels
        updateLabels();
    }

    /**
     * Updates the memory usage display.
     *
     * @param memoryUsageMB Current memory usage in megabytes
     */
    public void updateMemoryUsage(long memoryUsageMB) {
        if (memoryUsageMB > peakMemory) {
            peakMemory = memoryUsageMB;
            peakMemoryLabel.setText(peakMemory + " MB");
        }
    }

    private void updateLabels() {
        long total = totalActions.get();
        totalActionsLabel.setText(String.valueOf(total));

        if (total > 0) {
            long avgDuration = totalDuration.get() / total;
            avgActionDurationLabel.setText(avgDuration + " ms");

            double successRate = (successfulActions.get() * 100.0) / total;
            successRateLabel.setText(String.format("%.1f%%", successRate));
        }
    }

    /** Resets all metrics to initial values. */
    public void reset() {
        totalActions.set(0);
        successfulActions.set(0);
        totalDuration.set(0);
        peakMemory = 0;

        totalActionsLabel.setText("0");
        avgActionDurationLabel.setText("0 ms");
        successRateLabel.setText("0%");
        peakMemoryLabel.setText("0 MB");

        actionDurationSeries.getData().clear();
        matchTimeSeries.getData().clear();
        performanceMetrics.clear();
    }

    /** Internal class for tracking performance metrics. */
    @Getter
    private static class PerformanceMetric {
        private final long timestamp;
        private final long actionDuration;
        private final long matchTime;
        private final boolean successful;

        public PerformanceMetric(long actionDuration, long matchTime, boolean successful) {
            this.timestamp = System.currentTimeMillis();
            this.actionDuration = actionDuration;
            this.matchTime = matchTime;
            this.successful = successful;
        }
    }
}
