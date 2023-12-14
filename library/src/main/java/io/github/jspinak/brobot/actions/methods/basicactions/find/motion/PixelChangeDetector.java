package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.imageUtils.MatOps;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Getter
public class PixelChangeDetector {

    private List<Mat> originals = new ArrayList<>();

    /**
     * Applying grayscale reduces the Mat to 1-dimension.
     */
    private boolean useGrayscale;
    private List<Mat> grays = new ArrayList<>();

    /**
     Apply Gaussian blur to the images using a kernel size of 5x5 and standard deviation of 0.
     This helps smooth the images and reduce noise.
     **/
    private boolean useGaussianBlur;
    private int gaussianWidth = 5;
    private int gaussianHeight = 5;
    private int gaussianSigmaX = 0;
    private List<Mat> gaussians = new ArrayList<>();

    /**
     * The absolute difference between pixels is calculated after any grayscale or gaussian operations,
     * but before dilation or threshold operations.
     * This Mat is 1D.
     */
    private Mat absDiff;

    /**
    Dilation Operation: For each pixel in the binary image (absDiff), the maximum pixel value in the neighborhood
    defined by the kernel is computed. If any pixel in the neighborhood has a value of 1 (white), the result at
    the corresponding position in the output image (dilated) will be set to 1.
    Effect: Dilation tends to increase the size of bright regions, making them more prominent and connected.
    It can be useful for filling small gaps, joining nearby contours, or accentuating features in an image.
     **/
    private boolean useDilation;
    private int dilationRows = 5;
    private int dilationCols = 5;
    private int dilationType = 1;
    private Mat dilation;

    /**
    Create a binary mask (binary) by thresholding the absolute difference image (absDiff). Pixels with a difference
    value greater than or equal to thresh=50 are set to 255 (white), while others are set to 0 (black). This step
    effectively separates regions with significant differences from those with minor differences.
     **/
    private boolean useThreshold;
    private int threshMin = 50;
    private int threshMax = 255;
    private Mat threshold;

    /**
     * The final Mat will be 1D.
     */
    private Mat changeMask;

    public void print(int rows, int cols, int channels) {
        for (int i=0; i<originals.size(); i++) MatOps.printPartOfMat(originals.get(i), rows, cols, channels, "original #"+i);
        for (int i=0; i<grays.size(); i++) MatOps.printPartOfMat(grays.get(i), rows, cols, channels, "gray #"+i);
        for (int i=0; i<gaussians.size(); i++) MatOps.printPartOfMat(gaussians.get(i), rows, cols, channels, "gaussian #"+i);
        MatOps.printPartOfMat(absDiff, rows, cols, channels, "absDiff");
        MatOps.printPartOfMat(dilation, rows, cols, channels, "dilation");
        MatOps.printPartOfMat(threshold, rows, cols, channels, "threshold");
        MatOps.printPartOfMat(changeMask, rows, cols, channels, "finalMat");
    }

    public static class Builder {
        private List<Mat> originals = new ArrayList<>();
        private boolean useGrayscale;
        private List<Mat> grays = new ArrayList<>();
        private boolean useGaussianBlur;
        private int gaussianWidth = 5;
        private int gaussianHeight = 5;
        private int gaussianSigmaX = 0;
        private List<Mat> gaussians = new ArrayList<>();
        private Mat absDiff = new Mat();
        private boolean useDilation;
        private int dilationRows = 5;
        private int dilationCols = 5;
        private int dilationType = 1;
        private Mat dilation = new Mat();
        private boolean useThreshold;
        private int threshMin = 50;
        private int threshMax = 255;
        private Mat threshold = new Mat();
        private Mat finalMat = new Mat();

        public Builder useGrayscale() {
            this.useGrayscale = true;
            return this;
        }

        public Builder useGaussianBlur(int width, int height, int sigmaX) {
            this.useGaussianBlur = true;
            this.gaussianWidth = width;
            this.gaussianHeight = height;
            this.gaussianSigmaX = sigmaX;
            return this;
        }

        public Builder useDilation(int rows, int cols, int type) {
            this.useDilation = true;
            this.dilationRows = rows;
            this.dilationCols = cols;
            this.dilationType = type;
            return this;
        }

        public Builder useThreshold(int min, int max) {
            this.useThreshold = true;
            this.threshMin = min;
            this.threshMax = max;
            return this;
        }

