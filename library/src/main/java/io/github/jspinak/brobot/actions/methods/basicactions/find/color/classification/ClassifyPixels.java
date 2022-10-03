package io.github.jspinak.brobot.classification;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetAllProfiles;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetKMeansProfiles;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.imageUtils.GetImage;
import io.github.jspinak.brobot.manageStates.StateMemory;
import io.github.jspinak.brobot.services.StateService;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

/**
 * DynamicImages classify as probabilities, standard Images have 100% pixel probability when found.
 * The sparse matrix holds probabilities for each StateImageObject.
 * The screen Mat should be in hsv format.
 */
@Component
public class ClassifyPixels {

    private StateMemory stateMemory;
    private StateService stateService;
    private GetImage getImage;
    private SetAllProfiles setAllProfiles;
    private SetKMeansProfiles setKMeansProfiles;

    public ClassifyPixels(StateMemory stateMemory, StateService stateService, GetImage getImage,
                          SetAllProfiles setAllProfiles, SetKMeansProfiles setKMeansProfiles) {
        this.stateMemory = stateMemory;
        this.stateService = stateService;
        this.getImage = getImage;
        this.setAllProfiles = setAllProfiles;
        this.setKMeansProfiles = setKMeansProfiles;
    }

    public List<SparseMatrix> classify(int kmeans, List<StateImageObject> targetImages,
                                       List<StateImageObject> additionalImagesForClassification,
                                       List<Mat> scenesToClassify) {
        List<StateImageObject> allImages = new ArrayList<>();
        allImages.addAll(targetImages);
        allImages.addAll(additionalImagesForClassification);
        List<SparseMatrix> sparseMatrices = new ArrayList<>();
        setKMeansProfiles.addKMeansIfNeeded(allImages, kmeans);
        scenesToClassify.forEach(scene -> sparseMatrices.add(new SparseMatrix(scene, allImages, kmeans)));
        return sparseMatrices;
    }


}
