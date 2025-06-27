package io.github.jspinak.brobot.analysis.color.kmeans;

import io.github.jspinak.brobot.model.analysis.color.ColorSchema;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorInfo.ColorStat.MEAN;

/**
 * Manages K-means clustering profiles for multiple color schemas in the Brobot framework.
 * 
 * <p>This class serves as a central repository for K-means clustering results across different
 * color spaces (BGR and HSV). It maintains separate {@link KmeansProfiles} for each color schema,
 * allowing the framework to analyze images using different color representations and choose
 * the most appropriate one for specific automation tasks.</p>
 * 
 * <p>The class supports two primary color schemas:
 * <ul>
 *   <li><b>BGR</b>: Blue-Green-Red color space, standard for OpenCV operations</li>
 *   <li><b>HSV</b>: Hue-Saturation-Value color space, often better for color-based segmentation</li>
 * </ul>
 * </p>
 * 
 * <p>Key functionality includes:
 * <ul>
 *   <li>Storage and retrieval of K-means profiles for different numbers of clusters</li>
 *   <li>Generation of {@link ColorCluster} objects that combine both BGR and HSV representations</li>
 *   <li>Creation of visualization matrices for color profiles</li>
 *   <li>Validation of profile availability across color schemas</li>
 * </ul>
 * </p>
 * 
 * @see KmeansProfiles
 * @see KmeansProfile
 * @see ColorCluster
 * @see ColorSchema
 */
@Getter
@Setter
public class KmeansProfilesAllSchemas {

    private Map<ColorCluster.ColorSchemaName, KmeansProfiles> kmeansProfiles = Map.of(
            BGR, new KmeansProfiles(),
            ColorCluster.ColorSchemaName.HSV, new KmeansProfiles()
    );

    /**
     * Retrieves the K-means profiles for a specific color schema.
     * 
     * @param colorSchemaName The color schema (BGR or HSV) to retrieve profiles for
     * @return The {@link KmeansProfiles} containing all clustering results for the specified schema
     */
    public KmeansProfiles getKmeansProfiles(ColorCluster.ColorSchemaName colorSchemaName) {
        return kmeansProfiles.get(colorSchemaName);
    }

    /**
     * Adds a K-means profile to the specified color schema's collection.
     * 
     * @param colorSchemaName The color schema (BGR or HSV) to add the profile to
     * @param kmeansProfile The K-means clustering profile to add
     */
    public void addKmeansProfile(ColorCluster.ColorSchemaName colorSchemaName, KmeansProfile kmeansProfile) {
        kmeansProfiles.get(colorSchemaName).add(kmeansProfile);
    }

    /**
     * Retrieves a specific K-means profile for a given color schema and number of centers.
     * 
     * @param colorSchemaName The color schema (BGR or HSV) to retrieve from
     * @param numberOfCenters The number of cluster centers (k value) for the desired profile
     * @return The {@link KmeansProfile} for the specified parameters, or null if not found
     */
    public KmeansProfile getKmeansProfile(ColorCluster.ColorSchemaName colorSchemaName, int numberOfCenters) {
        return kmeansProfiles.get(colorSchemaName).getProfiles().get(numberOfCenters);
    }

    /**
     * Replaces the entire K-means profiles collection for a specific color schema.
     * 
     * <p>Note: The method name contains a typo (missing 's' in 'Kmeans'). Consider
     * renaming to 'addKmeansProfiles' for consistency.</p>
     * 
     * @param colorSchemaName The color schema (BGR or HSV) to update
     * @param kmeansProfiles The new {@link KmeansProfiles} collection to set
     */
    public void addKmeanProfiles(ColorCluster.ColorSchemaName colorSchemaName, KmeansProfiles kmeansProfiles) {
        this.kmeansProfiles.put(colorSchemaName, kmeansProfiles);
    }

    /**
     * Checks if a K-means profile exists for the specified color schema and number of centers.
     * 
     * @param colorSchemaName The color schema (BGR or HSV) to check
     * @param numberOfCenters The number of cluster centers to look for
     * @return true if a profile exists for the given parameters, false otherwise
     */
    public boolean contains(ColorCluster.ColorSchemaName colorSchemaName, int numberOfCenters) {
        return kmeansProfiles.get(colorSchemaName).getProfiles().containsKey(numberOfCenters);
    }

    /**
     * Checks if K-means profiles exist for both BGR and HSV color schemas with the specified number of centers.
     * 
     * <p>This method is useful for ensuring that complete color analysis is available before
     * attempting to generate combined color clusters.</p>
     * 
     * @param numberOfCenters The number of cluster centers to check for
     * @return true if profiles exist for both BGR and HSV schemas, false otherwise
     */
    public boolean containsAll(int numberOfCenters) {
        return kmeansProfiles.get(BGR).getProfiles().containsKey(numberOfCenters) &&
                kmeansProfiles.get(ColorCluster.ColorSchemaName.HSV).getProfiles().containsKey(numberOfCenters);
    }

    /**
     * Creates ColorClusters from the ColorSchemas from the BGR and HSV KmeansProfile objects.
     * Each KmeansProfile has only one ColorSchema and not a full ColorCluster with both BGR and HSV.
     *
     * @param means number of means to use
     * @return a list of both BGR and HSV ColorClusters
     */
    public List<ColorCluster> getColorProfiles(int means) {
        if (!containsAll(means)) {
            ConsoleReporter.println("KmeansProfiles does not have both BGR and HSV profiles for "+means+" means");
            return new ArrayList<>();
        }
        List<ColorCluster> colorClusters = new ArrayList<>();
        List<ColorSchema> bgrSchemas = kmeansProfiles.get(BGR)
                .get(means).get().getClusters().stream()
                .map(KmeansCluster::getColorSchema)
                .toList();
        List<ColorSchema> hsvSchemas = kmeansProfiles.get(ColorCluster.ColorSchemaName.HSV)
                .get(means).get().getClusters().stream()
                .map(KmeansCluster::getColorSchema)
                .toList();
        for (int i=0; i<means; i++) {
            ColorCluster colorCluster = new ColorCluster();
            colorCluster.put(BGR, bgrSchemas.get(i));
            colorCluster.put(ColorCluster.ColorSchemaName.HSV, hsvSchemas.get(i));
            colorClusters.add(colorCluster);
        }
        return colorClusters;
    }

    /**
     * Generates visualization matrices for all color profiles with the specified number of means.
     * 
     * <p>This method creates OpenCV Mat objects that visually represent each color cluster's
     * mean color in BGR format. These matrices are useful for displaying color profiles
     * in the user interface or for debugging color detection algorithms.</p>
     * 
     * @param means The number of cluster centers to visualize
     * @param size The desired size for each color visualization matrix
     * @return A list of Mat objects, each representing a color cluster's mean BGR value,
     *         or an empty list if profiles for the specified means don't exist
     */
    public List<Mat> getColorProfileMats(int means, Size size) {
        List<Mat> colorProfileMats = new ArrayList<>();
        List<ColorCluster> colorClusters = getColorProfiles(means);
            colorClusters.forEach(colorProfile -> colorProfileMats.add(colorProfile.getMat(BGR, MEAN, size)));
        return colorProfileMats;
    }
}
