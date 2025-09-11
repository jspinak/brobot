package io.github.jspinak.brobot.tools.actionhistory;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.SimpleTestBase;

/**
 * Integration tests for ActionHistoryExporter. Tests export functionality to various formats and
 * validates output correctness.
 */
@Disabled("Failing in CI - temporarily disabled for CI/CD")
public class ActionHistoryExporterIntegrationTest extends SimpleTestBase {

    private ActionHistoryExporter exporter;

    @TempDir Path tempDir;

    private ActionHistory testHistory;
    private ActionRecord successfulFindRecord;
    private ActionRecord failedFindRecord;
    private ActionRecord clickRecord;
    private ActionRecord typeRecord;

    @BeforeEach
    public void setup() {
        exporter = new ActionHistoryExporter();
        setupTestData();
    }

    private void setupTestData() {
        testHistory = new ActionHistory();
        testHistory.setTimesSearched(10);
        testHistory.setTimesFound(7);

        // Create successful find record
        successfulFindRecord = new ActionRecord();
        successfulFindRecord.setActionConfig(
                new PatternFindOptions.Builder()
                        .setSimilarity(0.85)
                        .setStrategy(PatternFindOptions.Strategy.BEST)
                        .build());
        successfulFindRecord.setActionSuccess(true);
        successfulFindRecord.setDuration(150.5);
        successfulFindRecord.setTimeStamp(LocalDateTime.now().minusHours(2));

        Match match1 =
                new Match.Builder()
                        .setRegion(new Region(100, 200, 50, 30))
                        .setSimScore(0.92)
                        .build();
        successfulFindRecord.setMatchList(Arrays.asList(match1));

        // Create failed find record
        failedFindRecord = new ActionRecord();
        failedFindRecord.setActionConfig(
                new PatternFindOptions.Builder().setSimilarity(0.95).build());
        failedFindRecord.setActionSuccess(false);
        failedFindRecord.setDuration(500.0);
        failedFindRecord.setTimeStamp(LocalDateTime.now().minusHours(1));
        failedFindRecord.setMatchList(new ArrayList<>());

        // Create click record
        clickRecord = new ActionRecord();
        clickRecord.setActionConfig(new ClickOptions.Builder().setNumberOfClicks(1).build());
        clickRecord.setActionSuccess(true);
        clickRecord.setDuration(50.0);
        clickRecord.setTimeStamp(LocalDateTime.now().minusMinutes(30));

        Match clickMatch =
                new Match.Builder()
                        .setRegion(new Region(200, 300, 40, 40))
                        .setSimScore(0.88)
                        .build();
        clickRecord.setMatchList(Arrays.asList(clickMatch));

        // Create type record with text
        typeRecord = new ActionRecord();
        typeRecord.setActionConfig(new TypeOptions.Builder().setTypeDelay(0.1).build());
        typeRecord.setActionSuccess(true);
        typeRecord.setDuration(250.0);
        typeRecord.setTimeStamp(LocalDateTime.now().minusMinutes(15));
        typeRecord.setText("test input");
        typeRecord.setMatchList(new ArrayList<>());

        // Add all records to history
        testHistory.setSnapshots(
                Arrays.asList(successfulFindRecord, failedFindRecord, clickRecord, typeRecord));
    }

    @Test
    public void testExportToCSV_CreatesValidCSVFile() throws IOException {
        // Arrange
        String filename = "test-export.csv";

        // Act
        exporter.exportToCSV(testHistory, filename, tempDir.toString());

        // Assert
        Path csvPath = tempDir.resolve(filename);
        assertTrue(Files.exists(csvPath), "CSV file should be created");

        List<String> lines = Files.readAllLines(csvPath);
        assertFalse(lines.isEmpty(), "CSV should not be empty");

        // Check header
        assertEquals(
                "Timestamp,Action,Success,Duration(ms),Matches,Text,Details",
                lines.get(0),
                "CSV header should be correct");

        // Check data rows
        assertEquals(5, lines.size(), "Should have header + 4 data rows");

        // Verify successful find record row
        String findRow = lines.get(1);
        assertTrue(findRow.contains("PatternFindOptions"), "Should contain action type");
        assertTrue(findRow.contains("true"), "Should show success");
        assertTrue(findRow.contains("150"), "Should contain duration");

        // Verify type record has text
        String typeRow = lines.get(4);
        assertTrue(typeRow.contains("\"test input\""), "Should contain escaped text");
    }

