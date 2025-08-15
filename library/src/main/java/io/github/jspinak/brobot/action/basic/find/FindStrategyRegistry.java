package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
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
 * This component acts as a central registry that maps find strategy types
 * to their corresponding implementation functions. It provides a unified way to access
 * various find strategies such as image matching, text finding, color detection,
 * motion analysis, and more. The registry pattern allows for easy extension with
 * custom find strategies.
 * 
 * @see ActionConfig
 * @see FindImage
 * @see FindText
 * @see FindColor
 * @see FindMotion
 */
@Component
public class FindStrategyRegistry {

    // Strategy enum for find operations
    public enum FindStrategy {
        FIRST, BEST, EACH, ALL,
        HISTOGRAM, COLOR, MOTION, REGIONS_OF_MOTION,
        ALL_WORDS, SIMILAR_IMAGES,
        FIXED_PIXELS, DYNAMIC_PIXELS,
        CUSTOM
    }
    
    private final Map<FindStrategy, BiConsumer<ActionResult, List<ObjectCollection>>>
            findFunction = new HashMap<>();

    public FindStrategyRegistry(FindHistogram findHistogram,
                         FindColor findColor, FindMotion findMotion, FindRegionsOfMotion findRegionsOfMotion,
                         FindImage findImage, FindText findText, FindSimilarImages findSimilarImages,
                         FindFixedPixelMatches findFixedPixelMatches, FindDynamicPixelMatches findDynamicPixelMatches) {
        findFunction.put(FindStrategy.FIRST, findImage::findBest);
        findFunction.put(FindStrategy.BEST, findImage::findBest);
        findFunction.put(FindStrategy.EACH, findImage::findEachStateObject);
        findFunction.put(FindStrategy.ALL, findImage::findAll);
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
     * Registers a custom find function that can be used with FindStrategy.CUSTOM.
     * <p>
     * This method allows users to extend the find functionality by providing their own
     * implementation. The custom function will be invoked when the action config specifies
     * CUSTOM as the find strategy.
     * 
     * @param customFind The custom find function to register. Must accept an {@link ActionResult}
     *                   (which will be populated with matches) and a list of {@link ObjectCollection}s
     *                   to search within. Must not be null.
     */
    public void addCustomFind(BiConsumer<ActionResult, List<ObjectCollection>> customFind) {
        findFunction.put(FindStrategy.CUSTOM, customFind);
    }

    /**
     * Retrieves the appropriate find function based on the action configuration.
     * <p>
     * This method returns the registered function for the specified
     * find strategy based on the ActionConfig type.
     * 
     * @param actionConfig The configuration specifying which find strategy to use
     * @return The find function corresponding to the specified strategy.
     *         May return null if no function is registered for the specified type.
     */
    public BiConsumer<ActionResult, List<ObjectCollection>> get(ActionConfig actionConfig) {
        FindStrategy strategy = getStrategyFromConfig(actionConfig);
        return findFunction.get(strategy);
    }
    
    /**
     * Retrieves the appropriate find function based on the strategy.
     * 
     * @param strategy The find strategy to use
     * @return The find function corresponding to the specified strategy.
     */
    public BiConsumer<ActionResult, List<ObjectCollection>> get(FindStrategy strategy) {
        return findFunction.get(strategy);
    }
    
    /**
     * Determines the find strategy from an ActionConfig.
     * 
     * @param actionConfig The action configuration
     * @return The corresponding FindStrategy
     */
    private FindStrategy getStrategyFromConfig(ActionConfig actionConfig) {
        String className = actionConfig.getClass().getSimpleName();
        
        // Map config classes to strategies
        if (className.contains("PatternFind")) {
            if (actionConfig instanceof PatternFindOptions) {
                PatternFindOptions findOptions = (PatternFindOptions) actionConfig;
                switch (findOptions.getStrategy()) {
                    case FIRST: return FindStrategy.FIRST;
                    case BEST: return FindStrategy.BEST;
                    case EACH: return FindStrategy.EACH;
                    case ALL: return FindStrategy.ALL;
                }
            }
            return FindStrategy.BEST; // default
        }
        if (className.contains("Histogram")) return FindStrategy.HISTOGRAM;
        if (className.contains("Color")) return FindStrategy.COLOR;
        if (className.contains("Motion")) return FindStrategy.MOTION;
        if (className.contains("Text")) return FindStrategy.ALL_WORDS;
        if (className.contains("FixedPixel")) return FindStrategy.FIXED_PIXELS;
        if (className.contains("DynamicPixel")) return FindStrategy.DYNAMIC_PIXELS;
        
        return FindStrategy.BEST; // default
    }
}
