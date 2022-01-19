package actions.methods.basicactions.find;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.stateObject.stateImageObject.StateImageObject;
import com.brobot.multimodule.reports.Report;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends the Image object to either FindImage or FindRIP depending on whether the Image location can vary.
 * Contains methods for Find options FIRST, EACH, ALL, BEST
 */
@Component
public class FindImageOrRIP {

    private final Map<Boolean, FindImageObject> findMethod = new HashMap<>();

    public FindImageOrRIP(FindImage findImage, FindRIP findRIP) {
        findMethod.put(false, findImage);
        findMethod.put(true, findRIP);
    }

    /**
     * For Find.FIRST or Find.ALL, depending on the ActionOptions.
     */
    public Matches find(ActionOptions actionOptions, List<StateImageObject> images) {
        if (Report.minReportingLevel(Report.OutputLevel.LOW)) {
            System.out.format("Find.%s ", actionOptions.getFind());
        }
        Matches matches = new Matches();
        images.forEach(image -> {
            matches.addAll(findMethod.get(image.isFixed()).find(actionOptions, image));
        });
        return matches;
    }

    /**
     * Searches all patterns and returns the Match with the best Score.
     */
    public Matches best(ActionOptions actionOptions, List<StateImageObject> images) {
        Matches matches = new Matches();
        find(actionOptions, images).getBestMatch().ifPresent(matches::add);
        return matches;
    }

    /**
     * Searches each Pattern separately and returns one Match per Pattern if found.
     */
    public Matches each(ActionOptions actionOptions, List<StateImageObject> images) {
        Matches matches = new Matches();
        images.forEach(image ->
                find(actionOptions, Collections.singletonList(image)).getBestMatch().ifPresent(matches::add));
        return matches;
    }
}
