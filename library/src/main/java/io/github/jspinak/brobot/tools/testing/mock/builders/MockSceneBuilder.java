package io.github.jspinak.brobot.tools.testing.mock.builders;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;

import io.github.jspinak.brobot.model.analysis.color.*;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Builder for creating mock Scene and SceneAnalysis objects for testing. Provides properly
 * initialized objects that avoid NPEs in tests.
 */
public class MockSceneBuilder {

    /** Creates a mock Scene with a valid image. Only works in mock mode. */
    public static Scene createMockScene() {
        // Remove the runtime check - this is a test utility that should work regardless of mode
        Pattern pattern = createMockPattern();
        return new Scene(pattern);
    }

    /** Creates a mock Pattern with BGR and HSV images. */
    public static Pattern createMockPattern() {
        Pattern pattern = new Pattern();

        // Create a simple 100x100 BGR image
        Mat matBGR = new Mat(100, 100, CV_8UC3, new Scalar(128, 128, 128, 0));

        // Create Image from Mat
        Image image = new Image(matBGR, "mock_image");
        pattern.setImage(image);

        return pattern;
    }

    /**
     * Creates a mock SceneAnalysis with the specified number of pixel profiles. Each profile is
     * properly initialized with ColorClusters and schemas.
     */
    public static SceneAnalysis createMockSceneAnalysis(int numProfiles) {
        List<PixelProfiles> profiles = new ArrayList<>();
        Scene scene = createMockScene();

        for (int i = 0; i < numProfiles; i++) {
            profiles.add(createMockPixelProfile(i, scene));
        }
        return new SceneAnalysis(profiles, scene);
    }

    /** Creates a mock PixelProfiles with properly initialized ColorCluster. */
    public static PixelProfiles createMockPixelProfile(int index) {
        Scene scene = createMockScene();
        return createMockPixelProfile(index, scene);
    }

    /** Creates a mock PixelProfiles with properly initialized ColorCluster. */
    public static PixelProfiles createMockPixelProfile(int index, Scene scene) {
        PixelProfiles profiles = new PixelProfiles(scene);

        // Set StateImage with ColorCluster
        StateImage stateImage = new StateImage.Builder().build();
        stateImage.setIndex(index);
        stateImage.setColorCluster(createMockColorCluster());
        profiles.setStateImage(stateImage);

        // Note: PixelProfile has no public constructor or setters,
        // so we can't add mock PixelProfile objects to the list.
        // The list will remain empty, but the ColorCluster is properly initialized
        // which prevents NPEs in SceneAnalysis operations.

        return profiles;
    }

    /** Creates a mock ColorCluster with BGR and HSV schemas. */
    public static ColorCluster createMockColorCluster() {
        ColorCluster cluster = new ColorCluster();

        // Add BGR schema using setSchema method
        ColorSchema bgrSchema = createMockColorSchema(BGR);
        cluster.setSchema(BGR, bgrSchema);

        // Add HSV schema using setSchema method
        ColorSchema hsvSchema = createMockColorSchema(HSV);
        cluster.setSchema(HSV, hsvSchema);

        return cluster;
    }

    /** Creates a mock ColorSchema with color statistics. */
    public static ColorSchema createMockColorSchema(ColorCluster.ColorSchemaName schemaName) {
        ColorSchema schema;

        if (schemaName == BGR) {
            // Create BGR schema with BLUE, GREEN, RED color values
            schema =
                    new ColorSchema(
                            ColorSchema.ColorValue.BLUE,
                            ColorSchema.ColorValue.GREEN,
                            ColorSchema.ColorValue.RED);
            // Set values for each channel (min, max, mean, stddev)
            schema.setValues(ColorSchema.ColorValue.BLUE, 100.0, 156.0, 128.0, 10.0);
            schema.setValues(ColorSchema.ColorValue.GREEN, 100.0, 156.0, 128.0, 10.0);
            schema.setValues(ColorSchema.ColorValue.RED, 100.0, 156.0, 128.0, 10.0);
        } else {
            // Create HSV schema with HUE, SATURATION, VALUE color values
            schema =
                    new ColorSchema(
                            ColorSchema.ColorValue.HUE,
                            ColorSchema.ColorValue.SATURATION,
                            ColorSchema.ColorValue.VALUE);
            // Set values for each channel (min, max, mean, stddev)
            schema.setValues(ColorSchema.ColorValue.HUE, 0.0, 360.0, 180.0, 50.0);
            schema.setValues(ColorSchema.ColorValue.SATURATION, 100.0, 156.0, 128.0, 10.0);
            schema.setValues(ColorSchema.ColorValue.VALUE, 100.0, 156.0, 128.0, 10.0);
        }

        return schema;
    }

    /** Builder class for more complex SceneAnalysis configurations. */
    public static class SceneAnalysisBuilder {
        private Scene scene;
        private List<PixelProfiles> profiles = new ArrayList<>();
        private boolean useDefaultColorCluster = true;

        public SceneAnalysisBuilder() {
            this.scene = createMockScene();
        }

        public SceneAnalysisBuilder withScene(Scene scene) {
            this.scene = scene;
            return this;
        }

        public SceneAnalysisBuilder withDefaultColorCluster() {
            this.useDefaultColorCluster = true;
            return this;
        }

        public SceneAnalysisBuilder withPixelProfile(int index) {
            PixelProfiles profile = createMockPixelProfile(index, scene);
            profiles.add(profile);
            return this;
        }

        public SceneAnalysisBuilder withPixelProfiles(PixelProfiles... profilesArray) {
            for (PixelProfiles p : profilesArray) {
                profiles.add(p);
            }
            return this;
        }

        public SceneAnalysis build() {
            if (profiles.isEmpty()) {
                // Add at least one profile
                withPixelProfile(0);
            }
            return new SceneAnalysis(profiles, scene);
        }
    }

    /** Creates a builder for SceneAnalysis. */
    public static SceneAnalysisBuilder sceneAnalysis() {
        return new SceneAnalysisBuilder();
    }
}
