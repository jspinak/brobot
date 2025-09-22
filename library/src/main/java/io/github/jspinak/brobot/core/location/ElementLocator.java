package io.github.jspinak.brobot.core.location;

import java.util.List;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Core interface for element location operations.
 *
 * <p>This interface defines the contract for locating GUI elements on screen, orchestrating pattern
 * matching and screen capture operations. It sits at a higher level than PatternMatcher but lower
 * than Find/Actions, providing element location services without any circular dependencies.
 *
 * <p>ElementLocator is the key abstraction that breaks the circular dependency between Find and
 * other actions. Actions that need to find elements use ElementLocator directly instead of
 * depending on Find.
 *
 * <p>Key design principles:
 *
 * <ul>
 *   <li>Depends ONLY on core services (PatternMatcher, ScreenCaptureService)
 *   <li>NO dependencies on Find or any Action classes
 *   <li>Provides element location without action semantics
 *   <li>Thread-safe and stateless
 * </ul>
 *
 * @since 2.0.0
 */
public interface ElementLocator {

    /**
     * Request object for element location operations.
     * Encapsulates all parameters needed for locating elements, providing a clean and extensible API.
     */
    class LocateRequest {
        private List<Pattern> patterns;
        private List<StateImage> stateImages;
        private List<Region> searchRegions;
        private FindStrategy strategy = FindStrategy.ALL;
        private double similarity = 0.7;
        private int maxMatches = Integer.MAX_VALUE;

        /**
         * Gets the patterns to search for.
         *
         * @return list of patterns
         */
        public List<Pattern> getPatterns() {
            return patterns;
        }

        /**
         * Sets the patterns to search for.
         *
         * @param patterns list of patterns to find
         */
        public void setPatterns(List<Pattern> patterns) {
            this.patterns = patterns;
        }

        /**
         * Gets the state images to search for.
         *
         * @return list of state images
         */
        public List<StateImage> getStateImages() {
            return stateImages;
        }

        /**
         * Sets the state images to search for.
         *
         * @param stateImages list of state images to find
         */
        public void setStateImages(List<StateImage> stateImages) {
            this.stateImages = stateImages;
        }

        /**
         * Gets the regions to search within.
         *
         * @return list of search regions
         */
        public List<Region> getSearchRegions() {
            return searchRegions;
        }

        /**
         * Sets the regions to search within.
         *
         * @param searchRegions list of regions to constrain search
         */
        public void setSearchRegions(List<Region> searchRegions) {
            this.searchRegions = searchRegions;
        }

        /**
         * Gets the find strategy.
         *
         * @return the current find strategy
         */
        public FindStrategy getStrategy() {
            return strategy;
        }

        /**
         * Sets the find strategy.
         *
         * @param strategy strategy to use for finding elements
         */
        public void setStrategy(FindStrategy strategy) {
            this.strategy = strategy;
        }

        /**
         * Gets the similarity threshold.
         *
         * @return similarity threshold (0.0-1.0)
         */
        public double getSimilarity() {
            return similarity;
        }

        /**
         * Sets the similarity threshold for matching.
         *
         * @param similarity threshold value (0.0-1.0)
         */
        public void setSimilarity(double similarity) {
            this.similarity = similarity;
        }

        /**
         * Gets the maximum number of matches to find.
         *
         * @return maximum match count
         */
        public int getMaxMatches() {
            return maxMatches;
        }

        /**
         * Sets the maximum number of matches to find.
         *
         * @param maxMatches maximum number of matches
         */
        public void setMaxMatches(int maxMatches) {
            this.maxMatches = maxMatches;
        }

        /**
         * Builder for creating LocateRequest instances.
         */
        public static class Builder {
            private final LocateRequest request = new LocateRequest();

            /**
             * Sets patterns to search for.
             *
             * @param patterns list of patterns
             * @return this builder for chaining
             */
            public Builder withPatterns(List<Pattern> patterns) {
                request.patterns = patterns;
                return this;
            }

            /**
             * Sets state images to search for.
             *
             * @param stateImages list of state images
             * @return this builder for chaining
             */
            public Builder withStateImages(List<StateImage> stateImages) {
                request.stateImages = stateImages;
                return this;
            }

            /**
             * Sets regions to search within.
             *
             * @param searchRegions list of regions
             * @return this builder for chaining
             */
            public Builder withSearchRegions(List<Region> searchRegions) {
                request.searchRegions = searchRegions;
                return this;
            }

            /**
             * Sets the find strategy.
             *
             * @param strategy find strategy to use
             * @return this builder for chaining
             */
            public Builder withStrategy(FindStrategy strategy) {
                request.strategy = strategy;
                return this;
            }

            /**
             * Sets the similarity threshold.
             *
             * @param similarity threshold (0.0-1.0)
             * @return this builder for chaining
             */
            public Builder withSimilarity(double similarity) {
                request.similarity = similarity;
                return this;
            }

            /**
             * Sets the maximum matches to find.
             *
             * @param maxMatches maximum match count
             * @return this builder for chaining
             */
            public Builder withMaxMatches(int maxMatches) {
                request.maxMatches = maxMatches;
                return this;
            }

            /**
             * Builds the LocateRequest with configured parameters.
             *
             * @return the constructed LocateRequest
             */
            public LocateRequest build() {
                return request;
            }
        }
    }

