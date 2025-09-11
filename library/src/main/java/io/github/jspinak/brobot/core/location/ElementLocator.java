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
     * Request object for element location operations. This encapsulates all parameters needed for
     * locating elements, providing a clean and extensible API.
     */
    class LocateRequest {
        private List<Pattern> patterns;
        private List<StateImage> stateImages;
        private List<Region> searchRegions;
        private FindStrategy strategy = FindStrategy.ALL;
        private double similarity = 0.7;
        private int maxMatches = Integer.MAX_VALUE;

        public List<Pattern> getPatterns() {
            return patterns;
        }

        public void setPatterns(List<Pattern> patterns) {
            this.patterns = patterns;
        }

        public List<StateImage> getStateImages() {
            return stateImages;
        }

        public void setStateImages(List<StateImage> stateImages) {
            this.stateImages = stateImages;
        }

        public List<Region> getSearchRegions() {
            return searchRegions;
        }

        public void setSearchRegions(List<Region> searchRegions) {
            this.searchRegions = searchRegions;
        }

        public FindStrategy getStrategy() {
            return strategy;
        }

        public void setStrategy(FindStrategy strategy) {
            this.strategy = strategy;
        }

        public double getSimilarity() {
            return similarity;
        }

        public void setSimilarity(double similarity) {
            this.similarity = similarity;
        }

        public int getMaxMatches() {
            return maxMatches;
        }

        public void setMaxMatches(int maxMatches) {
            this.maxMatches = maxMatches;
        }

        public static class Builder {
            private final LocateRequest request = new LocateRequest();

            public Builder withPatterns(List<Pattern> patterns) {
                request.patterns = patterns;
                return this;
            }

            public Builder withStateImages(List<StateImage> stateImages) {
                request.stateImages = stateImages;
                return this;
            }

            public Builder withSearchRegions(List<Region> searchRegions) {
                request.searchRegions = searchRegions;
                return this;
            }

            public Builder withStrategy(FindStrategy strategy) {
                request.strategy = strategy;
                return this;
            }

            public Builder withSimilarity(double similarity) {
                request.similarity = similarity;
                return this;
            }

            public Builder withMaxMatches(int maxMatches) {
                request.maxMatches = maxMatches;
                return this;
            }

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
     * Result of an element location operation. This lightweight class contains the found element
     * without action-specific semantics.
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

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Pattern getSourcePattern() {
            return sourcePattern;
        }

        public void setSourcePattern(Pattern sourcePattern) {
            this.sourcePattern = sourcePattern;
        }

        public StateImage getSourceStateImage() {
            return sourceStateImage;
        }

        public void setSourceStateImage(StateImage sourceStateImage) {
            this.sourceStateImage = sourceStateImage;
        }

        /** Gets the center location of this element. */
        public io.github.jspinak.brobot.model.element.Location getLocation() {
            return new io.github.jspinak.brobot.model.element.Location(
                    x + width / 2, y + height / 2);
        }

        /** Gets the region bounds of this element. */
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