        private void doGrayscale() {
            if (!useGrayscale) return;
            this.originals.forEach(og -> grays.add(MatOps.getGrayscale(og)));
        }

        private void doGaussians(List<Mat> mats) {
            mats.forEach(mat -> {
                Mat gaussian = new Mat();
                GaussianBlur(mat, gaussian, new Size(this.gaussianWidth,this.gaussianHeight),this.gaussianSigmaX);
                gaussians.add(gaussian);
            });
        }

        private void doGaussians() {
            if (!useGaussianBlur) return;
            if (!grays.isEmpty()) doGaussians(grays);
            else doGaussians(originals);
        }

        /**
         * Absolute difference can be calculated in different ways when there are more than two Mat objects.
         * Here, the greatest difference among all Mat objects is used.
         * If there are 3 channels, they are compared individually and the greatest difference from all channels is used.
         */
        private void doAbsoluteDifference(List<Mat> mats) {
            Mat min = MatOps.getNewMatWithPerCellMinsOrMaxes(mats, REDUCE_MIN);
            Mat max = MatOps.getNewMatWithPerCellMinsOrMaxes(mats, REDUCE_MAX);
            absdiff(min, max, absDiff);
            absDiff = MatOps.getMinOrMaxPerCellAcrossChannels(absDiff, REDUCE_MAX);
        }

        private void doAbsoluteDifference() {
            if (!gaussians.isEmpty()) doAbsoluteDifference(gaussians);
            else if (!grays.isEmpty()) doAbsoluteDifference(grays);
            else doAbsoluteDifference(originals);
        }

        private void doDilation() {
            if (useDilation) {
                Mat kernel = new Mat(Mat.ones(dilationRows, dilationCols, dilationType));
                dilate(absDiff, dilation, kernel);
            }
        }

        private void doThreshold() {
            if (useThreshold) {
                if (useDilation) threshold(dilation, threshold, this.threshMin, this.threshMax, THRESH_BINARY);
                else threshold(absDiff, threshold, this.threshMin, this.threshMax, THRESH_BINARY);
            }
        }

        private void doFinal() {
            if (useThreshold) finalMat = threshold.clone();
            else if (useDilation) finalMat = dilation.clone();
            else finalMat = absDiff.clone();
        }

        public Builder addMats(Mat... mats) {
            this.originals.addAll(Arrays.asList(mats));
            return this;
        }

        public Builder addMats(List<Mat> mats) {
            this.originals.addAll(mats);
            return this;
        }

        public Builder setMats(List<Mat> mats) {
            this.originals = mats;
            return this;
        }

        public Builder setMats(MatVector mats) {
            this.originals.addAll(Arrays.asList(mats.get()));
            return this;
        }

        private void calculateAllMats() {
            doGrayscale();
            doGaussians();
            doAbsoluteDifference();
            doDilation();
            doThreshold();
            doFinal();
        }

        public PixelChangeDetector build() {
            PixelChangeDetector pixelChangeDetector = new PixelChangeDetector();
            pixelChangeDetector.originals = this.originals;
            pixelChangeDetector.useGrayscale = this.useGrayscale;
            pixelChangeDetector.useGaussianBlur = this.useGaussianBlur;
            pixelChangeDetector.gaussianWidth = this.gaussianWidth;
            pixelChangeDetector.gaussianHeight = this.gaussianHeight;
            pixelChangeDetector.gaussianSigmaX = this.gaussianSigmaX;
            pixelChangeDetector.useDilation = this.useDilation;
            pixelChangeDetector.dilationRows = this.dilationRows;
            pixelChangeDetector.dilationCols = this.dilationCols;
            pixelChangeDetector.dilationType = this.dilationType;
            pixelChangeDetector.useThreshold = this.useThreshold;
            pixelChangeDetector.threshMin = this.threshMin;
            pixelChangeDetector.threshMax = this.threshMax;
            calculateAllMats();
            pixelChangeDetector.grays = this.grays;
            pixelChangeDetector.gaussians = this.gaussians;
            pixelChangeDetector.absDiff = this.absDiff;
            pixelChangeDetector.dilation = this.dilation;
            pixelChangeDetector.threshold = this.threshold;
            pixelChangeDetector.changeMask = this.finalMat;
            return pixelChangeDetector;
        }

    }



}
