package io.github.jspinak.brobot.app.services.entityServices;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.database.repositories.ImageRepo;
import io.github.jspinak.brobot.app.database.repositories.StateRepo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ImageEntityService {

    private final ImageRepo imageRepo;

    public ImageEntityService(ImageRepo imageRepo) {
        this.imageRepo = imageRepo;
    }

    public List<ImageEntity> getAllImages() {
        return imageRepo.findAll();
        //return imageRepo.findByProjectId()
    }

    public List<ImageEntity> getImage(String name) {
        return imageRepo.findByName(name);
    }
}
