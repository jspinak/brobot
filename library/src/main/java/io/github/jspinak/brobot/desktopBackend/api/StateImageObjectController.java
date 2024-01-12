package io.github.jspinak.brobot.desktopBackend.api;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stateimageobjects")
public class StateImageObjectController {

    private StateImageObjectService stateImageObjectService;

    public StateImageObjectController(StateImageObjectService stateImageObjectService) {
        this.stateImageObjectService = stateImageObjectService;
    }

    public @ResponseBody List<StateImageObject> getAllStateImageObjects() {
        return stateImageObjectService.getAllStateImageObjects();
    }

    public @ResponseBody StateImageObject getStateImageObject(String name) {
        return stateImageObjectService.getStateImageObject(name);
    }
}
