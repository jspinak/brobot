package io.github.jspinak.brobot.aspects.annotations;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.*;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.TestCategories;

/**
 * Comprehensive test suite for @Monitored annotation. Tests performance monitoring attributes,
 * thresholds, sampling, and metadata.
 */
@DisplayName("@Monitored Annotation Tests")
@Tag(TestCategories.UNIT)
@Tag(TestCategories.FAST)
@Tag("annotations")
@Tag("aspects")
public class MonitoredAnnotationTest extends BrobotTestBase {

    // Test classes with various @Monitored configurations
    static class MonitoredTestClass {

        @Monitored
        public void simpleMonitoredMethod() {}

        @Monitored(name = "CustomOperation", threshold = 5000)
        public void customMonitoredMethod() {}

        @Monitored(
                name = "ComplexOperation",
                threshold = 10000,
                trackMemory = true,
                logParameters = true,
                logResult = true,
                tags = {"critical", "database"},
                samplingRate = 0.5,
                createSpan = true,
                customMetrics = {"cache_hits", "db_queries"})
        public String complexMonitoredMethod(String param) {
            return "result";
        }

        @Monitored(threshold = -1)
        public void defaultThresholdMethod() {}

        @Monitored(samplingRate = 0.1)
        public void sampledMethod() {}

        @Monitored(tags = {})
        public void emptyTagsMethod() {}

        public void unmonitoredMethod() {}
    }

    @Monitored
    static class ClassLevelMonitored {
        public void method1() {}

        public void method2() {}
    }

    @BeforeEach
    public void setupTest() {
        super.setupTest();
    }

    @Nested
    @DisplayName("Annotation Presence Tests")
    class AnnotationPresenceTests {

        @Test
        @DisplayName("Should detect @Monitored on methods")
        void shouldDetectMonitoredOnMethods() throws NoSuchMethodException {
            Method simpleMethod = MonitoredTestClass.class.getMethod("simpleMonitoredMethod");
            Method customMethod = MonitoredTestClass.class.getMethod("customMonitoredMethod");
            Method unmonitoredMethod = MonitoredTestClass.class.getMethod("unmonitoredMethod");

            assertTrue(simpleMethod.isAnnotationPresent(Monitored.class));
            assertTrue(customMethod.isAnnotationPresent(Monitored.class));
            assertFalse(unmonitoredMethod.isAnnotationPresent(Monitored.class));
        }

        @Test
        @DisplayName("Should detect @Monitored on class")
        void shouldDetectMonitoredOnClass() {
            assertTrue(ClassLevelMonitored.class.isAnnotationPresent(Monitored.class));
            assertFalse(MonitoredTestClass.class.isAnnotationPresent(Monitored.class));
        }

        @Test
        @DisplayName("Should support both method and class targets")
        void shouldSupportBothTargets() {
            // Verify annotation can be applied to both ElementType.METHOD and ElementType.TYPE
            Annotation[] classAnnotations = ClassLevelMonitored.class.getAnnotations();
            boolean hasMonitored =
                    Arrays.stream(classAnnotations).anyMatch(a -> a instanceof Monitored);
            assertTrue(hasMonitored);

            try {
                Method method = MonitoredTestClass.class.getMethod("simpleMonitoredMethod");
                assertTrue(method.isAnnotationPresent(Monitored.class));
            } catch (NoSuchMethodException e) {
                fail("Method should exist");
            }
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should have correct default values")
        void shouldHaveCorrectDefaultValues() throws NoSuchMethodException {
            Method method = MonitoredTestClass.class.getMethod("simpleMonitoredMethod");
            Monitored annotation = method.getAnnotation(Monitored.class);

            assertEquals("", annotation.name());
            assertEquals(-1, annotation.threshold());
            assertFalse(annotation.trackMemory());
            assertFalse(annotation.logParameters());
            assertFalse(annotation.logResult());
            assertArrayEquals(new String[] {}, annotation.tags());
            assertEquals(1.0, annotation.samplingRate(), 0.001);
            assertFalse(annotation.createSpan());
            assertArrayEquals(new String[] {}, annotation.customMetrics());
        }

        @Test
        @DisplayName("Should use -1 as default threshold")
        void shouldUseNegativeOneAsDefaultThreshold() throws NoSuchMethodException {
            Method method = MonitoredTestClass.class.getMethod("defaultThresholdMethod");
            Monitored annotation = method.getAnnotation(Monitored.class);

            assertEquals(-1, annotation.threshold());
            // -1 indicates "use global threshold"
        }
    }

    @Nested
    @DisplayName("Custom Configuration Tests")
    class CustomConfigurationTests {

        @Test
        @DisplayName("Should read custom name and threshold")
        void shouldReadCustomNameAndThreshold() throws NoSuchMethodException {
            Method method = MonitoredTestClass.class.getMethod("customMonitoredMethod");
            Monitored annotation = method.getAnnotation(Monitored.class);

            assertEquals("CustomOperation", annotation.name());
            assertEquals(5000, annotation.threshold());
        }

