package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import org.opencv.core.Mat;

import java.util.*;

/**
 * Contains a list of DistanceMatrix objects for each Pattern in the Image.
 * Each DistanceMatrix is a comparison of the pixel colors in the region with one of the
 * Pattern's color centers (obtained by running k-means on the Pattern's colors).
 */
public class DistanceMatrices {

    private Map<String, List<DistanceMatrix>> colorDifferences = new HashMap<>();

    public void addMatrix(String name, List<DistanceMatrix> matrices) {
        colorDifferences.put(name, matrices);
    }

    public List<DistanceMatrix> getAll() {
        List<DistanceMatrix> allMatrices = new ArrayList<>();
        colorDifferences.values().forEach(allMatrices::addAll);
        return allMatrices;
    }

    public void addAll(DistanceMatrices distanceMatrices) {
        distanceMatrices.colorDifferences.forEach((str, lst) -> {
            if (colorDifferences.containsKey(str)) colorDifferences.get(str).addAll(lst);
            else colorDifferences.put(str, lst);
        });
    }

    private Optional<DistanceMatrix> getMinimumAt(String key, int row, int col) {
        if (!colorDifferences.containsKey(key)) return Optional.empty();
        List<DistanceMatrix> allMatrices = colorDifferences.get(key);
        DistanceMatrix minMatrix = allMatrices.get(0);
        int i = 1;
        while (i < allMatrices.size()) {
            if (allMatrices.get(i).getDistance().get(row, col)[0] < minMatrix.getDistance().get(row, col)[0])
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
            Mat dist = colorDifferences.get(kS).get(0).getDistance();
            for (int i=0; i<dist.rows(); i++) {
                for (int j=0; j<dist.cols(); j++) {
                    Optional<DistanceMatrix> dm = getMinimumAt(kS, i, j);
                    if (dm.isPresent() && dm.get().getDistance().get(i,j)[0] <= minDistance)
                            colorClusters.addCluster(dm.get().getCluster(i,j,1, 1));
                }
            }
        });
        return colorClusters;
    }

}
