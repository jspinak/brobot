package io.github.jspinak.brobot.app.log;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.log.entities.StateImageLog;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class LogEntryStateImageMapper {

    public StateImageLog toLog(StateImage stateImage, Matches matches) {
        StateImageLog stateImageLog = new StateImageLog();
        stateImageLog.setName(getName(stateImage));
        stateImageLog.setStateOwnerName(stateImage.getOwnerStateName());

        // Encode and store all pattern images
        List<String> base64Images = new ArrayList<>();
        for (Pattern pattern : stateImage.getPatterns()) {
            if (pattern.getBImage() != null) {
                String base64Image = BufferedImageOps.bufferedImageToStringBase64(pattern.getBImage());
                if (base64Image != null) {
                    base64Images.add(base64Image);
                }
            }
        }
        stateImageLog.setImagesBase64(base64Images);

        // Set found status
        List<Match> matchList = matches.getMatchList();
        if (!matchList.isEmpty() && Objects.equals(
                matchList.get(0).getStateObjectData().getStateObjectId(),
                stateImage.getId())) {
            stateImageLog.setFound(true);

            // Additionally add any matched images to the base64 list
            matchList.forEach(match -> {
                if (match.getImage() != null && match.getImage().getBufferedImage() != null) {
                    String base64Image = BufferedImageOps.bufferedImageToStringBase64(
                            match.getImage().getBufferedImage());
                    if (base64Image != null) {
                        base64Images.add(base64Image);
                    }
                }
            });
        }

        return stateImageLog;
    }

    private String getName(StateImage stateImage) {
        if (!stateImage.getName().isEmpty()) return stateImage.getName();
        for (Pattern pattern : stateImage.getPatterns()) {
            if (!pattern.getName().isEmpty()) return pattern.getName();
        }
        return "";
    }
}