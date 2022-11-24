package io.github.jspinak.brobot.actions.actionExecution.actionLifecycle;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Getter
@Setter
public class ActionLifecylceRepo {

    private Map<Integer, ActionLifecycle> actionLifecycles = new HashMap<>();
    private int lastId = 0;

    public int add(ActionLifecycle actionLifecycle) {
        lastId++;
        actionLifecycles.put(lastId, actionLifecycle);
        return lastId;
    }
}
