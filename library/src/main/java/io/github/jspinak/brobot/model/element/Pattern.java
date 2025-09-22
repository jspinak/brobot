package io.github.jspinak.brobot.model.element;

import java.awt.image.BufferedImage;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import io.github.jspinak.brobot.util.string.FilenameExtractor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a visual template for pattern matching in the Brobot GUI automation framework.
 *
 * <p>Pattern is the fundamental unit of visual recognition in Brobot, encapsulating an image
 * template along with its matching parameters and metadata. It serves as the building block for
 * StateImages and provides the core pattern matching capability that enables visual GUI automation.
 *
 * <p>Key components:
 *
 * <ul>
 *   <li><b>Image Data</b>: The actual visual template stored as a BufferedImage
 *   <li><b>Search Configuration</b>: Regions where this pattern should be searched for
 *   <li><b>Target Parameters</b>: Position and offset for precise interaction points
 *   <li><b>Match History</b>: Historical data for mocking and analysis
 *   <li><b>Anchors</b>: Reference points for defining relative positions
 * </ul>
 *
 * <p>Pattern types:
 *
 * <ul>
 *   <li><b>Fixed</b>: Patterns that always appear in the same screen location
 *   <li><b>Dynamic</b>: Patterns with changing content that require special handling
 *   <li><b>Standard</b>: Regular patterns that can appear anywhere within search regions
 * </ul>
 *
 * <p>In the model-based approach, Patterns provide the visual vocabulary for describing GUI
 * elements. They enable the framework to recognize and interact with GUI components regardless of
 * the underlying technology, making automation truly cross-platform and technology-agnostic.
 *
 * @since 1.0
 * @see StateImage
 * @see Image
 * @see Match
 * @see SearchRegions
 * @see Anchors
 */
