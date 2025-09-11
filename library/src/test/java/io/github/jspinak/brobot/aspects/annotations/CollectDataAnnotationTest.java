package io.github.jspinak.brobot.aspects.annotations;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Documented;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.*;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.TestCategories;

/**
 * Comprehensive test suite for @CollectData annotation. Tests data collection configuration,
 * sampling, feature selection, and format options.
 */
@DisplayName("@CollectData Annotation Tests")
@Tag(TestCategories.UNIT)
@Tag(TestCategories.FAST)
@Tag("annotations")
@Tag("aspects")
@Tag("ml")
public class CollectDataAnnotationTest extends BrobotTestBase {

    // Test classes with various @CollectData configurations
    static class DataCollectionTestClass {

        @CollectData
        public void simpleCollectionMethod() {}

        @CollectData(category = "click_accuracy", captureScreenshots = true)
        public void clickDataMethod() {}

        @CollectData(
                category = "text_recognition",
                features = {"image", "location", "confidence"},
                samplingRate = 0.1)
        public String textExtractionMethod() {
            return "text";
        }

        @CollectData(
                category = "complex_action",
                features = {"input", "output", "timing", "context"},
                captureScreenshots = true,
                captureIntermediateStates = true,
                samplingRate = 0.5,
                maxSamples = 1000,
                onlySuccess = true,
                includeTiming = true,
                anonymize = false,
                format = CollectData.DataFormat.PARQUET,
                labels = {"success", "confidence_high"},
                compress = false)
        public boolean complexDataMethod(String input) {
            return true;
        }

        @CollectData(captureScreenshots = false)
        public void noScreenshotsMethod() {}

        @CollectData(captureIntermediateStates = true)
        public void intermediateStatesMethod() {}

        @CollectData(samplingRate = 0.0)
        public void neverSampleMethod() {}

        @CollectData(samplingRate = 1.0)
        public void alwaysSampleMethod() {}

        @CollectData(maxSamples = 100)
        public void limitedSamplesMethod() {}

        @CollectData(onlySuccess = true)
        public void successOnlyMethod() {}

        @CollectData(includeTiming = false)
        public void noTimingMethod() {}

        @CollectData(anonymize = false)
        public void nonAnonymizedMethod() {}

        @CollectData(format = CollectData.DataFormat.JSON)
        public void jsonFormatMethod() {}

        @CollectData(format = CollectData.DataFormat.CSV)
        public void csvFormatMethod() {}

        @CollectData(format = CollectData.DataFormat.BINARY)
        public void binaryFormatMethod() {}

        @CollectData(format = CollectData.DataFormat.TFRECORD)
        public void tfrecordFormatMethod() {}

        @CollectData(labels = {"label1", "label2", "label3"})
        public void labeledDataMethod() {}

        @CollectData(compress = false)
        public void uncompressedDataMethod() {}

        @CollectData(features = {})
        public void allFeaturesMethod() {}

        public void unmonitoredMethod() {}
    }

    @BeforeEach
    public void setupTest() {
        super.setupTest();
    }

    @Nested
    @DisplayName("Annotation Presence Tests")
    class AnnotationPresenceTests {

        @Test
        @DisplayName("Should detect @CollectData on methods")
        void shouldDetectCollectDataOnMethods() throws NoSuchMethodException {
            Method simpleMethod = DataCollectionTestClass.class.getMethod("simpleCollectionMethod");
            Method clickMethod = DataCollectionTestClass.class.getMethod("clickDataMethod");
            Method unmonitoredMethod = DataCollectionTestClass.class.getMethod("unmonitoredMethod");

            assertTrue(simpleMethod.isAnnotationPresent(CollectData.class));
            assertTrue(clickMethod.isAnnotationPresent(CollectData.class));
            assertFalse(unmonitoredMethod.isAnnotationPresent(CollectData.class));
        }

        @Test
        @DisplayName("Should have runtime retention")
        void shouldHaveRuntimeRetention() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("simpleCollectionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);
            assertNotNull(annotation);
        }

        @Test
        @DisplayName("Should be documented")
        void shouldBeDocumented() {
            assertTrue(CollectData.class.isAnnotationPresent(Documented.class));
        }

