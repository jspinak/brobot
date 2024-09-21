package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.services.ProjectService;
import io.github.jspinak.brobot.app.web.requests.ProjectRequest;
import io.github.jspinak.brobot.app.web.responseMappers.ProjectResponseMapper;
import io.github.jspinak.brobot.app.web.responses.ProjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectResponseMapper projectResponseMapper;

    public ProjectController(ProjectService projectService,
                             ProjectResponseMapper projectResponseMapper) {
        this.projectService = projectService;
        this.projectResponseMapper = projectResponseMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        List<ProjectResponse> projects = projectService.getAllProjects()
                .stream()
                .map(projectResponseMapper::entityToResponse)
                .toList();
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest projectRequest) {
        ProjectResponse createdProject = projectResponseMapper.entityToResponse(
                projectService.createProject(projectRequest.getName()));
        return ResponseEntity.ok(createdProject);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        ProjectResponse project = projectResponseMapper.entityToResponse(projectService.getProjectById(id));
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

}