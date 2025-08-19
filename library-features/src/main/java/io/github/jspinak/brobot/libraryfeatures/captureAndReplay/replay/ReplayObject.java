package io.github.jspinak.brobot.libraryfeatures.captureAndReplay.replay;

import lombok.Data;

/**
 * Stub implementation to satisfy compilation requirements.
 * The original class depended on ActionOptions which no longer exists.
 * This provides minimal functionality to allow the module to compile.
 */
@Data
public class ReplayObject {
    private double timelapseFromStartOfRecording;
    private boolean objectReady;
    private boolean pivotPoint;
    
    public double getTimelapseFromStartOfRecording() {
        return timelapseFromStartOfRecording;
    }
    
    public boolean isObjectReady() {
        return objectReady;
    }
    
    public boolean isPivotPoint() {
        return pivotPoint;
    }
}
