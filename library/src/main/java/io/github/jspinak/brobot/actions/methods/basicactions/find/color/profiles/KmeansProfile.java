package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import lombok.Getter;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class KmeansProfile {

    private Mat data;
    private Mat labels;
    private Mat centers;
    private double compactness;
    private List<KmeansCluster> clusters = new ArrayList<>();

    public KmeansProfile(Mat data, Mat labels, Mat centers, double compactness) {
        this.data = data;
        this.labels = labels;
        this.centers = centers;
        this.compactness = compactness;
        setClusters();
    }

    private void setClusters() {
        for (int i=0; i<centers.rows(); i++) {
            clusters.add(new KmeansCluster(centers.get(i,0), i, labels, data));
        }
    }

    public KmeansCluster getDominantCluster() {
        clusters.sort(Comparator.comparing(KmeansCluster::getPercentOfTotalPoints).reversed());
        return clusters.get(0);
    }
}