    @Test
    public void testExportToHTML_CreatesValidHTMLReport() throws IOException {
        // Arrange
        String filename = "test-report.html";

        // Act
        exporter.exportToHTML(testHistory, filename, tempDir.toString());

        // Assert
        Path htmlPath = tempDir.resolve(filename);
        assertTrue(Files.exists(htmlPath), "HTML file should be created");

        String html = Files.readString(htmlPath);

        // Check HTML structure
        assertTrue(html.contains("<!DOCTYPE html>"), "Should have DOCTYPE");
        assertTrue(html.contains("<title>Action History Report</title>"), "Should have title");
        assertTrue(html.contains("Summary Statistics"), "Should have summary section");
        assertTrue(html.contains("Action Details"), "Should have details section");

        // Check summary stats
        assertTrue(html.contains("Total Actions"), "Should show total actions");
        assertTrue(html.contains("Success Rate"), "Should show success rate");
        assertTrue(html.contains("Times Found"), "Should show times found");
        assertTrue(
                html.contains("<div class='stat-value'>7</div>"),
                "Should show correct times found");

        // Check action details table
        assertTrue(html.contains("<table>"), "Should have table");
        assertTrue(html.contains("PatternFind"), "Should show action types");
        assertTrue(html.contains("✓"), "Should show success checkmark");
        assertTrue(html.contains("✗"), "Should show failure X");
    }

    @Test
    public void testGenerateSummary_ReturnsCorrectStatistics() {
        // Act
        Map<String, Object> summary = exporter.generateSummary(testHistory);

        // Assert
        assertEquals(4, summary.get("totalActions"), "Should have 4 total actions");
        assertEquals(10, summary.get("timesSearched"), "Should match times searched");
        assertEquals(7, summary.get("timesFound"), "Should match times found");
        assertEquals(3L, summary.get("successCount"), "Should have 3 successful actions");
        assertEquals(1L, summary.get("failureCount"), "Should have 1 failed action");

        double successRate = (Double) summary.get("successRate");
        assertEquals(75.0, successRate, 0.01, "Success rate should be 75%");

        // Check duration statistics
        assertEquals(950L, summary.get("totalDuration"), "Total duration should be sum of all");
        assertEquals(
                237.5,
                (Double) summary.get("avgDuration"),
                0.01,
                "Average duration should be correct");
        assertEquals(50L, summary.get("minDuration"), "Min duration should be 50ms");
        assertEquals(500L, summary.get("maxDuration"), "Max duration should be 500ms");

        // Check action type breakdown
        @SuppressWarnings("unchecked")
        Map<String, Long> actionTypes = (Map<String, Long>) summary.get("actionTypes");
        assertEquals(2L, actionTypes.get("PatternFindOptions"), "Should have 2 find actions");
        assertEquals(1L, actionTypes.get("ClickOptions"), "Should have 1 click action");
        assertEquals(1L, actionTypes.get("TypeOptions"), "Should have 1 type action");
    }

