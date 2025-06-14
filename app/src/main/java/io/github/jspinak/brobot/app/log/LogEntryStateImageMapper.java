package io.github.jspinak.brobot.app.log;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.report.log.model.StateImageLogData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class LogEntryStateImageMapper {

    public StateImageLogData toLog(StateImage stateImage, Matches matches) {
        StateImageLogData stateImageLogData = new StateImageLogData();
        stateImageLogData.setStateImageId(stateImage.getId());
        // Set found status
        List<Match> matchList = matches.getMatchList();
        if (!matchList.isEmpty() && Objects.equals(
                matchList.get(0).getStateObjectData().getStateObjectId(),
                stateImage.getIdAsString())) {
            stateImageLogData.setFound(true);
        }
        return stateImageLogData;
    }

    private String getName(StateImage stateImage) {
        if (!stateImage.getName().isEmpty()) return stateImage.getName();
        for (Pattern pattern : stateImage.getPatterns()) {
            if (!pattern.getName().isEmpty()) return pattern.getName();
        }
        return "";
    }
}