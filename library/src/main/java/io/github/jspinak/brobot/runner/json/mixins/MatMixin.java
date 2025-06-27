package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * Jackson mixin for OpenCV's Mat class to control JSON serialization.
 * <p>
 * This mixin prevents serialization of Mat's setTo() method overloads that
 * accept other matrix types (UMat, Mat, GpuMat). These methods are used for
 * copying matrix data between different OpenCV matrix implementations and
 * would cause serialization issues due to their native data structures and
 * potential circular references.
 * <p>
 * Methods ignored:
 * <ul>
 * <li>setTo(UMat) - Sets matrix values from a UMat (Unified Memory)</li>
 * <li>setTo(Mat) - Sets matrix values from another Mat</li>
 * <li>setTo(GpuMat) - Sets matrix values from a GPU matrix</li>
 * </ul>
 *
 * @see org.bytedeco.opencv.opencv_core.Mat
 * @see org.bytedeco.opencv.opencv_core.UMat
 * @see org.bytedeco.opencv.opencv_core.GpuMat
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
public abstract class MatMixin {
    @JsonIgnore
    abstract public Mat setTo(org.bytedeco.opencv.opencv_core.UMat value);

    @JsonIgnore
    abstract public Mat setTo(org.bytedeco.opencv.opencv_core.Mat value);

    @JsonIgnore
    abstract public Mat setTo(org.bytedeco.opencv.opencv_core.GpuMat value);
}