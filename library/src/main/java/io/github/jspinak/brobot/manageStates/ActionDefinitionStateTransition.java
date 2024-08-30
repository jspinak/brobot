package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.dsl.ActionDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class ActionDefinitionStateTransition implements IStateTransition {
    private ActionDefinition actionDefinition;

    private StaysVisible staysVisibleAfterTransition;
    private Set<Long> activate;
    private Set<Long> exit;
    private int score = 0;
    private int timesSuccessful = 0;

    // this class does not use state names
    @Override
    public void convertNamesToIds(Function<String, Long> nameToIdConverter) {
    }
}
