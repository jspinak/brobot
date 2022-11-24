package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import lombok.Getter;

import java.util.*;

import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

/**
 * Creates a series of kmeans profiles for commonly used number of centers (2-10)
 * Calculating this up-front will save time and CPU/GPU.
 */
@Getter
public class KmeansProfiles {

    private ColorCluster.ColorSchemaName colorSchemaName;
    private Map<Integer, KmeansProfile> profiles = new HashMap<>();

    public void add(KmeansProfile profile) {
        profiles.put(profile.getNumberOfCenters(), profile);
    }

    public Optional<KmeansProfile> get(int numberOfCenters) {
        if (profiles.containsKey(numberOfCenters))
            return Optional.of(profiles.get(numberOfCenters));
        return Optional.empty();
    }

    private int getMostCompactNumberOfMeans() { // TODO: implement
        // sort by compactness, return smallest
        return 0;
    }

    public boolean containsMean(int mean) {
        return profiles.containsKey(mean);
    }

    private ColorCluster getColorProfile(ColorSchema bgrSchema, ColorSchema hsvSchema) {
        ColorCluster colorCluster = new ColorCluster();
        colorCluster.put(ColorCluster.ColorSchemaName.BGR, bgrSchema);
        colorCluster.put(ColorCluster.ColorSchemaName.HSV, hsvSchema);
        return colorCluster;
    }
}
