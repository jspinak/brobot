package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorInfo.ColorStat.MEAN;

public class KmeansProfilesAllSchemas {

    private Map<ColorCluster.ColorSchemaName, KmeansProfiles> kmeansProfiles = Map.of(
            BGR, new KmeansProfiles(),
            ColorCluster.ColorSchemaName.HSV, new KmeansProfiles()
    );

    public KmeansProfiles getKmeansProfiles(ColorCluster.ColorSchemaName colorSchemaName) {
        return kmeansProfiles.get(colorSchemaName);
    }

    public void addKmeansProfile(ColorCluster.ColorSchemaName colorSchemaName, KmeansProfile kmeansProfile) {
        kmeansProfiles.get(colorSchemaName).add(kmeansProfile);
    }

    public KmeansProfile getKmeansProfile(ColorCluster.ColorSchemaName colorSchemaName, int numberOfCenters) {
        return kmeansProfiles.get(colorSchemaName).getProfiles().get(numberOfCenters);
    }

    public void addKmeanProfiles(ColorCluster.ColorSchemaName colorSchemaName, KmeansProfiles kmeansProfiles) {
        this.kmeansProfiles.put(colorSchemaName, kmeansProfiles);
    }

    public boolean contains(ColorCluster.ColorSchemaName colorSchemaName, int numberOfCenters) {
        return kmeansProfiles.get(colorSchemaName).getProfiles().containsKey(numberOfCenters);
    }

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
            Report.println("KmeansProfiles does not have both BGR and HSV profiles for "+means+" means");
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

    public List<Mat> getColorProfileMats(int means, Size size) {
        List<Mat> colorProfileMats = new ArrayList<>();
        List<ColorCluster> colorClusters = getColorProfiles(means);
            colorClusters.forEach(colorProfile -> colorProfileMats.add(colorProfile.getMat(BGR, MEAN, size)));
        return colorProfileMats;
    }
}
