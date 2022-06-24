package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.imgproc.Imgproc.*;

@Component
public class HistComparison {

    private ImageRegionsHistograms imageRegionsHistograms;

    public HistComparison(ImageRegionsHistograms imageRegionsHistograms) {
        this.imageRegionsHistograms = imageRegionsHistograms;
    }

    /**
     * Returns the Wasserstein metric (Earth Mover's Distance) as
     * a measure of histogram similarity.
     * @param hist1 the first collection of histogram to compare.
     * @param hist2 the second collection of histogram to compare.
     * @param indexedColumn This is a column of 1,2,3... to size of the histogram, used
     *                      to construct a Mat necessary for the OpenCV EMD function.
     * @return the ChiSqr score.
     */
    public double compare(List<Mat> hist1, List<Mat> hist2, Mat indexedColumn) {
        double sim = 0;
        //int cells = 0;
        for (int i=0; i<hist1.size(); i++) {
            Mat h1 = addIndexedColumn(hist1.get(i), indexedColumn);
            Mat h2 = addIndexedColumn(hist2.get(i), indexedColumn);
            sim += Imgproc.EMD(h1, h2, CV_DIST_L2);
            //cells += hist1.get(i).height();
        }
        //sim /= cells;
        return sim;
    }

    private Mat setIndexedColumn(int totalBins) {
        Mat indexedColumn = new Mat(totalBins, 1, CV_32FC1);
        for (int i=0; i<totalBins; i++) {
            indexedColumn.put(i, 0, new float[]{i+1});
        }
        return indexedColumn;
    }

    private Mat addIndexedColumn(Mat mat, Mat indexedColumn) {
        List<Mat> concatMats = new ArrayList<>();
        concatMats.add(mat);
        concatMats.add(indexedColumn);
        Mat result = new Mat();
        Core.hconcat(concatMats, result);
        return result;
    }

    /**
     * Compare all Patterns in the Image with all regions.
     * @param image
     * @param regions
     * @return the best score per region.
     */
    public LinkedHashMap<Region, Double> compareAll(Image image, List<Region> regions) {
        List<List<Mat>> allImgHists = imageRegionsHistograms.getHistogramsFromAllPatterns(image);
        Mat indexedColumn = setIndexedColumn(imageRegionsHistograms.getTotalBins());
        Map<Region, Double> scores = new HashMap<>();
        for (Region reg : regions) {
            double bestScore = -1;
            List<Mat> regHists = imageRegionsHistograms.getHistogramsFromRegion(reg);
            for (List<Mat> imgHists : allImgHists) {
                double score = compare(regHists, imgHists, indexedColumn);
                if (bestScore == -1 || score < bestScore) bestScore = score;
            }
            scores.put(reg, bestScore);
        }
        LinkedHashMap<Region, Double> regScores = scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
        showHists(regScores);
        return regScores;
    }

    private void showHists(LinkedHashMap<Region, Double> regScores) {
        Region bestReg = regScores.entrySet().iterator().next().getKey();
        imageRegionsHistograms.showMaskedImage(bestReg);
        List<Mat> bestRegHists = imageRegionsHistograms.getHistogramsFromRegion(bestReg);
        for (int i=0; i<bestRegHists.size(); i++) imageRegionsHistograms.showHistogram(bestRegHists.get(i), "best"+i);
    }

}
