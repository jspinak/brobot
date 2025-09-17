package io.github.jspinak.brobot.action.basic.find;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.histogram.FindHistogram;
import io.github.jspinak.brobot.action.basic.find.motion.FindDynamicPixelMatches;
import io.github.jspinak.brobot.action.basic.find.motion.FindFixedPixelMatches;
import io.github.jspinak.brobot.action.basic.find.motion.FindMotion;
import io.github.jspinak.brobot.action.basic.find.motion.FindRegionsOfMotion;

/**
 * Registry for different find strategies available in the system.
 *
 * <p>This component acts as a central registry that maps {@link FindStrategy} types to their
 * corresponding implementation functions. It provides a unified way to access various find
 * strategies such as image matching, text finding, color detection, motion analysis, and more. The
 * registry pattern allows for easy extension with custom find strategies.
 *
 * <p>This is version 2 of the registry, updated to work with the new ActionConfig hierarchy and
 * FindStrategy enum instead of ActionConfig.Find.
 *
 * @see FindStrategy
 * @see BaseFindOptions
 * @see ImageFinder
 * @see FindText
 * @see FindColor
 * @see FindMotion
 * @since 2.0
 */
@Component
public class ModernFindStrategyRegistry {

    private final Map<FindStrategy, BiConsumer<ActionResult, List<ObjectCollection>>> findFunction =
            new HashMap<>();

    // Storage for custom find function
    private BiConsumer<ActionResult, List<ObjectCollection>> customFind;

    public ModernFindStrategyRegistry(
            FindHistogram findHistogram,
            FindColor findColor,
            FindMotion findMotion,
            FindRegionsOfMotion findRegionsOfMotion,
            ImageFinder findImageV2,
            FindText findText,
            FindSimilarImages findSimilarImages,
            FindFixedPixelMatches findFixedPixelMatches,
            FindDynamicPixelMatches findDynamicPixelMatches) {
        findFunction.put(FindStrategy.FIRST, findImageV2::findBest);
        findFunction.put(FindStrategy.BEST, findImageV2::findBest);
        findFunction.put(FindStrategy.EACH, findImageV2::findEachStateObject);
        findFunction.put(FindStrategy.ALL, findImageV2::findAll);
        findFunction.put(FindStrategy.HISTOGRAM, findHistogram::find);
        findFunction.put(FindStrategy.COLOR, findColor::find);
        findFunction.put(FindStrategy.MOTION, findMotion::find);
        findFunction.put(FindStrategy.REGIONS_OF_MOTION, findRegionsOfMotion::find);
        findFunction.put(FindStrategy.ALL_WORDS, findText::findAllWordMatches);
        findFunction.put(FindStrategy.SIMILAR_IMAGES, findSimilarImages::find);
        findFunction.put(FindStrategy.FIXED_PIXELS, findFixedPixelMatches::find);
        findFunction.put(FindStrategy.DYNAMIC_PIXELS, findDynamicPixelMatches::find);
    }

    /**
     * Registers a custom find function that can be used with {@link FindStrategy#CUSTOM}.
     *
     * <p>This method allows users to extend the find functionality by providing their own
     * implementation. The custom function will be invoked when the find options specify {@link
     * FindStrategy#CUSTOM} as the find strategy.
     *
     * @param customFind The custom find function to register. Must accept an {@link ActionResult}
     *     (which will be populated with matches) and a list of {@link ObjectCollection}s to search
     *     within. Must not be null.
     */
    public void setCustomFind(BiConsumer<ActionResult, List<ObjectCollection>> customFind) {
        this.customFind = customFind;
        findFunction.put(FindStrategy.CUSTOM, customFind);
    }

    /**
     * Retrieves the appropriate find function based on the find options.
     *
     * <p>This method extracts the find strategy from the provided options and returns the
     * corresponding registered function. For CUSTOM strategies, it returns the custom function if
     * one has been registered.
     *
     * @param findOptions The options specifying which find strategy to use
     * @return The find function corresponding to the specified strategy, or null if no function is
     *     registered for the specified find type.
     */
    public BiConsumer<ActionResult, List<ObjectCollection>> get(BaseFindOptions findOptions) {
        if (findOptions == null) {
            return null;
        }

        FindStrategy strategy = findOptions.getFindStrategy();

        // Special handling for CUSTOM strategy
        if (strategy == FindStrategy.CUSTOM && customFind != null) {
            return customFind;
        }

        return findFunction.get(strategy);
    }

    /**
     * Retrieves the appropriate find function based on the find strategy.
     *
     * <p>This is a convenience method that directly accepts a FindStrategy enum value.
     *
     * @param strategy The find strategy to use
     * @return The find function corresponding to the specified strategy, or null if no function is
     *     registered for the specified strategy.
     */
    public BiConsumer<ActionResult, List<ObjectCollection>> get(FindStrategy strategy) {
        if (strategy == FindStrategy.CUSTOM && customFind != null) {
            return customFind;
        }
        return findFunction.get(strategy);
    }

    /**
     * Executes the find strategy with the given parameters.
     *
     * <p>This method looks up and executes the appropriate find function based on the provided
     * strategy. If no function is registered for the strategy, this method returns without
     * performing any action.
     *
     * @param strategy The find strategy to execute
     * @param matches The action result to populate with matches
     * @param objectCollections The collections of objects to search for
     */
    public void runFindStrategy(
            FindStrategy strategy, ActionResult matches, ObjectCollection... objectCollections) {
        BiConsumer<ActionResult, List<ObjectCollection>> function = get(strategy);
        if (function != null) {
            function.accept(matches, List.of(objectCollections));
        }
    }
}
