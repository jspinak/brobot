package io.github.jspinak.brobot.runner.ui.components;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

/**
 * Panel for displaying resource usage charts.
 * Extracted from ResourceMonitorPanel for better modularity.
 */
public class ResourceChartPanel extends VBox {
    
    private LineChart<Number, Number> memoryChart;
    private XYChart.Series<Number, Number> memorySeries;
    private int timeIndex = 0;
    private static final int MAX_DATA_POINTS = 20;
    
    public ResourceChartPanel() {
        setupChart();
    }
    
    private void setupChart() {
        // Create axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (30s intervals)");
        yAxis.setLabel("Memory Usage (MB)");
        xAxis.setForceZeroInRange(false);
        yAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
        
        // Create chart
        memoryChart = new LineChart<>(xAxis, yAxis);
        memoryChart.setTitle("Memory Usage Over Time");
        memoryChart.setAnimated(false);
        memoryChart.setPrefHeight(200);
        memoryChart.setCreateSymbols(false);
        memoryChart.getStyleClass().add("memory-chart");
        memoryChart.setLegendVisible(false);
        
        // Create data series
        memorySeries = new XYChart.Series<>();
        memorySeries.setName("Memory (MB)");
        memoryChart.getData().add(memorySeries);
        
        // Add to panel
        getChildren().add(memoryChart);
        getStyleClass().add("resource-chart-panel");
    }
    
    /**
     * Add a new data point to the chart.
     * Automatically removes old data points to maintain performance.
     */
    public void addDataPoint(double memoryMB) {
        memorySeries.getData().add(new XYChart.Data<>(timeIndex++, memoryMB));
        
        // Remove old data points
        if (memorySeries.getData().size() > MAX_DATA_POINTS) {
            memorySeries.getData().remove(0);
        }
    }
    
    /**
     * Clear all data from the chart.
     */
    public void clearData() {
        memorySeries.getData().clear();
        timeIndex = 0;
    }
    
    /**
     * Set the visibility of the chart legend.
     */
    public void setLegendVisible(boolean visible) {
        memoryChart.setLegendVisible(visible);
    }
    
    /**
     * Update chart styling.
     */
    public void applyStyle(String... styleClasses) {
        memoryChart.getStyleClass().addAll(styleClasses);
    }
}