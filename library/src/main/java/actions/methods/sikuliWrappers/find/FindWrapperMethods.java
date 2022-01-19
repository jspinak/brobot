package actions.methods.sikuliWrappers.find;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Returns the class corresponding to the selected Find option.
 */
@Component
public class FindWrapperMethods {

    private Map<ActionOptions.Find, FindPatternInterface> methods = new HashMap<>();

    public FindWrapperMethods(FindFirstPattern findFirstPattern, FindAllPatterns findAllPatterns) {
        methods.put(ActionOptions.Find.FIRST, findFirstPattern);
        methods.put(ActionOptions.Find.ALL, findAllPatterns);
        methods.put(ActionOptions.Find.EACH, findFirstPattern);
        methods.put(ActionOptions.Find.BEST, findAllPatterns);
    }

    public FindPatternInterface get(ActionOptions.Find findType) {
        return methods.get(findType);
    }
}
