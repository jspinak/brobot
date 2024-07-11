package io.github.jspinak.brobot.datatypes.primitives.match;

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

import lombok.Data;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

/**
 * The SikuliX Match object has many protected fields, making it difficult to work with as a field. Instead,
 * the important SikuliX Match fields are included here and a constructor with the SikuliX Match is available.
 *
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
@Data
public class Match {

    // fields in the SikuliX Match
    private double score = 0.0;
    //private Location target; //
    private Image image;
    private String text = "";
    private String name = ""; // in SikuliX Element, extended by many SikuliX object classes, including Match.
    private Location target; // Location has a Brobot region (SikuliX Match has a SikuliX Region) and a Position and offsets.
    // ---------------------------

    /*
    The BufferedImage in `image` contains the pixels from the match region. This can be different from
    the image used to search with (similarity may be low, or the match area might be shifted).
    Also, some operations do not use an image to search, and are interested in the contents of the
    screen.

    `searchImage` holds the image used to find the match.
     */
    private Image searchImage;
    /*
    The match may have come from a Region, in which case it won't have a Pattern object. The anchors are recorded here,
    regardless of which object created the match.
     */
    private Anchors anchors;
    private StateObjectData stateObjectData;
    private Mat histogram;
    private Image scene;
    private LocalDateTime timeStamp;
    // the old MatchObject had `private double duration;`
    private int timesActedOn = 0;

    public Match() {} // for mapping

    public Match(Region region) {
        this.target = new Location(region);
    }

    public int x() {
        return getRegion().x();
    }

    public int y() {
        return getRegion().y();
    }

    public int w() {
        return getRegion().w();
    }

    public int h() {
        return getRegion().h();
    }

    public Region getRegion() {
        return target.getRegion();
    }

    public void setRegion(Region region) {
        target.setRegion(region);
    }

    public Mat getMat() {
        return image.getMatBGR();
    }

    public int compareByScore(Match m) {
        return (int)(score - m.getScore());
    }

    public int size() { // TODO: check. this used to refer to the size of the Pattern
        return getRegion().w() * getRegion().h();
    }

    public void incrementTimesActedOn() {
        timesActedOn++;
    }

    public void setImageWithScene() {
        if (scene == null) return;
        BufferedImage bImg = BufferedImageOps.getSubImage(scene.getBufferedImage(), getRegion());
        if (image == null) image = new Image(bImg);
        else image.setBufferedImage(bImg);
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
            stateImage.setName(stateObjectData.getStateObjectName()); // the StateObject name should take priority since it was the original StateImage
        }
        Image imageToUse = null;
        if (image != null) imageToUse = image;
        else if (searchImage != null) imageToUse = searchImage;
        Pattern newPattern = new Pattern.Builder()
            .setFixed(true)
            .setFixedRegion(target.getRegion())
            .setName(name)
            .setImage(imageToUse)
            .build();
        stateImage.addPatterns(newPattern);
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
        stringBuilder.append(String.format("%sR[%d,%d %dx%d] simScore:%.1f", nameText,
                getRegion().x(), getRegion().y(), getRegion().w(), getRegion().h(), score));
        if (getText() != null && !getText().isEmpty()) stringBuilder.append(" text:").append(getText());
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public org.sikuli.script.Match sikuli() {
        return new org.sikuli.script.Match(getRegion().sikuli(), score);
    }

    public static class Builder {
        private org.sikuli.script.Match sikuliMatch;
        private Location target = new Location();
        private Position position = new Position();
        private int offsetX = 0;
        private int offsetY = 0;
        private Image image;
        private BufferedImage bufferedImage;
        private Image searchImage;
        private Region region;
        private String name;
        private String text;
        private Anchors anchors;
        private StateObjectData stateObjectData;
        private Mat histogram;
        private Image scene;
        private double simScore = -1;

        public Builder setSikuliMatch(org.sikuli.script.Match sikuliMatch) {
            this.sikuliMatch = sikuliMatch;
            return this;
        }

        public Builder setMatch(Match match) {
            if (match.image != null) image = match.image;
            if (match.searchImage != null) this.searchImage = match.getSearchImage();
            if (match.getRegion() != null) this.setRegion(match.getRegion());
            if (match.name != null) this.name = match.getName();
            if (match.text != null) this.text = match.getText();
            if (match.anchors != null) this.anchors = match.getAnchors();
            if (match.stateObjectData != null) this.stateObjectData = match.getStateObjectData();
            if (match.histogram != null) this.histogram = match.histogram;
            if (match.scene != null) this.scene = match.scene;
            this.simScore = match.score;
            return this;
        }

        public Builder setRegion(Region region) {
            this.region = region;
            return this;
        }

        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        public Builder setOffsetX(int offsetX) {
            this.offsetX = offsetX;
            return this;
        }

        public Builder setOffsetY(int offsetY) {
            this.offsetY = offsetY;
            return this;
        }

        public Builder setOffset(Location offset) {
            this.offsetX = offset.getX();
            this.offsetY = offset.getY();
            return this;
        }

        public Builder setImage(Image image) {
            this.image = image;
            return this;
        }

        public Builder setRegion(Rect rect) {
            this.region = new Region(rect);
            return this;
        }

        public Builder setRegion(int x, int y, int w, int h) {
            this.region = new Region(x, y, w, h);
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

        public Builder setStateObjectData(StateObjectData stateObjectData) {
            this.stateObjectData = stateObjectData;
            return this;
        }

        public Builder setHistogram(Mat histogram) {
            this.histogram = histogram;
            return this;
        }

        public Builder setScene(Image scene) {
            this.scene = scene;
            return this;
        }

        public Builder setSimScore(double simScore) {
            this.simScore = simScore;
            return this;
        }

        public void setMatchImage(Match match) {
            if (image != null) {
                match.setImage(image);
                return;
            }
            if (bufferedImage != null) {
                if (match.image != null) match.getImage().setBufferedImage(bufferedImage);
                else match.setImage(new Image(bufferedImage));
                return;
            }
            if (match.scene != null) match.setImageWithScene();
        }

        public Match build() {
            Match match = new Match(new Region());
            if (sikuliMatch != null) {
                match.setScore(sikuliMatch.getScore());
                if (sikuliMatch.getImage() != null) match.image = new Image(sikuliMatch.getImage());
                if (sikuliMatch.getText() != null) match.text = sikuliMatch.getText();
                if (sikuliMatch.getName() != null) match.name = sikuliMatch.getName();
                region = new Region(sikuliMatch);
            }

            // set target location
            if (region != null) target.setRegion(region);
            target.setPosition(position);
            target.setOffsetX(offsetX);
            target.setOffsetY(offsetY);
            match.target = target;

            match.scene = scene;
            setMatchImage(match);
            if (name != null) match.name = name;
            if (text != null && !text.isEmpty()) match.setText(text); // otherwise, this erases the SikuliX match text
            if (simScore >= 0)
                match.setScore(simScore); // otherwise, this erases the SikuliX match simScore
            match.anchors = anchors;
            match.stateObjectData = stateObjectData;
            match.histogram = histogram;
            match.timeStamp = LocalDateTime.now();
            match.searchImage = searchImage;
            return match;
        }

    }

}
