package io.github.jspinak.brobot.analysis.histogram;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_DIST_L2;
import static org.bytedeco.opencv.global.opencv_imgproc.EMD;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Compares histograms using the Earth Mover's Distance (EMD) metric. This class provides
 * functionality to compare histograms between images and regions, enabling similarity analysis
 * based on color distribution.
 *
 * <p>The comparison uses the Wasserstein metric (EMD) which measures the minimum cost of
 * transforming one histogram into another, providing a robust measure of similarity between color
 * distributions.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Compares histograms using Earth Mover's Distance
 *   <li>Supports region-based histogram comparison
 *   <li>Processes multiple regions to find best matches
 *   <li>Returns sorted matches based on similarity scores
 * </ul>
 *
 * @see HistogramExtractor
 * @see HistogramRegions
 * @see Match
 */
@Component
public class HistogramComparator {

    private final HistogramExtractor getHistograms;

    /**
     * Constructs a HistogramComparator instance with the specified histogram service.
     *
     * @param getHistograms service for computing histograms from images
     */
    public HistogramComparator(HistogramExtractor getHistograms) {
        this.getHistograms = getHistograms;
    }

    /**
     * Computes the total Earth Mover's Distance between two sets of histogram regions. This method
     * compares corresponding regions (corners and center) and sums their individual EMD scores to
     * produce an overall similarity measure.
     *
     * @param histRegs1 the first collection of histograms to compare
     * @param histRegs2 the second collection of histograms to compare
     * @param indexedColumn a column matrix containing indices [0,1,2...histogram_size] required for
     *     the OpenCV EMD function
     * @return the total EMD score across all regions (lower values indicate higher similarity)
     */
    public double compare(
            HistogramRegions histRegs1, HistogramRegions histRegs2, Mat indexedColumn) {
        double sim = 0;
        sim +=
                getSimScore(
                        histRegs1.getTopLeft().getHistogram(),
                        histRegs2.getTopLeft().getHistogram(),
                        indexedColumn);
        sim +=
                getSimScore(
                        histRegs1.getTopRight().getHistogram(),
                        histRegs2.getTopRight().getHistogram(),
                        indexedColumn);
        sim +=
                getSimScore(
                        histRegs1.getBottomLeft().getHistogram(),
                        histRegs2.getBottomLeft().getHistogram(),
                        indexedColumn);
        sim +=
                getSimScore(
                        histRegs1.getBottomRight().getHistogram(),
                        histRegs2.getBottomRight().getHistogram(),
                        indexedColumn);
        sim +=
                getSimScore(
                        histRegs1.getEllipse().getHistogram(),
                        histRegs2.getEllipse().getHistogram(),
                        indexedColumn);
        return sim;
    }

    /**
     * Calculates the Earth Mover's Distance between two histograms. The indexed column is prepended
     * to each histogram to create the signature format required by OpenCV's EMD function.
     *
     * @param hist1 the first histogram
     * @param hist2 the second histogram
     * @param indexedColumn column of indices for EMD calculation
     * @return the EMD score using L2 distance metric
     */
    private double getSimScore(Mat hist1, Mat hist2, Mat indexedColumn) {
        Mat h1 = addIndexedColumn(hist1, indexedColumn);
        Mat h2 = addIndexedColumn(hist2, indexedColumn);
        return EMD(h1, h2, CV_DIST_L2);
    }

    /**
     * Creates an indexed column matrix containing sequential values from 0 to totalBins-1. This
     * column is required for the EMD function to properly index histogram bins.
     *
     * @param totalBins the number of histogram bins
     * @return a Mat containing sequential indices
     */
    private Mat setIndexedColumn(int totalBins) {
        // Create a column vector (totalBins x 1) with CV_32F type
        Mat indexedColumn = new Mat(totalBins, 1, CV_32F);
        for (int i = 0; i < totalBins; i++) {
            indexedColumn.ptr(i, 0).putFloat((float) i);
        }
        return indexedColumn;
    }

    /**
     * Prepends an indexed column to a histogram matrix. This creates the signature format required
     * by the EMD function, where each row contains [index, histogram_value].
     *
     * @param mat the histogram matrix
     * @param indexedColumn the column of indices to prepend
     * @return a new Mat with the indexed column concatenated to the histogram
     */
    private Mat addIndexedColumn(Mat mat, Mat indexedColumn) {
        // Create a copy to avoid modifying the original indexedColumn
        Mat convertedColumn = new Mat();
        indexedColumn.convertTo(convertedColumn, mat.type());

        // Concatenate the indexed column with the histogram
        MatVector mv = new MatVector(convertedColumn, mat);
        Mat result = new Mat();
        hconcat(mv, result);

        convertedColumn.release(); // Clean up the temporary matrix
        return result;
    }

    /**
     * Compares an image's histogram against histograms from multiple scene regions. This method
     * searches for the best matching regions by computing EMD scores between the image histogram
     * and each region's histogram.
     *
     * @param image the {@link StateImage} whose histogram will be compared
     * @param regions list of {@link Region} objects defining areas to search within the scene
     * @param sceneHSV the scene image in HSV color space
     * @return a sorted list of {@link Match} objects, ordered by similarity score (best first)
     * @see StateImage
     * @see Region
     * @see Match
     */
    public List<Match> compareAll(StateImage image, List<Region> regions, Mat sceneHSV) {
        HistogramRegions histogramRegions = getHistograms.getHistogramsHSV(image);
        Mat indexedColumn =
                setIndexedColumn(getHistograms.getHueBins()); // only the hue bins are considered
        // //imageRegionsHistograms.getTotalBins());
        List<Match> matchList = new ArrayList<>();
        double bestScore = -1;
        for (Region reg : regions) {
            Mat maskOnScene = new Mat(sceneHSV, reg.getJavaCVRect());
            HistogramRegions sceneHistRegs = getHistograms.getHistogramFromRegion(maskOnScene);
            double score = compare(sceneHistRegs, histogramRegions, indexedColumn);
            if (bestScore == -1) bestScore = score;
            if (score < bestScore) bestScore = score;
            Match match =
                    new Match.Builder()
                            .setRegion(reg)
                            .setStateObjectData(image)
                            .setHistogram(sceneHistRegs.getCombined().getHistogram())
                            .setSimScore(score)
                            .build();
            matchList.add(match);
        }
        return matchList.stream().sorted(Comparator.comparing(Match::getScore)).toList();
    }
}
