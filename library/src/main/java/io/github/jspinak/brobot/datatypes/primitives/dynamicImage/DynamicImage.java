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
 *
 * Last used in version 1.0.6. In version 1.0.7, color analysis is done on Pattern objects.
 *
 * A DynamicImage is an image that does not have a fixed pattern, and thus cannot be accurately found
 * with SikuliX or OpenCV pattern matching. These are images that represent objects, such as cats or dogs,
 * in the real world or in 3d environments such as role-playing-games. These objects are typically
 * found by training labeled datasets on neural nets.
 *
 * In Brobot, characteristics of these images are analyzed to give it a distinct profile. This profile is then
 * used, along with information about the active states and their boundaries, for determining the likelihood of
 * pixels belonging to this object. The end result is a sparse matrix containing the probabilities for each pixel of
 * belonging to all images (dynamic and other) on the screen. If the likelihood is very high (it will be 100% in
 * the case of matches on images with fixed patterns), these matches can be used directly. If it is uncertain to
 * which object a group of pixels belongs, a trained neural net is needed.
 *
 * DynamicImages are defined with images representing
 *  - pieces of the image with no background pixels
 *  - the entire image including background, fit to the boundaries of the object
 *  - scenes giving context, where the object is a small part of the overall image
 *
 * __Inside Images__
 * The inside images give us the colors that make up the object. Per-pixel analysis will be more effective when
 *   objects have less variation in color. For any set of objects there will often be some objects with unique
 *   color ranges. Anytime these ranges are seen, we can be fairly certain that the pixel belongs to this object.
 *   Objects with overlapping color ranges will not be able to be distinguished by color only. The ColorProfile
 *   gives the overall range of the object. Hue is more important here than saturation and value, which can
 *   change due to lighting. Main point: a hue outside the hue range of an object does not belong to the object.
 * Objects often have predominant colors. If a pixel has the predominant color of an object, it is more likely
 *   to belong to this object. Groups of pixels in this predominant color range increase the likelihood further.
 *   K-means gives us the predominant colors for objects. We also need to know how dominant each of the k-means
 *   are. This is calculated in a K-means profile. Main point: a pixel close to the hue of the dominant k-mean
 *   of an object is more likely to be that object.
 *
 * __Outside Images__
 * Contours and edges will be useful here. Look at Hough lines and edge detection in OpenCV.
 * In addition, you can do a color analysis of the background.
 *
 * __Context Images__
 * The histograms are calculated for the corners and center of the context images.
 *
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
