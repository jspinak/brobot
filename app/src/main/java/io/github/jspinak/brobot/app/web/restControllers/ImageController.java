package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.services.ImageService;
import io.github.jspinak.brobot.app.web.responseMappers.ImageResponseMapper;
import io.github.jspinak.brobot.app.web.responses.ImageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;
    private final ImageResponseMapper imageResponseMapper;

    public ImageController(ImageService imageService, ImageResponseMapper imageResponseMapper) {
        this.imageService = imageService;
        this.imageResponseMapper = imageResponseMapper;
    }

    @GetMapping("/all")
    public List<ImageResponse> getAllImages() {
        return imageService.getAllImageEntities().stream()
                .map(imageResponseMapper::map)
                .collect(Collectors.toList());
    }

    @GetMapping("/name/{name}")
    public List<ImageEntity> getImagesByName(@PathVariable String name) {
        return imageService.getImageEntities(name);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageResponse> getImageById(@PathVariable Long id) {
        return imageService.getImageEntity(id)
                .map(imageResponseMapper::map)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/")
    public String greeting() {
        System.out.println("console output: hello world should be in localhost:8080/api/images/");
        return "Hello, World";
    }
}
