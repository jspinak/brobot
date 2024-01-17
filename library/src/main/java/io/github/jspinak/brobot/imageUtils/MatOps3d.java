package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.opencv_core.Mat.ones;

/**
 * 3d versions of common OpenCV operations that only accept one channel.
 */
@Component
public class MatOps3d {

    private final BufferedImageOps bufferedImageOps;

    public MatOps3d(BufferedImageOps bufferedImageOps) {
        this.bufferedImageOps = bufferedImageOps;
    }

    /**
     * Combine a list of 3 channel Mats into a single 3 channel Mat with one column per channel.
     * Brobot Images can have multiple image files associates with them. Therefore, to analyze the combined pixels
     * across all image files, we need to aggregate them into one column per channel. This is because each image file
     * will likely have a different size.
     * @param mats the list of Mats to concatenate
     * @return the concatenated Mat
     */
    public Mat vConcatToSingleColumnPerChannel(List<Mat> mats) {
        if (mats.isEmpty()) return new Mat();
        MatVector columnMats = new MatVector(3);
        for (int i=0; i<3; i++) {
            columnMats.put(i, mats.get(0));
        }
        for (int m=0; m<mats.size(); m++) {
            Mat colMat = mats.get(m).reshape(0, (int) mats.get(m).total());
            MatVector colMatVec = sPlit(colMat);
            for (int i=0; i<3; i++) {
                if (m==0) {
                    columnMats.put(i, colMatVec.get(i));
                } else {
                    vconcat(columnMats.get(i), colMatVec.get(i), columnMats.get(i));
                }
            }
        }
        return mErge(columnMats);
    }

    /**
     * Sets 3d labels and centers for k-means clustering.
     * @param image the image to cluster
     * @param numberOfCenters the number of clusters
     * @param labels the output labels
     * @param termCriteria the termination criteria for k-means clustering
     * @param attempts the number of attempts to run k-means
     * @param centers the output centers
     * @return an array with the compactness score for each channel
     */
    public double[] kMeans(Mat image, int numberOfCenters, Mat labels,
                            TermCriteria termCriteria, int attempts, Mat centers) {
        Mat imageCV_32F = new Mat();
        image.convertTo(imageCV_32F, CV_32F);
        MatVector channels = new MatVector(3);
        split(imageCV_32F, channels);
        MatVector centersVector = new MatVector(3);
        MatVector labelsVector = new MatVector(3);
        double[] compactness = new double[3];
        for (int i=0; i<3; i++) {
            Mat channelLabels = new Mat();
            Mat channelCenters = new Mat();
            compactness[i] = kmeans(channels.get(i), numberOfCenters, channelLabels, termCriteria,
                    attempts, KMEANS_PP_CENTERS, channelCenters);
            labelsVector.put(i, channelLabels);
            centersVector.put(i, channelCenters);
        }
        merge(labelsVector, labels);
        merge(centersVector, centers);
        return compactness;
    }

    /**
     * Compare each channel of the image to the corresponding double in the array.
     * @param src the image to compare
     * @param cmpTo the array of doubles to compare to
     * @param cmpop the comparison operator
     * @return a 3 channel Mat with the comparison results
     */
    public Mat cOmpare(Mat src, double[] cmpTo, int cmpop) {
        MatVector channels = new MatVector(3);
        split(src, channels);
        MatVector maskVector = new MatVector(3);
        for (int i=0; i<3; i++) {
            Mat channelMask = new Mat(src.size(), CV_8UC1);
            compare(channels.get(i), new Mat(new Scalar(cmpTo[i])), channelMask, cmpop);
            maskVector.put(i, channelMask);
        }
        Mat mask = new Mat();
        merge(maskVector, mask);
        return mask;
    }

    /**
     * Compare each channel of the source Mat to each channel of the comparison Mat.
     *
     * @param src the source Mat
     * @param cmpTo the comparison Mat
     * @param dst the mask to set
     * @param cmpop the comparison operator
     * @return the comparison mask
     */
    public Mat cOmpare(Mat src, Mat cmpTo, Mat dst, int cmpop) {
        int numberOfChannels = Math.min(src.channels(), cmpTo.channels());
        MatVector channels = new MatVector(numberOfChannels);
        split(src, channels);
        MatVector cmpToChannels = new MatVector(numberOfChannels);
        split(cmpTo, cmpToChannels);
        MatVector maskVector = new MatVector(numberOfChannels);
        for (int i=0; i<numberOfChannels; i++) {
            Mat channelMask = new Mat(src.size(), CV_8UC1);
            compare(channels.get(i), cmpToChannels.get(i), channelMask, cmpop);
            maskVector.put(i, channelMask);
        }
        merge(maskVector, dst);
        return dst;
    }

    /**
     * Returns a 3-channel mask of a comparison of the src and cmpTo Mat(s).
     * @param src first Mat to compare
     * @param cmpTo second Mat to compare
     * @param cmpop the comparison operator
     * @return a 3-channel mask
     */
    public Mat cOmpare(Mat src, Mat cmpTo, int cmpop) {
        Mat mat = new Mat();
        cOmpare(src, cmpTo, mat, cmpop);
        return mat;
    }

