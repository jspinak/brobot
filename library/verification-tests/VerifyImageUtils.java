package io.github.jspinak.brobot.util.image.constants;

import org.bytedeco.opencv.opencv_core.Scalar;

/**
 * Verification program for image utility classes
 */
public class VerifyImageUtils {
    
    public static void main(String[] args) {
        System.out.println("Testing Image Utility classes...\n");
        
        boolean allTestsPassed = true;
        
        // Test 1: BgrColorConstants
        System.out.println("Test 1: BgrColorConstants");
        try {
            // Test BLUE
            Scalar blue = BgrColorConstants.BLUE.getScalar();
            assert blue != null;
            assert blue.get(0) == 255.0 : "Blue channel should be 255";
            assert blue.get(1) == 0.0 : "Green channel should be 0";
            assert blue.get(2) == 0.0 : "Red channel should be 0";
            assert blue.get(3) == 255.0 : "Alpha should be 255";
            
            // Test GREEN
            Scalar green = BgrColorConstants.GREEN.getScalar();
            assert green != null;
            assert green.get(0) == 0.0 : "Blue channel should be 0";
            assert green.get(1) == 255.0 : "Green channel should be 255";
            assert green.get(2) == 0.0 : "Red channel should be 0";
            assert green.get(3) == 255.0 : "Alpha should be 255";
            
            // Test RED
            Scalar red = BgrColorConstants.RED.getScalar();
            assert red != null;
            assert red.get(0) == 0.0 : "Blue channel should be 0";
            assert red.get(1) == 0.0 : "Green channel should be 0";
            assert red.get(2) == 255.0 : "Red channel should be 255";
            assert red.get(3) == 255.0 : "Alpha should be 255";
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 2: Enum operations
        System.out.println("Test 2: Enum operations");
        try {
            BgrColorConstants[] colors = BgrColorConstants.values();
            assert colors.length == 3 : "Should have 3 colors";
            
            assert BgrColorConstants.valueOf("BLUE") == BgrColorConstants.BLUE;
            assert BgrColorConstants.valueOf("GREEN") == BgrColorConstants.GREEN;
            assert BgrColorConstants.valueOf("RED") == BgrColorConstants.RED;
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 3: Scalar reusability
        System.out.println("Test 3: Scalar reusability");
        try {
            Scalar red1 = BgrColorConstants.RED.getScalar();
            Scalar red2 = BgrColorConstants.RED.getScalar();
            
            assert red1 == red2 : "Should return same Scalar instance";
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 4: Color uniqueness
        System.out.println("Test 4: Color uniqueness");
        try {
            Scalar blue = BgrColorConstants.BLUE.getScalar();
            Scalar green = BgrColorConstants.GREEN.getScalar();
            Scalar red = BgrColorConstants.RED.getScalar();
            
            // Each primary color should have only one channel at 255
            int blueNonZero = 0;
            int greenNonZero = 0;
            int redNonZero = 0;
            
            for (int i = 0; i < 3; i++) { // Check BGR channels (not alpha)
                if (blue.get(i) > 0) blueNonZero++;
                if (green.get(i) > 0) greenNonZero++;
                if (red.get(i) > 0) redNonZero++;
            }
            
            assert blueNonZero == 1 : "Blue should have one non-zero channel";
            assert greenNonZero == 1 : "Green should have one non-zero channel";
            assert redNonZero == 1 : "Red should have one non-zero channel";
            
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