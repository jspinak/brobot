package io.github.jspinak.brobot.runner.ui.illustration.analytics;

import io.github.jspinak.brobot.runner.ui.theme.UnifiedThemeManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics dashboard for illustration system performance and patterns.
 * <p>
 * This dashboard provides real-time and historical analytics including:
 * <ul>
 * <li>Performance metrics (throughput, processing time, queue status)</li>
 * <li>Action distribution and success rates</li>
 * <li>Quality metrics and sampling effectiveness</li>
 * <li>Resource utilization and optimization metrics</li>
 * </ul>
 *
 * @see IllustrationAnalyticsService
 * @see AnalyticsMetric
 */
@Component
@Getter
public class IllustrationAnalyticsDashboard extends ScrollPane {
    
    private final IllustrationAnalyticsService analyticsService;
    private final UnifiedThemeManager themeService;
    
    // Charts
    private final LineChart<String, Number> throughputChart;
    private final PieChart actionDistributionChart;
    private final BarChart<String, Number> successRateChart;
    private final LineChart<String, Number> performanceChart;
    
    // Metrics panels
    private final MetricPanel totalIllustrationsPanel;
    private final MetricPanel successRatePanel;
    private final MetricPanel avgProcessingTimePanel;
    private final MetricPanel queueHealthPanel;
    
    // Tables
    private final TableView<ActionStatistic> actionStatsTable;
    private final TableView<QualityMetric> qualityMetricsTable;
    
    // Update timeline
    private Timeline updateTimeline;
    
    @Autowired
    public IllustrationAnalyticsDashboard(IllustrationAnalyticsService analyticsService,
                                         UnifiedThemeManager themeService) {
        this.analyticsService = analyticsService;
        this.themeService = themeService;
        
        // Initialize charts
        throughputChart = createThroughputChart();
        actionDistributionChart = createActionDistributionChart();
        successRateChart = createSuccessRateChart();
        performanceChart = createPerformanceChart();
        
        // Initialize metric panels
        totalIllustrationsPanel = new MetricPanel("Total Illustrations", "0", Color.DODGERBLUE);
        successRatePanel = new MetricPanel("Success Rate", "0%", Color.GREEN);
        avgProcessingTimePanel = new MetricPanel("Avg Processing", "0ms", Color.ORANGE);
        queueHealthPanel = new MetricPanel("Queue Health", "Good", Color.PURPLE);
        
        // Initialize tables
        actionStatsTable = createActionStatsTable();
        qualityMetricsTable = createQualityMetricsTable();
        
        // Setup UI
        setupUI();
        
        // Start updates
        startUpdates();
    }
    
    /**
     * Sets up the dashboard UI layout.
     */
    private void setupUI() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: -color-bg-default;");
        
        // Header
        Label headerLabel = new Label("Illustration Analytics Dashboard");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Summary metrics row
        HBox metricsRow = new HBox(20);
        metricsRow.getChildren().addAll(
            totalIllustrationsPanel,
            successRatePanel,
            avgProcessingTimePanel,
            queueHealthPanel
        );
        
        // Charts grid
        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(20);
        chartsGrid.setVgap(20);
        
        // Configure chart sizes
        throughputChart.setPrefSize(600, 300);
        actionDistributionChart.setPrefSize(400, 300);
        successRateChart.setPrefSize(600, 300);
        performanceChart.setPrefSize(400, 300);
        
        chartsGrid.add(createChartContainer("Throughput Over Time", throughputChart), 0, 0);
        chartsGrid.add(createChartContainer("Action Distribution", actionDistributionChart), 1, 0);
        chartsGrid.add(createChartContainer("Success Rates by Action", successRateChart), 0, 1);
        chartsGrid.add(createChartContainer("Performance Trends", performanceChart), 1, 1);
        
        // Tables section
        HBox tablesRow = new HBox(20);
        tablesRow.getChildren().addAll(
            createTableContainer("Action Statistics", actionStatsTable),
            createTableContainer("Quality Metrics", qualityMetricsTable)
        );
        
        // Add all to content
        content.getChildren().addAll(
            headerLabel,
            new Separator(),
            metricsRow,
            chartsGrid,
            tablesRow
        );
        