    public MatVector sPlit(Mat src) {
        MatVector channels = new MatVector(src.channels());
        split(src, channels);
        return channels;
    }

    public Mat getFirstChannel(Mat src) {
        MatVector matVector = sPlit(src);
        Mat firstChannel = matVector.get(0);
        return firstChannel;
    }

    public Mat mErge(MatVector matVector) {
        Mat merged = new Mat();
        merge(matVector, merged);
        return merged;
    }

    /**
     * OpenCV's meanStdDev works with 3 channel images but only 1 channel masks (CV_8UC1)
     * @param src the image to compute the mean and standard deviation of
     * @param mask the 3 channel mask to use
     * @return the mean (index 0) and standard deviation (index 1) of the image
     */
    public MatVector mEanStdDev(Mat src, Mat mask) {
        MatVector meanStddev = new MatVector(2);
        Mat mean = new Mat(src.size(), src.type());
        Mat stddev = new Mat(src.size(), src.type());
        MatVector maskVector = sPlit(mask);
        meanStdDev(src, mean, stddev, maskVector.get(0));
        meanStdDev(src, mean, stddev, maskVector.get(1));
        meanStdDev(src, mean, stddev, maskVector.get(2));
        MatVector srcVector = sPlit(src);
        MatVector meanVector = new MatVector(3);
        MatVector stddevVector = new MatVector(3);
        for (int i=0; i<3; i++) {
            Mat channelMean = new Mat();
            Mat channelStddev = new Mat();
            meanStdDev(srcVector.get(i), channelMean, channelStddev, maskVector.get(i));
            meanVector.put(i, channelMean);
            stddevVector.put(i, channelStddev);
        }
        merge(meanVector, mean);
        merge(stddevVector, stddev);
        meanStddev.put(0, mean);
        meanStddev.put(1, stddev);
        return meanStddev;
    }

    public void minMax(Mat src, DoublePointer min, DoublePointer max, Mat mask) {
        MatVector channels = new MatVector(3);
        MatVector masks = new MatVector(3);
        split(src, channels);
        split(mask, masks);
        for (int i=0; i<3; i++) {
            DoublePointer channelMin = new DoublePointer(1);
            DoublePointer channelMax = new DoublePointer(1);
            Point minLoc = new Point();
            Point maxLoc = new Point();
            minMaxLoc(channels.get(i), channelMin, channelMax, minLoc, maxLoc, masks.get(i));
            min.put(i, channelMin.get(0));
            max.put(i, channelMax.get(0));
        }
    }

    /**
     * updates the minimum indices and minimum scores for each channel
     * @param minIndices a Mat with the current minimum indices, representing the images with the smallest scores
     * @param bestScores a Mat with the current best scores
     * @param challenger a Mat with the challenger values (image scores)
     * @param challengerIndex the index of the challenger
     */
    public void minIndex(Mat minIndices, Mat bestScores, Mat challenger, int challengerIndex) {
        Mat newMinMask = new Mat(minIndices.size(), minIndices.type());
        cOmpare(challenger, bestScores, newMinMask, CMP_LT);
        minIndices.setTo(new Mat(new Scalar(challengerIndex)), newMinMask);
        challenger.copyTo(bestScores, newMinMask);
    }

    public Mat replace2DmaskWith3Dmat(Mat mask, Mat mat) {
        MatVector channels = new MatVector(3);
        split(mat, channels);
        for (int i=0; i<3; i++) {
            channels.get(i).setTo(new Mat(new Scalar(0)), mask);
        }
        Mat merged = new Mat();
        merge(channels, merged);
        return merged;
    }

    /**
     * The indices Mat is reused to store the indices of the best scores. It is most likely passed to this
     * method with the indices of the best scores from the previous iteration. The maxValues Mat is updated
     * with the max values from the current iteration.
     *
     * The OpenCV function 'compare' only works on matrices with 1 channel. In this function, all channels
     * are used and the challenger wins only when it has the highest value in the majority of the channels.
     *
     * @param maxValues the Mat with the largest values
     * @param challenger the Mat to compare it with
     * @param indices the destination Mat with indices
     * @param scalar the index corresponding to the challenger Mat
     */
    public void getIndicesOfMax(Mat maxValues, Mat challenger, Mat indices, int scalar) {
        Mat challengerWinsMask = new Mat(maxValues.size(), CV_8UC3);
        Mat scalarMat = new Mat(maxValues.size(), CV_8UC3, new Scalar(scalar, scalar, scalar, 0));
        cOmpare(maxValues, challenger, challengerWinsMask, CMP_LT); // mask = challenger > maxValues
        challenger.copyTo(maxValues, challengerWinsMask);
        scalarMat.copyTo(indices, challengerWinsMask);
    }