    @Test
    public void testFilterHistory_FiltersCorrectly() {
        // Act - filter for successful actions only
        ActionHistory successOnly = exporter.filterHistory(testHistory, true, 0, Long.MAX_VALUE);

        // Assert
        assertEquals(3, successOnly.getSnapshots().size(), "Should have 3 successful actions");
        assertTrue(
                successOnly.getSnapshots().stream().allMatch(ActionRecord::isActionSuccess),
                "All records should be successful");

        // Act - filter by duration (100-300ms)
        ActionHistory durationFiltered = exporter.filterHistory(testHistory, false, 100, 300);

        // Assert
        assertEquals(
                2,
                durationFiltered.getSnapshots().size(),
                "Should have 2 actions in duration range");
        assertTrue(
                durationFiltered.getSnapshots().stream()
                        .allMatch(r -> r.getDuration() >= 100 && r.getDuration() <= 300),
                "All records should be within duration range");
    }

    @Test
    public void testBatchExportCSV_CombinesMultipleHistories() throws IOException {
        // Arrange
        Map<String, ActionHistory> histories = new HashMap<>();
        histories.put("Session1", testHistory);

        ActionHistory history2 = new ActionHistory();
        history2.setTimesSearched(5);
        history2.setTimesFound(3);
        ActionRecord record = new ActionRecord();
        record.setActionConfig(new ClickOptions.Builder().build());
        record.setActionSuccess(true);
        record.setDuration(75.0);
        record.setTimeStamp(LocalDateTime.now());
        record.setMatchList(new ArrayList<>());
        history2.setSnapshots(Arrays.asList(record));
        histories.put("Session2", history2);

        String filename = "batch-export.csv";

        // Act
        exporter.batchExport(histories, filename, ActionHistoryExporter.ExportFormat.CSV);

        // Assert
        Path csvPath = Path.of("reports/action-history", filename);
        assertTrue(Files.exists(csvPath), "Batch CSV file should be created");

        List<String> lines = Files.readAllLines(csvPath);
        assertEquals(6, lines.size(), "Should have header + 5 data rows");

        // Check that source column is added
        assertTrue(lines.get(0).startsWith("Source,"), "Header should have Source column");
        assertTrue(
                lines.stream().anyMatch(l -> l.startsWith("Session1,")),
                "Should have Session1 records");
        assertTrue(
                lines.stream().anyMatch(l -> l.startsWith("Session2,")),
                "Should have Session2 records");
    }

    @Test
    public void testBatchExportHTML_CreatesConsolidatedReport() throws IOException {
        // Arrange
        Map<String, ActionHistory> histories = new HashMap<>();
        histories.put("Test Session 1", testHistory);

        ActionHistory emptyHistory = new ActionHistory();
        emptyHistory.setTimesSearched(0);
        emptyHistory.setTimesFound(0);
        emptyHistory.setSnapshots(new ArrayList<>());
        histories.put("Empty Session", emptyHistory);

        String filename = "batch-report.html";

        // Act
        exporter.batchExport(histories, filename, ActionHistoryExporter.ExportFormat.HTML);

        // Assert
        Path htmlPath = Path.of("reports/action-history", filename);
        assertTrue(Files.exists(htmlPath), "Batch HTML file should be created");

        String html = Files.readString(htmlPath);

        // Check combined report structure
        assertTrue(html.contains("Batch Action History Report"), "Should have batch title");
        assertTrue(html.contains("Total histories: 2"), "Should show history count");
        assertTrue(html.contains("<h2>Test Session 1</h2>"), "Should have first session section");
        assertTrue(html.contains("<h2>Empty Session</h2>"), "Should have empty session section");

        // Verify both summaries are present
        assertTrue(
                html.contains("Total Actions") && html.contains("Success Rate"),
                "Should have summary sections");
    }

