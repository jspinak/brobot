package io.github.jspinak.brobot.app.web.requests;

import lombok.Data;

@Data
public class CaptureScreenshotsRequest {
    private int secondsToCapture;
    private double captureFrequency;
}
