package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Setter
public class ScreenObservations {
    private final MatVisualize matVisualize;

    private List<ScreenObservation> observations = new ArrayList<>();
    private boolean saveScreenshot;

    public ScreenObservations(MatVisualize matVisualize) {
        this.matVisualize = matVisualize;
    }

    public void addScreenObservation(ScreenObservation screenObservation) {
        int id = observations.size();
        screenObservation.setId(id);
        observations.add(screenObservation);
        if (saveScreenshot) matVisualize.writeMatToHistory(screenObservation.getScreenshot(), "Screenshot #"+id);
    }

    public List<ScreenObservation> getAll() {
        return observations;
    }

    public List<StateImage> getAllAsImages() {
        List<StateImage> stateImages = new ArrayList<>();
        for (int i=0; i<observations.size(); i++) {
            Pattern p = observations.get(i).getPattern();
            p.setIndex(i);
            stateImages.add(
                    new StateImage.Builder()
                            .addPattern(p)
                            .setIndex(i)
                            .build());
        }
        return stateImages;
    }

    public Optional<ScreenObservation> get(int id) {
        if (observations.size() > id && id >= 0) return Optional.of(observations.get(id));
        return Optional.empty();
    }

    public List<Integer> observationsWithUncheckedImages() {
        List<Integer> uncheckedObs = new ArrayList<>();
        for (ScreenObservation screenObservation : observations) {
            if (screenObservation.hasUnvisitedImages()) uncheckedObs.add(screenObservation.getId());
        }
        return uncheckedObs;
    }

}
