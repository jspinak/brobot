package io.github.jspinak.brobot.debug;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Generates comprehensive debug reports for image finding operations. Creates HTML and JSON reports
 * with detailed analysis and visualizations.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "brobot.debug.image.enabled", havingValue = "true")
public class DebugReportGenerator {

    @Autowired(required = false)
    @Qualifier("brobot.debug.image-io.github.jspinak.brobot.debug.ImageDebugConfig") private ImageDebugConfig config;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Data
    public static class SessionData {
        private String sessionId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<ImageFindDebugger.FindDebugInfo> operations = new ArrayList<>();
        private int totalOperations;
        private int successfulOperations;
        private int failedOperations;
        private double averageDuration;
        private double averageScore;
    }

    /** Add an operation to the current session. */
    public void addOperation(String sessionId, ImageFindDebugger.FindDebugInfo debugInfo) {
        SessionData session =
                sessions.computeIfAbsent(
                        sessionId,
                        k -> {
                            SessionData s = new SessionData();
                            s.sessionId = sessionId;
                            s.startTime = LocalDateTime.now();
                            return s;
                        });

        session.operations.add(debugInfo);
        session.totalOperations++;

        if (debugInfo.isFound()) {
            session.successfulOperations++;
        } else {
            session.failedOperations++;
        }
    }

    /** Generate HTML report for a session. */
    public void generateHtmlReport(String sessionId) {
        SessionData session = sessions.get(sessionId);
        if (session == null) {
            log.warn("No session data found for: {}", sessionId);
            return;
        }

        session.endTime = LocalDateTime.now();
        calculateStatistics(session);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append(
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Brobot Image Find Debug Report</title>\n");
        html.append(generateCss());
        html.append("</head>\n");
        html.append("<body>\n");

        // Header
        html.append("    <div class=\"header\">\n");
        html.append("        <h1>🔍 Brobot Image Find Debug Report</h1>\n");
        html.append("        <div class=\"session-info\">\n");
        html.append("            <span>Session: ").append(sessionId).append("</span>\n");
        html.append("            <span>Generated: ")
                .append(LocalDateTime.now().format(TIMESTAMP_FORMAT))
                .append("</span>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");

        // Summary Statistics
        html.append("    <div class=\"summary\">\n");
        html.append("        <h2>Summary Statistics</h2>\n");
        html.append("        <div class=\"stats-grid\">\n");
        html.append(
                generateStatCard(
                        "Total Operations", String.valueOf(session.totalOperations), "total"));
        html.append(
                generateStatCard(
                        "Successful", String.valueOf(session.successfulOperations), "success"));
        html.append(generateStatCard("Failed", String.valueOf(session.failedOperations), "failed"));
        html.append(
                generateStatCard(
                        "Success Rate",
                        String.format(
                                "%.1f%%",
                                (double) session.successfulOperations
                                        / session.totalOperations
                                        * 100),
                        "rate"));
        html.append(
                generateStatCard(
                        "Avg Duration",
                        String.format("%.0fms", session.averageDuration),
                        "duration"));
        html.append(
                generateStatCard(
                        "Avg Score", String.format("%.1f%%", session.averageScore * 100), "score"));
        html.append("        </div>\n");
        html.append("    </div>\n");

        // Operations Timeline
        html.append("    <div class=\"timeline\">\n");
        html.append("        <h2>Operations Timeline</h2>\n");
        html.append("        <div class=\"operations\">\n");

        for (ImageFindDebugger.FindDebugInfo op : session.operations) {
            html.append(generateOperationCard(op));
        }

        html.append("        </div>\n");
        html.append("    </div>\n");

        // Footer
        html.append("    <div class=\"footer\">\n");
        html.append("        <p>Generated by Brobot Debug System</p>\n");
        html.append("    </div>\n");

        html.append("</body>\n");
        html.append("</html>");

        saveReport(sessionId, "report.html", html.toString());
    }

    /** Generate JSON report for a session. */
    public void generateJsonReport(String sessionId) {
        SessionData session = sessions.get(sessionId);
        if (session == null) {
            log.warn("No session data found for: {}", sessionId);
            return;
        }

        session.endTime = LocalDateTime.now();
        calculateStatistics(session);

        try {
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            }

            String json = objectMapper.writeValueAsString(session);
            saveReport(sessionId, "report.json", json);
        } catch (Exception e) {
            log.error("Failed to generate JSON report", e);
        }
    }

    private void calculateStatistics(SessionData session) {
        if (session.operations.isEmpty()) return;

        double totalDuration = 0;
        double totalScore = 0;
        int scoreCount = 0;

        for (ImageFindDebugger.FindDebugInfo op : session.operations) {
            totalDuration += op.getSearchDuration();
            if (op.getBestScore() > 0) {
                totalScore += op.getBestScore();
                scoreCount++;
            }
        }

        session.averageDuration = totalDuration / session.operations.size();
        session.averageScore = scoreCount > 0 ? totalScore / scoreCount : 0;
    }