@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pattern {

    // fields from SikuliX Pattern
    private String url; // originally type URL, which requires conversion for use with JPA
    private String imgpath;

    private String nameWithoutExtension;
    /*
    An image that should always appear in the same location has fixed==true.
    */
    private boolean fixed = false;
    private SearchRegions searchRegions = new SearchRegions();
    private boolean setKmeansColorProfiles =
            false; // this is an expensive operation and should be done only when needed
    // @Embedded
    // private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
    // @Embedded
    // private ColorCluster colorCluster = new ColorCluster();
    private ActionHistory matchHistory = new ActionHistory();
    private int index; // a unique identifier used for classification matrices
    private boolean dynamic = false; // dynamic images cannot be found using pattern matching
    private Position targetPosition = new Position(.5, .5); // use to convert a match to a location
    private Location targetOffset = new Location(0, 0); // adjusts the match location
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
    private boolean needsDelayedLoading = false;

    /**
     * Creates a Pattern from an image file path.
     * In mock mode, image loading is skipped. During Spring initialization,
     * image loading is deferred until the context is ready.
     *
     * @param imgPath the path to the image file
     */
    public Pattern(String imgPath) {
        // Add this safety check at the beginning
        if (imgPath == null || imgPath.isEmpty()) {
            return;
        }
        setImgpath(imgPath);
        setNameFromFilenameIfEmpty(imgPath);

        ExecutionEnvironment env = ExecutionEnvironment.getInstance();

        // In mock mode, never load real images
        if (env.isMockMode()) {
            log.debug("Mock mode - skipping image load for: {}", imgPath);
            this.image = null;
            return;
        }

        // ALWAYS defer image loading during Spring initialization
        // This avoids unnecessary load attempts that will fail
        if (isSpringContextInitializing()) {
            log.debug("Deferring image load during Spring initialization: {}", imgPath);
            this.needsDelayedLoading = true;
            this.image = null;
            return;
        }

        // Only try to load if we're sure the infrastructure is ready
        loadImageNow(imgPath);
    }

    /**
     * Actually load the image. This is called either during construction (if Spring is ready) or
     * later during lazy loading.
     */
    private void loadImageNow(String imgPath) {
        BufferedImage bufferedImage = BufferedImageUtilities.getBuffImgFromFile(imgPath);

        if (bufferedImage != null) {
            this.image = new Image(bufferedImage, nameWithoutExtension);
            this.needsDelayedLoading = false;
            log.debug("Successfully loaded image: {}", imgPath);
        } else {
            // Log error but don't throw exception - let automation continue with null image
            log.error("Failed to load image: {}. Pattern will have null image and find operations will fail.", imgPath);
            this.image = null;
            this.needsDelayedLoading = false; // Don't retry, we've already logged the error
        }
    }

    /**
     * Creates a Pattern from a BufferedImage.
     *
     * @param bimg the BufferedImage to use as the pattern
     */
    public Pattern(BufferedImage bimg) {
        image = new Image(bimg);
    }

    /**
     * Check if we're currently in the Spring context initialization phase. This is a simple
     * heuristic based on whether the SmartImageLoader is available.
     */
    private boolean isSpringContextInitializing() {
        try {
            // If SmartImageLoader instance is not available, we're likely in early initialization
            // This is a simple way to detect if Spring beans are still being created
            return !BufferedImageUtilities.isSmartImageLoaderAvailable();
        } catch (Exception e) {
            // If we can't determine, assume we're initializing
            return true;
        }
    }

    /**
     * Retry loading the image if it was deferred during Spring initialization. This should be
     * called after Spring context is fully initialized.
     *
     * @return true if image was loaded successfully or was already loaded
     */
    public boolean retryImageLoad() {
        if (!needsDelayedLoading || image != null) {
            return true; // Already loaded or doesn't need loading
        }

        try {
            loadImageNow(getImgpath());
            return true;
        } catch (Exception e) {
            log.error("Failed to load deferred image: {}", getImgpath(), e);
            return false;
        }
    }

    /**
     * Override the Lombok-generated getter to implement lazy loading. This ensures images are
     * loaded on first use if they were deferred.
     */
    public Image getImage() {
        ensureImageLoaded();
        return image;
    }

    /**
     * Ensure the image is loaded. This implements lazy loading for patterns that were created
     * during Spring initialization.
     */
    private void ensureImageLoaded() {
        if (needsDelayedLoading && image == null) {
            // Try to load the image now (will log error if it fails)
            retryImageLoad();
        }
    }

    /**
     * Creates a Pattern from a Brobot Image object.
     *
     * @param image the Image object containing the pattern
     */
    public Pattern(Image image) {
        this.image = image;
        if (image != null) {
            setNameWithoutExtension(image.getName());
        }
    }

    /**
     * Creates a Pattern from a Match object.
     * The pattern is marked as fixed at the match location.
     *
     * @param match the Match to create a pattern from
     */
    public Pattern(Match match) {
        Image imageToUse = null;
        if (match.getImage() != null) imageToUse = match.getImage();
        else if (match.getSearchImage() != null) imageToUse = match.getSearchImage();
        fixed = true;
        searchRegions.setFixedRegion(match.getRegion());
        if (imageToUse != null) {
            image = imageToUse;
        }
        // Always set name from match
        nameWithoutExtension = match.getName();
    }

    /**
     * Creates a Pattern from an OpenCV Mat.
     *
     * @param mat the OpenCV Mat containing the image data
     */
    public Pattern(Mat mat) {
        image = new Image(mat);
    }

    /** Creates a generic Pattern without an associated image. */
    public Pattern() {}

    /**
     * Converts the BufferedImage in Image to a BGR JavaCV Mat.
     *
     * @return a BGR Mat.
     */
    @JsonIgnore
    public Mat getMat() {
        ensureImageLoaded();
        return image == null ? new Mat() : image.getMatBGR();
    }

    /**
     * Setting the Mat involves converting the BGR Mat parameter to a BufferedImage and saving it in
     * the SukiliX Image. Mat objects are not stored explicitly in Pattern but converted to and from
     * BufferedImage objects.
     *
     * @param matBGR a JavaCV Mat in BGR format.
     */
    @JsonIgnore
    public void setMat(Mat matBGR) {
        image.setBufferedImage(BufferedImageUtilities.fromMat(matBGR));
    }

    /**
     * Converts the BufferedImage in Image to an HSV JavaCV Mat.
     *
     * @return an HSV Mat.
     */
    @JsonIgnore
    public Mat getMatHSV() {
        ensureImageLoaded();
        return image == null ? new Mat() : image.getMatHSV();
    }

    @JsonIgnore
    private void setNameFromFilenameIfEmpty(String filename) {
        if (filename == null) return;
        if (nameWithoutExtension == null || nameWithoutExtension.isEmpty()) {
            setNameWithoutExtension(FilenameExtractor.getFilenameWithoutExtensionAndDirectory(filename));
        }
    }

    /**
     * Returns the width of the pattern image.
     *
     * @return the width in pixels, or 0 if no image is loaded
     */
    public int w() {
        ensureImageLoaded();
        if (image == null) return 0;
        return image.w();
    }

    /**
     * Returns the height of the pattern image.
     *
     * @return the height in pixels, or 0 if no image is loaded
     */
    public int h() {
        ensureImageLoaded();
        if (image == null) return 0;
        return image.h();
    }

    /**
     * If the image has a fixed location and has already been found, the region where it was found
     * is returned. If there are multiple regions, this returns a random selection.
     *
     * @return a region
     */
    @JsonIgnore
    public Region getRegion() {
        return searchRegions.getFixedIfDefinedOrRandomRegion(fixed);
    }

    /**
     * If the image has a fixed location and has already been found, the region where it was found
     * is returned. Otherwise, all regions are returned.
     *
     * @return all usable regions
     */
    @JsonIgnore
    public List<Region> getRegions() {
        return searchRegions.getRegions(fixed);
    }

    /**
     * Get regions for searching, with full-screen default if none are configured. This is the
     * method to use when actually performing searches.
     *
     * @return regions for searching (never empty)
     */
    @JsonIgnore
    public List<Region> getRegionsForSearch() {
        List<Region> regions = searchRegions.getRegions(fixed);
        if (regions.isEmpty()) {
            return List.of(new Region()); // full screen default
        }
        return regions;
    }

    /**
     * Adds a search region where this pattern should be looked for.
     *
     * @param region the region to add to the search areas
     */
    public void addSearchRegion(Region region) {
        searchRegions.addSearchRegions(region);
    }

    /**
     * Resets the fixed search region, allowing the pattern to be found anywhere.
     */
    public void resetFixedSearchRegion() {
        searchRegions.resetFixedRegion();
    }

    @JsonIgnore
    public void setSearchRegionsTo(Region... regions) {
        searchRegions.setRegions(List.of(regions));
    }

    /**
     * Calculates the area of the pattern image.
     *
     * @return the area in pixels (width * height)
     */
    public int size() {
        return w() * h();
    }

    /**
     * Adds a match snapshot to the pattern's history for analysis and mocking.
     *
     * @param matchSnapshot the ActionRecord to add to history
     */
    public void addMatchSnapshot(ActionRecord matchSnapshot) {
        matchHistory.addSnapshot(matchSnapshot);
    }

    /**
     * Adds a match with the specified coordinates to the pattern's history.
     *
     * @param x the x-coordinate of the match
     * @param y the y-coordinate of the match
     * @param w the width of the match
     * @param h the height of the match
     */
    public void addMatchSnapshot(int x, int y, int w, int h) {
        ActionRecord matchSnapshot = new ActionRecord(x, y, w, h);
        addMatchSnapshot(matchSnapshot);
    }

    /**
     * Creates a StateImage from this pattern, owned by the NULL state.
     *
     * @return a StateImage containing this pattern in the NULL state
     */
    public StateImage inNullState() {
        return new StateImage.Builder()
                .addPattern(this)
                .setName(nameWithoutExtension)
                .setOwnerStateName("null")
                .build();
    }

    @JsonIgnore
    public boolean isDefined() {
        return searchRegions.isDefined(fixed);
    }

    @JsonIgnore
    public boolean isEmpty() {
        ensureImageLoaded();
        return image == null || image.isEmpty();
    }

    // __convenience functions for the SikuliX Pattern object__
    // Cache for SikuliX Pattern to avoid recreating it multiple times
    @JsonIgnore private transient org.sikuli.script.Pattern cachedSikuliPattern = null;

    /**
     * Checks if an image with alpha channel actually has transparent pixels.
     *
     * @param img The image to check
     * @return true if the image has pixels with alpha < 255, false otherwise
     */
    private boolean hasTransparency(BufferedImage img) {
        if (!img.getColorModel().hasAlpha()) {
            return false;
        }

        // Check if any pixel has alpha < 255
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha < 255) {
                    return true; // Found transparency
                }
            }
        }
        return false; // All pixels are fully opaque
    }

    /**
     * Get string representation of BufferedImage type.
     *
     * @param type The BufferedImage type constant
     * @return String representation of the type
     */
    private String getImageTypeString(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
                return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB:
                return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE:
                return "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR:
                return "TYPE_INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "TYPE_4BYTE_ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                return "TYPE_4BYTE_ABGR_PRE";
            default:
                return "Type " + type;
        }
    }

    /**
     * Another way to get the SikuliX object. Uses BufferedImage directly since SikuliX converts to
     * BufferedImage internally anyway. No need for file path loading as it provides no real
     * benefit.
     *
     * @return the SikuliX Pattern object.
     */
    @JsonIgnore
    public org.sikuli.script.Pattern sikuli() {
        // Return cached pattern if available
        if (cachedSikuliPattern != null) {
            return cachedSikuliPattern;
        }

        // Ensure we have a valid image
        if (image == null || image.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot create SikuliX Pattern: No valid image for pattern: " + nameWithoutExtension);
        }

        // Get the BufferedImage - this is what SikuliX uses internally anyway
        BufferedImage buffImg = image.getBufferedImage();

        // Only log pattern creation in debug/verbose mode
        if (log.isDebugEnabled()
                && nameWithoutExtension != null
                && (nameWithoutExtension.contains("prompt") || nameWithoutExtension.contains("claude") || nameWithoutExtension.contains("debug"))) {
            log.debug(
                    "[PATTERN] Creating SikuliX pattern '{}' {}x{} type={}",
                    nameWithoutExtension,
                    buffImg.getWidth(),
                    buffImg.getHeight(),
                    getImageTypeString(buffImg.getType()));
        }

        // Create the SikuliX pattern from BufferedImage
        // This is exactly what SikuliX does internally when given a file path
        cachedSikuliPattern = new org.sikuli.script.Pattern(buffImg);
        return cachedSikuliPattern;
    }

    /**
     * Returns the BufferedImage representation of this pattern.
     *
     * @return the BufferedImage, or null if no image is loaded
     */
    @JsonIgnore
    public BufferedImage getBImage() {
        ensureImageLoaded();
        if (image == null) {
            log.debug("Pattern '{}' has null image", nameWithoutExtension);
            return null;
        }
        return image.getBufferedImage();
    }

    /**
     * Returns a detailed string representation of this pattern.
     *
     * @return string containing pattern name, dimensions, search regions, and other properties
     */
    @Override
    public String toString() {
        return "Pattern{"
                + "name='"
                + nameWithoutExtension
                + '\''
                + ", imgpath='"
                + imgpath
                + '\''
                + ", fixed="
                + fixed
                + ", dynamic="
                + dynamic
                + ", index="
                + index
                + ", image="
                + (image != null ? image.toString() : "null")
                + ", searchRegions="
                + searchRegions
                + ", targetPosition="
                + targetPosition
                + ", targetOffset="
                + targetOffset
                + ", anchors="
                + anchors
                + ", w="
                + w()
                + ", h="
                + h()
                + '}';
    }

    /**
     * Builder class for constructing Pattern instances with a fluent API.
     */
    @Slf4j
    public static class Builder {
        private String name = "";
        private Image image;
        private BufferedImage bufferedImage;
        private String filename = null;
        private boolean fixed = false;
        private SearchRegions searchRegions = new SearchRegions();
        private boolean setKmeansColorProfiles = false;
        // private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new
        // KmeansProfilesAllSchemas();
        private ActionHistory matchHistory = new ActionHistory();
        private int index;
        private boolean dynamic = false;
        private Position targetPosition = new Position(.5, .5);
        private Location targetOffset = new Location(0, 0);
        private Anchors anchors = new Anchors();

        /*
        This method allows you to change the name of the Pattern without changing the filename.
        Sometimes there is no filename, as when building the state structure and saving images to a database.
        Therefore, this method should not set the filename in addition to the name.
         */
        /**
         * Sets the name for this pattern.
         * The name is independent of the filename and can be set without changing the image source.
         *
         * @param name the pattern name
         * @return this builder for method chaining
         */
        public Builder setName(String name) {
            this.name = name;
            // if (this.filename.isEmpty()) this.filename = name;
            // if (this.image == null) this.image = new
            // Image(BufferedImageOps.getBuffImgFromFile(filename), name);
            return this;
        }

        /**
         * Sets the image from an OpenCV Mat.
         *
         * @param mat the OpenCV Mat to convert to BufferedImage
         * @return this builder for method chaining
         */
        public Builder setMat(Mat mat) {
            this.bufferedImage = BufferedImageUtilities.fromMat(mat);
            return this;
        }

        /**
         * Sets the Image object for this pattern.
         *
         * @param image the Image object
         * @return this builder for method chaining
         */
        public Builder setImage(Image image) {
            this.image = image;
            return this;
        }

        /**
         * Sets the BufferedImage for this pattern.
         *
         * @param bufferedImage the BufferedImage to use
         * @return this builder for method chaining
         */
        public Builder setBufferedImage(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
            return this;
        }

        /**
         * Sets the filename and loads the image from file.
         * If name is not set, extracts it from the filename.
         *
         * @param filename the image file path
         * @return this builder for method chaining
         */
        public Builder setFilename(String filename) {
            this.filename = filename;
            if (name.isEmpty())
                name = FilenameExtractor.getFilenameWithoutExtensionAndDirectory(filename);
            this.image = new Image(BufferedImageUtilities.getBuffImgFromFile(filename), name);
            return this;
        }

        /**
         * Sets whether this pattern has a fixed location on screen.
         *
         * @param isFixed true if the pattern always appears in the same location
         * @return this builder for method chaining
         */
        public Builder setFixed(boolean isFixed) {
            this.fixed = isFixed;
            return this;
        }

        /**
         * Sets the fixed region where this pattern is always found.
         *
         * @param fixedRegion the fixed location region
         * @return this builder for method chaining
         */
        public Builder setFixedRegion(Region fixedRegion) {
            this.searchRegions.setFixedRegion(fixedRegion);
            return this;
        }

        /**
         * Sets the search regions configuration for this pattern.
         *
         * @param searchRegions the SearchRegions object
         * @return this builder for method chaining
         */
        public Builder setSearchRegions(SearchRegions searchRegions) {
            this.searchRegions = searchRegions;
            return this;
        }

        /**
         * Adds a search region where this pattern should be looked for.
         *
         * @param searchRegion the region to add
         * @return this builder for method chaining
         */
        public Builder addSearchRegion(Region searchRegion) {
            this.searchRegions.addSearchRegions(searchRegion);
            return this;
        }

        /**
         * Sets whether to generate K-means color profiles for this pattern.
         * This is an expensive operation and should only be enabled when needed.
         *
         * @param setKmeansColorProfiles true to enable K-means profiling
         * @return this builder for method chaining
         */
        public Builder setSetKmeansColorProfiles(boolean setKmeansColorProfiles) {
            this.setKmeansColorProfiles = setKmeansColorProfiles;
            return this;
        }

        // public Builder setKmeansProfilesAllSchemas(KmeansProfilesAllSchemas
        // kmeansProfilesAllSchemas) {
        //    this.kmeansProfilesAllSchemas = kmeansProfilesAllSchemas;
        //    return this;
        // }

        /**
         * Sets the match history for this pattern.
         *
         * @param matchHistory the ActionHistory object
         * @return this builder for method chaining
         */
        public Builder setMatchHistory(ActionHistory matchHistory) {
            this.matchHistory = matchHistory;
            return this;
        }

        /**
         * Adds a match snapshot to the pattern's history.
         *
         * @param matchSnapshot the ActionRecord to add
         * @return this builder for method chaining
         */
        public Builder addMatchSnapshot(ActionRecord matchSnapshot) {
            this.matchHistory.addSnapshot(matchSnapshot);
            return this;
        }

        /**
         * Sets the unique identifier for this pattern.
         * Used for classification matrices and pattern indexing.
         *
         * @param index the unique index
         * @return this builder for method chaining
         */
        public Builder setIndex(int index) {
            this.index = index;
            return this;
        }

        /**
         * Sets whether this pattern has dynamic content.
         * Dynamic images cannot be found using standard pattern matching.
         *
         * @param isDynamic true if the pattern content changes
         * @return this builder for method chaining
         */
        public Builder setDynamic(boolean isDynamic) {
            this.dynamic = isDynamic;
            return this;
        }

        /**
         * Sets the target position within the pattern for interactions.
         *
         * @param targetPosition the Position object
         * @return this builder for method chaining
         */
        public Builder setTargetPosition(Position targetPosition) {
            this.targetPosition = targetPosition;
            return this;
        }

        /**
         * Set the target position as a percent of the width and height of the image. For width, 0
         * is the leftmost point and 100 the rightmost point.
         *
         * @param w percent of width
         * @param h percent of height
         * @return the Builder
         */
        public Builder setTargetPosition(int w, int h) {
            this.targetPosition = new Position(w, h);
            return this;
        }

        /**
         * Sets the target offset from the calculated position.
         *
         * @param location the offset Location
         * @return this builder for method chaining
         */
        public Builder setTargetOffset(Location location) {
            this.targetOffset = location;
            return this;
        }

        /**
         * Move the target location by x and y.
         *
         * @param x move x by
         * @param y move y by
         * @return the Builder
         */
        public Builder setTargetOffset(int x, int y) {
            this.targetOffset = new Location(x, y);
            return this;
        }

        /**
         * Sets the anchors for defining relative positions.
         *
         * @param anchors the Anchors object
         * @return this builder for method chaining
         */
        public Builder setAnchors(Anchors anchors) {
            this.anchors = anchors;
            return this;
        }

        /**
         * Adds an anchor for defining relative positions.
         *
         * @param anchor the Anchor to add
         * @return this builder for method chaining
         */
        public Builder addAnchor(Anchor anchor) {
            this.anchors.add(anchor);
            return this;
        }

        /**
         * Adds an anchor using named positions.
         *
         * @param inRegionToDefine the position in the region being defined
         * @param inMatch the position in the matched pattern
         * @return this builder for method chaining
         */
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
                BufferedImage loadedImage =
                        BufferedImageUtilities.getBuffImgFromFile(this.filename);
                if (loadedImage != null) {
                    pattern.setImage(new Image(loadedImage, this.name));
                    pattern.setImgpath(this.filename);
                } else {
                    // Log warning but don't create an Image with null BufferedImage
                    log.error("Failed to load image from file: {}", this.filename);
                }
            }
        }

        /**
         * Builds and returns a new Pattern with the configured properties.
         *
         * @return a new Pattern instance
         */
        public Pattern build() {
            Pattern pattern = new Pattern(); // Start with a truly empty pattern
            if (name != null) pattern.setNameWithoutExtension(name);

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
