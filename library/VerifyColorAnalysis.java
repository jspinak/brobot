package io.github.jspinak.brobot.model.analysis.color;

/**
 * Verification program for color analysis classes
 */
public class VerifyColorAnalysis {
    
    public static void main(String[] args) {
        System.out.println("Testing Color Analysis classes...\n");
        
        boolean allTestsPassed = true;
        
        // Test 1: ColorInfo
        System.out.println("Test 1: ColorInfo");
        try {
            ColorInfo colorInfo = new ColorInfo(ColorSchema.ColorValue.HUE);
            colorInfo.setAll(10.0, 170.0, 90.0, 30.0);
            
            assert colorInfo.getColorValue() == ColorSchema.ColorValue.HUE;
            assert colorInfo.getStat(ColorInfo.ColorStat.MIN) == 10.0;
            assert colorInfo.getStat(ColorInfo.ColorStat.MAX) == 170.0;
            assert colorInfo.getStat(ColorInfo.ColorStat.MEAN) == 90.0;
            assert colorInfo.getStat(ColorInfo.ColorStat.STDDEV) == 30.0;
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 2: ColorCluster
        System.out.println("Test 2: ColorCluster");
        try {
            ColorCluster cluster = new ColorCluster();
            assert cluster.getColorSchemas() != null;
            assert cluster.getColorSchemas().isEmpty();
            
            // Add BGR schema
            ColorSchemaBGR bgrSchema = new ColorSchemaBGR();
            cluster.setSchema(ColorCluster.ColorSchemaName.BGR, bgrSchema);
            assert cluster.getSchema(ColorCluster.ColorSchemaName.BGR) == bgrSchema;
            
            // Add HSV schema
            ColorSchemaHSV hsvSchema = new ColorSchemaHSV();
            cluster.setSchema(ColorCluster.ColorSchemaName.HSV, hsvSchema);
            assert cluster.getSchema(ColorCluster.ColorSchemaName.HSV) == hsvSchema;
            
            assert cluster.getColorSchemas().size() == 2;
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 3: ColorSchema integration
        System.out.println("Test 3: ColorSchema integration");
        try {
            ColorCluster cluster = new ColorCluster();
            ColorSchemaBGR bgr = new ColorSchemaBGR();
            
            // Add color info to BGR schema
            ColorInfo blue = new ColorInfo(ColorSchema.ColorValue.BLUE);
            blue.setAll(0.0, 100.0, 50.0, 20.0);
            bgr.getColorInfos().put(ColorSchema.ColorValue.BLUE, blue);
            
            ColorInfo green = new ColorInfo(ColorSchema.ColorValue.GREEN);
            green.setAll(50.0, 150.0, 100.0, 25.0);
            bgr.getColorInfos().put(ColorSchema.ColorValue.GREEN, green);
            
            ColorInfo red = new ColorInfo(ColorSchema.ColorValue.RED);
            red.setAll(100.0, 255.0, 180.0, 30.0);
            bgr.getColorInfos().put(ColorSchema.ColorValue.RED, red);
            
            cluster.setSchema(ColorCluster.ColorSchemaName.BGR, bgr);
            
            ColorSchemaBGR retrieved = (ColorSchemaBGR) cluster.getSchema(ColorCluster.ColorSchemaName.BGR);
            assert retrieved != null;
            assert retrieved.getColorInfos().size() == 3;
            assert retrieved.getColorInfos().get(ColorSchema.ColorValue.BLUE).getStat(ColorInfo.ColorStat.MEAN) == 50.0;
            assert retrieved.getColorInfos().get(ColorSchema.ColorValue.GREEN).getStat(ColorInfo.ColorStat.MEAN) == 100.0;
            assert retrieved.getColorInfos().get(ColorSchema.ColorValue.RED).getStat(ColorInfo.ColorStat.MEAN) == 180.0;
            
            System.out.println("✓ PASSED\n");
        } catch (Exception | AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
        
        // Test 4: Red color profile
        System.out.println("Test 4: Red color profile");
        try {
            ColorCluster redCluster = new ColorCluster();
            
            // Create red BGR profile
            ColorSchemaBGR bgr = new ColorSchemaBGR();
            ColorInfo blue = new ColorInfo(ColorSchema.ColorValue.BLUE);
            blue.setAll(0.0, 50.0, 25.0, 10.0);
            ColorInfo green = new ColorInfo(ColorSchema.ColorValue.GREEN);
            green.setAll(0.0, 50.0, 25.0, 10.0);
            ColorInfo red = new ColorInfo(ColorSchema.ColorValue.RED);
            red.setAll(200.0, 255.0, 230.0, 15.0);
            
            bgr.getColorInfos().put(ColorSchema.ColorValue.BLUE, blue);
            bgr.getColorInfos().put(ColorSchema.ColorValue.GREEN, green);
            bgr.getColorInfos().put(ColorSchema.ColorValue.RED, red);
            redCluster.setSchema(ColorCluster.ColorSchemaName.BGR, bgr);
            
            // Verify red characteristics
            ColorSchemaBGR result = (ColorSchemaBGR) redCluster.getSchema(ColorCluster.ColorSchemaName.BGR);
            assert result.getColorInfos().get(ColorSchema.ColorValue.RED).getStat(ColorInfo.ColorStat.MEAN) > 200;
            assert result.getColorInfos().get(ColorSchema.ColorValue.BLUE).getStat(ColorInfo.ColorStat.MAX) <= 50;
            assert result.getColorInfos().get(ColorSchema.ColorValue.GREEN).getStat(ColorInfo.ColorStat.MAX) <= 50;
            
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