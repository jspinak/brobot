package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.database.entities.ActionDefinitionEntity;
import io.github.jspinak.brobot.app.services.ActionDefinitionService;
import io.github.jspinak.brobot.app.web.requests.ActionDefinitionRequest;
import io.github.jspinak.brobot.app.web.requests.ActionStepRequest;
import io.github.jspinak.brobot.app.web.responseMappers.ActionDefinitionResponseMapper;
import io.github.jspinak.brobot.app.web.responses.ActionDefinitionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/action-definitions")
public class ActionDefinitionController {

    private final ActionDefinitionService actionDefinitionService;
    private final ActionDefinitionResponseMapper actionDefinitionResponseMapper;

    public ActionDefinitionController(
            ActionDefinitionService actionDefinitionService,
            ActionDefinitionResponseMapper actionDefinitionResponseMapper) {
        this.actionDefinitionService = actionDefinitionService;
        this.actionDefinitionResponseMapper = actionDefinitionResponseMapper;
    }

    @PostMapping
    public ResponseEntity<ActionDefinitionResponse> createActionDefinition(
            @RequestBody ActionDefinitionRequest request) {
        try {
            ActionDefinitionEntity entity = actionDefinitionResponseMapper.fromRequest(request);
            ActionDefinitionEntity savedEntity = actionDefinitionService.save(entity);
            return ResponseEntity.ok(actionDefinitionResponseMapper.toResponse(savedEntity));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActionDefinitionResponse> getActionDefinition(@PathVariable Long id) {
        try {
            ActionDefinitionEntity entity = actionDefinitionService.findById(id);
            if (entity != null) {
                return ResponseEntity.ok(actionDefinitionResponseMapper.toResponse(entity));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ActionDefinitionResponse>> getProjectActionDefinitions(
            @PathVariable Long projectId) {
        try {
            List<ActionDefinitionEntity> entities = actionDefinitionService
                    .findByProjectId(projectId);
            List<ActionDefinitionResponse> responses = entities.stream()
                    .map(actionDefinitionResponseMapper::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActionDefinition(@PathVariable Long id) {
        try {
            actionDefinitionService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/steps")
    public ResponseEntity<ActionDefinitionResponse> updateActionDefinitionSteps(
            @PathVariable Long id,
            @RequestBody List<ActionStepRequest> steps) {
        try {
            ActionDefinitionEntity entity = actionDefinitionService.updateSteps(id, steps);
            return ResponseEntity.ok(actionDefinitionResponseMapper.toResponse(entity));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
