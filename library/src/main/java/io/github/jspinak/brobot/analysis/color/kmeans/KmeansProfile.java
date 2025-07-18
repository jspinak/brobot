package io.github.jspinak.brobot.analysis.color.kmeans;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;

import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.Comparator;
import java.util.List;

/**
 * KmeansProfile represents a k-means analysis for one number of means and one color schema (BGR, HSV, etc.).
 * It holds a list of clusters, each of which describes, for each channel, the points associated with one of the centers.
 */
@Getter
public class KmeansProfile {

    private ColorCluster.ColorSchemaName colorSchemaName;
    private int numberOfCenters;
    private Mat labels; // 3 channels, shows which cluster each cell belongs to
    private Mat centers; // 3 channels
    private double[] compactness; // one for each channel
    private List<KmeansCluster> clusters; // one per center

    public KmeansProfile(ColorCluster.ColorSchemaName colorSchemaName, int numberOfCenters, Mat labels, Mat centers,
                         double[] compactness, List<KmeansCluster> clusters) {
        this.colorSchemaName = colorSchemaName;
        this.numberOfCenters = numberOfCenters;
        this.labels = labels;
        this.centers = centers;
        this.compactness = compactness;
        this.clusters = clusters;
    }

    /**
     * Retrieves the dominant color cluster based on overall pixel coverage.
     * 
     * <p>This method identifies the cluster that contains the highest percentage
     * of pixels across all color channels. The dominant cluster typically represents
     * the primary color theme of the image and is useful for color-based matching
     * and classification tasks.</p>
     * 
     * <p>Note: This method sorts the clusters list as a side effect, which modifies
     * the internal state of the object.</p>
     * 
     * @return The {@link KmeansCluster} with the highest percentage of pixels
     */
    public KmeansCluster getDominantCluster() {
        clusters.sort(Comparator.comparing(KmeansCluster::getPercentOfPointsOverall).reversed());
        return clusters.get(0);
    }
}
