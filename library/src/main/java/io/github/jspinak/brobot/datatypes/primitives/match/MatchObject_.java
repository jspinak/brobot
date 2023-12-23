package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Match;

import java.time.LocalDateTime;

/**
 * Simplified version of the MatchObject of versions 1.0.6 and earlier.
 * Removed: action duration
 * Simplified: Text is a String and not a Text object.
 * Replaced:
 * - StateImage with Pattern. This is more granular, since a find action searches for Pattern objects.
 * - sceneName with scene.
 */
@Getter
public class MatchObject_ {

    private Match match;
    /*
    For simplicity, one String is used to represent text here. While it may be a stochastic variable (text read from
    the screen may differ at different readings), the processing of the text read can be performed before creating
    a MatchObject.
     */
    private String text;
    private Pattern pattern;
    private Mat histogram;
    private Scene scene;
    /*
    The score is used for classification and other actions that rank Match objects.
     */
    private double score;
    private LocalDateTime timeStamp;

    public Location getLocation() {
        return new Location(match, pattern.getPosition());
    }

    public static class Builder {
        private Match match;
        private String text;
        private Pattern pattern;
        private Mat histogram;
        private Scene scene;
        private double score;
        private LocalDateTime timeStamp;

        public Builder setMatch(Match match) {
            this.match = match;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setPattern(Pattern pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder setHistogram(Mat histogram) {
            this.histogram = histogram;
            return this;
        }

        public Builder setScene(Scene scene) {
            this.scene = scene;
            return this;
        }

        public Builder setScore(double score) {
            this.score = score;
            return this;
        }

        public MatchObject_ build() {
            MatchObject_ matchObject = new MatchObject_();
            matchObject.match = match;
            matchObject.text = text;
            matchObject.pattern = pattern;
            matchObject.histogram = histogram;
            matchObject.scene = scene;
            matchObject.score = score;
            matchObject.timeStamp = LocalDateTime.now();
            return matchObject;
        }

    }

}
