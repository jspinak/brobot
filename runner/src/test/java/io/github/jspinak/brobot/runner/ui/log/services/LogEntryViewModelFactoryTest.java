package io.github.jspinak.brobot.runner.ui.log.services;

import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.ui.log.models.LogEntryViewModel;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import io.github.jspinak.brobot.tools.logging.model.PerformanceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LogEntryViewModelFactory.
 */
class LogEntryViewModelFactoryTest {
    
    private LogEntryViewModelFactory factory;
    
    @BeforeEach
    void setUp() {
        factory = new LogEntryViewModelFactory();
    }
    
    @Test
    @DisplayName("Should create view model from LogData")
    void testCreateFromLogData() {
        // Given
        LogData logData = new LogData();
        logData.setTimestamp(Instant.now());
        logData.setType(LogEventType.ACTION);
        logData.setDescription("Test action performed");
        logData.setSuccess(true);
        logData.setCurrentStateName("TestState");
        
        // When
        LogEntryViewModel viewModel = factory.createFromLogData(logData);
        
        // Then
        assertNotNull(viewModel);
        assertEquals("ACTION", viewModel.getType());
        assertEquals("Test action performed", viewModel.getMessage());
        assertTrue(viewModel.isSuccess());
        assertEquals("INFO", viewModel.getLevel());
        assertNotNull(viewModel.getTime());
    }
    
    @Test
    @DisplayName("Should create view model from LogEvent")
    void testCreateFromLogEvent() {
        // Given
        LogEvent logEvent = LogEvent.error(
            this, 
            "Test error message", 
            "TEST_CATEGORY", 
            new RuntimeException("Test exception")
        );
        
        // When
        LogEntryViewModel viewModel = factory.createFromLogEvent(logEvent);
        
        // Then
        assertNotNull(viewModel);
        assertEquals("ERROR", viewModel.getLevel());
        assertEquals("Test error message", viewModel.getMessage());
        assertFalse(viewModel.isSuccess());
        assertEquals("SYSTEM", viewModel.getType()); // Category doesn't match LogEventType
    }
    
    @Test
    @DisplayName("Should handle null LogData")
    void testCreateFromNullLogData() {
        // When
        LogEntryViewModel viewModel = factory.createFromLogData(null);
        
        // Then
        assertNotNull(viewModel);
        assertNull(viewModel.getTime());
        assertNull(viewModel.getMessage());
    }
    
    @Test
    @DisplayName("Should create batch from LogData list")
    void testCreateBatchFromLogData() {
        // Given
        LogData data1 = new LogData();
        data1.setTimestamp(Instant.now());
        data1.setDescription("Log 1");
        data1.setType(LogEventType.STATE);
        
        LogData data2 = new LogData();
        data2.setTimestamp(Instant.now());
        data2.setDescription("Log 2");
        data2.setType(LogEventType.ERROR);
        data2.setSuccess(false);
        
        List<LogData> logDataList = Arrays.asList(data1, data2);
        
        // When
        List<LogEntryViewModel> viewModels = factory.createBatchFromLogData(logDataList);
        
        // Then
        assertEquals(2, viewModels.size());
        assertEquals("Log 1", viewModels.get(0).getMessage());
        assertEquals("STATE", viewModels.get(0).getType());
        assertEquals("Log 2", viewModels.get(1).getMessage());
        assertEquals("ERROR", viewModels.get(1).getLevel());
    }
    
    @Test
    @DisplayName("Should format detailed text")
    void testFormatDetailedText() {
        // Given
        LogData logData = new LogData();
        logData.setTimestamp(Instant.now());
        logData.setDescription("Test message");
        logData.setType(LogEventType.ACTION);
        logData.setSuccess(true);
        logData.setActionType("CLICK");
        logData.setCurrentStateName("ActiveState");
        
        PerformanceData performance = new PerformanceData();
        performance.setActionDuration(150);
        logData.setPerformance(performance);
        
        LogEntryViewModel viewModel = factory.createFromLogData(logData);
        
        // When
        String detailedText = factory.formatDetailedText(
            viewModel, 
            LogEntryViewModelFactory.DetailFormat.DETAILED
        );
        
        // Then
        assertNotNull(detailedText);
        assertTrue(detailedText.contains("Test message"));
        assertTrue(detailedText.contains("Action Type: CLICK"));
        assertTrue(detailedText.contains("Current State: ActiveState"));
        assertTrue(detailedText.contains("Action Duration: 150 ms"));
    }
    
    @Test
    @DisplayName("Should format compact text")
    void testFormatCompactText() {
        // Given
        LogData logData = new LogData();
        logData.setTimestamp(Instant.now());
        logData.setDescription("Compact test");
        logData.setType(LogEventType.INFO);
        LogEntryViewModel viewModel = factory.createFromLogData(logData);
        
        // When
        String compactText = factory.formatDetailedText(
            viewModel, 
            LogEntryViewModelFactory.DetailFormat.COMPACT
        );
        
        // Then
        assertNotNull(compactText);
        assertTrue(compactText.contains("[INFO]"));
        assertTrue(compactText.contains("Compact test"));
        assertFalse(compactText.contains("Current State")); // Should not include details
    }
    
