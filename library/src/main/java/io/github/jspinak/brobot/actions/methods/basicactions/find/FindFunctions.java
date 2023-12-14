package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.classification.FindColor;
import io.github.jspinak.brobot.actions.methods.basicactions.find.histogram.FindHistogram;
import io.github.jspinak.brobot.actions.methods.basicactions.find.motion.FindMotion;
import io.github.jspinak.brobot.actions.methods.basicactions.find.motion.FindRegionsOfMotion;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Retrieve a Find function with its Find type.
 */
@Component
public class FindFunctions {

    private Map<ActionOptions.Find, BiFunction<ActionOptions, List<ObjectCollection>, Matches>>
            findFunction = new HashMap<>();

    public FindFunctions(FindImageOrRIP findImageOrRIP, FindHistogram findHistogram,
                         FindColor findColor, FindMotion findMotion, FindRegionsOfMotion findRegionsOfMotion) {
        findFunction.put(ActionOptions.Find.FIRST, findImageOrRIP::find);
        findFunction.put(ActionOptions.Find.BEST, findImageOrRIP::best);
        findFunction.put(ActionOptions.Find.EACH, findImageOrRIP::each);
        findFunction.put(ActionOptions.Find.ALL, findImageOrRIP::find);
        findFunction.put(ActionOptions.Find.HISTOGRAM, findHistogram::find);
        findFunction.put(ActionOptions.Find.COLOR, findColor::find);
        findFunction.put(ActionOptions.Find.MOTION, findMotion::find);
        findFunction.put(ActionOptions.Find.REGIONS_OF_MOTION, findRegionsOfMotion::find);
    }

    public void addCustomFind(BiFunction<ActionOptions, List<ObjectCollection>, Matches> customFind) {
        findFunction.put(ActionOptions.Find.CUSTOM, customFind);
    }

    public BiFunction<ActionOptions, List<ObjectCollection>, Matches> get(ActionOptions actionOptions) {
        if (actionOptions.getTempFind() != null) return actionOptions.getTempFind();
        return findFunction.get(actionOptions.getFind());
    }
}
