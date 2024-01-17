package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.imageUtils.ImageOps;
import io.github.jspinak.brobot.imageUtils.MatOps;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Simplified version of the MatchObject of versions 1.0.6 and earlier.
 * Removed: action duration
 * Simplified: Text is a String and not a Text object.
 * Replaced:
 * - StateImage with Pattern. This is more granular, since a find action searches for Pattern objects.
 * - sceneName with scene.
 *
 *  * Match is used to store information about new Pattern matches. It includes:
 *  *   The SikuliX Match
 *  *   The String found during text detection
 *  *   The StateObject used in the Find operation
 *  *   The time the MatchObject was created
 *  *
 *  *   This object is similar to MatchSnapshot but has the following differences:
 *  *     Match objects always have successful matches, whereas Snapshots can have failed matches.
 *  *     Snapshots are contained within Image objects and StateRegions. Match objects include
 *  *       a StateObject as a variable.
 *  *     Match objects have only one match, as opposed to Snapshots, which can have
 *  *       multiple Match objects.
 */
@Embeddable
@Getter
@Setter
public class Match extends org.sikuli.script.Match {

    private String name = "";
    /*
    Match.Match(SikuliX).Image(SikuliX).BufferedImage contains the pixels from the match region. This can be different from
    the image used to search with (similarity may be low, or the match area might be shifted).
    Also, some operations do not use an image to search, and are interested in the contents of the
    screen.

    searchImage holds the image used to find the match.
     */
    private Image searchImage;
    /*
    The match may have come from a Region, in which case it won't have a Pattern object. The anchors are recorded here,
    regardless of which object created the match.
     */
    @Embedded
    private Anchors anchors;
    @Embedded
    private StateObjectData stateObjectData;
    private Mat histogram;
    @Embedded
    private Scene scene;
    private LocalDateTime timeStamp;
    // the old MatchObject had `private double duration;`
    private int timesActedOn = 0;

    public Match(org.sikuli.script.Match match) {
        super(match);
        this.setName(match.getName());
    }

    public Mat getMat() {
        return ImageOps.getMatBGR(getImage());
    }

    public void setMat(Mat mat) {
        getImage().setBimg(BufferedImageOps.fromMat(mat));
    }

    public void setText(String text) {
        super.setText(text);
    }

    public Match(Region region) {
        super(region.toMatch());
    }

    public Match(int x, int y, int w, int h) {
        super(new org.sikuli.script.Match(new org.sikuli.script.Region(x, y, w, h), .99));
    }

    public Location getLocation() {
        if (getTarget() == null) {
            Position tempPosition = new Position(50,50);
            return new Location(this, tempPosition);
        }
        return new Location(getTarget());
    }

    public int compareByScore(Match m) {
        return (int)(this.getScore() - m.getScore());
    }

    public int size() { // TODO: check. this used to refer to the size of the Pattern
        return w * h;
    }

    public void incrementTimesActedOn() {
        timesActedOn++;
    }

    public void setImageWithScene() {
        if (scene == null) return;
        Optional<Mat> mat = MatOps.applyIfOk(scene.getImage().getMatBGR(), new Region(this).getJavaCVRect());
        mat.ifPresent(m -> setImage(ImageOps.getImage(m)));
    }

    public String getOwnerStateName() {
        return stateObjectData.getOwnerStateName();
    }

