package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.ImageEntityMapper;
import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.database.repositories.ImageRepo;
import io.github.jspinak.brobot.app.exceptions.EntityNotFoundException;
import io.github.jspinak.brobot.app.web.requests.ImageRequest;
import io.github.jspinak.brobot.app.web.responseMappers.ImageResponseMapper;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final ImageRepo imageRepo;
    private final ImageEntityMapper imageEntityMapper;
    private final ImageResponseMapper imageResponseMapper;

    public ImageService(ImageRepo imageRepo, ImageEntityMapper imageEntityMapper,
                        ImageResponseMapper imageResponseMapper) {
        this.imageRepo = imageRepo;
        this.imageEntityMapper = imageEntityMapper;
        this.imageResponseMapper = imageResponseMapper;
    }

    @Transactional(readOnly = true)
    public List<Image> getImages(String name) {
        List<ImageEntity> imageEntities = imageRepo.findByName(name);
        return imageEntities.stream()
                .map(imageEntityMapper::map)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Image> getAllImages() {
        List<ImageEntity> imageEntities = imageRepo.findAll();
        return imageEntities.stream()
                .map(imageEntityMapper::map)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ImageEntity> getAllImageEntities() {
        return imageRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<ImageEntity> getImageEntities(String name) {
        return imageRepo.findByName(name);
    }

    @Transactional
    public void saveImages(Image... images) {
        saveImages(List.of(images));
    }

    @Transactional
    public void saveImages(List<Image> images) {
        images.forEach(image -> imageRepo.save(imageEntityMapper.map(image)));
    }

    @Transactional
    public ImageEntity createImage(ImageRequest imageRequest) {
        ImageEntity imageEntity = imageResponseMapper.fromRequest(imageRequest);
        return imageRepo.save(imageEntity);
    }

    @Transactional
    public ImageEntity saveImage(Image image) {
        ImageEntity imageEntity = imageEntityMapper.map(image);
        return imageRepo.save(imageEntity);
    }

    @Transactional
    public ImageEntity createOrUpdateImage(ImageRequest request) {
        ImageEntity image = request.getId() != null
                ? imageRepo.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Image not found with id: " + request.getId()))
                : new ImageEntity();

        updateImageFromRequest(image, request);
        return imageRepo.save(image);
    }

    private void updateImageFromRequest(ImageEntity image, ImageRequest request) {
        image.setName(request.getName());
        if (request.getImageBase64() != null && !request.getImageBase64().isEmpty()) {
            image.setBytes(Base64.getDecoder().decode(request.getImageBase64()));
        }
        // Set other fields as necessary...
    }

    @Transactional
    public ImageEntity getOrCreate(ImageRequest request) {
        return request != null ? createOrUpdateImage(request) : null;
    }
}
