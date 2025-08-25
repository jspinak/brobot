package io.github.jspinak.brobot.model.analysis.color;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.*;

/**
 * Comprehensive test suite for ColorCluster.
 * Tests color profile management across multiple color spaces.
 */
@DisplayName("ColorCluster Tests")
public class ColorClusterTest extends BrobotTestBase {
    
    private ColorCluster colorCluster;
    private ColorSchemaBGR bgrSchema;
    private ColorSchemaHSV hsvSchema;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        colorCluster = new ColorCluster();
        bgrSchema = new ColorSchemaBGR();
        hsvSchema = new ColorSchemaHSV();
    }
    
    @Nested
    @DisplayName("Constructor and Initialization")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create empty color cluster")
        void shouldCreateEmptyColorCluster() {
            assertNotNull(colorCluster);
            assertNotNull(colorCluster.getColorSchemas());
            assertTrue(colorCluster.getColorSchemas().isEmpty());
        }
        
        @Test
        @DisplayName("Should initialize with empty schema map")
        void shouldInitializeWithEmptyMap() {
            assertEquals(0, colorCluster.getColorSchemas().size());
            assertNull(colorCluster.getSchema(BGR));
            assertNull(colorCluster.getSchema(HSV));
        }
    }
    
    @Nested
    @DisplayName("Schema Management")
    class SchemaManagement {
        
        @Test
        @DisplayName("Should add BGR schema")
        void shouldAddBGRSchema() {
            colorCluster.setSchema(BGR, bgrSchema);
            
            assertNotNull(colorCluster.getSchema(BGR));
            assertEquals(bgrSchema, colorCluster.getSchema(BGR));
            assertEquals(1, colorCluster.getColorSchemas().size());
        }
        
        @Test
        @DisplayName("Should add HSV schema")
        void shouldAddHSVSchema() {
            colorCluster.setSchema(HSV, hsvSchema);
            
            assertNotNull(colorCluster.getSchema(HSV));
            assertEquals(hsvSchema, colorCluster.getSchema(HSV));
            assertEquals(1, colorCluster.getColorSchemas().size());
        }
        
        @Test
        @DisplayName("Should store both BGR and HSV schemas")
        void shouldStoreBothSchemas() {
            colorCluster.setSchema(BGR, bgrSchema);
            colorCluster.setSchema(HSV, hsvSchema);
            
            assertEquals(2, colorCluster.getColorSchemas().size());
            assertNotNull(colorCluster.getSchema(BGR));
            assertNotNull(colorCluster.getSchema(HSV));
            assertEquals(bgrSchema, colorCluster.getSchema(BGR));
            assertEquals(hsvSchema, colorCluster.getSchema(HSV));
        }
        
        @Test
        @DisplayName("Should replace existing schema")
        void shouldReplaceExistingSchema() {
            ColorSchemaBGR firstBGR = new ColorSchemaBGR();
            ColorSchemaBGR secondBGR = new ColorSchemaBGR();
            
            colorCluster.setSchema(BGR, firstBGR);
            assertEquals(firstBGR, colorCluster.getSchema(BGR));
            
            colorCluster.setSchema(BGR, secondBGR);
            assertEquals(secondBGR, colorCluster.getSchema(BGR));
            assertNotEquals(firstBGR, colorCluster.getSchema(BGR));
            assertEquals(1, colorCluster.getColorSchemas().size());
        }
        
        @Test
        @DisplayName("Should handle null schema")
        void shouldHandleNullSchema() {
            colorCluster.setSchema(BGR, null);
            
            assertNull(colorCluster.getSchema(BGR));
            assertTrue(colorCluster.getColorSchemas().containsKey(BGR));
        }
        
        @ParameterizedTest
        @EnumSource(ColorCluster.ColorSchemaName.class)
        @DisplayName("Should retrieve schemas by name")
        void shouldRetrieveSchemasByName(ColorCluster.ColorSchemaName schemaName) {
            ColorSchema schema = (schemaName == BGR) ? bgrSchema : hsvSchema;
            colorCluster.setSchema(schemaName, schema);
            
            assertEquals(schema, colorCluster.getSchema(schemaName));
        }
    }
    
    @Nested
    @DisplayName("Schema Map Operations")
    class SchemaMapOperations {
        
        @Test
        @DisplayName("Should get mutable schema map")
        void shouldGetMutableSchemaMap() {
            Map<ColorCluster.ColorSchemaName, ColorSchema> schemas = colorCluster.getColorSchemas();
            
            schemas.put(BGR, bgrSchema);
            assertEquals(bgrSchema, colorCluster.getSchema(BGR));
        }
        
        @Test
        @DisplayName("Should set entire schema map")
        void shouldSetEntireSchemaMap() {
            Map<ColorCluster.ColorSchemaName, ColorSchema> newSchemas = Map.of(
                BGR, bgrSchema,
                HSV, hsvSchema
            );
            
            colorCluster.setColorSchemas(newSchemas);
            
            assertEquals(2, colorCluster.getColorSchemas().size());
            assertEquals(bgrSchema, colorCluster.getSchema(BGR));
            assertEquals(hsvSchema, colorCluster.getSchema(HSV));
        }
        
        @Test
        @DisplayName("Should clear all schemas")
        void shouldClearAllSchemas() {
            colorCluster.setSchema(BGR, bgrSchema);
            colorCluster.setSchema(HSV, hsvSchema);
            assertEquals(2, colorCluster.getColorSchemas().size());
            
            colorCluster.getColorSchemas().clear();
            
            assertEquals(0, colorCluster.getColorSchemas().size());
            assertNull(colorCluster.getSchema(BGR));
            assertNull(colorCluster.getSchema(HSV));
        }
    }
    
    @Nested
    @DisplayName("Color Schema Integration")
    class ColorSchemaIntegration {
        
        @Test
        @DisplayName("Should integrate with BGR schema")
        void shouldIntegrateWithBGRSchema() {
            // Set up BGR schema with color info
            ColorInfo blue = new ColorInfo(ColorSchema.ColorValue.BLUE);
            blue.setAll(10.0, 50.0, 30.0, 10.0);
            bgrSchema.getColorInfos().put(ColorSchema.ColorValue.BLUE, blue);
            
            ColorInfo green = new ColorInfo(ColorSchema.ColorValue.GREEN);
            green.setAll(100.0, 150.0, 125.0, 15.0);
            bgrSchema.getColorInfos().put(ColorSchema.ColorValue.GREEN, green);
            
            ColorInfo red = new ColorInfo(ColorSchema.ColorValue.RED);
            red.setAll(200.0, 255.0, 230.0, 20.0);
            bgrSchema.getColorInfos().put(ColorSchema.ColorValue.RED, red);
            
            colorCluster.setSchema(BGR, bgrSchema);
            
            ColorSchemaBGR retrievedBGR = (ColorSchemaBGR) colorCluster.getSchema(BGR);
            assertNotNull(retrievedBGR);
            assertEquals(3, retrievedBGR.getColorInfos().size());
            assertNotNull(retrievedBGR.getColorInfos().get(ColorSchema.ColorValue.BLUE));
            assertNotNull(retrievedBGR.getColorInfos().get(ColorSchema.ColorValue.GREEN));
            assertNotNull(retrievedBGR.getColorInfos().get(ColorSchema.ColorValue.RED));
        }
        
        @Test
        @DisplayName("Should integrate with HSV schema")
        void shouldIntegrateWithHSVSchema() {
            // Set up HSV schema with color info
            ColorInfo hue = new ColorInfo(ColorSchema.ColorValue.HUE);
            hue.setAll(0.0, 179.0, 90.0, 30.0);
            hsvSchema.getColorInfos().put(ColorSchema.ColorValue.HUE, hue);
            
            ColorInfo saturation = new ColorInfo(ColorSchema.ColorValue.SATURATION);
            saturation.setAll(100.0, 255.0, 200.0, 25.0);
            hsvSchema.getColorInfos().put(ColorSchema.ColorValue.SATURATION, saturation);
            
            ColorInfo value = new ColorInfo(ColorSchema.ColorValue.VALUE);
            value.setAll(50.0, 255.0, 150.0, 40.0);
            hsvSchema.getColorInfos().put(ColorSchema.ColorValue.VALUE, value);
            
            colorCluster.setSchema(HSV, hsvSchema);
            
            ColorSchemaHSV retrievedHSV = (ColorSchemaHSV) colorCluster.getSchema(HSV);
            assertNotNull(retrievedHSV);
            assertEquals(3, retrievedHSV.getColorInfos().size());
            assertNotNull(retrievedHSV.getColorInfos().get(ColorSchema.ColorValue.HUE));
            assertNotNull(retrievedHSV.getColorInfos().get(ColorSchema.ColorValue.SATURATION));
            assertNotNull(retrievedHSV.getColorInfos().get(ColorSchema.ColorValue.VALUE));
        }
    }
    
    @Nested
    @DisplayName("Use Cases")
    class UseCases {
        
        @Test
        @DisplayName("Should create red color cluster")
        void shouldCreateRedColorCluster() {
            // BGR: High red, low blue and green
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
            
            // HSV: Hue around 0 (red), high saturation
            ColorSchemaHSV hsv = new ColorSchemaHSV();
            ColorInfo hue = new ColorInfo(ColorSchema.ColorValue.HUE);
            hue.setAll(0.0, 10.0, 5.0, 3.0); // Red hue range
            ColorInfo saturation = new ColorInfo(ColorSchema.ColorValue.SATURATION);
            saturation.setAll(200.0, 255.0, 230.0, 15.0); // High saturation
            ColorInfo value = new ColorInfo(ColorSchema.ColorValue.VALUE);
            value.setAll(200.0, 255.0, 230.0, 15.0); // High value
            
            hsv.getColorInfos().put(ColorSchema.ColorValue.HUE, hue);
            hsv.getColorInfos().put(ColorSchema.ColorValue.SATURATION, saturation);
            hsv.getColorInfos().put(ColorSchema.ColorValue.VALUE, value);
            
            colorCluster.setSchema(BGR, bgr);
            colorCluster.setSchema(HSV, hsv);
            
            // Verify red characteristics in both color spaces
            ColorSchemaBGR bgrResult = (ColorSchemaBGR) colorCluster.getSchema(BGR);
            assertTrue(bgrResult.getColorInfos().get(ColorSchema.ColorValue.RED).getStat(ColorInfo.ColorStat.MEAN) > 200);
            assertTrue(bgrResult.getColorInfos().get(ColorSchema.ColorValue.BLUE).getStat(ColorInfo.ColorStat.MAX) < 100);
            
            ColorSchemaHSV hsvResult = (ColorSchemaHSV) colorCluster.getSchema(HSV);
            assertTrue(hsvResult.getColorInfos().get(ColorSchema.ColorValue.HUE).getStat(ColorInfo.ColorStat.MAX) < 20);
            assertTrue(hsvResult.getColorInfos().get(ColorSchema.ColorValue.SATURATION).getStat(ColorInfo.ColorStat.MEAN) > 200);
        }
        
        @Test
        @DisplayName("Should create grayscale color cluster")
        void shouldCreateGrayscaleColorCluster() {
            // For grayscale, BGR values are similar
            ColorSchemaBGR bgr = new ColorSchemaBGR();
            double grayLevel = 128.0;
            double tolerance = 5.0;
            
            for (ColorSchema.ColorValue channel : new ColorSchema.ColorValue[]{
                ColorSchema.ColorValue.BLUE, 
                ColorSchema.ColorValue.GREEN, 
                ColorSchema.ColorValue.RED}) {
                
                ColorInfo info = new ColorInfo(channel);
                info.setAll(grayLevel - tolerance, grayLevel + tolerance, grayLevel, 2.0);
                bgr.getColorInfos().put(channel, info);
            }
            
            // HSV: Low saturation for grayscale
            ColorSchemaHSV hsv = new ColorSchemaHSV();
            ColorInfo saturation = new ColorInfo(ColorSchema.ColorValue.SATURATION);
            saturation.setAll(0.0, 20.0, 10.0, 5.0); // Very low saturation
            hsv.getColorInfos().put(ColorSchema.ColorValue.SATURATION, saturation);
            
            colorCluster.setSchema(BGR, bgr);
            colorCluster.setSchema(HSV, hsv);
            
            // Verify grayscale characteristics
            ColorSchemaBGR bgrResult = (ColorSchemaBGR) colorCluster.getSchema(BGR);
            double blueMean = bgrResult.getColorInfos().get(ColorSchema.ColorValue.BLUE).getStat(ColorInfo.ColorStat.MEAN);
            double greenMean = bgrResult.getColorInfos().get(ColorSchema.ColorValue.GREEN).getStat(ColorInfo.ColorStat.MEAN);
            double redMean = bgrResult.getColorInfos().get(ColorSchema.ColorValue.RED).getStat(ColorInfo.ColorStat.MEAN);
            
            assertEquals(blueMean, greenMean, 1.0);
            assertEquals(greenMean, redMean, 1.0);
            
            ColorSchemaHSV hsvResult = (ColorSchemaHSV) colorCluster.getSchema(HSV);
            if (hsvResult.getColorInfos().get(ColorSchema.ColorValue.SATURATION) != null) {
                assertTrue(hsvResult.getColorInfos().get(ColorSchema.ColorValue.SATURATION).getStat(ColorInfo.ColorStat.MAX) < 30);
            }
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle empty schemas")
        void shouldHandleEmptySchemas() {
            ColorSchemaBGR emptyBGR = new ColorSchemaBGR();
            colorCluster.setSchema(BGR, emptyBGR);
            
            assertNotNull(colorCluster.getSchema(BGR));
            // ColorSchemaBGR constructor creates 3 ColorInfo objects (BLUE, GREEN, RED)
            assertEquals(3, ((ColorSchemaBGR) colorCluster.getSchema(BGR)).getColorInfos().size());
        }
        
        @Test
        @DisplayName("Should handle missing schema retrieval")
        void shouldHandleMissingSchemaRetrieval() {
            colorCluster.setSchema(BGR, bgrSchema);
            
            assertNull(colorCluster.getSchema(HSV));
            assertNotNull(colorCluster.getSchema(BGR));
        }
        
        @Test
        @DisplayName("Should handle null schema name")
        void shouldHandleNullSchemaName() {
            assertNull(colorCluster.getSchema(null));
        }
    }
}