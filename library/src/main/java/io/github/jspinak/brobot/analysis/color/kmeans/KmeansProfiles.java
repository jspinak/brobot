package io.github.jspinak.brobot.analysis.color.kmeans;

import java.util.*;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.ColorSchema;

import lombok.Getter;

/**
 * Manages a collection of K-means clustering profiles for a single color schema.
 *
 * <p>This class maintains a set of pre-computed K-means clustering results for different numbers of
 * cluster centers (k values). By calculating these profiles in advance for commonly used values
 * (typically 2-10), the framework can quickly access clustering results without the computational
 * overhead of running K-means at runtime.
 *
 * <p>Benefits of pre-computation:
 *
 * <ul>
 *   <li>Significant performance improvement for color-based matching operations
 *   <li>Consistent clustering results across multiple runs
 *   <li>Reduced CPU/GPU usage during automation execution
 *   <li>Ability to analyze and optimize cluster configurations offline
 * </ul>
 *
 * <p>The class stores profiles indexed by the number of centers, allowing quick retrieval of the
 * appropriate clustering configuration based on the complexity of the color analysis required.
 *
 * @see KmeansProfile
 * @see KmeansProfilesAllSchemas
 * @see ColorCluster
 */
@Getter
public class KmeansProfiles {

    private ColorCluster.ColorSchemaName colorSchemaName;
    private Map<Integer, KmeansProfile> profiles = new HashMap<>();

    /**
     * Adds a K-means profile to the collection.
     *
     * <p>The profile is indexed by its number of centers, replacing any existing profile with the
     * same number of centers.
     *
     * @param profile The {@link KmeansProfile} to add to the collection
     */
    public void add(KmeansProfile profile) {
        profiles.put(profile.getNumberOfCenters(), profile);
    }

    /**
     * Retrieves a K-means profile for the specified number of centers.
     *
     * @param numberOfCenters The number of cluster centers (k value) to retrieve
     * @return An Optional containing the {@link KmeansProfile} if found, or empty if no profile
     *     exists for the specified number of centers
     */
    public Optional<KmeansProfile> get(int numberOfCenters) {
        if (profiles.containsKey(numberOfCenters))
            return Optional.of(profiles.get(numberOfCenters));
        return Optional.empty();
    }

    /**
     * Determines the optimal number of means based on cluster compactness.
     *
     * <p>This method would analyze all stored profiles and select the one with the best compactness
     * metric, indicating well-separated clusters. Compactness is typically measured by the
     * within-cluster sum of squares (WCSS).
     *
     * <p>TODO: Implement this method to analyze compactness metrics and return the optimal number
     * of clusters.
     *
     * @return The number of centers that produces the most compact clustering, currently returns 0
     *     as placeholder
     */
    private int getMostCompactNumberOfMeans() {
        // sort by compactness, return smallest
        return 0;
    }

    /**
     * Checks if a profile exists for the specified number of means.
     *
     * @param mean The number of cluster centers to check for
     * @return true if a profile exists for the specified number of means, false otherwise
     */
    public boolean containsMean(int mean) {
        return profiles.containsKey(mean);
    }

    /**
     * Creates a combined color cluster from separate BGR and HSV color schemas.
     *
     * <p>This utility method combines color information from both color spaces into a single {@link
     * ColorCluster} object, which provides a complete color profile for matching operations.
     *
     * @param bgrSchema The color schema in BGR color space
     * @param hsvSchema The color schema in HSV color space
     * @return A {@link ColorCluster} containing both BGR and HSV color information
     */
    private ColorCluster getColorProfile(ColorSchema bgrSchema, ColorSchema hsvSchema) {
        ColorCluster colorCluster = new ColorCluster();
        colorCluster.put(ColorCluster.ColorSchemaName.BGR, bgrSchema);
        colorCluster.put(ColorCluster.ColorSchemaName.HSV, hsvSchema);
        return colorCluster;
    }
}
