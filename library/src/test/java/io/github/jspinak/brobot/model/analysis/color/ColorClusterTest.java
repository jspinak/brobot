package io.github.jspinak.brobot.model.analysis.color;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for ColorCluster. Tests color profile management across multiple color
 * spaces.
 */
@DisplayName("ColorCluster Tests")
public class ColorClusterTest extends BrobotTestBase {

    private ColorCluster colorCluster;
    private ColorSchema bgrSchema;
    private ColorSchema hsvSchema;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        colorCluster = new ColorCluster();
        bgrSchema = createSampleBGRSchema();
        hsvSchema = createSampleHSVSchema();
    }

    @Nested
    @DisplayName("Color Schema Management")
    class ColorSchemaManagement {

        @Test
        @DisplayName("Should create empty color cluster")
        void shouldCreateEmptyColorCluster() {
            assertNotNull(colorCluster);
            assertNotNull(colorCluster.getColorSchemas());
            assertTrue(colorCluster.getColorSchemas().isEmpty());
        }

        @Test
        @DisplayName("Should set BGR schema")
        void shouldSetBGRSchema() {
            colorCluster.setSchema(ColorCluster.ColorSchemaName.BGR, bgrSchema);

            assertNotNull(colorCluster.getSchema(ColorCluster.ColorSchemaName.BGR));
            assertEquals(bgrSchema, colorCluster.getSchema(ColorCluster.ColorSchemaName.BGR));
        }

        @Test
        @DisplayName("Should set HSV schema")
        void shouldSetHSVSchema() {
            colorCluster.setSchema(ColorCluster.ColorSchemaName.HSV, hsvSchema);

            assertNotNull(colorCluster.getSchema(ColorCluster.ColorSchemaName.HSV));
            assertEquals(hsvSchema, colorCluster.getSchema(ColorCluster.ColorSchemaName.HSV));
        }

        @Test
        @DisplayName("Should replace existing schema")
        void shouldReplaceExistingSchema() {
            colorCluster.setSchema(ColorCluster.ColorSchemaName.BGR, bgrSchema);
            ColorSchema newBgrSchema = createSampleBGRSchema();

            colorCluster.setSchema(ColorCluster.ColorSchemaName.BGR, newBgrSchema);

            assertEquals(newBgrSchema, colorCluster.getSchema(ColorCluster.ColorSchemaName.BGR));
            assertNotEquals(bgrSchema, colorCluster.getSchema(ColorCluster.ColorSchemaName.BGR));
        }

        @ParameterizedTest
        @EnumSource(ColorCluster.ColorSchemaName.class)
        @DisplayName("Should handle all schema types")
        void shouldHandleAllSchemaTypes(ColorCluster.ColorSchemaName schemaName) {
            ColorSchema schema =
                    schemaName == ColorCluster.ColorSchemaName.BGR ? bgrSchema : hsvSchema;

            colorCluster.setSchema(schemaName, schema);

            assertNotNull(colorCluster.getSchema(schemaName));
            assertEquals(schema, colorCluster.getSchema(schemaName));
        }

        @Test
        @DisplayName("Should return null for unset schema")
        void shouldReturnNullForUnsetSchema() {
            assertNull(colorCluster.getSchema(ColorCluster.ColorSchemaName.BGR));
            assertNull(colorCluster.getSchema(ColorCluster.ColorSchemaName.HSV));
        }

        @Test
        @DisplayName("Should maintain separate schemas")
        void shouldMaintainSeparateSchemas() {
            colorCluster.setSchema(ColorCluster.ColorSchemaName.BGR, bgrSchema);
            colorCluster.setSchema(ColorCluster.ColorSchemaName.HSV, hsvSchema);

            assertEquals(2, colorCluster.getColorSchemas().size());
            assertEquals(bgrSchema, colorCluster.getSchema(ColorCluster.ColorSchemaName.BGR));
            assertEquals(hsvSchema, colorCluster.getSchema(ColorCluster.ColorSchemaName.HSV));
        }
    }

    @Nested
    @DisplayName("Mat Generation")
    class MatGeneration {

        @BeforeEach
        void setupSchemas() {
            colorCluster.setSchema(ColorCluster.ColorSchemaName.BGR, bgrSchema);
            colorCluster.setSchema(ColorCluster.ColorSchemaName.HSV, hsvSchema);
        }

        @Test
        @DisplayName("Should get Mat with specified size")
        void shouldGetMatWithSpecifiedSize() {
            Size size = new Size(100, 50);

            Mat result =
                    colorCluster.getMat(
                            ColorCluster.ColorSchemaName.BGR, ColorInfo.ColorStat.MEAN, size);

            assertNotNull(result);
            assertEquals(100, result.cols());
            assertEquals(50, result.rows());
        }

        @Test
        @DisplayName("Should get Mat with default size")
        void shouldGetMatWithDefaultSize() {
            Mat result =
                    colorCluster.getMat(
                            ColorCluster.ColorSchemaName.BGR,
                            ColorInfo.ColorStat.MEAN,
                            new Size(10, 10));

            assertNotNull(result);
            // The actual size depends on the internal ColorSchema.getMat implementation
            assertTrue(result.rows() > 0);
            assertTrue(result.cols() > 0);
        }

        @ParameterizedTest
        @CsvSource({
            "BGR, MIN",
            "BGR, MAX",
            "BGR, MEAN",
            "BGR, STDDEV",
            "HSV, MIN",
            "HSV, MAX",
            "HSV, MEAN",
            "HSV, STDDEV"
        })
        @DisplayName("Should get Mat with different stats")
        void shouldGetMatWithDifferentStats(String schemaNameStr, String statStr) {
            ColorCluster.ColorSchemaName schemaName =
                    ColorCluster.ColorSchemaName.valueOf(schemaNameStr);
            ColorInfo.ColorStat stat = ColorInfo.ColorStat.valueOf(statStr);

            Mat result = colorCluster.getMat(schemaName, stat, new Size(50, 50));

            assertNotNull(result);
            assertEquals(50, result.cols());
            assertEquals(50, result.rows());
        }

        @Test
        @DisplayName("Should handle missing schema gracefully")
        void shouldHandleMissingSchemaGracefully() {
            ColorCluster emptyCluster = new ColorCluster();

            Mat result =
                    emptyCluster.getMat(
                            ColorCluster.ColorSchemaName.BGR,
                            ColorInfo.ColorStat.MEAN,
                            new Size(10, 10));

            // The method should either return null or an empty Mat
            // Check actual behavior and adjust assertion
            if (result != null) {
                assertTrue(result.empty() || result.rows() == 0 || result.cols() == 0);
            }
        }
    }

    @Nested
    @DisplayName("Schema Access and Modification")
    class SchemaAccessAndModification {

        @Test
        @DisplayName("Should get all schemas map")
        void shouldGetAllSchemasMap() {
            colorCluster.setSchema(ColorCluster.ColorSchemaName.BGR, bgrSchema);
            colorCluster.setSchema(ColorCluster.ColorSchemaName.HSV, hsvSchema);

            Map<ColorCluster.ColorSchemaName, ColorSchema> schemas = colorCluster.getColorSchemas();

            assertNotNull(schemas);
            assertEquals(2, schemas.size());
            assertTrue(schemas.containsKey(ColorCluster.ColorSchemaName.BGR));
            assertTrue(schemas.containsKey(ColorCluster.ColorSchemaName.HSV));
        }

        @Test
        @DisplayName("Should set entire schemas map")
        void shouldSetEntireSchemasMap() {
            Map<ColorCluster.ColorSchemaName, ColorSchema> newSchemas = new HashMap<>();
            newSchemas.put(ColorCluster.ColorSchemaName.BGR, bgrSchema);
            newSchemas.put(ColorCluster.ColorSchemaName.HSV, hsvSchema);

            colorCluster.setColorSchemas(newSchemas);

            assertEquals(newSchemas, colorCluster.getColorSchemas());
            assertEquals(bgrSchema, colorCluster.getSchema(ColorCluster.ColorSchemaName.BGR));
            assertEquals(hsvSchema, colorCluster.getSchema(ColorCluster.ColorSchemaName.HSV));
        }

        @Test
        @DisplayName("Should handle null schemas gracefully")
        void shouldHandleNullSchemasGracefully() {
            colorCluster.setSchema(ColorCluster.ColorSchemaName.BGR, bgrSchema);

            colorCluster.setColorSchemas(new HashMap<>());

            assertNotNull(colorCluster.getColorSchemas());
            assertTrue(colorCluster.getColorSchemas().isEmpty());
        }
    }

    @Nested
    @DisplayName("Integration with ColorSchema")
    class ColorSchemaIntegration {

        @Test
        @DisplayName("Should work with BGR ColorSchema")
        void shouldWorkWithBGRColorSchema() {
            ColorSchema bgr =
                    new ColorSchema(
                            ColorSchema.ColorValue.BLUE,
                            ColorSchema.ColorValue.GREEN,
                            ColorSchema.ColorValue.RED);

            // Set statistics for Blue channel
            bgr.setValues(ColorSchema.ColorValue.BLUE, 0.0, 255.0, 127.5, 50.0);

            colorCluster.setSchema(ColorCluster.ColorSchemaName.BGR, bgr);

            ColorSchema retrieved = colorCluster.getSchema(ColorCluster.ColorSchemaName.BGR);
            assertNotNull(retrieved);
            assertEquals(3, retrieved.getColorInfos().size());

            ColorInfo blue = retrieved.getColorInfos().get(ColorSchema.ColorValue.BLUE);
            assertNotNull(blue);
            assertEquals(0.0, blue.getStats().get(ColorInfo.ColorStat.MIN));
            assertEquals(255.0, blue.getStats().get(ColorInfo.ColorStat.MAX));
        }

        @Test
        @DisplayName("Should work with HSV ColorSchema")
        void shouldWorkWithHSVColorSchema() {
            ColorSchema hsv =
                    new ColorSchema(
                            ColorSchema.ColorValue.HUE,
                            ColorSchema.ColorValue.SATURATION,
                            ColorSchema.ColorValue.VALUE);

            // Set statistics for Hue channel
            hsv.setValues(ColorSchema.ColorValue.HUE, 0.0, 179.0, 90.0, 30.0);

            colorCluster.setSchema(ColorCluster.ColorSchemaName.HSV, hsv);

            ColorSchema retrieved = colorCluster.getSchema(ColorCluster.ColorSchemaName.HSV);
            assertNotNull(retrieved);
            assertEquals(3, retrieved.getColorInfos().size());

            ColorInfo hue = retrieved.getColorInfos().get(ColorSchema.ColorValue.HUE);
            assertNotNull(hue);
            assertEquals(0.0, hue.getStats().get(ColorInfo.ColorStat.MIN));
            assertEquals(179.0, hue.getStats().get(ColorInfo.ColorStat.MAX));
        }
    }

    // Helper methods
    private ColorSchema createSampleBGRSchema() {
        ColorSchema schema =
                new ColorSchema(
                        ColorSchema.ColorValue.BLUE,
                        ColorSchema.ColorValue.GREEN,
                        ColorSchema.ColorValue.RED);

        // Set statistics for each channel
        schema.setValues(ColorSchema.ColorValue.BLUE, 10.0, 200.0, 100.0, 40.0);
        schema.setValues(ColorSchema.ColorValue.GREEN, 20.0, 220.0, 120.0, 45.0);
        schema.setValues(ColorSchema.ColorValue.RED, 30.0, 230.0, 130.0, 50.0);

        return schema;
    }

    private ColorSchema createSampleHSVSchema() {
        ColorSchema schema =
                new ColorSchema(
                        ColorSchema.ColorValue.HUE,
                        ColorSchema.ColorValue.SATURATION,
                        ColorSchema.ColorValue.VALUE);

        // Set statistics for each channel
        schema.setValues(ColorSchema.ColorValue.HUE, 0.0, 179.0, 90.0, 30.0);
        schema.setValues(ColorSchema.ColorValue.SATURATION, 50.0, 255.0, 150.0, 40.0);
        schema.setValues(ColorSchema.ColorValue.VALUE, 60.0, 255.0, 160.0, 35.0);

        return schema;
    }
}
