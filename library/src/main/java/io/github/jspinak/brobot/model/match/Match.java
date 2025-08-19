package io.github.jspinak.brobot.model.match;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.element.Anchors;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Data;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a successful pattern match found on the screen in the Brobot model-based GUI automation framework.
 * 
 * <p>A Match is created when a Find operation successfully locates a GUI element (image, text, or region) 
 * on the screen. It encapsulates all information about the match including its location, similarity score, 
 * the image content at that location, and metadata about how it was found.</p>
 * 
 * <p>In the model-based approach, matches are fundamental for:
 * <ul>
 *   <li>Providing targets for mouse and keyboard actions (clicks, typing, etc.)</li>
 *   <li>Verifying that GUI elements exist in expected States</li>
 *   <li>Building dynamic relationships between GUI elements</li>
 *   <li>Creating visual feedback through match highlighting</li>
 *   <li>Tracking interaction history with GUI elements</li>
 * </ul>
 * </p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Score</b>: Similarity score (0.0-1.0) indicating match quality</li>
 *   <li><b>Target</b>: Location within the matched region for precise interactions</li>
 *   <li><b>Image content</b>: Actual pixels from the screen at the match location</li>
 *   <li><b>Search image</b>: The pattern that was searched for</li>
 *   <li><b>State context</b>: Information about which State object found this match</li>
 * </ul>
 * </p>
 * 
 * <p>Unlike MatchSnapshot which can represent failed matches, a Match object always 
 * represents a successful find operation. Multiple Match objects are aggregated in 
 * an ActionResult.</p>
 * 
 * @since 1.0
 * @see ActionResult
 * @see Region
 * @see Location
 * @see Pattern
 * @see StateObject
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Match {

    // fields in the SikuliX Match (but Brobot datatypes)
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
    private StateObjectMetadata stateObjectData;
    @JsonIgnore
    private Mat histogram;
    private Scene scene;
    private LocalDateTime timeStamp;
    // the old MatchObject had `private double duration;`
    private int timesActedOn = 0;

    public Match() {} // for mapping

    public Match(Region region) {
        this.target = new Location(region);
    }

    public int x() {
        Region region = getRegion();
        return region != null ? region.x() : 0;
    }

    public int y() {
        Region region = getRegion();
        return region != null ? region.y() : 0;
    }

    public int w() {
        Region region = getRegion();
        return region != null ? region.w() : 0;
    }

    public int h() {
        Region region = getRegion();
        return region != null ? region.h() : 0;
    }

    public Region getRegion() {
        if (target == null) return null;
        return target.getRegion();
    }

    public void setRegion(Region region) {
        if (target == null) {
            target = new Location(region);
        }
        else target.setRegion(region);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public Mat getMat() {
        return image != null ? image.getMatBGR() : null;
    }

    public double compareByScore(Match m) {
        return score - m.getScore();
    }

    public int size() { // TODO: check. this used to refer to the size of the Pattern
        return getRegion().w() * getRegion().h();
    }

    public void incrementTimesActedOn() {
        timesActedOn++;
    }

    public void setImageWithScene() {
        if (scene == null) return;
        BufferedImage bImg = BufferedImageUtilities.getSubImage(scene.getPattern().getBImage(), getRegion());
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
            if (!stateObjectData.getStateObjectName().isEmpty()) stateImage.setName(stateObjectData.getStateObjectName()); // the StateObject name should take priority since it was the original StateImage
        }
        stateImage.addPatterns(new Pattern(this));
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
        Region r = getRegion();
        if (r != null) {
            stringBuilder.append(String.format("%sR[%d,%d %dx%d] simScore:%.1f", nameText,
                    r.x(), r.y(), r.w(), r.h(), score));
        } else {
            stringBuilder.append(String.format("%sR[null] simScore:%.1f", nameText, score));
        }
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
        private StateObjectMetadata stateObjectData = new StateObjectMetadata();
        private Mat histogram;
        private Scene scene;
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
            this.offsetX = offset.getCalculatedX();
            this.offsetY = offset.getCalculatedY();
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
            this.bufferedImage = BufferedImageUtilities.fromMat(mat);
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

        public Builder setSearchImage(Image image) {
            this.searchImage = image;
            return this;
        }

        public Builder setAnchors(Anchors anchors) {
            this.anchors = anchors;
            return this;
        }

        public Builder setStateObjectData(StateObject stateObject) {
            this.stateObjectData = new StateObjectMetadata(stateObject);
            return this;
        }

        public Builder setStateObjectData(StateObjectMetadata stateObjectData) {
            this.stateObjectData = stateObjectData;
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

        private void setMatchImage(Match match) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Match match = (Match) obj;
        
        // Compare essential fields for match equality
        return Double.compare(match.score, score) == 0 &&
                Objects.equals(name, match.name) &&
                Objects.equals(text, match.text) &&
                Objects.equals(getRegion(), match.getRegion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, name, text, getRegion());
    }

}
