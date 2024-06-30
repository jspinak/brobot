package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.indexer.*;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Arrays;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class MatOps {

    public static void printPartOfMat(Mat mat, int rows, int cols, String title) {
        printPartOfMat(mat, rows, cols, mat.channels(), title);
    }

    public static void printPartOfMat(Mat mat, int rows, int cols) {
        printPartOfMat(mat, rows, cols, mat.channels());
    }

    public static void printPartOfMat(Mat mat, int rows, int cols, int channels) {
        printPartOfMat(mat, rows, cols, channels, "");
    }

    public static void putInt(Mat mat, int row, int col, short... values) {
        if (mat.channels() != 1) {
            Report.println("MatOps.putInt: Mat has more than 1 channel");
            return;
        }
        if (mat.rows() <= row || mat.cols() <= col) {
            Report.println("MatOps.putInt: Mat is too small");
            return;
        }
        if (values.length > mat.cols() - col) {
            Report.println("MatOps.putInt: Mat is too small");
            return;
        }
        UByteRawIndexer indexer = mat.createIndexer(); //IntRawIndexer indexer32s = mat.createIndexer();
        for (int i = 0; i < values.length; i++) {
            indexer.put(row, col + i, values[i]);
        }
    }

    /**
     * In a multi-dimensional array, such as a 3D matrix or tensor, the indices i,j,k typically represent the indices
     * along the first, second, and third dimensions, respectively. The variable i is likely used to refer to the row index.
     * @param row i in JavaCV
     * @param col j in JavaCV
     * @param channel k in JavaCV
     * @param mat the mat to analyze
     * @return the value at row, col, channel
     */
    public static double getDouble(int row, int col, int channel, Mat mat) {
        UByteRawIndexer indexer8u; IntRawIndexer indexer32s; FloatIndexer indexer32f;
        if (mat.type() % 8 == 0) { // 8 bits = 1 byte
            indexer8u = mat.createIndexer();
            return indexer8u.get(row, col, channel);
        }
        if ((mat.type() - 4) % 8 == 0) { // 32 bit signed int
            indexer32s = mat.createIndexer();
            return indexer32s.get(row, col, channel);
        }
        if ((mat.type() - 5) % 8 == 0) { // 32 bit float
            indexer32f = mat.createIndexer();
            return indexer32f.get(row, col, channel);
        }
        return 0;
    }

    public static double[] getDoubleRow(int row, Mat mat) {
        double[] rowArray = new double[mat.cols()];
        for (int i = 0; i < mat.cols(); i++) {
            rowArray[i] = getDouble(row, i, 0, mat);
        }
        return rowArray;
    }

    public static double[] getDoubleColumn(int col, Mat mat) {
        double[] colArray = new double[mat.rows()];
        for (int i = 0; i < mat.rows(); i++) {
            colArray[i] = getDouble(i, col, 0, mat);
        }
        return colArray;
    }

    private static Map<Integer, String> getNonConsecutiveZeros(double[] array) {
        Map<Integer, String> noConsecutiveZeros = new HashMap<>();
        for (int i = 0; i < array.length; i++) {
            if (array[i] != 0 || i == array.length - 1) {
                noConsecutiveZeros.put(i, String.valueOf(array[i]));
            } else {
                if (array[i + 1] != 0) {
                    noConsecutiveZeros.put(i, " 0 "); // a single 0
                } else {
                    noConsecutiveZeros.put(i, "..."); // multiple 0s
                    while (array[i] == 0 && i < array.length - 1) {
                        i++;
                    }
                }
            }
        }
        return noConsecutiveZeros;
    }

    /**
     * Prints part of the matrix, starting from the top left corner.
     * Consecutive 0 values will be printed as ...
     * The first channel is printed first and its 2d form is maintained.
     * @param mat matrix to print
     * @param rows number of rows to print
     * @param cols number of columns to print
     * @param channels number of channels to print
     * @param title title of the printed matrix
     */
    public static void printPartOfMat(Mat mat, int rows, int cols, int channels, String title) {
        String boldTextStart = "\u001B[1m"; // ANSI escape code for bold text
        String resetFormatting = "\u001B[0m"; // ANSI escape code to reset text formatting
        Report.print(boldTextStart + title + resetFormatting);
        if (mat == null || mat.isNull() || mat.empty()) {
            Report.println("Mat is empty or null." );
            return;
        }
        Report.formatln(" rows.cols.channels = %d.%d.%d of %d.%d.%d", rows, cols, channels, mat.rows(), mat.cols(), mat.channels());
        int ch = Math.min(channels, mat.channels());
        for (int z = 0; z < ch; z++) {
            if (ch > 1) Report.formatln("channel %d", z);
            for (int y = 0; y < Math.min(rows, mat.rows()); y++) {
                Report.format("[%d] ", y);
                double[] row = getDoubleRow(y, mat);
                for (double d : row) System.out.printf("%-8.1f", d);
                //getNonConsecutiveZeros(row).forEach((key, value) -> Report.format("%-2.12s ", value));
                Report.println();
            }
        }
    }

    public static org.opencv.core.Mat convertToOpenCVmat(Mat mat) {
        OpenCVFrameConverter.ToMat converter1 = new OpenCVFrameConverter.ToMat();
        OpenCVFrameConverter.ToOrgOpenCvCoreMat converter2 = new OpenCVFrameConverter.ToOrgOpenCvCoreMat();
        return converter2.convert(converter1.convert(mat));
    }

    public static Mat convertToJavaCVmat(org.opencv.core.Mat mat) {
        OpenCVFrameConverter.ToMat converter1 = new OpenCVFrameConverter.ToMat();
        OpenCVFrameConverter.ToOrgOpenCvCoreMat converter2 = new OpenCVFrameConverter.ToOrgOpenCvCoreMat();
        return converter1.convert(converter2.convert(mat));
    }

    public static void printDimensions(org.opencv.core.Mat mat) {
        Report.format("rows.cols.channels = %d.%d.%d\n", mat.rows(), mat.cols(), mat.channels());
    }

    public static void printDimensions(Mat mat) {
        Report.format("rows.cols.channels = %d.%d.%d\n", mat.rows(), mat.cols(), mat.channels());
    }

    public static void printDimensions(Mat mat, String title) {
        if (mat == null) Report.println(title+"Mat is not defined.");
        else Report.format("%s rows.cols.channels = %d.%d.%d\n", title, mat.rows(), mat.cols(), mat.channels());
    }

    public static void info(Mat mat, String... strings) {
        double[] minmax = getMinMaxOfFirstChannel(mat);
        Arrays.stream(strings).forEach(str -> Report.print(str+" "));
        Report.format("rows.cols.channels type = %d.%d.%d %d ", mat.rows(), mat.cols(), mat.channels(), mat.type());
        if (minmax.length > 1) Report.format("min = %,.1f max = %,.1f\n", minmax[0], minmax[1]);
        else if (mat.total() == 0) Report.formatln(" Mat is empty." );
        else Report.println();
    }

    public static void info(MatVector matVector, String... strings) {
        Arrays.stream(strings).forEach(str -> Report.print(str+" "));
        Report.println();
        for (int i = 0; i < matVector.size(); i++) {
            info(matVector.get(i), "mat"+i);
        }
    }

    /**
     * minMaxLoc requires a single channel array. This method uses the first channel if the Mat is a multi-channel array.
     * miniMaxLoc does not consider 0 as a minimum but this function does.
     */
    public static double[] getMinMaxOfFirstChannel(Mat mat) {
        Mat singleChannelMat = getFirstChannel(mat);
        DoublePointer min = new DoublePointer(1);
        DoublePointer max = new DoublePointer(1);
        Point minLoc = new Point(1);
        Point maxLoc = new Point(1);
        Mat mask = new Mat();
        minMaxLoc(singleChannelMat, min, max, minLoc, maxLoc, mask);
        if (min.isNull() || max.isNull()) {
            Report.println("no min max");
            return new double[] {};
        }
        double[] minmax = new double[] {min.get(), max.get()};
        if (firstChannelContains(mat, 0)) {
            if (min.get() > 0) minmax[0] = 0;
            if (max.get() < 0) minmax[1] = 0;
        }
        return minmax;
    }

    public static Mat getFirstChannel(Mat mat) {
        if (mat.channels() == 1) return mat;
        MatVector matVector = new MatVector(mat.channels());
        split(mat, matVector);
        return matVector.get(0);
    }

    public static boolean firstChannelContains(Mat mat, int x) {
        if (mat == null || mat.isNull() || mat.empty()) return false;
        Mat singleChannelMat = mat;
        if (mat.channels() > 1) singleChannelMat = getFirstChannel(mat);
        Mat booleanMat = new Mat();
        inRange(singleChannelMat, new Mat(new Scalar(x)), new Mat(new Scalar(x)), booleanMat); // 255 where inRange, 0 where not
        return countNonZero(booleanMat) > 0;
    }

    public static Mat makeMat(Size size, int type, double value) {
        Mat mat = new Mat(size, type);
        mat.setTo(new Mat(new Scalar(value)));
        return mat;
    }

    public static Mat makeMat(int rows, int cols, int type, double value) {
        return makeMat(new Size(cols, rows), type, value);
    }

    public static Mat makeMat(Size size, int type, double channel1, double channel2, double channel3) {
        return makeMat(size.height(), size.width(), type, channel1, channel2, channel3);
    }

    /**
     * Makes either a one channel or 3 channel Mat.
     * If another number of channels is provided, an empty Mat is returned.
     *
     * @param size the size of the Mat
     * @param type the type of the Mat
     * @param channels the values of the channels
     * @return the Mat
     */
    public static Mat makeMat(Size size, int type, double[] channels) {
        if (channels.length == 1) return makeMat(size, type, channels[0]);
        if (channels.length == 3) return makeMat(size, type, channels[0], channels[1], channels[2]);
        return new Mat();
    }

    public static Mat makeMat(int rows, int cols, int type, double channel1, double channel2, double channel3) {
        MatVector matVector = new MatVector(3);
        int oneChannelType = type - 16; // CV_8UC3 = 16, CV_8UC1 = 0
        matVector.put(0, makeMat(rows, cols, oneChannelType, channel1));
        matVector.put(1, makeMat(rows, cols, oneChannelType, channel2));
        matVector.put(2, makeMat(rows, cols, oneChannelType, channel3));
        Mat mat = new Mat();
        merge(matVector, mat);
        return mat;
    }

    /**
     * Makes a 3x3, 1-channel Mat with random values (0-255).
     * @return a 3x3 Mat with random values.
     */
    public static Mat make3x3Mat() {
        Mat mat = makeMat(new Size(3,3), CV_8U, 0);
        Random rand = new Random();
        for (short row=0; row<3; row++) {
            for (short col=0; col<3; col++) {
                putInt(mat, row, col, (short)rand.nextInt(256));
            }
        }
        return mat;
    }

    /**
     * Cell values are inserted by row from left to right (the 4th value goes in row 1, column 0).
     * If the array of values is less than 9, the value 0 is placed in the remaining cells.
     * @param values the cell values for a 3x3 Mat
     * @return the new Mat
     */
    public static Mat make3x3Mat(short[] values) {
        Mat mat = makeMat(new Size(3,3), CV_8U, 0);
        for (short row=0; row<3; row++) {
            for (short col=0; col<3; col++) {
                short index = (short)(row*3 + col);
                short value = values.length > index ? values[index] : 0;
                putInt(mat, row, col, value);
            }
        }
        return mat;
    }

    public static Mat makeMat(short... values) {
        short length = 1;
        for (short i = 1; values.length > Math.pow(i, 2); i++) {
            length = (short)(i+1);
        }
        Mat mat = makeMat(new Size(length,length), CV_8U, 0);
        for (short row=0; row<length; row++) {
            for (short col=0; col<length; col++) {
                short index = (short)(row*length + col);
                short value = values.length > index ? values[index] : 0;
                putInt(mat, row, col, value);
            }
        }
        return mat;
    }

    public static Mat getGrayscale(Mat mat) {
        Mat gray = new Mat();
        if (mat.channels() > 1) {
            if (mat.depth() == opencv_core.CV_16S) {
                mat.convertTo(mat, opencv_core.CV_8U); // Convert to 8-bit for grayscale conversion
            }
            cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);
        } else {
            gray = mat.clone(); // No need to convert, already grayscale
        }
        return gray;
    }

    public static boolean matsDontMatch(int minMats, List<Mat> mats) {
        if (mats == null || mats.isEmpty()) {
            System.out.println("List of Mats is null or empty");
            return true;
        }
        if (mats.size() < minMats) {
            System.out.println("List has less than " + minMats + " Mat objects.");
            return true;
        }
        // Ensure all Mats have the same number of rows and columns
        int rows = mats.get(0).rows();
        int cols = mats.get(0).cols();
        int channels = mats.get(0).channels();
        for (Mat mat : mats) {
            if (mat.rows() != rows || mat.cols() != cols || mat.channels() != channels) {
                System.out.println("All Mats must have the same number of rows, columns, and channels");
                return true;
            }
        }
        return false;
    }

    public static Mat getNewMatWithPerCellMinsOrMaxes(List<Mat> mats, int operation) {
        if (mats.size() == 1) return mats.get(0);
        if (matsDontMatch(2, mats)) return new Mat();
        Mat results = mats.get(0).clone();
        for (int i=1; i<mats.size(); i++) {
            if (operation == REDUCE_MIN) min(results, mats.get(i), results);
            if (operation == REDUCE_MAX) max(results, mats.get(i), results);
        }
        return results;
    }

    /**
     * Gets the minimum or maximum value in a cell position across channels.
     * @param mat multi-channel Mat
     * @param operation minimum or maximum
     * @return a one-channel Mat
     */
    public static Mat getMinOrMaxPerCellAcrossChannels(Mat mat, int operation) {
        if (mat.empty() || mat.channels() < 2) return mat;
        MatVector matVector = new MatVector(mat.channels());
        split(mat, matVector);
        Mat results = matVector.get(0).clone();
        for (int i=1; i<matVector.size(); i++) {
            if (operation == REDUCE_MIN) min(results, matVector.get(i), results);
            if (operation == REDUCE_MAX) max(results, matVector.get(i), results);
        }
        return results;
    }

    public static Optional<Mat> applyIfOk(Mat mat, Rect roi) {
        boolean matIsNull = mat == null;
        boolean smallX = roi.x() < 0;
        boolean smallY = roi.y() < 0;
        boolean smallW = roi.width() < 0;
        boolean smallH = roi.height() < 0;
        boolean outsideMatX = false;
        boolean outsideMatY = false;
        if (!matIsNull) {
            outsideMatX = roi.x() + roi.width() > mat.cols();
            outsideMatY = roi.y() + roi.height() > mat.rows();
        }
        if (matIsNull || smallX || smallY || smallW || smallH || outsideMatX || outsideMatY) return Optional.empty();
        return Optional.of(mat.apply(roi));
    }

    public static Optional<Mat> bufferedImageToMat(BufferedImage bufferedImage) {
        if (bufferedImage == null) return Optional.empty();
        return Optional.of(Java2DFrameUtils.toMat(bufferedImage));
    }

    /**
     * Returns a new grayscale Mat. Doesn't change the original BGR Mat.
     * @param bgr the BGR Mat to convert.
     * @return a new grayscale Mat.
     */
    public static Mat toGrayscale(Mat bgr) {
        Mat grayImage = new Mat();
        cvtColor(bgr, grayImage, COLOR_BGR2GRAY);
        return grayImage;
    }

    /**
     * Returns a new HSV Mat. Doesn't change the original BGR Mat.
     * @param bgr the BGR Mat to convert.
     * @return a new HSV Mat.
     */
    public static Mat BGRtoHSV(Mat bgr) {
        Mat hsv = new Mat();
        // Check the number of channels in the input image
        if (bgr.channels() == 1) {
            // Convert grayscale to BGR
            Mat bgrImage = new Mat();
            cvtColor(bgr, bgrImage, COLOR_GRAY2BGR);
            bgr = bgrImage;
        }
        cvtColor(bgr, hsv, COLOR_BGR2HSV);
        return hsv;
    }

}