    /**
     * The inRange function of OpenCV will return a 1 channel mask, in which each cell is 255 if the
     * corresponding cell in every channel of the source Mat matches the lower and upper bounds.
     * This function will return a 3 channel mask where each cell is 255 if
     * the cell in that channel matches the lower and upper bounds.
     * @param src the source Mat
     * @param dst the destination Mat
     * @param lowerb the lower bound
     * @param upperb the upper bound
     */
    public void inrange(Mat src, Mat dst, int lowerb, int upperb) {
        MatVector channels = new MatVector(3);
        split(src, channels);
        MatVector masks = new MatVector(3);
        for (int i=0; i<3; i++) {
            Mat mask = new Mat(src.size(), CV_8UC1);
            inRange(channels.get(i), new Mat(new Scalar(lowerb)), new Mat(new Scalar(upperb)), mask);
            masks.put(i, mask);
        }
        merge(masks, dst);
    }

    public Mat getMatWithOnlyTheseIndices(Mat indices, Set<Integer> indicesToKeep) {
        Mat mask = new Mat(indices.size(), indices.type());
        for (int i : indicesToKeep) {
            Mat mask2 = new Mat(indices.size(), indices.type());
            inrange(indices, mask2, i, i);
            bitwise_or(mask, mask2, mask);
        }
        // the mask will have 255 for the indices to keep and 0 for the others
        Mat onlyIndicesToKeep = new Mat(indices.size(), indices.type());
        Report.println();
        bitwise_and(indices, mask, onlyIndicesToKeep);
        return onlyIndicesToKeep;
    }

    public List<Integer> cOuntNonZero(Mat mat) {
        MatVector matVector = sPlit(mat);
        List<Integer> counts = new ArrayList<>();
        for (Mat m : matVector.get()) {
            counts.add(countNonZero(m));
        }
        return counts;
    }

    public int getMaxNonZeroCellsByChannel(Mat mat) {
        List<Integer> counts = cOuntNonZero(mat);
        return Collections.max(counts);
    }

    public Mat bItwise_and(Mat mat1, Mat mat2) {
        MatVector vec1 = sPlit(mat1);
        MatVector vec2 = sPlit(mat2);
        MatVector andVec = sPlit(new Mat(mat1.size(), mat1.type()));
        for (int i=0; i<Math.min(vec1.size(),vec2.size()); i++) {
            if (vec1.get(i).rows() != vec2.get(i).rows()) { // print out mismatch. program will end.
                System.out.println("rows: " + vec1.get(i).rows() + " " + vec2.get(i).rows());
                System.out.println("cols: " + vec1.get(i).cols() + " " + vec2.get(i).cols());
            }
            bitwise_and(vec1.get(i), vec2.get(i), andVec.get(i));
        }
        return mErge(andVec);
    }

    public Mat bItwise_or(Mat mat1, Mat mat2) {
        MatVector vec1 = sPlit(mat1);
        MatVector vec2 = sPlit(mat2);
        MatVector vec = sPlit(new Mat(mat1.size(), mat1.type()));
        for (int i=0; i<Math.min(vec1.size(),vec2.size()); i++) {
            if (vec1.get(i).rows() != vec2.get(i).rows()) { // print out mismatch. program will end.
                System.out.println("rows: " + vec1.get(i).rows() + " " + vec2.get(i).rows());
                System.out.println("cols: " + vec1.get(i).cols() + " " + vec2.get(i).cols());
            }
            bitwise_or(vec1.get(i), vec2.get(i), vec.get(i));
        }
        return mErge(vec);
    }

    public Mat bItwise_not(Mat mat1) {
        MatVector vec1 = sPlit(mat1);
        MatVector vec = sPlit(new Mat(mat1.size(), mat1.type()));
        for (int i=0; i<vec1.get().length; i++) {
            bitwise_not(vec1.get(i), vec.get(i));
        }
        return mErge(vec);
    }

    public static Mat createColorMat(Size size, Scalar colorScalar) {
        return new Mat(size, CV_8UC3, colorScalar);
    }

    /**
     * Add the color to the original Mat where the mask is turned on (255).
     * @param original the base Mat
     * @param mask shows where to color the base Mat
     * @param colorToAdd the color to use
     */
    public void addColorToMat(Mat original, Mat mask, Scalar colorToAdd) {
        Mat colorMat = createColorMat(original.size(), colorToAdd);
        colorMat.copyTo(original, mask);
    }

    /**
     * All channel get the same values.
     * @param values Max of 9 cell values.
     * @return the new Mat
     */
    public Mat makeMat3D(short... values) {
        Mat channel1 = MatOps.makeMat(values);
        Mat channel2 = MatOps.makeMat(values);
        Mat channel3 = MatOps.makeMat(values);
        MatVector matVector = new MatVector(channel1, channel2, channel3);
        return mErge(matVector);
    }

    public Mat makeMat3D(short[] channel1, short[] channel2, short[] channel3) {
        Mat ch1 = MatOps.makeMat(channel1);
        Mat ch2 = MatOps.makeMat(channel2);
        Mat ch3 = MatOps.makeMat(channel3);
        MatVector matVector = new MatVector(ch1, ch2, ch3);
        return mErge(matVector);
    }

    public Pattern makeTestPattern(short[] values) {
        Mat mat = makeMat3D(values);
        BufferedImage bufferedImage = bufferedImageOps.convert(mat);
        return new Pattern(bufferedImage);
    }

}
