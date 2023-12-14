package io.github.jspinak.brobot.testingAUTs;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ActionLogCreator {

    public ActionLog create(//LocalDateTime startTime,
                            //LocalDateTime endTime,
                            Matches matches,
                            ActionOptions actionOptions,
                            ObjectCollection... objectCollections) {
        Set<String> images = new HashSet<>();
        Set<String> ownerStates = new HashSet<>();
        for (ObjectCollection objColl : objectCollections) {
            images.addAll(objColl.getAllImageFilenames());
            ownerStates.addAll(objColl.getAllOwnerStates());
        }
        return new ActionLog(actionOptions.getAction().toString(), matches.isSuccess(), images, ownerStates);
    }
}
