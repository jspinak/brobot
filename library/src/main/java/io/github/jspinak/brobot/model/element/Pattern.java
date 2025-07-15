package io.github.jspinak.brobot.model.element;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import io.github.jspinak.brobot.util.string.FilenameExtractor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.Mat;


import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Represents a visual template for pattern matching in the Brobot GUI automation framework.
 * 
 * <p>Pattern is the fundamental unit of visual recognition in Brobot, encapsulating an image 
 * template along with its matching parameters and metadata. It serves as the building block 
 * for StateImages and provides the core pattern matching capability that enables visual 
 * GUI automation.</p>
 * 
 * <p>Key components:
 * <ul>
 *   <li><b>Image Data</b>: The actual visual template stored as a BufferedImage</li>
 *   <li><b>Search Configuration</b>: Regions where this pattern should be searched for</li>
 *   <li><b>Target Parameters</b>: Position and offset for precise interaction points</li>
 *   <li><b>Match History</b>: Historical data for mocking and analysis</li>
 *   <li><b>Anchors</b>: Reference points for defining relative positions</li>
 * </ul>
 * </p>
 * 
 * <p>Pattern types:
 * <ul>
 *   <li><b>Fixed</b>: Patterns that always appear in the same screen location</li>
 *   <li><b>Dynamic</b>: Patterns with changing content that require special handling</li>
 *   <li><b>Standard</b>: Regular patterns that can appear anywhere within search regions</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Patterns provide the visual vocabulary for describing 
 * GUI elements. They enable the framework to recognize and interact with GUI components 
 * regardless of the underlying technology, making automation truly cross-platform and 
 * technology-agnostic.</p>
 * 
 * @since 1.0
 * @see StateImage
 * @see Image
 * @see Match
 * @see SearchRegions
 * @see Anchors
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pattern {

    // fields from SikuliX Pattern
    private String url; // originally type URL, which requires conversion for use with JPA
    private String imgpath;

    private String name;
    /*
    An image that should always appear in the same location has fixed==true.
    */
    private boolean fixed = false;
    private SearchRegions searchRegions = new SearchRegions();
    private boolean setKmeansColorProfiles = false; // this is an expensive operation and should be done only when needed
    //@Embedded
    //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
    //@Embedded
    //private ColorCluster colorCluster = new ColorCluster();
    private ActionHistory matchHistory = new ActionHistory();
    private int index; // a unique identifier used for classification matrices
    private boolean dynamic = false; // dynamic images cannot be found using pattern matching
    private Position targetPosition = new Position(.5,.5); // use to convert a match to a location
    private Location targetOffset = new Location(0,0); // adjusts the match location
    private Anchors anchors = new Anchors(); // for defining regions using this object as input

    /*
    The SikuliX Image object is part of Pattern. It loads an image from file or the web into memory
    and provides a BufferedImage object. There are, however, occasions requiring an image to be
    captured only in memory. For example, when Brobot creates a model of the environment, the images
    captured can be saved in a database and do not need to be on file. This could be achieved with a
    SikuliX Image by setting the BufferedImage directly `super(bimg)`.

    It would be possible to use the SikuliX Image within Pattern for working with the physical representation of
    an image, but this raises architecture concerns in Brobot. Most importantly, Match needs a representation of the
    image, and having Pattern within Match would create a circular class chain. Pattern also needs a MatchHistory to
    perform mock runs and MatchHistory contains a list of MatchSnapshot, which itself contains a list of Match objects.
    For this reason, I've created a Brobot Image object and use this object in Match and in Pattern.
     */
    private Image image;

    public Pattern(String imgPath) {
        // Add this safety check at the beginning
        if (imgPath == null || imgPath.isEmpty()) {
            return;
        }
        setImgpath(imgPath);
        setNameFromFilenameIfEmpty(imgPath);
        
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        if (env.useRealFiles()) {
            // Load real image - works in headless but not mock mode
            BufferedImage bufferedImage = BufferedImageUtilities.getBuffImgFromFile(imgPath);
            if (bufferedImage == null) {
                throw new IllegalStateException("Failed to load image: " + imgPath + 
                    ". Make sure the image exists in the configured image path and has .png extension.");
            }
            this.image = new Image(bufferedImage, name);
        } else {
            // Only create dummy in mock mode
            this.image = new Image(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB), name);
        }
    }

    public Pattern(BufferedImage bimg) {
        image = new Image(bimg);
    }

    public Pattern(Image image) {
        this.image = image;
        setName(image.getName());
    }

    public Pattern(Match match) {
        Image imageToUse = null;
        if (match.getImage() != null) imageToUse = match.getImage();
        else if (match.getSearchImage() != null) imageToUse = match.getSearchImage();
        fixed = true;
        searchRegions.setFixedRegion(match.getRegion());
        if (imageToUse != null) {
            image = imageToUse;
            name = match.getName();
        }
        else imgpath = match.getName();
    }

    public Pattern(Mat mat) {
        image = new Image(mat);
    }

    /**
     * Creates a generic Pattern without an associated image.
     */
    public Pattern() {}

    /**
     * Converts the BufferedImage in Image to a BGR JavaCV Mat.
     * @return a BGR Mat.
     */
    @JsonIgnore
    public Mat getMat() {
        return image.getMatBGR();
    }

    /**
     * Setting the Mat involves converting the BGR Mat parameter to a BufferedImage and saving it in the SukiliX Image.
     * Mat objects are not stored explicitly in Pattern but converted to and from BufferedImage objects.
     * @param matBGR a JavaCV Mat in BGR format.
     */
    @JsonIgnore
    public void setMat(Mat matBGR) {
        image.setBufferedImage(BufferedImageUtilities.fromMat(matBGR));
    }

    /**
     * Converts the BufferedImage in Image to an HSV JavaCV Mat.
     * @return an HSV Mat.
     */
    @JsonIgnore
    public Mat getMatHSV() {
        return image.getMatHSV();
    }

    @JsonIgnore
    private void setNameFromFilenameIfEmpty(String filename) {
        if (filename == null) return;
        if (name == null || name.isEmpty()) {
            setName(FilenameExtractor.getFilenameWithoutExtensionAndDirectory(filename));
        }
    }

    public int w() {
        if (image == null) return 0;
        return image.w();
    }

    public int h() {
        if (image == null) return 0;
        return image.h();
    }

    /**
     * If the image has a fixed location and has already been found, the region where it was found is returned.
     * If there are multiple regions, this returns a random selection.
     * @return a region
     */
    @JsonIgnore
    public Region getRegion() {
        return searchRegions.getFixedIfDefinedOrRandomRegion(fixed);
    }

    /**
     * If the image has a fixed location and has already been found, the region where it was found is returned.
     * Otherwise, all regions are returned.
     * @return all usable regions
     */
    @JsonIgnore
    public List<Region> getRegions() {
        return searchRegions.getRegions(fixed);
    }

    public void addSearchRegion(Region region) {
        searchRegions.addSearchRegions(region);
    }

    public void resetFixedSearchRegion() {
        searchRegions.resetFixedRegion();
    }

    @JsonIgnore
    public void setSearchRegionsTo(Region... regions) {
        searchRegions.setRegions(List.of(regions));
    }

    public int size() {
        return w() * h();
    }

    public void addMatchSnapshot(ActionRecord matchSnapshot) {
        matchHistory.addSnapshot(matchSnapshot);
    }

    public void addMatchSnapshot(int x, int y, int w, int h) {
        ActionRecord matchSnapshot = new ActionRecord(x, y, w, h);
        addMatchSnapshot(matchSnapshot);
    }

    public StateImage inNullState() {
        return new StateImage.Builder()
                .addPattern(this)
                .setName(name)
                .setOwnerStateName("null")
                .build();
    }

    @JsonIgnore
    public boolean isDefined() {
        return searchRegions.isDefined(fixed);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return image == null || image.isEmpty();
    }

    // __convenience functions for the SikuliX Pattern object__
    /**
     * Another way to get the SikuliX object.
     * @return the SikuliX Pattern object.
     */
    @JsonIgnore
    public org.sikuli.script.Pattern sikuli() {
        if (image == null) {
            throw new IllegalStateException("Cannot create SikuliX Pattern: Image is null for pattern: " + name);
        }
        if (image.isEmpty()) {
            throw new IllegalStateException("Cannot create SikuliX Pattern: Image has no BufferedImage. " +
                "Image file may not exist or failed to load for pattern: " + name);
        }
        return new org.sikuli.script.Pattern(image.sikuli());
    }

    @JsonIgnore
    public BufferedImage getBImage() {
        return image.getBufferedImage();
    }

    @Override
    public String toString() {
        return "Pattern{" +
                "name='" + name + '\'' +
                ", imgpath='" + imgpath + '\'' +
                ", fixed=" + fixed +
                ", dynamic=" + dynamic +
                ", index=" + index +
                ", image=" + (image != null ? image.toString() : "null") +
                ", searchRegions=" + searchRegions +
                ", targetPosition=" + targetPosition +
                ", targetOffset=" + targetOffset +
                ", anchors=" + anchors +
                ", w=" + w() +
                ", h=" + h() +
                '}';
    }

    @Slf4j
    public static class Builder {
        private String name = "";
        private Image image;
        private BufferedImage bufferedImage;
        private String filename = null;
        private boolean fixed = false;
        private SearchRegions searchRegions = new SearchRegions();
        private boolean setKmeansColorProfiles = false;
        //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
        private ActionHistory matchHistory = new ActionHistory();
        private int index;
        private boolean dynamic = false;
        private Position targetPosition = new Position(.5,.5);
        private Location targetOffset = new Location(0, 0);
        private Anchors anchors = new Anchors();

        /*
        This method allows you to change the name of the Pattern without changing the filename.
        Sometimes there is no filename, as when building the state structure and saving images to a database.
        Therefore, this method should not set the filename in addition to the name.
         */
        public Builder setName(String name) {
            this.name = name;
            //if (this.filename.isEmpty()) this.filename = name;
            //if (this.image == null) this.image = new Image(BufferedImageOps.getBuffImgFromFile(filename), name);
            return this;
        }

        public Builder setMat(Mat mat) {
            this.bufferedImage = BufferedImageUtilities.fromMat(mat);
            return this;
        }

        public Builder setImage(Image image) {
            this.image = image;
            return this;
        }

        public Builder setBufferedImage(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
            return this;
        }

        public Builder setFilename(String filename) {
            this.filename = filename;
            if (name.isEmpty()) name = FilenameExtractor.getFilenameWithoutExtensionAndDirectory(filename);
            this.image = new Image(BufferedImageUtilities.getBuffImgFromFile(filename), name);
            return this;
        }

        public Builder setFixed(boolean isFixed) {
            this.fixed = isFixed;
            return this;
        }

        public Builder setFixedRegion(Region fixedRegion) {
            this.searchRegions.setFixedRegion(fixedRegion);
            return this;
        }

        public Builder setSearchRegions(SearchRegions searchRegions) {
            this.searchRegions = searchRegions;
            return this;
        }

        public Builder addSearchRegion(Region searchRegion) {
            this.searchRegions.addSearchRegions(searchRegion);
            return this;
        }

        public Builder setSetKmeansColorProfiles(boolean setKmeansColorProfiles) {
            this.setKmeansColorProfiles = setKmeansColorProfiles;
            return this;
        }

        //public Builder setKmeansProfilesAllSchemas(KmeansProfilesAllSchemas kmeansProfilesAllSchemas) {
        //    this.kmeansProfilesAllSchemas = kmeansProfilesAllSchemas;
        //    return this;
        //}

        public Builder setMatchHistory(ActionHistory matchHistory) {
            this.matchHistory = matchHistory;
            return this;
        }

        public Builder addMatchSnapshot(ActionRecord matchSnapshot) {
            this.matchHistory.addSnapshot(matchSnapshot);
            return this;
        }

        public Builder setIndex(int index) {
            this.index = index;
            return this;
        }

        public Builder setDynamic(boolean isDynamic) {
            this.dynamic = isDynamic;
            return this;
        }

        public Builder setTargetPosition(Position targetPosition) {
            this.targetPosition = targetPosition;
            return this;
        }

        /**
         * Set the target position as a percent of the width and height of the image.
         * For width, 0 is the leftmost point and 100 the rightmost point.
         * @param w percent of width
         * @param h percent of height
         * @return the Builder
         */
        public Builder setTargetPosition(int w, int h) {
            this.targetPosition = new Position(w, h);
            return this;
        }

        public Builder setTargetOffset(Location location) {
            this.targetOffset = location;
            return this;
        }

        /**
         * Move the target location by x and y.
         * @param x move x by
         * @param y move y by
         * @return the Builder
         */
        public Builder setTargetOffset(int x, int y) {
            this.targetOffset = new Location(x,y);
            return this;
        }

        public Builder setAnchors(Anchors anchors) {
            this.anchors = anchors;
            return this;
        }

        public Builder addAnchor(Anchor anchor) {
            this.anchors.add(anchor);
            return this;
        }

        public Builder addAnchor(Positions.Name inRegionToDefine, Positions.Name inMatch) {
            this.anchors.add(new Anchor(inRegionToDefine, new Position(inMatch)));
            return this;
        }

        private Pattern makeNewPattern() {
            if (filename != null) return new Pattern(filename);
            if (bufferedImage != null) return new Pattern(bufferedImage);
            return new Pattern();
        }

        /*
        Sets the image in the Builder.
         */
        private void createImageFromSources(Pattern pattern) {
            if (this.image != null) {
                pattern.setImage(this.image);
                if (name != null && pattern.getImage() != null) pattern.getImage().setName(name);
            } else if (this.bufferedImage != null) {
                pattern.setImage(new Image(this.bufferedImage, this.name));
            } else if (this.filename != null && !this.filename.isEmpty()) {
                BufferedImage loadedImage = BufferedImageUtilities.getBuffImgFromFile(this.filename);
                if (loadedImage != null) {
                    pattern.setImage(new Image(loadedImage, this.name));
                    pattern.setImgpath(this.filename);
                } else {
                    // Log warning but don't create an Image with null BufferedImage
                    log.error("Failed to load image from file: {}", this.filename);
                }
            }
        }

        public Pattern build() {
            Pattern pattern = new Pattern(); // Start with a truly empty pattern
            if (name != null) pattern.setName(name);

            createImageFromSources(pattern);

            pattern.setFixed(fixed);
            pattern.setSearchRegions(searchRegions);
            pattern.setSetKmeansColorProfiles(setKmeansColorProfiles);
            pattern.setMatchHistory(matchHistory);
            pattern.setIndex(index);
            pattern.setDynamic(dynamic);
            pattern.setTargetPosition(targetPosition);
            pattern.setTargetOffset(targetOffset);
            pattern.setAnchors(anchors);
            return pattern;
        }
    }

}
