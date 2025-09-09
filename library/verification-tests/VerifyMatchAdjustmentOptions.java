package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;

/**
 * Verification program for MatchAdjustmentOptions functionality
 */
public class VerifyMatchAdjustmentOptions {
    
    public static void main(String[] args) {
        System.out.println("Testing MatchAdjustmentOptions...\n");
        
        boolean allTestsPassed = true;
        
        // Test 1: Default builder
        System.out.println("Test 1: Default builder");
        try {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder().build();
            assert options != null : "Options should not be null";
            assert options.getTargetPosition() == null : "Default target position should be null";
            assert options.getTargetOffset() == null : "Default target offset should be null";
            assert options.getAddW() == 0 : "Default addW should be 0";
            assert options.getAddH() == 0 : "Default addH should be 0";
            assert options.getAbsoluteW() == -1 : "Default absoluteW should be -1";
            assert options.getAbsoluteH() == -1 : "Default absoluteH should be -1";
            assert options.getAddX() == 0 : "Default addX should be 0";
            assert options.getAddY() == 0 : "Default addY should be 0";
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 2: Size adjustments
        System.out.println("Test 2: Size adjustments");
        try {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddW(20)
                .setAddH(15)
                .build();
            
            assert options.getAddW() == 20 : "AddW should be 20";
            assert options.getAddH() == 15 : "AddH should be 15";
            
            // Test negative adjustments
            MatchAdjustmentOptions shrink = MatchAdjustmentOptions.builder()
                .setAddW(-10)
                .setAddH(-5)
                .build();
            
            assert shrink.getAddW() == -10 : "Can subtract from width";
            assert shrink.getAddH() == -5 : "Can subtract from height";
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 3: Absolute size
        System.out.println("Test 3: Absolute size");
        try {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAbsoluteW(200)
                .setAbsoluteH(150)
                .build();
            
            assert options.getAbsoluteW() == 200 : "Absolute width should be 200";
            assert options.getAbsoluteH() == 150 : "Absolute height should be 150";
            
            // Test disabled absolute size
            MatchAdjustmentOptions disabled = MatchAdjustmentOptions.builder()
                .setAbsoluteW(-1)
                .setAbsoluteH(-1)
                .build();
            
            assert disabled.getAbsoluteW() == -1 : "Negative means disabled";
            assert disabled.getAbsoluteH() == -1 : "Negative means disabled";
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 4: Position adjustments
        System.out.println("Test 4: Position adjustments");
        try {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddX(30)
                .setAddY(40)
                .build();
            
            assert options.getAddX() == 30 : "AddX should be 30";
            assert options.getAddY() == 40 : "AddY should be 40";
            
            // Test negative position adjustments
            MatchAdjustmentOptions negative = MatchAdjustmentOptions.builder()
                .setAddX(-20)
                .setAddY(-30)
                .build();
            
            assert negative.getAddX() == -20 : "Can have negative X adjustment";
            assert negative.getAddY() == -30 : "Can have negative Y adjustment";
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 5: Target position and offset
        System.out.println("Test 5: Target position and offset");
        try {
            Position center = new Position(50, 50);
            Location offset = new Location(10, 15);
            
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setTargetPosition(center)
                .setTargetOffset(offset)
                .build();
            
            assert options.getTargetPosition() == center : "Target position should be set";
            assert options.getTargetOffset() == offset : "Target offset should be set";
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 6: Builder chaining
        System.out.println("Test 6: Builder chaining");
        try {
            MatchAdjustmentOptions options = MatchAdjustmentOptions.builder()
                .setAddW(10)
                .setAddH(15)
                .setAbsoluteW(300)
                .setAbsoluteH(200)
                .setAddX(20)
                .setAddY(25)
                .build();
            
            assert options.getAddW() == 10 : "All values should be set";
            assert options.getAddH() == 15;
            assert options.getAbsoluteW() == 300;
            assert options.getAbsoluteH() == 200;
            assert options.getAddX() == 20;
            assert options.getAddY() == 25;
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 7: toBuilder copy functionality
        System.out.println("Test 7: toBuilder copy functionality");
        try {
            MatchAdjustmentOptions original = MatchAdjustmentOptions.builder()
                .setAddW(10)
                .setAddH(20)
                .build();
            
            MatchAdjustmentOptions modified = original.toBuilder()
                .setAddX(30)
                .setAddY(40)
                .build();
            
            assert original.getAddX() == 0 : "Original should be unchanged";
            assert original.getAddY() == 0 : "Original should be unchanged";
            assert modified.getAddX() == 30 : "Modified should have new values";
            assert modified.getAddY() == 40 : "Modified should have new values";
            assert modified.getAddW() == original.getAddW() : "Other values preserved";
            assert modified.getAddH() == original.getAddH() : "Other values preserved";
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Summary
        System.out.println("========================================");
        if (allTestsPassed) {
            System.out.println("✓ ALL TESTS PASSED!");
        } else {
            System.out.println("✗ SOME TESTS FAILED!");
        }
        System.out.println("========================================");
        
        System.exit(allTestsPassed ? 0 : 1);
    }
}