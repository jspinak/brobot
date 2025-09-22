package io.github.jspinak.brobot.model.match;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Objects;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Anchors;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;

import lombok.Data;

/**
 * Represents a successful pattern match found on the screen in the Brobot model-based GUI
 * automation framework.
 *
 * <p>A Match is created when a Find operation successfully locates a GUI element (image, text, or
 * region) on the screen. It encapsulates all information about the match including its location,
 * similarity score, the image content at that location, and metadata about how it was found.
 *
 * <p>In the model-based approach, matches are fundamental for:
 *
 * <ul>
 *   <li>Providing targets for mouse and keyboard actions (clicks, typing, etc.)
 *   <li>Verifying that GUI elements exist in expected States
 *   <li>Building dynamic relationships between GUI elements
 *   <li>Creating visual feedback through match highlighting
 *   <li>Tracking interaction history with GUI elements
 * </ul>
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Score</b>: Similarity score (0.0-1.0) indicating match quality
 *   <li><b>Target</b>: Location within the matched region for precise interactions
 *   <li><b>Image content</b>: Actual pixels from the screen at the match location
 *   <li><b>Search image</b>: The pattern that was searched for
 *   <li><b>State context</b>: Information about which State object found this match
 * </ul>
 *
 * <p>Unlike MatchSnapshot which can represent failed matches, a Match object always represents a
 * successful find operation. Multiple Match objects are aggregated in an ActionResult.
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * // Find and process matches
 * StateImage buttonImage = new StateImage.Builder()
 *     .addPatterns("button.png")
 *     .build();
 * ActionResult result = action.find(buttonImage);
 *
 * // Process each match
 * for (Match match : result.getMatchList()) {
 *     // Get match properties
 *     double score = match.getScore();  // Similarity score (0.0-1.0)
 *     Region region = match.getRegion();  // Bounding box
 *     Location center = match.getLocation();  // Center point
 *
 *     // Click at different positions
 *     action.click(match);  // Click at match center
 *     action.click(match.getTopLeft());  // Click top-left corner
 *     action.click(match.getBottomRight());  // Click bottom-right
 *
 *     // Highlight for debugging
 *     action.highlight(match);
 *
 *     // Check match quality
 *     if (match.getScore() > 0.95) {
 *         System.out.println("High confidence match at: " + match.getLocation());
 *     }
 * }
 *
 * // Get best match
 * Optional<Match> best = result.getBestMatch();
 * if (best.isPresent()) {
 *     Match bestMatch = best.get();
 *     System.out.println("Best match score: " + bestMatch.getScore());
 *     action.click(bestMatch);
 * }
 * }</pre>
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
    // private Location target; //
    private Image image;
    private String text = "";
    private String name =
            ""; // in SikuliX Element, extended by many SikuliX object classes, including Match.
    private Location
            target; // Location has a Brobot region (SikuliX Match has a SikuliX Region) and a
    // Position and offsets.
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
    @JsonIgnore private Mat histogram;
    private Scene scene;
    private LocalDateTime timeStamp;
    // the old MatchObject had `private double duration;`
    private int timesActedOn = 0;

    /**
     * Default constructor for Jackson JSON mapping.
     */
    public Match() {} // for mapping

    /**
     * Creates a Match with the specified region as its location.
     *
     * @param region the region where this match was found
     */
    public Match(Region region) {
        this.target = new Location(region);
    }

    /**
     * Returns the x-coordinate of the match region's top-left corner.
     *
     * @return the x-coordinate, or 0 if no region is set
     */
    public int x() {
        Region region = getRegion();
        return region != null ? region.x() : 0;
    }

    /**
     * Returns the y-coordinate of the match region's top-left corner.
     *
     * @return the y-coordinate, or 0 if no region is set
     */
    public int y() {
        Region region = getRegion();
        return region != null ? region.y() : 0;
    }

    /**
     * Returns the width of the match region.
     *
     * @return the width in pixels, or 0 if no region is set
     */
    public int w() {
        Region region = getRegion();
        return region != null ? region.w() : 0;
    }

    /**
     * Returns the height of the match region.
     *
     * @return the height in pixels, or 0 if no region is set
     */
    public int h() {
        Region region = getRegion();
        return region != null ? region.h() : 0;
    }

    /**
     * Returns the region where this match was found on the screen.
     *
     * @return the match region, or null if no target location is set
     */
    public Region getRegion() {
        if (target == null) return null;
        return target.getRegion();
    }

    /**
     * Sets the region for this match, creating a target location if necessary.
     *
     * @param region the new region for this match
     */
    public void setRegion(Region region) {
        if (target == null) {
            target = new Location(region);
        } else target.setRegion(region);
    }

    /**
     * Returns the OpenCV Mat representation of the match image in BGR format.
     *
     * @return the Mat object, or null if no image is set
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Mat getMat() {
        return image != null ? image.getMatBGR() : null;
    }

    /**
     * Compares this match with another match by their similarity scores.
     *
     * @param m the match to compare with
     * @return positive if this match has higher score, negative if lower, zero if equal
     */
    public double compareByScore(Match m) {
        return score - m.getScore();
    }

    /**
     * Returns the area of the match region in pixels.
     *
     * @return the width * height of the match region
     */
    public int size() { // TODO: check. this used to refer to the size of the Pattern
        return getRegion().w() * getRegion().h();
    }

    /**
     * Increments the counter tracking how many times this match has been acted upon.
     */
    public void incrementTimesActedOn() {
        timesActedOn++;
    }

    /**
     * Extracts and sets the match image from the associated scene using the match region.
     * If no scene is set, this method does nothing.
     */
    public void setImageWithScene() {
        if (scene == null) return;
        BufferedImage bImg =
                BufferedImageUtilities.getSubImage(scene.getPattern().getBImage(), getRegion());
        if (image == null) image = new Image(bImg);
        else image.setBufferedImage(bImg);
    }

    /**
     * Returns the name of the State that owns the StateObject that produced this match.
     *
     * @return the owner state name from the state object metadata
     */
    public String getOwnerStateName() {
        return stateObjectData.getOwnerStateName();
    }

    /**
     * If there is a StateObject, we try to recreate it as a StateImage. The StateObject was likely
     * a StateImage itself. If there is no Pattern, there are no SearchRegions. Since the Match
     * region needs to be saved, a new Pattern is created and the Match region is saved to the fixed
     * region of the Pattern's SearchRegions. In an operation like Find.ALL_WORDS, there may not be
     * a StateObject, although Pattern objects should be created.
     *
     * @return the StateImage created from the Match
     */
    public StateImage toStateImage() {
        StateImage stateImage = new StateImage();
        stateImage.setName(name);
        if (stateObjectData != null) {
            stateImage.setOwnerStateName(stateObjectData.getOwnerStateName());
            if (!stateObjectData.getStateObjectName().isEmpty())
                stateImage.setName(
                        stateObjectData
                                .getStateObjectName()); // the StateObject name should take priority
            // since it was the original StateImage
        }
        stateImage.addPatterns(new Pattern(this));
        return stateImage;
    }

    /**
     * Converts this Match to an ObjectCollection containing this match as a StateImage. Useful for
     * Action methods that require ObjectCollection parameters.
     *
     * @return ObjectCollection containing this Match converted to a StateImage
     */
    public ObjectCollection toObjectCollection() {
        return new ObjectCollection.Builder().withMatchObjectsAsStateImages(this).build();
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
            stringBuilder.append(
                    String.format(
                            "%sR[%d,%d %dx%d] simScore:%.1f",
                            nameText, r.x(), r.y(), r.w(), r.h(), score));
        } else {
            stringBuilder.append(String.format("%sR[null] simScore:%.1f", nameText, score));
        }
        if (getText() != null && !getText().isEmpty())
            stringBuilder.append(" text:").append(getText());
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    /**
     * Converts this Brobot Match to a SikuliX Match for compatibility with SikuliX operations.
     *
     * @return a SikuliX Match object with the same region and score
     */
    public org.sikuli.script.Match sikuli() {
        return new org.sikuli.script.Match(getRegion().sikuli(), score);
    }

    /**
     * Builder class for constructing Match instances with a fluent API.
     * Supports creating matches from SikuliX matches, regions, images, and metadata.
     */
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

        /**
         * Sets the SikuliX match to convert to a Brobot Match.
         *
         * @param sikuliMatch the SikuliX match to convert
         * @return this builder for method chaining
         */
        public Builder setSikuliMatch(org.sikuli.script.Match sikuliMatch) {
            this.sikuliMatch = sikuliMatch;
            return this;
        }

        /**
         * Copies properties from an existing Match to this builder.
         *
         * @param match the match to copy properties from
         * @return this builder for method chaining
         */
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

        /**
         * Sets the region where the match was found.
         *
         * @param region the match region
         * @return this builder for method chaining
         */
        public Builder setRegion(Region region) {
            this.region = region;
            return this;
        }

        /**
         * Sets the position within the region for action targeting.
         *
         * @param position the target position (e.g., CENTER, TOP_LEFT)
         * @return this builder for method chaining
         */
        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        /**
         * Sets the horizontal offset from the target position.
         *
         * @param offsetX the x-axis offset in pixels
         * @return this builder for method chaining
         */
        public Builder setOffsetX(int offsetX) {
            this.offsetX = offsetX;
            return this;
        }

        /**
         * Sets the vertical offset from the target position.
         *
         * @param offsetY the y-axis offset in pixels
         * @return this builder for method chaining
         */
        public Builder setOffsetY(int offsetY) {
            this.offsetY = offsetY;
            return this;
        }

        /**
         * Sets both horizontal and vertical offsets from a Location.
         *
         * @param offset the location containing x and y offsets
         * @return this builder for method chaining
         */
        public Builder setOffset(Location offset) {
            this.offsetX = offset.getCalculatedX();
            this.offsetY = offset.getCalculatedY();
            return this;
        }

        /**
         * Sets the image captured from the screen at the match location.
         *
         * @param image the screen capture at the match location
         * @return this builder for method chaining
         */
        public Builder setImage(Image image) {
            this.image = image;
            return this;
        }

        /**
         * Sets the region from an OpenCV Rect.
         *
         * @param rect the OpenCV rectangle defining the match area
         * @return this builder for method chaining
         */
        public Builder setRegion(Rect rect) {
            this.region = new Region(rect);
            return this;
        }

        /**
         * Sets the region using explicit coordinates and dimensions.
         *
         * @param x the x-coordinate of the top-left corner
         * @param y the y-coordinate of the top-left corner
         * @param w the width in pixels
         * @param h the height in pixels
         * @return this builder for method chaining
         */
        public Builder setRegion(int x, int y, int w, int h) {
            this.region = new Region(x, y, w, h);
            return this;
        }

        /**
         * Sets the descriptive name for this match.
         *
         * @param name the match name
         * @return this builder for method chaining
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the text content found at the match location (for OCR matches).
         *
         * @param text the OCR text
         * @return this builder for method chaining
         */
        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        /**
         * Sets the BufferedImage captured from the match location.
         *
         * @param bufferedImage the captured image
         * @return this builder for method chaining
         */
        public Builder setBufferedImage(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
            return this;
        }

        /**
         * Sets the BufferedImage by converting from an OpenCV Mat.
         *
         * @param mat the OpenCV Mat to convert
         * @return this builder for method chaining
         */
        public Builder setBufferedImage(Mat mat) {
            this.bufferedImage = BufferedImageUtilities.fromMat(mat);
            return this;
        }

        /**
         * Sets the pattern image that was searched for to find this match.
         *
         * @param bufferedImage the search pattern image
         * @return this builder for method chaining
         */
        public Builder setSearchImage(BufferedImage bufferedImage) {
            this.searchImage = new Image(bufferedImage);
            return this;
        }

        /**
         * Sets the search pattern image from an OpenCV Mat.
         *
         * @param mat the OpenCV Mat of the search pattern
         * @return this builder for method chaining
         */
        public Builder setSearchImage(Mat mat) {
            this.searchImage = new Image(mat);
            return this;
        }

        /**
         * Sets the search pattern image.
         *
         * @param image the search pattern
         * @return this builder for method chaining
         */
        public Builder setSearchImage(Image image) {
            this.searchImage = image;
            return this;
        }

        /**
         * Sets the anchor points used for relative positioning.
         *
         * @param anchors the anchor configuration
         * @return this builder for method chaining
         */
        public Builder setAnchors(Anchors anchors) {
            this.anchors = anchors;
            return this;
        }

        /**
         * Sets metadata from the StateObject that produced this match.
         *
         * @param stateObject the source state object
         * @return this builder for method chaining
         */
        public Builder setStateObjectData(StateObject stateObject) {
            this.stateObjectData = new StateObjectMetadata(stateObject);
            return this;
        }

        /**
         * Sets the state object metadata directly.
         *
         * @param stateObjectData the metadata to set
         * @return this builder for method chaining
         */
        public Builder setStateObjectData(StateObjectMetadata stateObjectData) {
            this.stateObjectData = stateObjectData;
            return this;
        }

        /**
         * Sets the histogram for the match image (used for color analysis).
         *
         * @param histogram the OpenCV histogram
         * @return this builder for method chaining
         */
        public Builder setHistogram(Mat histogram) {
            this.histogram = histogram;
            return this;
        }

        /**
         * Sets the scene containing the full screen capture where this match was found.
         *
         * @param scene the scene object
         * @return this builder for method chaining
         */
        public Builder setScene(Scene scene) {
            this.scene = scene;
            return this;
        }

        /**
         * Sets the similarity score for this match.
         *
         * @param simScore the similarity score (0.0 to 1.0)
         * @return this builder for method chaining
         */
        public Builder setSimScore(double simScore) {
            this.simScore = simScore;
            return this;
        }

        /**
         * Internal method to set the appropriate image on the match.
         * Priority: explicit image > bufferedImage > scene extraction.
         *
         * @param match the match to set the image on
         */
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

        /**
         * Builds and returns a new Match instance with the configured properties.
         *
         * @return a new Match instance
         */
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
            if (text != null && !text.isEmpty())
                match.setText(text); // otherwise, this erases the SikuliX match text
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
        return Double.compare(match.score, score) == 0
                && Objects.equals(name, match.name)
                && Objects.equals(text, match.text)
                && Objects.equals(getRegion(), match.getRegion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, name, text, getRegion());
    }
}
