package io.github.jspinak.brobot.datatypes.primitives.image;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.datatypes.primitives.location.*;
//import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.actions.BrobotEnvironment;
import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.stringUtils.NameSelector;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Pattern and StateImage_ (to replace StateImage) are for a code restructuring that gets rid of RegionImagePairs.
 * This can also replace DynamicImage, which currently only uses KmeansProfiles for one of its Image objects.
 * StateImage, which can hold multiple Pattern objects, is currently used for classification. Pattern could also be used
 * but not all Pattern objects are associated with StateImage objects and some are generic (without an image).
 *
 * TODO: If Pattern is never used for classification, you can remove the KMeansProfiles.
 *
 * <p>Database: Pattern is an @Entity since a single Pattern may be in multiple StateImage objects. All fields in
 * Pattern should be @Embedded. This is the lowest level of object that should be stored in the database.
 * I removed the SikuliX Pattern since it is a non-JPA entity and caused problems with persistence. </p>
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
    private MatchHistory matchHistory = new MatchHistory();
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
        
        BrobotEnvironment env = BrobotEnvironment.getInstance();
        
        if (env.useRealFiles()) {
            // Load real image - works in headless but not mock mode
            this.image = new Image(BufferedImageOps.getBuffImgFromFile(imgPath), name);
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
        image.setBufferedImage(BufferedImageOps.fromMat(matBGR));
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
            setName(NameSelector.getFilenameWithoutExtensionAndDirectory(filename));
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

    public void addMatchSnapshot(MatchSnapshot matchSnapshot) {
        matchHistory.addSnapshot(matchSnapshot);
    }

    public void addMatchSnapshot(int x, int y, int w, int h) {
        MatchSnapshot matchSnapshot = new MatchSnapshot(x, y, w, h);
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
            System.out.println("Image is null for pattern: " + name);
            return new org.sikuli.script.Pattern();
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

    public static class Builder {
        private String name = "";
        private Image image;
        private BufferedImage bufferedImage;
        private String filename = null;
        private boolean fixed = false;
        private SearchRegions searchRegions = new SearchRegions();
        private boolean setKmeansColorProfiles = false;
        //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
        private MatchHistory matchHistory = new MatchHistory();
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
            this.bufferedImage = BufferedImageOps.fromMat(mat);
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
            if (name.isEmpty()) name = NameSelector.getFilenameWithoutExtensionAndDirectory(filename);
            this.image = new Image(BufferedImageOps.getBuffImgFromFile(filename), name);
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

        public Builder setMatchHistory(MatchHistory matchHistory) {
            this.matchHistory = matchHistory;
            return this;
        }

        public Builder addMatchSnapshot(MatchSnapshot matchSnapshot) {
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
                pattern.setImage(new Image(BufferedImageOps.getBuffImgFromFile(this.filename), this.name));
                pattern.setImgpath(this.filename);
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
