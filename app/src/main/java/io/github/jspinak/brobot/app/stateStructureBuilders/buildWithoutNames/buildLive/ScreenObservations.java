package io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildLive;

import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.GetScreenObservationFromScreenshot;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.ScreenObservation;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Setter
public class ScreenObservations {
    private final GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;

    public ScreenObservations(GetScreenObservationFromScreenshot getScreenObservationFromScreenshot) {
        this.getScreenObservationFromScreenshot = getScreenObservationFromScreenshot;
    }

    public ScreenObservation addScreenObservation(Scene screenshot, List<ScreenObservation> observations,
                                                  StateStructureConfiguration config) {
        int id = observations.size();
        ScreenObservation screenObservation = getScreenObservationFromScreenshot.getNewScreenObservation(screenshot, config);
        screenObservation.setId(id);
        observations.add(screenObservation);
        return screenObservation;
    }

    public List<StateImage> getAllAsImages(List<ScreenObservation> observations) {
        List<StateImage> stateImages = new ArrayList<>();
        for (int i=0; i<observations.size(); i++) {
            Pattern p = observations.get(i).getScene().getPattern();
            p.setIndex(i);
            stateImages.add(
                    new StateImage.Builder()
                            .addPattern(p)
                            .setIndex(i)
                            .build());
        }
        return stateImages;
    }

    public Optional<ScreenObservation> get(int id, List<ScreenObservation> observations) {
        if (observations.size() > id && id >= 0) return Optional.of(observations.get(id));
        return Optional.empty();
    }

    public List<Integer> observationsWithUncheckedImages(List<ScreenObservation> observations) {
        List<Integer> uncheckedObs = new ArrayList<>();
        for (ScreenObservation screenObservation : observations) {
            if (screenObservation.hasUnvisitedImages()) uncheckedObs.add(screenObservation.getId());
        }
        return uncheckedObs;
    }

}
