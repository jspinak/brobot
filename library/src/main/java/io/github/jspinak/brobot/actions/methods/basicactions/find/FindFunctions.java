package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.classification.FindColor;
import io.github.jspinak.brobot.actions.methods.basicactions.find.compareImages.FindSimilarImages;
import io.github.jspinak.brobot.actions.methods.basicactions.find.fixedAndDynamicPixels.FindDynamicPixelMatches;
import io.github.jspinak.brobot.actions.methods.basicactions.find.fixedAndDynamicPixels.FindFixedPixelMatches;
import io.github.jspinak.brobot.actions.methods.basicactions.find.histogram.FindHistogram;
import io.github.jspinak.brobot.actions.methods.basicactions.find.motion.FindMotion;
import io.github.jspinak.brobot.actions.methods.basicactions.find.motion.FindRegionsOfMotion;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Retrieve a Find function with its Find type.
 */
@Component
public class FindFunctions {

    private final Map<ActionOptions.Find, BiConsumer<Matches, List<ObjectCollection>>>
            findFunction = new HashMap<>();

    public FindFunctions(FindHistogram findHistogram,
                         FindColor findColor, FindMotion findMotion, FindRegionsOfMotion findRegionsOfMotion,
                         FindWords findWords, FindImages findImages, FindSimilarImages findSimilarImages,
                         FindFixedPixelMatches findFixedPixelMatches, FindDynamicPixelMatches findDynamicPixelMatches) {
        findFunction.put(ActionOptions.Find.FIRST, findImages::findBest);
        findFunction.put(ActionOptions.Find.BEST, findImages::findBest);
        findFunction.put(ActionOptions.Find.EACH, findImages::findEachStateObject);
        findFunction.put(ActionOptions.Find.ALL, findImages::findAll);
        findFunction.put(ActionOptions.Find.HISTOGRAM, findHistogram::find);
        findFunction.put(ActionOptions.Find.COLOR, findColor::find);
        findFunction.put(ActionOptions.Find.MOTION, findMotion::find);
        findFunction.put(ActionOptions.Find.REGIONS_OF_MOTION, findRegionsOfMotion::find);
        findFunction.put(ActionOptions.Find.ALL_WORDS, findWords::findAllWordMatches);
        findFunction.put(ActionOptions.Find.SIMILAR_IMAGES, findSimilarImages::find);
        findFunction.put(ActionOptions.Find.FIXED_PIXELS, findFixedPixelMatches::find);
        findFunction.put(ActionOptions.Find.DYNAMIC_PIXELS, findDynamicPixelMatches::find);
    }

    public void addCustomFind(BiConsumer<Matches, List<ObjectCollection>> customFind) {
        findFunction.put(ActionOptions.Find.CUSTOM, customFind);
    }

    public BiConsumer<Matches, List<ObjectCollection>> get(ActionOptions actionOptions) {
        if (actionOptions.getTempFind() != null) return actionOptions.getTempFind();
        return findFunction.get(actionOptions.getFind());
    }
}
