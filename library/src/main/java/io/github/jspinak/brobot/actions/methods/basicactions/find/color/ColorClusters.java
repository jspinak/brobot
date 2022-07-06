package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.reports.Report;
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
    public void sort(ActionOptions.Color color) {
        //if (color == ActionOptions.Color.KMEANS) {
        //    clusters.sort(Comparator.comparing(ColorCluster::getScore).reversed());
        //    clusters.sort(Comparator.comparing(ColorCluster::getMatchingPixels).reversed());
        //}
        //if (color == ActionOptions.Color.MU)
            clusters.sort(Comparator.comparing(ColorCluster::getScore));
        /*
        for (int i=0; i<Math.min(50,clusters.size()); i++) {
            ColorCluster cc = clusters.get(i);
            Report.println("score#"+i+" = "+cc.getScore()+" pixel = "+cc.getImage().dump());
        }
        if (!clusters.isEmpty()) Report.println("worst score = "+clusters.get(clusters.size()-1).getScore());
        */
    }

    void printClusters() {
        Report.println("cluster size = "+ clusters.size());
        for (int i=0; i<Math.min(clusters.size(), 10); i++) {
            ColorCluster cc = clusters.get(i);
            Report.print(cc.getImage().dump());
            Report.formatln(" score=%.1f x.y=%d.%d", cc.getScore(), cc.getRegion().x, cc.getRegion().y);
        }
    }

}
