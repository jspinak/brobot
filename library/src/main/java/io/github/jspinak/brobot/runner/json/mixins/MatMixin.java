package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * Jackson mixin for OpenCV's Mat class to control JSON serialization.
 * <p>
 * This mixin prevents serialization of Mat's internal implementation
 * details and heavyweight objects that are not suitable for JSON representation.
 * Mat objects contain native memory references and complex internal state
 * that would cause serialization issues and circular references.
 * <p>
 * Properties ignored include:
 * <ul>
 * <li>data - Native memory pointer to image data</li>
 * <li>refcount - Reference counting for memory management</li>
 * <li>allocator - Memory allocation strategy</li>
 * <li>u - Internal UMat reference</li>
 * <li>step - Step array for matrix dimensions</li>
 * <li>dims - Dimension information</li>
 * <li>size - Size information</li>
 * </ul>
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
@JsonIgnoreProperties({"data", "refcount", "allocator", "u", "step", "dims", "size"})
public abstract class MatMixin {
    @JsonIgnore
    abstract public Mat setTo(org.bytedeco.opencv.opencv_core.UMat value);

    @JsonIgnore
    abstract public Mat setTo(org.bytedeco.opencv.opencv_core.Mat value);

    @JsonIgnore
    abstract public Mat setTo(org.bytedeco.opencv.opencv_core.GpuMat value);
}