        @Test
        @DisplayName("Should target methods only")
        void shouldTargetMethodsOnly() {
            // CollectData should not be present on the class itself
            assertFalse(DataCollectionTestClass.class.isAnnotationPresent(CollectData.class));
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should have correct default values")
        void shouldHaveCorrectDefaultValues() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("simpleCollectionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertEquals("general", annotation.category());
            assertArrayEquals(new String[] {}, annotation.features());
            assertTrue(annotation.captureScreenshots());
            assertFalse(annotation.captureIntermediateStates());
            assertEquals(1.0, annotation.samplingRate(), 0.001);
            assertEquals(-1, annotation.maxSamples());
            assertFalse(annotation.onlySuccess());
            assertTrue(annotation.includeTiming());
            assertTrue(annotation.anonymize());
            assertEquals(CollectData.DataFormat.JSON, annotation.format());
            assertArrayEquals(new String[] {}, annotation.labels());
            assertTrue(annotation.compress());
        }
    }

    @Nested
    @DisplayName("Category and Features Tests")
    class CategoryAndFeaturesTests {

        @Test
        @DisplayName("Should read custom category")
        void shouldReadCustomCategory() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("clickDataMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertEquals("click_accuracy", annotation.category());
        }

        @Test
        @DisplayName("Should read feature selection")
        void shouldReadFeatureSelection() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("textExtractionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            String[] features = annotation.features();
            assertEquals(3, features.length);
            assertTrue(Arrays.asList(features).contains("image"));
            assertTrue(Arrays.asList(features).contains("location"));
            assertTrue(Arrays.asList(features).contains("confidence"));
        }

        @Test
        @DisplayName("Should handle empty features array")
        void shouldHandleEmptyFeaturesArray() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("allFeaturesMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            String[] features = annotation.features();
            assertNotNull(features);
            assertEquals(0, features.length);
            // Empty array means collect all available features
        }
    }

    @Nested
    @DisplayName("Screenshot Capture Tests")
    class ScreenshotCaptureTests {

        @Test
        @DisplayName("Should enable screenshots by default")
        void shouldEnableScreenshotsByDefault() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("simpleCollectionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertTrue(annotation.captureScreenshots());
        }

        @Test
        @DisplayName("Should disable screenshots when specified")
        void shouldDisableScreenshots() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("noScreenshotsMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertFalse(annotation.captureScreenshots());
        }

        @Test
        @DisplayName("Should support intermediate state capture")
        void shouldSupportIntermediateStateCapture() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("intermediateStatesMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertTrue(annotation.captureIntermediateStates());
        }
    }

    @Nested
    @DisplayName("Sampling Configuration Tests")
    class SamplingConfigurationTests {

        @Test
        @DisplayName("Should support partial sampling")
        void shouldSupportPartialSampling() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("textExtractionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertEquals(0.1, annotation.samplingRate(), 0.001);
        }

        @Test
        @DisplayName("Should support no sampling")
        void shouldSupportNoSampling() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("neverSampleMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertEquals(0.0, annotation.samplingRate(), 0.001);
        }

        @Test
        @DisplayName("Should support full sampling")
        void shouldSupportFullSampling() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("alwaysSampleMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertEquals(1.0, annotation.samplingRate(), 0.001);
        }

        @Test
        @DisplayName("Should support sample limit")
        void shouldSupportSampleLimit() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("limitedSamplesMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertEquals(100, annotation.maxSamples());
        }

        @Test
        @DisplayName("Should default to unlimited samples")
        void shouldDefaultToUnlimitedSamples() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("simpleCollectionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertEquals(-1, annotation.maxSamples());
        }
    }

    @Nested
    @DisplayName("Data Collection Filters Tests")
    class DataCollectionFiltersTests {

        @Test
        @DisplayName("Should support success-only filtering")
        void shouldSupportSuccessOnlyFiltering() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("successOnlyMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertTrue(annotation.onlySuccess());
        }

        @Test
        @DisplayName("Should collect all executions by default")
        void shouldCollectAllByDefault() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("simpleCollectionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertFalse(annotation.onlySuccess());
        }
    }

    @Nested
    @DisplayName("Metadata Collection Tests")
    class MetadataCollectionTests {

        @Test
        @DisplayName("Should include timing by default")
        void shouldIncludeTimingByDefault() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("simpleCollectionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertTrue(annotation.includeTiming());
        }

        @Test
        @DisplayName("Should allow disabling timing")
        void shouldAllowDisablingTiming() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("noTimingMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertFalse(annotation.includeTiming());
        }
    }

