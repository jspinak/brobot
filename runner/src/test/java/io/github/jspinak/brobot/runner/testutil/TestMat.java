package io.github.jspinak.brobot.runner.testutil;

import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;

// Custom Mat implementation for testing
@Getter
public class TestMat extends Mat {

    private boolean released = false;

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public void release() {
        released = true;
    }
}
