package io.github.jspinak.brobot.action.basic.find;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for MatchFusionOptions. Tests configuration for fusing adjacent matches
 * in find operations.
 */
@DisplayName("MatchFusionOptions Tests")
public class MatchFusionOptionsTest extends BrobotTestBase {

    private ObjectMapper objectMapper;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Default Configuration")
    class DefaultConfiguration {

        @Test
        @DisplayName("Default builder creates valid configuration")
        public void testDefaultBuilder() {
            MatchFusionOptions options = MatchFusionOptions.builder().build();

            assertNotNull(options);
            assertEquals(MatchFusionOptions.FusionMethod.NONE, options.getFusionMethod());
            assertEquals(5, options.getMaxFusionDistanceX());
            assertEquals(5, options.getMaxFusionDistanceY());
            assertEquals(0, options.getSceneToUseForCaptureAfterFusingMatches());
        }

        @Test
        @DisplayName("Default fusion method is NONE")
        public void testDefaultFusionMethod() {
            MatchFusionOptions options = MatchFusionOptions.builder().build();

            assertEquals(MatchFusionOptions.FusionMethod.NONE, options.getFusionMethod());
        }

        @Test
        @DisplayName("Default fusion distances are 5 pixels")
        public void testDefaultFusionDistances() {
            MatchFusionOptions options = MatchFusionOptions.builder().build();

            assertEquals(5, options.getMaxFusionDistanceX());
            assertEquals(5, options.getMaxFusionDistanceY());
        }
    }

    @Nested
    @DisplayName("Fusion Method Configuration")
    class FusionMethodConfiguration {

        @Test
        @DisplayName("Configure NONE fusion method")
        public void testNoneFusion() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setFusionMethod(MatchFusionOptions.FusionMethod.NONE)
                            .build();

            assertEquals(MatchFusionOptions.FusionMethod.NONE, options.getFusionMethod());
        }

        @Test
        @DisplayName("Configure ABSOLUTE fusion method")
        public void testAbsoluteFusion() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                            .build();

