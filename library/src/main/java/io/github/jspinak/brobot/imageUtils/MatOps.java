package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.indexer.IntRawIndexer;
import org.bytedeco.javacpp.indexer.UByteRawIndexer;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_core.*;

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

    public static void putInt(Mat mat, int row, int col, int... values) {
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
        //printPartOfMat(mat, 5, 5, 1, "mat in putInt");
        IntRawIndexer indexer32s = mat.createIndexer();
        for (int i = 0; i < values.length; i++) {
            indexer32s.put(row, col + i, values[i]);
        }
/*
        int zeroRows = mat.rows() - row - 1;
        if (zeroRows > 0) {
            Mat zeroMat = new Mat(zeroRows, mat.cols(), mat.type(), Scalar.ZERO);
            Report.println("Zero rows: " + zeroRows);
            mat.push_back(zeroMat);
        }
        Mat addRow = new Mat(1, mat.cols(), mat.type(), Scalar.ZERO);
        if (mat.rows() <= row) {
            Report.println("MatOps: putInt: mat.rows() <= row: push_back");
            mat.push_back(addRow);
            //vconcat(mat, addRow, mat);
        }
        if (mat.rows() <= row) {
            Report.println("MatOps: putInt: mat.rows() <= row: vconcat");
            //mat.push_back(addRow);
            vconcat(mat, addRow, mat);
        }
        /*
        if (mat.rows() <= row) {
            mat.resize(row + 1);
        }
        IntRawIndexer indexer32s = mat.createIndexer();
        indexer32s.put(row, col, value);

         */

    }

    public static double getDouble(int x, int y, int z, Mat mat) {
        UByteRawIndexer indexer8u; IntRawIndexer indexer32s; FloatIndexer indexer32f;
        if (mat.type() % 8 == 0) { // 8 bits = 1 byte
            indexer8u = mat.createIndexer();
            return indexer8u.get(y, x, z);
        }
        if ((mat.type() - 4) % 8 == 0) { // 32 bit signed int
            indexer32s = mat.createIndexer();
            return indexer32s.get(y, x, z);
        }
        if ((mat.type() - 5) % 8 == 0) { // 32 bit float
            indexer32f = mat.createIndexer();
            return indexer32f.get(y, x, z);
        }
        return 0;
    }

    public static double[] getDoubleRow(int row, Mat mat) {
        double[] rowArray = new double[mat.cols()];
        for (int i = 0; i < mat.cols(); i++) {
            rowArray[i] = getDouble(i, row, 0, mat);
        }
        return rowArray;
    }

    public static double[] getDoubleColumn(int col, Mat mat) {
        double[] colArray = new double[mat.rows()];
        for (int i = 0; i < mat.rows(); i++) {
            colArray[i] = getDouble(col, i, 0, mat);
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
        if (!title.isEmpty()) Report.println(title);
        if (mat == null || mat.isNull() || mat.empty()) {
            Report.println("Mat is empty or null." );
            return;
        }
        Report.formatln("printing rows.cols.channels = %d.%d.%d of %d.%d.%d", rows, cols, channels, mat.rows(), mat.cols(), mat.channels());
        for (int z = 0; z < Math.min(channels, mat.channels()); z++) {
            Report.formatln("channel %d", z);
            for (int y = 0; y < Math.min(rows, mat.rows()); y++) {
                Report.format("[%d] ", y);
                double[] row = getDoubleRow(y, mat);
                getNonConsecutiveZeros(row).forEach((key, value) -> Report.format("%-2.12s ", value));
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
}
