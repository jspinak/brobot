package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class ColorClusters {

    private List<ColorCluster> clusters = new ArrayList<>();

    public void addCluster(ColorCluster colorCluster) {
        clusters.add(colorCluster);
    }

    public void addAllClusters(ColorClusters colorClusters) {
        clusters.addAll(colorClusters.getClusters());
    }

    // sort primarily by number of matching pixels, then by score
    public void sort() {
        clusters.sort(Comparator.comparing(ColorCluster::getScore).reversed());
        clusters.sort(Comparator.comparing(ColorCluster::getMatchingPixels).reversed());
    }
}
