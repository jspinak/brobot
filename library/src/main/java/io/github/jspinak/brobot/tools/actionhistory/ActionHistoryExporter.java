package io.github.jspinak.brobot.tools.actionhistory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Exports ActionHistory data to various formats for analysis and reporting.
 *
 * <p>This class provides export functionality for ActionHistory data, supporting multiple formats
 * including CSV, HTML, and summary reports. It's useful for analyzing automation performance,
 * debugging issues, and generating documentation.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Export to CSV for spreadsheet analysis
 *   <li>Generate HTML reports with visualizations
 *   <li>Create summary statistics
 *   <li>Export filtered data based on criteria
 *   <li>Batch export multiple histories
 * </ul>
 *
 * @since 1.2.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActionHistoryExporter {

    private static final String DEFAULT_EXPORT_PATH = "reports/action-history";

    /**
     * Export ActionHistory to CSV format.
     *
     * @param history the ActionHistory to export
     * @param filename the output filename
     * @throws IOException if unable to write file
     */
    public void exportToCSV(ActionHistory history, String filename) throws IOException {
        exportToCSV(history, filename, DEFAULT_EXPORT_PATH);
    }

    /**
     * Export ActionHistory to CSV format with custom path.
     *
     * @param history the ActionHistory to export
     * @param filename the output filename
     * @param directory the output directory
     * @throws IOException if unable to write file
     */
    public void exportToCSV(ActionHistory history, String filename, String directory)
            throws IOException {
        Path path = Path.of(directory, filename);
        Files.createDirectories(path.getParent());

        StringBuilder csv = new StringBuilder();

        // CSV Header
        csv.append("Timestamp,Action,Success,Duration(ms),Matches,Text,Details\n");

        // Data rows
        for (ActionRecord record : history.getSnapshots()) {
            csv.append(formatCSVRow(record));
        }

        Files.writeString(path, csv.toString());
        log.info("Exported ActionHistory to CSV: {}", path);
    }

    private String formatCSVRow(ActionRecord record) {
        StringBuilder row = new StringBuilder();

        // Timestamp
        String timestamp =
                record.getTimeStamp() != null
                        ? record.getTimeStamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        : "";
        row.append(timestamp).append(",");

        // Action type
        String actionType =
                record.getActionConfig() != null
                        ? record.getActionConfig().getClass().getSimpleName()
                        : "Unknown";
        row.append(actionType).append(",");

        // Success
        row.append(record.isActionSuccess()).append(",");

        // Duration
        row.append((long) record.getDuration()).append(",");

        // Match count
        row.append(record.getMatchList().size()).append(",");

        // Text (escape commas and quotes)
        String text =
                record.getText() != null
                        ? "\"" + record.getText().replace("\"", "\"\"") + "\""
                        : "";
        row.append(text).append(",");

        // Details (match locations)
        String details =
                record.getMatchList().stream()
                        .map(this::matchToString)
                        .collect(Collectors.joining("; "));
        row.append("\"").append(details).append("\"");

        row.append("\n");
        return row.toString();
    }

    private String matchToString(Match match) {
        return String.format(
                "(%d,%d,%dx%d)",
                match.getRegion().getX(),
                match.getRegion().getY(),
                match.getRegion().getW(),
                match.getRegion().getH());
    }

    /**
     * Export ActionHistory to HTML report.
     *
     * @param history the ActionHistory to export
     * @param filename the output filename
     * @throws IOException if unable to write file
     */
    public void exportToHTML(ActionHistory history, String filename) throws IOException {
        exportToHTML(history, filename, DEFAULT_EXPORT_PATH);
    }

    /**
     * Export ActionHistory to HTML report with custom path.
     *
     * @param history the ActionHistory to export
     * @param filename the output filename
     * @param directory the output directory
     * @throws IOException if unable to write file
     */
    public void exportToHTML(ActionHistory history, String filename, String directory)
            throws IOException {
        Path path = Path.of(directory, filename);
        Files.createDirectories(path.getParent());

        String html = generateHTMLReport(history);
        Files.writeString(path, html);

        log.info("Exported ActionHistory to HTML: {}", path);
    }

    private String generateHTMLReport(ActionHistory history) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<title>Action History Report</title>\n");
        html.append("<style>\n");
        html.append(getHTMLStyles());
        html.append("</style>\n");
        html.append("</head>\n<body>\n");

        // Header
        html.append("<h1>Action History Report</h1>\n");
        html.append("<p>Generated: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .append("</p>\n");

        // Summary statistics
        html.append("<div class='summary'>\n");
        html.append("<h2>Summary Statistics</h2>\n");
        html.append(generateSummaryHTML(history));
        html.append("</div>\n");

        // Action details table
        html.append("<div class='details'>\n");
        html.append("<h2>Action Details</h2>\n");
        html.append("<table>\n");
        html.append("<thead>\n");
        html.append("<tr><th>Time</th><th>Action</th><th>Success</th>");
        html.append("<th>Duration</th><th>Matches</th><th>Text</th></tr>\n");
        html.append("</thead>\n");
        html.append("<tbody>\n");

        for (ActionRecord record : history.getSnapshots()) {
            html.append(formatHTMLRow(record));
        }

        html.append("</tbody>\n");
        html.append("</table>\n");
        html.append("</div>\n");

        html.append("</body>\n</html>");

        return html.toString();
    }

    private String getHTMLStyles() {
        return "body { font-family: Arial, sans-serif; margin: 20px; }\n"
                + "h1 { color: #333; }\n"
                + "h2 { color: #666; }\n"
                + ".summary { background: #f0f0f0; padding: 15px; border-radius: 5px; margin:"
                + " 20px 0; }\n"
                + ".stats { display: flex; gap: 20px; }\n"
                + ".stat { flex: 1; }\n"
                + ".stat-value { font-size: 24px; font-weight: bold; color: #2196F3; }\n"
                + "table { width: 100%; border-collapse: collapse; }\n"
                + "th { background: #2196F3; color: white; padding: 10px; text-align: left; }\n"
                + "td { padding: 8px; border-bottom: 1px solid #ddd; }\n"
                + "tr:hover { background: #f5f5f5; }\n"
                + ".success { color: green; font-weight: bold; }\n"
                + ".failure { color: red; font-weight: bold; }\n";
    }

    private String generateSummaryHTML(ActionHistory history) {
        int totalActions = history.getSnapshots().size();
        long successCount =
                history.getSnapshots().stream().filter(ActionRecord::isActionSuccess).count();
        double successRate = totalActions > 0 ? (double) successCount / totalActions * 100 : 0;

        long totalDuration =
                history.getSnapshots().stream().mapToLong(r -> (long) r.getDuration()).sum();

        long avgDuration = totalActions > 0 ? totalDuration / totalActions : 0;

        StringBuilder summary = new StringBuilder();
        summary.append("<div class='stats'>\n");

        summary.append("<div class='stat'>\n");
        summary.append("<div>Total Actions</div>\n");
        summary.append("<div class='stat-value'>").append(totalActions).append("</div>\n");
        summary.append("</div>\n");

        summary.append("<div class='stat'>\n");
        summary.append("<div>Success Rate</div>\n");
        summary.append("<div class='stat-value'>")
                .append(String.format("%.1f%%", successRate))
                .append("</div>\n");
        summary.append("</div>\n");

        summary.append("<div class='stat'>\n");
        summary.append("<div>Times Found</div>\n");
        summary.append("<div class='stat-value'>")
                .append(history.getTimesFound())
                .append("</div>\n");
        summary.append("</div>\n");

        summary.append("<div class='stat'>\n");
        summary.append("<div>Avg Duration</div>\n");
        summary.append("<div class='stat-value'>").append(avgDuration).append(" ms</div>\n");
        summary.append("</div>\n");

        summary.append("</div>\n");

        return summary.toString();
    }

    private String formatHTMLRow(ActionRecord record) {
        StringBuilder row = new StringBuilder();
        row.append("<tr>\n");

        // Timestamp
        String timestamp =
                record.getTimeStamp() != null
                        ? record.getTimeStamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        : "";
        row.append("<td>").append(timestamp).append("</td>\n");

        // Action type
        String actionType =
                record.getActionConfig() != null
                        ? record.getActionConfig().getClass().getSimpleName().replace("Options", "")
                        : "Unknown";
        row.append("<td>").append(actionType).append("</td>\n");

        // Success
        String successClass = record.isActionSuccess() ? "success" : "failure";
        String successText = record.isActionSuccess() ? "✓" : "✗";
        row.append("<td class='")
                .append(successClass)
                .append("'>")
                .append(successText)
                .append("</td>\n");

        // Duration
        row.append("<td>").append((long) record.getDuration()).append(" ms</td>\n");

        // Matches
        row.append("<td>").append(record.getMatchList().size()).append("</td>\n");

        // Text
        String text = record.getText() != null ? record.getText() : "";
        row.append("<td>").append(escapeHTML(text)).append("</td>\n");

        row.append("</tr>\n");
        return row.toString();
    }

    private String escapeHTML(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Generate a summary report of ActionHistory.
     *
     * @param history the ActionHistory to analyze
     * @return summary statistics as a map
     */
    public Map<String, Object> generateSummary(ActionHistory history) {
        Map<String, Object> summary = new HashMap<>();

        int totalActions = history.getSnapshots().size();
        summary.put("totalActions", totalActions);
        summary.put("timesSearched", history.getTimesSearched());
        summary.put("timesFound", history.getTimesFound());

        if (totalActions > 0) {
            long successCount =
                    history.getSnapshots().stream().filter(ActionRecord::isActionSuccess).count();

            summary.put("successCount", successCount);
            summary.put("failureCount", totalActions - successCount);
            summary.put("successRate", (double) successCount / totalActions * 100);

            // Duration statistics
            LongSummaryStatistics durationStats =
                    history.getSnapshots().stream()
                            .mapToLong(r -> (long) r.getDuration())
                            .summaryStatistics();

            summary.put("totalDuration", durationStats.getSum());
            summary.put("avgDuration", durationStats.getAverage());
            summary.put("minDuration", durationStats.getMin());
            summary.put("maxDuration", durationStats.getMax());

            // Action type breakdown
            Map<String, Long> actionTypes =
                    history.getSnapshots().stream()
                            .filter(r -> r.getActionConfig() != null)
                            .collect(
                                    Collectors.groupingBy(
                                            r -> r.getActionConfig().getClass().getSimpleName(),
                                            Collectors.counting()));
            summary.put("actionTypes", actionTypes);

            // Timestamp range
            if (!history.getSnapshots().isEmpty()) {
                LocalDateTime firstTime = history.getSnapshots().get(0).getTimeStamp();
                LocalDateTime lastTime =
                        history.getSnapshots()
                                .get(history.getSnapshots().size() - 1)
                                .getTimeStamp();
                if (firstTime != null)
                    summary.put(
                            "firstAction", firstTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                if (lastTime != null)
                    summary.put(
                            "lastAction", lastTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        }

        return summary;
    }

    /**
     * Export filtered ActionHistory based on criteria.
     *
     * @param history the ActionHistory to filter
     * @param successOnly include only successful actions
     * @param minDuration minimum duration in milliseconds
     * @param maxDuration maximum duration in milliseconds
     * @return filtered ActionHistory
     */
    public ActionHistory filterHistory(
            ActionHistory history, boolean successOnly, long minDuration, long maxDuration) {
        ActionHistory filtered = new ActionHistory();
        filtered.setTimesSearched(history.getTimesSearched());
        filtered.setTimesFound(history.getTimesFound());

        List<ActionRecord> filteredRecords =
                history.getSnapshots().stream()
                        .filter(r -> !successOnly || r.isActionSuccess())
                        .filter(r -> (long) r.getDuration() >= minDuration)
                        .filter(r -> (long) r.getDuration() <= maxDuration)
                        .collect(Collectors.toList());

        filtered.setSnapshots(filteredRecords);

        log.debug(
                "Filtered {} records to {} records",
                history.getSnapshots().size(),
                filteredRecords.size());

        return filtered;
    }

    /**
     * Batch export multiple histories to a single report.
     *
     * @param histories map of name to ActionHistory
     * @param filename output filename
     * @param format export format (csv or html)
     * @throws IOException if unable to write file
     */
    public void batchExport(
            Map<String, ActionHistory> histories, String filename, ExportFormat format)
            throws IOException {
        switch (format) {
            case CSV:
                batchExportCSV(histories, filename);
                break;
            case HTML:
                batchExportHTML(histories, filename);
                break;
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private void batchExportCSV(Map<String, ActionHistory> histories, String filename)
            throws IOException {
        Path path = Path.of(DEFAULT_EXPORT_PATH, filename);
        Files.createDirectories(path.getParent());

        StringBuilder csv = new StringBuilder();
        csv.append("Source,Timestamp,Action,Success,Duration(ms),Matches,Text\n");

        for (Map.Entry<String, ActionHistory> entry : histories.entrySet()) {
            String source = entry.getKey();
            for (ActionRecord record : entry.getValue().getSnapshots()) {
                csv.append(source).append(",");
                csv.append(formatCSVRow(record));
            }
        }

        Files.writeString(path, csv.toString());
        log.info("Batch exported {} histories to CSV: {}", histories.size(), path);
    }

    private void batchExportHTML(Map<String, ActionHistory> histories, String filename)
            throws IOException {
        // Combine all histories into a single HTML report with sections
        StringBuilder combinedHTML = new StringBuilder();

        combinedHTML.append("<!DOCTYPE html>\n<html>\n<head>\n");
        combinedHTML.append("<title>Batch Action History Report</title>\n");
        combinedHTML.append("<style>\n").append(getHTMLStyles()).append("</style>\n");
        combinedHTML.append("</head>\n<body>\n");

        combinedHTML.append("<h1>Batch Action History Report</h1>\n");
        combinedHTML
                .append("<p>Generated: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .append("</p>\n");
        combinedHTML.append("<p>Total histories: ").append(histories.size()).append("</p>\n");

        for (Map.Entry<String, ActionHistory> entry : histories.entrySet()) {
            combinedHTML.append("<hr>\n");
            combinedHTML.append("<h2>").append(entry.getKey()).append("</h2>\n");
            combinedHTML.append("<div class='summary'>\n");
            combinedHTML.append(generateSummaryHTML(entry.getValue()));
            combinedHTML.append("</div>\n");
        }

        combinedHTML.append("</body>\n</html>");

        Path path = Path.of(DEFAULT_EXPORT_PATH, filename);
        Files.createDirectories(path.getParent());
        Files.writeString(path, combinedHTML.toString());

        log.info("Batch exported {} histories to HTML: {}", histories.size(), path);
    }

    /** Export formats supported by the exporter. */
    public enum ExportFormat {
        CSV,
        HTML
    }
}
