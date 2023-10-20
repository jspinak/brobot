package io.github.jspinak.brobot.testingAUTs.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(indexName = "actionlog")
@Getter
@Setter
public class ActionLog {

    @Id
    private String id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ActionOptions.Action action;
    private boolean success;
    private Set<String> images = new HashSet<>();
    private Set<StateEnum> ownerStates = new HashSet<>(); // states that own the objects used for the action (all objects, not just images)

    public ActionLog(LocalDateTime startTime,
                     LocalDateTime endTime,
                     Matches matches,
                     ActionOptions actionOptions,
                     ObjectCollection... objectCollections) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.action = actionOptions.getAction();
        this.success = matches.isSuccess();
        for (ObjectCollection objColl : objectCollections) {
            images.addAll(objColl.getAllImageFilenames());
            ownerStates.addAll(objColl.getAllOwnerStates());
        }
    }

    // Method to serialize an instance to a JSON string
    public String toJson() {
        // Create an ObjectMapper and register the JavaTimeModule
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            // Handle the exception or log it
            e.printStackTrace();
            return "{}"; // Return an empty JSON object as a fallback
        }
    }
}
