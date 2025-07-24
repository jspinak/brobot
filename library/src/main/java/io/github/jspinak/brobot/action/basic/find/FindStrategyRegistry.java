package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.basic.find.motion.FindDynamicPixelMatches;
import io.github.jspinak.brobot.action.basic.find.motion.FindFixedPixelMatches;
import io.github.jspinak.brobot.action.basic.find.histogram.FindHistogram;
import io.github.jspinak.brobot.action.basic.find.motion.FindMotion;
import io.github.jspinak.brobot.action.basic.find.motion.FindRegionsOfMotion;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Registry for different find strategies available in the system.
 * <p>
 * This component acts as a central registry that maps {@link ActionOptions.Find} types
 * to their corresponding implementation functions. It provides a unified way to access
 * various find strategies such as image matching, text finding, color detection,
 * motion analysis, and more. The registry pattern allows for easy extension with
 * custom find strategies.
 * 
 * @see ActionOptions.Find
 * @see FindImage
 * @see FindText
 * @see FindColor
 * @see FindMotion
 */
@Component
public class FindStrategyRegistry {

    private final Map<ActionOptions.Find, BiConsumer<ActionResult, List<ObjectCollection>>>
            findFunction = new HashMap<>();

    public FindStrategyRegistry(FindHistogram findHistogram,
                         FindColor findColor, FindMotion findMotion, FindRegionsOfMotion findRegionsOfMotion,
                         FindImage findImage, FindText findText, FindSimilarImages findSimilarImages,
                         FindFixedPixelMatches findFixedPixelMatches, FindDynamicPixelMatches findDynamicPixelMatches) {
        findFunction.put(ActionOptions.Find.FIRST, findImage::findBest);
        findFunction.put(ActionOptions.Find.BEST, findImage::findBest);
        findFunction.put(ActionOptions.Find.EACH, findImage::findEachStateObject);
        findFunction.put(ActionOptions.Find.ALL, findImage::findAll);
        findFunction.put(ActionOptions.Find.HISTOGRAM, findHistogram::find);
        findFunction.put(ActionOptions.Find.COLOR, findColor::find);
        findFunction.put(ActionOptions.Find.MOTION, findMotion::find);
        findFunction.put(ActionOptions.Find.REGIONS_OF_MOTION, findRegionsOfMotion::find);
        findFunction.put(ActionOptions.Find.ALL_WORDS, findText::findAllWordMatches);
        findFunction.put(ActionOptions.Find.SIMILAR_IMAGES, findSimilarImages::find);
        findFunction.put(ActionOptions.Find.FIXED_PIXELS, findFixedPixelMatches::find);
        findFunction.put(ActionOptions.Find.DYNAMIC_PIXELS, findDynamicPixelMatches::find);
    }

    /**
     * Registers a custom find function that can be used with {@link ActionOptions.Find#CUSTOM}.
     * <p>
     * This method allows users to extend the find functionality by providing their own
     * implementation. The custom function will be invoked when the action options specify
     * {@link ActionOptions.Find#CUSTOM} as the find strategy.
     * 
     * @param customFind The custom find function to register. Must accept an {@link ActionResult}
     *                   (which will be populated with matches) and a list of {@link ObjectCollection}s
     *                   to search within. Must not be null.
     */
    public void addCustomFind(BiConsumer<ActionResult, List<ObjectCollection>> customFind) {
        findFunction.put(ActionOptions.Find.CUSTOM, customFind);
    }

    /**
     * Retrieves the appropriate find function based on the action options.
     * <p>
     * This method first checks if a temporary find function is specified in the
     * action options. If not, it returns the registered function for the specified
     * find type. The temporary find function allows for one-time overrides of the
     * standard find behavior.
     * 
     * @param actionOptions The options specifying which find strategy to use
     * @return The find function corresponding to the specified strategy, or the temporary
     *         find function if one is set. May return null if no function is registered
     *         for the specified find type.
     */
    public BiConsumer<ActionResult, List<ObjectCollection>> get(ActionOptions actionOptions) {
        if (actionOptions.getTempFind() != null) return actionOptions.getTempFind();
        return findFunction.get(actionOptions.getFind());
    }
}
