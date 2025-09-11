package io.github.jspinak.brobot.core.location;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.core.services.PatternMatcher;
import io.github.jspinak.brobot.core.services.ScreenCaptureService;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

/**
 * Visual implementation of the ElementLocator interface.
 *
 * <p>This implementation uses visual pattern matching to locate GUI elements on screen. It
 * orchestrates the ScreenCaptureService and PatternMatcher to find elements without any dependency
 * on Find or Action classes.
 *
 * <p>This class is the key to breaking circular dependencies. Actions that previously depended on
 * Find now depend on ElementLocator instead.
 *
 * <p>Key characteristics:
 *
 * <ul>
 *   <li>Depends ONLY on core services (Layer 1)
 *   <li>NO dependencies on Find, Actions, or any higher layers
 *   <li>Thread-safe through stateless operations
 *   <li>Supports all find strategies (ALL, BEST, FIRST, EACH)
 * </ul>
 *
 * @since 2.0.0
 */
@Component
public class VisualElementLocator implements ElementLocator {

    private final PatternMatcher patternMatcher;
    private final ScreenCaptureService screenCapture;

    public VisualElementLocator(PatternMatcher patternMatcher, ScreenCaptureService screenCapture) {
        this.patternMatcher = patternMatcher;
        this.screenCapture = screenCapture;
    }

    @Override
    public List<Element> locate(LocateRequest request) {
        // Validate request
        if (request == null) {
            return new ArrayList<>();
        }

        // Capture the screen
        BufferedImage screen = screenCapture.captureScreen();
        if (screen == null) {
            ConsoleReporter.println("[VisualElementLocator] Failed to capture screen");
            return new ArrayList<>();
        }

        // Get patterns to search for
        List<Pattern> patterns = extractPatterns(request);
        if (patterns.isEmpty()) {
            ConsoleReporter.println("[VisualElementLocator] No patterns to search for");
            return new ArrayList<>();
        }

        // Find all matches for all patterns
        List<Element> allElements = new ArrayList<>();
        for (Pattern pattern : patterns) {
            List<Element> patternElements = findPatternElements(screen, pattern, request);
            allElements.addAll(patternElements);
        }

        // Apply strategy
        return applyStrategy(allElements, request);
    }

    @Override
    public List<Element> locateInRegion(LocateRequest request, Region region) {
        if (request == null || region == null) {
            return new ArrayList<>();
        }

        // Capture the specific region
        BufferedImage regionImage = screenCapture.captureRegion(region);
        if (regionImage == null) {
            ConsoleReporter.println("[VisualElementLocator] Failed to capture region");
            return new ArrayList<>();
        }

        // Get patterns to search for
        List<Pattern> patterns = extractPatterns(request);

        // Find all matches in the region
        List<Element> allElements = new ArrayList<>();
        for (Pattern pattern : patterns) {
            List<Element> patternElements = findPatternElements(regionImage, pattern, request);

            // Adjust coordinates to screen space
            for (Element element : patternElements) {
                element.setX(element.getX() + region.x());
                element.setY(element.getY() + region.y());
            }

            allElements.addAll(patternElements);
        }

        return applyStrategy(allElements, request);
    }

    @Override
    public boolean verifyElement(Element element) {
        if (element == null) {
            return false;
        }

        // Capture the region where the element was
        Region elementRegion = element.getRegion();
        BufferedImage regionImage = screenCapture.captureRegion(elementRegion);
        if (regionImage == null) {
            return false;
        }

        // Try to find the same pattern in that region
        Pattern pattern = element.getSourcePattern();
        if (pattern == null) {
            return false;
        }

        PatternMatcher.MatchOptions options =
                new PatternMatcher.MatchOptions.Builder()
                        .withSimilarity(element.getConfidence() * 0.9) // Slightly lower threshold
                        .withFindAll(false)
                        .build();

        List<PatternMatcher.MatchResult> results =
                patternMatcher.findPatterns(regionImage, pattern, options);
        return !results.isEmpty();
    }

    @Override
    public List<Element> waitForElement(LocateRequest request, double timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = (long) (timeoutSeconds * 1000);

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            List<Element> elements = locate(request);
            if (!elements.isEmpty()) {
                return elements;
            }

            // Wait a bit before next attempt
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();
    }

    @Override
    public boolean waitForVanish(Element element, double timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = (long) (timeoutSeconds * 1000);

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (!verifyElement(element)) {
                return true;
            }

            // Wait a bit before next check
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }

    @Override
    public String getImplementationName() {
        return "Visual";
    }

    private List<Pattern> extractPatterns(LocateRequest request) {
        List<Pattern> patterns = new ArrayList<>();

        // Direct patterns
        if (request.getPatterns() != null) {
            patterns.addAll(request.getPatterns());
        }

        // Patterns from StateImages
        if (request.getStateImages() != null) {
            for (StateImage stateImage : request.getStateImages()) {
                if (stateImage.getPatterns() != null) {
                    patterns.addAll(stateImage.getPatterns());
                }
            }
        }

        return patterns;
    }

    private List<Element> findPatternElements(
            BufferedImage screen, Pattern pattern, LocateRequest request) {
        // Configure pattern matching
        PatternMatcher.MatchOptions options =
                new PatternMatcher.MatchOptions.Builder()
                        .withSimilarity(request.getSimilarity())
                        .withFindAll(request.getStrategy() != FindStrategy.FIRST)
                        .withMaxMatches(request.getMaxMatches())
                        .build();

        // Find matches
        List<PatternMatcher.MatchResult> results =
                patternMatcher.findPatterns(screen, pattern, options);

        // Convert to Elements
        List<Element> elements = new ArrayList<>();
        for (PatternMatcher.MatchResult result : results) {
            Element element = new Element();
            element.setX(result.getX());
            element.setY(result.getY());
            element.setWidth(result.getWidth());
            element.setHeight(result.getHeight());
            element.setConfidence(result.getConfidence());
            element.setName(pattern.getName());
            element.setSourcePattern(pattern);

            // Find source StateImage if available
            if (request.getStateImages() != null) {
                for (StateImage stateImage : request.getStateImages()) {
                    if (stateImage.getPatterns().contains(pattern)) {
                        element.setSourceStateImage(stateImage);
                        break;
                    }
                }
            }

            elements.add(element);
        }

        return elements;
    }

    private List<Element> applyStrategy(List<Element> allElements, LocateRequest request) {
        if (allElements.isEmpty()) {
            return allElements;
        }

        switch (request.getStrategy()) {
            case FIRST:
                // Return just the first element
                return allElements.subList(0, 1);

            case BEST:
                // Return the element with highest confidence
                Element best =
                        allElements.stream()
                                .max(Comparator.comparing(Element::getConfidence))
                                .orElse(allElements.get(0));
                List<Element> bestList = new ArrayList<>();
                bestList.add(best);
                return bestList;

            case EACH:
                // Return one element per pattern
                List<Element> eachList = new ArrayList<>();
                List<Pattern> seenPatterns = new ArrayList<>();

                for (Element element : allElements) {
                    if (!seenPatterns.contains(element.getSourcePattern())) {
                        eachList.add(element);
                        seenPatterns.add(element.getSourcePattern());
                    }
                }
                return eachList;

            case ALL:
            default:
                // Return all elements
                return allElements;
        }
    }
}
