package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import org.opencv.core.Mat;

import java.util.*;

/**
 * Contains a list of DistanceMatrix objects for each Pattern in the Image.
 * Each DistanceMatrix is a comparison of the pixel colors in the region with one of the
 * Pattern's color centers (obtained by running k-means on the Pattern's colors).
 * DistanceMatrices can be used with images that have different colors. You could have
 *   an image with both red and yellow and by choosing 2 k-means you would find both of those
 *   colors. This is not the case with the ColorProfile, which in this case would give the best scores
 *   to colors between red and yellow (some variation of orange).
 */
public class DistanceMatrices {

    private Map<String, List<ScoresMat>> colorDifferences = new HashMap<>();

    public void addMatrices(String name, List<ScoresMat> matrices) {
        colorDifferences.put(name, matrices);
    }

    public List<ScoresMat> getAll() {
        List<ScoresMat> allMatrices = new ArrayList<>();
        colorDifferences.values().forEach(allMatrices::addAll);
        return allMatrices;
    }

    public void addAll(DistanceMatrices distanceMatrices) {
        distanceMatrices.colorDifferences.forEach((str, lst) -> {
            if (colorDifferences.containsKey(str)) colorDifferences.get(str).addAll(lst);
            else colorDifferences.put(str, lst);
        });
    }

    private Optional<ScoresMat> getMinimumAt(String key, int row, int col) {
        if (!colorDifferences.containsKey(key)) return Optional.empty();
        List<ScoresMat> allMatrices = colorDifferences.get(key);
        ScoresMat minMatrix = allMatrices.get(0);
        int i = 1;
        while (i < allMatrices.size()) {
            if (allMatrices.get(i).getScores().get(row, col)[0] < minMatrix.getScores().get(row, col)[0])
                minMatrix = allMatrices.get(i);
            i++;
        }
        return Optional.of(minMatrix);
    }

    public ColorClusters getPixels(double minDistance) {
        ColorClusters colorClusters = new ColorClusters();
        if (colorDifferences.isEmpty()) return colorClusters;
        // compare for each String, since different Patterns will most likely have different sized Mats.
        colorDifferences.keySet().forEach(kS -> {
            Mat dist = colorDifferences.get(kS).get(0).getScores();
            for (int i=0; i<dist.rows(); i++) {
                for (int j=0; j<dist.cols(); j++) {
                    Optional<ScoresMat> dm = getMinimumAt(kS, i, j);
                    if (dm.isPresent() && dm.get().getScores().get(i,j)[0] <= minDistance)
                            colorClusters.addCluster(dm.get().getCluster(i,j,1, 1));
                }
            }
        });
        return colorClusters;
    }

}
