package io.github.jspinak.brobot.runner.examples;

import io.github.jspinak.brobot.runner.ui.UiComponentFactory;
import io.github.jspinak.brobot.runner.ui.monitoring.PerformanceMonitorPanel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Example application demonstrating how to use the PerformanceMonitorPanel
 * during development to monitor UI performance metrics.
 * 
 * This example shows:
 * 1. How to integrate the performance monitor into your application
 * 2. How to add it as a developer tool menu item
 * 3. How to export performance reports
 */
@SpringBootApplication
public class PerformanceMonitoringExample extends Application {
    
    private ConfigurableApplicationContext springContext;
    private UiComponentFactory uiComponentFactory;
    
    @Override
    public void init() {
        springContext = SpringApplication.run(PerformanceMonitoringExample.class);
        uiComponentFactory = springContext.getBean(UiComponentFactory.class);
    }
    
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        
        // Create menu bar with developer tools
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);
        
        // Create main application tabs
        TabPane mainTabs = createMainTabs();
        root.setCenter(mainTabs);
        
        // Create scene
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add("/css/application.css");
        
        primaryStage.setTitle("Brobot Runner - With Performance Monitoring");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> System.exit(0));
        fileMenu.getItems().add(exitItem);
        
        // Developer menu
        Menu devMenu = new Menu("Developer");
        
        MenuItem perfMonitorItem = new MenuItem("Show Performance Monitor");
        perfMonitorItem.setOnAction(e -> showPerformanceMonitor());
        
        MenuItem exportMetricsItem = new MenuItem("Export Performance Report");
        exportMetricsItem.setOnAction(e -> exportPerformanceReport());
        
        devMenu.getItems().addAll(perfMonitorItem, exportMetricsItem);
        
        menuBar.getMenus().addAll(fileMenu, devMenu);
        return menuBar;
    }
    
    private TabPane createMainTabs() {
        TabPane tabPane = new TabPane();
        
        // Create all refactored panels
        var panels = uiComponentFactory.createAllRefactoredPanels();
        
        // Add as tabs
        Tab automationTab = new Tab("Automation", panels.automationPanel());
        Tab resourceTab = new Tab("Resources", panels.resourceMonitorPanel());
        Tab configTab = new Tab("Configuration", panels.configDetailsPanel());
        Tab executionTab = new Tab("Execution", panels.executionDashboardPanel());
        Tab logsTab = new Tab("Logs", panels.logsPanel());
        
        tabPane.getTabs().addAll(automationTab, resourceTab, configTab, executionTab, logsTab);
        
        // Make tabs non-closable
        tabPane.getTabs().forEach(tab -> tab.setClosable(false));
        
        return tabPane;
    }
    
    private void showPerformanceMonitor() {
        // Create performance monitor window
        Stage perfStage = new Stage();
        perfStage.setTitle("Performance Monitor");
        
        PerformanceMonitorPanel perfPanel = uiComponentFactory.createPerformanceMonitorPanel();
        
        Scene scene = new Scene(perfPanel, 800, 600);
        scene.getStylesheets().add("/css/application.css");
        
        perfStage.setScene(scene);
        perfStage.show();
    }
    
    private void exportPerformanceReport() {
        // Get or create performance monitor
        PerformanceMonitorPanel perfPanel = uiComponentFactory.createPerformanceMonitorPanel();
        
        // Export report
        String report = perfPanel.exportMetricsReport();
        
        // In a real application, you would save this to a file
        // For this example, we'll just print it
        System.out.println("\n=== PERFORMANCE REPORT ===\n");
        System.out.println(report);
        System.out.println("\n=== END REPORT ===\n");
        
        // You could also show an alert with save options
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle("Performance Report");
        alert.setHeaderText("Performance report generated");
        alert.setContentText("Report has been printed to console. In production, this would save to a file.");
        alert.showAndWait();
    }
    
    @Override
    public void stop() {
        springContext.close();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Example of programmatically checking performance in code.
     */
    public void checkPerformanceInCode() {
        PerformanceMonitorPanel perfPanel = uiComponentFactory.createPerformanceMonitorPanel();
        String report = perfPanel.exportMetricsReport();
        
        // Parse report for slow tasks
        if (report.contains("Warning:") && report.contains("slow tasks")) {
            System.err.println("Performance warning detected!");
            // Could trigger automated optimization or alerts
        }
    }
}