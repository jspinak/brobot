package io.github.jspinak.brobot.app;

import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.BuildStateStructureFromScreenshots;
import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.StateStructureTemplate;
import org.sikuli.script.ImagePath;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyStartupRunner implements CommandLineRunner {

    private final BuildStateStructureFromScreenshots buildStateStructureFromScreenshots;

    public MyStartupRunner(BuildStateStructureFromScreenshots buildStateStructureFromScreenshots) {
        this.buildStateStructureFromScreenshots = buildStateStructureFromScreenshots;
    }

    @Override
    public void run(String... args) {
        ImagePath.setBundlePath("images");
        StateStructureTemplate stateStructureTemplate = new StateStructureTemplate.Builder()
                .addImagesInScreenshotsFolder("floranext0", "floranext1", "floranext2")
                .setBoundaryImages("topleft", "bottomR2")
                .setSaveStateIllustrations(false)
                .setSaveScreenshots(false)
                .setSaveDecisionMats(false)
                .setSaveMatchingImages(false)
                .setSaveScreenWithMotionAndImages(false)
                .build();
        buildStateStructureFromScreenshots.build(stateStructureTemplate);
    }
}