            assertEquals(MatchFusionOptions.FusionMethod.ABSOLUTE, options.getFusionMethod());
        }

        @Test
        @DisplayName("Configure RELATIVE fusion method")
        public void testRelativeFusion() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setFusionMethod(MatchFusionOptions.FusionMethod.RELATIVE)
                            .build();

            assertEquals(MatchFusionOptions.FusionMethod.RELATIVE, options.getFusionMethod());
        }

        @ParameterizedTest
        @EnumSource(MatchFusionOptions.FusionMethod.class)
        @DisplayName("All fusion methods are supported")
        public void testAllFusionMethods(MatchFusionOptions.FusionMethod method) {
            MatchFusionOptions options =
                    MatchFusionOptions.builder().setFusionMethod(method).build();

            assertEquals(method, options.getFusionMethod());
        }
    }

    @Nested
    @DisplayName("Fusion Distance Configuration")
    class FusionDistanceConfiguration {

        @Test
        @DisplayName("Set custom X fusion distance")
        public void testCustomXDistance() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder().setMaxFusionDistanceX(20).build();

            assertEquals(20, options.getMaxFusionDistanceX());
            assertEquals(5, options.getMaxFusionDistanceY()); // Default Y
        }

        @Test
        @DisplayName("Set custom Y fusion distance")
        public void testCustomYDistance() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder().setMaxFusionDistanceY(15).build();

            assertEquals(5, options.getMaxFusionDistanceX()); // Default X
            assertEquals(15, options.getMaxFusionDistanceY());
        }

        @Test
        @DisplayName("Set both fusion distances")
        public void testBothDistances() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setMaxFusionDistanceX(30)
                            .setMaxFusionDistanceY(25)
                            .build();

            assertEquals(30, options.getMaxFusionDistanceX());
            assertEquals(25, options.getMaxFusionDistanceY());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 5, 10, 20, 50, 100})
        @DisplayName("Various fusion distance values")
        public void testVariousFusionDistances(int distance) {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setMaxFusionDistanceX(distance)
                            .setMaxFusionDistanceY(distance)
                            .build();

            assertEquals(distance, options.getMaxFusionDistanceX());
            assertEquals(distance, options.getMaxFusionDistanceY());
        }

        @Test
        @DisplayName("Zero fusion distance (exact adjacency)")
        public void testZeroFusionDistance() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setMaxFusionDistanceX(0)
                            .setMaxFusionDistanceY(0)
                            .build();

            assertEquals(0, options.getMaxFusionDistanceX());
            assertEquals(0, options.getMaxFusionDistanceY());
        }

        @Test
        @DisplayName("Negative fusion distances are allowed")
        public void testNegativeFusionDistances() {
            // Negative distances might mean overlap required
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setMaxFusionDistanceX(-5)
                            .setMaxFusionDistanceY(-5)
                            .build();

            assertEquals(-5, options.getMaxFusionDistanceX());
            assertEquals(-5, options.getMaxFusionDistanceY());
        }
    }

    @Nested
    @DisplayName("Scene Configuration")
    class SceneConfiguration {

        @Test
        @DisplayName("Default scene index is 0")
        public void testDefaultSceneIndex() {
            MatchFusionOptions options = MatchFusionOptions.builder().build();

            assertEquals(0, options.getSceneToUseForCaptureAfterFusingMatches());
        }

        @Test
        @DisplayName("Set custom scene index")
        public void testCustomSceneIndex() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setSceneToUseForCaptureAfterFusingMatches(2)
                            .build();

            assertEquals(2, options.getSceneToUseForCaptureAfterFusingMatches());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 5, 10})
        @DisplayName("Various scene indices")
        public void testVariousSceneIndices(int sceneIndex) {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setSceneToUseForCaptureAfterFusingMatches(sceneIndex)
                            .build();

            assertEquals(sceneIndex, options.getSceneToUseForCaptureAfterFusingMatches());
        }
    }

    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {

        @Test
        @DisplayName("Builder chaining works correctly")
        public void testBuilderChaining() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setFusionMethod(MatchFusionOptions.FusionMethod.RELATIVE)
                            .setMaxFusionDistanceX(15)
                            .setMaxFusionDistanceY(10)
                            .setSceneToUseForCaptureAfterFusingMatches(1)
                            .build();

            assertNotNull(options);
            assertEquals(MatchFusionOptions.FusionMethod.RELATIVE, options.getFusionMethod());
            assertEquals(15, options.getMaxFusionDistanceX());
            assertEquals(10, options.getMaxFusionDistanceY());
            assertEquals(1, options.getSceneToUseForCaptureAfterFusingMatches());
        }

        @Test
        @DisplayName("toBuilder creates modifiable copy")
        public void testToBuilder() {
            MatchFusionOptions original =
                    MatchFusionOptions.builder()
                            .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                            .setMaxFusionDistanceX(20)
                            .build();

            MatchFusionOptions modified = original.toBuilder().setMaxFusionDistanceY(30).build();

            // Original unchanged
            assertEquals(5, original.getMaxFusionDistanceY());

            // Modified has new value
            assertEquals(30, modified.getMaxFusionDistanceY());

            // Other values preserved
            assertEquals(original.getFusionMethod(), modified.getFusionMethod());
            assertEquals(original.getMaxFusionDistanceX(), modified.getMaxFusionDistanceX());
        }

        @Test
        @DisplayName("Multiple builds from same builder")
        public void testMultipleBuilds() {
            MatchFusionOptions.Builder builder =
                    MatchFusionOptions.builder()
                            .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE);

            MatchFusionOptions options1 = builder.build();
            MatchFusionOptions options2 = builder.setMaxFusionDistanceX(10).build();

            assertNotNull(options1);
            assertNotNull(options2);
            assertEquals(MatchFusionOptions.FusionMethod.ABSOLUTE, options1.getFusionMethod());
            assertEquals(MatchFusionOptions.FusionMethod.ABSOLUTE, options2.getFusionMethod());
            assertEquals(10, options2.getMaxFusionDistanceX());
        }
    }

    @Nested
    @DisplayName("JSON Serialization")
    class JsonSerialization {

        @Test
        @DisplayName("Serialize and deserialize with default values")
        public void testDefaultSerialization() throws JsonProcessingException {
            MatchFusionOptions original = MatchFusionOptions.builder().build();

            String json = objectMapper.writeValueAsString(original);
            assertNotNull(json);

            MatchFusionOptions deserialized =
                    objectMapper.readValue(json, MatchFusionOptions.class);
            assertNotNull(deserialized);
            assertEquals(original.getFusionMethod(), deserialized.getFusionMethod());
            assertEquals(original.getMaxFusionDistanceX(), deserialized.getMaxFusionDistanceX());
            assertEquals(original.getMaxFusionDistanceY(), deserialized.getMaxFusionDistanceY());
        }

        @Test
        @DisplayName("Serialize and deserialize with custom values")
        public void testCustomSerialization() throws JsonProcessingException {
            MatchFusionOptions original =
                    MatchFusionOptions.builder()
                            .setFusionMethod(MatchFusionOptions.FusionMethod.RELATIVE)
                            .setMaxFusionDistanceX(25)
                            .setMaxFusionDistanceY(35)
                            .setSceneToUseForCaptureAfterFusingMatches(3)
                            .build();

            String json = objectMapper.writeValueAsString(original);
            MatchFusionOptions deserialized =
                    objectMapper.readValue(json, MatchFusionOptions.class);

            assertEquals(original.getFusionMethod(), deserialized.getFusionMethod());
            assertEquals(original.getMaxFusionDistanceX(), deserialized.getMaxFusionDistanceX());
            assertEquals(original.getMaxFusionDistanceY(), deserialized.getMaxFusionDistanceY());
            assertEquals(
                    original.getSceneToUseForCaptureAfterFusingMatches(),
                    deserialized.getSceneToUseForCaptureAfterFusingMatches());
        }

        @Test
        @DisplayName("Deserialize with unknown properties (forward compatibility)")
        public void testDeserializeWithUnknownProperties() throws JsonProcessingException {
            String json =
                    "{\"fusionMethod\":\"ABSOLUTE\",\"maxFusionDistanceX\":10,\"unknownField\":\"value\"}";

            MatchFusionOptions options = objectMapper.readValue(json, MatchFusionOptions.class);
            assertNotNull(options);
            assertEquals(MatchFusionOptions.FusionMethod.ABSOLUTE, options.getFusionMethod());
            assertEquals(10, options.getMaxFusionDistanceX());
        }

        @Test
        @DisplayName("Deserialize partial JSON")
        public void testPartialDeserialization() throws JsonProcessingException {
            String json = "{\"fusionMethod\":\"RELATIVE\"}";

            MatchFusionOptions options = objectMapper.readValue(json, MatchFusionOptions.class);
            assertNotNull(options);
            assertEquals(MatchFusionOptions.FusionMethod.RELATIVE, options.getFusionMethod());
            // Should use defaults for unspecified fields
            assertEquals(5, options.getMaxFusionDistanceX());
            assertEquals(5, options.getMaxFusionDistanceY());
        }
    }

    @Nested
    @DisplayName("Use Cases")
    class UseCases {

        @Test
        @DisplayName("Configuration for text line fusion")
        public void testTextLineFusion() {
            // Text often needs horizontal fusion with minimal vertical
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                            .setMaxFusionDistanceX(50) // Allow wide horizontal gaps
                            .setMaxFusionDistanceY(5) // Tight vertical constraint
                            .build();

            assertEquals(MatchFusionOptions.FusionMethod.ABSOLUTE, options.getFusionMethod());
            assertEquals(50, options.getMaxFusionDistanceX());
            assertEquals(5, options.getMaxFusionDistanceY());
        }

        @Test
        @DisplayName("Configuration for icon grid fusion")
        public void testIconGridFusion() {
            // Icons in a grid need equal X/Y fusion
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                            .setMaxFusionDistanceX(20)
                            .setMaxFusionDistanceY(20)
                            .build();

            assertEquals(20, options.getMaxFusionDistanceX());
            assertEquals(20, options.getMaxFusionDistanceY());
        }

        @Test
        @DisplayName("Configuration for relative size fusion")
        public void testRelativeSizeFusion() {
            // Use relative fusion for elements of varying sizes
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setFusionMethod(MatchFusionOptions.FusionMethod.RELATIVE)
                            .setMaxFusionDistanceX(10)
                            .setMaxFusionDistanceY(10)
                            .build();

            assertEquals(MatchFusionOptions.FusionMethod.RELATIVE, options.getFusionMethod());
        }

        @Test
        @DisplayName("Disabled fusion configuration")
        public void testDisabledFusion() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setFusionMethod(MatchFusionOptions.FusionMethod.NONE)
                            .build();

            assertEquals(MatchFusionOptions.FusionMethod.NONE, options.getFusionMethod());
            // Distance values are ignored when fusion is NONE
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Very large fusion distances")
        public void testLargeFusionDistances() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setMaxFusionDistanceX(Integer.MAX_VALUE)
                            .setMaxFusionDistanceY(Integer.MAX_VALUE)
                            .build();

            assertEquals(Integer.MAX_VALUE, options.getMaxFusionDistanceX());
            assertEquals(Integer.MAX_VALUE, options.getMaxFusionDistanceY());
        }

        @Test
        @DisplayName("Negative scene index")
        public void testNegativeSceneIndex() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setSceneToUseForCaptureAfterFusingMatches(-1)
                            .build();

            assertEquals(-1, options.getSceneToUseForCaptureAfterFusingMatches());
        }

        @Test
        @DisplayName("Asymmetric fusion distances")
        public void testAsymmetricFusionDistances() {
            MatchFusionOptions options =
                    MatchFusionOptions.builder()
                            .setMaxFusionDistanceX(100)
                            .setMaxFusionDistanceY(0)
                            .build();

            assertEquals(100, options.getMaxFusionDistanceX());
            assertEquals(0, options.getMaxFusionDistanceY());
        }
    }
}