        setContent(content);
        setFitToWidth(true);
    }
    
    /**
     * Creates a container for a chart with title.
     */
    private VBox createChartContainer(String title, Region chart) {
        VBox container = new VBox(5);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        container.getChildren().addAll(titleLabel, chart);
        return container;
    }
    
    /**
     * Creates a container for a table with title.
     */
    private VBox createTableContainer(String title, TableView<?> table) {
        VBox container = new VBox(5);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        table.setPrefHeight(200);
        
        container.getChildren().addAll(titleLabel, table);
        return container;
    }
    
    /**
     * Creates the throughput line chart.
     */
    private LineChart<String, Number> createThroughputChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Illustrations/min");
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        
        // Add series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Throughput");
        chart.getData().add(series);
        
        return chart;
    }
    
    /**
     * Creates the action distribution pie chart.
     */
    private PieChart createActionDistributionChart() {
        PieChart chart = new PieChart();
        chart.setAnimated(true);
        chart.setLegendVisible(true);
        return chart;
    }
    
    /**
     * Creates the success rate bar chart.
     */
    private BarChart<String, Number> createSuccessRateChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Action Type");
        
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Success Rate (%)");
        
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setAnimated(true);
        
        // Add series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Success Rate");
        chart.getData().add(series);
        
        return chart;
    }
    
    /**
     * Creates the performance trends line chart.
     */
    private LineChart<String, Number> createPerformanceChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Processing Time (ms)");
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        
        // Add multiple series
        XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
        avgSeries.setName("Average");
        
        XYChart.Series<String, Number> maxSeries = new XYChart.Series<>();
        maxSeries.setName("Maximum");
        
        chart.getData().addAll(avgSeries, maxSeries);
        
        return chart;
    }
    
    /**
     * Creates the action statistics table.
     */
    private TableView<ActionStatistic> createActionStatsTable() {
        TableView<ActionStatistic> table = new TableView<>();
        
        TableColumn<ActionStatistic, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data -> data.getValue().actionProperty());
        
        TableColumn<ActionStatistic, Integer> countCol = new TableColumn<>("Count");
        countCol.setCellValueFactory(data -> data.getValue().countProperty().asObject());
        
        TableColumn<ActionStatistic, String> successRateCol = new TableColumn<>("Success Rate");
        successRateCol.setCellValueFactory(data -> data.getValue().successRateProperty());
        
        TableColumn<ActionStatistic, String> avgTimeCol = new TableColumn<>("Avg Time");
        avgTimeCol.setCellValueFactory(data -> data.getValue().avgTimeProperty());
        
        table.getColumns().addAll(actionCol, countCol, successRateCol, avgTimeCol);
        
        return table;
    }
    
    /**
     * Creates the quality metrics table.
     */
    private TableView<QualityMetric> createQualityMetricsTable() {
        TableView<QualityMetric> table = new TableView<>();
        
        TableColumn<QualityMetric, String> metricCol = new TableColumn<>("Metric");
        metricCol.setCellValueFactory(data -> data.getValue().metricNameProperty());
        
        TableColumn<QualityMetric, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(data -> data.getValue().valueProperty());
        
        TableColumn<QualityMetric, String> trendCol = new TableColumn<>("Trend");
        trendCol.setCellValueFactory(data -> data.getValue().trendProperty());
        
        table.getColumns().addAll(metricCol, valueCol, trendCol);
        
        return table;
    }
    
    /**
     * Starts the periodic update timeline.
     */
    private void startUpdates() {
        updateTimeline = new Timeline(new KeyFrame(
            Duration.seconds(2),
            e -> updateDashboard()
        ));
        updateTimeline.setCycleCount(Animation.INDEFINITE);
        updateTimeline.play();
        
        // Initial update
        updateDashboard();
    }
    
    /**
     * Updates all dashboard components with latest data.
     */
    private void updateDashboard() {
        AnalyticsSnapshot snapshot = analyticsService.getCurrentSnapshot();
        
        // Update metric panels
        updateMetricPanels(snapshot);
        
        // Update charts
        updateThroughputChart(snapshot);
        updateActionDistributionChart(snapshot);
        updateSuccessRateChart(snapshot);
        updatePerformanceChart(snapshot);
        
        // Update tables
        updateActionStatsTable(snapshot);
        updateQualityMetricsTable(snapshot);
    }
    
    /**
     * Updates the metric panels.
     */
    private void updateMetricPanels(AnalyticsSnapshot snapshot) {
        totalIllustrationsPanel.setValue(String.valueOf(snapshot.getTotalIllustrations()));
        successRatePanel.setValue(String.format("%.1f%%", snapshot.getOverallSuccessRate() * 100));
        avgProcessingTimePanel.setValue(String.format("%.0fms", snapshot.getAverageProcessingTime()));
        
        // Queue health based on queue size and processing time
        String queueHealth = "Good";
        Color healthColor = Color.GREEN;
        
        if (snapshot.getCurrentQueueSize() > 20) {
            queueHealth = "High Load";
            healthColor = Color.ORANGE;
        } else if (snapshot.getCurrentQueueSize() > 40) {
            queueHealth = "Critical";
            healthColor = Color.RED;
        }
        
        queueHealthPanel.setValue(queueHealth);
        queueHealthPanel.setColor(healthColor);
    }
    
    /**
     * Updates the throughput chart.
     */
    private void updateThroughputChart(AnalyticsSnapshot snapshot) {
        XYChart.Series<String, Number> series = throughputChart.getData().get(0);
        ObservableList<XYChart.Data<String, Number>> data = series.getData();
        
        // Add new data point
        String timeLabel = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        data.add(new XYChart.Data<>(timeLabel, snapshot.getIllustrationsPerMinute()));
        
        // Keep only last 20 points
        if (data.size() > 20) {
            data.remove(0);
        }
    }
    
    /**
     * Updates the action distribution chart.
     */
    private void updateActionDistributionChart(AnalyticsSnapshot snapshot) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        
        snapshot.getActionCounts().forEach((action, count) -> {
            pieData.add(new PieChart.Data(action, count.get()));
        });
        
        actionDistributionChart.setData(pieData);
    }
    
    /**
     * Updates the success rate chart.
     */
    private void updateSuccessRateChart(AnalyticsSnapshot snapshot) {
        XYChart.Series<String, Number> series = successRateChart.getData().get(0);
        series.getData().clear();
        
        snapshot.getSuccessRatesByAction().forEach((action, rate) -> {
            series.getData().add(new XYChart.Data<>(action, rate * 100));
        });
    }
    
    /**
     * Updates the performance chart.
     */
    private void updatePerformanceChart(AnalyticsSnapshot snapshot) {
        String timeLabel = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        
        // Average series
        XYChart.Series<String, Number> avgSeries = performanceChart.getData().get(0);
        avgSeries.getData().add(new XYChart.Data<>(timeLabel, snapshot.getAverageProcessingTime()));
        
        // Max series
        XYChart.Series<String, Number> maxSeries = performanceChart.getData().get(1);
        maxSeries.getData().add(new XYChart.Data<>(timeLabel, snapshot.getMaxProcessingTime()));
        
        // Keep only last 15 points
        if (avgSeries.getData().size() > 15) {
            avgSeries.getData().remove(0);
            maxSeries.getData().remove(0);
        }
    }
    
    /**
     * Updates the action statistics table.
     */
    private void updateActionStatsTable(AnalyticsSnapshot snapshot) {
        ObservableList<ActionStatistic> items = FXCollections.observableArrayList();
        
        snapshot.getActionCounts().forEach((action, count) -> {
            double successRate = snapshot.getSuccessRatesByAction().getOrDefault(action, 0.0);
            double avgTime = snapshot.getAverageTimeByAction().getOrDefault(action, 0.0);
            
            items.add(new ActionStatistic(action, count.get(), successRate, avgTime));
        });
        
        actionStatsTable.setItems(items);
    }
    
    /**
     * Updates the quality metrics table.
     */
    private void updateQualityMetricsTable(AnalyticsSnapshot snapshot) {
        ObservableList<QualityMetric> items = FXCollections.observableArrayList();
        
        items.add(new QualityMetric("High Quality Rate", 
            String.format("%.1f%%", snapshot.getHighQualityRate() * 100),
            getTrend(snapshot.getHighQualityRate())));
            
        items.add(new QualityMetric("Skip Rate", 
            String.format("%.1f%%", snapshot.getSkipRate() * 100),
            getTrend(1 - snapshot.getSkipRate()))); // Inverse for trend
            
        items.add(new QualityMetric("Batch Efficiency", 
            String.format("%.1f%%", snapshot.getBatchEfficiency() * 100),
            getTrend(snapshot.getBatchEfficiency())));
            
        items.add(new QualityMetric("Sampling Effectiveness", 
            String.format("%.1f%%", snapshot.getSamplingEffectiveness() * 100),
            getTrend(snapshot.getSamplingEffectiveness())));
        
        qualityMetricsTable.setItems(items);
    }
    
    /**
     * Gets trend indicator based on value.
     */
    private String getTrend(double value) {
        if (value > 0.8) return "↑";
        if (value < 0.5) return "↓";
        return "→";
    }
    
    /**
     * Stops dashboard updates.
     */
    public void stop() {
        if (updateTimeline != null) {
            updateTimeline.stop();
        }
    }
    
    /**
     * Metric panel component.
     */
    private static class MetricPanel extends VBox {
        private final Label valueLabel;
        private final Rectangle colorBar;
        
        public MetricPanel(String title, String initialValue, Color color) {
            super(5);
            setAlignment(Pos.CENTER);
            setPadding(new Insets(15));
            setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 5;");
            setPrefWidth(150);
            
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-fg-subtle;");
            
            valueLabel = new Label(initialValue);
            valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            
            colorBar = new Rectangle(100, 3);
            colorBar.setFill(color);
            
            getChildren().addAll(titleLabel, valueLabel, colorBar);
        }
        
        public void setValue(String value) {
            valueLabel.setText(value);
        }
        
        public void setColor(Color color) {
            colorBar.setFill(color);
        }
    }
}