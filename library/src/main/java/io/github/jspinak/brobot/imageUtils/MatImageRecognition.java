package io.github.jspinak.brobot.imageUtils;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.sikuli.script.Match;
import org.sikuli.script.Region;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.bytedeco.opencv.global.opencv_core.minMaxLoc;
import static org.bytedeco.opencv.global.opencv_imgproc.TM_CCOEFF_NORMED;
import static org.bytedeco.opencv.global.opencv_imgproc.matchTemplate;

@Component
public class MatImageRecognition {

    public Optional<Match> findTemplateMatch(Mat template, Mat searchImage, double threshold) {
        // Ensure the loaded images are valid
        if (template.empty() || searchImage.empty()) {
            throw new RuntimeException("Error loading images.");
        }
        if (template.rows() > searchImage.rows() || template.cols() > searchImage.cols())
            return Optional.empty();

        // Create a result Mat to store the match result
        Mat result = new Mat();

        // Perform template matching
        matchTemplate(searchImage, template, result, TM_CCOEFF_NORMED);

        // Find the location of the best match
        double[] minVal = new double[1];
        double[] maxVal = new double[1];
        Point minLoc = new Point();
        Point maxLoc = new Point();
        minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, null);

        // Check if the match meets the threshold criteria
        if (maxVal[0] >= threshold) {
            // Create a Match object using SikuliX
            Match match = new Match(new Region((int) maxLoc.x(), (int) maxLoc.y(), template.cols(), template.rows()), maxVal[0]);

            // Return the Match object wrapped in an Optional
            return Optional.of(match);
        } else {
            // No valid match found, return an empty Optional
            return Optional.empty();
        }
    }

}