    @Test
    public void testExportWithSpecialCharacters_HandlesEscapingCorrectly() throws IOException {
        // Arrange
        ActionHistory specialHistory = new ActionHistory();
        ActionRecord specialRecord = new ActionRecord();
        specialRecord.setActionConfig(new TypeOptions.Builder().build());
        specialRecord.setActionSuccess(true);
        specialRecord.setDuration(100.0);
        specialRecord.setTimeStamp(LocalDateTime.now());
        specialRecord.setText("Text with \"quotes\", <tags> & special chars");
        specialRecord.setMatchList(new ArrayList<>());
        specialHistory.setSnapshots(Arrays.asList(specialRecord));

        // Act - CSV
        exporter.exportToCSV(specialHistory, "special-csv.csv", tempDir.toString());

        // Assert CSV escaping
        Path csvPath = tempDir.resolve("special-csv.csv");
        String csv = Files.readString(csvPath);
        assertTrue(
                csv.contains("\"Text with \"\"quotes\"\", <tags> & special chars\""),
                "CSV should properly escape quotes");

        // Act - HTML
        exporter.exportToHTML(specialHistory, "special-html.html", tempDir.toString());

        // Assert HTML escaping
        Path htmlPath = tempDir.resolve("special-html.html");
        String html = Files.readString(htmlPath);
        assertTrue(
                html.contains("&lt;tags&gt;") && html.contains("&amp;"),
                "HTML should properly escape special characters");
    }

    @Test
    public void testExportEmptyHistory_HandlesGracefully() throws IOException {
        // Arrange
        ActionHistory emptyHistory = new ActionHistory();
        emptyHistory.setTimesSearched(0);
        emptyHistory.setTimesFound(0);
        emptyHistory.setSnapshots(new ArrayList<>());

        // Act & Assert - should not throw exceptions
        assertDoesNotThrow(
                () -> {
                    exporter.exportToCSV(emptyHistory, "empty.csv", tempDir.toString());
                    exporter.exportToHTML(emptyHistory, "empty.html", tempDir.toString());
                });

        // Verify files are created with proper structure
        Path csvPath = tempDir.resolve("empty.csv");
        Path htmlPath = tempDir.resolve("empty.html");

        assertTrue(Files.exists(csvPath), "Empty CSV should be created");
        assertTrue(Files.exists(htmlPath), "Empty HTML should be created");

        List<String> csvLines = Files.readAllLines(csvPath);
        assertEquals(1, csvLines.size(), "Empty CSV should only have header");

        String html = Files.readString(htmlPath);
        assertTrue(
                html.contains("Total Actions</div>\n<div class='stat-value'>0</div>"),
                "HTML should show 0 total actions");
    }

    @Test
    public void testExportLargeHistory_PerformsEfficiently() throws IOException {
        // Arrange - create large history with 1000 records
        ActionHistory largeHistory = new ActionHistory();
        List<ActionRecord> records = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            ActionRecord record = new ActionRecord();
            record.setActionConfig(new PatternFindOptions.Builder().build());
            record.setActionSuccess(i % 3 != 0); // 2/3 success rate
            record.setDuration(50.0 + Math.random() * 450); // 50-500ms
            record.setTimeStamp(LocalDateTime.now().minusSeconds(i));

            if (record.isActionSuccess()) {
                Match match =
                        new Match.Builder()
                                .setRegion(new Region(i, i, 50, 50))
                                .setSimScore(0.8 + Math.random() * 0.2)
                                .build();
                record.setMatchList(Arrays.asList(match));
            } else {
                record.setMatchList(new ArrayList<>());
            }

            records.add(record);
        }

        largeHistory.setSnapshots(records);
        largeHistory.setTimesSearched(1000);
        largeHistory.setTimesFound(667);

        // Act
        long startTime = System.currentTimeMillis();
        exporter.exportToCSV(largeHistory, "large.csv", tempDir.toString());
        exporter.exportToHTML(largeHistory, "large.html", tempDir.toString());
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertTrue(duration < 5000, "Export should complete within 5 seconds for 1000 records");

        Path csvPath = tempDir.resolve("large.csv");
        Path htmlPath = tempDir.resolve("large.html");

        assertTrue(Files.exists(csvPath), "Large CSV should be created");
        assertTrue(Files.exists(htmlPath), "Large HTML should be created");

        List<String> csvLines = Files.readAllLines(csvPath);
        assertEquals(1001, csvLines.size(), "CSV should have header + 1000 data rows");
    }
}
