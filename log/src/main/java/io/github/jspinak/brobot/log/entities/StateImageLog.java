package io.github.jspinak.brobot.log.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class StateImageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String stateOwnerName;

    @ElementCollection
    @CollectionTable(name = "state_image_log_images", joinColumns = @JoinColumn(name = "state_image_log_id"))
    @Column(name = "image_base64", columnDefinition = "TEXT")
    private List<String> imagesBase64 = new ArrayList<>();

    private boolean found;

    @Override
    public String toString() {
        return "StateImageLog{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", stateOwnerName='" + stateOwnerName + '\'' +
                ", found=" + found +
                '}';
    }
}
