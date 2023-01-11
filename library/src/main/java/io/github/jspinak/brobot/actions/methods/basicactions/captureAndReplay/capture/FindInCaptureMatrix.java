package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.capture;

import io.github.jspinak.brobot.imageUtils.MatOps;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

@Component
public class FindInCaptureMatrix {

    /**
     * The Mat is ordered from the oldest to the newest row.
     * The guess comes from finding the screenshot that is the most similar to the current screen, based on
     * the sequence of actions to be performed. The query is like a look-up table for the sequence.
     * @param guessIndex index of the previous last row
     * @param startTime time of the first row
     * @param endTime time of the last row
     * @return a Mat with the rows between startTime and endTime (not inclusive)
     */
    public Mat getSequence(Mat mat, int guessIndex, int startTime, int endTime) {
        int start = getStartIndex(mat, guessIndex, startTime);
        if (start > mat.rows()) {
            return new Mat();
        }
        int end = getEndIndex(mat, start, endTime);
        if (end == -1) {
            return new Mat();
        }
        return mat.rowRange(start, end);
    }

    private int getStartIndex(Mat mat, int guessIndex, int startTime) {
        guessIndex = Math.max(0, guessIndex);
        guessIndex = Math.min(mat.rows() - 1, guessIndex);
        int firstIndexAfterStartTime = guessIndex;
        double time = MatOps.getDouble(4, guessIndex, 0, mat);
        if (time < startTime) {
            while (time < startTime) {
                firstIndexAfterStartTime++;
                if (firstIndexAfterStartTime > mat.rows()) { // we are at the end of the matrix
                    return firstIndexAfterStartTime;
                }
                time = MatOps.getDouble(4, firstIndexAfterStartTime, 0, mat);
            }
        } else {
            while (time > startTime) {
                if (firstIndexAfterStartTime == 0) { // we are at the beginning of the matrix
                    break;
                }
                double previousTime = MatOps.getDouble(4, firstIndexAfterStartTime - 1, 0, mat);
                time = MatOps.getDouble(4, firstIndexAfterStartTime, 0, mat);
                if (previousTime < startTime && time > startTime) {
                    break;
                }
                firstIndexAfterStartTime--;
            }
        }
        return firstIndexAfterStartTime;
    }

    private int getEndIndex(Mat mat, int guessIndex, int endTime) {
        guessIndex = Math.max(0, guessIndex);
        guessIndex = Math.min(mat.rows() - 1, guessIndex);
        int lastIndexBeforeEndTime = guessIndex;
        double time = MatOps.getDouble(4, guessIndex, 0, mat);
        if (time < endTime) {
            while (time < endTime) {
                if (lastIndexBeforeEndTime + 1 == mat.rows()) { // we are at the end of the matrix
                    return lastIndexBeforeEndTime;
                }
                double nextTime = MatOps.getDouble(4, lastIndexBeforeEndTime + 1, 0, mat);
                if (time < endTime && nextTime > endTime) {
                    return lastIndexBeforeEndTime;
                }
                lastIndexBeforeEndTime++;
                time = MatOps.getDouble(4, lastIndexBeforeEndTime, 0, mat);
            }
        } else {
            while (time > endTime) {
                if (lastIndexBeforeEndTime == 0) { // we are at the beginning of the matrix
                    return -1; // no rows before endTime
                }
                lastIndexBeforeEndTime--;
                time = MatOps.getDouble(4, lastIndexBeforeEndTime, 0, mat);
            }
        }
        return lastIndexBeforeEndTime;
    }

}