        @Test
        @DisplayName("Should read all complex configuration")
        void shouldReadComplexConfiguration() throws NoSuchMethodException {
            Method method =
                    MonitoredTestClass.class.getMethod("complexMonitoredMethod", String.class);
            Monitored annotation = method.getAnnotation(Monitored.class);

            assertEquals("ComplexOperation", annotation.name());
            assertEquals(10000, annotation.threshold());
            assertTrue(annotation.trackMemory());
            assertTrue(annotation.logParameters());
            assertTrue(annotation.logResult());

            String[] tags = annotation.tags();
            assertEquals(2, tags.length);
            assertTrue(Arrays.asList(tags).contains("critical"));
            assertTrue(Arrays.asList(tags).contains("database"));

            assertEquals(0.5, annotation.samplingRate(), 0.001);
            assertTrue(annotation.createSpan());

            String[] metrics = annotation.customMetrics();
            assertEquals(2, metrics.length);
            assertTrue(Arrays.asList(metrics).contains("cache_hits"));
            assertTrue(Arrays.asList(metrics).contains("db_queries"));
        }
    }

    @Nested
    @DisplayName("Sampling Rate Tests")
    class SamplingRateTests {

        @Test
        @DisplayName("Should support sampling rate between 0 and 1")
        void shouldSupportSamplingRate() throws NoSuchMethodException {
            Method method = MonitoredTestClass.class.getMethod("sampledMethod");
            Monitored annotation = method.getAnnotation(Monitored.class);

            assertEquals(0.1, annotation.samplingRate(), 0.001);
            assertTrue(annotation.samplingRate() >= 0.0);
            assertTrue(annotation.samplingRate() <= 1.0);
        }

        @Test
        @DisplayName("Should default to 1.0 sampling rate")
        void shouldDefaultToFullSampling() throws NoSuchMethodException {
            Method method = MonitoredTestClass.class.getMethod("simpleMonitoredMethod");
            Monitored annotation = method.getAnnotation(Monitored.class);

            assertEquals(1.0, annotation.samplingRate(), 0.001);
        }
    }

    @Nested
    @DisplayName("Tags and Metrics Tests")
    class TagsAndMetricsTests {

        @Test
        @DisplayName("Should handle empty tags array")
        void shouldHandleEmptyTags() throws NoSuchMethodException {
            Method method = MonitoredTestClass.class.getMethod("emptyTagsMethod");
            Monitored annotation = method.getAnnotation(Monitored.class);

            String[] tags = annotation.tags();
            assertNotNull(tags);
            assertEquals(0, tags.length);
        }

        @Test
        @DisplayName("Should handle multiple tags")
        void shouldHandleMultipleTags() throws NoSuchMethodException {
            Method method =
                    MonitoredTestClass.class.getMethod("complexMonitoredMethod", String.class);
            Monitored annotation = method.getAnnotation(Monitored.class);

            String[] tags = annotation.tags();
            assertEquals(2, tags.length);

            // Tags can be used for categorization and filtering
            for (String tag : tags) {
                assertNotNull(tag);
                assertFalse(tag.isEmpty());
            }
        }

        @Test
        @DisplayName("Should handle custom metrics array")
        void shouldHandleCustomMetrics() throws NoSuchMethodException {
            Method method =
                    MonitoredTestClass.class.getMethod("complexMonitoredMethod", String.class);
            Monitored annotation = method.getAnnotation(Monitored.class);

            String[] metrics = annotation.customMetrics();
            assertNotNull(metrics);
            assertEquals(2, metrics.length);
        }
    }

    @Nested
    @DisplayName("Security and Logging Tests")
    class SecurityAndLoggingTests {

        @Test
        @DisplayName("Should default to secure logging settings")
        void shouldDefaultToSecureSettings() throws NoSuchMethodException {
            Method method = MonitoredTestClass.class.getMethod("simpleMonitoredMethod");
            Monitored annotation = method.getAnnotation(Monitored.class);

            assertFalse(annotation.logParameters(), "Should not log parameters by default");
            assertFalse(annotation.logResult(), "Should not log result by default");
        }

        @Test
        @DisplayName("Should allow explicit parameter and result logging")
        void shouldAllowExplicitLogging() throws NoSuchMethodException {
            Method method =
                    MonitoredTestClass.class.getMethod("complexMonitoredMethod", String.class);
            Monitored annotation = method.getAnnotation(Monitored.class);

            assertTrue(annotation.logParameters());
            assertTrue(annotation.logResult());
        }
    }

    @Nested
    @DisplayName("Memory Tracking Tests")
    class MemoryTrackingTests {

        @Test
        @DisplayName("Should default to no memory tracking")
        void shouldDefaultToNoMemoryTracking() throws NoSuchMethodException {
            Method method = MonitoredTestClass.class.getMethod("simpleMonitoredMethod");
            Monitored annotation = method.getAnnotation(Monitored.class);

            assertFalse(annotation.trackMemory());
        }

