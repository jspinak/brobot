package io.github.jspinak.brobot.analysis.color.profiles;

import static org.bytedeco.opencv.global.opencv_core.CMP_EQ;
import static org.bytedeco.opencv.global.opencv_core.countNonZero;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.TermCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.analysis.color.ColorClusterFactory;
import io.github.jspinak.brobot.analysis.color.kmeans.KmeansCluster;
import io.github.jspinak.brobot.analysis.color.kmeans.KmeansProfile;
import io.github.jspinak.brobot.analysis.color.kmeans.KmeansProfilesAllSchemas;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.ColorSchema;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;

/**
 * Generates and manages K-means clustering profiles for state images across multiple color schemas.
 *
 * <p>This class is responsible for creating comprehensive K-means clustering analyses of images in
 * the Brobot framework. It performs K-means clustering on image color data to identify dominant
 * color patterns, which are essential for color-based image matching and state recognition.
 *
 * <p>Key functionality includes:
 *
 * <ul>
 *   <li>Running K-means clustering with various numbers of centers (k values)
 *   <li>Processing images in both BGR and HSV color spaces
 *   <li>Generating statistical profiles for each color cluster
 *   <li>Managing pre-computed profiles for performance optimization
 * </ul>
 *
 * <p>The K-means profiles are used throughout the framework for:
 *
 * <ul>
 *   <li>Dynamic image matching based on color similarity
 *   <li>Identifying state transitions through color changes
 *   <li>Filtering false matches based on color expectations
 *   <li>Optimizing search algorithms by pre-filtering color regions
 * </ul>
 *
 * @see KmeansProfile
 * @see KmeansProfilesAllSchemas
 * @see KmeansCluster
 * @see StateImage
 */
@Component
public class KmeansProfileBuilder {

    @Autowired private BrobotProperties brobotProperties;

    private ColorClusterFactory setColorCluster;
    private ColorMatrixUtilities matOps3d;
    private ProfileSetBuilder setAllProfiles;

    /**
     * Constructs a KmeansProfileBuilder instance with required dependencies.
     *
     * @param setColorCluster Component for creating color clusters from image data
     * @param matOps3d Utility for 3D matrix operations on multi-channel images
     * @param setAllProfiles Component for comprehensive profile initialization
     */
    public KmeansProfileBuilder(
            ColorClusterFactory setColorCluster,
            ColorMatrixUtilities matOps3d,
            ProfileSetBuilder setAllProfiles) {
        this.setColorCluster = setColorCluster;
        this.matOps3d = matOps3d;
        this.setAllProfiles = setAllProfiles;
    }

    /**
     * Sets the kmeans profiles for the given image, for means from 1 to the max means as specified
     * in the settings.
     *
     * @param img the image to set the profiles for
     */
    public void setProfiles(StateImage img) {
        KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
        for (int i = 1; i <= brobotProperties.getAnalysis().getMaxKMeansToStore(); i++) {
            addNewProfiles(kmeansProfilesAllSchemas, img, i);
        }
        img.setKmeansProfilesAllSchemas(kmeansProfilesAllSchemas);
    }

    /**
     * Adds new K-means profiles for both BGR and HSV color schemas to an existing collection.
     *
     * <p>Note: This method contains a bug - it iterates up to maxKMeansToStoreInProfile but should
     * only create profiles for the specified 'means' parameter.
     *
     * @param kmeansProfiles The collection to add new profiles to
     * @param img The state image to analyze
     * @param means The number of cluster centers to use for K-means clustering
     */
    public void addNewProfiles(KmeansProfilesAllSchemas kmeansProfiles, StateImage img, int means) {
        for (int i = 1; i <= brobotProperties.getAnalysis().getMaxKMeansToStore(); i++) {
            KmeansProfile kmeansProfilesForBGR =
                    getProfile(img.getOneColumnBGRMat(), means, ColorCluster.ColorSchemaName.BGR);
            kmeansProfiles.addKmeansProfile(ColorCluster.ColorSchemaName.BGR, kmeansProfilesForBGR);
            KmeansProfile kmeansProfilesForHSV =
                    getProfile(img.getOneColumnHSVMat(), means, ColorCluster.ColorSchemaName.HSV);
            kmeansProfiles.addKmeansProfile(ColorCluster.ColorSchemaName.HSV, kmeansProfilesForHSV);
        }
    }

