package io.github.jspinak.brobot.tools.actionhistory;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.actionhistory.PerformanceValidator.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified tests for PerformanceValidator class to ensure coverage.
 */
public class PerformanceValidatorSimpleTest extends BrobotTestBase {
    
    private PerformanceValidator validator;
    private ActionHistory history;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        validator = new PerformanceValidator();
        history = new ActionHistory();
    }
    
    @Test
    @DisplayName("Should handle empty history")
    public void testEmptyHistory() {
        ValidationResult result = validator.validate(history);
        
        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
    }
    
    @Test
    @DisplayName("Should validate successful history")
    public void testSuccessfulValidation() {
        // Create history with good performance
        for (int i = 0; i < 20; i++) {
            history.addSnapshot(createSuccessfulRecord(500));
        }
        
        ValidationResult result = validator.validate(history);
        
        assertTrue(result.isValid());
        assertEquals(100.0, result.getSuccessRate(), 0.1);
    }
    
    @Test
    @DisplayName("Should detect low success rate")
    public void testLowSuccessRate() {
        // Create history with 50% success rate
        for (int i = 0; i < 10; i++) {
            boolean success = i < 5;
            history.addSnapshot(createRecord(success, 500));
        }
        
        ValidationResult result = validator.validate(history);
        
        assertFalse(result.isValid());
        assertEquals(50.0, result.getSuccessRate(), 0.1);
    }
    
    @Test
    @DisplayName("Should compare two histories")
    public void testCompareHistories() {
        ActionHistory baseline = new ActionHistory();
        ActionHistory current = new ActionHistory();
        
        // Baseline with good performance
        for (int i = 0; i < 10; i++) {
            baseline.addSnapshot(createSuccessfulRecord(500));
        }
        
        // Current with degraded performance
        for (int i = 0; i < 10; i++) {
            current.addSnapshot(createSuccessfulRecord(800));
        }
        
        ComparisonResult result = validator.compare(baseline, current);
        
        assertTrue(result.isRegression());
        assertEquals(500, result.getBaselineAvgDuration(), 1);
        assertEquals(800, result.getCurrentAvgDuration(), 1);
    }
    
    @Test
    @DisplayName("Should detect anomalies")
    public void testAnomalyDetection() {
        // Create normal records
        for (int i = 0; i < 20; i++) {
            history.addSnapshot(createSuccessfulRecord(500 + i * 10));
        }
        // Add outliers
        history.addSnapshot(createSuccessfulRecord(5000)); // High outlier
        
        ValidationResult result = validator.validate(history);
        
        assertTrue(result.getAnomalyCount() > 0);
    }
    
    @Test
    @DisplayName("Should handle custom config")
    public void testCustomConfig() {
        ValidationConfig config = new ValidationConfig()
            .withMinSuccessRate(50.0)
            .withMaxAverageDuration(1000)
            .withMaxDuration(3000)
            .withMinSamples(5)
            .withMaxConsecutiveFailures(5);  // Allow more consecutive failures
        
        // Create history that would fail default but pass custom
        // Spread out the failures to avoid consecutive failure issue
        for (int i = 0; i < 10; i++) {
            boolean success = (i % 2 == 0) || i < 2; // Alternating pattern, 60% success
            history.addSnapshot(createRecord(success, 800));
        }
        
        ValidationResult result = validator.validate(history, config);
        
        assertTrue(result.isValid());
    }
    
    @Test
    @DisplayName("Should detect consecutive failures")
    public void testConsecutiveFailures() {
        // Create history with consecutive failures
        for (int i = 0; i < 20; i++) {
            boolean success = !(i >= 5 && i <= 9); // 5 consecutive failures
            history.addSnapshot(createRecord(success, 500));
        }
        
        ValidationResult result = validator.validate(history);
        
        assertFalse(result.isValid());
    }
    
    @Test
    @DisplayName("Should format results correctly")
    public void testResultFormatting() {
        ValidationResult result = new ValidationResult();
        result.setSuccessRate(85.5);
        result.setAverageDuration(1250.75);
        result.addError("Test error");
        
        String output = result.toString();
        
        assertTrue(output.contains("85.5%"));
        assertTrue(output.contains("Test error"));
    }
    
    @Test
    @DisplayName("Should format comparison results")
    public void testComparisonFormatting() {
        ComparisonResult result = new ComparisonResult();
        result.setBaselineAvgDuration(500);
        result.setCurrentAvgDuration(600);
        result.setDurationChange(20);
        result.setRegression(true);
        
        String output = result.toString();
        
        assertTrue(output.contains("500ms -> 600ms"));
        assertTrue(output.contains("Regression: true"));
    }
    
    private ActionRecord createSuccessfulRecord(double duration) {
        return createRecord(true, duration);
    }
    
    private ActionRecord createRecord(boolean success, double duration) {
        ActionRecord.Builder builder = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder().build())
            .setActionSuccess(success)
            .setDuration(duration);
        
        if (success) {
            builder.addMatch(new Match.Builder()
                .setRegion(100, 100, 50, 50)
                .setSimScore(0.95)
                .build());
        }
        
        return builder.build();
    }
}