    @Nested
    @DisplayName("Privacy and Security Tests")
    class PrivacyAndSecurityTests {

        @Test
        @DisplayName("Should anonymize by default")
        void shouldAnonymizeByDefault() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("simpleCollectionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertTrue(annotation.anonymize());
        }

        @Test
        @DisplayName("Should allow non-anonymized collection")
        void shouldAllowNonAnonymizedCollection() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("nonAnonymizedMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertFalse(annotation.anonymize());
        }
    }

    @Nested
    @DisplayName("Data Format Tests")
    class DataFormatTests {

        @Test
        @DisplayName("Should default to JSON format")
        void shouldDefaultToJsonFormat() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("simpleCollectionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertEquals(CollectData.DataFormat.JSON, annotation.format());
        }

        @Test
        @DisplayName("Should support CSV format")
        void shouldSupportCsvFormat() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("csvFormatMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertEquals(CollectData.DataFormat.CSV, annotation.format());
        }

        @Test
        @DisplayName("Should support binary format")
        void shouldSupportBinaryFormat() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("binaryFormatMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertEquals(CollectData.DataFormat.BINARY, annotation.format());
        }

        @Test
        @DisplayName("Should support TFRecord format")
        void shouldSupportTfrecordFormat() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("tfrecordFormatMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertEquals(CollectData.DataFormat.TFRECORD, annotation.format());
        }

        @Test
        @DisplayName("Should support Parquet format")
        void shouldSupportParquetFormat() throws NoSuchMethodException {
            Method method =
                    DataCollectionTestClass.class.getMethod("complexDataMethod", String.class);
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertEquals(CollectData.DataFormat.PARQUET, annotation.format());
        }

        @Test
        @DisplayName("Should have all format enum values")
        void shouldHaveAllFormatEnumValues() {
            CollectData.DataFormat[] formats = CollectData.DataFormat.values();

            assertEquals(5, formats.length);
            assertTrue(Arrays.asList(formats).contains(CollectData.DataFormat.JSON));
            assertTrue(Arrays.asList(formats).contains(CollectData.DataFormat.CSV));
            assertTrue(Arrays.asList(formats).contains(CollectData.DataFormat.BINARY));
            assertTrue(Arrays.asList(formats).contains(CollectData.DataFormat.TFRECORD));
            assertTrue(Arrays.asList(formats).contains(CollectData.DataFormat.PARQUET));
        }
    }

    @Nested
    @DisplayName("Labeling and Compression Tests")
    class LabelingAndCompressionTests {

        @Test
        @DisplayName("Should support multiple labels")
        void shouldSupportMultipleLabels() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("labeledDataMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            String[] labels = annotation.labels();
            assertEquals(3, labels.length);
            assertTrue(Arrays.asList(labels).contains("label1"));
            assertTrue(Arrays.asList(labels).contains("label2"));
            assertTrue(Arrays.asList(labels).contains("label3"));
        }

        @Test
        @DisplayName("Should have empty labels by default")
        void shouldHaveEmptyLabelsByDefault() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("simpleCollectionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            String[] labels = annotation.labels();
            assertNotNull(labels);
            assertEquals(0, labels.length);
        }

        @Test
        @DisplayName("Should compress by default")
        void shouldCompressByDefault() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("simpleCollectionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertTrue(annotation.compress());
        }

        @Test
        @DisplayName("Should allow disabling compression")
        void shouldAllowDisablingCompression() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("uncompressedDataMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            assertFalse(annotation.compress());
        }
    }

    @Nested
    @DisplayName("Complex Configuration Tests")
    class ComplexConfigurationTests {

