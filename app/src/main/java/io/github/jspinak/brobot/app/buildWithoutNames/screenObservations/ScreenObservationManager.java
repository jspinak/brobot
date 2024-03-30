package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Updates information related to screen observations and provides functionality to help with the process
 * of observing and recording screens and images.
 */
@Component
@Getter
@Setter
public class ScreenObservationManager {

    private int currentScreenId = -1; // initially, there are no saved screens
    private ScreenObservation currentScreenObservation;
    private int nextUnassignedScreenId = 0;
    private int screenIndex = 0; // this is the screenshot index to retrieve when not running live
    private double maxSimilarityForUniqueImage = .95; // images matching below this similarity as considered unique


}
