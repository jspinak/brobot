package io.github.jspinak.brobot.runner.json.mixins;

import org.bytedeco.opencv.opencv_core.Mat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin for OpenCV's Mat class to control JSON serialization.
 *
 * <p>This mixin prevents serialization of Mat's internal implementation details and heavyweight
 * objects that are not suitable for JSON representation. Mat objects contain native memory
 * references and complex internal state that would cause serialization issues and circular
 * references.
 *
 * <p>Properties ignored include:
 *
 * <ul>
 *   <li>data - Native memory pointer to image data
 *   <li>refcount - Reference counting for memory management
 *   <li>allocator - Memory allocation strategy
 *   <li>u - Internal UMat reference
 *   <li>step - Step array for matrix dimensions
 *   <li>dims - Dimension information
 *   <li>size - Size information
 * </ul>
 *
 * <p>Methods ignored:
 *
 * <ul>
 *   <li>setTo(UMat) - Sets matrix values from a UMat (Unified Memory)
 *   <li>setTo(Mat) - Sets matrix values from another Mat
 *   <li>setTo(GpuMat) - Sets matrix values from a GPU matrix
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
    public abstract Mat setTo(org.bytedeco.opencv.opencv_core.UMat value);

    @JsonIgnore
    public abstract Mat setTo(org.bytedeco.opencv.opencv_core.Mat value);

    @JsonIgnore
    public abstract Mat setTo(org.bytedeco.opencv.opencv_core.GpuMat value);
}
