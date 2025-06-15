package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.cache.CacheManager;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.session.SessionManager;
import io.github.jspinak.brobot.runner.session.SessionSummary;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ResourceMonitorPanel extends BorderPane {

    private final ResourceManager resourceManager;
    private final ImageResourceManager imageResourceManager;
    private final CacheManager cacheManager;
    private final SessionManager sessionManager;

    private Label totalResourcesLabel;
    private Label imageResourcesLabel;
    private Label matResourcesLabel;
    private Label memoryCachedLabel;
    private Label cacheStatsLabel;
    private Label sessionStatusLabel;

    private TableView<SessionSummary> sessionsTable;
    private ComboBox<String> cacheSelector;
    private LineChart<Number, Number> memoryChart;
    private XYChart.Series<Number, Number> memorySeries;
    private AtomicInteger timeCounter = new AtomicInteger(0);

    private ScheduledExecutorService refreshExecutor;

    @Autowired
    public ResourceMonitorPanel(ResourceManager resourceManager,
                                ImageResourceManager imageResourceManager,
                                CacheManager cacheManager,
                                SessionManager sessionManager) {
        this.resourceManager = resourceManager;
        this.imageResourceManager = imageResourceManager;
        this.cacheManager = cacheManager;
        this.sessionManager = sessionManager;

        setupUI();
    }

    @PostConstruct
    public void initialize() {
        startRefreshTask();
    }

    private void setupUI() {
        setPadding(new Insets(20));

        // Top section - Resource summary
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(0, 0, 20, 0));

        Label titleLabel = new Label("Resource Monitor");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        GridPane resourceGrid = new GridPane();
        resourceGrid.setHgap(10);
        resourceGrid.setVgap(5);

        resourceGrid.add(new Label("Total Managed Resources:"), 0, 0);
        totalResourcesLabel = new Label("0");
        resourceGrid.add(totalResourcesLabel, 1, 0);

        resourceGrid.add(new Label("Cached Images:"), 0, 1);
        imageResourcesLabel = new Label("0");
        resourceGrid.add(imageResourcesLabel, 1, 1);

        resourceGrid.add(new Label("Active OpenCV Mats:"), 0, 2);
        matResourcesLabel = new Label("0");
        resourceGrid.add(matResourcesLabel, 1, 2);

        resourceGrid.add(new Label("Memory Cached:"), 0, 3);
        memoryCachedLabel = new Label("0 MB");
        resourceGrid.add(memoryCachedLabel, 1, 3);

        // Memory usage chart
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (30s intervals)");
        yAxis.setLabel("Memory Usage (MB)");

        memoryChart = new LineChart<>(xAxis, yAxis);
        memoryChart.setTitle("Memory Usage");
        memoryChart.setAnimated(false);
        memoryChart.setPrefHeight(200);

        memorySeries = new XYChart.Series<>();
        memorySeries.setName("Cached Memory");
        memoryChart.getData().add(memorySeries);

        topSection.getChildren().addAll(titleLabel, resourceGrid, memoryChart);

        // Middle section - Cache management
        VBox middleSection = new VBox(10);
        middleSection.setPadding(new Insets(0, 0, 20, 0));

        Label cacheLabel = new Label("Cache Management");
        cacheLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        HBox cacheControls = new HBox(10);
        cacheSelector = new ComboBox<>();
        cacheSelector.getItems().addAll("All Caches", "State", "StateImage", "Pattern", "Matches");
        cacheSelector.setValue("All Caches");

        Button clearCacheButton = new Button("Clear Cache");
        clearCacheButton.setOnAction(e -> clearSelectedCache());

        cacheStatsLabel = new Label("No cache statistics available");

        cacheControls.getChildren().addAll(cacheSelector, clearCacheButton);
        middleSection.getChildren().addAll(cacheLabel, cacheControls, cacheStatsLabel);

        // Bottom section - Session management
        VBox bottomSection = new VBox(10);

        Label sessionsLabel = new Label("Session Management");
        sessionsLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        sessionStatusLabel = new Label("No active session");

        // Sessions table
        sessionsTable = new TableView<>();
        sessionsTable.setPrefHeight(200);

        TableColumn<SessionSummary, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getId().substring(0, 8) + "..."
        ));

        TableColumn<SessionSummary, String> projectColumn = new TableColumn<>("Project");
        projectColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getProjectName()
        ));

        TableColumn<SessionSummary, String> startTimeColumn = new TableColumn<>("Start Time");
        startTimeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStartTime() != null ?
                        data.getValue().getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) :
                        "Unknown"
        ));

        TableColumn<SessionSummary, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getFormattedDuration()
        ));

        TableColumn<SessionSummary, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus()
        ));

        sessionsTable.getColumns().addAll(idColumn, projectColumn, startTimeColumn, durationColumn, statusColumn);

        HBox sessionButtons = new HBox(10);
        Button refreshSessionsButton = new Button("Refresh");
        refreshSessionsButton.setOnAction(e -> refreshSessions());

        Button restoreSessionButton = new Button("Restore Selected");
        restoreSessionButton.setOnAction(e -> restoreSelectedSession());
        restoreSessionButton.disableProperty().bind(sessionsTable.getSelectionModel().selectedItemProperty().isNull());

        Button deleteSessionButton = new Button("Delete Selected");
        deleteSessionButton.setOnAction(e -> deleteSelectedSession());
        deleteSessionButton.disableProperty().bind(sessionsTable.getSelectionModel().selectedItemProperty().isNull());

        sessionButtons.getChildren().addAll(refreshSessionsButton, restoreSessionButton, deleteSessionButton);

        bottomSection.getChildren().addAll(sessionsLabel, sessionStatusLabel, sessionsTable, sessionButtons);

        // Layout
        VBox mainContent = new VBox(20);
        mainContent.getChildren().addAll(topSection, middleSection, bottomSection);
        setCenter(mainContent);

        // Initial data load
        refreshSessions();
    }

    private void startRefreshTask() {
        refreshExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        refreshExecutor.scheduleAtFixedRate(this::refreshData, 0, 30, TimeUnit.SECONDS);
    }

    public void stopRefreshTask() {
        if (refreshExecutor != null) {
            refreshExecutor.shutdown();
        }
    }

    private void refreshData() {
        Platform.runLater(() -> {
            // Update resource labels
            totalResourcesLabel.setText(String.valueOf(resourceManager.getResourceCount()));
            imageResourcesLabel.setText(String.valueOf(imageResourceManager.getCachedImageCount()));
            matResourcesLabel.setText(String.valueOf(imageResourceManager.getActiveMatCount()));

            long memoryCached = imageResourceManager.getCachedMemoryUsage();
            memoryCachedLabel.setText(String.format("%.2f MB", memoryCached / 1024.0 / 1024.0));

            // Update memory chart
            int time = timeCounter.getAndIncrement();
            memorySeries.getData().add(new XYChart.Data<>(time, memoryCached / 1024.0 / 1024.0));

            // Keep only the last 20 points
            if (memorySeries.getData().size() > 20) {
                memorySeries.getData().remove(0);
            }

            // Update cache stats
            updateCacheStats();

            // Update session status
            if (sessionManager.isSessionActive()) {
                String sessionId = sessionManager.getCurrentSession().getId();
                LocalDateTime lastAutosave = sessionManager.getLastAutosaveTime();
                String lastAutosaveStr = lastAutosave != null ?
                        lastAutosave.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "never";

                sessionStatusLabel.setText(String.format(
                        "Active session: %s (last autosaved: %s)",
                        sessionId.substring(0, 8) + "...",
                        lastAutosaveStr
                ));
            } else {
                sessionStatusLabel.setText("No active session");
            }
        });
    }

    private void updateCacheStats() {
        Map<String, Map<String, Long>> allStats = cacheManager.getAllCacheStats();

        StringBuilder statsText = new StringBuilder();
        String selectedCache = cacheSelector.getValue();

        if ("All Caches".equals(selectedCache)) {
            for (Map.Entry<String, Map<String, Long>> entry : allStats.entrySet()) {
                Map<String, Long> stats = entry.getValue();
                statsText.append(entry.getKey()).append(": ");
                statsText.append(stats.get("size")).append("/").append(stats.get("maxSize"));
                statsText.append(" items, ");
                statsText.append(stats.get("hitRatio")).append("% hit ratio\n");
            }
        } else {
            Map<String, Long> stats = allStats.get(selectedCache.toLowerCase());
            if (stats != null) {
                statsText.append("Size: ").append(stats.get("size")).append("/").append(stats.get("maxSize")).append("\n");
                statsText.append("Hits: ").append(stats.get("hits")).append("\n");
                statsText.append("Misses: ").append(stats.get("misses")).append("\n");
                statsText.append("Hit Ratio: ").append(stats.get("hitRatio")).append("%\n");
                statsText.append("Puts: ").append(stats.get("puts"));
            } else {
                statsText.append("No statistics available for ").append(selectedCache);
            }
        }

        cacheStatsLabel.setText(statsText.toString());
    }

    private void clearSelectedCache() {
        String selectedCache = cacheSelector.getValue();

        if ("All Caches".equals(selectedCache)) {
            cacheManager.clearAllCaches();
        } else {
            switch (selectedCache) {
                case "State":
                    cacheManager.getStateCache().invalidateAll();
                    break;
                case "StateImage":
                    cacheManager.getStateImageCache().invalidateAll();
                    break;
                case "Pattern":
                    cacheManager.getPatternCache().invalidateAll();
                    break;
                case "Matches":
                    cacheManager.getMatchesCache().invalidateAll();
                    break;
            }
        }

        updateCacheStats();
    }

    private void refreshSessions() {
        List<SessionSummary> sessions = sessionManager.getAllSessionSummaries();
        sessionsTable.getItems().clear();
        sessionsTable.getItems().addAll(sessions);
    }

    private void restoreSelectedSession() {
        SessionSummary selected = sessionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Restore Session");
            alert.setHeaderText("Restore session " + selected.getId() + "?");
            alert.setContentText("This will end the current session and restore the selected one.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = sessionManager.restoreSession(selected.getId());
                    if (success) {
                        showInfoDialog("Session Restored",
                                "Session " + selected.getId() + " has been restored successfully.");
                        refreshSessions();
                        refreshData();
                    } else {
                        showErrorDialog("Restore Failed",
                                "Failed to restore session " + selected.getId());
                    }
                }
            });
        }
    }

    private void deleteSelectedSession() {
        SessionSummary selected = sessionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Session");
            alert.setHeaderText("Delete session " + selected.getId() + "?");
            alert.setContentText("This action cannot be undone.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = sessionManager.deleteSession(selected.getId());
                    if (success) {
                        showInfoDialog("Session Deleted",
                                "Session " + selected.getId() + " has been deleted.");
                        refreshSessions();
                    } else {
                        showErrorDialog("Delete Failed",
                                "Failed to delete session " + selected.getId());
                    }
                }
            });
        }
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}