    /**
     * Produces a KmeansProfile given a one-column Mat, the number of means, and the color schema.
     *
     * @param oneColumnMat the images to set the profile for, as a one-column Mat
     * @param kmeans the number of means to use
     * @param schema the color schema to use
     * @return the kmeans profile
     */
    public KmeansProfile getProfile(
            Mat oneColumnMat, int kmeans, ColorCluster.ColorSchemaName schema) {
        Mat labels = new Mat();
        Mat centers = new Mat();
        double[] compactness =
                matOps3d.kMeans(oneColumnMat, kmeans, labels, new TermCriteria(), 15, centers);
        List<KmeansCluster> clusters =
                getKmeansClusters(oneColumnMat, labels, centers, kmeans, schema);
        return new KmeansProfile(schema, kmeans, labels, centers, compactness, clusters);
    }

    /**
     * Creates K-means cluster objects from clustering results.
     *
     * <p>This method processes the raw K-means output to create structured cluster objects that
     * contain color statistics, center values, and pixel distribution information for each
     * identified cluster.
     *
     * @param oneColumnMat The original one-column image matrix
     * @param labels Matrix containing cluster assignments for each pixel
     * @param centers Matrix containing the center values for each cluster
     * @param kmeans The number of clusters
     * @param schema The color schema being processed (BGR or HSV)
     * @return List of {@link KmeansCluster} objects representing each color cluster
     */
    private List<KmeansCluster> getKmeansClusters(
            Mat oneColumnMat,
            Mat labels,
            Mat centers,
            int kmeans,
            ColorCluster.ColorSchemaName schema) {
        List<KmeansCluster> clusters = new ArrayList<>();
        for (int k = 0; k < kmeans; k++) { // for each kmeans cluster
            Mat center = centers.row(k);
            // get the masks for this cluster for each channel
            Mat masks = matOps3d.cOmpare(labels, new double[] {k, k, k}, CMP_EQ);
            // get the color profiles for this cluster (one for each channel)
            ColorSchema colorSchema = setColorCluster.getColorSchema(oneColumnMat, masks, schema);
            double[] percentOfPointsInChannel = new double[3];
            for (int c = 0; c < 3; c++) {
                Mat mask = matOps3d.sPlit(masks).get(c);
                percentOfPointsInChannel[c] = countNonZero(mask) / (double) (labels.total());
            }
            clusters.add(
                    new KmeansCluster(
                            colorSchema, center, matOps3d.sPlit(masks), percentOfPointsInChannel));
        }
        return clusters;
    }

    /**
     * Ensures all images in a set have K-means profiles for the specified number of clusters.
     *
     * <p>This method performs lazy initialization of K-means profiles, only computing them when
     * they don't already exist. It also ensures that prerequisite data (one-column matrices and
     * color profiles) are initialized if necessary.
     *
     * <p>This is particularly useful when the framework needs to perform color-based operations
     * with a specific number of clusters that wasn't pre-computed.
     *
     * @param allImages Set of state images to process
     * @param kMeans The number of cluster centers to ensure profiles exist for
     */
    public void addKMeansIfNeeded(Set<StateImage> allImages, int kMeans) {
        allImages.forEach(
                img -> {
                    KmeansProfilesAllSchemas profiles = img.getKmeansProfilesAllSchemas();
                    if (profiles == null) profiles = new KmeansProfilesAllSchemas();
                    if (img.getOneColumnBGRMat() == null)
                        setAllProfiles.setMatsAndColorProfiles(
                                img); // initProfileMats.setOneColumnMats(img);
                    if (!profiles.containsAll(kMeans)) {
                        addNewProfiles(profiles, img, kMeans);
                    }
                    img.setKmeansProfilesAllSchemas(profiles);
                });
    }
}
