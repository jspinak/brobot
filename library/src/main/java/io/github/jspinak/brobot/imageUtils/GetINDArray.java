package io.github.jspinak.brobot.imageUtils;

import org.bytedeco.opencv.opencv_core.MatVector;
import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class GetINDArray {

    public List<INDArray> convertFromMats(MatVector matVector) {
        List<INDArray> arrays = new ArrayList<>();
        NativeImageLoader loader = new NativeImageLoader();
        for (int i = 0; i < matVector.size(); i++) {
            try {
                arrays.add(loader.asMatrix(matVector.get(i)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return arrays;
    }


}
