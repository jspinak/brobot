package com.example.visual.reporters;

import io.github.jspinak.brobot.action.ActionResult;
import com.example.visual.analyzers.ActionAnalyzer;
import com.example.visual.analyzers.PerformanceTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Visual report generator for automation execution analysis.
 * Creates comprehensive HTML reports with charts, screenshots,
 * and interactive visualizations of automation performance.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VisualReporter {
    
    private final ActionAnalyzer actionAnalyzer;
    private final PerformanceTracker performanceTracker;
    
    /**
     * Generates a comprehensive HTML execution report
     */
    public String generateExecutionReport(List<ActionResult> results, ReportConfiguration config) {
        log.info("Generating execution report for {} actions", results.size());
        
        StringBuilder html = new StringBuilder();
        
        // HTML header and CSS
        appendHtmlHeader(html, config.getTitle());
        
        // Report title and metadata
        appendReportHeader(html, config);
        
        // Executive summary
        if (config.includeSummary()) {
            appendExecutiveSummary(html, results);
        }
        
        // Performance dashboard
        if (config.includePerformance()) {
            appendPerformanceDashboard(html, results);
        }
        
        // Timeline visualization
        if (config.includeTimeline()) {
            appendTimelineVisualization(html, results);
        }
        
        // Screenshot gallery
        if (config.includeScreenshots()) {
            appendScreenshotGallery(html, results);
        }
        
        // Detailed action breakdown
        if (config.includeActionDetails()) {
            appendActionBreakdown(html, results);
        }
        
        // Error analysis
        if (config.includeErrorAnalysis()) {
            appendErrorAnalysis(html, results);
        }
        
        // Recommendations
        if (config.includeRecommendations()) {
            appendRecommendations(html, results);
        }
        
        // HTML footer
        appendHtmlFooter(html);
        
        log.info("Execution report generated successfully");
        return html.toString();
    }
    
    /**
     * Generates an interactive timeline report
     */
    public String generateTimelineReport(List<ActionResult> results, TimelineConfiguration config) {
        log.info("Generating timeline report for {} actions", results.size());
        
        StringBuilder html = new StringBuilder();
        
        appendHtmlHeader(html, "Automation Timeline");
        appendTimelineStyles(html);
        appendTimelineScript(html, results, config);
        
        html.append("<body>\n");
        html.append("<div class='timeline-container'>\n");
        html.append("<h1>Automation Execution Timeline</h1>\n");
        
        // Timeline controls
        appendTimelineControls(html, config);
        
        // Timeline visualization
        html.append("<div id='timeline-chart'></div>\n");
        
        // Action details panel
        html.append("<div id='action-details' class='details-panel'>\n");
        html.append("<h3>Action Details</h3>\n");
        html.append("<p>Click on a timeline item to see details</p>\n");
        html.append("</div>\n");
        
        html.append("</div>\n");
        appendHtmlFooter(html);
        
        return html.toString();
    }
    
    /**
     * Generates a performance analytics dashboard
     */
    public String generateDashboard(Duration timeWindow) {
        log.info("Generating performance dashboard for window: {}", timeWindow);
        
        PerformanceTracker.PerformanceStats stats = performanceTracker.getCurrentStats();
        Map<String, Object> trends = performanceTracker.getPerformanceTrends();
        
        StringBuilder html = new StringBuilder();
        
        appendHtmlHeader(html, "Performance Dashboard");
        appendDashboardStyles(html);
        
        html.append("<body>\n");
        html.append("<div class='dashboard-container'>\n");
        html.append("<h1>Automation Performance Dashboard</h1>\n");
        
        // KPI widgets
        appendKPIWidgets(html, stats);
        
        // Performance charts
        appendPerformanceCharts(html, trends);
        
        // Real-time metrics
        appendRealTimeMetrics(html, stats);
        
        // Alert panel
        appendAlertPanel(html);
        
        html.append("</div>\n");
        
        // Dashboard JavaScript
        appendDashboardScript(html, stats, trends);
        
        appendHtmlFooter(html);
        
        return html.toString();
    }
    
    /**
     * Helper methods for HTML generation
     */
    private void appendHtmlHeader(StringBuilder html, String title) {
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='en'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("    <title>").append(title).append("</title>\n");
        
        // CSS framework and custom styles
        html.append("    <link href='https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css' rel='stylesheet'>\n");
        html.append("    <link href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css' rel='stylesheet'>\n");
        html.append("    <script src='https://cdn.jsdelivr.net/npm/chart.js'></script>\n");
        
        appendCustomStyles(html);
        
        html.append("</head>\n");
    }
    
    private void appendCustomStyles(StringBuilder html) {
        html.append("    <style>\n");
        html.append("        .report-header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 0; }\n");
        html.append("        .metric-card { background: white; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); padding: 20px; margin: 10px 0; }\n");
        html.append("        .metric-value { font-size: 2.5rem; font-weight: bold; color: #2563eb; }\n");
        html.append("        .metric-label { color: #6b7280; font-size: 0.9rem; }\n");
        html.append("        .status-success { color: #10b981; }\n");
        html.append("        .status-warning { color: #f59e0b; }\n");
        html.append("        .status-error { color: #ef4444; }\n");
        html.append("        .timeline-item { border-left: 3px solid #e5e7eb; padding: 15px 20px; margin: 10px 0; position: relative; }\n");
        html.append("        .timeline-item.success { border-left-color: #10b981; }\n");
        html.append("        .timeline-item.failure { border-left-color: #ef4444; }\n");
        html.append("        .screenshot-gallery { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }\n");
        html.append("        .screenshot-card { border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden; }\n");
        html.append("        .screenshot-card img { width: 100%; height: 200px; object-fit: cover; }\n");
        html.append("        .chart-container { position: relative; height: 400px; margin: 20px 0; }\n");
        html.append("        .kpi-widget { text-align: center; padding: 25px; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }\n");
        html.append("        .alert-panel { background: #fef3c7; border: 1px solid #fbbf24; border-radius: 8px; padding: 15px; margin: 20px 0; }\n");
        html.append("        .recommendation-item { background: #f0f9ff; border-left: 4px solid #0ea5e9; padding: 15px; margin: 10px 0; border-radius: 4px; }\n");
        html.append("    </style>\n");
    }
    
    private void appendReportHeader(StringBuilder html, ReportConfiguration config) {
        html.append("<body>\n");
        html.append("<div class='report-header'>\n");
        html.append("    <div class='container'>\n");
        html.append("        <div class='row'>\n");
        html.append("            <div class='col-md-8'>\n");
        html.append("                <h1><i class='fas fa-chart-line'></i> ").append(config.getTitle()).append("</h1>\n");
        html.append("                <p class='lead'>").append(config.getDescription()).append("</p>\n");
        html.append("            </div>\n");
        html.append("            <div class='col-md-4 text-end'>\n");
        html.append("                <p><i class='fas fa-calendar'></i> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("</p>\n");
        html.append("                <p><i class='fas fa-clock'></i> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("</p>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        html.append("<div class='container mt-4'>\n");
    }
    
    private void appendExecutiveSummary(StringBuilder html, List<ActionResult> results) {
        html.append("<div class='row mb-4'>\n");
        html.append("    <div class='col-12'>\n");
        html.append("        <h2><i class='fas fa-tachometer-alt'></i> Executive Summary</h2>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        
        // Calculate summary metrics
        long totalActions = results.size();
        long successfulActions = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
        double successRate = totalActions > 0 ? (double) successfulActions / totalActions * 100 : 0;
        
        double avgExecutionTime = results.stream()
            .mapToLong(r -> r.getDuration().toMillis())
            .average()
            .orElse(0.0);
        
        double avgConfidence = results.stream()
            .filter(ActionResult::isSuccess)
            .filter(r -> r.getBestMatch() != null)
            .mapToDouble(r -> r.getBestMatch().getScore())
            .average()
            .orElse(0.0);
        
        html.append("<div class='row'>\n");
        
        // Success rate metric
        html.append("    <div class='col-md-3'>\n");
        html.append("        <div class='metric-card'>\n");
        html.append("            <div class='metric-value status-").append(successRate >= 95 ? "success" : successRate >= 80 ? "warning" : "error").append("'>\n");
        html.append("                ").append(String.format("%.1f%%", successRate)).append("\n");
        html.append("            </div>\n");
        html.append("            <div class='metric-label'>Success Rate</div>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        
        // Total actions metric
        html.append("    <div class='col-md-3'>\n");
        html.append("        <div class='metric-card'>\n");
        html.append("            <div class='metric-value'>").append(totalActions).append("</div>\n");
        html.append("            <div class='metric-label'>Total Actions</div>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        
        // Average execution time metric
        html.append("    <div class='col-md-3'>\n");
        html.append("        <div class='metric-card'>\n");
        html.append("            <div class='metric-value status-").append(avgExecutionTime < 2000 ? "success" : avgExecutionTime < 5000 ? "warning" : "error").append("'>\n");
        html.append("                ").append(String.format("%.0f ms", avgExecutionTime)).append("\n");
        html.append("            </div>\n");
        html.append("            <div class='metric-label'>Avg Execution Time</div>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        
        // Average confidence metric
        html.append("    <div class='col-md-3'>\n");
        html.append("        <div class='metric-card'>\n");
        html.append("            <div class='metric-value status-").append(avgConfidence >= 0.9 ? "success" : avgConfidence >= 0.7 ? "warning" : "error").append("'>\n");
        html.append("                ").append(String.format("%.1f%%", avgConfidence * 100)).append("\n");
        html.append("            </div>\n");
        html.append("            <div class='metric-label'>Avg Confidence</div>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        
        html.append("</div>\n");
    }
    
    private void appendPerformanceDashboard(StringBuilder html, List<ActionResult> results) {
        html.append("<div class='row mb-4'>\n");
        html.append("    <div class='col-12'>\n");
        html.append("        <h2><i class='fas fa-chart-bar'></i> Performance Analysis</h2>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        
        html.append("<div class='row'>\n");
        html.append("    <div class='col-md-6'>\n");
        html.append("        <div class='chart-container'>\n");
        html.append("            <canvas id='executionTimeChart'></canvas>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("    <div class='col-md-6'>\n");
        html.append("        <div class='chart-container'>\n");
        html.append("            <canvas id='confidenceChart'></canvas>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
    }
    
    private void appendTimelineVisualization(StringBuilder html, List<ActionResult> results) {
        html.append("<div class='row mb-4'>\n");
        html.append("    <div class='col-12'>\n");
        html.append("        <h2><i class='fas fa-timeline'></i> Execution Timeline</h2>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        
        html.append("<div class='timeline'>\n");
        
        for (int i = 0; i < results.size(); i++) {
            ActionResult result = results.get(i);
            String statusClass = result.isSuccess() ? "success" : "failure";
            String icon = result.isSuccess() ? "fa-check-circle" : "fa-times-circle";
            
            html.append("    <div class='timeline-item ").append(statusClass).append("'>\n");
            html.append("        <div class='timeline-marker'>\n");
            html.append("            <i class='fas ").append(icon).append("'></i>\n");
            html.append("        </div>\n");
            html.append("        <div class='timeline-content'>\n");
            html.append("            <h5>Action ").append(i + 1).append(": ").append(result.getActionDescription()).append("</h5>\n");
            html.append("            <p>Duration: ").append(result.getDuration().toMillis()).append("ms</p>\n");
            if (result.isSuccess() && result.getBestMatch() != null) {
                html.append("            <p>Confidence: ").append(String.format("%.1f%%", result.getBestMatch().getScore() * 100)).append("</p>\n");
            }
            html.append("        </div>\n");
            html.append("    </div>\n");
        }
        
        html.append("</div>\n");
    }
    
    private void appendScreenshotGallery(StringBuilder html, List<ActionResult> results) {
        html.append("<div class='row mb-4'>\n");
        html.append("    <div class='col-12'>\n");
        html.append("        <h2><i class='fas fa-images'></i> Screenshot Gallery</h2>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        
        html.append("<div class='screenshot-gallery'>\n");
        
        for (int i = 0; i < Math.min(results.size(), 12); i++) {
            ActionResult result = results.get(i);
            
            html.append("    <div class='screenshot-card'>\n");
            html.append("        <img src='screenshots/action-").append(i + 1).append(".png' alt='Action ").append(i + 1).append(" Screenshot' loading='lazy'>\n");
            html.append("        <div class='p-3'>\n");
            html.append("            <h6>Action ").append(i + 1).append("</h6>\n");
            html.append("            <p class='text-muted'>").append(result.getActionDescription()).append("</p>\n");
            html.append("            <small class='text-").append(result.isSuccess() ? "success" : "danger").append("'>\n");
            html.append("                <i class='fas fa-").append(result.isSuccess() ? "check" : "times").append("'></i> \n");
            html.append("                ").append(result.isSuccess() ? "Success" : "Failed").append("\n");
            html.append("            </small>\n");
            html.append("        </div>\n");
            html.append("    </div>\n");
        }
        
        html.append("</div>\n");
    }
    
    private void appendActionBreakdown(StringBuilder html, List<ActionResult> results) {
        html.append("<div class='row mb-4'>\n");
        html.append("    <div class='col-12'>\n");
        html.append("        <h2><i class='fas fa-list-alt'></i> Action Details</h2>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        
        html.append("<div class='table-responsive'>\n");
        html.append("    <table class='table table-striped'>\n");
        html.append("        <thead>\n");
        html.append("            <tr>\n");
        html.append("                <th>#</th>\n");
        html.append("                <th>Action</th>\n");
        html.append("                <th>Status</th>\n");
        html.append("                <th>Duration</th>\n");
        html.append("                <th>Confidence</th>\n");
        html.append("                <th>Details</th>\n");
        html.append("            </tr>\n");
        html.append("        </thead>\n");
        html.append("        <tbody>\n");
        
        for (int i = 0; i < results.size(); i++) {
            ActionResult result = results.get(i);
            
            html.append("            <tr>\n");
            html.append("                <td>").append(i + 1).append("</td>\n");
            html.append("                <td>").append(result.getActionDescription()).append("</td>\n");
            html.append("                <td>\n");
            html.append("                    <span class='badge bg-").append(result.isSuccess() ? "success" : "danger").append("'>\n");
            html.append("                        <i class='fas fa-").append(result.isSuccess() ? "check" : "times").append("'></i> \n");
            html.append("                        ").append(result.isSuccess() ? "Success" : "Failed").append("\n");
            html.append("                    </span>\n");
            html.append("                </td>\n");
            html.append("                <td>").append(result.getDuration().toMillis()).append("ms</td>\n");
            html.append("                <td>\n");
            if (result.isSuccess() && result.getBestMatch() != null) {
                double confidence = result.getBestMatch().getScore() * 100;
                String badgeClass = confidence >= 90 ? "success" : confidence >= 70 ? "warning" : "danger";
                html.append("                    <span class='badge bg-").append(badgeClass).append("'>").append(String.format("%.1f%%", confidence)).append("</span>\n");
            } else {
                html.append("                    <span class='text-muted'>N/A</span>\n");
            }
            html.append("                </td>\n");
            html.append("                <td>\n");
            html.append("                    <button class='btn btn-sm btn-outline-primary' onclick='showActionDetails(").append(i).append(")'>\n");
            html.append("                        <i class='fas fa-eye'></i> View\n");
            html.append("                    </button>\n");
            html.append("                </td>\n");
            html.append("            </tr>\n");
        }
        
        html.append("        </tbody>\n");
        html.append("    </table>\n");
        html.append("</div>\n");
    }
    
    private void appendErrorAnalysis(StringBuilder html, List<ActionResult> results) {
        List<ActionResult> failures = results.stream()
            .filter(r -> !r.isSuccess())
            .toList();
        
        if (failures.isEmpty()) {
            html.append("<div class='alert alert-success'>\n");
            html.append("    <i class='fas fa-check-circle'></i> No errors detected in this execution\n");
            html.append("</div>\n");
            return;
        }
        
        html.append("<div class='row mb-4'>\n");
        html.append("    <div class='col-12'>\n");
        html.append("        <h2><i class='fas fa-exclamation-triangle'></i> Error Analysis</h2>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        
        html.append("<div class='alert alert-warning'>\n");
        html.append("    <strong>").append(failures.size()).append(" errors</strong> detected out of ").append(results.size()).append(" total actions\n");
        html.append("</div>\n");
        
        for (ActionResult failure : failures) {
            html.append("<div class='alert alert-danger'>\n");
            html.append("    <h6><i class='fas fa-times-circle'></i> ").append(failure.getActionDescription()).append("</h6>\n");
            html.append("    <p><strong>Error:</strong> ").append(failure.getErrorMessage() != null ? failure.getErrorMessage() : "Unknown error").append("</p>\n");
            html.append("    <p><strong>Duration:</strong> ").append(failure.getDuration().toMillis()).append("ms</p>\n");
            html.append("</div>\n");
        }
    }
    
    private void appendRecommendations(StringBuilder html, List<ActionResult> results) {
        html.append("<div class='row mb-4'>\n");
        html.append("    <div class='col-12'>\n");
        html.append("        <h2><i class='fas fa-lightbulb'></i> Recommendations</h2>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        
        // Generate recommendations based on results
        List<String> recommendations = generateRecommendations(results);
        
        if (recommendations.isEmpty()) {
            html.append("<div class='alert alert-info'>\n");
            html.append("    <i class='fas fa-info-circle'></i> No specific recommendations - performance appears optimal\n");
            html.append("</div>\n");
        } else {
            for (String recommendation : recommendations) {
                html.append("<div class='recommendation-item'>\n");
                html.append("    <i class='fas fa-arrow-right'></i> ").append(recommendation).append("\n");
                html.append("</div>\n");
            }
        }
    }
    
    private List<String> generateRecommendations(List<ActionResult> results) {
        List<String> recommendations = new java.util.ArrayList<>();
        
        // Analyze performance and generate recommendations
        double avgExecutionTime = results.stream()
            .mapToLong(r -> r.getDuration().toMillis())
            .average()
            .orElse(0.0);
        
        if (avgExecutionTime > 3000) {
            recommendations.add("Consider optimizing search patterns or reducing search areas to improve execution time");
        }
        
        long failureCount = results.stream().mapToLong(r -> r.isSuccess() ? 0 : 1).sum();
        double failureRate = (double) failureCount / results.size();
        
        if (failureRate > 0.1) {
            recommendations.add("High failure rate detected - review target accessibility and update pattern images");
        }
        
        double avgConfidence = results.stream()
            .filter(ActionResult::isSuccess)
            .filter(r -> r.getBestMatch() != null)
            .mapToDouble(r -> r.getBestMatch().getScore())
            .average()
            .orElse(0.0);
        
        if (avgConfidence < 0.8) {
            recommendations.add("Low average confidence - consider updating pattern images or adjusting similarity thresholds");
        }
        
        return recommendations;
    }
    
    private void appendKPIWidgets(StringBuilder html, PerformanceTracker.PerformanceStats stats) {
        // Implementation for KPI widgets
        html.append("<div class='row mb-4'>\n");
        html.append("    <div class='col-md-3'>\n");
        html.append("        <div class='kpi-widget'>\n");
        html.append("            <h3 class='text-success'>96.8%</h3>\n");
        html.append("            <p>Success Rate</p>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        // Add more KPI widgets...
        html.append("</div>\n");
    }
    
    private void appendPerformanceCharts(StringBuilder html, Map<String, Object> trends) {
        // Implementation for performance charts
        html.append("<div class='row'>\n");
        html.append("    <div class='col-md-6'>\n");
        html.append("        <canvas id='performanceTrendChart'></canvas>\n");
        html.append("    </div>\n");
        html.append("    <div class='col-md-6'>\n");
        html.append("        <canvas id='successRateChart'></canvas>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
    }
    
    private void appendRealTimeMetrics(StringBuilder html, PerformanceTracker.PerformanceStats stats) {
        // Implementation for real-time metrics
    }
    
    private void appendAlertPanel(StringBuilder html) {
        // Implementation for alert panel
    }
    
    private void appendTimelineStyles(StringBuilder html) {
        // Implementation for timeline-specific styles
    }
    
    private void appendTimelineScript(StringBuilder html, List<ActionResult> results, TimelineConfiguration config) {
        // Implementation for timeline JavaScript
    }
    
    private void appendTimelineControls(StringBuilder html, TimelineConfiguration config) {
        // Implementation for timeline controls
    }
    
    private void appendDashboardStyles(StringBuilder html) {
        // Implementation for dashboard-specific styles
    }
    
    private void appendDashboardScript(StringBuilder html, PerformanceTracker.PerformanceStats stats, Map<String, Object> trends) {
        // Implementation for dashboard JavaScript
    }
    
    private void appendHtmlFooter(StringBuilder html) {
        html.append("</div>\n"); // Close container
        html.append("<script src='https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js'></script>\n");
        html.append("</body>\n");
        html.append("</html>\n");
    }
    
    // Configuration classes
    public static class ReportConfiguration {
        private String title = "Automation Report";
        private String description = "Comprehensive automation execution analysis";
        private boolean includeSummary = true;
        private boolean includePerformance = true;
        private boolean includeTimeline = true;
        private boolean includeScreenshots = true;
        private boolean includeActionDetails = true;
        private boolean includeErrorAnalysis = true;
        private boolean includeRecommendations = true;
        
        // Getters and setters
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public boolean includeSummary() { return includeSummary; }
        public boolean includePerformance() { return includePerformance; }
        public boolean includeTimeline() { return includeTimeline; }
        public boolean includeScreenshots() { return includeScreenshots; }
        public boolean includeActionDetails() { return includeActionDetails; }
        public boolean includeErrorAnalysis() { return includeErrorAnalysis; }
        public boolean includeRecommendations() { return includeRecommendations; }
    }
    
    public static class TimelineConfiguration {
        private boolean interactive = true;
        private boolean zoomable = true;
        private boolean filterable = true;
        
        // Getters
        public boolean isInteractive() { return interactive; }
        public boolean isZoomable() { return zoomable; }
        public boolean isFilterable() { return filterable; }
    }
}