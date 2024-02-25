package com.brobot.app.restControllers;

import io.github.jspinak.brobot.database.services.ImageService;
import io.github.jspinak.brobot.datatypes.primitives.image.ImageResponse;
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

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * @return a list of images for display in a browser
     */
    @GetMapping("/all") // Maps to GET /api/images/all
    public List<ImageResponse> getAllImages() {
        return imageService.getAllImages().stream()
                .map(ImageResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{name}") // Maps to GET /api/images/{name}
    public List<ImageResponse> getImages(@PathVariable String name) {
        return imageService.getImages(name).stream()
                .map(ImageResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/") // Maps to GET /api/images/
    public String greeting() {
        System.out.println("console output: hello world should be in localhost:8080/api/images/");
        return "Hello, World";
    }
}
