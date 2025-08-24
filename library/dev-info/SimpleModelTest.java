package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.model.element.Movement;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.action.ActionHistory;

/**
 * Simple test to verify model classes work correctly
 */
public class SimpleModelTest {
    
    public static void main(String[] args) {
        System.out.println("Testing model classes...");
        
        // Test Movement
        testMovement();
        
        // Test Text
        testText();
        
        // Test ActionRecord
        testActionRecord();
        
        // Test ActionHistory
        testActionHistory();
        
        System.out.println("\nAll basic tests passed!");
    }
    
    private static void testMovement() {
        System.out.println("\nTesting Movement...");
        
        Location start = new Location(100, 200);
        Location end = new Location(300, 500);
        Movement movement = new Movement(start, end);
        
        assert movement.getDeltaX() == 200 : "DeltaX should be 200";
        assert movement.getDeltaY() == 300 : "DeltaY should be 300";
        assert movement.getStartLocation().equals(start) : "Start location mismatch";
        assert movement.getEndLocation().equals(end) : "End location mismatch";
        
        // Test null handling
        try {
            new Movement(null, end);
            assert false : "Should throw NullPointerException for null start";
        } catch (NullPointerException e) {
            // Expected
        }
        
        try {
            new Movement(start, null);
            assert false : "Should throw NullPointerException for null end";
        } catch (NullPointerException e) {
            // Expected
        }
        
        System.out.println("  ✓ Movement class works correctly");
    }
    
    private static void testText() {
        System.out.println("\nTesting Text...");
        
        Text text = new Text();
        assert text.isEmpty() : "New text should be empty";
        assert text.size() == 0 : "Size should be 0";
        
        text.add("First");
        text.add("Second");
        text.add("Third");
        
        assert text.size() == 3 : "Size should be 3";
        assert !text.isEmpty() : "Text should not be empty";
        assert "First".equals(text.get(0)) : "First element mismatch";
        assert "Second".equals(text.get(1)) : "Second element mismatch";
        assert "Third".equals(text.get(2)) : "Third element mismatch";
        
        // Test addAll
        Text otherText = new Text();
        otherText.add("Fourth");
        otherText.add("Fifth");
        text.addAll(otherText);
        
        assert text.size() == 5 : "Size should be 5 after addAll";
        assert "Fourth".equals(text.get(3)) : "Fourth element mismatch";
        assert "Fifth".equals(text.get(4)) : "Fifth element mismatch";
        
        System.out.println("  ✓ Text class works correctly");
    }
    
    private static void testActionRecord() {
        System.out.println("\nTesting ActionRecord...");
        
        ActionRecord record = new ActionRecord();
        assert record.getText().equals("") : "Default text should be empty";
        assert record.getDuration() == 0.0 : "Default duration should be 0";
        assert !record.isActionSuccess() : "Default action success should be false";
        assert !record.isResultSuccess() : "Default result success should be false";
        
        record.setText("Test text");
        record.setDuration(100.5);
        record.setActionSuccess(true);
        record.setResultSuccess(true);
        
        assert "Test text".equals(record.getText()) : "Text mismatch";
        assert record.getDuration() == 100.5 : "Duration mismatch";
        assert record.isActionSuccess() : "Action success should be true";
        assert record.isResultSuccess() : "Result success should be true";
        
        System.out.println("  ✓ ActionRecord class works correctly");
    }
    
    private static void testActionHistory() {
        System.out.println("\nTesting ActionHistory...");
        
        ActionHistory history = new ActionHistory();
        assert history.isEmpty() : "New history should be empty";
        assert history.getTimesSearched() == 0 : "Times searched should be 0";
        assert history.getTimesFound() == 0 : "Times found should be 0";
        
        // Add a successful record
        ActionRecord successRecord = new ActionRecord();
        successRecord.setText("Found");
        history.addSnapshot(successRecord);
        
        assert !history.isEmpty() : "History should not be empty";
        assert history.getTimesSearched() == 1 : "Times searched should be 1";
        assert history.getTimesFound() == 1 : "Times found should be 1";
        
        // Add an unsuccessful record
        ActionRecord failRecord = new ActionRecord();
        history.addSnapshot(failRecord);
        
        assert history.getTimesSearched() == 2 : "Times searched should be 2";
        assert history.getTimesFound() == 1 : "Times found should still be 1";
        
        System.out.println("  ✓ ActionHistory class works correctly");
    }
}