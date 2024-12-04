package io.github.jspinak.brobot.app.log;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.services.ImageService;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class StateImageDTOMapper {

    private final ImageService imageService;

    public StateImageDTOMapper(ImageService imageService) {
        this.imageService = imageService;
    }

    public StateImageDTO toDTO(StateImageEntity entity) {
        StateImageDTO dto = new StateImageDTO();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProjectId());
        dto.setName(entity.getName());
        dto.setStateOwnerName(entity.getOwnerStateName());

        // Convert all pattern images to Base64
        for (PatternEntity pattern : entity.getPatterns()) {
            Optional<ImageEntity> imageEntity = imageService.getImageEntity(pattern.getImageId());
            imageEntity.ifPresent(image -> {
                String base64Image = Base64.getEncoder().encodeToString(image.getImageData());
                dto.getImagesBase64().add(base64Image);
            });
        }

        return dto;
    }

    public List<StateImageDTO> toDTOList(List<StateImageEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}