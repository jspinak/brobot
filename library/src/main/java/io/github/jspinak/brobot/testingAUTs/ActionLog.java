package io.github.jspinak.brobot.testingAUTs;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ActionLog {

    private String id;
    //private LocalDateTime startTime;
    //private LocalDateTime endTime;
    private String action;
    private boolean success;
    private Set<String> images = new HashSet<>();
    private Set<String> ownerStates = new HashSet<>(); // states that own the objects used for the action (all objects, not just images)

    public ActionLog() {}
    public ActionLog(//LocalDateTime startTime,
                     //LocalDateTime endTime,
                     String action, boolean success, Set<String> images, Set<String> ownerStates
                     ) {
        //this.startTime = startTime;
        //this.endTime = endTime;
        this.action = action;
        this.success = success;
        this.images = images;
        this.ownerStates = ownerStates;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ActionLog{" +
                "id='" + id + '\'' +
                ", action='" + action + '\'' +
                ", images={");
        int i=0;
        for (String str : images) {
            stringBuilder.append("'" + str + "'");
            i++;
            if (i<images.size()) stringBuilder.append(", ");
        }
        stringBuilder.append("}");
        stringBuilder.append(", ownerStates={");
        i=0;
        for (String str : ownerStates) {
            stringBuilder.append("'" + str + "'");
            i++;
            if (i<images.size()) stringBuilder.append(", ");
        }
        stringBuilder.append("}");
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
