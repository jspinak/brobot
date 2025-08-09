package io.github.jspinak.brobot.tools.testing.data;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ActionRecordTestUtilsTest {
    
    @Test
    @DisplayName("Should create basic action record with specified parameters")
    void testCreateActionRecord() {
        ActionRecord record = ActionRecordTestUtils.createActionRecord(0.95, 100, 200, 50, 30);
        
        assertNotNull(record);
        assertTrue(record.isActionSuccess());
        assertEquals(1, record.getMatchList().size());
        
        Match match = record.getMatchList().get(0);
        assertEquals(100, match.x());
        assertEquals(200, match.y());
        assertEquals(50, match.w());
        assertEquals(30, match.h());
        assertEquals(0.95, match.getScore(), 0.001);
        
        assertNotNull(record.getActionConfig());
        assertTrue(record.getActionConfig() instanceof PatternFindOptions);
    }
    
    @Test
    @DisplayName("Should create success record with default dimensions")
    void testCreateSuccessRecord() {
        ActionRecord record = ActionRecordTestUtils.createSuccessRecord(0.92, 150, 250);
        
        assertNotNull(record);
        assertTrue(record.isActionSuccess());
        assertEquals(1, record.getMatchList().size());
        
        Match match = record.getMatchList().get(0);
        assertEquals(150, match.x());
        assertEquals(250, match.y());
        assertEquals(80, match.w()); // Default width
        assertEquals(30, match.h()); // Default height
        assertEquals(0.92, match.getScore(), 0.001);
    }
    
    @Test
    @DisplayName("Should create failure record")
    void testCreateFailureRecord() {
        ActionRecord record = ActionRecordTestUtils.createFailureRecord();
        
        assertNotNull(record);
        assertFalse(record.isActionSuccess());
        assertFalse(record.isResultSuccess());
        assertTrue(record.getMatchList().isEmpty());
        assertNotNull(record.getActionConfig());
    }
    
    @Test
    @DisplayName("Should create multi-match record")
    void testCreateMultiMatchRecord() {
        int[][] matches = {
            {100, 100, 50, 50},
            {200, 200, 60, 60},
            {300, 300, 70, 70}
        };
        
        ActionRecord record = ActionRecordTestUtils.createMultiMatchRecord(0.88, matches);
        
        assertNotNull(record);
        assertTrue(record.isActionSuccess());
        assertEquals(3, record.getMatchList().size());
        
        for (int i = 0; i < matches.length; i++) {
            Match match = record.getMatchList().get(i);
            assertEquals(matches[i][0], match.x());
            assertEquals(matches[i][1], match.y());
            assertEquals(matches[i][2], match.w());
            assertEquals(matches[i][3], match.h());
            assertEquals(0.88, match.getScore(), 0.001);
        }
        
        PatternFindOptions config = (PatternFindOptions) record.getActionConfig();
        assertEquals(PatternFindOptions.Strategy.ALL, config.getStrategy());
    }
    
    @Test
    @DisplayName("Should create text record")
    void testCreateTextRecord() {
        String text = "Test Text";
        ActionRecord record = ActionRecordTestUtils.createTextRecord(text, 50, 75, 100, 25);
        
        assertNotNull(record);
        assertTrue(record.isActionSuccess());
        assertEquals(text, record.getText());
        assertEquals(1, record.getMatchList().size());
        
        Match match = record.getMatchList().get(0);
        assertEquals(50, match.x());
        assertEquals(75, match.y());
        assertEquals(100, match.w());
        assertEquals(25, match.h());
        assertEquals(text, match.getText());
    }
    
    @Test
    @DisplayName("Should create timed record with duration")
    void testCreateTimedRecord() {
        double durationSeconds = 1.5;
        ActionRecord record = ActionRecordTestUtils.createTimedRecord(
            0.90, 200, 300, 40, 40, durationSeconds);
        
        assertNotNull(record);
        assertTrue(record.isActionSuccess());
        assertEquals(durationSeconds, record.getDuration(), 0.001);
        assertEquals(1, record.getMatchList().size());
        
        Match match = record.getMatchList().get(0);
        assertEquals(200, match.x());
        assertEquals(300, match.y());
        assertEquals(40, match.w());
        assertEquals(40, match.h());
        assertEquals(0.90, match.getScore(), 0.001);
    }
    
    @Test
    @DisplayName("Should handle empty matches array for multi-match")
    void testCreateMultiMatchRecordEmpty() {
        ActionRecord record = ActionRecordTestUtils.createMultiMatchRecord(0.85);
        
        assertNotNull(record);
        assertTrue(record.isActionSuccess());
        assertTrue(record.getMatchList().isEmpty());
    }
    
    @Test
    @DisplayName("Should handle invalid match dimensions in multi-match")
    void testCreateMultiMatchRecordInvalid() {
        int[][] matches = {
            {100, 100, 50, 50},
            {200, 200}, // Invalid - only 2 values
            {300, 300, 70, 70}
        };
        
        ActionRecord record = ActionRecordTestUtils.createMultiMatchRecord(0.88, matches);
        
        assertNotNull(record);
        assertTrue(record.isActionSuccess());
        assertEquals(2, record.getMatchList().size()); // Only 2 valid matches
    }
}