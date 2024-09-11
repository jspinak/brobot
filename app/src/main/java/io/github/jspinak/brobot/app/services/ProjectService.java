package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.entities.ProjectEntity;
import io.github.jspinak.brobot.app.database.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
    }

    public ProjectEntity createProject(String name) {
        ProjectEntity project = new ProjectEntity();
        project.setName(name);
        return projectRepository.save(project);
    }

    public ProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
    }

    public ProjectEntity getProjectByName(String name) {
        return projectRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Project not found with name: " + name));
    }

    public void deleteProject(Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        projectRepository.delete(project);
    }

}