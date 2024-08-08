package io.github.jspinak.brobot.app;

import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.BuildStateStructureFromScreenshots;
import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import org.sikuli.script.ImagePath;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("build-state-structure")
public class MyStartupRunner implements CommandLineRunner {

    private final BuildStateStructureFromScreenshots buildStateStructureFromScreenshots;

    public MyStartupRunner(BuildStateStructureFromScreenshots buildStateStructureFromScreenshots) {
        this.buildStateStructureFromScreenshots = buildStateStructureFromScreenshots;
    }

    @Override
    public void run(String... args) {
        ImagePath.setBundlePath("images");
        StateStructureConfiguration stateStructureConfiguration = new StateStructureConfiguration.Builder()
                .addImagesInScreenshotsFolder("floranext0", "floranext1", "floranext2")
                .setBoundaryImages("topleft", "bottomR2")
                .setMinImageArea(15)
                .build();
        buildStateStructureFromScreenshots.build(stateStructureConfiguration);
    }
}
