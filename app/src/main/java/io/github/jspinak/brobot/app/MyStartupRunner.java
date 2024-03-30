package io.github.jspinak.brobot.app;

import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.BuildStateStructure;
import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.StateStructureTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyStartupRunner implements CommandLineRunner {
    private final BuildStateStructure buildStateStructure;

    public MyStartupRunner(BuildStateStructure buildStateStructure) {
        this.buildStateStructure = buildStateStructure;
    }

    @Override
    public void run(String... args) {
        // Code to execute on startup
        StateStructureTemplate stateStructureTemplate = new StateStructureTemplate.Builder()
                .addImagesInScreenshotsFolder("floranext0", "floranext1", "floranext2")
                .setBoundaryImages("bottomR", "topleft")
                .setSaveStateIllustrations(false)
                .setSaveScreenshots(false)
                .setSaveDecisionMats(false)
                .setSaveMatchingImages(false)
                .setSaveScreenWithMotionAndImages(false)
                .build();
        buildStateStructure.execute(stateStructureTemplate);
    }
}