    @Test
    @DisplayName("Should format JSON text")
    void testFormatJsonText() {
        // Given
        LogData logData = new LogData();
        logData.setTimestamp(Instant.now());
        logData.setDescription("JSON test");
        logData.setType(LogEventType.STATE);
        logData.setSuccess(true);
        logData.setErrorMessage("Test error");
        LogEntryViewModel viewModel = factory.createFromLogData(logData);
        
        // When
        String jsonText = factory.formatDetailedText(
            viewModel, 
            LogEntryViewModelFactory.DetailFormat.JSON
        );
        
        // Then
        assertNotNull(jsonText);
        assertTrue(jsonText.startsWith("{"));
        assertTrue(jsonText.endsWith("}"));
        assertTrue(jsonText.contains("\"message\": \"JSON test\""));
        assertTrue(jsonText.contains("\"error\": \"Test error\""));
        assertTrue(jsonText.contains("\"success\": true"));
    }
    
    @Test
    @DisplayName("Should format XML text")
    void testFormatXmlText() {
        // Given
        LogData logData = new LogData();
        logData.setTimestamp(Instant.now());
        logData.setDescription("XML test");
        logData.setType(LogEventType.TRANSITION);
        LogEntryViewModel viewModel = factory.createFromLogData(logData);
        
        // When
        String xmlText = factory.formatDetailedText(
            viewModel, 
            LogEntryViewModelFactory.DetailFormat.XML
        );
        
        // Then
        assertNotNull(xmlText);
        assertTrue(xmlText.startsWith("<logEntry>"));
        assertTrue(xmlText.endsWith("</logEntry>"));
        assertTrue(xmlText.contains("<message>XML test</message>"));
        assertTrue(xmlText.contains("<type>TRANSITION</type>"));
    }
    
    @Test
    @DisplayName("Should handle special characters in JSON")
    void testJsonEscaping() {
        // Given
        LogData logData = new LogData();
        logData.setTimestamp(Instant.now());
        logData.setDescription("Test with \"quotes\" and \nnewlines");
        logData.setType(LogEventType.ACTION);
        LogEntryViewModel viewModel = factory.createFromLogData(logData);
        
        // When
        String jsonText = factory.formatDetailedText(
            viewModel, 
            LogEntryViewModelFactory.DetailFormat.JSON
        );
        
        // Then
        assertTrue(jsonText.contains("\\\"quotes\\\""));
        assertTrue(jsonText.contains("\\nnewlines"));
    }
    
    @Test
    @DisplayName("Should handle special characters in XML")
    void testXmlEscaping() {
        // Given
        LogData logData = new LogData();
        logData.setTimestamp(Instant.now());
        logData.setDescription("Test with <tags> & special chars");
        logData.setType(LogEventType.ACTION);
        LogEntryViewModel viewModel = factory.createFromLogData(logData);
        
        // When
        String xmlText = factory.formatDetailedText(
            viewModel, 
            LogEntryViewModelFactory.DetailFormat.XML
        );
        
        // Then
        assertTrue(xmlText.contains("&lt;tags&gt;"));
        assertTrue(xmlText.contains("&amp;"));
    }
    
    @Test
    @DisplayName("Should manage cache when enabled")
    void testCaching() {
        // Given
        LogEntryViewModelFactory.ViewModelConfiguration config = 
            LogEntryViewModelFactory.ViewModelConfiguration.builder()
                .cacheEnabled(true)
                .maxCacheSize(10)
                .build();
        factory.setConfiguration(config);
        
        LogData logData = new LogData();
        logData.setTimestamp(Instant.now());
        logData.setDescription("Cached entry");
        logData.setType(LogEventType.ACTION);
        
        // When
        LogEntryViewModel viewModel1 = factory.createFromLogData(logData);
        LogEntryViewModel viewModel2 = factory.createFromLogData(logData);
        
        // Then
        assertEquals(1, factory.getCacheSize());
        // Note: Can't test object equality due to new instance creation
        
        // Test cache clearing
        factory.clearCache();
        assertEquals(0, factory.getCacheSize());
    }
    
    @Test
    @DisplayName("Should handle empty lists")
    void testEmptyLists() {
        // When
        List<LogEntryViewModel> fromLogData = factory.createBatchFromLogData(Arrays.asList());
        List<LogEntryViewModel> fromLogEvents = factory.createBatchFromLogEvents(Arrays.asList());
        
        // Then
        assertNotNull(fromLogData);
        assertTrue(fromLogData.isEmpty());
        assertNotNull(fromLogEvents);
        assertTrue(fromLogEvents.isEmpty());
    }
}