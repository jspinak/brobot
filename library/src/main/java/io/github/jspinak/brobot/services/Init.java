package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetAllProfiles;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetKMeansProfiles;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

@Component
public class Init {

    private final AllStatesInProjectService allStatesInProjectService;
    private final SetAllProfiles setAllProfiles;
    private final SetKMeansProfiles setKMeansProfiles;

    private int lastImageIndex = 1; // 0 should correspond to "no class" since matrices are typically initialized with 0s

    public Init(AllStatesInProjectService allStatesInProjectService, SetAllProfiles setAllProfiles, SetKMeansProfiles setKMeansProfiles) {
        this.allStatesInProjectService = allStatesInProjectService;
        this.setAllProfiles = setAllProfiles;
        this.setKMeansProfiles = setKMeansProfiles;
    }

    /**
     * This method is called from the client app after all beans have been initialized.
     * @param path Path to the directory containing the images.
     */
    public void setBundlePathAndPreProcessImages(String path) {
        org.sikuli.script.ImagePath.setBundlePath(path);
        Report.println("Saving indices for images in states: ");
        allStatesInProjectService.getAllStates().forEach(this::preProcessImages);
        Report.println();
    }

    private void preProcessImages(State state) {
        if (!state.getStateImages().isEmpty()) Report.print(state.getName() + ": ");
        for (StateImage stateImage : state.getStateImages()) {
            Report.print("[" + lastImageIndex + "," + stateImage.getName() + "] ");
            stateImage.setIndex(lastImageIndex);
            setAllProfiles.setMatsAndColorProfiles(stateImage);
            lastImageIndex++;
            if (BrobotSettings.initProfilesForDynamicImages && stateImage.isDynamic() ||
                    (BrobotSettings.initProfilesForStaticfImages && !stateImage.isDynamic())) {
                setKMeansProfiles.setProfiles(stateImage);
            }
        }
        if (!state.getStateImages().isEmpty()) Report.println();
    }

    public void add(String path) {
        org.sikuli.script.ImagePath.add(path);
    }
}