    private String generateCss() {
        return """
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
                    line-height: 1.6;
                    color: #333;
                    background: #f5f5f5;
                }
                .header {
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: white;
                    padding: 2rem;
                    text-align: center;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                }
                .header h1 { margin-bottom: 0.5rem; }
                .session-info {
                    display: flex;
                    justify-content: center;
                    gap: 2rem;
                    opacity: 0.9;
                    font-size: 0.9rem;
                }
                .summary {
                    max-width: 1200px;
                    margin: 2rem auto;
                    padding: 0 1rem;
                }
                .summary h2 {
                    margin-bottom: 1rem;
                    color: #444;
                }
                .stats-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
                    gap: 1rem;
                    margin-bottom: 2rem;
                }
                .stat-card {
                    background: white;
                    padding: 1.5rem;
                    border-radius: 8px;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    text-align: center;
                    transition: transform 0.2s;
                }
                .stat-card:hover { transform: translateY(-2px); }
                .stat-card.total { border-left: 4px solid #3b82f6; }
                .stat-card.success { border-left: 4px solid #10b981; }
                .stat-card.failed { border-left: 4px solid #ef4444; }
                .stat-card.rate { border-left: 4px solid #f59e0b; }
                .stat-card.duration { border-left: 4px solid #8b5cf6; }
                .stat-card.score { border-left: 4px solid #ec4899; }
                .stat-label {
                    font-size: 0.875rem;
                    color: #666;
                    margin-bottom: 0.5rem;
                }
                .stat-value {
                    font-size: 2rem;
                    font-weight: bold;
                    color: #333;
                }
                .timeline {
                    max-width: 1200px;
                    margin: 2rem auto;
                    padding: 0 1rem;
                }
                .operations {
                    display: flex;
                    flex-direction: column;
                    gap: 1rem;
                }
                .op-card {
                    background: white;
                    padding: 1.5rem;
                    border-radius: 8px;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    display: grid;
                    grid-template-columns: auto 1fr auto;
                    gap: 1rem;
                    align-items: center;
                }
                .op-card.success { border-left: 4px solid #10b981; }
                .op-card.failed { border-left: 4px solid #ef4444; }
                .op-status {
                    font-size: 2rem;
                    width: 40px;
                    text-align: center;
                }
                .op-details h3 {
                    margin-bottom: 0.25rem;
                    color: #333;
                }
                .op-meta {
                    display: flex;
                    gap: 1rem;
                    font-size: 0.875rem;
                    color: #666;
                }
                .op-metrics {
                    text-align: right;
                    font-size: 0.875rem;
                }
                .op-metrics .score {
                    font-size: 1.25rem;
                    font-weight: bold;
                    color: #333;
                    margin-bottom: 0.25rem;
                }
                .footer {
                    text-align: center;
                    padding: 2rem;
                    color: #666;
                    font-size: 0.875rem;
                }
                .tag {
                    display: inline-block;
                    padding: 0.25rem 0.5rem;
                    background: #e5e7eb;
                    border-radius: 4px;
                    font-size: 0.75rem;
                    margin-left: 0.5rem;
                }
                .tag.matches { background: #dbeafe; color: #1e40af; }
            </style>
            """;
    }

    private String generateStatCard(String label, String value, String type) {
        return String.format(
                """
            <div class="stat-card %s">
                <div class="stat-label">%s</div>
                <div class="stat-value">%s</div>
            </div>
            """,
                type, label, value);
    }

    private String generateOperationCard(ImageFindDebugger.FindDebugInfo op) {
        String statusIcon = op.isFound() ? "✅" : "❌";
        String statusClass = op.isFound() ? "success" : "failed";

        StringBuilder html = new StringBuilder();
        html.append(String.format("<div class=\"op-card %s\">\n", statusClass));
        html.append(String.format("    <div class=\"op-status\">%s</div>\n", statusIcon));
        html.append("    <div class=\"op-details\">\n");
        html.append(
                String.format(
                        "        <h3>%s</h3>\n",
                        op.getPatternName() != null ? op.getPatternName() : "Unknown Pattern"));
        html.append("        <div class=\"op-meta\">\n");
        html.append(
                String.format(
                        "            <span>🕐 %s</span>\n",
                        op.getTimestamp() != null
                                ? op.getTimestamp()
                                        .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
                                : ""));

        if (op.getPatternWidth() > 0) {
            html.append(
                    String.format(
                            "            <span>📐 %dx%d</span>\n",
                            op.getPatternWidth(), op.getPatternHeight()));
        }

        if (op.isFound()) {
            html.append(
                    String.format(
                            "            <span class=\"tag matches\">%d matches</span>\n",
                            op.getMatchCount()));
        } else if (op.getFailureReason() != null) {
            html.append(String.format("            <span>⚠️ %s</span>\n", op.getFailureReason()));
        }

        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("    <div class=\"op-metrics\">\n");

        if (op.getBestScore() > 0) {
            html.append(
                    String.format(
                            "        <div class=\"score\">%.1f%%</div>\n",
                            op.getBestScore() * 100));
        }

        html.append(String.format("        <div>⏱️ %dms</div>\n", op.getSearchDuration()));
        html.append("    </div>\n");
        html.append("</div>\n");

        return html.toString();
    }

    private void saveReport(String sessionId, String filename, String content) {
        try {
            Path outputDir = Paths.get(config.getOutputDir(), sessionId, "reports");
            Files.createDirectories(outputDir);

            Path reportPath = outputDir.resolve(filename);
            Files.write(reportPath, content.getBytes(StandardCharsets.UTF_8));

            log.info("Saved debug report: {}", reportPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save report", e);
        }
    }

    /** Clean up old sessions to prevent memory leaks. */
    public void cleanupOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        sessions.entrySet()
                .removeIf(
                        entry -> {
                            SessionData session = entry.getValue();
                            return session.endTime != null && session.endTime.isBefore(cutoff);
                        });
    }
}
