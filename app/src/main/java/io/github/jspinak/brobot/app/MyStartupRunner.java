package io.github.jspinak.brobot.app;

import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.BuildStateStructureFromScreenshots;
import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.app.services.SceneService;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.imageUtils.SceneCreator;
import org.sikuli.script.ImagePath;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("build-state-structure")
public class MyStartupRunner implements CommandLineRunner {

    private final BuildStateStructureFromScreenshots buildStateStructureFromScreenshots;
    private final SceneService sceneService;
    private final SceneCreator sceneCreator;

    public MyStartupRunner(BuildStateStructureFromScreenshots buildStateStructureFromScreenshots,
                           SceneService sceneService, SceneCreator sceneCreator) {
        this.buildStateStructureFromScreenshots = buildStateStructureFromScreenshots;
        this.sceneService = sceneService;
        this.sceneCreator = sceneCreator;
    }

    @Override
    public void run(String... args) {
        ImagePath.setBundlePath("images");
        List<Scene> scenes = sceneCreator.createScenesFromScreenshots();
        sceneService.saveScenes(scenes);
        StateStructureConfiguration stateStructureConfiguration = new StateStructureConfiguration.Builder()
                .addScenes(scenes)
                .setBoundaryImages("topleft", "bottomR2")
                .setMinImageArea(15)
                .build();
        buildStateStructureFromScreenshots.build(stateStructureConfiguration);
    }
}
