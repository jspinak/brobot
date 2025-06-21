package io.github.jspinak.brobot.datatypes.primitives.dynamicImage;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.KmeansProfilesAllSchemas;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced representation for variable-appearance objects in GUI automation.
 * 
 * <p>DynamicImage addresses the challenge of recognizing objects that lack fixed visual 
 * patterns, such as 3D game characters, natural images, or dynamically rendered content. 
 * Unlike traditional template matching which requires exact or near-exact visual matches, 
 * DynamicImage uses color profiling and contextual analysis to identify objects with 
 * variable appearances. This enables automation of applications with dynamic visual 
 * content that would otherwise require neural networks or machine learning.</p>
 * 
 * <p><b>Note:</b> Last used in version 1.0.6. In version 1.0.7+, color analysis has 
 * been moved to Pattern objects for better integration.</p>
 * 
 * <p>Three-layer analysis approach:
 * <ul>
 *   <li><b>Inside Images</b>: Core object pixels without background</li>
 *   <li><b>Full Images</b>: Complete object including surrounding pixels</li>
 *   <li><b>Context Images</b>: Wider scene providing environmental context</li>
 * </ul>
 * </p>
 * 
 * <p>Inside image analysis:
 * <ul>
 *   <li>Extracts color characteristics unique to the object</li>
 *   <li>Creates color profiles using ranges and dominant colors</li>
 *   <li>Uses K-means clustering to identify predominant colors</li>
 *   <li>Focuses on hue (more stable than saturation/value under lighting changes)</li>
 *   <li>Enables pixel-level probability calculations</li>
 * </ul>
 * </p>
 * 
 * <p>Full image analysis:
 * <ul>
 *   <li>Captures object boundaries and edge characteristics</li>
 *   <li>Identifies background colors to exclude</li>
 *   <li>Creates "outside" color cluster (full minus inside)</li>
 *   <li>Helps distinguish object from background</li>
 * </ul>
 * </p>
 * 
 * <p>Context image analysis:
 * <ul>
 *   <li>Provides environmental cues for object location</li>
 *   <li>Calculates histograms for corners and center</li>
 *   <li>Helps predict where objects are likely to appear</li>
 *   <li>Useful for objects that appear in consistent contexts</li>
 * </ul>
 * </p>
 * 
 * <p>Practical applications:
 * <ul>
 *   <li>Game automation with 3D characters or items</li>
 *   <li>Nature photography applications</li>
 *   <li>Medical imaging interfaces</li>
 *   <li>Augmented reality applications</li>
 *   <li>Any GUI with dynamically rendered content</li>
 * </ul>
 * </p>
 * 
 * <p>Color profiling strategy:
 * <ul>
 *   <li>Unique color ranges provide high confidence matches</li>
 *   <li>Overlapping ranges require additional context</li>
 *   <li>Dominant colors increase likelihood when found in groups</li>
 *   <li>Hue-based analysis resists lighting variations</li>
 *   <li>Probabilistic approach handles uncertainty</li>
 * </ul>
 * </p>
 * 
 * <p>Integration with state model:
 * <ul>
 *   <li>Active states provide search boundaries</li>
 *   <li>State context improves recognition accuracy</li>
 *   <li>Combines with fixed patterns for hybrid recognition</li>
 *   <li>Produces probability matrices for decision making</li>
 * </ul>
 * </p>
 * 
 * <p>Future enhancements (theoretical):
 * <ul>
 *   <li>Edge detection and contour analysis</li>
 *   <li>Hough line transforms for geometric features</li>
 *   <li>Integration with neural networks for complex objects</li>
 *   <li>Motion analysis for moving objects</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, DynamicImage represents an attempt to bridge 
 * traditional template matching with more advanced computer vision techniques. 
 * While superseded by Pattern-based color analysis in newer versions, it 
 * demonstrates Brobot's evolution toward handling increasingly complex visual 
 * recognition challenges in modern GUIs.</p>
 * 
 * @deprecated As of version 1.0.7, use Pattern objects with color analysis instead
 * @since 1.0
 * @see StateImage
 * @see ColorCluster
 * @see KmeansProfilesAllSchemas
 * @see Pattern
 */
@Getter
@Setter
public class DynamicImage {

    /*
    Inside images are cut from within the boundaries of the object.
     */
    private StateImage inside = new StateImage();
    private ColorCluster insideColorCluster = new ColorCluster();
    private KmeansProfilesAllSchemas insideKmeansProfiles;
    // for illustration
    private Mat oneColumnBGRMat; // initialized when program is run
    private Mat oneColumnHSVMat; // initialized when program is run
    private Mat imagesMat; // initialized when program is run, shows the images in the StateImage
    private Mat profilesMat; // initialized when program is run, shows the color profiles in the StateImage

    /*
    The full image contains the complete image and some background. This type of image is the most common
    type of image representation found in image classification or detection datasets.
    The ousideColorCluster is the ColorCluster resulting from the full colors minus the inside colors.
     */
    private StateImage full = new StateImage();
    private ColorCluster outsideColorCluster = new ColorCluster(); // only colors that are not in the inside profile

    /*
    The context is a larger image showing important aspects of the background.
     */
    private StateImage context = new StateImage();
    private List<List<Mat>> contextHistograms = new ArrayList<>();

    public static class Builder {
        private StateImage inside = new StateImage();
        private StateImage full = new StateImage();
        private StateImage context = new StateImage();

        public Builder addInsideImages(String... filenames) {
            inside.addPatterns(filenames);
            return this;
        }

        public Builder addFullImages(String... filenames) {
            full.addPatterns(filenames);
            return this;
        }

        public Builder addContextImages(String... filenames) {
            context.addPatterns(filenames);
            return this;
        }

        public DynamicImage build() {
            DynamicImage dynamicImage = new DynamicImage();
            dynamicImage.inside = inside;
            dynamicImage.full = full;
            dynamicImage.context = context;
            return dynamicImage;
        }
    }
}
