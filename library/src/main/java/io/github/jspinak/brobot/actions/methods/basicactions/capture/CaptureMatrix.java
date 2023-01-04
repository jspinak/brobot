package io.github.jspinak.brobot.actions.methods.basicactions.capture;

import io.github.jspinak.brobot.imageUtils.MatBuilder;
import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.sikuli.script.Mouse;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

@Getter
public class CaptureMatrix {

    private Mat mat;
    private int insertRowAt = 0;
    private LocalDateTime startTime;
    private int firstWhenInEventString;

    public CaptureMatrix() {
        mat = new Mat(1, 5, 4, new Scalar(0));
        startTime = LocalDateTime.now();
    }

    public void addRow(int action, int key, int millis) {
        Mat newRowMat = new Mat(1, 5, 4, new Scalar(0));
        int millisDiff = millis - firstWhenInEventString;
        if (insertRowAt == 0) {
            mat = new MatBuilder()
                    .newOneChannelRowMat(action, Mouse.at().x, Mouse.at().y, key, 0) //getTimelapse())
                    .build();
            firstWhenInEventString = millis;
        }
        else {
            MatOps.putInt(newRowMat, 0, 0, action, Mouse.at().x, Mouse.at().y, key, millisDiff);// getTimelapse());
            mat = new MatBuilder()
                    .setMat(mat)
                    .addVerticalSubmats(newRowMat)
                    .build();
        }
        insertRowAt++;
    }

    public void writeMatToCapture() {
        MatOps.info(mat, "final mat");
        MatOps.printPartOfMat(mat, 15, 5, 1, "actions");
        imwrite("capture/actions.bmp", mat);
    }

    public int getTimelapse() {
        return (int)Duration.between(startTime, LocalDateTime.now()).toMillis();
    }
}
