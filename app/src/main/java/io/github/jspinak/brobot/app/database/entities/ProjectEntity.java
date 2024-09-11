package io.github.jspinak.brobot.app.database.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "projects")
@Data
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = 0L;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<StateEntity> states = new HashSet<>();

    // Helper method to add a state
    public void addState(StateEntity state) {
        states.add(state);
        state.setProject(this);
    }

    // Helper method to remove a state
    public void removeState(StateEntity state) {
        states.remove(state);
        state.setProject(null);
    }

    // Remove the states from toString, equals, and hashCode methods
    @Override
    public String toString() {
        return "ProjectEntity(id=" + id + ", name=" + name + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectEntity)) return false;
        ProjectEntity that = (ProjectEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}