package io.github.jspinak.brobot.testingAUTs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
public class ActionLogInfo {

    private int actionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ActionOptions.Action action;
    private boolean success;
    private Set<String> images = new HashSet<>();
    private Set<StateEnum> ownerStates = new HashSet<>(); // states that own the objects used for the action (all objects, not just images)

    @JsonCreator
    public ActionLogInfo(@JsonProperty("actionId") int actionId,
                         @JsonProperty("startTime") LocalDateTime startTime,
                         @JsonProperty("endTime") LocalDateTime endTime,
                         @JsonProperty("matches") Matches matches,
                         @JsonProperty("actionOptions") ActionOptions actionOptions,
                         @JsonProperty("objectCollections") ObjectCollection... objectCollections) {
        this.actionId = actionId;
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
