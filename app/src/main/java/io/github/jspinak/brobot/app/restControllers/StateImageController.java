package io.github.jspinak.brobot.app.restControllers;

import io.github.jspinak.brobot.app.services.StateImageService;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stateimages")
public class StateImageController {

    private final StateImageService stateImageService;

    public StateImageController(StateImageService stateImageService) {
        this.stateImageService = stateImageService;
    }

    public @ResponseBody List<StateImage> getAllStateImages() {
        return stateImageService.getAllStateImages();
    }

    public @ResponseBody StateImage getStateImage(String name) {
        return stateImageService.getStateImage(name);
    }

    //public @ResponseBody void removeStateImage(StateImageResponse stateImageResponse) {
    //    stateImageService.removeStateImage(stateImageResponse);
    //}
}