        @Test
        @DisplayName("Should support memory tracking")
        void shouldSupportMemoryTracking() throws NoSuchMethodException {
            Method method =
                    MonitoredTestClass.class.getMethod("complexMonitoredMethod", String.class);
            Monitored annotation = method.getAnnotation(Monitored.class);

            assertTrue(annotation.trackMemory());
        }
    }

    @Nested
    @DisplayName("Distributed Tracing Tests")
    class DistributedTracingTests {

        @Test
        @DisplayName("Should default to no span creation")
        void shouldDefaultToNoSpanCreation() throws NoSuchMethodException {
            Method method = MonitoredTestClass.class.getMethod("simpleMonitoredMethod");
            Monitored annotation = method.getAnnotation(Monitored.class);

            assertFalse(annotation.createSpan());
        }

        @Test
        @DisplayName("Should support span creation")
        void shouldSupportSpanCreation() throws NoSuchMethodException {
            Method method =
                    MonitoredTestClass.class.getMethod("complexMonitoredMethod", String.class);
            Monitored annotation = method.getAnnotation(Monitored.class);

            assertTrue(annotation.createSpan());
        }
    }

    @Nested
    @DisplayName("Class Level Monitoring Tests")
    class ClassLevelMonitoringTests {

        @Test
        @DisplayName("Should apply to class")
        void shouldApplyToClass() {
            Monitored annotation = ClassLevelMonitored.class.getAnnotation(Monitored.class);
            assertNotNull(annotation);
        }

        @Test
        @DisplayName("Class-level annotation should have defaults")
        void classLevelShouldHaveDefaults() {
            Monitored annotation = ClassLevelMonitored.class.getAnnotation(Monitored.class);

            assertEquals("", annotation.name());
            assertEquals(-1, annotation.threshold());
            assertEquals(1.0, annotation.samplingRate(), 0.001);
        }
    }

    @Nested
    @DisplayName("Annotation Metadata Tests")
    class AnnotationMetadataTests {

        @Test
        @DisplayName("Should have runtime retention")
        void shouldHaveRuntimeRetention() throws NoSuchMethodException {
            Method method = MonitoredTestClass.class.getMethod("simpleMonitoredMethod");
            Monitored annotation = method.getAnnotation(Monitored.class);
            assertNotNull(annotation, "Should be available at runtime");
        }

        @Test
        @DisplayName("Should be documented")
        void shouldBeDocumented() {
            assertTrue(Monitored.class.isAnnotationPresent(Documented.class));
        }

        @Test
        @DisplayName("Should have proper annotation methods")
        void shouldHaveProperAnnotationMethods() throws NoSuchMethodException {
            // Verify all annotation methods exist
            assertNotNull(Monitored.class.getMethod("name"));
            assertNotNull(Monitored.class.getMethod("threshold"));
            assertNotNull(Monitored.class.getMethod("trackMemory"));
            assertNotNull(Monitored.class.getMethod("logParameters"));
            assertNotNull(Monitored.class.getMethod("logResult"));
            assertNotNull(Monitored.class.getMethod("tags"));
            assertNotNull(Monitored.class.getMethod("samplingRate"));
            assertNotNull(Monitored.class.getMethod("createSpan"));
            assertNotNull(Monitored.class.getMethod("customMetrics"));

            // Verify return types
            assertEquals(String.class, Monitored.class.getMethod("name").getReturnType());
            assertEquals(long.class, Monitored.class.getMethod("threshold").getReturnType());
            assertEquals(boolean.class, Monitored.class.getMethod("trackMemory").getReturnType());
            assertEquals(double.class, Monitored.class.getMethod("samplingRate").getReturnType());
            assertEquals(String[].class, Monitored.class.getMethod("tags").getReturnType());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle method with parameters")
        void shouldHandleMethodWithParameters() throws NoSuchMethodException {
            Method method =
                    MonitoredTestClass.class.getMethod("complexMonitoredMethod", String.class);
            assertTrue(method.isAnnotationPresent(Monitored.class));

            // Method can have any parameters
            assertEquals(1, method.getParameterCount());
        }

        @Test
        @DisplayName("Should handle method with return value")
        void shouldHandleMethodWithReturnValue() throws NoSuchMethodException {
            Method method =
                    MonitoredTestClass.class.getMethod("complexMonitoredMethod", String.class);
            Monitored annotation = method.getAnnotation(Monitored.class);

            // Method can have any return type
            assertEquals(String.class, method.getReturnType());
            assertTrue(annotation.logResult());
        }

        @Test
        @DisplayName("Should compare annotation instances")
        void shouldCompareAnnotationInstances() throws NoSuchMethodException {
            Method method1 = MonitoredTestClass.class.getMethod("simpleMonitoredMethod");
            Method method2 = MonitoredTestClass.class.getMethod("customMonitoredMethod");

            Monitored ann1 = method1.getAnnotation(Monitored.class);
            Monitored ann2 = method1.getAnnotation(Monitored.class);
            Monitored ann3 = method2.getAnnotation(Monitored.class);

            assertEquals(ann1, ann2);
            assertNotEquals(ann1, ann3);
        }
    }
}