        @Test
        @DisplayName("Should read all complex configuration")
        void shouldReadAllComplexConfiguration() throws NoSuchMethodException {
            Method method =
                    DataCollectionTestClass.class.getMethod("complexDataMethod", String.class);
            CollectData annotation = method.getAnnotation(CollectData.class);

            // Verify all attributes
            assertEquals("complex_action", annotation.category());

            String[] features = annotation.features();
            assertEquals(4, features.length);
            assertTrue(Arrays.asList(features).contains("input"));
            assertTrue(Arrays.asList(features).contains("output"));
            assertTrue(Arrays.asList(features).contains("timing"));
            assertTrue(Arrays.asList(features).contains("context"));

            assertTrue(annotation.captureScreenshots());
            assertTrue(annotation.captureIntermediateStates());
            assertEquals(0.5, annotation.samplingRate(), 0.001);
            assertEquals(1000, annotation.maxSamples());
            assertTrue(annotation.onlySuccess());
            assertTrue(annotation.includeTiming());
            assertFalse(annotation.anonymize());
            assertEquals(CollectData.DataFormat.PARQUET, annotation.format());

            String[] labels = annotation.labels();
            assertEquals(2, labels.length);
            assertTrue(Arrays.asList(labels).contains("success"));
            assertTrue(Arrays.asList(labels).contains("confidence_high"));

            assertFalse(annotation.compress());
        }
    }

    @Nested
    @DisplayName("Annotation Metadata Tests")
    class AnnotationMetadataTests {

        @Test
        @DisplayName("Should have proper annotation methods")
        void shouldHaveProperAnnotationMethods() throws NoSuchMethodException {
            assertNotNull(CollectData.class.getMethod("category"));
            assertNotNull(CollectData.class.getMethod("features"));
            assertNotNull(CollectData.class.getMethod("captureScreenshots"));
            assertNotNull(CollectData.class.getMethod("captureIntermediateStates"));
            assertNotNull(CollectData.class.getMethod("samplingRate"));
            assertNotNull(CollectData.class.getMethod("maxSamples"));
            assertNotNull(CollectData.class.getMethod("onlySuccess"));
            assertNotNull(CollectData.class.getMethod("includeTiming"));
            assertNotNull(CollectData.class.getMethod("anonymize"));
            assertNotNull(CollectData.class.getMethod("format"));
            assertNotNull(CollectData.class.getMethod("labels"));
            assertNotNull(CollectData.class.getMethod("compress"));

            // Verify return types
            assertEquals(String.class, CollectData.class.getMethod("category").getReturnType());
            assertEquals(String[].class, CollectData.class.getMethod("features").getReturnType());
            assertEquals(
                    boolean.class,
                    CollectData.class.getMethod("captureScreenshots").getReturnType());
            assertEquals(double.class, CollectData.class.getMethod("samplingRate").getReturnType());
            assertEquals(int.class, CollectData.class.getMethod("maxSamples").getReturnType());
            assertEquals(
                    CollectData.DataFormat.class,
                    CollectData.class.getMethod("format").getReturnType());
        }

        @Test
        @DisplayName("Should have proper annotation type")
        void shouldHaveProperAnnotationType() {
            assertTrue(CollectData.class.isAnnotation());
            assertEquals("CollectData", CollectData.class.getSimpleName());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle method with return value")
        void shouldHandleMethodWithReturnValue() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("textExtractionMethod");
            assertTrue(method.isAnnotationPresent(CollectData.class));
            assertEquals(String.class, method.getReturnType());
        }

        @Test
        @DisplayName("Should handle method with parameters")
        void shouldHandleMethodWithParameters() throws NoSuchMethodException {
            Method method =
                    DataCollectionTestClass.class.getMethod("complexDataMethod", String.class);
            assertTrue(method.isAnnotationPresent(CollectData.class));
            assertEquals(1, method.getParameterCount());
        }

        @Test
        @DisplayName("Should compare annotation instances")
        void shouldCompareAnnotationInstances() throws NoSuchMethodException {
            Method method1 = DataCollectionTestClass.class.getMethod("simpleCollectionMethod");
            Method method2 = DataCollectionTestClass.class.getMethod("clickDataMethod");

            CollectData ann1 = method1.getAnnotation(CollectData.class);
            CollectData ann2 = method1.getAnnotation(CollectData.class);
            CollectData ann3 = method2.getAnnotation(CollectData.class);

            assertEquals(ann1, ann2);
            assertNotEquals(ann1, ann3);
        }

        @Test
        @DisplayName("Should validate sampling rate bounds")
        void shouldValidateSamplingRateBounds() throws NoSuchMethodException {
            Method method = DataCollectionTestClass.class.getMethod("textExtractionMethod");
            CollectData annotation = method.getAnnotation(CollectData.class);

            double rate = annotation.samplingRate();
            assertTrue(rate >= 0.0 && rate <= 1.0, "Sampling rate should be between 0.0 and 1.0");
        }
    }
}
