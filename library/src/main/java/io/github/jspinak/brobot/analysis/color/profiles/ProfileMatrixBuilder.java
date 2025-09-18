package io.github.jspinak.brobot.analysis.color.profiles;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;

import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.ColorInfo;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.recognition.ImageLoader;
import io.github.jspinak.brobot.util.image.visualization.MatBuilder;

/**
 * Creates and manages visualization matrices for state images and their color profiles.
 *
 * <p>This class generates visual representations of images and their associated color profiles for
 * display in user interfaces, debugging tools, and documentation. It creates compact matrix layouts
 * that show both the original images and their color analysis results side by side, making it easy
 * to understand the color characteristics of state images.
 *
 * <p>The visualization matrices are particularly useful for:
 *
 * <ul>
 *   <li>Debugging color-based matching algorithms
 *   <li>Displaying state image previews in configuration tools
 *   <li>Generating documentation of color profiles
 *   <li>Validating K-means clustering results
 * </ul>
 *
 * @see StateImage
 * @see MatBuilder
 * @see ColorCluster
 */
@Component
public class ProfileMatrixBuilder {

    private final BrobotProperties brobotProperties;
    private final ImageLoader getImage;

    /** Default width and height for visualization thumbnails in pixels */
    private int imgsWH = 40;

    /** Spacing between visualization elements in pixels */
    private int spaceBetween = 2;

    @Autowired
    public ProfileMatrixBuilder(BrobotProperties brobotProperties, ImageLoader getImage) {
        this.brobotProperties = brobotProperties;
        this.getImage = getImage;
    }

    /**
     * Returns a Mat with the image files shown horizontally.
     *
     * @param img the image with the files to be shown
     * @return Mat with the image files shown horizontally
     */
    public Mat getImagesMat(StateImage img) {
        List<Mat> imgMats = getImage.getMats(img, ColorCluster.ColorSchemaName.BGR);
        return new MatBuilder()
                .setName(img.getName() + "_imgMats")
                .setSubmatMaxHeight(imgsWH)
                .setSubmatMaxWidth(imgsWH)
                .setSpaceBetween(spaceBetween)
                .setSubMats(imgMats)
                .build();
    }

    /**
     * Creates a visualization matrix showing the mean color profile of a state image.
     *
     * <p>This method generates a single color swatch representing the average color of the entire
     * image, which is useful for quick visual identification of images based on their dominant
     * color.
     *
     * @param img The state image whose color profile to visualize
     * @return A Mat containing the mean color visualization
     */
    public Mat getProfilesMat(StateImage img) {
        Mat profile =
                img.getColorCluster()
                        .getMat(BGR, ColorInfo.ColorStat.MEAN, new Size(imgsWH, imgsWH));
        return new MatBuilder()
                .setName(img.getName() + "_profile")
                .setSubmatMaxHeight(imgsWH)
                .setSubmatMaxWidth(imgsWH)
                .addSubMat(new Location(0, 0), profile)
                .build();
    }

    /**
     * Creates a visualization matrix showing K-means color clusters for a state image.
     *
     * <p>This method generates a horizontal array of color swatches, each representing one of the
     * K-means clusters identified in the image. The swatches are arranged from most to least
     * dominant, providing a visual color palette for the image.
     *
     * @param img The state image containing K-means profiles
     * @param kMeans The number of clusters to visualize
     * @return A Mat containing the K-means cluster visualizations arranged horizontally
     */
    public Mat getKmeansProfilesMat(StateImage img, int kMeans) {
        List<Mat> profiles =
                img.getKmeansProfilesAllSchemas()
                        .getColorProfileMats(kMeans, new Size(imgsWH, imgsWH));
        return new MatBuilder()
                .setName(img.getName() + "_kmeansProfile")
                .setSubmatMaxHeight(imgsWH)
                .setSubmatMaxWidth(imgsWH)
                .addHorizontalSubmats(profiles)
                .build();
    }

    /**
     * Creates a visualization matrix based on the specified action options.
     *
     * <p>This method selects between mean color profile or K-means cluster visualization based on
     * the K-means setting in the action options. It provides flexibility in choosing the
     * appropriate visualization type for different automation scenarios.
     *
     * @param img The state image to visualize
     * @param actionConfig Options specifying the visualization type: - kmeans < 0: Use default from
     *     BrobotProperties - kmeans = 0: Show mean color profile only - kmeans > 0: Show K-means
     *     clusters
     * @return A Mat containing the appropriate color profile visualization
     */
    public Mat getProfilesMat(StateImage img, ActionConfig actionConfig) {
        // For now, use default k-means value from framework settings
        // Different ActionConfig implementations could have different k-means settings
        int kMeans = brobotProperties.getAnalysis().getKMeansInProfile();
        if (kMeans == 0) return getProfilesMat(img);
        return getKmeansProfilesMat(img, kMeans);
    }

    /**
     * Sets visualization matrices on a state image for both images and color profiles.
     *
     * <p>This method generates and stores both the image thumbnails matrix and the color profile
     * matrix in the state image object. These matrices are then available for display or further
     * processing.
     *
     * @param img The state image to update with visualization matrices. This object is modified by
     *     setting its imagesMat and profilesMat fields.
     */
    public void setMats(StateImage img) {
        img.setImagesMat(getImagesMat(img));
        img.setProfilesMat(getProfilesMat(img));
    }
}