    /** Find strategies that determine how elements are located. */
    enum FindStrategy {
        /** Find all occurrences of all patterns */
        ALL,
        /** Find only the best match across all patterns */
        BEST,
        /** Find the first match and stop */
        FIRST,
        /** Find one match per pattern/StateImage */
        EACH
    }

    /**
     * Result of an element location operation.
     * This lightweight class contains the found element without action-specific semantics.
     */
    class Element {
        private int x;
        private int y;
        private int width;
        private int height;
        private double confidence;
        private String name;
        private Pattern sourcePattern;
        private StateImage sourceStateImage;

        /**
         * Gets the x-coordinate of the element.
         *
         * @return x-coordinate in pixels
         */
        public int getX() {
            return x;
        }

        /**
         * Sets the x-coordinate of the element.
         *
         * @param x x-coordinate in pixels
         */
        public void setX(int x) {
            this.x = x;
        }

        /**
         * Gets the y-coordinate of the element.
         *
         * @return y-coordinate in pixels
         */
        public int getY() {
            return y;
        }

        /**
         * Sets the y-coordinate of the element.
         *
         * @param y y-coordinate in pixels
         */
        public void setY(int y) {
            this.y = y;
        }

        /**
         * Gets the width of the element.
         *
         * @return width in pixels
         */
        public int getWidth() {
            return width;
        }

        /**
         * Sets the width of the element.
         *
         * @param width width in pixels
         */
        public void setWidth(int width) {
            this.width = width;
        }

        /**
         * Gets the height of the element.
         *
         * @return height in pixels
         */
        public int getHeight() {
            return height;
        }

        /**
         * Sets the height of the element.
         *
         * @param height height in pixels
         */
        public void setHeight(int height) {
            this.height = height;
        }

        /**
         * Gets the confidence score of the match.
         *
         * @return confidence score (0.0-1.0)
         */
        public double getConfidence() {
            return confidence;
        }

        /**
         * Sets the confidence score of the match.
         *
         * @param confidence confidence score (0.0-1.0)
         */
        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        /**
         * Gets the name of the element.
         *
         * @return element name or null
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name of the element.
         *
         * @param name element name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the pattern that matched this element.
         *
         * @return source pattern or null
         */
        public Pattern getSourcePattern() {
            return sourcePattern;
        }

        /**
         * Sets the pattern that matched this element.
         *
         * @param sourcePattern the matching pattern
         */
        public void setSourcePattern(Pattern sourcePattern) {
            this.sourcePattern = sourcePattern;
        }

        /**
         * Gets the state image that contained the matching pattern.
         *
         * @return source state image or null
         */
        public StateImage getSourceStateImage() {
            return sourceStateImage;
        }

        /**
         * Sets the state image that contained the matching pattern.
         *
         * @param sourceStateImage the source state image
         */
        public void setSourceStateImage(StateImage sourceStateImage) {
            this.sourceStateImage = sourceStateImage;
        }

        /**
         * Gets the center location of this element.
         *
         * @return center point as Location
         */
        public io.github.jspinak.brobot.model.element.Location getLocation() {
            return new io.github.jspinak.brobot.model.element.Location(
                    x + width / 2, y + height / 2);
        }

        /**
         * Gets the region bounds of this element.
         *
         * @return bounding box as Region
         */
        public Region getRegion() {
            return new Region(x, y, width, height);
        }
    }

    /**
     * Locates elements on screen based on the request parameters.
     *
     * <p>This is the main method that orchestrates screen capture and pattern matching to locate
     * GUI elements. The behavior is controlled by the FindStrategy in the request.
     *
     * @param request The location request containing patterns and parameters
     * @return List of found elements, empty if none found
     */
    List<Element> locate(LocateRequest request);

    /**
     * Locates elements within a specific region.
     *
     * <p>This method optimizes the search by limiting it to a specific screen region, improving
     * performance for targeted searches.
     *
     * @param request The location request containing patterns and parameters
     * @param region The screen region to search within
     * @return List of found elements within the region, empty if none found
     */
    List<Element> locateInRegion(LocateRequest request, Region region);

    /**
     * Checks if an element is still present at its last known location.
     *
     * <p>This method performs a quick verification to see if an element is still visible at the
     * same location, useful for wait operations.
     *
     * @param element The element to verify
     * @return true if the element is still present, false otherwise
     */
    boolean verifyElement(Element element);

    /**
     * Waits for an element to appear on screen.
     *
     * <p>This method repeatedly searches for elements until found or timeout is reached.
     *
     * @param request The location request
     * @param timeoutSeconds Maximum time to wait
     * @return List of found elements, empty if timeout reached
     */
    List<Element> waitForElement(LocateRequest request, double timeoutSeconds);

    /**
     * Waits for an element to disappear from screen.
     *
     * <p>This method repeatedly checks if elements are still present until they disappear or
     * timeout is reached.
     *
     * @param element The element to wait for disappearance
     * @param timeoutSeconds Maximum time to wait
     * @return true if element disappeared, false if timeout reached
     */
    boolean waitForVanish(Element element, double timeoutSeconds);

    /**
     * Gets the name of this element locator implementation.
     *
     * @return Implementation name for logging/debugging
     */
    String getImplementationName();
}
