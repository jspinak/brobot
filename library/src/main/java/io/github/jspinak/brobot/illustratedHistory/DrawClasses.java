package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.KmeansCluster;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.classification.SparseMatrix;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.nd4j.linalg.indexing.BooleanIndexing.replaceWhere;
import static org.opencv.core.Core.add;

@Component
public class DrawSparseMatrix {

    private IllustrationFilename illustrationFilename;
    private ClassificationLegend classificationLegend;

    public DrawSparseMatrix(IllustrationFilename illustrationFilename, ClassificationLegend classificationLegend) {
        this.illustrationFilename = illustrationFilename;
        this.classificationLegend = classificationLegend;
    }

    public void paintClasses(SparseMatrix sparseMatrix) {
        ActionOptions actionOptions = new ActionOptions.Builder().setAction(ActionOptions.Action.CLASSIFY).build();
        String outputPath = illustrationFilename.getFilename(actionOptions);
        //Mat bgr = convertIndicesToColors(sparseMatrix);
        writeImage(sparseMatrix.getResultsAsColorsBGR(), outputPath, sparseMatrix);
    }

    private Mat convertIndicesToColors(SparseMatrix sparseMatrix) {
        INDArray indices = sparseMatrix.getResults();
        Mat indMat = new NativeImageLoader().asMat(indices);
        /*
        for (StateImageObject img : sparseMatrix.getStateImageObjects()) {
            System.out.println(img.getName()+" index is "+img.getIndex());
            replaceIndexWithColorValue(hue, sat, val, img.getKmeans().get(0), img.getIndex());
        }
         */
        Mat hFinal = new Mat();
        Mat sFinal = new Mat();
        Mat vFinal = new Mat();
        for (StateImageObject img : sparseMatrix.getStateImageObjects()) {
            replaceAllIndexMatsWithColor(hFinal, sFinal, vFinal, indMat, img.getKmeans().get(0), img.getIndex());
        }
        MatVector hsv = new MatVector(hFinal, sFinal, vFinal);
        Mat hsvMat = new Mat();
        merge(hsv, hsvMat);
        Mat bgr = new Mat();
        opencv_imgproc.cvtColor(hsvMat, bgr, Imgproc.COLOR_HSV2BGR);
        return bgr;
    }

    private void replaceAllIndexMatsWithColor(Mat hDst, Mat sDst, Mat vDst, Mat indices,
                                              KmeansCluster replaceWith, int index) {
        replaceIndexWithColor(hDst, indices, replaceWith.getHue(), index);
        replaceIndexWithColor(sDst, indices, replaceWith.getSaturation(), index);
        replaceIndexWithColor(vDst, indices, replaceWith.getValue(), index);
    }

    private void replaceIndexWithColor(Mat dst, Mat src, double replaceWith, int index) {
        Mat mask = new Mat();
        System.out.println("index = "+index);
        inRange(src, new Mat(new Scalar(index)), new Mat(new Scalar(index)), mask);
        bitwise_and(src, new Mat(new Scalar(replaceWith)), dst, mask);
    }

    private void writeImage(Mat screen, String file, SparseMatrix sparseMatrix) {
        Mat legend = classificationLegend.draw(screen, sparseMatrix);
        //System.out.println("mat: "+screen.rows()+"."+screen.cols()+"."+screen.channels()+" array: "+ "."+array.shapeInfo());
        Mat fused = fuseScreenAndLegend(screen, legend);
        imwrite(file, fused);
    }

    private Mat fuseScreenAndLegend(Mat screen, Mat legend) {
        MatVector concatMats = new MatVector(screen, legend);
        Mat result = new Mat();
        hconcat(concatMats, result);
        return result;
    }

}