    /**
     * If there is a StateObject, we try to recreate it as a StateImage. The StateObject was likely a StateImage itself.
     * If there is no Pattern, there are no SearchRegions. Since the Match region needs to be saved, a new Pattern
     * is created and the Match region is saved to the fixed region of the Pattern's SearchRegions.
     * In an operation like Find.ALL_WORDS, there may not be a StateObject, although Pattern objects should be
     * created.
     * @return the StateImage created from the Match
     */
    public StateImage toStateImage() {
        StateImage stateImage = new StateImage();
        stateImage.setName(name);
        if (stateObjectData != null) {
            stateImage.setOwnerStateName(stateObjectData.getOwnerStateName());
            stateImage.setName(stateObjectData.getName()); // the StateObject name should take priority since it was the original StateImage
        }
        if (searchImage != null) stateImage.addPatterns(new Pattern(searchImage.get()));
        else {
            Pattern newPattern = new Pattern.Builder()
                    .setFixed(true)
                    .setFixedRegion(new Region(this))
                    .setName(name)
                    .build();
            stateImage.addPatterns(newPattern);
        }
        return stateImage;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("M[");
        String nameText = "";
        if (name != null && !name.isEmpty()) {
            nameText = "#" + name + "# ";
        }
        stringBuilder.append(String.format("%sR[%d,%d %dx%d] simScore:%.1f", nameText, this.x, this.y, this.w, this.h, super.getScore()));
        if (getText() != null && !getText().isEmpty()) stringBuilder.append(" text:").append(getText());
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    /*
    This is a workaround. It is generally a bad idea to set variables like this.
    TODO: ask Raimund to code a setter for simScore
     */
    public void setSimScore(double simScore) {
        try {
            // Get the "simScore" field from the superclass (Match)
            Field simScoreField = org.sikuli.script.Match.class.getDeclaredField("simScore");

            // Make the field accessible, even if it is private
            simScoreField.setAccessible(true);

            // Set the value of the field in this instance
            simScoreField.set(this, simScore);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Handle exceptions (e.g., NoSuchFieldException, IllegalAccessException)
            e.printStackTrace();
        }
    }

    public static class Builder {
        private org.sikuli.script.Match match;
        private BufferedImage bufferedImage;
        private Image searchImage;
        private String name;
        private String text;
        private Anchors anchors;
        private StateObjectData stateObjectData;
        private Mat histogram;
        private Scene scene;
        private LocalDateTime timeStamp;
        private double simScore = -1;

        public Builder setMatch(org.sikuli.script.Match match) {
            this.match = match;
            return this;
        }

        public Builder setMatch(Region region) {
            this.match = new org.sikuli.script.Match(region, .99);
            return this;
        }

        public Builder setMatch(Rect rect) {
            this.match = new org.sikuli.script.Match(new Region(rect), .99);
            return this;
        }

        public Builder setMatch(int x, int y, int w, int h) {
            this.match = new org.sikuli.script.Match(new Region(x, y, w, h), .99);
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setBufferedImage(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
            return this;
        }

        public Builder setBufferedImage(Mat mat) {
            this.bufferedImage = BufferedImageOps.fromMat(mat);
            return this;
        }

        public Builder setSearchImage(BufferedImage bufferedImage) {
            this.searchImage = new Image(bufferedImage);
            return this;
        }

        public Builder setSearchImage(Mat mat) {
            this.searchImage = new Image(mat);
            return this;
        }

        public Builder setAnchors(Anchors anchors) {
            this.anchors = anchors;
            return this;
        }

        public Builder setStateObjectData(StateObject stateObject) {
            this.stateObjectData = new StateObjectData(stateObject);
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

        public Builder setSimScore(double simScore) {
            this.simScore = simScore;
            return this;
        }

        public Match build() {
            Match matchObject = new Match(match);
            matchObject.name = name;
            if (text != null) matchObject.setText(text); // otherwise, this erases the SikuliX match text
            matchObject.anchors = anchors;
            matchObject.stateObjectData = stateObjectData;
            matchObject.histogram = histogram;
            matchObject.scene = scene;
            matchObject.timeStamp = LocalDateTime.now();
            matchObject.setImageWithScene();
            if (bufferedImage != null) matchObject.setImage(new org.sikuli.script.Image(bufferedImage));
            if (simScore >= 0) matchObject.setSimScore(simScore); // otherwise, this erases the SikuliX match simScore
            matchObject.searchImage = searchImage;
            return matchObject;
        }

    }

}
