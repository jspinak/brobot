package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.services.ImageService;
import io.github.jspinak.brobot.app.web.responseMappers.ImageResponseMapper;
import io.github.jspinak.brobot.app.web.responses.ImageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/images") // Base path for all endpoints in this controller
public class ImageController {

    private final ImageService imageService;
    private final ImageResponseMapper imageResponseMapper;

    public ImageController(ImageService imageService, ImageResponseMapper imageResponseMapper) {
        this.imageService = imageService;
        this.imageResponseMapper = imageResponseMapper;
    }

    /**
     * @return a list of images for display in a browser
     */
    @GetMapping("/all") // Maps to GET /api/images/all
    public List<ImageResponse> getAllImages() {
        return imageService.getAllImageEntities().stream()
                .map(imageResponseMapper::map)
                .collect(Collectors.toList());
    }

    @GetMapping("/{name}") // Maps to GET /api/images/{name}
    public List<ImageEntity> getImages(@PathVariable String name) {
        return imageService.getImageEntities(name);
    }

    @GetMapping("/") // Maps to GET /api/images/
    public String greeting() {
        System.out.println("console output: hello world should be in localhost:8080/api/images/");
        return "Hello, World";
    }
}
