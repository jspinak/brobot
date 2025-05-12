package io.github.jspinak.brobot.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bytedeco.opencv.opencv_core.Mat;

// Mixin for Mat class
public abstract class MatMixin {
    @JsonIgnore
    abstract public Mat setTo(org.bytedeco.opencv.opencv_core.UMat value);

    @JsonIgnore
    abstract public Mat setTo(org.bytedeco.opencv.opencv_core.Mat value);

    @JsonIgnore
    abstract public Mat setTo(org.bytedeco.opencv.opencv_core.GpuMat value);
}